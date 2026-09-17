package com.rs.cache.modern;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950IdValidity;
import com.rs.game.player.client.Native950NpcViewport;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public final class Native950DiagnosticNpcTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private Store previous;
    @Before public void rememberCache() {
        previous = Cache.STORE;
        NPCDefinitions.decodeStrict947(42,bill(),null);
    }
    @After public void restoreCache() { Cache.STORE = previous; }

    @Test public void currentCacheIdentityUsesActualSizeAndExactTileWithoutLegacyCombatOrAi() throws Exception {
        install(bill());
        WorldTile tile = new WorldTile(3200,3201,2);
        NPC npc = NPC.createNative950Diagnostic(42,tile);
        assertEquals(42,npc.getId()); assertEquals(2,npc.getSize()); assertEquals("Bill",npc.getName());
        assertEquals(3200,npc.getX()); assertEquals(3201,npc.getY()); assertEquals(2,npc.getPlane());
        assertTrue(npc.isNative950DiagnosticDefinition());
        assertEquals(0,npc.getIndex()); assertNull(npc.getNative950CombatProfile());
        assertEquals(1,npc.getHitpoints()); assertFalse(npc.hasWalkSteps());
        try { npc.getCombatDefinitions(); fail(); } catch (UnsupportedOperationException expected) { }
        try { npc.processNPC(); fail(); } catch (UnsupportedOperationException expected) { }
    }

    @Test public void repurposedDiagnosticIdIsPublishedButOrdinarySpawnOfSameOldIdStaysRefused() throws Exception {
        install(bill());
        assertFalse(Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,42));
        EmbeddedChannel channel = new EmbeddedChannel();
        NPC diagnostic = NPC.createNative950Diagnostic(42,new WorldTile(3200,3200,0));
        NPC ordinary = NPC.createNative950(42,new WorldTile(3201,3200,0),2);
        try {
            World.addNative950Npc(diagnostic); World.addNative950Npc(ordinary);
            Player viewer = Player.createNative950("diag-test",new WorldTile(3200,3200,0),channel);
            Native950NpcViewport view = new Native950NpcViewport(24,250,Native950NpcViewport.NO_MASKS);
            view.frame(viewer,7,false,Arrays.asList(diagnostic,ordinary));
            assertArrayEquals(new int[]{diagnostic.getIndex()},view.snapshot().indices);
            // Simulate a buggy raw-ID mutation: admission belongs to42, not any later repurposed ID.
            Field id = NPC.class.getDeclaredField("id"); id.setAccessible(true); id.setInt(diagnostic,1327);
            assertFalse(diagnostic.isNative950DiagnosticDefinition());
            assertFalse(Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,1327));
            view.frame(viewer,7,false,Arrays.asList(diagnostic,ordinary));
            assertEquals(0,view.snapshot().indices.length);
            view.close();
        } finally {
            World.removeNative950Npc(diagnostic); World.removeNative950Npc(ordinary);
            channel.finishAndReleaseAll();
        }
    }

    @Test public void markerCannotSurviveSwitchingToAnotherCacheStore() throws Exception {
        install(bill());
        NPC npc = NPC.createNative950Diagnostic(42,new WorldTile(3200,3200,0));
        assertTrue(npc.isNative950DiagnosticDefinition());
        install(bill());
        assertFalse(npc.isNative950DiagnosticDefinition());
    }

    @Test public void missingMalformedInvisibleAndVariableFormsAreNotRegistered() throws Exception {
        for (byte[] bytes : new byte[][]{
                new byte[]{2,'B','i','l','l',0,0},
                new byte[]{2,'B','i','l','l',0,(byte)190,0},
                new byte[]{1,1,0,1,2,'B','i','l','l',0,(byte)187,0,0,1,(byte)255,(byte)255,0,0,43,0}}) {
            install(bytes);
            try { NPC.createNative950Diagnostic(42,new WorldTile(3200,3200,0)); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        install(bill());
        for (int id : new int[]{-1,43,65535,Integer.MAX_VALUE}) {
            try { NPC.createNative950Diagnostic(id,new WorldTile(3200,3200,0)); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        try { NPC.createNative950Diagnostic(42,new WorldTile(16383,3200,0)); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("boundary")); }
    }

    private void install(byte[] bytes) throws Exception {
        Path path = temp.newFolder().toPath();
        FlatStoreTest.fixture(path,18,0,new int[]{42},new byte[][]{bytes});
        Cache.STORE = Store.openFlatReadOnly(path);
    }
    private static byte[] bill() {
        return new byte[]{1,1,0,1,2,'B','i','l','l',0,12,2,0};
    }
}
