package com.opennxt.filesystem.flat

import com.opennxt.ext.toFilesystemHash
import com.opennxt.filesystem.Container
import com.opennxt.filesystem.compression.ContainerCompression
import com.opennxt.filesystem.openFilesystem
import org.junit.jupiter.api.io.TempDir
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlatFilesystemTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `factory opens sparse flat cache without creating database files`() {
        writeFile("255/66.dat", Container.wrap(byteArrayOf(6)).array())
        writeFile("255/255.dat", byteArrayOf(0))
        writeFile("255/notes.dat", byteArrayOf(0))
        val raw = Container(byteArrayOf(1, 2, 3), ContainerCompression.NONE, 321).compress().array()
        writeFile("66/123.dat", raw)

        val filesystem = assertIs<FlatFilesystem>(openFilesystem(tempDir))
        assertEquals(67, filesystem.numIndices())
        assertTrue(filesystem.exists(66, 123))
        assertFalse(filesystem.exists(66, 124))
        assertContentEquals(raw, filesystem.read(66, 123).requireBytes())
        assertEquals(321, Container.decode(assertNotNull(filesystem.read(66, 123))).version)
        assertNull(filesystem.read(0, 1))
        assertNull(filesystem.readReferenceTable(0))
        assertFailsWith<IndexOutOfBoundsException> { filesystem.readReferenceTable(67) }
        assertFalse(Files.exists(tempDir.resolve("js5-0.jcache")))
    }

    @Test
    fun `named reads use the flat reference table`() {
        // Format 6, names enabled, one archive (5) with one file (0).
        val reference = ByteBuffer.allocate(30)
            .put(6.toByte()).putInt(17).put(1.toByte()).putShort(1.toShort())
            .putShort(5.toShort()).putInt("huffman".toFilesystemHash())
            .putInt(0).putInt(9).putShort(1.toShort()).putShort(0.toShort()).putInt(0)
            .array()
        val rawReference = Container.wrap(reference).array()
        val rawArchive = Container.wrap(byteArrayOf(4, 5, 6)).array()
        writeFile("255/10.dat", rawReference)
        writeFile("10/5.dat", rawArchive)

        FlatFilesystem(tempDir).use { filesystem ->
            assertContentEquals(rawReference, filesystem.readReferenceTable(10).requireBytes())
            assertEquals(17, filesystem.getReferenceTable(10)?.version)
            assertContentEquals(rawArchive, filesystem.read(10, "huffman").requireBytes())
            assertNull(filesystem.read(10, "missing"))
        }
    }

    @Test
    fun `all writes fail and preserve the original bytes`() {
        val original = Container.wrap(byteArrayOf(1, 2, 3)).array()
        writeFile("255/0.dat", original)
        writeFile("0/1.dat", original)
        FlatFilesystem(tempDir).use { filesystem ->
            assertFailsWith<UnsupportedOperationException> { filesystem.createIndex(1) }
            assertFailsWith<UnsupportedOperationException> { filesystem.write(0, 1, Container(byteArrayOf(9))) }
            assertFailsWith<UnsupportedOperationException> { filesystem.write(0, 1, byteArrayOf(9), 1, 2) }
            assertFailsWith<UnsupportedOperationException> { filesystem.writeReferenceTable(0, Container(byteArrayOf(9))) }
            assertFailsWith<UnsupportedOperationException> { filesystem.writeReferenceTable(0, byteArrayOf(9), 1, 2) }
        }
        assertContentEquals(original, Files.readAllBytes(tempDir.resolve("255/0.dat")))
        assertContentEquals(original, Files.readAllBytes(tempDir.resolve("0/1.dat")))
        assertFalse(Files.exists(tempDir.resolve("1")))
    }

    private fun writeFile(relative: String, bytes: ByteArray) {
        val destination = tempDir.resolve(relative)
        Files.createDirectories(destination.parent)
        Files.write(destination, bytes)
    }

    private fun ByteBuffer?.requireBytes(): ByteArray {
        val buffer = assertNotNull(this).duplicate()
        return ByteArray(buffer.remaining()).also(buffer::get)
    }
}
