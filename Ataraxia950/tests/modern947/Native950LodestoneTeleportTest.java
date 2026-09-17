package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.content.Magic;
import com.rs.utils.Utils;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950LodestoneTeleportTest {
    private EmbeddedChannel channel;
    private Player player;
    private final WorldTile arrival=new WorldTile(3233,3221,0);
    private TestAccess access;
    private int relocations;
    @Before public void setup() {
        channel=new EmbeddedChannel();player=Player.createNative950("teleport-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true);player.setRunning(true);access=new TestAccess();
    }
    @After public void close() {player.getActionManager().forceStop();channel.finishAndReleaseAll();}
    @Test public void usesOriginalHomeTeleportTimelineAndMovesOnceToTheFinalArrival() throws Exception {
        Gate controller=controller();
        assertTrue(start());assertTrue(player.getActionManager().getAction() instanceof HomeTeleport);
        tick();assertEquals(16385,animation());assertEquals(3017,player.getNextGraphics1().getId());
        for(int i=2;i<=17;i++)tick();
        assertEquals(0,relocations);assertEquals(3217,player.getX());assertFalse(player.isLocked());
        tick();assertEquals(1,relocations);assertEquals(arrival.getX(),player.getX());assertEquals(arrival.getY(),player.getY());
        assertTrue(player.isLocked());assertEquals(2,controller.checked);assertEquals(1,controller.notified);
        tick();assertEquals(16386,animation());
        for(int i=20;i<=23;i++)tick();
        assertTrue(player.isLocked());tick();assertFalse(player.isLocked());assertEquals(16393,animation());
        tick();assertFalse(player.getActionManager().hasSkillWorking());assertEquals(1,relocations);
        assertEquals(2,controller.checked);assertEquals(1,controller.notified);
    }
    @Test public void actionCancellationBeforeDepartureNeverLeavesALateTeleport() {
        assertTrue(start());for(int i=0;i<8;i++)tick();player.getActionManager().forceStop();
        for(int i=0;i<30;i++)tick();assertEquals(0,relocations);assertFalse(player.isLocked());assertEquals(-1,animation());
    }
    @Test public void startingAndReceivingCombatAreBothBlockedAndMidCountdownCombatCancels() {
        player.setAttackingDelay(access.now);assertFalse(start());
        player.setAttackingDelay(0);player.setAttackedByDelay(access.now+6000);assertFalse(start());
        access.now+=16000;assertTrue(start());tick();
        player.setAttackedByDelay(access.now+6000);tick();
        assertFalse(player.getActionManager().hasSkillWorking());assertEquals(0,relocations);
    }
    @Test public void pendingAggroWithoutAnyDamageCannotBypassTheHomeTeleportGate() {
        NPC npc=NPC.createNative950(12353,new WorldTile(player),1);player.setAttackedBy(npc);
        assertFalse(start());assertSame(npc,player.getAttackedBy());assertEquals(0,relocations);
    }
    @Test public void currentAndNewControllerRestrictionsAreHonoured() throws Exception {
        Gate gate=controller();gate.allowed=false;assertFalse(start());assertEquals(1,gate.checked);
        gate.allowed=true;assertTrue(start());for(int i=0;i<17;i++)tick();gate.allowed=false;tick();
        assertFalse(player.getActionManager().hasSkillWorking());assertEquals(0,relocations);assertEquals(0,gate.notified);
    }
    @Test public void arrivalMustBeClearAtStartAndAgainAtTheCommit() {
        access.clear=false;assertFalse(start());access.clear=true;assertTrue(start());
        for(int i=0;i<17;i++)tick();access.clear=false;tick();
        assertFalse(player.getActionManager().hasSkillWorking());assertFalse(player.isLocked());assertEquals(0,relocations);
    }
    @Test public void assetFailureAndPendingTeleportDoNotReplaceTheExistingMove() {
        access.assets=false;assertFalse(start());access.assets=true;
        WorldTile other=new WorldTile(3220,3258,0);player.setNextWorldTile(other);
        assertFalse(start());assertSame(other,player.getNextWorldTile());
        player.setNextWorldTile(null);assertTrue(start());tick();player.setNextWorldTile(other);
        player.getActionManager().process();assertFalse(player.getActionManager().hasSkillWorking());
        assertSame(other,player.getNextWorldTile());
    }
    @Test public void walkingOrAnExternalPositionChangeCancelsTheCountdown() {
        assertTrue(start());tick();player.getWalkSteps().add(new Object[0]);
        player.getActionManager().process();assertFalse(player.getActionManager().hasSkillWorking());
        player.resetWalkSteps();assertTrue(start());tick();player.setLocation(3218,3258,0);tick();
        assertFalse(player.getActionManager().hasSkillWorking());assertEquals(0,relocations);
    }
    @Test public void deathAndLogoutCannotLeaveTheTeleportLockHeld() {
        assertTrue(start());for(int i=0;i<18;i++)tick();assertTrue(player.isLocked());
        player.setHitpoints(0);tick();assertFalse(player.isLocked());assertFalse(player.getActionManager().hasSkillWorking());
        player.setHitpoints(100);player.setLocation(3217,3258,0);assertTrue(start());for(int i=0;i<18;i++)tick();
        player.setActive(false);tick();assertFalse(player.isLocked());assertFalse(player.getActionManager().hasSkillWorking());
    }
    @Test public void cancellationDoesNotUnlockAnUnrelatedReplacementLock() {
        assertTrue(start());for(int i=0;i<18;i++)tick();player.lock();
        tick();assertFalse(player.getActionManager().hasSkillWorking());assertEquals(Long.MAX_VALUE,player.getLockDelay());
        player.unlock();
    }
    @Test public void successfulSelectionStartsPromptlyAfterAnOldSkillingDelay() {
        player.getActionManager().setActionDelay(8);access.assets=false;
        assertFalse(start());assertEquals(8,player.getActionManager().getActionDelay());
        access.assets=true;assertTrue(start());assertEquals(0,player.getActionManager().getActionDelay());
        tick();assertEquals(16385,animation());
        for(int i=2;i<=18;i++)tick();assertEquals(1,relocations);
    }
    @Test public void invalidActorAndDestinationAreRefusedWithoutStartingAnAction() {
        assertFalse(Native950LodestoneTeleport.start(null,arrival,access));
        player.setActive(false);assertFalse(start());player.setActive(true);
        assertFalse(Native950LodestoneTeleport.start(player,null,access));assertFalse(player.getActionManager().hasSkillWorking());
    }
    private boolean start(){return Native950LodestoneTeleport.start(player,arrival,access);}
    private void tick(){
        player.getActionManager().process();WorldTile pending=player.getNextWorldTile();
        if(pending!=null){relocations++;player.setLocation(pending);player.setNextWorldTile(null);}
        access.now+=600;
    }
    private int animation(){return player.getNextAnimation()==null?-2:player.getNextAnimation().getIds()[0];}
    private Gate controller() throws Exception {
        Gate gate=new Gate();ControlerManager manager=player.getControlerManager();
        Field current=ControlerManager.class.getDeclaredField("controler");current.setAccessible(true);current.set(manager,gate);
        Field inited=ControlerManager.class.getDeclaredField("inited");inited.setAccessible(true);inited.setBoolean(manager,true);
        return gate;
    }
    private static final class Gate extends Controller {
        boolean allowed=true;int checked,notified;
        @Override public void start() { }
        @Override public boolean processMagicTeleport(WorldTile tile){checked++;return allowed;}
        @Override public void magicTeleported(int type){assertEquals(Magic.MAGIC_TELEPORT,type);notified++;}
    }
    private static final class TestAccess implements Native950LodestoneTeleport.Access {
        long now=Utils.currentTimeMillis();boolean clear=true,assets=true;
        public long now(){return now;}public boolean assets(){return assets;}public boolean clear(WorldTile tile){return clear;}
    }
}
