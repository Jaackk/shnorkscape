package com.rs.game.player.client;
import com.rs.game.WorldTile;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class Native950ConjureFormationTest {
 @Test public void fourCompanionsHaveSeparateAdjacentFootprints(){
  WorldTile owner=new WorldTile(3200,3200,0);List<WorldTile> tiles=new ArrayList<>();List<Integer> sizes=new ArrayList<>();
  for(int i=0;i<4;i++){WorldTile t=Native950ConjureFormation.place(owner,1,1,tiles,sizes,(p,s)->true);
   assertNotNull(t);assertEquals(1,Math.abs(t.getX()-3200)+Math.abs(t.getY()-3200));tiles.add(t);sizes.add(1);}
  assertEquals(4,new HashSet<>(tiles).size());
 }
 @Test public void blockedNeighboursUseNearestClearFootprintAndNeverOwnerFallback(){
  WorldTile owner=new WorldTile(3200,3200,0);
  WorldTile t=Native950ConjureFormation.place(owner,2,2,Collections.emptyList(),Collections.emptyList(),(p,s)->p.getX()==3202&&p.getY()==3200);
  assertNotNull(t);assertEquals(3202,t.getX());assertFalse(Native950ConjureFormation.overlap(owner,2,t,2));
  assertNull(Native950ConjureFormation.place(owner,1,1,Collections.emptyList(),Collections.emptyList(),(p,s)->false));
 }
}
