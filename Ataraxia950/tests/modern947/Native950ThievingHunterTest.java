package com.rs.game.player.client;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.actions.thieving.def.Stalls;
import org.junit.Test;
import static org.junit.Assert.*;
public final class Native950ThievingHunterTest {
 @Test public void multiLootRequiresBothOriginalThievingAndAgilityThresholds(){
  assertEquals(1,Native950Thieving.lootMultiplier(PickPocketableNPC.MAN,99,99,false));
  assertEquals(1,Native950Thieving.lootMultiplier(PickPocketableNPC.MAN,1,99,true));
  assertEquals(2,Native950Thieving.lootMultiplier(PickPocketableNPC.MAN,31,1,true));
  assertEquals(3,Native950Thieving.lootMultiplier(PickPocketableNPC.MAN,31,11,true));
  assertEquals(4,Native950Thieving.lootMultiplier(PickPocketableNPC.MAN,31,21,true));
 }
 @Test public void originalPickpocketCurveHasFailuresAndLevelSensitiveSuccess(){
  assertFalse(Native950Thieving.success(1,1,0,1,1.0));
  assertTrue(Native950Thieving.success(1,1,2,1,1.0));
  assertFalse(Native950Thieving.success(40,55,40,40,1.0));
  assertTrue(Native950Thieving.success(99,55,99,40,1.0));
 }
 @Test public void onlyActualStallOptionsAreAdmitted(){
  assertTrue(Native950Thieving.isStealOption("Steal-from"));assertTrue(Native950Thieving.isStealOption("Steal from"));
  assertFalse(Native950Thieving.isStealOption("Search"));assertFalse(Native950Thieving.isStealOption("Trade"));assertFalse(Native950Thieving.isStealOption(null));
 }
 @Test public void stallCooldownConvertsOriginal1500MillisecondTimesAndNeverRoundsDown(){
  assertEquals(7,Native950Thieving.stallRespawnTicks(Stalls.CAKE));
  assertEquals(18,Native950Thieving.stallRespawnTicks(Stalls.TEA_STALL));
  assertEquals(200,Native950Thieving.stallRespawnTicks(Stalls.SPICE_STALL));
 }
 @Test public void butterflyFormulaRetainsOriginalNetBonusAndNinetyFivePercentCeiling(){
  assertEquals(45,Native950Hunter.catchChance(15,15,false));
  assertEquals(50,Native950Hunter.catchChance(15,15,true));
  assertEquals(65,Native950Hunter.catchChance(25,15,false));
  assertEquals(95,Native950Hunter.catchChance(120,15,false));
 }
}
