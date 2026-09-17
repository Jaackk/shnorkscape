package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.*;import java.util.*;import static org.junit.Assert.*;
public class Native950InventionTest {
 private Player player;private EmbeddedChannel channel;
 @Before public void setup(){channel=new EmbeddedChannel();player=Player.createNative950("invention-unit",new WorldTile(3217,3258,0),channel);player.setActive(true);}
 @After public void close(){channel.finishAndReleaseAll();}
 @Test public void cappedRollsAreDiscardedWithoutRerollingOrReducingOldSavedStock(){
  int[] stock=new int[128],add=new int[128];stock[1]=99999;add[1]=3;add[2]=1;stock[82]=1999999999;add[82]=2;
  assertEquals(3,Native950Invention.addCapped(stock,add));assertEquals(100000,stock[1]);assertEquals(1,stock[2]);assertEquals(2000000000,stock[82]);
  stock[1]=150000;assertEquals(5,Native950Invention.addCapped(stock,add));assertEquals(150000,stock[1]);
 }
 @Test public void pouchOnlyAcceptsItsOwnNonItemIcon(){
  assertTrue(Native950InventionUi.target((1473<<16)|9,1,-1));
  assertFalse(Native950InventionUi.target((1473<<16)|5,0,-1));
  assertFalse(Native950InventionUi.target((1473<<16)|9,0,-1));
  assertFalse(Native950InventionUi.target((1473<<16)|9,0,1511));
 }
 @Test public void unlockRequirementsAreBaseLevelsNotTemporaryBoosts(){
  for(int skill:new int[]{com.rs.game.player.Skills.CRAFTING,com.rs.game.player.Skills.SMITHING,com.rs.game.player.Skills.DIVINATION})player.getSkills().setLevelWithoutRefresh(skill,99);
  assertNotNull(Native950Invention.requirement(player));
  for(int skill:new int[]{com.rs.game.player.Skills.CRAFTING,com.rs.game.player.Skills.SMITHING,com.rs.game.player.Skills.DIVINATION})player.getSkills().setXpWithoutRefresh(skill,com.rs.game.player.Skills.getXPForLevel(skill,80));
  assertNull(Native950Invention.requirement(player));
 }
 @Test public void newCharactersHaveNoFreeComponents(){assertArrayEquals(new int[128],Native950Invention.materials(player));}
 @Test public void saveSnapshotsCannotMutatePlayerAndRestoreClonesInput(){int[] a=new int[128];a[3]=12;Native950Invention.restoreMaterials(player,a);a[3]=19;int[] b=Native950Invention.materials(player);assertEquals(12,b[3]);b[3]=99;assertEquals(12,Native950Invention.materials(player)[3]);}
 @Test(expected=IllegalArgumentException.class) public void rejectsNegativeSavedComponents(){int[] a=new int[128];a[1]=-1;Native950Invention.restoreMaterials(player,a);}
 @Test(expected=IllegalArgumentException.class) public void rejectsWrongSavedShape(){Native950Invention.restoreMaterials(player,new int[127]);}
 @Test public void overflowRefusesWholeMaterialReward(){int[] stock=new int[128],add=new int[128];stock[0]=Integer.MAX_VALUE;add[0]=1;add[1]=1;assertFalse(Native950Invention.canAdd(stock,add));assertEquals(0,stock[1]);}
 @Test public void junkUsesNew950SlotNotHistoricalComponents(){ItemDisassembleData d=new ItemDisassembleData(8,1,0.3,100,new Component[]{new Component("Base parts",1,1,.5)});int[] r=Native950Invention.roll(d,new Random(1));assertEquals(8,r[82]);assertEquals(0,r[75]);}
 @Test public void guaranteedAndWeightedComponentsRetain910Quantities(){ItemDisassembleData d=new ItemDisassembleData(8,1,0.3,0,new Component[]{new Component("Base parts",1,3,1),new Component("Blade parts",2,1,.5)});int[] r=Native950Invention.roll(d,new Random(1));assertEquals(3,r[1]);assertEquals(8,r[2]);assertEquals(0,r[82]);}
 @Test public void researchAffectsEveryOrdinaryDisassemblyButNotGuaranteedMaterials(){
  ItemDisassembleData d=new ItemDisassembleData(1,1,1,100,new Component[]{new Component("Special",3,2,1),new Component("Base parts",1,1,.5)});
  Random high=new Random(){public double nextDouble(){return .90;}};
  int[] before=Native950Invention.roll(d,high,0),after=Native950Invention.roll(d,high,9);
  assertEquals(1,before[82]);assertEquals(0,after[82]);assertEquals(1,after[1]);assertEquals(2,before[3]);assertEquals(2,after[3]);
  assertEquals(40.4,Native950InventionResearch.effectiveJunk(50.5,9),.00001);
 }
 @Test public void researchHasNoFreeUnlocksAndRejectsInvalidRestores(){
  assertEquals(0,Native950InventionResearch.tier(player));Native950InventionResearch.restore(player,3);assertEquals(.95,Native950InventionResearch.multiplier(Native950InventionResearch.tier(player)),0);
  try{Native950InventionResearch.restore(player,10);fail("Invalid research accepted");}catch(IllegalArgumentException expected){}
  assertEquals(3,Native950InventionResearch.tier(player));
 }
 @Test public void malformedTableRowsFailClosed(){assertFalse(Native950Invention.validData(new ItemDisassembleData(1,0,1,0,new Component[]{new Component("X",0,1,.5)})));assertFalse(Native950Invention.validData(new ItemDisassembleData(1,1,Double.NaN,0,new Component[]{new Component("X",0,1,.5)})));assertFalse(Native950Invention.validData(new ItemDisassembleData(1,1,1,0,new Component[]{new Component("X",128,1,.5)})));}
}
