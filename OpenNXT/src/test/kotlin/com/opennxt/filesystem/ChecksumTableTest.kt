package com.opennxt.filesystem

import com.opennxt.filesystem.flat.FlatFilesystem
import org.junit.jupiter.api.io.TempDir
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ChecksumTableTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `default HTTP table retains 41 entries with only index 40 populated`() {
        createFilesystem().use { filesystem ->
            val table = ChecksumTable.create(filesystem, http = true)

            assertEquals(41, table.entries.size)
            assertEquals(listOf(40), populatedIndices(table))
            assertEquals(1040, table.entries[40].version)
        }
    }

    @Test
    fun `native HTTP table advertises index 56 with the full filesystem index count`() {
        createFilesystem().use { filesystem ->
            val rawTable = ChecksumTable.create(filesystem, http = false)
            val table = ChecksumTable.create(filesystem, http = true, httpIndices = setOf(40, 56))

            assertEquals(67, table.entries.size)
            assertEquals(listOf(40, 56), populatedIndices(table))
            assertEquals(rawTable.entries[40], table.entries[40])
            assertEquals(rawTable.entries[56], table.entries[56])
            assertNotEquals(ChecksumTable.TableEntry.EMPTY, rawTable.entries[66])

            val encoded = table.encode(BigInteger.ONE.shiftLeft(1024).subtract(BigInteger.ONE), BigInteger.ONE)
            assertEquals(67, encoded[0].toInt() and 0xff)
            assertEquals(1 + 67 * 80 + 128, encoded.size)
            assertEquals(table, ChecksumTable.decode(ByteBuffer.wrap(encoded)))
        }
    }

    @Test
    fun `native HTTP table leaves a missing index 56 empty`() {
        createFilesystem(includeIndex56 = false).use { filesystem ->
            val table = ChecksumTable.create(filesystem, http = true, httpIndices = setOf(40, 56))

            assertEquals(67, table.entries.size)
            assertEquals(listOf(40), populatedIndices(table))
            assertEquals(ChecksumTable.TableEntry.EMPTY, table.entries[56])
        }
    }

    private fun createFilesystem(includeIndex56: Boolean = true): FlatFilesystem {
        val directory = Files.createDirectories(tempDir.resolve("255"))
        val indices = if (includeIndex56) listOf(1, 40, 56, 66) else listOf(1, 40, 66)
        indices.forEach { index ->
            val reference = ByteBuffer.allocate(8)
                .put(7.toByte())
                .putInt(1000 + index)
                .put(0.toByte())
                .putShort(0.toShort())
                .array()
            Files.write(directory.resolve("$index.dat"), Container.wrap(reference).array())
        }
        return FlatFilesystem(tempDir)
    }

    private fun populatedIndices(table: ChecksumTable): List<Int> =
        table.entries.indices.filter { table.entries[it] != ChecksumTable.TableEntry.EMPTY }
}
