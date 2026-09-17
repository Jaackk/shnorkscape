package com.opennxt.tools.bundle

import com.opennxt.filesystem.Container
import com.opennxt.filesystem.ReferenceTable
import com.opennxt.filesystem.flat.FlatFilesystem
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/** Creates native 950 cache databases from the read-only OpenRS2 flat cache. */
object PrepareClientCache {
    private val startupIndexes = setOf(2, 3, 10, 12, 13, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 26, 27, 28, 29, 42, 49, 57, 58, 59, 60, 61, 62, 65, 66)
    private const val SCHEMA = "(KEY INTEGER PRIMARY KEY, DATA BLOB, VERSION INTEGER, CRC INTEGER)"
    private const val BATCH_SIZE = 512

    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size in 2..3) {
            "Usage: PrepareClientCache <sourceFlatCache> <destinationRuneScapeDirectory> [startup]"
        }
        val source = Path.of(args[0]).toRealPath()
        val destination = Path.of(args[1]).toAbsolutePath().normalize()
        val mode = args.getOrElse(2) { "startup" }
        require(mode == "startup") { "Only startup mode is supported" }
        require(destination != source && !destination.startsWith(source)) {
            "The native cache destination must be outside the source flat cache"
        }
        val referenceDirectory = source.resolve("255")
        require(Files.isDirectory(referenceDirectory)) { "Missing reference-table directory: $referenceDirectory" }
        val indexes = Files.newDirectoryStream(referenceDirectory, "*.dat").use { entries ->
            entries.mapNotNull { entry -> entry.fileName.toString().removeSuffix(".dat").toIntOrNull()
                ?.takeIf { it in 0..254 && Files.isRegularFile(entry) } }.sorted()
        }
        require(indexes.isNotEmpty()) { "No flat-cache reference tables found" }
        require(indexes.containsAll(startupIndexes)) {
            "Missing required startup indexes: ${startupIndexes - indexes.toSet()}"
        }
        Files.createDirectories(destination)
        val resolvedDestination = destination.toRealPath()
        require(!resolvedDestination.startsWith(source)) { "Destination resolves into the source flat cache" }
        val started = System.nanoTime()
        var inserted = 0L
        var reused = 0L
        var bytes = 0L
        FlatFilesystem(source).use { filesystem ->
            for (index in indexes) {
                val result = prepareIndex(filesystem, source, resolvedDestination, index,
                    index in startupIndexes)
                inserted += result.inserted
                reused += result.reused
                bytes += result.sourceBytes
            }
        }
        println("[Client cache] Complete: mode=$mode indexes=${indexes.size} inserted=$inserted " +
            "reused=$reused sourceBytes=$bytes elapsedMs=${elapsed(started)}")
    }

    private data class Result(val inserted: Int, val reused: Int, val sourceBytes: Long)
    private data class Metadata(val version: Int, val crc: Int)

    private fun prepareIndex(filesystem: FlatFilesystem, source: Path, destination: Path,
                             index: Int, importGroups: Boolean): Result {
        val started = System.nanoTime()
        val rawReference = readSource(source.resolve("255").resolve("$index.dat"))
        val referenceEnd = containerEnd(rawReference, "reference $index")
        val referenceCrc = crc(rawReference, referenceEnd)
        val decodedReference = Container.decode(ByteBuffer.wrap(rawReference)).data
        require(decodedReference.size >= 6 && decodedReference[0].toInt() in 5..7) {
            "Invalid reference-table header: $index"
        }
        val version = if (decodedReference[0].toInt() >= 6)
            ByteBuffer.wrap(decodedReference, 1, 4).int else 0
        val referenceTable = if (importGroups) ReferenceTable(filesystem, index).also {
            it.decode(ByteBuffer.wrap(decodedReference))
        } else null
        val target = destination.resolve("js5-$index.jcache")
        require(!Files.isSymbolicLink(target)) { "Refusing a symlink cache database: $target" }
        val existed = Files.exists(target, LinkOption.NOFOLLOW_LINKS)
        require(!existed || Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            "Native cache path is not a regular file: $target"
        }
        val working = if (existed) target else destination.resolve(".js5-$index-${UUID.randomUUID()}.seed.tmp")
        var inserted = 0
        var reused = 0
        var sourceBytes = 0L
        var completed = false
        try {
            DriverManager.getConnection("jdbc:sqlite:$working").use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("PRAGMA busy_timeout=1000")
                    if (!existed) {
                        statement.execute("CREATE TABLE cache $SCHEMA")
                        statement.execute("CREATE TABLE cache_index $SCHEMA")
                    }
                }
                validateSchema(connection, target)
                connection.autoCommit = false
                try {
                    val referenceMetadata = connection.prepareStatement(
                        "SELECT VERSION,CRC FROM cache_index WHERE KEY=1").use { statement ->
                        statement.executeQuery().use { rows ->
                            if (rows.next()) Metadata(rows.getInt(1), rows.getInt(2)) else null
                        }
                    }
                    if (referenceMetadata != Metadata(version, referenceCrc + 1)) {
                        val blob = nativeReference(decodedReference)
                        connection.prepareStatement("INSERT INTO cache_index(KEY,DATA,VERSION,CRC) VALUES(1,?,?,?) " +
                            "ON CONFLICT(KEY) DO UPDATE SET DATA=excluded.DATA,VERSION=excluded.VERSION,CRC=excluded.CRC").use {
                            it.setBytes(1, blob)
                            it.setInt(2, version)
                            it.setInt(3, referenceCrc + 1)
                            it.executeUpdate()
                        }
                    }
                    if (referenceTable != null) {
                        val existing = HashMap<Int, Metadata>()
                        connection.prepareStatement("SELECT KEY,VERSION,CRC FROM cache").use { statement ->
                            statement.executeQuery().use { rows ->
                                while (rows.next()) existing[rows.getInt(1)] = Metadata(rows.getInt(2), rows.getInt(3))
                            }
                        }
                        connection.prepareStatement("INSERT INTO cache(KEY,DATA,VERSION,CRC) VALUES(?,?,?,?) " +
                            "ON CONFLICT(KEY) DO UPDATE SET DATA=excluded.DATA,VERSION=excluded.VERSION,CRC=excluded.CRC").use { statement ->
                            var pending = 0
                            for ((group, archive) in referenceTable.archives) {
                                val previous = existing[group]
                                if (previous?.version == archive.version && previous.crc == archive.crc + 1) {
                                    reused++
                                    continue
                                }
                                val file = source.resolve(index.toString()).resolve("$group.dat")
                                val raw = readSource(file)
                                val end = containerEnd(raw, "group $index:$group")
                                require(crc(raw, end) == archive.crc) { "CRC mismatch in flat cache group $index:$group: $file" }
                                val native = nativeReference(nativeGroupPayload(
                                    Container.decode(ByteBuffer.wrap(raw)).data, archive.files.size))
                                statement.setInt(1, group)
                                statement.setBytes(2, native)
                                statement.setInt(3, archive.version)
                                statement.setInt(4, archive.crc + 1)
                                statement.addBatch()
                                pending++
                                inserted++
                                sourceBytes += raw.size
                                if (pending == BATCH_SIZE) {
                                    statement.executeBatch()
                                    statement.clearBatch()
                                    pending = 0
                                }
                            }
                            if (pending != 0) statement.executeBatch()
                        }
                    }
                    connection.commit()
                } catch (failure: Throwable) {
                    runCatching { connection.rollback() }.onFailure { failure.addSuppressed(it) }
                    throw failure
                }
            }
            if (!existed) {
                // Same-directory rename publishes a complete database. Do not replace a file
                // that appeared meanwhile: callers also hold the client-launch mutex.
                Files.move(working, target)
            }
            completed = true
        } finally {
            if (!existed && !completed) {
                Files.deleteIfExists(working)
                Files.deleteIfExists(Path.of("$working-journal"))
            }
        }
        println("[Client cache] index=$index groups=${referenceTable?.archives?.size ?: 0} " +
            "inserted=$inserted reused=$reused sourceBytes=$sourceBytes elapsedMs=${elapsed(started)}")
        return Result(inserted, reused, sourceBytes)
    }

    private fun validateSchema(connection: Connection, target: Path) {
        for (table in listOf("cache", "cache_index")) {
            val actual = mutableListOf<Triple<String, String, Int>>()
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info($table)").use { rows ->
                    while (rows.next()) actual += Triple(rows.getString("name").uppercase(),
                        rows.getString("type").uppercase(), rows.getInt("pk"))
                }
            }
            require(actual == listOf(Triple("KEY", "INTEGER", 1), Triple("DATA", "BLOB", 0),
                Triple("VERSION", "INTEGER", 0), Triple("CRC", "INTEGER", 0))) {
                "Unexpected native cache schema in $target ($table); existing data was preserved"
            }
        }
    }

    private fun containerEnd(raw: ByteArray, description: String): Int {
        require(raw.size >= 5 && raw[0].toInt() in 0..3) { "Invalid JS5 container: $description" }
        val size = ByteBuffer.wrap(raw, 1, 4).int
        val header = if (raw[0].toInt() == 0) 5 else 9
        require(raw.size >= header && (header == 5 || ByteBuffer.wrap(raw, 5, 4).int >= 0)) {
            "Invalid JS5 compression header: $description"
        }
        val end = header.toLong() + size
        require(size >= 0 && end <= Int.MAX_VALUE && (raw.size.toLong() == end || raw.size.toLong() == end + 2)) {
            "Invalid JS5 container length: $description"
        }
        return end.toInt()
    }

    private fun crc(raw: ByteArray, length: Int): Int = CRC32().also { it.update(raw, 0, length) }.value.toInt()

    private fun readSource(path: Path): ByteArray = try {
        FileInputStream(path.toFile()).use { it.readAllBytes() }
    } catch (failure: FileNotFoundException) {
        throw IllegalArgumentException("Missing or unreadable flat cache file: $path", failure)
    }

    private fun nativeReference(decoded: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        output.write(byteArrayOf(0x5a, 0x4c, 0x42, 0x01))
        output.write(ByteBuffer.allocate(4).putInt(decoded.size).array())
        val deflater = Deflater(Deflater.BEST_SPEED)
        try {
            DeflaterOutputStream(output, deflater).use { it.write(decoded) }
        } finally {
            deflater.end()
        }
        return output.toByteArray()
    }

    private fun nativeGroupPayload(decoded: ByteArray, fileCount: Int): ByteArray {
        if (fileCount <= 1) return decoded
        require(decoded.isNotEmpty()) { "Empty multi-file group" }
        val chunks = decoded.last().toInt() and 0xff
        val tableBytes = chunks.toLong() * fileCount * 4
        require(chunks > 0 && tableBytes < decoded.size) { "Invalid multi-file chunk directory" }
        val tableStart = decoded.size - 1 - tableBytes.toInt()
        val input = ByteBuffer.wrap(decoded)
        input.position(tableStart)
        val sizes = IntArray(fileCount)
        val chunkSizes = IntArray(chunks * fileCount)
        for (chunk in 0 until chunks) {
            var chunkSize = 0
            for (file in 0 until fileCount) {
                chunkSize = Math.addExact(chunkSize, input.int)
                require(chunkSize >= 0) { "Invalid multi-file chunk size" }
                chunkSizes[chunk * fileCount + file] = chunkSize
                sizes[file] = Math.addExact(sizes[file], chunkSize)
            }
        }
        require(sizes.sumOf { it.toLong() } == tableStart.toLong()) { "Multi-file chunk lengths do not cover the group" }
        val header = Math.addExact(5, Math.multiplyExact(4, fileCount))
        val output = ByteBuffer.allocate(Math.addExact(header, tableStart))
        output.put(1)
        val offsets = IntArray(fileCount)
        var offset = header
        for (file in 0 until fileCount) {
            offsets[file] = offset
            output.putInt(offset)
            offset += sizes[file]
        }
        output.putInt(offset)
        var sourceOffset = 0
        for (chunk in 0 until chunks) {
            for (file in 0 until fileCount) {
                val size = chunkSizes[chunk * fileCount + file]
                System.arraycopy(decoded, sourceOffset, output.array(), offsets[file], size)
                sourceOffset += size
                offsets[file] += size
            }
        }
        return output.array()
    }

    private fun elapsed(start: Long): Long = (System.nanoTime() - start) / 1_000_000
}
