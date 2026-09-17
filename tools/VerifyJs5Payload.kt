package com.opennxt.verification

import com.opennxt.OpenNXT
import com.opennxt.filesystem.Container
import com.opennxt.filesystem.Js5ContainerPayload
import com.opennxt.filesystem.compression.ContainerCompression
import com.opennxt.filesystem.flat.FlatFilesystem
import com.opennxt.net.PreLoginForensics
import com.opennxt.net.http.endpoints.Js5MsEndpoint
import com.opennxt.net.js5.Js5Session
import com.opennxt.net.js5.packet.Js5Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.embedded.EmbeddedChannel
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ReadOnlyBufferException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32

/** Standalone checks using only the distribution's compiler and runtime; no JUnit or downloads. */
object VerifyJs5Payload {
    private var checks = 0

    @JvmStatic
    fun main(args: Array<String>) {
        val scratch = if (args.isNotEmpty()) Path.of(args[0]) else Files.createTempDirectory("js5-payload-check-")
        Files.createDirectories(scratch)
        PreLoginForensics.transportEventsPathOverride = scratch.resolve("transport-events.jsonl")
        checkNormalization()
        checkMalformedContainers()
        checkReferenceAndChecksumPaths(scratch.resolve("reference-cache"))
        checkContinuationAndXor(scratch.resolve("continuation-cache"))
        checkPriorityBudget(scratch.resolve("priority-cache"))
        println("PASS: $checks JS5/HTTP payload checks, including opaque compression, buffer state, malformed lengths, index 255, continuation and XOR")
    }

    private fun expect(condition: Boolean, message: String) {
        checks++
        check(condition) { message }
    }

    private fun bytesEqual(expected: ByteArray, actual: ByteArray, message: String) =
        expect(expected.contentEquals(actual), "$message (expected ${expected.size} bytes, got ${actual.size})")

