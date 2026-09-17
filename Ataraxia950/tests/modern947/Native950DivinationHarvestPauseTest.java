package com.rs.game.player.client;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import org.junit.Test;
import static org.junit.Assert.*;
public final class Native950DivinationHarvestPauseTest {
 private NPC wisp(){NPC npc=NPC.createNative950(18150,new WorldTile(3218,3258,0),1);npc.setNative950Wander(5);return npc;}
 @Test public void twoHarvestersPauseUntilLastOwnerStopsAndDiscardQueuedPaths(){
  NPC npc=wisp();npc.addWalkSteps(3219,3258,1,false);assertTrue(npc.hasWalkSteps());
  Native950Divination.HarvestPause first=Native950Divination.pauseHarvestWander(npc),second=Native950Divination.pauseHarvestWander(npc);
  assertSame(first,second);assertEquals(0,npc.getNative950Wander());assertFalse(npc.hasWalkSteps());
  Native950Divination.releaseHarvestWander(npc,first);assertEquals(0,npc.getNative950Wander());
  npc.addWalkSteps(3219,3258,1,false);Native950Divination.releaseHarvestWander(npc,second);
  assertEquals(5,npc.getNative950Wander());assertFalse(npc.hasWalkSteps());assertEquals(0,Native950Divination.activeHarvestPauses());
  Native950Divination.releaseHarvestWander(npc,second);assertEquals(5,npc.getNative950Wander());
 }
 @Test public void laterExternalWanderChangeIsPreserved(){
  NPC npc=wisp();Native950Divination.HarvestPause pause=Native950Divination.pauseHarvestWander(npc);
  npc.setNative950Wander(2);npc.addWalkSteps(3219,3258,1,false);
  Native950Divination.releaseHarvestWander(npc,pause);
  assertEquals(2,npc.getNative950Wander());assertTrue(npc.hasWalkSteps());assertEquals(0,Native950Divination.activeHarvestPauses());
 }
 @Test public void finishedNpcReleasesMapEntryEvenWhenOtherHarvesterHasNotStoppedYet(){
  NPC npc=wisp();Native950Divination.HarvestPause a=Native950Divination.pauseHarvestWander(npc),b=Native950Divination.pauseHarvestWander(npc);
  npc.setFinished(true);Native950Divination.releaseHarvestWander(npc,a);assertEquals(0,Native950Divination.activeHarvestPauses());
  Native950Divination.releaseHarvestWander(npc,b);assertEquals(0,Native950Divination.activeHarvestPauses());
 }
}
