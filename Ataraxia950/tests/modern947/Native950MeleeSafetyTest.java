package com.rs.game.player.client;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Encounter failure boundaries and login recovery using real actors, without legacy world callbacks. */
public final class Native950MeleeSafetyTest {
    private EmbeddedChannel firstChannel, secondChannel;
    private Player first, second;
    private NPC firstNpc, secondNpc;
    private Access access;
    private Native950MeleeCombat combat;
    private Player brokenLoadout;

    @Before public void setup() {
        firstChannel=new EmbeddedChannel(); secondChannel=new EmbeddedChannel();
        first=player("melee-safety-one",3210,3215,1,firstChannel);
        second=player("melee-safety-two",3225,3215,2,secondChannel);
        firstNpc=NPC.createNative950(12353,new WorldTile(3211,3215,0),1); firstNpc.setIndex(1);
        secondNpc=NPC.createNative950(12353,new WorldTile(3226,3215,0),1); secondNpc.setIndex(2);
        access=new Access();
        access.players.add(first); access.players.add(second);
        access.npcs.add(firstNpc); access.npcs.add(secondNpc);
        combat=new Native950MeleeCombat(Thread.currentThread(),access,new Native950MeleeCombat.Rolls() {
            @Override public boolean accurate(long attack,long defence) { return true; }
            @Override public int damage(int maximum) { return maximum; }
        },p -> {
            if(p==brokenLoadout) throw new IllegalStateException("Injected encounter loadout failure");
            return new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1);
        });
        combat.attach(first); combat.attach(second);
        combat.register(firstNpc,profile()); combat.register(secondNpc,profile());
    }

    @After public void cleanup() {
        if(combat!=null) combat.clear();
        if(firstChannel!=null) firstChannel.finishAndReleaseAll();
        if(secondChannel!=null) secondChannel.finishAndReleaseAll();
    }

    @Test public void beforeMovementFailureClosesOnlyItsEncounterAndHealthyCombatContinues() {
        beginBoth();
        access.brokenReach=first;
        combat.beforeMovement();
        combat.afterMovement();
        assertFailedFirstAndHealthySecond();
        // Isolation remains true on the following tick, not only during this iteration.
        combat.beforeMovement(); combat.afterMovement();
        assertEquals(40,secondNpc.getHitpoints());
        assertTrue(secondChannel.isActive());
    }

    @Test public void afterMovementFailureClosesOnlyItsEncounterAndHealthyCombatContinues() {
        beginBoth();
        combat.beforeMovement();
        brokenLoadout=first;
        combat.afterMovement();
        assertFailedFirstAndHealthySecond();
    }

    @Test public void newAttackStopsAnExistingActionExactlyOnce() {
        ProbeAction action=new ProbeAction();
        assertTrue(first.getActionManager().setAction(action));
        assertSame(action,first.getActionManager().getAction());
        assertNull(combat.attack(first,firstNpc));
        assertEquals(1,action.stops);
        assertNull(first.getActionManager().getAction());
        assertNull(combat.attack(first,firstNpc));
        assertEquals(1,action.stops);
    }

    @Test public void savedZeroHpRecoversOnAttachWithoutChangingInventory() {
        combat.detach(first);
        Item coins=new Item(995,345), logs=new Item(1511,1);
        first.getInventory().getItems().set(3,coins);
        first.getInventory().getItems().set(19,logs);
        first.setHitpoints(0); first.lock(10);
        combat.attach(first);
        assertTrue(first.isDead());
        combat.beforeMovement();
        assertEquals(first.getMaxHitpoints(),first.getHitpoints());
        assertFalse(first.isLocked());
        assertReturnTile(first.getNextWorldTile());
        assertSame(combat,first.getNative950Combat());
        assertTrue(firstChannel.isActive());
        assertEquals(2,first.getInventory().getItems().getUsedSlots());
        assertSame(coins,first.getInventory().getItems().get(3));
        assertEquals(345,coins.getAmount());
        assertSame(logs,first.getInventory().getItems().get(19));
        assertNull(first.getInventory().getItems().get(0));
    }

    @Test public void blockedSavedPlayerRecoveryWaitsWhileOtherEncountersContinue() {
        attachDeadFirst();
        access.blockReturn=true;
        assertNull(combat.attack(second,secondNpc));
        combat.beforeMovement(); combat.afterMovement();
        assertTrue(first.isDead());
        assertNull(first.getNextWorldTile());
        assertTrue(firstChannel.isActive());
        assertEquals(40,secondNpc.getHitpoints());
        assertEquals(90,second.getHitpoints());
        assertTrue(secondChannel.isActive());
        access.blockReturn=false;
        combat.beforeMovement();
        assertEquals(first.getMaxHitpoints(),first.getHitpoints());
        assertReturnTile(first.getNextWorldTile());
    }

    @Test public void failedSavedPlayerRecoveryDoesNotAbortOtherEncounters() {
        attachDeadFirst();
        access.brokenRecovery=first;
        assertNull(combat.attack(second,secondNpc));
        combat.beforeMovement(); combat.afterMovement();
        assertFalse(firstChannel.isActive());
        assertTrue(first.isDead());
        assertTrue(secondChannel.isActive());
        assertEquals(40,secondNpc.getHitpoints());
        assertEquals(90,second.getHitpoints());
    }

    @Test public void foreignThreadCannotMutateCombatOwnership() throws InterruptedException {
        AtomicReference<Throwable> thrown=new AtomicReference<>();
        Thread foreign=new Thread(() -> {
            try { combat.attack(first,firstNpc); }
            catch(Throwable failure) { thrown.set(failure); }
        },"native950-melee-foreign-owner-test");
        foreign.setDaemon(true);
        foreign.start(); foreign.join(5000);
        assertFalse("Owner guard should return immediately",foreign.isAlive());
        assertTrue(thrown.get() instanceof IllegalStateException);
        assertEquals("Native melee state belongs to its world thread",thrown.get().getMessage());
        assertFalse(firstNpc.isNative950CombatEngaged());
        assertEquals(50,firstNpc.getHitpoints());
        assertEquals(100,first.getHitpoints());
        assertTrue(firstChannel.isActive());
        assertNull(combat.attack(first,firstNpc)); // The owner remains able to start this encounter.
    }

    private void beginBoth() {
        assertNull(combat.attack(first,firstNpc));
        assertNull(combat.attack(second,secondNpc));
    }
    private void assertFailedFirstAndHealthySecond() {
        assertFalse(firstChannel.isActive());
        assertFalse(firstNpc.isNative950CombatEngaged());
        assertEquals(50,firstNpc.getHitpoints());
        assertEquals(100,first.getHitpoints());
        assertTrue(secondChannel.isActive());
        assertTrue(secondNpc.isNative950CombatEngaged());
        assertEquals(40,secondNpc.getHitpoints());
        assertEquals(90,second.getHitpoints());
    }
    private void attachDeadFirst() {
        combat.detach(first); first.setHitpoints(0); combat.attach(first);
    }
    private static void assertReturnTile(WorldTile tile) {
        assertNotNull(tile); assertEquals(3217,tile.getX()); assertEquals(3258,tile.getY()); assertEquals(0,tile.getPlane());
    }
    private static Player player(String name,int x,int y,int index,EmbeddedChannel channel) {
        Player p=Player.createNative950(name,new WorldTile(x,y,0),channel);
        p.setIndex(index); p.setActive(true); return p;
    }
    private static Native950NpcCombatProfile profile() {
        return new Native950NpcCombatProfile(12353,1,2,50,8,8,10,5,1,3,-1,-1,-1,2,12);
    }
    private static final class ProbeAction extends Action {
        int stops;
        @Override public boolean start(Player player) { return true; }
        @Override public boolean process(Player player) { return true; }
        @Override public int processWithDelay(Player player) { return 1; }
        @Override public void stop(Player player) { stops++; }
    }
    private static final class Access implements Native950MeleeCombat.Access {
        final Set<Player> players=Collections.newSetFromMap(new IdentityHashMap<Player,Boolean>());
        final Set<NPC> npcs=Collections.newSetFromMap(new IdentityHashMap<NPC,Boolean>());
        Player brokenReach,brokenRecovery;
        boolean blockReturn;
        @Override public void activate(NPC npc) {}
        @Override public boolean player(Player player) {
            if(player==brokenRecovery) throw new IllegalStateException("Injected saved-player recovery failure");
            return players.contains(player)&&player.getRealChannel().isActive();
        }
        @Override public boolean npc(NPC npc) { return npcs.contains(npc); }
        @Override public boolean clear(WorldTile tile) {
            return !blockReturn||tile.getX()!=3217||tile.getY()!=3258||tile.getPlane()!=0;
        }
        @Override public boolean reach(Entity from,Entity to) {
            if(from==brokenReach) throw new IllegalStateException("Injected encounter reach failure");
            return from.getPlane()==to.getPlane();
        }
        @Override public boolean approach(Player player,NPC npc) { return true; }
    }
}