    private fun ByteBuffer.bytes(): ByteArray = ByteArray(remaining()).also(duplicate()::get)
    private fun crc(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value

    private fun container(compression: Int, payload: ByteArray = byteArrayOf(74, 65, 71, 65, 0, 15), expanded: Int = 100): ByteArray {
        val header = if (compression == 0) 5 else 9
        return ByteBuffer.allocate(header + payload.size).apply {
            put(compression.toByte())
            putInt(payload.size)
            if (compression != 0) putInt(expanded)
            put(payload)
        }.array()
    }

    private fun views(bytes: ByteArray): List<ByteBuffer> {
        val positioned = ByteBuffer.wrap(byteArrayOf(91, 92) + bytes + byteArrayOf(93, 94))
            .order(ByteOrder.LITTLE_ENDIAN).apply { position(2); limit(2 + bytes.size) }
        val direct = ByteBuffer.allocateDirect(bytes.size + 4).apply {
            put(byteArrayOf(91, 92)); put(bytes); put(byteArrayOf(93, 94))
            position(2); limit(2 + bytes.size)
        }
        return listOf(
            ByteBuffer.wrap(bytes.copyOf()),
            positioned,
            positioned.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN),
            positioned.slice().order(ByteOrder.LITTLE_ENDIAN),
            positioned.slice().asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN),
            direct.order(ByteOrder.LITTLE_ENDIAN),
            direct.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN),
        )
    }

    private fun untouched(raw: ByteBuffer, action: () -> Unit) {
        val position = raw.position()
        val limit = raw.limit()
        val order = raw.order()
        val contents = raw.duplicate().apply { clear() }.bytes()
        raw.mark()
        try {
            action()
        } finally {
            expect(raw.position() == position, "Source position changed")
            expect(raw.limit() == limit, "Source limit changed")
            expect(raw.order() == order, "Source byte order changed")
            expect(raw.reset().position() == position, "Source mark changed")
            bytesEqual(contents, raw.duplicate().apply { clear() }.bytes(), "Source contents changed")
        }
    }

    private fun checkNormalization() {
        withSession { session, _ ->
            for (compression in 0..3) {
                // Expanded MAX_VALUE and deliberately opaque bytes prove this path does
                // not decompress, allocate the expanded body, or inspect compressed data.
                val wire = container(compression, expanded = Int.MAX_VALUE)
                for (trailer in listOf(byteArrayOf(), byteArrayOf(0, 15), byteArrayOf(-1, -1))) {
                    for (raw in views(wire + trailer)) {
                        untouched(raw) {
                            val result = Js5ContainerPayload.archiveBody(raw)
                            expect(result.position() == 0 && result.limit() == wire.size, "Incorrect result bounds")
                            expect(result.isReadOnly, "Helper result must be read-only")
                            expect(result.order() == ByteOrder.BIG_ENDIAN, "Helper result must be big-endian")
                            bytesEqual(wire, result.bytes(), "Helper changed wire bytes")
                            bytesEqual(wire, session.formatArchivePayload(47, raw), "JS5 changed wire bytes")
                            bytesEqual(wire, Js5MsEndpoint.archiveHttpBody(raw).bytes(), "HTTP changed wire bytes")
                            expect(crc(wire) == crc(result.bytes()), "Archive CRC changed")
                            var rejected = false
                            try { result.put(0, 1) } catch (_: ReadOnlyBufferException) { rejected = true }
                            expect(rejected, "Returned view permits writing")
                        }
                    }
                }
                val empty = container(compression, byteArrayOf(), expanded = 0)
                bytesEqual(empty, session.formatArchivePayload(47, ByteBuffer.wrap(empty + byteArrayOf(0, 0))), "Empty container boundary")
            }
            val payload = ByteArray(16 * 1024) { ((it * 31) xor (it shr 3)).toByte() }
            for (compression in ContainerCompression.values()) {
                val stored = Container(payload, compression, 65535).compress().bytes()
                expect(Container.decode(ByteBuffer.wrap(stored)).version == 65535, "Real compressed fixture version")
                bytesEqual(stored.copyOf(stored.size - 2), session.formatArchivePayload(47, ByteBuffer.wrap(stored)), "Real compressed container bytes")
            }
        }
    }

    private fun checkMalformedContainers() {
        val invalid = mutableListOf<ByteArray>()
        for (compression in 0..3) {
            val wire = container(compression)
            val header = if (compression == 0) 5 else 9
            for (length in 0 until header) invalid += wire.copyOf(length)
            for (length in listOf(-1, Int.MIN_VALUE, Int.MAX_VALUE, Int.MAX_VALUE - header + 1)) {
                invalid += wire.copyOf().also { ByteBuffer.wrap(it).putInt(1, length) }
            }
            invalid += wire.copyOf(wire.size - 1)
            invalid += wire + byteArrayOf(1)
            invalid += wire + byteArrayOf(1, 2, 3)
            invalid += wire + byteArrayOf(1, 2, 3, 4)
            if (compression != 0) invalid += wire.copyOf().also { ByteBuffer.wrap(it).putInt(5, -1) }
        }
        for (compression in listOf(4, 127, 128, 255)) {
            invalid += container(0).also { it[0] = compression.toByte() }
        }
        withSession { session, _ ->
            for ((case, bytes) in invalid.withIndex()) for (raw in views(bytes)) {
                untouched(raw) {
                    for (normalize in listOf<(ByteBuffer) -> Any>(
                        { Js5ContainerPayload.archiveBody(it) },
                        { session.formatArchivePayload(47, it) },
                        { Js5MsEndpoint.archiveHttpBody(it) },
                    )) {
                        var rejected = false
                        try { normalize(raw) } catch (_: IllegalArgumentException) { rejected = true }
                        expect(rejected, "Malformed container $case was accepted")
                    }
                }
            }
        }
    }

    private fun checkReferenceAndChecksumPaths(directory: Path) {
        // Index 255 deliberately bypasses normal archive validation and trimming.
        val reference = container(0, byteArrayOf(6, 0, 15)) + byteArrayOf(0, 99)
        withFilesystem(directory, emptyMap(), reference) { session, _ ->
            for (raw in views(reference)) untouched(raw) {
                bytesEqual(reference, session.formatArchivePayload(255, raw), "Reference trailer was stripped")
            }
            bytesEqual(byteArrayOf(-1, 2, 3), session.formatArchivePayload(255, ByteBuffer.wrap(byteArrayOf(-1, 2, 3))), "Reference path was normalized")
            val resolved = Js5MsEndpoint.resolvePayload(255, 47) ?: error("Missing reference fixture")
            expect(resolved.kind == "reference-table", "Reference response kind")
            bytesEqual(reference, resolved.data.bytes(), "HTTP reference was normalized")
            val raw = session.loadFileData(Js5Packet.RequestFile(true, 255, 47, 947)) ?: error("Missing JS5 reference")
            try { bytesEqual(reference, ByteArray(raw.readableBytes()).also(raw::readBytes), "JS5 reference was normalized") } finally { raw.release() }
            val checksum = byteArrayOf(42, 1, 2, 3)
            OpenNXT.checksumTable = checksum
            OpenNXT.httpChecksumTable = checksum
            bytesEqual(checksum, Js5MsEndpoint.resolvePayload(255, 255)!!.data.bytes(), "HTTP checksum changed")
            val master = session.loadFileData(Js5Packet.RequestFile(true, 255, 255, 947)) ?: error("Missing JS5 checksum")
            try { bytesEqual(checksum, ByteArray(master.readableBytes()).also(master::readBytes), "JS5 checksum changed") } finally { master.release() }
        }
    }

    private fun checkContinuationAndXor(directory: Path) {
        val wire = container(0, ByteArray(2 * 102395 + 32) { (it * 31).toByte() })
        withFilesystem(directory, mapOf(700 to (wire + byteArrayOf(0, 15)))) { session, channel ->
            channel.attr(Js5Session.XOR_KEY).set(0x5a)
            session.enqueueRequest(Js5Packet.RequestFile(true, 47, 700, 947), 17)
            val reconstructed = ByteArrayOutputStream()
            for (payloadLength in listOf(102395, 102395, 37)) {
                expect(session.process(97) == payloadLength + 5, "Native block size changed")
                val block = drain(channel)
                val decoded = ByteBuffer.wrap(ByteArray(block.size) { (block[it].toInt() xor 0x5a).toByte() })
                expect(decoded.get().toInt() == 47 && decoded.int == 700, "Continuation/XOR header changed")
                expect(decoded.remaining() == payloadLength, "Continuation payload boundary changed")
                reconstructed.write(decoded.bytes())
            }
            expect(session.process(97) == 0, "Response did not finish")
            bytesEqual(wire, reconstructed.toByteArray(), "Continuation/XOR changed archive bytes")
        }
    }

    private fun checkPriorityBudget(directory: Path) {
        val high = container(0, ByteArray(20) { 11 })
        val low = container(0, ByteArray(45) { 22 })
        withFilesystem(directory, mapOf(1 to high, 2 to low)) { session, channel ->
            session.enqueueRequest(Js5Packet.RequestFile(true, 47, 1, 947), 17)
            session.enqueueRequest(Js5Packet.RequestFile(false, 47, 2, 947), 16)
            val executor = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "js5-budget-check").apply { isDaemon = true } }
            try { expect(executor.submit<Int> { session.process(31) }.get(2, TimeUnit.SECONDS) == 85, "Priority budget response length") }
            finally { executor.shutdownNow() }
            val wire = ByteBuffer.wrap(drain(channel))
            expect(wire.get().toInt() == 47 && wire.int == 1, "High priority header")
            bytesEqual(high, ByteArray(high.size).also(wire::get), "High priority body")
            expect(wire.get().toInt() == 47 && wire.int == (Int.MIN_VALUE or 2), "Low priority header")
            bytesEqual(low, ByteArray(low.size).also(wire::get), "Low priority body")
            expect(wire.remaining() == 0 && session.process(31) == 0, "Priority response did not finish")
        }
    }

    private fun withSession(action: (Js5Session, EmbeddedChannel) -> Unit) {
        val channel = EmbeddedChannel()
        val session = Js5Session(channel)
        session.updateLoggedInState(true)
        try { action(session, channel) }
        finally { session.close(); channel.finishAndReleaseAll() }
    }

    private fun withFilesystem(directory: Path, archives: Map<Int, ByteArray>, reference: ByteArray = container(0, byteArrayOf(6)), action: (Js5Session, EmbeddedChannel) -> Unit) {
        Files.createDirectories(directory.resolve("255"))
        Files.createDirectories(directory.resolve("47"))
        Files.write(directory.resolve("255/47.dat"), reference)
        archives.forEach { (id, bytes) -> Files.write(directory.resolve("47/$id.dat"), bytes) }
        val previous = runCatching { OpenNXT.filesystem }.getOrNull()
        FlatFilesystem(directory).use { filesystem ->
            OpenNXT.filesystem = filesystem
            try { withSession(action) }
            finally { if (previous != null) OpenNXT.filesystem = previous }
        }
    }

    private fun drain(channel: EmbeddedChannel): ByteArray {
        val result = ByteArrayOutputStream()
        while (true) {
            val buffer = channel.readOutbound<ByteBuf>() ?: break
            try { result.write(ByteArray(buffer.readableBytes()).also(buffer::readBytes)) }
            finally { buffer.release() }
        }
        return result.toByteArray()
    }
}
