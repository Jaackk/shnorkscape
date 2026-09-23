package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;import org.junit.Test;import java.util.*;import static org.junit.Assert.*;
public class Native950CombatEffectUiTest {
 @Test public void livingDeathPublishesIdentityNativeTimerAndRemovalWithoutCrossingPlayers(){
  EmbeddedChannel a=new EmbeddedChannel(),b=new EmbeddedChannel();
  Player p=Player.createNative950("buff-ui-a",new WorldTile(3200,3200,0),a),q=Player.createNative950("buff-ui-b",new WorldTile(3200,3201,0),b);
  p.setActive(true);q.setActive(true);Native950CombatBuffs effects=new Native950CombatBuffs();
  try{
   effects.apply(p,Native950CombatBuffs.Type.LIVING_DEATH,10);
   assertEquals(48324,p.getVarsManager().getValue(11059));assertEquals(0,q.getVarsManager().getValue(11059));
   assertPacket(a,Native950Packets.runClientScript(4252,48339,50));assertNull(b.readOutbound());
   effects.expire(60,(owner,type)->{});assertEquals(0,p.getVarsManager().getValue(11059));
   assertPacket(a,Native950Packets.runClientScript(10624,48339,0));
  }finally{a.finishAndReleaseAll();b.finishAndReleaseAll();}
 }
 @Test public void devotionExtensionUsesOriginalActivationClockAndNativeEffectStructs(){
  EmbeddedChannel c=new EmbeddedChannel();Player p=Player.createNative950("buff-devotion",new WorldTile(3200,3200,0),c);p.setActive(true);
  try{Native950CombatBuffs effects=new Native950CombatBuffs();effects.apply(p,Native950CombatBuffs.Type.DEVOTION,100);
   assertPacket(c,Native950Packets.runClientScript(9379,25028,0,16));effects.onKill(p,108);
   assertPacket(c,Native950Packets.runClientScript(9379,25028,0,24));
   assertEquals(52801,Native950CombatEffectUi.buff(Native950CombatBuffs.Type.SEARING_WINDS));
   assertEquals(52802,Native950CombatEffectUi.buff(Native950CombatBuffs.Type.SHADOW_IMBUED));
   assertEquals(3636,Native950CombatEffectUi.buff(Native950CombatBuffs.Type.REVENGE));
  }finally{c.finishAndReleaseAll();}
 }
 static void assertPacket(EmbeddedChannel c,Native950Packets.Packet expected){c.flushOutbound();Object v;boolean found=false;
  while((v=c.readOutbound())!=null)if(v instanceof Native950Packets.Packet){Native950Packets.Packet p=(Native950Packets.Packet)v;
   found|=p.type()==expected.type()&&Arrays.equals(p.payload(),expected.payload());}
  assertTrue("Native effect packet missing",found);
 }
}
