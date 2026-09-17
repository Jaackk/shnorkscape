package com.rs.game.player.client;
import com.rs.game.player.actions.slayer.SlayerMasterData;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950CombatSecondPassTest {
 @Test public void airSpellsChangeAtEachActualLevelBoundary(){
  int[] levels={1,16,17,40,41,61,62,80,81,120};int[] runes={1,1,2,2,3,3,4,4,5,5};
  for(int i=0;i<levels.length;i++)assertEquals(runes[i],Native950AutoSpells.select(levels[i]).airRunes);
 }
 @Test public void magicDamageUsesLowestOfPlayerWeaponAndSpellCaps(){
  assertEquals(16,Native950AutoSpells.damageTier(99,99,Native950AutoSpells.Spell.STRIKE));
  assertEquals(30,Native950AutoSpells.damageTier(99,30,Native950AutoSpells.Spell.SURGE));
  assertEquals(50,Native950AutoSpells.damageTier(50,99,Native950AutoSpells.Spell.SURGE));
 }
 @Test public void slayerPointsStartOnFifthAndUseFiftyTaskMultiplier15(){
  for(int n=1;n<5;n++)assertEquals(0,Native950Slayer.taskPoints(SlayerMasterData.MORVRAN,n));
  assertEquals(20,Native950Slayer.taskPoints(SlayerMasterData.MORVRAN,5));
  assertEquals(100,Native950Slayer.taskPoints(SlayerMasterData.MORVRAN,10));
  assertEquals(300,Native950Slayer.taskPoints(SlayerMasterData.MORVRAN,50));
  assertEquals(0,Native950Slayer.taskPoints(SlayerMasterData.TURAEL,50));
 }
 @Test public void paidCancelIsAtomicAndPreservesStreakAndCompletedCounters(){
  Native950Slayer.State s=task(29);assertFalse(s.cancelForPoints());assertEquals(29,s.points());assertTrue(s.hasTask());
  s=task(30);assertTrue(s.cancelForPoints());assertEquals(0,s.points());assertFalse(s.hasTask());assertEquals(9,s.streak());assertEquals(12,s.completed());
  assertFalse(s.cancelForPoints());Map<String,Integer> saved=new HashMap<>();s.writeSettings(saved);
  Native950Slayer.State loaded=new Native950Slayer.State();loaded.restore(saved);assertFalse(loaded.hasTask());assertEquals(9,loaded.streak());
 }
 @Test public void rewardSpendingCannotOverdrawOrAcceptNegativeCosts(){
  Native950Slayer.State s=task(400);assertFalse(s.spend(-1));assertFalse(s.spend(401));assertTrue(s.spend(400));assertFalse(s.spend(400));assertEquals(0,s.points());
 }
 @Test public void allNineDistinctChallengeDeathsAreRequiredAndRewardCannotReplay(){
  Native950Dungeoneering.Objectives o=new Native950Dungeoneering.Objectives(9);
  for(int i=0;i<8;i++){assertTrue(o.kill(i));assertFalse(o.kill(i));assertFalse(o.claim());}
  assertEquals(1,o.remaining());assertTrue(o.kill(8));assertTrue(o.claim());assertFalse(o.claim());
 }
 @Test public void challengeRewardsAreCapturedAtEntryAndBounded(){
  Native950Dungeoneering.Run low=new Native950Dungeoneering.Run(0,true,20),high=new Native950Dungeoneering.Run(0,true,120);
  assertEquals(450,low.baseXp);assertEquals(1450,high.baseXp);assertEquals(9,high.objectives.total);
  assertEquals(high.baseXp,new Native950Dungeoneering.Run(0,true,Integer.MAX_VALUE).baseXp);
  assertEquals(150,new Native950Dungeoneering.Run(0,false,120).baseXp);
 }
 private static Native950Slayer.State task(int points){Map<String,Integer> m=new HashMap<>();
  m.put(Native950Slayer.TASK,Native950Slayer.code(SlayerTaskData.GOBLINS));m.put(Native950Slayer.MASTER,SlayerMasterData.TURAEL.getNpcId());
  m.put(Native950Slayer.REMAINING,10);m.put(Native950Slayer.COMPLETED,12);m.put(Native950Slayer.STREAK,9);m.put(Native950Slayer.POINTS,points);
  Native950Slayer.State s=new Native950Slayer.State();s.restore(m);return s;}
}
