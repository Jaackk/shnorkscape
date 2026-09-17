package com.opennxt.filesystem.flat

import com.opennxt.ext.toFilesystemHash
import com.opennxt.filesystem.Container
import com.opennxt.filesystem.Filesystem
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/** Reads an extracted OpenRS2 flat-file cache without modifying its source files. */
class FlatFilesystem(path: Path) : Filesystem(path), Closeable {
    private val indexCount = referenceTableIds(path).maxOrNull()?.plus(1)
        ?: throw IllegalArgumentException("No OpenRS2 reference tables found in ${path.resolve("255")}")

    companion object {
        fun isFlatCache(path: Path): Boolean = referenceTableIds(path).isNotEmpty()

        private fun referenceTableIds(path: Path): List<Int> {
            val directory = path.resolve("255")
            if (!Files.isDirectory(directory)) return emptyList()
            return Files.newDirectoryStream(directory, "*.dat").use { entries ->
                entries.mapNotNull { entry ->
                    entry.fileName.toString().removeSuffix(".dat").toIntOrNull()
                        ?.takeIf { it in 0..254 && Files.isRegularFile(entry) }
                }
            }
        }
    }

    override fun numIndices(): Int = indexCount

    override fun exists(index: Int, archive: Int): Boolean =
        Files.isRegularFile(archivePath(index, archive))

    override fun read(index: Int, archive: Int): ByteBuffer? = readRaw(archivePath(index, archive))

    override fun read(index: Int, name: String): ByteBuffer? {
        validateIndex(index)
        val table = getReferenceTable(index) ?: return null
        val hash = name.toFilesystemHash()
        val archive = table.archives.entries.firstOrNull { it.value.name == hash }?.key ?: return null
        return read(index, archive)
    }

    override fun readReferenceTable(index: Int): ByteBuffer? {
        validateIndex(index)
        return readRaw(path.resolve("255").resolve("$index.dat"))
    }

    private fun archivePath(index: Int, archive: Int): Path {
        validateIndex(index)
        require(archive >= 0) { "archive must be non-negative: $archive" }
        return path.resolve(index.toString()).resolve("$archive.dat")
    }

    private fun readRaw(file: Path): ByteBuffer? = try {
        // Preserve container bytes, including optional group version trailers, for CRCs and JS5.
        ByteBuffer.wrap(Files.readAllBytes(file))
    } catch (_: NoSuchFileException) {
        null
    }

    private fun validateIndex(index: Int) {
        if (index !in 0 until indexCount) {
            throw IndexOutOfBoundsException("index out of bounds: $index")
        }
    }

    private fun readOnly(): Nothing =
        throw UnsupportedOperationException("OpenRS2 flat cache is read-only: $path")

    override fun createIndex(id: Int): Unit = readOnly()

    override fun write(index: Int, archive: Int, data: Container): Unit = readOnly()

    override fun write(index: Int, archive: Int, compressed: ByteArray, version: Int, crc: Int): Unit = readOnly()

    override fun writeReferenceTable(index: Int, data: Container): Unit = readOnly()

    override fun writeReferenceTable(index: Int, compressed: ByteArray, version: Int, crc: Int): Unit = readOnly()

    override fun close() = Unit
}
