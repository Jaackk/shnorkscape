package com.rs.cache.modern;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.filestore.store.Index;
import com.rs.game.Region;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.CRC32;
import static org.junit.Assert.*;

public class FlatStoreTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void legacyApiReadsSparseFilesAndRejectsWritesWithoutChangingSource() throws Exception {
        Path root = temp.newFolder().toPath();
        byte[] payload = new byte[]{2, 'C', 'o', 'i', 'n', 's', 0, 0};
        fixture(root, 19, 3, new int[]{227}, new byte[][]{payload});
        byte[] before = Files.readAllBytes(root.resolve("19/3.dat"));
        Store store = Store.openFlatReadOnly(root);
        Index index = store.getIndexes()[19];
        assertTrue(store.isReadOnly());
        assertEquals(3, index.getLastArchiveId());
        assertEquals(227, index.getLastFileId(3));
        assertArrayEquals(payload, index.getFile(3,227));
        byte[] mutable = index.getFile(3,227); mutable[0] = 99;
        assertArrayEquals(payload, index.getFile(3,227));
        assertNull(index.getFile(3,228));
        assertNull(index.getFile(3,-1));
        assertArrayEquals(payload, index.getMainFile().getArchive(3).getData());
        rejected(() -> index.putFile(3,227,new byte[]{1}));
        rejected(() -> index.removeFile(3,227));
        rejected(() -> store.addIndex(false,false,0));
        rejected(() -> index.getMainFile().putArchiveData(3,new byte[]{1}));
        assertArrayEquals(before, Files.readAllBytes(root.resolve("19/3.dat")));
        assertFalse(Files.exists(root.resolve("main_file_cache.dat2")));
        byte[] corrupt = before.clone(); corrupt[5] ^= 1; Files.write(root.resolve("19/3.dat"),corrupt);
        index.resetCachedFiles();
        try { index.getFile(3,227); fail("Expected CRC failure"); }
        catch (java.io.UncheckedIOException expected) { assertTrue(expected.getCause().getMessage().contains("CRC")); }
    }

    @Test public void regionLoadsCollisionSynchronouslyWithoutLegacySpawns() throws Exception {
        Path root = temp.newFolder().toPath();
        byte[] settings = new byte[4*64*64+3];
        settings[0] = 8; settings[1] = 1; settings[2] = 1; // modern two-byte height
        int tile = 1*64+1+2;
        settings[tile] = 2; settings[tile+1] = 1;
        // Object 1276 at local 10,10: rectangular type10 with a two-tile X footprint.
        byte[] objects = ByteBuffer.allocate(7).putShort((short)(32768+1277)).putShort((short)(32768+651)).put((byte)40).put((byte)0).put((byte)0).array();
        fixture(root,5,50|(50<<7),new int[]{0,3},new byte[][]{objects,modernSettings(settings)});
        fixture(root,16,1276>>>8,new int[]{1276&255},new byte[][]{modernObjectDefinition()});
        Store previous = Cache.STORE;
        try {
            Cache.STORE = Store.openFlatReadOnly(root);
            com.rs.cache.loaders.ObjectDefinitions.clearObjectDefinitions();
            Region region = new Region((50<<8)|50);
            region.checkLoadMap();
            assertEquals(2,region.getLoadMapStage());
            assertEquals(0,region.getMask(0,5,5));
            assertNotEquals(0,region.getMask(0,1,1));
            assertNotEquals(0,region.getMask(0,10,10));
            assertNotEquals(0,region.getMask(0,11,10));
            assertEquals(1,region.getObjects().size());
            assertTrue(region.getSpawnedObjects().isEmpty());
        } finally { com.rs.cache.loaders.ObjectDefinitions.clearObjectDefinitions(); Cache.STORE = previous; }
    }

    @Test public void unsupportedDefinitionBlocksRegionAndRetainsDiagnostic() throws Exception {
        Path root = temp.newFolder().toPath();
        byte[] objects = ByteBuffer.allocate(7).putShort((short)(32768+1281)).putShort((short)(32768+651)).put((byte)40).put((byte)0).put((byte)0).array();
        fixture(root,5,50|(50<<7),new int[]{0,3},new byte[][]{objects,modernSettings(new byte[4*64*64])});
        fixture(root,16,1280>>>8,new int[]{1280&255},new byte[][]{new byte[]{(byte)250,0}});
        Store previous = Cache.STORE;
        try {
            Cache.STORE = Store.openFlatReadOnly(root);
            com.rs.cache.loaders.ObjectDefinitions.clearObjectDefinitions();
            Region region = new Region((50<<8)|50);
            try { region.quickLoad(); fail("Expected unknown definition failure"); }
            catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("movement blocked")); }
            assertEquals(-1,region.getMask(0,5,5));
            try { region.checkLoadMap(); fail("Expected retained failure"); }
            catch (IllegalStateException expected) { assertNotNull(expected.getCause()); }
        } finally { com.rs.cache.loaders.ObjectDefinitions.clearObjectDefinitions(); Cache.STORE = previous; }
    }

    private interface Checked { void run() throws Exception; }
    private static void rejected(Checked operation) throws Exception {
        try { operation.run(); fail("Expected read-only guard"); }
        catch (UnsupportedOperationException expected) { assertTrue(expected.getMessage().contains("read-only")); }
    }

    static void fixture(Path root, int index, int group, int[] ids, byte[][] files) throws Exception {
        Files.createDirectories(root.resolve("255")); Files.createDirectories(root.resolve(Integer.toString(index)));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        for (byte[] file : files) out.write(file);
        if (files.length > 1) {
            int prior = 0;
            for (byte[] file : files) { out.writeInt(file.length-prior); prior=file.length; }
            out.writeByte(1);
        }
        byte[] raw = container(bytes.toByteArray());
        CRC32 crc = new CRC32(); crc.update(raw);
        ByteBuffer reference = ByteBuffer.allocate(20+2*ids.length).put((byte)7).putInt(1).put((byte)0)
                .putShort((short)1).putShort((short)group).putInt((int)crc.getValue()).putInt(9).putShort((short)ids.length);
        int prior = 0;
        for (int id : ids) { reference.putShort((short)(id-prior)); prior=id; }
        Files.write(root.resolve("255").resolve(index+".dat"),container(reference.array()));
        Files.write(root.resolve(Integer.toString(index)).resolve(group+".dat"),raw);
    }
    private static byte[] container(byte[] data) {
        return ByteBuffer.allocate(data.length+5).put((byte)0).putInt(data.length).put(data).array();
    }
    private static byte[] modernSettings(byte[] tiles) {
        return ByteBuffer.allocate(tiles.length+13).putInt(0x6a616778).put((byte)1).put(tiles).putLong(0).array();
    }
    private static byte[] modernObjectDefinition() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        for (int flag : new int[]{188,198,199,203}) out.writeByte(flag);
        out.writeByte(202); out.writeByte(3);
        out.writeByte(204); out.writeByte(1); out.write(new byte[27]);
        out.writeByte(205); out.writeShort(18); out.writeShort(65535); out.writeShort(65535);
        out.writeByte(2); out.writeByte(1); out.writeByte(1); out.writeByte(1);
        out.writeShort(1); out.writeShort(2); out.writeShort(1); out.writeShort(65535);
        // A >127 morph count was previously read as one byte, turning IDs into opcodes.
        out.writeByte(77); out.writeShort(65535); out.writeShort(65535); out.writeShort(32768+128);
        for (int i=0;i<=128;i++) out.writeShort(32767);
        out.write(new byte[]{14,2,15,1,0});
        return bytes.toByteArray();
    }
}
