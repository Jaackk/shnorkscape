package modern947;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import org.junit.Test;

import static org.junit.Assert.*;

public final class Native950NpcEntityTest {
    @Test
    public void initializerCreatesRealEntityWithoutLegacyDefinitionsOrWorldRegistration() {
        WorldTile tile = new WorldTile(3217, 3257, 0);
        NPC npc = NPC.createNative950(494, tile, 1);
        tile.setLocation(new WorldTile(4000, 4000, 1));
        assertEquals(NPC.class, npc.getClass());
        assertTrue(npc.isNative950());
        assertEquals(494, npc.getId());
        assertEquals(1, npc.getSize());
        assertEquals(3217, npc.getX());
        assertEquals(3257, npc.getY());
        assertEquals(0, npc.getPlane());
        assertEquals(3217, npc.getRespawnTile().getX());
        assertEquals(0, npc.getIndex());
        assertEquals(-1, npc.getLastRegionId());
        assertFalse(npc.isDead());
        assertFalse(npc.hasFinished());
        assertFalse(npc.getRun());
        assertNotNull(npc.getWalkSteps());
        assertTrue(npc.getWalkSteps().isEmpty());
        assertTrue(npc.getMapRegionsIds().isEmpty());
        assertEquals(-1, npc.getNextWalkDirection());
        assertEquals(-1, npc.getNextRunDirection());
        assertNull(npc.getCombat());
        assertTrue(npc.toString().contains("494"));
    }

    @Test
    public void rejectsLegacyEntryPointsBeforeDefinitionOrCombatLookups() {
        NPC npc = NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1);
        unsupported(npc::getDefinitions);
        unsupported(npc::getCombatDefinitions);
        unsupported(npc::processNPC);
        unsupported(npc::processEntity);
        unsupported(() -> npc.setNPC(495));
        assertEquals(494, npc.getId());
        assertEquals(1, npc.getSize());
    }

    @Test
    public void validatesNativeDefinitionSizeAndTileBeforeConstruction() {
        WorldTile tile = new WorldTile(3217, 3257, 0);
        invalid(() -> NPC.createNative950(-1, tile, 1));
        invalid(() -> NPC.createNative950(494, tile, 0));
        invalid(() -> NPC.createNative950(494, tile, 256));
        invalid(() -> NPC.createNative950(494, new WorldTile(-1, 3257, 0), 1));
        invalid(() -> NPC.createNative950(494, new WorldTile(3217, 16384, 0), 1));
        assertEquals(2, NPC.createNative950(494, tile, 2).getSize());
    }

    /**
     * P6 replaced the single-NPC slot with the ordinary {@code EntityList} registry: N native
     * NPCs coexist, each keeping the index EntityList assigned (which is what NPC_INFO reports),
     * a finished NPC still frees its index, and the freed index is reused lowest-first.
     */
    @Test
    public void nativeNpcsShareTheWorldRegistryAndFreeTheirIndexOnFinish() {
        NPC first = NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1);
        NPC second = NPC.createNative950(494, new WorldTile(3218, 3257, 0), 1);
        NPC third = NPC.createNative950(494, new WorldTile(3219, 3257, 0), 1);
        try {
            assertTrue(World.getNPCs().isEmpty());
            World.addNative950Npc(first);
            World.addNative950Npc(second);
            assertEquals(1, first.getIndex());
            assertEquals(2, second.getIndex());
            assertSame(first, World.getNPCs().get(1));
            assertSame(second, World.getNPCs().get(2));
            assertEquals(2, World.getNPCs().size());
            try {
                World.addNative950Npc(first);
                fail("A registered native NPC must not enter the world twice");
            } catch (IllegalStateException expected) {
                assertEquals(1, first.getIndex());
            }
            first.finish();
            assertTrue(first.hasFinished());
            assertEquals(0, first.getIndex());
            assertEquals(1, World.getNPCs().size());
            // EntityList reuses the lowest free index, which is exactly the reuse NPC_INFO
            // viewports have to detect; the world reports it as-is.
            World.addNative950Npc(third);
            assertEquals(1, third.getIndex());
            assertSame(third, World.getNPCs().get(1));
            assertEquals(2, World.getNPCs().size());
        } finally {
            World.removeNative950Npc(first);
            World.removeNative950Npc(second);
            World.removeNative950Npc(third);
        }
        assertTrue(World.getNPCs().isEmpty());
    }

    /**
     * P6 lifted the movement gate only: a native NPC may run Entity movement once it has been
     * opted in, and everything that needs the 910 definition, combat or respawn tables stays
     * refused. A stationary native NPC (the default) never loads a map region set, which is
     * why enabling movement is what loads it.
     */
    @Test
    public void nativeMovementIsOptInAndLeavesTheLegacyGatesRefused() {
        NPC npc = NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1);
        assertFalse(npc.isNative950Movable());
        assertEquals(0, npc.getNative950Wander());
        assertTrue(npc.getMapRegionsIds().isEmpty());
        npc.processNative950Movement(); // stationary: no-op, and safe before registration
        assertEquals(-1, npc.getNextWalkDirection());
        npc.setNative950Wander(3);
        assertTrue(npc.isNative950Movable());
        assertEquals(3, npc.getNative950Wander());
        assertFalse("enabling movement must load the region set needMapUpdate() reads",
                npc.getMapRegionsIds().isEmpty());
        unsupported(npc::getDefinitions);
        unsupported(npc::getCombatDefinitions);
        unsupported(npc::processNPC);
        unsupported(npc::processEntity);
        unsupported(() -> npc.setNPC(495));
        try {
            npc.setNative950Wander(33);
            fail("The wander radius must be bounded");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("947"));
        }
    }

    @Test
    public void regionUpdateRequiresRegistrationAndFinishedEntityCannotBeReadmitted() {
        NPC npc = NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1);
        try {
            World.updateEntityRegion(npc);
            fail("An unregistered NPC must not enter region indices");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("registered"));
        }
        World.removeNative950Npc(npc);
        try {
            World.addNative950Npc(npc);
            fail("Finished NPCs must be replaced by fresh entities");
        } catch (IllegalStateException expected) {
            assertTrue(npc.hasFinished());
        }
    }

    private static void unsupported(Runnable operation) {
        try {
            operation.run();
            fail("Native NPCs must not enter legacy behavior");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("947"));
        }
    }

    private static void invalid(Runnable operation) {
        try {
            operation.run();
            fail("Invalid NPC state must be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("947"));
        }
    }
}
