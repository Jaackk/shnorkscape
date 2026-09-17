package com.rs.game.player.client;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises full combat ownership and HP changes with the shared footprint rules. */
public class Native950MeleeFootprintIntegrationTest {
    @Test public void farSideOfALargeCreatureFightsWithoutAnAnchorBasedLeash() {
        try (Fight fight = new Fight(64, new WorldTile(3264, 3230, 0))) {
            fight.register();
            assertEquals(1, Native950MeleeCombat.distanceToFootprint(fight.player, fight.npc, 64));
            assertNull(fight.combat.attack(fight.player, fight.npc));
            fight.step();
            assertEquals(40, fight.npc.getHitpoints());
            assertEquals(90, fight.player.getHitpoints());
            // Seventeen tiles beyond its east edge (the original910 home leash is16) releases ownership without another hit.
            fight.player.setLocation(3280, 3230, 0);
            fight.step();
            assertFalse(fight.npc.isNative950CombatEngaged());
            assertEquals(40, fight.npc.getHitpoints());
            assertEquals("Move closer to that creature.", fight.combat.attack(fight.player, fight.npc));
        }
    }

    @Test public void registrationChecksTheEntireBodyBeforeInstallingACombatProfile() {
        try (Fight fight = new Fight(3, new WorldTile(3199, 3201, 0))) {
            fight.grid.blockedSteps.add("3201,3201,1,0");
            fight.register();
            assertFalse(fight.combat.supports(fight.npc));
            assertNull(fight.npc.getNative950CombatProfile());
            fight.grid.blockedSteps.clear();
            fight.register();
            assertTrue(fight.combat.supports(fight.npc));
            assertNull(fight.combat.attack(fight.player, fight.npc));
            fight.step();
            assertEquals(40, fight.npc.getHitpoints());
        }
    }

    @Test public void deathWaitsForTheEntireHomeFootprintToClearBeforeRespawning() {
        try (Fight fight = new Fight(3, new WorldTile(3199, 3201, 0))) {
            fight.register();
            fight.npc.setHitpoints(1);
            assertNull(fight.combat.attack(fight.player, fight.npc));
            fight.step();
            assertTrue(fight.npc.isDead());
            fight.grid.blockedFloors.add("3201,3201"); // Origin is still clear.
            for (int tick = 0; tick < 8; tick++) fight.step();
            assertTrue(fight.npc.isDead());
            assertFalse(fight.npc.isNative950DeathVisible());
            assertNull(fight.npc.getNextWorldTile());
            fight.grid.blockedFloors.clear();
            fight.step();
            assertEquals(50, fight.npc.getHitpoints());
            assertEquals(3200, fight.npc.getNextWorldTile().getX());
            assertEquals(3200, fight.npc.getNextWorldTile().getY());
        }
    }

    private static final class Fight implements AutoCloseable, Native950MeleeCombat.Access {
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Grid grid = new Grid();
        final Player player;
        final NPC npc;
        final Native950MeleeCombat combat;
        final Native950NpcCombatProfile profile;
        Fight(int size, WorldTile playerTile) {
            player = Player.createNative950("footprint-test", playerTile, channel);
            player.setIndex(1); player.setActive(true);
            npc = NPC.createNative950(9999, new WorldTile(3200, 3200, 0), size);
            npc.setIndex(1);
            profile = new Native950NpcCombatProfile(9999, size, 2, 50, 8, 8, 10, 5, 1, 3, -1, -1, -1, 2, 12);
            combat = new Native950MeleeCombat(Thread.currentThread(), this, new Native950MeleeCombat.Rolls() {
                public boolean accurate(long attack, long defence) { return true; }
                public int damage(int maximum) { return maximum; }
            }, p -> new Native950MeleeCombat.Loadout(0, 0, 0, 4, -1, -1));
            combat.attach(player);
        }
        void register() { combat.register(npc, profile); }
        void step() { player.resetMasks(); npc.resetMasks(); combat.beforeMovement(); combat.afterMovement(); }
        public void activate(NPC n) { }
        public boolean player(Player p) { return p == player; }
        public boolean npc(NPC n) { return n == npc; }
        public boolean clear(WorldTile tile) { return clear(tile, 1); }
        public boolean clear(WorldTile tile, int size) { return Native950MeleeReach.clearFootprint(tile, size, grid); }
        public boolean reach(Entity from, Entity to) {
            return Native950MeleeReach.canReach(from, from.getSize(), to, to.getSize(), grid);
        }
        public boolean approach(Player p, NPC n) { return false; }
        public void close() { combat.clear(); channel.finishAndReleaseAll(); }
    }
    private static final class Grid implements Native950MeleeReach.Collision {
        final Set<String> blockedFloors = new HashSet<>(), blockedSteps = new HashSet<>();
        public boolean floorFree(int plane, int x, int y) { return !blockedFloors.contains(x + "," + y); }
        public boolean step(int plane, int x, int y, int dx, int dy) {
            return !blockedSteps.contains(x + "," + y + "," + dx + "," + dy);
        }
    }
}
