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
            assertEquals(Integer.valueOf(0),restored.nativeSettingsSnapshot().get("actionBar.0.0"));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void badAndOldSettingsProduceEmptySlots(){
        Native950ActionBar bar=new Native950ActionBar();
        bar.restore(Collections.singletonMap("actionBar.0",-1));Map<String,Integer> out=new HashMap<>();bar.writeSettings(out);
        assertEquals(Integer.valueOf(0),out.get("actionBar.0.0"));assertEquals(23,out.size());
    }
    @Test public void threeBarsRoundTripIndependentlyWithinTheNativeSettingsCap(){
        Native950ActionBar bar=new Native950ActionBar();Map<String,Integer> legacy=new HashMap<>();
        legacy.put("actionBar.0",Native950ActionBar.pack(1,3));bar.restore(legacy);
        EmbeddedChannel c=new EmbeddedChannel();try{
            bar.setActiveBar(c,1);bar.testBar(c);bar.setActiveBar(c,2);bar.testBar(c);
            Map<String,Integer> settings=new HashMap<>();bar.writeSettings(settings);
            assertEquals(23,settings.size());assertTrue(settings.size()<=Native950Save.MAX_SETTINGS);
            Native950ActionBar restored=new Native950ActionBar();restored.restore(settings);
            assertEquals(Native950ActionBar.pack(1,3),restored.slot(0,0));
            assertEquals(Native950ActionBar.pack(1,3),restored.slot(1,0));
            assertEquals(Native950ActionBar.pack(6,3),restored.slot(2,2));
            assertEquals(2,restored.activeBar());
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void revolutionStatePersistsAndUsesTheVerifiedClientVarbits(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Native950ActionBar bar=new Native950ActionBar();bar.setRevolutionEnabled(c,true);
            assertTrue(bar.isRevolutionEnabled());Map<String,Integer> settings=new HashMap<>();bar.writeSettings(settings);
            Native950ActionBar restored=new Native950ActionBar();restored.restore(settings);assertTrue(restored.isRevolutionEnabled());
            c.flush();Object packet;boolean fullManual=false,revolution=false;
            Native950Packets.Packet expectedFullManual=Native950Packets.varbitSmall(Native950ActionBar.FULL_MANUAL_MODE_VARBIT,0);
            Native950Packets.Packet expectedRevolution=Native950Packets.varbitSmall(Native950ActionBar.REVOLUTION_MODE_VARBIT,1);
            while((packet=c.readOutbound())!=null)if(packet instanceof Native950Packets.Packet){
                Native950Packets.Packet actual=(Native950Packets.Packet)packet;
                fullManual|=actual.type()==expectedFullManual.type()&&Arrays.equals(actual.payload(),expectedFullManual.payload());
                revolution|=actual.type()==expectedRevolution.type()&&Arrays.equals(actual.payload(),expectedRevolution.payload());
            }
            assertTrue(fullManual);assertTrue(revolution);
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
    @Test public void nativeBarSelectorAndTrashTargetUseTheVerified950Components(){
        EmbeddedChannel c=new EmbeddedChannel();try{
            new Native950ActionBar().bootstrap(c);c.flush();List<Native950Packets.Packet> packets=new ArrayList<>();Object next;
            while((next=c.readOutbound())!=null)if(next instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)next);
            assertTrue(hasPacket(packets,Native950Packets.interfaceEvents(1430,16,-1,-1,2046)));
            assertTrue(hasPacket(packets,Native950Packets.interfaceEvents(1430,254,-1,-1,2046)));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void bootstrapSelectsTheSavedActiveBar(){
        EmbeddedChannel c=new EmbeddedChannel();try{
            Native950ActionBar bar=new Native950ActionBar();bar.setActiveBar(c,2);c.flush();
            while(c.readOutbound()!=null){}
            bar.bootstrap(c);c.flush();List<Native950Packets.Packet> packets=new ArrayList<>();Object next;
            while((next=c.readOutbound())!=null)if(next instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)next);
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(1893,3)));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void everySavedBarMutationSynchronizesTheSameNativeDisplayContext(){
        EmbeddedChannel c=new EmbeddedChannel();try{
            Native950ActionBar bar=new Native950ActionBar();bar.testBar(c);c.flush();
            List<Native950Packets.Packet> packets=new ArrayList<>();Object next;
            while((next=c.readOutbound())!=null)if(next instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)next);
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(1893,1)));
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(1892,0)));
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(Native950ActionBar.FULL_MANUAL_MODE_VARBIT,1)));
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(Native950ActionBar.REVOLUTION_MODE_VARBIT,0)));
            assertTrue(hasPacket(packets,Native950Packets.varp(739,Native950ActionBar.pack(1,3))));
            assertTrue(hasPacket(packets,Native950Packets.runClientScript(6992)));
            assertTrue(hasPacket(packets,Native950Packets.runClientScript(7964,1436,0,0,1,-1)));
            bar.setActiveBar(c,1);c.flush();packets.clear();
            while((next=c.readOutbound())!=null)if(next instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)next);
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(1893,2)));
            assertTrue(hasPacket(packets,Native950Packets.varp(739,0)));
        }finally{c.finishAndReleaseAll();}
    }
    private static boolean hasPacket(List<Native950Packets.Packet> packets,Native950Packets.Packet expected){
        for(Native950Packets.Packet actual:packets)if(actual.type()==expected.type()&&Arrays.equals(actual.payload(),expected.payload()))return true;
        return false;
    }
    @Test public void hubActionsAreBoundedToTheHubAndSupportedOptions(){
        WorldObject altar=new WorldObject(114748,10,0,3304,10125,0);
        assertTrue(Native950WarsRetreat.handles(altar,1));assertFalse(Native950WarsRetreat.handles(altar,2));
        assertFalse(Native950WarsRetreat.handles(new WorldObject(114748,10,0,3217,3258,0),1));
        assertFalse(Native950WarsRetreat.handles(new WorldObject(114761,10,0,3283,10149,0),1));
        assertTrue(Native950WarsRetreat.bank(114750));assertFalse(Native950WarsRetreat.bank(114749));
    }
    @Test public void developerTravelDestinationsUseVerifiedLegacyWorldTiles(){
        assertEquals(3294,Native950WarsRetreat.WARS_RETREAT.getX());assertEquals(10129,Native950WarsRetreat.WARS_RETREAT.getY());
        assertEquals(3419,Native950WarsRetreat.DEATHS_OFFICE.getX());assertEquals(5270,Native950WarsRetreat.DEATHS_OFFICE.getY());
        assertEquals(2972,Native950WarsRetreat.VORAGO_ENTRANCE.getX());assertEquals(3430,Native950WarsRetreat.VORAGO_ENTRANCE.getY());
    }
}
