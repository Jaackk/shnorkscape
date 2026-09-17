package com.rs.cache.modern;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.SlidingTilesRoom;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import static org.junit.Assert.*;

/** Menu queries used by generic Talk-to must not enter NPC.getDefinitions(). */
public class Native950NpcMenuTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private Store previous;

    @Before public void rememberCache() {
        previous = Cache.STORE;
        // Initialize the legacy array before installing a deliberately tiny fixture.
        NPCDefinitions.decodeStrict947(278, cook(), null);
    }
    @After public void restoreCache() { Cache.STORE = previous; }

    @Test public void nativeNameAndMenuUseStrictFlatCacheWhileLegacyDefinitionAccessStaysForbidden() throws Exception {
        install(cook());
        NPC npc = NPC.createNative950(278, new WorldTile(3209, 3215, 0), 1);
        assertEquals("Cook", npc.getName());
        assertEquals("cook", npc.getLowercaseName());
        assertTrue(npc.hasMenuOption("talk-to"));
        assertFalse(npc.hasMenuOption("Listen-to"));
        try { npc.getDefinitions(); fail("Native menu access must not enable legacy definitions"); }
        catch (UnsupportedOperationException expected) { assertTrue(expected.getMessage().contains("947")); }
        try { npc.getCombatDefinitions(); fail("Native menu access must not enable combat definitions"); }
        catch (UnsupportedOperationException expected) { assertTrue(expected.getMessage().contains("947")); }
    }

    @Test public void malformedDefinitionCannotLeakTheNameDecodedBeforeItsUnknownOpcode() throws Exception {
        // Opcode 190 is outside the 950 NpcType ladder's accepted set, so the strict decoder
        // refuses it. This used to use 185, which the 950 port implemented - 185 is a real opcode
        // on this revision (x4 in the cache), so the fixture stopped being malformed and the test
        // stopped testing anything.
        install(new byte[] {2, 'C', 'o', 'o', 'k', 0, (byte) 190, 0});
        NPC npc = NPC.createNative950(278, new WorldTile(3209, 3215, 0), 1);
        try { npc.getName(); fail("Partial native metadata must not classify an NPC"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("opcode 190")); }
        try { npc.hasMenuOption("Talk-to"); fail("Failed decode must not be cached as usable metadata"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("opcode 190")); }
    }

    @Test public void unrelatedNativeCookFallsThroughTheSharedSlidingPuzzleClassifier() throws Exception {
        install(cook());
        NPC npc = NPC.createNative950(278, new WorldTile(3209, 3215, 0), 1);
        // Classification must stop before reading any player's dungeon/puzzle state.
        assertFalse(SlidingTilesRoom.handleSlidingBlock(null, npc));
        try { npc.getDefinitions(); fail("Fallthrough must not admit native NPCs to legacy definitions"); }
        catch (UnsupportedOperationException expected) { assertTrue(expected.getMessage().contains("947")); }
    }

    @Test public void missingCacheFileIsNotClassifiedUsingAPlaceholderName() throws Exception {
        install(cook());
        NPC npc = NPC.createNative950(279, new WorldTile(3209, 3215, 0), 1);
        try { npc.getName(); fail("Missing definition must stay refused"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("Missing native 947")); }
    }

    @Test public void metadataMustMatchTheSizeAlreadyAdmittedIntoTheNativeWorld() throws Exception {
        install(cook());
        NPC npc = NPC.createNative950(278, new WorldTile(3209, 3215, 0), 2);
        try { npc.getName(); fail("Entity and cache size mismatch must stay refused"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("does not match")); }
    }

    @Test public void nativeMenuQueriesNeverFallBackToAnAbsentOrLegacyCache() {
        Cache.STORE = null;
        NPC npc = NPC.createNative950(278, new WorldTile(3209, 3215, 0), 1);
        try { npc.getName(); fail("A native NPC cannot borrow legacy name metadata"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("read-only cache")); }
        try { npc.hasMenuOption("Talk-to"); fail("A native NPC cannot borrow legacy menu metadata"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("read-only cache")); }
    }

    private void install(byte[] data) throws Exception {
        Path root = temp.newFolder().toPath();
        FlatStoreTest.fixture(root, 18, 278 >>> 7, new int[] {278 & 127}, new byte[][] {data});
        Cache.STORE = Store.openFlatReadOnly(root);
    }

    private static byte[] cook() {
        return new byte[] {2, 'C', 'o', 'o', 'k', 0, 30, 'T', 'a', 'l', 'k', '-', 't', 'o', 0, 12, 1, 0};
    }
}
