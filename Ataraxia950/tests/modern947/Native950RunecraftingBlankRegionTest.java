package com.rs.game;
import org.junit.Test;
import static org.junit.Assert.*;
/** Regression for metadata-only cache squares bordering the Runecrafting altar scenes. */
public final class Native950RunecraftingBlankRegionTest {
 @Test public void absentWorldFilesFinishAsBlockedOnEveryPlaneWithoutReadingPayloads(){
  Region region=new Region(11084);
  region.loadNative950RegionMap(false,false,()->{throw new AssertionError("Absent terrain read");},()->{throw new AssertionError("Absent locations read");});
  region.setLoadMapStage(2);
  for(int p=0;p<4;p++)for(int x=0;x<64;x++)for(int y=0;y<64;y++){
   assertEquals(-1,region.getMask(p,x,y));assertEquals(-1,region.getMaskClipedOnly(p,x,y));
  }
 }
 @Test public void declaredButMissingPayloadCannotBeTreatedAsAnEmptyScene(){
  Region region=new Region(11084);
  try{region.loadNative950RegionMap(true,true,()->null,()->new byte[]{0});fail("Declared terrain silently accepted");}catch(IllegalStateException expected){assertTrue(expected.getMessage().contains("Declared"));}
  assertEquals(-1,region.getMask(0,0,0));
 }
 @Test public void locationsWithoutTerrainAreAnErrorRatherThanVoid(){
  Region region=new Region(11084);
  try{region.loadNative950RegionMap(false,true,()->new byte[]{0},()->new byte[]{0});fail("Locations without terrain silently accepted");}catch(IllegalStateException expected){assertTrue(expected.getMessage().contains("without terrain"));}
 }
 @Test public void terrainWithoutLocationsIsValidAndStillDecoded(){
  final byte[] terrain={7};final boolean[] decoded={false};
  Region region=new Region(11084){@Override public byte[][][] loadMapSettings(byte[] data){assertSame(terrain,data);decoded[0]=true;return new byte[4][64][64];}};
  region.forceGetRegionMap();region.forceGetRegionMapClipedOnly();
  region.loadNative950RegionMap(true,false,()->terrain,()->{throw new AssertionError("Undeclared locations read");});
  region.setLoadMapStage(2);assertTrue(decoded[0]);assertEquals(0,region.getMask(0,0,0));
 }
 @Test public void corruptArchiveReadIsNotSwallowed(){
  Region region=new Region(11084);IllegalArgumentException corrupt=new IllegalArgumentException("CRC mismatch");
  try{region.loadNative950RegionMap(true,true,()->{throw corrupt;},()->new byte[]{0});fail("Corrupt archive silently accepted");}catch(IllegalArgumentException expected){assertSame(corrupt,expected);}
  assertEquals(-1,region.getMask(0,0,0));
 }
}
