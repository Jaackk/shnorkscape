package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.*;import static org.junit.Assert.*;
public class Native950ArchaeologyTest {
 private Player p;private EmbeddedChannel c;@Before public void setup(){c=new EmbeddedChannel();p=Player.createNative950("archaeology-unit",new WorldTile(3217,3258,0),c);p.setActive(true);}@After public void close(){c.finishAndReleaseAll();}
 @Test public void storesStartEmptyAndSnapshotsAreIndependent(){int[] a=Native950Archaeology.materials(p);assertEquals(64,a.length);a[0]=999;assertEquals(0,Native950Archaeology.materials(p)[0]);a[0]=12;Native950Archaeology.restoreMaterials(p,a);a[0]=99;assertEquals(12,Native950Archaeology.materials(p)[0]);}
 @Test(expected=IllegalArgumentException.class) public void negativeMaterialsCannotBeRestored(){int[] a=new int[64];a[1]=-1;Native950Archaeology.restoreMaterials(p,a);}
 @Test(expected=IllegalArgumentException.class) public void rejectsWrongSaveShape(){Native950Archaeology.restoreMaterials(p,new int[63]);}
 @Test public void materialSlotsAreAppendOnlyAndOnlyDamagedArtefactRemainsInBackpack(){assertEquals(0,Native950Archaeology.materialSlot(49444));assertEquals(3,Native950Archaeology.materialSlot(49514));assertEquals(-1,Native950Archaeology.materialSlot(49921));Native950Production.Recipe r=new Native950Production.Recipe("Restore",Skills.ARCHAEOLOGY,5,305.1,-1,3,new Item[]{new Item(49921,1),new Item(49460,16),new Item(49514,12)},new Item[]{new Item(49922,1)});Item[] in=Native950Archaeology.backpackInputs(r);assertEquals(1,in.length);assertEquals(49921,in[0].getId());}

 @Test public void precisionScalesProgressWithoutChangingSavedUnits(){
  assertEquals(1,Native950Archaeology.advanceProgress(Native950Archaeology.Site.CENTURION,0,49539));
  assertEquals(2,Native950Archaeology.advanceProgress(Native950Archaeology.Site.CENTURION,0,49542));
  assertEquals(6,Native950Archaeology.advanceProgress(Native950Archaeology.Site.CENTURION,0,49554));
  assertEquals(10,Native950Archaeology.advanceProgress(Native950Archaeology.Site.CENTURION,0,49584));
  assertEquals(32,Native950Archaeology.advanceProgress(Native950Archaeology.Site.VENATOR,31,49584));
  assertEquals(50,Native950Archaeology.precision(49566));assertEquals(40,Native950Archaeology.precision(49557));
 }
 @Test(expected=IllegalArgumentException.class) public void unrecognizedToolCannotAdvanceArtefact(){Native950Archaeology.advanceProgress(Native950Archaeology.Site.CENTURION,0,995);}
 @Test public void excavationGaugeUsesOnlyVerifiedBlueProgressChannel(){
  com.rs.game.hitbar.impl.MiningHitBar stamina=com.rs.game.hitbar.impl.MiningHitBar.stamina(100);p.getNextHitBars().add(stamina);
  Native950Archaeology.progressBar(p,Native950Archaeology.Site.CENTURION,5,false);
  assertEquals(2,p.getNextHitBars().size());assertTrue(p.getNextHitBars().contains(stamina));
  com.rs.game.hitbar.HitBar bar=p.getNextHitBars().stream().filter(b->b.getType()==49).findFirst().get();assertEquals(127,bar.getPercentage());
  Native950Archaeology.progressBar(p,Native950Archaeology.Site.CENTURION,0,true);
  assertEquals(2,p.getNextHitBars().size());assertTrue(p.getNextHitBars().contains(stamina));
  assertTrue(((com.rs.game.hitbar.impl.MiningHitBar)p.getNextHitBars().stream().filter(b->b.getType()==49).findFirst().get()).isRemoval());
 }
}
