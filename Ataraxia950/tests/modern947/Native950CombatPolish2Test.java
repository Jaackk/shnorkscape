package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class Native950CombatPolish2Test {
 @Test public void worldGraphicMatchesNativeTileParser(){
  for(int[] v:new int[][]{{3200,3201,2,3856,30},{0,16383,3,-1,0},{16383,0,0,65534,32767}}){
   byte[] frame=Native950Packets.worldSpotanim(v[0],v[1],v[2],v[3],v[4]).frame(()->0);
   assertEquals(17,frame.length);assertEquals(128,frame[0]&255);assertEquals(197,frame[1]&255);
   int[] b=new int[15];for(int i=0;i<15;i++)b[i]=frame[i+2]&255;
   int target=b[2]<<24|b[3]<<16|b[0]<<8|b[1];
   assertTrue((target&0x40000000)!=0);assertEquals(v[0],target>>>14&16383);assertEquals(v[1],target&16383);assertEquals(v[2],target>>>28&3);
   int offsets=b[6]<<16|b[5]<<8|b[7];assertEquals(1023,offsets&2047);assertEquals(1023,offsets>>>11&2047);
   assertEquals(0,(b[8]+128)&255);assertEquals(0,(b[9]<<8)|((b[10]-128)&255));
   assertEquals(v[3]&65535,(b[12]<<8)|((b[11]-128)&255));assertEquals(v[4],(b[14]<<8)|((b[13]-128)&255));
  }
 }
 @Test public void sharedTileAreaExpiryDoesNotEraseAnotherPlayersArea(){
  EmbeddedChannel a=new EmbeddedChannel(),b=new EmbeddedChannel();
  Player first=Player.createNative950("area-first",new WorldTile(3200,3200,0),a),second=Player.createNative950("area-second",new WorldTile(3200,3200,0),b);
  List<Integer> writes=new ArrayList<>();List<WorldTile> positions=new ArrayList<>();
  Native950CombatAreas areas=new Native950CombatAreas((tile,id)->{positions.add(new WorldTile(tile));writes.add(id);});
  try{
   areas.start(first,3856);areas.start(second,3856);areas.remove(first);assertEquals(Arrays.asList(3856,3856),writes);
   first.setLocation(3210,3210,0);areas.remove(second);assertEquals(Arrays.asList(3856,3856,-1),writes);assertTrue(positions.get(2).matches(new WorldTile(3200,3200,0)));
   areas.remove(second);assertEquals(3,writes.size());
  }finally{a.finishAndReleaseAll();b.finishAndReleaseAll();}
 }
}
