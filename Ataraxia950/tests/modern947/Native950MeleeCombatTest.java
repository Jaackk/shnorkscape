package com.rs.game.player.client;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Deterministic world-phase combat tests; real Player/NPC HP and queued masks, no sleeps/sockets. */
public class Native950MeleeCombatTest {
    private EmbeddedChannel channel;
    private Player player;
    private NPC npc;
    private FakeAccess access;
    private FixedRolls rolls;
    private Native950MeleeCombat combat;
    private final RewardRecorder rewards = new RewardRecorder();
    private boolean supported=true;
    @Before public void setup(){
        channel=new EmbeddedChannel();player=Player.createNative950("melee-test",new WorldTile(3217,3258,0),channel);
        player.setIndex(1);player.setActive(true);npc=NPC.createNative950(12353,new WorldTile(3218,3258,0),1);npc.setIndex(1);
        access=new FakeAccess();access.players.add(player);access.npcs.add(npc);rolls=new FixedRolls();
        combat=new Native950MeleeCombat(Thread.currentThread(),access,rolls,p->{
            if(!supported)throw new IllegalArgumentException("Unsupported equipment");
            return new Native950MeleeCombat.Loadout(0,0,0,4,-1,-1);
        }, rewards);
        combat.attach(player);combat.register(npc,profile(50,10,3));
    }
    @After public void cleanup(){combat.clear();channel.finishAndReleaseAll();}
    @Test public void nativePotionHitsPreserveEquipmentWithoutLegacyDegradation() {
        com.rs.game.item.Item helm=new com.rs.game.item.Item(20137);
        player.getEquipment().getItems().set(0,helm);
        player.setAttackingDelay(com.rs.utils.Utils.currentTimeMillis()+60000);
        for(int i=0;i<5;i++)player.processHit(new com.rs.game.Hit(player,10,com.rs.game.Hit.HitLook.REGULAR_DAMAGE));
        assertEquals(50,player.getHitpoints());
        assertEquals(5,player.getNextHits().size());
        assertSame(helm,player.getEquipment().getItem(0));
        assertNull(helm.getAttributes());
        assertEquals(20137,helm.getId());
    }
    @Test public void abilityWaitsForWorldPhaseAndHonoursCooldownAcrossTargetChanges(){
        player.getSkills().set(0,31);npc.setHitpoints(1000);
        assertNotNull(combat.ability(player,14682));
        assertNull(combat.attack(player,npc));assertNull(combat.ability(player,14682));
        assertEquals(1000,npc.getHitpoints());step();
        assertTrue(npc.getHitpoints()<1000);assertEquals(100,player.getHitpoints());
        assertNull("No cache means no invented sequence; real-cache acceptance checks the mapping",player.getNextAnimation());
        assertNotNull(combat.ability(player,14682));
        combat.stop(player);combat.attack(player,npc);
        assertNotNull(combat.ability(player,14682));
        for(int i=0;i<3;i++)step();
        assertNotNull(combat.ability(player,14682));
    }
    @Test public void validManualAbilityQueuesDuringGlobalCooldownAndStillRevalidatesBeforeExecution(){
        player.getSkills().set(0,31);npc.setHitpoints(1000);assertNull(combat.attack(player,npc));
        assertNull(combat.ability(player,14682));step();
        assertEquals("Fury queued.",combat.ability(player,14701));
        access.reachable=false;
        for(int i=0;i<3;i++)step();
        assertEquals("Queued ability must not damage after its target becomes unreachable",990,npc.getHitpoints());
    }
    @Test public void nativeTargetLifecycleKeepsThePlayerInCombatUntilStopped(){
        assertNull(combat.attack(player,npc));
        assertSame(npc,player.getTarget());assertSame(npc,combat.combatTarget(player));
        assertTrue(player.isUnderCombat());
        combat.stop(player);
        assertNull(player.getTarget());assertNull(combat.combatTarget(player));
    }
    @Test public void queuedBerserkSurvivesEarlyTicksUntilGlobalCooldownExpires(){
        player.getSkills().set(0,99);player.getCombatDefinitions().setSpecialAttackPercentage(100);npc.setHitpoints(1000);
        assertNull(combat.attack(player,npc));assertNull(combat.ability(player,14682));step();
        assertEquals("Berserk queued.",combat.ability(player,14707));
        step();assertFalse(combat.isBerserkActive(player));assertEquals(990,npc.getHitpoints());
        step();assertFalse(combat.isBerserkActive(player));assertEquals(990,npc.getHitpoints());
        step();assertTrue(combat.isBerserkActive(player));assertEquals(990,npc.getHitpoints());
        assertEquals(0,player.getCombatDefinitions().getSpecialAttackPercentage());
    }
    @Test public void thresholdAdmissionIsFiftyButConsumptionIsFifteen(){
        player.getSkills().set(0,99);npc.setHitpoints(10000);combat.attack(player,npc);
        player.getCombatDefinitions().setSpecialAttackPercentage(49);
        assertEquals("Threshold abilities require 50% adrenaline.",combat.ability(player,14704));
        player.getCombatDefinitions().setSpecialAttackPercentage(50);
        assertNull(combat.ability(player,14704));step();
        assertEquals(35,player.getCombatDefinitions().getSpecialAttackPercentage());
    }
    @Test public void queuedUltimateRechecksAdrenalineBeforeExecution(){
        player.getSkills().set(0,99);player.getCombatDefinitions().setSpecialAttackPercentage(100);npc.setHitpoints(1000);
        combat.attack(player,npc);combat.ability(player,14682);step();
        assertEquals("Berserk queued.",combat.ability(player,14707));
        player.getCombatDefinitions().setSpecialAttackPercentage(99);
        for(int i=0;i<4;i++)step();
        assertFalse(combat.isBerserkActive(player));
    }
    @Test public void ownCooldownCanQueueOnlyWithinOneGlobalCooldownOfReadiness(){
        player.getSkills().set(0,99);player.setDevelopmentGodMode(true);npc.setHitpoints(10000);
        combat.attack(player,npc);combat.ability(player,14682);step();
        assertTrue(combat.ability(player,14682).contains("25 ticks remaining"));
        for(int i=0;i<22;i++)step();
        assertEquals("Backhand queued.",combat.ability(player,14682));
        int hp=npc.getHitpoints();step();step();assertEquals(hp,npc.getHitpoints());
        step();assertTrue(npc.getHitpoints()<hp);
    }
    @Test public void nativeTargetDoesNotEnterTheUnverifiedLegacyTargetPanel(){
        boolean strict=Native950PacketDispatcher.isStrict();Native950PacketDispatcher.setStrict(true);
        try {
            assertNull(combat.attack(player,npc));
            player.getCombatDefinitions().processCombatStance();
            assertSame(npc,player.getTarget());assertSame(npc,combat.combatTarget(player));
            assertNull(player.getCombatDefinitions().getCurrentTarget());
        } finally {Native950PacketDispatcher.setStrict(strict);}
    }
    @Test public void abilityRechecksRangeAndStateBeforeDamage(){
        player.getSkills().set(0,31);combat.attack(player,npc);
        assertNull(combat.ability(player,14682));access.reachable=false;step();
        assertEquals(50,npc.getHitpoints());
        access.reachable=true;
        assertNull(combat.ability(player,14682));combat.stop(player);step();
        assertEquals(50,npc.getHitpoints());
    }
    @Test public void abilityRejectsWrongWeaponLowLevelAndUnsupportedEffects(){
        combat.attack(player,npc);
        assertNotNull(combat.ability(player,14682));player.getSkills().set(0,31);
        assertNotNull(combat.ability(player,14664));assertNotNull(combat.ability(player,14727));
        assertNotNull(combat.ability(player,14688));assertNull(combat.ability(player,14682));
    }
    @Test public void berserkIsAThirtyThreeTickMeleeBuffRatherThanAnImmediateHit(){
        player.getSkills().set(0,76);player.getCombatDefinitions().setSpecialAttackPercentage(100);npc.setHitpoints(1000);
        assertNull(combat.attack(player,npc));assertNull(combat.ability(player,14707));step();
        assertTrue(combat.isBerserkActive(player));assertEquals("Berserk must not deal an immediate hit",1000,npc.getHitpoints());
        for(int i=0;i<33;i++)step();
        assertFalse(combat.isBerserkActive(player));
    }
    @Test public void multiHitAbilitiesPublishTheirFirstHitBeforeTheirScheduledFollowUps(){
        player.getSkills().set(0,21);npc.setHitpoints(1000);combat.attack(player,npc);
        assertNull(combat.ability(player,14701));step();
        assertEquals("Fury must not publish all three hits on its opening tick",1,npc.getNextHits().size());
        assertEquals("Fury must schedule its other two hits for later world ticks",2,combat.pendingHitCount(player));
    }
    @Test public void cancellingCombatRemovesUnlandedMultiHitAbilityDamage(){
        player.getSkills().set(0,21);npc.setHitpoints(1000);combat.attack(player,npc);
        assertNull(combat.ability(player,14701));step();
        assertEquals(2,combat.pendingHitCount(player));
        combat.cancelAttack(player);
        assertEquals(0,combat.pendingHitCount(player));
    }
    @Test public void repeatedClicksRespectBothCadencesAndExchangeRealDamage(){
        assertNull(combat.attack(player,npc));step();
        assertEquals(40,npc.getHitpoints());assertEquals(90,player.getHitpoints());
        assertEquals(1,npc.getNextHits().size());assertEquals(10,npc.getNextHits().get(0).getDamage());
        for(int i=0;i<3;i++){assertNull(combat.attack(player,npc));step();}
        assertEquals(40,npc.getHitpoints());assertEquals(90,player.getHitpoints());
        step();assertEquals(30,npc.getHitpoints());assertEquals(90,player.getHitpoints());
        step();assertEquals(30,npc.getHitpoints());assertEquals(80,player.getHitpoints());
    }
    @Test public void missesKeepHpAndStillPublishAZeroHit(){
        rolls.accurate=false;combat.attack(player,npc);step();
        assertEquals(50,npc.getHitpoints());assertEquals(100,player.getHitpoints());
        assertEquals(0,npc.getNextHits().get(0).getDamage());assertFalse(npc.getNextHitBars().isEmpty());
    }
    @Test public void godModeBlocksLethalNativeHitsAndDisablesImmediately(){
        player.setHitpoints(5);player.setDevelopmentGodMode(true);
        combat.attack(player,npc);step();
        assertEquals(5,player.getHitpoints());assertFalse(player.isDead());
        assertEquals(0,player.getNextHits().get(0).getDamage());
        player.setDevelopmentGodMode(false);
        for(int i=0;i<5;i++)step();
        assertTrue(player.isDead());
    }
    @Test public void detachClearsDevelopmentToggles(){
        player.setDevelopmentGodMode(true);player.getPrayer().setInfinitePrayer(true);
        player.setInfiniteRunEnergy(true);player.setInfiniteCombatRunes(true);player.setInfiniteAmmunition(true);
        player.getCombatDefinitions().setInfiniteAdrenaline(true);combat.detach(player);
        assertFalse(player.isDevelopmentGodMode());assertFalse(player.getPrayer().isInfinitePrayer());
        assertFalse(player.getCombatDefinitions().isInfiniteAdrenaline());
        assertFalse(player.isInfiniteRunEnergy());assertFalse(player.isInfiniteCombatRunes());assertFalse(player.isInfiniteAmmunition());
    }
    @Test public void removingDiagnosticFighterStopsCombatAndCancelsRespawn(){
        combat.attack(player,npc);step();int hp=player.getHitpoints();
        combat.unregister(npc);assertFalse(combat.supports(npc));assertFalse(npc.isNative950CombatEngaged());
        for(int i=0;i<12;i++)step();assertEquals(hp,player.getHitpoints());
    }
    @Test public void wallsAndApproachPreventHitsUntilPostMovementReach(){
        access.reachable=false;combat.attack(player,npc);step();
        assertEquals(1,access.approaches);assertEquals(50,npc.getHitpoints());assertEquals(100,player.getHitpoints());
        access.reachable=true;step();assertEquals(40,npc.getHitpoints());
    }
    @Test public void failedApproachCancelsWithoutSwing(){
        access.reachable=false;access.routePossible=false;combat.attack(player,npc);step();
        assertEquals(50,npc.getHitpoints());assertFalse(npc.isNative950CombatEngaged());
        access.reachable=true;step();assertEquals(50,npc.getHitpoints());
    }
    @Test public void cancellationAndRetargetingCannotBypassPlayerCooldown(){
        combat.attack(player,npc);step();combat.stop(player);assertFalse(npc.isNative950CombatEngaged());
        assertNull(combat.attack(player,npc));step();assertEquals(40,npc.getHitpoints());
        combat.stop(player);for(int i=0;i<3;i++)step();assertEquals(40,npc.getHitpoints());
    }
    @Test public void lethalHitIsCappedAndCorpseStaysVisibleBeforeRespawn(){
        npc.setHitpoints(5);combat.attack(player,npc);step();
        assertEquals(0,npc.getHitpoints());assertEquals(5,npc.getNextHits().get(0).getDamage());
        assertTrue(npc.isNative950DeathVisible());assertEquals(100,player.getHitpoints());
        assertNotNull(combat.attack(player,npc));
        step();step();assertTrue(npc.isNative950DeathVisible());
        step();assertFalse(npc.isNative950DeathVisible());assertTrue(npc.isDead());
        step();step();step();assertEquals(50,npc.getHitpoints());assertFalse(npc.isNative950CombatEngaged());
        assertNotNull(npc.getNextWorldTile());assertEquals(3218,npc.getNextWorldTile().getX());
    }
    @Test public void blockedRespawnStaysDeadUntilOriginalTileBecomesClear(){
        npc.setHitpoints(1);combat.attack(player,npc);step();access.clear=false;
        for(int i=0;i<9;i++)step();assertTrue(npc.isDead());assertFalse(npc.isNative950DeathVisible());
        access.clear=true;step();assertEquals(50,npc.getHitpoints());
    }
    @Test public void playerDeathStopsCombatAndRecoversWithoutChangingInventory(){
        player.setHitpoints(5);combat.attack(player,npc);step();
        assertTrue(player.isDead());assertTrue(player.isLocked());assertFalse(npc.isNative950CombatEngaged());
        for(int i=0;i<6;i++)step();assertEquals(player.getMaxHitpoints(),player.getHitpoints());assertFalse(player.isLocked());
        assertEquals(3217,player.getNextWorldTile().getX());assertEquals(3258,player.getNextWorldTile().getY());
        assertEquals(0,player.getInventory().getItems().getUsedSlots());
    }
    @Test public void disconnectAndReusedNpcIdentityReleaseOwnership(){
        combat.attack(player,npc);access.npcs.remove(npc);step();assertFalse(npc.isNative950CombatEngaged());
        assertFalse(combat.supports(npc));combat.detach(player);assertNull(player.getNative950Combat());
        assertNotNull(combat.attack(player,npc));
    }
    @Test public void lockedTeleportingOrUnsupportedPlayerNeverDealsDamage(){
        player.lock(1);assertNotNull(combat.attack(player,npc));player.unlock();
        supported=false;assertNotNull(combat.attack(player,npc));supported=true;
        combat.attack(player,npc);player.setNextWorldTile(new WorldTile(3220,3258,0));step();
        assertEquals(50,npc.getHitpoints());assertFalse(npc.isNative950CombatEngaged());
    }
    @Test public void secondPlayerCannotStealAnActiveSingleCombatCreature(){
        EmbeddedChannel otherChannel=new EmbeddedChannel();
        try{Player other=Player.createNative950("other",new WorldTile(3218,3257,0),otherChannel);other.setActive(true);other.setIndex(2);access.players.add(other);combat.attach(other);
            assertNull(combat.attack(player,npc));assertNotNull(combat.attack(other,npc));combat.stop(player);assertNull(combat.attack(other,npc));combat.detach(other);
        }finally{otherChannel.finishAndReleaseAll();}
    }
    @Test public void equipmentChangesCancelBeforeNextSwing(){
        combat.attack(player,npc);step();supported=false;step();assertEquals(40,npc.getHitpoints());assertFalse(npc.isNative950CombatEngaged());
    }
    @Test public void clearedWorldCannotRespawnAnOrphanedCorpse(){
        npc.setHitpoints(1);combat.attack(player,npc);step();combat.clear();
        for(int i=0;i<10;i++)step();assertTrue(npc.isDead());assertFalse(combat.supports(npc));
    }
    @Test public void walkingCancelsOnlyThePlayerAttackWhileNpcKeepsFollowing() {
        combat.attack(player,npc);step();
        combat.cancelAttack(player);access.reachable=false;step();
        assertTrue(npc.isNative950CombatEngaged());
        assertSame(player,access.followTarget);
        assertEquals(1,access.follows);
        assertEquals("walking must not restart the player's approach",0,access.approaches);
        assertEquals("walking must not cause another player hit",40,npc.getHitpoints());
        assertEquals(1,rewards.hitCalls);
    }
    @Test public void aMissStillProvokesNpcPursuitButNeverAwardsExperience() {
        rolls.accurate=false;combat.attack(player,npc);step();
        combat.cancelAttack(player);access.reachable=false;step();
        assertSame(player,access.followTarget);
        assertTrue(npc.isNative950CombatEngaged());
        assertEquals(0,rewards.hitCalls);
        assertEquals(0,rewards.deathCalls);
    }
    @Test public void disabledAutoRetaliateDoesNotResumePlayerSwingsAfterWalking() {
        player.getCombatDefinitions().setAutoRetaliate(false);
        combat.attack(player,npc);step();combat.cancelAttack(player);
        for(int i=0;i<10;i++)step();
        assertEquals("NPC keeps retaliating",70,player.getHitpoints());
        assertEquals("player action stays cancelled",40,npc.getHitpoints());
        assertEquals(1,rewards.hitCalls);
    }
    @Test public void enabledAutoRetaliateResumesAfterNpcNextHitWhenPlayerIsIdle() {
        combat.attack(player,npc);step();combat.cancelAttack(player);
        for(int i=0;i<5;i++)step();
        assertEquals("NPC hit resumes the action without an immediate extra player hit",40,npc.getHitpoints());
        step();assertEquals(30,npc.getHitpoints());assertEquals(2,rewards.hitCalls);
    }
    @Test public void walkingAwayBeforeTheFirstSwingDoesNotProvokeTheNpc() {
        access.reachable=false;combat.attack(player,npc);combat.cancelAttack(player);step();
        assertFalse(npc.isNative950CombatEngaged());assertEquals(0,access.follows);
        assertEquals(0,rewards.hitCalls);
    }
    @Test public void leavingSpawnLeashReleasesPlayerAndReturnsNpcToItsOriginalHome() {
        combat.attack(player,npc);step();combat.cancelAttack(player);
        npc.setLocation(3221,3258,0);player.setLocation(3235,3258,0);step();
        assertNull(player.getAttackedBy());
        assertTrue("return journey keeps random walking suppressed",npc.isNative950CombatEngaged());
        player.setLocation(3217,3258,0);
        assertEquals("That creature is returning home.",combat.attack(player,npc));
        access.moveOnFollow=true;step();
        assertFalse("the return target is a tile, not the departed player",access.followTarget instanceof Player);
        assertEquals(3218,npc.getX());assertEquals(3258,npc.getY());
        step();assertFalse(npc.isNative950CombatEngaged());
        assertNull(combat.attack(player,npc));
    }
    @Test public void detachHardStopsBothCombatOwnersAndCannotKeepFollowingTheDisconnectedPlayer() {
        combat.attack(player,npc);step();combat.cancelAttack(player);combat.detach(player);
        access.reachable=false;for(int i=0;i<5;i++)step();
        assertNull(player.getNative950Combat());assertNull(npc.getAttackedBy());
        assertFalse(npc.isNative950CombatEngaged());assertEquals(0,access.follows);
        assertEquals(1,rewards.hitCalls);assertEquals(0,rewards.deathCalls);
    }
    @Test public void rewardHooksReceiveOnlyCappedDamageAndOneDeathPerLife() {
        npc.setHitpoints(5);combat.attack(player,npc);step();
        assertEquals(1,rewards.hitCalls);assertEquals(5,rewards.damage);
        assertEquals(1,rewards.deathCalls);assertSame(player,rewards.deathOwner);
        for(int i=0;i<10;i++)step();
        assertEquals(1,rewards.hitCalls);assertEquals(1,rewards.deathCalls);
        assertEquals("the original damage ledger is cleared at respawn",0,npc.getReceivedDamage().size());
    }
    @Test public void originalDamageLedgerChoosesDropOwnerInsteadOfAlwaysGivingLootToFinalHitter() {
        EmbeddedChannel otherChannel=new EmbeddedChannel();
        try {
            Player other=Player.createNative950("earlier-hitter",new WorldTile(3218,3257,0),otherChannel);
            other.setActive(true);other.setIndex(2);
            npc.addReceivedDamage(other,20);npc.setHitpoints(5);
            combat.attack(player,npc);step();
            assertSame(other,rewards.deathOwner);
            assertEquals("XP remains tied to the final hitter's actual five damage",5,rewards.damage);
        } finally {otherChannel.finishAndReleaseAll();}
    }
    @Test public void repeatedFailedPursuitReleasesTheTargetAfterTheBoundedRetryWindow() {
        combat.attack(player,npc);step();combat.cancelAttack(player);
        access.reachable=false;access.routePossible=false;
        for(int i=0;i<25;i++)step();
        assertTrue(npc.isNative950CombatEngaged());
        step();assertFalse(npc.isNative950CombatEngaged());
        assertNull(npc.getAttackedBy());assertEquals(26,access.follows);
        assertEquals(1,rewards.hitCalls);
    }
    @Test public void blockedReturnPathFallsBackToTheVerifiedClearHomeAfterBoundedRetries() {
        combat.attack(player,npc);step();npc.setLocation(3221,3258,0);combat.stop(player);
        access.routePossible=false;
        for(int i=0;i<26;i++)step();
        assertNotNull(npc.getNextWorldTile());
        assertEquals(3218,npc.getNextWorldTile().getX());
        assertEquals(3258,npc.getNextWorldTile().getY());
        assertFalse(npc.isNative950CombatEngaged());
    }
    @Test public void blockedHomeDoesNotTeleportNpcInsideSceneryDuringReturnRecovery() {
        combat.attack(player,npc);step();npc.setLocation(3221,3258,0);combat.stop(player);
        access.routePossible=false;access.clear=false;
        for(int i=0;i<30;i++)step();
        assertNull(npc.getNextWorldTile());assertTrue(npc.isNative950CombatEngaged());
        access.clear=true;step();
        assertNotNull(npc.getNextWorldTile());assertFalse(npc.isNative950CombatEngaged());
    }
    private static final class RewardRecorder implements Native950MeleeCombat.Rewards {
        int hitCalls,deathCalls,damage;Player deathOwner;
        public void hit(Player player,NPC npc,int value){hitCalls++;damage+=value;}
        public void death(NPC npc,Player owner){deathCalls++;deathOwner=owner;}
    }
    private void step(){player.resetMasks();npc.resetMasks();combat.beforeMovement();combat.afterMovement();}
    private static Native950NpcCombatProfile profile(int hp,int maxHit,int respawn){return new Native950NpcCombatProfile(12353,1,2,hp,8,8,maxHit,5,3,respawn,-1,-1,-1,2,12);}
    private static final class FixedRolls implements Native950MeleeCombat.Rolls {boolean accurate=true;public boolean accurate(long attack,long defence){return accurate;}public int damage(int maximum){return maximum;}}
    private static final class FakeAccess implements Native950MeleeCombat.Access {
        final Set<Player> players=Collections.newSetFromMap(new IdentityHashMap<Player,Boolean>());
        final Set<NPC> npcs=Collections.newSetFromMap(new IdentityHashMap<NPC,Boolean>());
        boolean clear=true,reachable=true,routePossible=true,moveOnFollow;int approaches,follows;WorldTile followTarget;
        public void activate(NPC npc){}
        public boolean player(Player p){return players.contains(p);}
        public boolean npc(NPC n){return npcs.contains(n);}
        public boolean clear(WorldTile tile){return clear;}
        public boolean reach(Entity a,Entity b){return reachable&&a.getPlane()==b.getPlane();}
        public boolean approach(Player p,NPC n){approaches++;return routePossible;}
        public boolean follow(NPC npc,WorldTile target){follows++;followTarget=target;if(moveOnFollow)npc.setLocation(target);return routePossible;}
    }
}
