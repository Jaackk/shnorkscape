package com.opennxt.net.js5

import com.opennxt.OpenNXT
import com.opennxt.filesystem.Container
import com.opennxt.filesystem.flat.FlatFilesystem
import com.opennxt.net.js5.packet.Js5Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.embedded.EmbeddedChannel
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Js5SessionBudgetTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `small scheduling budgets preserve full native continuation blocks`() {
        val raw = Container.wrap(ByteArray(2 * 102395 + 32) { (it * 31).toByte() }).array()
        withSession(mapOf(700 to raw)) { session, channel ->
            channel.attr(Js5Session.XOR_KEY).set(0x5a)
            session.enqueueRequest(Js5Packet.RequestFile(true, 47, 700, 947), 17)

            assertEquals(102400, session.process(97))
            val first = drain(channel)
            assertEquals(102400, session.process(97))
            val second = drain(channel)
            assertEquals(42, session.process(97))
            val last = drain(channel)
            assertEquals(0, session.process(97))

            // Decode each 100KiB wire block using the native fixed boundary. Only
            // the final block may be shorter; scheduling calls are not boundaries.
            val reconstructed = ByteArrayOutputStream()
            for ((block, payloadLength) in listOf(first to 102395, second to 102395, last to 37)) {
                val decoded = ByteBuffer.wrap(ByteArray(block.size) { (block[it].toInt() xor 0x5a).toByte() })
                assertEquals(47, decoded.get().toInt())
                assertEquals(700, decoded.int)
                assertEquals(payloadLength, decoded.remaining())
                reconstructed.write(ByteArray(decoded.remaining()).also(decoded::get))
            }
            assertContentEquals(raw, reconstructed.toByteArray())
        }
    }

    @Test
    fun `one remaining budget byte cannot spin or truncate a lower priority response`() {
        val high = Container.wrap(ByteArray(20) { 11 }).array() // 30 bytes including the wire header.
        val low = Container.wrap(ByteArray(45) { 22 }).array() // 55 bytes including the wire header.
        withSession(mapOf(1 to high, 2 to low)) { session, channel ->
            session.enqueueRequest(Js5Packet.RequestFile(true, 47, 1, 947), 17)
            session.enqueueRequest(Js5Packet.RequestFile(false, 47, 2, 947), 16)

            assertTimeoutPreemptively(Duration.ofSeconds(2), Executable {
                assertEquals(85, session.process(31))
            })
            val wire = ByteBuffer.wrap(drain(channel))
            assertEquals(47, wire.get().toInt())
            assertEquals(1, wire.int)
            assertContentEquals(high, ByteArray(high.size).also(wire::get))
            assertEquals(47, wire.get().toInt())
            assertEquals(Int.MIN_VALUE or 2, wire.int)
            assertContentEquals(low, ByteArray(low.size).also(wire::get))
            assertEquals(0, wire.remaining())
            assertEquals(0, session.process(31))
        }
    }

    private fun withSession(archives: Map<Int, ByteArray>, action: (Js5Session, EmbeddedChannel) -> Unit) {
        Files.createDirectories(tempDir.resolve("255"))
        Files.write(tempDir.resolve("255/47.dat"), Container.wrap(byteArrayOf(6)).array())
        Files.createDirectories(tempDir.resolve("47"))
        archives.forEach { (id, raw) -> Files.write(tempDir.resolve("47/$id.dat"), raw) }
        val previousFilesystem = runCatching { OpenNXT.filesystem }.getOrNull()
        FlatFilesystem(tempDir).use { filesystem ->
            OpenNXT.filesystem = filesystem
            val channel = EmbeddedChannel()
            val session = Js5Session(channel)
            // Keep this test entirely local and avoid any retail fallback.
            session.updateLoggedInState(true)
            try {
                action(session, channel)
            } finally {
                session.close()
                channel.finishAndReleaseAll()
                if (previousFilesystem != null) OpenNXT.filesystem = previousFilesystem
            }
        }
    }

    private fun drain(channel: EmbeddedChannel): ByteArray {
        val result = ByteArrayOutputStream()
        while (true) {
            val buffer = channel.readOutbound<ByteBuf>() ?: break
            try {
                result.write(ByteArray(buffer.readableBytes()).also(buffer::readBytes))
            } finally {
                buffer.release()
            }
        }
        return result.toByteArray()
    }
}
