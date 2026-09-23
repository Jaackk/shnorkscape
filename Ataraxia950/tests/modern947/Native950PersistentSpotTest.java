package modern947;
import com.rs.game.player.client.Native950Viewport;
import com.rs.network.protocol.modern950.*;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Actor;
import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class Native950PersistentSpotTest {
 static Actor actor(int index,int count){return Actor.builder(index,3200+index,3200,0).identity(index)
  .persistentSpots(count+1,Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(
   Native950PlayerMasks.Spotanim.of(4,count==0?-1:7865+count,0,0,0,0,0)))).build();}
 @Test public void reentryIndexReuseAndSameFrameCastRetainIndependentSoulSlot(){
  Native950Viewport viewer=new Native950Viewport(2);Actor local=Actor.at(2,3202,3200,0);Actor[] w=world(actor(1,1),local);
  viewer.initialScene(w,7,0,0,0);assertTrue(contains(viewer.frame(w).payload(),mask(1)));
  viewer.frame(world(local));viewer.frame(world(local));
  assertTrue(contains(viewer.frame(w).payload(),mask(1)));
  Native950PlayerMasks.Spotanim cast=Native950PlayerMasks.Spotanim.of(0,7899,0,0,0,0,0);
  Native950PlayerMasks.Spotanim soul=Native950PlayerMasks.Spotanim.of(4,7867,0,0,0,0,0);
  Actor replacement=Actor.builder(1,3201,3200,0).identity(91)
   .persistentSpots(3,Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(soul)))
   .masks(()->Native950PlayerMasks.builder().spotanims(Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(cast)))).build();
  byte[] combined=Native950PlayerMasks.encodeWithSkippedPrefix(Native950PlayerMasks.builder().spotanims(
   Native950PlayerMasks.SpotanimList.of(new int[0],Arrays.asList(cast,soul))).build());
  // Reused indices first remove the old actor, then add the new identity on the next frame.
  assertFalse(contains(viewer.frame(world(replacement,local)).payload(),combined));
  assertTrue(contains(viewer.frame(world(replacement,local)).payload(),combined));
 }
 static Actor[] world(Actor... actors){Actor[] w=new Actor[2048];for(Actor a:actors)w[a.index]=a;return w;}
 static boolean contains(byte[] bytes,byte[] needle){outer:for(int i=0;i<=bytes.length-needle.length;i++){for(int j=0;j<needle.length;j++)if(bytes[i+j]!=needle[j])continue outer;return true;}return false;}
 static byte[] mask(int count){return Native950PlayerMasks.encodeWithSkippedPrefix(Native950PlayerMasks.builder().spotanims(
  Native950PlayerMasks.SpotanimList.of(new int[0],Collections.singletonList(Native950PlayerMasks.Spotanim.of(4,count==0?-1:7865+count,0,0,0,0,0)))).build());}
 @Test public void persistentSoulsPublishOnceToBothViewersAndLateJoinThenClear(){
  Native950Viewport a=new Native950Viewport(1),b=new Native950Viewport(2);
  Actor owner=actor(1,3),other=Actor.at(2,3202,3200,0);Actor[] w=world(owner,other);
  a.initialScene(w,7,0,0,0);b.initialScene(w,7,0,0,0);
  assertTrue(contains(a.frame(w).payload(),mask(3)));assertTrue(contains(b.frame(w).payload(),mask(3)));
  for(int i=0;i<3;i++){assertFalse(contains(a.frame(w).payload(),mask(3)));assertFalse(contains(b.frame(w).payload(),mask(3)));}
  Native950Viewport late=new Native950Viewport(2);late.initialScene(w,7,0,0,0);assertTrue(contains(late.frame(w).payload(),mask(3)));
  w=world(actor(1,5),other);assertTrue(contains(a.frame(w).payload(),mask(5)));assertTrue(contains(b.frame(w).payload(),mask(5)));
  w=world(actor(1,0),other);assertTrue(contains(a.frame(w).payload(),mask(0)));assertTrue(contains(b.frame(w).payload(),mask(0)));
  assertFalse(contains(a.frame(w).payload(),mask(0)));
 }
}

