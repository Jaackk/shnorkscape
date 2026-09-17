package com.rs.game.player.client;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import org.junit.Test;import static org.junit.Assert.*;
public class Native950ImplingsTest {
 @Test public void originalWeightedTiersAndAbsentRareTierFallbackAreBounded() {
  assertSame(FlyingEntities.EARTH_IMPLING.getRarleyCommon(),Native950Implings.bucket(FlyingEntities.EARTH_IMPLING,599));
  assertSame(FlyingEntities.EARTH_IMPLING.getCommon(),Native950Implings.bucket(FlyingEntities.EARTH_IMPLING,600));
  assertSame(FlyingEntities.EARTH_IMPLING.getRare(),Native950Implings.bucket(FlyingEntities.EARTH_IMPLING,900));
  assertSame(FlyingEntities.EARTH_IMPLING.getExtremelyRare(),Native950Implings.bucket(FlyingEntities.EARTH_IMPLING,998));
  assertSame(FlyingEntities.BABY_IMPLING.getRare(),Native950Implings.bucket(FlyingEntities.BABY_IMPLING,999));
 }
 @Test(expected=IllegalArgumentException.class)public void invalidLootRollCannotSelectBucket(){Native950Implings.bucket(FlyingEntities.BABY_IMPLING,1000);}
 @Test public void implingPopulationCandidatesDoNotEnableUnrelatedNpcSpawns(){
  Native950SpawnScope scope=Native950SpawnScope.parse("12850");
  for(FlyingEntities data:FlyingEntities.values())if(data.isImpling())assertTrue(Native950SkillNpcPopulation.allows(scope,9271,data.getNpcId()));
  assertFalse(Native950SkillNpcPopulation.allows(scope,9271,50));
 }
}
