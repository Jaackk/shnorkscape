package com.rs.game.player.client;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950DungeoneeringTest {
    @Test public void allThreeDistinctCreditedDeathsAreRequiredBeforeAnyReward() {
        Native950Dungeoneering.Objectives o = new Native950Dungeoneering.Objectives();
        assertFalse(o.claim()); assertEquals(3, o.remaining());
        assertTrue(o.kill(2)); assertFalse(o.claim());
        assertTrue(o.kill(0)); assertFalse(o.claim());
        assertTrue(o.kill(1)); assertEquals(0, o.remaining()); assertTrue(o.claim());
    }
    @Test public void ReplayedForeignAndOutOfRangeDeathsNeverReduceTheObjectiveTwice() {
        Native950Dungeoneering.Objectives o = new Native950Dungeoneering.Objectives();
        for (int slot : new int[]{-1, 3, 31, Integer.MAX_VALUE}) assertFalse(o.kill(slot));
        assertTrue(o.kill(1)); for (int replay = 0; replay < 100; replay++) assertFalse(o.kill(1));
        assertEquals(2, o.remaining()); assertFalse(o.claim());
    }
    @Test public void ExitReplayAndAnyLaterDeathCannotReawardCompletion() {
        Native950Dungeoneering.Objectives o = new Native950Dungeoneering.Objectives();
        for (int i = 0; i < 3; i++) assertTrue(o.kill(i));
        assertTrue(o.claim());
        for (int i = 0; i < 20; i++) { assertFalse(o.claim()); assertFalse(o.kill(i % 3)); }
    }
    @Test public void OnlyTheTwoActualEntrancesAndFourActualExitsCanStartOrComplete() {
        assertTrue(Native950Dungeoneering.handlesObject(new WorldObject(48496,10,3,3445,3722,0),1));
        assertTrue(Native950Dungeoneering.handlesObject(new WorldObject(48496,10,1,3454,3722,0),1));
        assertFalse(Native950Dungeoneering.handlesObject(new WorldObject(48496,10,3,3446,3722,0),1));
        assertFalse(Native950Dungeoneering.handlesObject(new WorldObject(48496,10,3,3445,3722,1),1));
        assertFalse(Native950Dungeoneering.handlesObject(new WorldObject(48496,10,3,3445,3722,0),2));
        for (int y : Native950Dungeoneering.ROOM_Y) {
            assertTrue(Native950Dungeoneering.handlesObject(new WorldObject(51156,10,2,112,y+2,0),1));
            assertFalse(Native950Dungeoneering.handlesObject(new WorldObject(51156,10,2,113,y+2,0),1));
        }
    }
    @Test public void RingRoutesDoNotCaptureWearDestroyOrUnrelatedItems() {
        assertTrue(Native950Dungeoneering.handlesItem(15707,"Open party interface"));
        assertTrue(Native950Dungeoneering.handlesItem(15707,"Teleport to Daemonheim"));
        assertFalse(Native950Dungeoneering.handlesItem(15707,"Wear"));
        assertFalse(Native950Dungeoneering.handlesItem(15707,"Destroy"));
        assertFalse(Native950Dungeoneering.handlesItem(15708,"Teleport to Daemonheim"));
    }
    @Test public void RestoringInterruptedProgressNeverRestoresARewardableRun() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player p = Player.createNative950("dungrestore",new WorldTile(119,4999,0),channel);
            Native950Dungeoneering.restoreProgress(p,75,5,true);
            assertEquals(75,Native950Dungeoneering.tokens(p)); assertEquals(5,Native950Dungeoneering.completed(p));
            assertTrue(Native950Dungeoneering.interrupted(p)); assertNull(Native950Dungeoneering.state(p).claim());
            Native950Dungeoneering.onLogout(p);
            assertTrue(Native950Dungeoneering.outside().matches(p));
            assertFalse(Native950Dungeoneering.interrupted(p)); assertEquals(75,Native950Dungeoneering.tokens(p));
        } finally { channel.finishAndReleaseAll(); }
    }
    @Test public void CorruptNegativeCountersClampAndMaximumCountersSurviveHydration() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player p = Player.createNative950("dungbounds",Native950Dungeoneering.outside(),channel);
            Native950Dungeoneering.restoreProgress(p,-50,-9,false);
            assertEquals(0,Native950Dungeoneering.tokens(p)); assertEquals(0,Native950Dungeoneering.completed(p));
            Native950Dungeoneering.restoreProgress(p,Integer.MAX_VALUE,Integer.MAX_VALUE,false);
            assertEquals(Integer.MAX_VALUE,Native950Dungeoneering.tokens(p)); assertEquals(Integer.MAX_VALUE,Native950Dungeoneering.completed(p));
        } finally { channel.finishAndReleaseAll(); }
    }
    @Test public void RoomsHaveSeparateBoundariesAndNoPlaneOverlap() {
        for (int i = 0; i < Native950Dungeoneering.ROOM_Y.length; i++) {
            assertTrue(Native950Dungeoneering.inRoom(Native950Dungeoneering.entry(i),i));
            assertFalse(Native950Dungeoneering.inRoom(new WorldTile(111,Native950Dungeoneering.ROOM_Y[i]+7,0),i));
            assertFalse(Native950Dungeoneering.inRoom(new WorldTile(119,Native950Dungeoneering.ROOM_Y[i]+7,1),i));
            for (int j = 0; j < Native950Dungeoneering.ROOM_Y.length; j++) if (i != j)
                assertFalse(Native950Dungeoneering.inRoom(Native950Dungeoneering.entry(i),j));
        }
    }
}
