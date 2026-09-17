package modern947;

import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.rfd.AgrithNaNa;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/** Regression for the Cook's legacy encounter contaminating later native world logins. */
public final class Native950NpcOwnershipTest {
    private final List<NPC> owned = new ArrayList<NPC>();
    private Field schedulerField;
    private Object previousScheduler;

    @Before
    public void installNativeOwnershipWithoutStartingThreads() throws Exception {
        assertTrue(World.getNPCs().isEmpty());
        schedulerField = CoresManager.class.getDeclaredField("native947Scheduler");
        schedulerField.setAccessible(true);
        previousScheduler = schedulerField.get(null);
        schedulerField.set(null, new Native950TickScheduler());
    }

    @After
    public void restoreRegistryAndScheduler() throws Exception {
        for (NPC npc : owned) {
            if (npc.isNative950()) World.removeNative950Npc(npc);
            else if (World.containsNPC(npc)) World.removeNPC(npc);
        }
        if (schedulerField != null) schedulerField.set(null, previousScheduler);
    }

    @Test
    public void legacyInsertionIsRefusedBeforeIndexOrRegistryMutation() throws Exception {
        NPC banker = nativeNpc();
        World.addNative950Npc(banker);
        NPC legacy = legacyShell();
        refuse(() -> World.addNPC(legacy), "registration");
        assertEquals(0, legacy.getIndex());
        assertEquals(-1, legacy.getLastRegionId());
        assertEquals(1, World.getNPCs().size());
        assertSame(banker, World.getNPCs().get(banker.getIndex()));
        assertFalse(World.containsNPC(legacy));
    }

    @Test
    public void actualCookBossConstructorIsRefusedBeforeLegacyCacheAndCombatAccess() {
        // Exact first-wave class and ID from ImpossibleJad.startWave. No cache, combat
        // tables, controller, dynamic map or server executor is initialized by this test.
        refuse(() -> new AgrithNaNa(3493, new WorldTile(72, 73, 2), null), "construction");
        assertTrue(World.getNPCs().isEmpty());
    }

    @Test
    public void refusedEncounterCannotBlockNativeBankerRecreation() {
        NPC first = nativeNpc();
        World.addNative950Npc(first);
        refuse(() -> World.addNPC(legacyShell()), "registration");
        refuse(() -> new AgrithNaNa(3493, new WorldTile(72, 73, 2), null), "construction");
        World.removeNative950Npc(first);
        assertTrue(World.getNPCs().isEmpty());
        NPC reconnectBanker = nativeNpc();
        World.addNative950Npc(reconnectBanker);
        assertEquals(1, reconnectBanker.getIndex());
        assertSame(reconnectBanker, World.getNPCs().get(1));
    }

    @Test
    public void ordinaryRegistrationKeepsLegacyBehaviorOutsideNativeMode() throws Exception {
        schedulerField.set(null, null);
        NPC legacy = legacyShell();
        World.addNPC(legacy);
        assertFalse(legacy.isNative950());
        assertEquals(1, legacy.getIndex());
        assertSame(legacy, World.getNPCs().get(1));
    }

    @Test
    public void nativeNpcCannotBypassItsAdmissionChecksThroughLegacyEntryPoint() {
        NPC nativeNpc = nativeNpc();
        refuse(() -> World.addNPC(nativeNpc), "registration");
        assertEquals(0, nativeNpc.getIndex());
        assertTrue(World.getNPCs().isEmpty());
        World.addNative950Npc(nativeNpc);
        assertEquals(1, nativeNpc.getIndex());
    }

    private NPC nativeNpc() {
        NPC npc = NPC.createNative950(494, new WorldTile(3217, 3257, 0), 1);
        owned.add(npc);
        return npc;
    }

    private NPC legacyShell() {
        // Registration does not need definitions. Build an initialized, unregistered
        // entity then give this fixture the legacy ownership tag, avoiding fake caches.
        NPC npc = nativeNpc();
        try {
            Field size = NPC.class.getDeclaredField("native947Size");
            size.setAccessible(true);
            size.setInt(npc, 0);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
        assertFalse(npc.isNative950());
        return npc;
    }

    private static void refuse(Runnable operation, String phase) {
        try {
            operation.run();
            fail("Legacy NPC " + phase + " entered the native world");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Legacy NPC " + phase));
            assertTrue(expected.getMessage(), expected.getMessage().contains("native 950 world"));
        }
    }
}
