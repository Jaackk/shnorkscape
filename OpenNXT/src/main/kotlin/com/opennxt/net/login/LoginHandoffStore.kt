package com.opennxt.net.login

import com.opennxt.model.Build
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

object LoginHandoffStore {
    data class LobbySnapshot(
        val build: Build,
        val username: String,
        val password: String,
        val remaining: ByteArray
    )

    private val latestByHost = ConcurrentHashMap<String, LobbySnapshot>()
    private data class GuestHandoff(val snapshot: LobbySnapshot, val expires: Long)
    private val guests = ConcurrentHashMap<String, GuestHandoff>()

    private fun guestKey(remote: SocketAddress?, keys: IntArray): String? {
        if (keys.size != 4 || keys.all { it == 0 }) return null
        val host = hostKey(remote) ?: return null
        val bytes = java.nio.ByteBuffer.allocate(16).apply { keys.forEach { putInt(it) } }.array()
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
        return host + ":" + java.util.Base64.getEncoder().encodeToString(digest)
    }

    /** Called only after successful account authentication, never from raw packet decoding. */
    @Synchronized
    fun rememberGuest(remote: SocketAddress?, build: Build, username: String, password: String,
                      keys: IntArray, remaining: ByteArray) {
        val key = guestKey(remote, keys) ?: throw IllegalArgumentException("Invalid guest session keys")
        val now = System.nanoTime()
        guests.entries.removeIf { it.value.expires < now }
        check(guests.size < 128 && !guests.containsKey(key)) { "Guest handoff limit or reused session keys" }
        guests[key] = GuestHandoff(LobbySnapshot(build, username, password, remaining.copyOf()), now + 120_000_000_000L)
    }

    fun recall(remote: SocketAddress?, header: LoginRSAHeader): LobbySnapshot? {
        if (com.opennxt.security.NativeLanAccess.loopback(remote)) return recall(remote)
        if (header !is LoginRSAHeader.Reconnecting) return null
        val key = guestKey(remote, header.oldSeeds) ?: return null
        val handoff = guests.remove(key) ?: return null
        return handoff.snapshot.takeIf { System.nanoTime() <= handoff.expires }
    }

    fun remember(remoteAddress: SocketAddress?, request: LoginPacket.LobbyLoginRequest) {
        val key = hostKey(remoteAddress) ?: return

        request.remaining.markReaderIndex()
        val remaining = ByteArray(request.remaining.readableBytes())
        request.remaining.readBytes(remaining)
        request.remaining.resetReaderIndex()

        if (!com.opennxt.security.NativeLanAccess.loopback(remoteAddress)) {
            rememberGuest(remoteAddress, request.build, request.username, request.password, request.header.seeds, remaining)
            return
        }
        latestByHost[key] = LobbySnapshot(
            build = request.build,
            username = request.username,
            password = request.password,
            remaining = remaining
        )
    }

    fun recall(remoteAddress: SocketAddress?): LobbySnapshot? {
        if (!com.opennxt.security.NativeLanAccess.loopback(remoteAddress)) return null
        val key = hostKey(remoteAddress) ?: return null
        return latestByHost[key]
    }

    private fun hostKey(remoteAddress: SocketAddress?): String? {
        val address = remoteAddress as? InetSocketAddress ?: return remoteAddress?.toString()
        return address.address?.hostAddress ?: address.hostString
    }
}
