package com.rs.game.player.client;
import com.rs.game.WorldTile;import com.rs.game.player.Player;import java.util.*;import io.netty.channel.embedded.EmbeddedChannel;import org.junit.Test;import static org.junit.Assert.*;
public class Native950CombatPreferencesTest {
 @Test public void allNativeBindingRowsAndPreferencesRoundTripWithinExistingSaveCap(){
  EmbeddedChannel c=new EmbeddedChannel();try{
   Player p=Player.createNative950("pref",new WorldTile(3200,3200,0),c);Native950ActionBar b=p.getNative950ActionBar();
   b.preferences.bindingEnabled=true;b.preferences.numeric=1;b.preferences.shiftDrop=false;
   for(int i=0;i<18;i++){b.preferences.styles[i]=i%13;b.preferences.bars[i]=i%5;}
   Map<String,Integer> save=p.nativeSettingsSnapshot();assertTrue(save.size()<=48);
   Native950ActionBar restored=new Native950ActionBar();restored.restore(save);
   assertArrayEquals(b.preferences.styles,restored.preferences.styles);assertArrayEquals(b.preferences.bars,restored.preferences.bars);
   assertTrue(restored.preferences.bindingEnabled);assertFalse(restored.preferences.shiftDrop);assertEquals(1,restored.preferences.numeric);
   assertEquals(2,new Native950ActionBar().preferences.numeric);
  }finally{c.finishAndReleaseAll();}
 }
 @Test public void selectionAuthorityAndEquipmentMatchesRemainPlayerLocal(){
  EmbeddedChannel c=new EmbeddedChannel();try{
   Native950CombatPreferences a=new Native950CombatPreferences(),other=new Native950CombatPreferences();
   assertFalse(a.value(null,c,1));assertTrue(a.select(null,c,10496));assertTrue(a.value(null,c,1));assertEquals(1,a.numeric);assertEquals(2,other.numeric);
   a.select(null,c,10496);a.select(null,c,100);assertFalse(a.value(null,c,0));assertEquals(1,a.numeric);
   a.select(null,c,10754);assertTrue(a.bindingEnabled);
   a.select(null,c,10756);assertTrue(a.value(null,c,10));a.select(null,c,10758);assertTrue(a.value(null,c,4));
   assertEquals(3,a.boundBar(3,false,true));assertEquals(-1,a.boundBar(0,false,true));assertEquals(-1,other.boundBar(3,false,true));
   a.select(null,c,10758);a.value(null,c,18);assertEquals(4,a.bars[0]);
   a.close();assertFalse(a.value(null,c,0));
  }finally{c.finishAndReleaseAll();}
 }
}
