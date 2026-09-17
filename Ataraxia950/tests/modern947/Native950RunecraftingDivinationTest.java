package com.rs.game.player.client;
import com.rs.game.player.actions.divination.*;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import org.junit.Test;
import static org.junit.Assert.*;
public final class Native950RunecraftingDivinationTest {
 @Test public void runecraftingUsesOriginalAltarRulesAndGuaranteedMultiplierThresholds(){
  assertEquals(13,Native950Runecrafting.Altar.values().length);
  assertEquals(28,Native950Runecrafting.Altar.AIR.output(28,1));
  assertEquals(56,Native950Runecrafting.Altar.AIR.output(28,11));
  assertEquals(280,Native950Runecrafting.Altar.AIR.output(28,99));
  assertEquals(56,Native950Runecrafting.Altar.COSMIC.output(28,59));
  assertEquals(5.0,Native950Runecrafting.Altar.AIR.xp(),0);
  assertEquals(10.5,Native950Runecrafting.Altar.BLOOD.xp(),0);
  assertFalse(Native950Runecrafting.Altar.BODY.pureOnly);assertTrue(Native950Runecrafting.Altar.COSMIC.pureOnly);
 }
 @Test public void ordinaryHarvestCurvesRetainTierLevelAndCaps(){
  assertEquals(12,WispInfo.values().length);
  for(WispInfo info:WispInfo.values()){
   assertEquals(62,DivinationHarvest.ordinaryMemoryChance(info.getLevel(),info));
   assertEquals(92,DivinationHarvest.ordinaryMemoryChance(250,info));
   assertEquals(3,DivinationHarvest.ordinaryEnrichedChance(info.getLevel(),info));
   assertEquals(60,DivinationHarvest.ordinaryEnrichedChance(400,info));
  }
  assertEquals(1,DivinationHarvest.ordinaryEnergyAmount(54));assertEquals(2,DivinationHarvest.ordinaryEnergyAmount(55));assertEquals(3,DivinationHarvest.ordinaryEnergyAmount(75));
 }
 @Test public void conversionMoreXpRequiresEnergyAndEnrichmentAndBoonMultiplyExactlyOnce(){
  assertEquals(3,DivinationConvert.ordinaryXp(MemoryInfo.PALE,false,ConvertMode.CONVERT_TO_XP,false,false),0);
  assertEquals(3,DivinationConvert.ordinaryXp(MemoryInfo.PALE,false,ConvertMode.CONVERT_TO_MORE_XP,false,false),0);
  assertEquals(3.75,DivinationConvert.ordinaryXp(MemoryInfo.PALE,false,ConvertMode.CONVERT_TO_MORE_XP,true,false),0);
  assertEquals(123.75,DivinationConvert.ordinaryXp(MemoryInfo.INCANDESCENT,true,ConvertMode.CONVERT_TO_MORE_XP,true,true),0.0001);
  assertEquals(1.1,DivinationConvert.ordinaryXp(MemoryInfo.INCANDESCENT,true,ConvertMode.CONVERT_TO_ENERGY,false,true),0.0001);
 }
 @Test public void conversionEnergyCurveIsClampedAndDoesNotIndexPastLevel120(){
  assertEquals(1.5,DivinationConvert.ordinaryEnergyRate(MemoryInfo.PALE,1,false,false),0);
  assertEquals(1.8,DivinationConvert.ordinaryEnergyRate(MemoryInfo.PALE,120,false,false),0);
  assertEquals(5.2*1.5*1.1,DivinationConvert.ordinaryEnergyRate(MemoryInfo.INCANDESCENT,120,true,true),0.0001);
 }
}
