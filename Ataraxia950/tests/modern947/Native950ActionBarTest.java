package com.rs.game.player.client;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950ActionBarTest {
    @Test public void packingUsesTheCurrentSevenBitTypeAboveTheThirteenBitAbilityId(){
        assertEquals((1<<17)|(3<<4),Native950ActionBar.pack(1,3));
        assertTrue(Native950ActionBar.valid(Native950ActionBar.pack(6,263)));
        assertFalse(Native950ActionBar.valid((3<<4)|1));
        assertFalse(Native950ActionBar.valid(-1));
        assertFalse(Native950ActionBar.valid(1<<17));
    }
    @Test public void widgetMappingIsExactAndBounded(){
        for(int i=0;i<14;i++){
            assertEquals(i,Native950ActionBar.barSlot(1430,65+i*13));
            assertEquals(i,Native950ActionBar.barSlot(1430,66+i*13));
            assertEquals(-1,Native950ActionBar.barSlot(1430,67+i*13));
        }
        assertEquals(-1,Native950ActionBar.barSlot(1430,247));
        assertEquals(-1,Native950ActionBar.barSlot(1670,65));
        assertEquals(1,Native950ActionBar.bookType(1460,1));
        assertEquals(5,Native950ActionBar.bookType(1452,1));
        assertEquals(6,Native950ActionBar.bookType(1461,1));
        assertEquals(-1,Native950ActionBar.bookType(1461,2));
    }
    @Test public void bindingsRoundTripThroughExistingPlayerSettingsWithoutChangingSkills(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("bar-test",new WorldTile(3217,3258,0),c);
            p.getNative950ActionBar().testBar(c);
            Map<String,Integer> snapshot=p.nativeSettingsSnapshot();
            assertTrue(snapshot.size()<=Native950Save.MAX_SETTINGS);
            Player restored=Player.createNative950("bar-test",new WorldTile(3217,3258,0),c);
            restored.applyNativeSettings(snapshot);
            assertEquals(snapshot,restored.nativeSettingsSnapshot());
            assertEquals(1,restored.getSkills().getLevel(0));
            restored.getNative950ActionBar().clear(c);
            assertEquals(Integer.valueOf(0),restored.nativeSettingsSnapshot().get("actionBar.0"));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void badAndOldSettingsProduceEmptySlots(){
        Native950ActionBar bar=new Native950ActionBar();
        bar.restore(Collections.singletonMap("actionBar.0",-1));Map<String,Integer> out=new HashMap<>();bar.writeSettings(out);
        assertEquals(Integer.valueOf(0),out.get("actionBar.0"));assertEquals(15,out.size());
    }
    @Test public void revolutionStatePersistsAndUsesTheVerifiedClientVarbit(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Native950ActionBar bar=new Native950ActionBar();bar.setRevolutionEnabled(c,true);
            assertTrue(bar.isRevolutionEnabled());Map<String,Integer> settings=new HashMap<>();bar.writeSettings(settings);
            Native950ActionBar restored=new Native950ActionBar();restored.restore(settings);assertTrue(restored.isRevolutionEnabled());
            c.flush();Object packet;boolean config=false;
            Native950Packets.Packet expected=Native950Packets.varbitSmall(21682,1);
            while((packet=c.readOutbound())!=null)if(packet instanceof Native950Packets.Packet){
                Native950Packets.Packet actual=(Native950Packets.Packet)packet;
                config|=actual.type()==expected.type()&&Arrays.equals(actual.payload(),expected.payload());
            }
            assertTrue(config);
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void cooldownUsesTheNativeActionBarScript(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            new Native950ActionBar().cooldown(c,14682,100,25);c.flush();Object packet=c.readOutbound();
            assertTrue(packet instanceof Native950Packets.Packet);Native950Packets.Packet actual=(Native950Packets.Packet)packet;
            Native950Packets.Packet expected=Native950Packets.runClientScript(6570,14682,100,125,1,1);
            assertEquals(expected.type(),actual.type());assertArrayEquals(expected.payload(),actual.payload());
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void hubActionsAreBoundedToTheHubAndSupportedOptions(){
        WorldObject altar=new WorldObject(114748,10,0,3304,10125,0);
        assertTrue(Native950WarsRetreat.handles(altar,1));assertFalse(Native950WarsRetreat.handles(altar,2));
        assertFalse(Native950WarsRetreat.handles(new WorldObject(114748,10,0,3217,3258,0),1));
        assertFalse(Native950WarsRetreat.handles(new WorldObject(114761,10,0,3283,10149,0),1));
        assertTrue(Native950WarsRetreat.bank(114750));assertFalse(Native950WarsRetreat.bank(114749));
    }
}
