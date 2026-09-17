package com.rs.game.player.client;

import com.rs.Settings;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.network.modern.Native950GameTransport;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Real player inventory/Skills with controllable world interleavings at the old action boundary. */
public final class Native950FiremakingTest {
    private EmbeddedChannel channel;
    private Player player;
    private WorldAccess world;
    @Before public void setup() {
        channel=new EmbeddedChannel(new Native950GameTransport(()->0,()->0,Thread.currentThread()));
        player=Player.createNative950("firemaking-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true);world=new WorldAccess();
        player.getInventory().items.set(0,new Item(590,1));
        player.getInventory().items.set(1,new Item(1511,1));
        player.getInventory().items.set(2,new Item(1511,1));
    }
    @After public void cleanup() { channel.finishAndReleaseAll(); }
    private Native950Firemaking.Attempt begin() { return Native950Firemaking.begin(player,Log.NORMAL,world); }
    private double xp() { return player.getSkills().getXp(Skills.FIREMAKING); }
    private double normalXp() { return 40*Settings.IRONMAN_XP*3.5; }

    @Test public void lightingTransfersExactlyOneLogThenCommitsFireAndOriginalXpOnce() {
        Native950Firemaking.Attempt attempt=begin();assertNotNull(attempt);
        assertEquals(1,player.getInventory().getAmountOf(1511));assertEquals(1,world.piles.size());
        assertEquals(2,attempt.delay());assertEquals(0,xp(),0);
        attempt.finish();attempt.finish();assertEquals(1,world.later.size());assertEquals(1,world.steps);
        attempt.stop(); // ActionManager always stops an action whose completion returns -1.
        world.run();world.run();
        assertTrue(world.piles.isEmpty());assertEquals(1,world.fires);assertEquals(normalXp(),xp(),1e-6);
        assertEquals(Log.NORMAL.getLife(),world.life);assertEquals(70755,world.fireId);
    }
    @Test public void cancellingLeavesTheOwnedLogForPickupAndNeverAwardsXp() {
        Native950Firemaking.Attempt attempt=begin();attempt.stop();attempt.finish();world.run();
        assertEquals(1,world.piles.size());assertEquals(player.getUsername(),world.piles.get(0).getOwner());
        assertTrue(world.piles.get(0).isInvisible());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void walkingOffTheAttemptTileCancelsCompletion() {
        Native950Firemaking.Attempt attempt=begin();player.setLocation(3218,3258,0);
        assertFalse(attempt.process());attempt.finish();world.run();assertEquals(0,world.fires);assertEquals(1,world.piles.size());
    }
    @Test public void pickingUpLogsDuringTheAnimationPreventsFreeFireAndXp() {
        Native950Firemaking.Attempt attempt=begin();world.piles.clear();
        assertFalse(attempt.process());attempt.finish();world.run();assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void anEqualReplacementPileIsNotTheAttemptResource() {
        Native950Firemaking.Attempt attempt=begin();FloorItem old=world.piles.remove(0);
        FloorItem replacement=new FloorItem(new Item(1511,1),new WorldTile(player),player,false,true);
        assertEquals(old,replacement);world.piles.add(replacement);
        assertFalse(attempt.process());attempt.finish();world.run();assertSame(replacement,world.piles.get(0));
        assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void aCompetingFireBeforeTheDeferredCommitLeavesTheLogUntouched() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();world.clear=false;world.run();
        assertEquals(1,world.piles.size());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void logoutAfterCompletionWasQueuedLeavesTheLogWithoutFireOrExperience() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();player.setActive(false);world.run();
        assertEquals(1,world.piles.size());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void deathAfterCompletionWasQueuedLeavesTheLogWithoutFireOrExperience() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();player.setHitpoints(0);world.run();
        assertEquals(1,world.piles.size());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void explicitInputCancellationRetiresEvenAQueuedCompletion() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();Native950Firemaking.cancelPending(player);world.run();
        assertEquals(1,world.piles.size());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void removalVetoPreventsFireAndExperience() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();world.remove=false;world.run();
        assertEquals(1,world.piles.size());assertEquals(0,world.fires);assertEquals(0,xp(),0);
    }
    @Test public void basicToolbeltLightsLogsWithoutCarriedTinderbox() {
        player.getInventory().items.set(0,null);Native950Firemaking.Attempt attempt=begin();assertNotNull(attempt);
        attempt.finish();world.run();assertEquals(1,player.getInventory().getAmountOf(1511));assertEquals(1,world.fires);assertEquals(normalXp(),xp(),1e-6);
    }
    @Test public void insufficientLevelLeavesOakLogsUnchanged() {
        player.getInventory().items.set(1,new Item(1521,1));
        assertNull(Native950Firemaking.begin(player,Log.OAK,world));assertEquals(1,player.getInventory().getAmountOf(1521));
    }
    @Test public void noLogsBlockedTileAndUnverifiedContentCannotBegin() {
        player.getInventory().items.set(1,null);player.getInventory().items.set(2,null);assertNull(begin());
        player.getInventory().items.set(1,new Item(1511,1));world.clear=false;assertNull(begin());
        world.clear=true;world.supported=false;assertNull(begin());assertEquals(1,player.getInventory().getAmountOf(1511));
    }
    @Test public void failedInventoryCommitCannotCreateAGroundLog() {
        world.consume=false;assertNull(begin());assertTrue(world.piles.isEmpty());assertEquals(2,player.getInventory().getAmountOf(1511));
    }
    @Test public void deadOrLockedPlayersCannotBeginOrContinue() {
        player.setHitpoints(0);assertNull(begin());player.setHitpoints(100);player.lock(10);assertNull(begin());player.unlock();
        Native950Firemaking.Attempt attempt=begin();player.setHitpoints(0);assertFalse(attempt.process());
    }
    @Test public void recentSuccessfulFireUsesTheOriginalQuickLightingDelay() {
        Native950Firemaking.Attempt attempt=begin();attempt.finish();world.run();
        Native950Firemaking.Attempt next=begin();assertNotNull(next);assertEquals(1,next.delay());
    }
    @Test public void bonfireConsumesOneLogAndUsesTheOriginalXpAndSixTickDelay() {
        WorldObject fire=new WorldObject(70755,10,0,3218,3258,0);
        assertEquals(6,Native950Firemaking.burnBonfireLog(player,Log.NORMAL,fire,world));
        assertEquals(1,player.getInventory().getAmountOf(1511));assertEquals(normalXp(),xp(),1e-6);
        assertEquals(16703,player.getNextAnimation().getIds()[0]);
    }
    @Test public void bonfireNeedsLogsAndLiveAdjacentFireButNoTinderbox() {
        WorldObject fire=new WorldObject(70755,10,0,3218,3258,0);
        player.getInventory().items.set(0,null);assertTrue(Native950Firemaking.checkBonfire(player,Log.NORMAL,fire,world));
        world.fireExists=false;assertEquals(-1,Native950Firemaking.burnBonfireLog(player,Log.NORMAL,fire,world));
        world.fireExists=true;player.setLocation(3220,3258,0);assertFalse(Native950Firemaking.checkBonfire(player,Log.NORMAL,fire,world));
        player.setLocation(3217,3258,1);assertFalse(Native950Firemaking.checkBonfire(player,Log.NORMAL,fire,world));assertEquals(0,xp(),0);
    }
    @Test public void bonfireCannotAwardXpWhenConsumptionFails() {
        world.consume=false;
        assertEquals(-1,Native950Firemaking.burnBonfireLog(player,Log.NORMAL,new WorldObject(70755,10,0,3218,3258,0),world));
        assertEquals(2,player.getInventory().getAmountOf(1511));assertEquals(0,xp(),0);
    }
    @Test public void assetPinsRejectAbsentAlteredAndUnknownDefinitions() {
        assertFalse(Native950Firemaking.matchesPin("sequence.16700",null));
        assertFalse(Native950Firemaking.matchesPin("sequence.16700",new byte[]{0}));
        assertFalse(Native950Firemaking.matchesPin("sequence.16701",new byte[]{0}));
        assertNull(Native950Firemaking.itemEntry(3239)); // Old Evil bark is modern Bark.
    }
    private final class WorldAccess implements Native950Firemaking.Access {
        boolean clear=true,remove=true,consume=true,supported=true,fireExists=true;
        int fires,steps,fireId,life;
        final List<FloorItem> piles=new ArrayList<FloorItem>();
        final List<Runnable> later=new ArrayList<Runnable>();
        public boolean supported(Log log) { return supported && log != null; }
        public boolean clear(WorldTile tile) { return clear; }
        public FloorItem drop(Player owner,Log log,WorldTile tile) {
            FloorItem pile=new FloorItem(new Item(log.getLogId(),1),tile,owner,false,true);piles.add(pile);return pile;
        }
        public boolean present(FloorItem pile) { for(FloorItem item:piles)if(item==pile)return true;return false; }
        public boolean remove(FloorItem pile) { return remove && piles.removeIf(item->item==pile); }
        public void fire(Log log,WorldTile tile) { fires++;fireId=log.getFireId();life=log.getLife(); }
        public void later(Runnable task) { later.add(task); }
        public void consume(Player player,Log log) { if(consume)player.getInventory().items.remove(new Item(log.getLogId(),1)); }
        public void stepAside(Player player) { steps++; }
        public boolean fireExists(WorldObject object) { return fireExists; }
        void run() { List<Runnable> tasks=new ArrayList<Runnable>(later);later.clear();for(Runnable task:tasks)task.run(); }
    }
}

