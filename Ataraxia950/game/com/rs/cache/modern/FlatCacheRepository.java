package com.rs.cache.modern;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.tukaani.xz.LZMAInputStream;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

/** Read-only OpenRS2 flat cache access. No legacy Store or writable file handles. */
public final class FlatCacheRepository {
    private static final int MAX_BYTES = 64 * 1024 * 1024;
    private static final int MAX_ENTRIES = 1_000_000;
    private final Path root;
    private final Map<Integer, Index> indexes = new TreeMap<>();

    public FlatCacheRepository(Path root) throws IOException {
        // Reads require directory access, not Windows handle privileges for
        // resolving every ancestor/reparse point through toRealPath().
        this.root = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(this.root)) throw new IOException("Missing cache directory " + this.root);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(this.root.resolve("255"), "*.dat")) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                int id;
                try { id = Integer.parseInt(name.substring(0, name.length() - 4)); }
                catch (NumberFormatException ignored) { continue; }
                if (id < 0 || id >= 255) continue;
                byte[] raw = readBounded(file);
                indexes.put(id, decodeIndex(id, decodeContainer(raw), checksum(raw, containerLength(raw))));
            }
        }
        if (indexes.isEmpty()) throw new IOException("No reference tables in " + this.root.resolve("255"));
    }

    public Path getRoot() { return root; }
    public Map<Integer, Index> getIndexes() { return Collections.unmodifiableMap(indexes); }

    /** Validated raw JS5 container, including its optional two-byte version trailer. */
    public byte[] readContainer(int indexId, int groupId) throws IOException {
        if (indexId == 255) {
            Index index = indexes.get(groupId);
            if (index == null) return null;
            byte[] raw = readBounded(root.resolve("255").resolve(groupId + ".dat"));
            if (checksum(raw, containerLength(raw)) != index.crc)
                throw new IOException("Reference CRC changed at 255:" + groupId);
            return raw;
        }
        Index index = indexes.get(indexId);
        Group group = index == null ? null : index.groups.get(groupId);
        if (group == null) return null;
        byte[] raw = readBounded(root.resolve(Integer.toString(indexId)).resolve(groupId + ".dat"));
        int length = containerLength(raw);
        if (checksum(raw, length) != group.crc) throw new IOException("CRC mismatch at " + indexId + ":" + groupId);
        if (raw.length == length + 2) {
            int version = ((raw[length] & 255) << 8) | (raw[length + 1] & 255);
            if (version != (group.version & 65535)) throw new IOException("Version mismatch at " + indexId + ":" + groupId);
        }
        return raw;
    }

    public byte[] readDecodedContainer(int indexId, int groupId) throws IOException {
        byte[] raw = readContainer(indexId, groupId);
        return raw == null ? null : decodeContainer(raw);
    }

    public byte[] readFile(int indexId, int groupId, int fileId) throws IOException {
        Index index = indexes.get(indexId);
        Group group = index == null ? null : index.groups.get(groupId);
        if (group == null || Arrays.binarySearch(group.fileIds, fileId) < 0) return null;
        Map<Integer, byte[]> files = readGroup(indexId, groupId);
        return files.get(fileId);
    }

    public Map<Integer, byte[]> readGroup(int indexId, int groupId) throws IOException {
        Index index = indexes.get(indexId);
        Group group = index == null ? null : index.groups.get(groupId);
        if (group == null) throw new IOException("Unknown cache group " + indexId + ":" + groupId);
        return splitGroup(readDecodedContainer(indexId, groupId), group.fileIds);
    }

    private static byte[] readBounded(Path file) throws IOException {
        long size = Files.size(file);
        if (size < 5 || size > MAX_BYTES) throw new IOException("Invalid cache container size " + size + " at " + file);
        return Files.readAllBytes(file);
    }

    static int containerLength(byte[] raw) throws IOException {
        if (raw.length < 5) throw new IOException("Truncated container header");
        int compression = raw[0] & 255;
        if (compression > 3) throw new IOException("Unsupported compression " + compression);
        int size = ByteBuffer.wrap(raw, 1, 4).getInt();
        long end = (compression == 0 ? 5L : 9L) + size;
        if (size < 0 || end > MAX_BYTES || (end != raw.length && end + 2 != raw.length))
            throw new IOException("Container length does not match header");
        return (int) end;
    }

    static byte[] decodeContainer(byte[] raw) throws IOException {
        int end = containerLength(raw);
        int compression = raw[0] & 255;
        if (compression == 0) return Arrays.copyOfRange(raw, 5, end);
        int size = ByteBuffer.wrap(raw, 5, 4).getInt();
        if (size < 0 || size > MAX_BYTES) throw new IOException("Invalid expanded length " + size);
        byte[] result = new byte[size];
        InputStream decoded;
        if (compression == 1) {
            // JS5 omits the four-byte BZIP header; its blocks use size1.
            decoded = new BZip2CompressorInputStream(new SequenceInputStream(
                    new ByteArrayInputStream(new byte[]{'B', 'Z', 'h', '1'}),
                    new ByteArrayInputStream(raw, 9, end - 9)));
        } else if (compression == 2) {
            decoded = new GZIPInputStream(new ByteArrayInputStream(raw, 9, end - 9));
        } else {
            if (end < 14) throw new IOException("Truncated LZMA properties");
            byte properties = raw[9];
            int dictionary = ByteBuffer.wrap(raw, 10, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (dictionary < 0 || dictionary > MAX_BYTES) throw new IOException("LZMA dictionary exceeds limit");
            decoded = new LZMAInputStream(new ByteArrayInputStream(raw, 14, end - 14), size, properties, dictionary);
        }
        try (InputStream input = decoded) {
            new DataInputStream(input).readFully(result);
            if (input.read() != -1) throw new IOException("Expanded container exceeds declared length");
        } catch (EOFException e) {
            throw new IOException("Expanded container is shorter than its declared length", e);
        }
        return result;
    }

    static Index decodeIndex(int id, byte[] data, int crc) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int format = buffer.get() & 255;
            if (format < 5 || format > 7) throw new IOException("Unsupported reference format " + format + " in index " + id);
            int version = format >= 6 ? buffer.getInt() : 0;
            int flags = buffer.get() & 255;
            if ((flags & ~15) != 0) throw new IOException("Unknown reference flags " + flags);
            int count = readCount(buffer, format);
            // Each group needs a delta, CRC, version, and file count at minimum.
            if (count > buffer.remaining() / 10) throw new IOException("Impossible reference group count");
            Group[] groups = new Group[count];
            int groupId = 0;
            for (int i = 0; i < count; i++) {
                int delta = readCount(buffer, format);
                if (i > 0 && delta == 0) throw new IOException("Duplicate group ID");
                groupId = Math.addExact(groupId, delta);
                groups[i] = new Group(groupId);
            }
            if ((flags & 1) != 0) for (Group group : groups) group.nameHash = buffer.getInt();
            for (Group group : groups) group.crc = buffer.getInt();
            if ((flags & 8) != 0) skip(buffer, (long) count * 4);
            if ((flags & 2) != 0) skip(buffer, (long) count * 64);
            if ((flags & 4) != 0) skip(buffer, (long) count * 8);
            for (Group group : groups) group.version = buffer.getInt();
            long filesTotal = 0;
            for (Group group : groups) {
                int files = readCount(buffer, format);
                filesTotal += files;
                if (filesTotal > MAX_ENTRIES) throw new IOException("Too many reference files");
                group.fileIds = new int[files];
            }
            for (Group group : groups) {
                int fileId = 0;
                for (int i = 0; i < group.fileIds.length; i++) {
                    int delta = readCount(buffer, format);
                    if (i > 0 && delta == 0) throw new IOException("Duplicate file ID");
                    group.fileIds[i] = fileId = Math.addExact(fileId, delta);
                }
            }
            if ((flags & 1) != 0) skip(buffer, filesTotal * 4);
            if (buffer.hasRemaining()) throw new IOException("Trailing bytes in reference index " + id);
            return new Index(id, version, crc, groups);
        } catch (BufferUnderflowException | IndexOutOfBoundsException | ArithmeticException | IllegalArgumentException e) {
            throw new IOException("Malformed reference index " + id, e);
        }
    }

    private static int readCount(ByteBuffer buffer, int format) throws IOException {
        int value = format >= 7 && buffer.get(buffer.position()) < 0
                ? buffer.getInt() & 0x7fffffff : buffer.getShort() & 65535;
        if (value > MAX_ENTRIES) throw new IOException("Reference value exceeds limit: " + value);
        return value;
    }

    private static void skip(ByteBuffer buffer, long bytes) throws IOException {
        if (bytes < 0 || bytes > buffer.remaining()) throw new IOException("Truncated reference fields");
        buffer.position(buffer.position() + (int) bytes);
    }

    static Map<Integer, byte[]> splitGroup(byte[] data, int[] ids) throws IOException {
        Map<Integer, byte[]> result = new LinkedHashMap<>();
        if (ids.length == 1) { result.put(ids[0], data); return result; }
        if (ids.length == 0 || data.length == 0) throw new IOException("Empty multi-file group");
        int chunks = data[data.length - 1] & 255;
        long tableStart = data.length - 1L - (long) chunks * ids.length * 4;
        if (chunks == 0 || tableStart < 0) throw new IOException("Invalid group chunk table");
        ByteBuffer table = ByteBuffer.wrap(data);
        table.position((int) tableStart);
        int[][] lengths = new int[chunks][ids.length];
        int[] totals = new int[ids.length];
        long sum = 0;
        for (int c = 0; c < chunks; c++) {
            long size = 0;
            for (int f = 0; f < ids.length; f++) {
                size += table.getInt();
                if (size < 0 || size > tableStart || totals[f] + size > MAX_BYTES) throw new IOException("Invalid group chunk length");
                lengths[c][f] = (int) size;
                totals[f] += (int) size;
                sum += size;
            }
        }
        if (sum != tableStart) throw new IOException("Group chunks do not consume payload exactly");
        byte[][] files = new byte[ids.length][];
        for (int f = 0; f < ids.length; f++) files[f] = new byte[totals[f]];
        int[] offsets = new int[ids.length];
        int source = 0;
        for (int c = 0; c < chunks; c++) for (int f = 0; f < ids.length; f++) {
            int length = lengths[c][f];
            System.arraycopy(data, source, files[f], offsets[f], length);
            source += length;
            offsets[f] += length;
        }
        for (int f = 0; f < ids.length; f++) result.put(ids[f], files[f]);
        return result;
    }

    private static int checksum(byte[] data, int length) {
        CRC32 crc = new CRC32();
        crc.update(data, 0, length);
        return (int) crc.getValue();
    }

    public static final class Index {
        public final int id, version, crc;
        private final Map<Integer, Group> groups = new TreeMap<>();
        private Index(int id, int version, int crc, Group[] entries) {
            this.id = id; this.version = version; this.crc = crc;
            for (Group group : entries) groups.put(group.id, group);
        }
        public Map<Integer, Group> getGroups() { return Collections.unmodifiableMap(groups); }
        public int findGroup(String name) {
            int hash = 0;
            for (int i = 0; i < name.length(); i++) hash = hash * 31 + name.charAt(i);
            for (Group group : groups.values()) if (group.nameHash == hash) return group.id;
            return -1;
        }
    }

    public static final class Group {
        public final int id;
        private int crc, version, nameHash;
        private int[] fileIds;
        private Group(int id) { this.id = id; }
        public int[] getFileIds() { return fileIds.clone(); }
    }
}
