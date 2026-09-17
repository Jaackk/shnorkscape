package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.*;import java.util.*;import static org.junit.Assert.*;
public class Native950FarmingStateTest {
 private EmbeddedChannel channel;private Player p;
 @Before public void setup(){channel=new EmbeddedChannel();p=Player.createNative950("farm-state",new WorldTile(3217,3258,0),channel);}
 @After public void close(){channel.finishAndReleaseAll();}
 private Native950Farming.Plot crop(long at,int remaining){return new Native950Farming.Plot(8550,3050,3307,0,5318,at,true,0,remaining);}
 @Test public void cropTimeAndStagesSurviveSnapshotRestore(){long now=1700000000000L;Native950Farming.Crop c=Native950Farming.crop(5318);Native950Farming.Plot a=crop(now,7);Native950Farming.restore(p,Arrays.asList(a));assertFalse(a.mature(now+c.growMillis-1));assertTrue(a.mature(now+c.growMillis));assertEquals(c.start,a.value(now-1000));assertEquals(c.start+c.stages,a.value(now+c.growMillis));assertEquals(a,Native950Farming.snapshot(p).get(0));}
 @Test public void cropTypesAndMalformedSnapshotsFailClosed(){assertFalse(Native950Farming.valid(new Native950Farming.Plot(8150,3050,3307,0,5318,1,true,0,7)));assertFalse(Native950Farming.valid(crop(-1,7)));assertFalse(Native950Farming.valid(crop(1,61)));assertFalse(Native950Farming.valid(crop(1,0)));assertFalse(Native950Farming.valid(new Native950Farming.Plot(8550,-1,3307,0,0,0,true,0,0)));assertFalse(Native950Farming.valid(new Native950Farming.Plot(8550,1,1,0,0,1,true,0,0)));}
 @Test public void invalidRestoreDoesNotEraseExistingProgress(){Native950Farming.Plot a=crop(1000,7);Native950Farming.restore(p,Arrays.asList(a));try{Native950Farming.restore(p,Arrays.asList(a,a));fail();}catch(IllegalArgumentException expected){}assertEquals(1,Native950Farming.snapshot(p).size());assertEquals(a,Native950Farming.snapshot(p).get(0));}
 @Test public void distinctSceneryTilesOfOnePatchCannotCreateDuplicateCrops(){Native950Farming.Plot a=crop(1000,7),b=new Native950Farming.Plot(8550,3051,3307,0,5318,1000,true,0,7);try{Native950Farming.restore(p,Arrays.asList(a,b));fail();}catch(IllegalArgumentException expected){}assertTrue(Native950Farming.snapshot(p).isEmpty());}
 @Test public void snapshotsCannotBeMutatedByCaller(){Native950Farming.restore(p,Arrays.asList(crop(1000,7)));try{Native950Farming.snapshot(p).clear();fail();}catch(UnsupportedOperationException expected){}assertEquals(1,Native950Farming.snapshot(p).size());}
 @Test public void allAdmittedCropsHaveBoundedOriginalRatesAndGrowth(){assertEquals(36,Native950Farming.crops().size());for(Native950Farming.Crop c:Native950Farming.crops()){assertTrue(c.level>=1&&c.level<=120);assertTrue(c.growMillis>=60000L&&c.growMillis<86400000L);assertTrue(c.plantXp>0&&c.harvestXp>0);assertTrue(c.stages>=1);assertTrue(c.yield(3)>=c.yield(0));assertTrue(c.yield(3)<=18);}}

 @Test public void compostAddsHarvestLivesAndLevelAffectsConservation(){
  Native950Farming.Crop potato=Native950Farming.crop(5318),herb=Native950Farming.crop(5291);
  assertEquals(3,Native950Farming.harvestAmount(potato,1,0,b->99));
  assertEquals(6,Native950Farming.harvestAmount(potato,1,3,b->99));
  assertEquals(3,Native950Farming.harvestAmount(potato,1,0,b->30));
  assertEquals(60,Native950Farming.harvestAmount(potato,99,0,b->30));
  assertEquals(60,Native950Farming.harvestAmount(herb,9,3,b->0));
  final int[] roll={0};assertEquals(12,Native950Farming.harvestAmount(potato,99,3,b->roll[0]++%2==0?0:99));
 }
 @Test public void flowerYieldDoesNotScaleWithCompostAndLimpwurtXpIsPerPatch(){
  for(int rank=0;rank<4;rank++){
   assertEquals(1,Native950Farming.harvestAmount(Native950Farming.crop(5096),99,rank,b->0));
   assertEquals(3,Native950Farming.harvestAmount(Native950Farming.crop(5100),99,rank,b->0));
  }
  assertEquals(120,3*Native950Farming.crop(5100).harvestExperience(),0);
  assertEquals(47,Native950Farming.crop(5096).harvestExperience(),0);
  assertEquals(18,Native950Farming.compostExperience(6032),0);assertEquals(26,Native950Farming.compostExperience(6034),0);assertEquals(36,Native950Farming.compostExperience(43966),0);assertEquals(0,Native950Farming.compostExperience(1925),0);
 }
 @Test public void maximumHarvestSurvivesSnapshotAndRejectsUnboundedCounts(){
  Native950Farming.Plot maximum=crop(1000,60);assertTrue(Native950Farming.valid(maximum));
  Native950Farming.restore(p,Arrays.asList(maximum));assertEquals(60,Native950Farming.snapshot(p).get(0).harvestRemaining);
  assertFalse(Native950Farming.valid(crop(1000,61)));
  assertFalse(Native950Farming.valid(new Native950Farming.Plot(7847,3054,3307,0,5096,1000,true,3,4)));
 }
}
