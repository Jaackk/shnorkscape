package com.rs.game.player.client;
import com.rs.game.Hit.HitLook;
import org.junit.Test;
import static org.junit.Assert.*;
public class Native950BossRulesTest {
 @Test public void kingsRequireTheirCounterStyleIncludingNecromancy(){
  HitLook[] styles={HitLook.MELEE_DAMAGE,HitLook.RANGE_DAMAGE,HitLook.MAGIC_DAMAGE,HitLook.NECROMANCY_DAMAGE};
  for(int id=2881;id<=2883;id++)for(int style=0;style<styles.length;style++)
   assertEquals("id="+id+" style="+style,id-2881==style,Native950BossRules.acceptsDamage(id,styles[style]));
  assertTrue(Native950BossRules.acceptsDamage(2883,HitLook.POISON_DAMAGE));
  assertTrue(Native950BossRules.acceptsDamage(12353,HitLook.NECROMANCY_DAMAGE));
 }
 @Test public void rexCanBeStunnedButPrimeAndSupremeCannot(){
  assertTrue(Native950BossRules.stunImmune(2881));assertTrue(Native950BossRules.stunImmune(2882));
  assertFalse(Native950BossRules.stunImmune(2883));assertFalse(Native950BossRules.stunImmune(12353));
 }
 @Test public void executionBindingFailsClosedWithoutThePairedCache(){
  try{Native950Symbols.require("npc","missing_boss");fail();}catch(IllegalStateException expected){}
  assertEquals(14,Native950GamevalLookup.search("npc shnorkscape").size());
  assertEquals(18,Native950GamevalLookup.search("object shnorkscape").size());
  assertFalse(Native950GamevalLookup.search("npc dagannoth").get(0).matches(new byte[]{0}));
 }
 @Test public void stairsRolloutDoesNotEnableUnknownRegions(){
  assertTrue(Native950WorldTraversal.ordinaryRegion(13623));
  assertFalse(Native950WorldTraversal.ordinaryRegion(11423));
 }
}
