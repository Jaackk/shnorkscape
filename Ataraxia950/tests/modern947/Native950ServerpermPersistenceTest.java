package modern947;

import com.rs.game.player.client.Native950ServerpermStore;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Structural coverage for the proven native permanent-variable persistence lifecycle. */
public final class Native950ServerpermPersistenceTest {
    private Path directory;
    private Native950ServerpermStore store;

    @Before
    public void setUp() throws Exception {
        directory = Files.createTempDirectory("native950-serverperm");
        Set<Integer> allowed = new java.util.HashSet<Integer>(Arrays.asList(7, 11, 3296, 65535));
        store = new Native950ServerpermStore(directory, allowed);
    }

    @After
    public void tearDown() throws Exception {
        if (directory == null) return;
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException failure) { throw new AssertionError(failure); }
            });
        }
    }

    @Test
    public void opcode14DecodesTheCaptured215RecordShapeAndRejectsMalformedChunks() {
        byte[] capturedShape = new byte[1 + 215 * 6];
        capturedShape[0] = 1;
        for (int i = 0, offset = 1; i < 215; i++, offset += 6) {
            int id = i;
            capturedShape[offset] = (byte) (id >>> 8);
            capturedShape[offset + 1] = (byte) id;
            capturedShape[offset + 2] = (byte) (i >>> 24);
            capturedShape[offset + 3] = (byte) (i >>> 16);
            capturedShape[offset + 4] = (byte) (i >>> 8);
            capturedShape[offset + 5] = (byte) i;
        }
        Native950Actions.ServerpermVarcsAction decoded =
                (Native950Actions.ServerpermVarcsAction) Native950Actions.decode(14, capturedShape);
        assertNotNull(decoded);
        assertTrue(decoded.complete());
        assertEquals(215, decoded.values().size());
        assertEquals(Integer.valueOf(214), decoded.values().get(214));
        assertEquals(-2, Native950Protocol.clientSize(14));
        assertNull(Native950Actions.decode(14, new byte[0]));
        assertNull(Native950Actions.decode(14, new byte[] {2}));
        assertNull(Native950Actions.decode(14, new byte[] {1, 0}));
        assertNull(Native950Actions.decode(14, new byte[1 + 257 * 6]));
    }

    @Test
    public void defaultsStayCompleteWhilePersistedValuesOverlayOnlyTheirOwnAccount() throws Exception {
        Map<Integer, Integer> defaults = new LinkedHashMap<Integer, Integer>();
        for (int id = 0; id < 214; id++) defaults.put(id, id + 1000);
        defaults.put(3296, 3296000);
        Map<Integer, Integer> jaxa = new LinkedHashMap<Integer, Integer>();
        jaxa.put(7, 777);
        jaxa.put(3296, -1234567);
        store.save("Jaxa", jaxa);

        Native950ServerpermStore.overlay(defaults, store.load("jaxa"));
        assertEquals("all healthy defaults remain present", 215, defaults.size());
        assertEquals(Integer.valueOf(777), defaults.get(7));
        assertEquals(Integer.valueOf(-1234567), defaults.get(3296));
        assertEquals(Integer.valueOf(1008), defaults.get(8));
        assertTrue("another account cannot see Jaxa's state", store.load("otheracct").isEmpty());
    }

    @Test
    public void malformedOrInvalidStateFailsClosedWithoutReplacingThePreviousValidFile() throws Exception {
        Map<Integer, Integer> valid = new HashMap<Integer, Integer>();
        valid.put(11, 42);
        store.save("Jaxa", valid);
        Path saved;
        try (Stream<Path> paths = Files.list(directory)) { saved = paths.findFirst().get(); }
        byte[] previous = Files.readAllBytes(saved);

        Map<Integer, Integer> invalid = new HashMap<Integer, Integer>();
        invalid.put(12, 99);
        try {
            store.save("Jaxa", invalid);
            fail("invalid permanent id must not be stored");
        } catch (IOException expected) { }
        assertTrue(Arrays.equals(previous, Files.readAllBytes(saved)));

        byte[] corrupt = previous.clone();
        corrupt[0] ^= 1;
        Files.write(saved, corrupt);
        try {
            store.load("Jaxa");
            fail("corrupt checksum must fail closed");
        } catch (IOException expected) { }
        assertTrue(store.loadOrEmpty("Jaxa").isEmpty());
    }

    @Test
    public void acknowledgementIsTheProvenEmptyOpcode136Packet() {
        Native950Packets.Packet acknowledgement = Native950Packets.storeServerPermVarcs();
        assertEquals(Native950Protocol.ServerPacket.STORE_SERVERPERM_VARCS, acknowledgement.type());
        assertEquals(136, acknowledgement.type().opcode());
        assertEquals(0, acknowledgement.type().size());
        assertEquals(0, acknowledgement.payload().length);
    }

    @Test
    public void nonFinalChunksCanBeMergedWithoutCommittingUntilTheCompletionChunk() {
        Native950Actions.ServerpermVarcsAction more = (Native950Actions.ServerpermVarcsAction)
                Native950Actions.decode(14, new byte[] {0, 0, 7, 0, 0, 0, 1});
        assertNotNull(more);
        assertFalse(more.complete());
        Map<Integer, Integer> completed = new LinkedHashMap<Integer, Integer>();
        completed.putAll(more.values());
        assertEquals(Integer.valueOf(1), completed.get(7));
        Native950Actions.ServerpermVarcsAction finalChunk = (Native950Actions.ServerpermVarcsAction)
                Native950Actions.decode(14, new byte[] {1, 0, 11, 0, 0, 0, 2});
        assertNotNull(finalChunk);
        completed.putAll(finalChunk.values());
        assertTrue(finalChunk.complete());
        assertEquals(2, completed.size());
        assertEquals(Integer.valueOf(2), completed.get(11));
    }
}
