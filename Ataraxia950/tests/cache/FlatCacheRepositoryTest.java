package com.rs.cache.modern;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.Assert.*;

public class FlatCacheRepositoryTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    private static byte[] container(byte[] data) {
        return ByteBuffer.allocate(data.length + 5).put((byte) 0).putInt(data.length).put(data).array();
    }

    @Test public void splitsSparseFilesWithNegativeChunkDeltas() throws Exception {
        byte[] group = ByteBuffer.allocate(5 + 8 + 1).put(new byte[]{1,2,3,4,5}).putInt(3).putInt(-1).put((byte)1).array();
        Map<Integer, byte[]> files = FlatCacheRepository.splitGroup(group, new int[]{0,7});
        assertArrayEquals(new byte[]{1,2,3}, files.get(0));
        assertArrayEquals(new byte[]{4,5}, files.get(7));
    }

    @Test public void decompressesGzipAndRejectsTruncation() throws Exception {
        byte[] data = new byte[25000]; Arrays.fill(data, (byte)42);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) { gzip.write(data); }
        byte[] body = compressed.toByteArray();
        byte[] raw = ByteBuffer.allocate(9 + body.length).put((byte)2).putInt(body.length).putInt(data.length).put(body).array();
        assertArrayEquals(data, FlatCacheRepository.decodeContainer(raw));
        expectFailure(() -> FlatCacheRepository.decodeContainer(Arrays.copyOf(raw, raw.length - 1)));
    }

    @Test public void validatesBzipChecksumAndCompleteExpandedLength() throws Exception {
        byte[] data = new byte[5000]; Arrays.fill(data, (byte)27);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzip = new BZip2CompressorOutputStream(output, 1)) { bzip.write(data); }
        byte[] body = Arrays.copyOfRange(output.toByteArray(), 4, output.size());
        byte[] raw = ByteBuffer.allocate(9 + body.length).put((byte)1).putInt(body.length).putInt(data.length).put(body).array();
        assertArrayEquals(data, FlatCacheRepository.decodeContainer(raw));
        byte[] invalid = ByteBuffer.allocate(10).put((byte)1).putInt(1).putInt(10).put((byte)0x17).array();
        expectFailure(() -> FlatCacheRepository.decodeContainer(invalid));
        // A valid stream with the wrong declared length must not be padded with zeroes.
        ByteBuffer.wrap(raw, 5, 4).putInt(data.length + 1);
        expectFailure(() -> FlatCacheRepository.decodeContainer(raw));
    }

    @Test public void rejectsUnconsumedPayloadAndNegativeChunkLength() throws Exception {
        byte[] bad = ByteBuffer.allocate(3 + 8 + 1).put(new byte[]{1,2,3}).putInt(1).putInt(0).put((byte)1).array();
        expectFailure(() -> FlatCacheRepository.splitGroup(bad, new int[]{0,1}));
        bad[6] = (byte)255;
        expectFailure(() -> FlatCacheRepository.splitGroup(bad, new int[]{0,1}));
    }

    @Test public void readsFixtureWithoutChangingSourceAndDetectsCorruption() throws Exception {
        Path root = temp.newFolder().toPath();
        Files.createDirectory(root.resolve("255")); Files.createDirectory(root.resolve("19"));
        byte[] raw = container(new byte[]{2, 'C', 'o', 'i', 'n', 's', 0, 0});
        CRC32 crc = new CRC32(); crc.update(raw);
        // format7, revision1, no optional fields, one group3 with sparse file227.
        byte[] reference = ByteBuffer.allocate(22).put((byte)7).putInt(1).put((byte)0)
            .putShort((short)1).putShort((short)3).putInt((int)crc.getValue()).putInt(9)
            .putShort((short)1).putShort((short)227).array();
        Path ref = root.resolve("255/19.dat"), group = root.resolve("19/3.dat");
        byte[] referenceRaw = container(reference);
        Files.write(ref, referenceRaw); Files.write(group, raw);
        FlatCacheRepository cache = new FlatCacheRepository(root);
        assertArrayEquals(Arrays.copyOfRange(raw, 5, raw.length), cache.readFile(19,3,227));
        assertNull(cache.readFile(19,3,228));
        assertArrayEquals(referenceRaw, Files.readAllBytes(ref)); assertArrayEquals(raw, Files.readAllBytes(group));
        raw[6] ^= 1; Files.write(group, raw);
        expectFailure(() -> cache.readFile(19,3,227));
    }

    @Test public void rejectsUnknownFormatsAndTrailingReferenceBytes() throws Exception {
        expectFailure(() -> FlatCacheRepository.decodeIndex(1, new byte[]{8}, 0));
        expectFailure(() -> FlatCacheRepository.decodeIndex(1, new byte[]{7,0,0,0,1,0}, 0));
        byte[] empty = ByteBuffer.allocate(9).put((byte)7).putInt(1).put((byte)0).putShort((short)0).put((byte)99).array();
        expectFailure(() -> FlatCacheRepository.decodeIndex(1, empty, 0));
    }

    private interface Checked { void run() throws Exception; }
    private static void expectFailure(Checked operation) throws Exception {
        try { operation.run(); fail("Expected malformed cache rejection"); }
        catch (IOException expected) { assertNotNull(expected.getMessage()); }
    }
}
