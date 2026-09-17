package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950AbilityFoundationTest {
    @Test public void sevenDefinitionsSeparateUtilityFromDamage(){
        assertEquals(7,Native950AbilityCatalog.DEFINITIONS.size());
        long melee=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==1).count();
        long magic=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==6&&d.targetRequired()).count();
        assertEquals(3,melee);assertEquals(3,magic);
        Native950AbilityCatalog.Definition surge=Native950AbilityCatalog.get(14726);
        assertFalse(surge.targetRequired());assertEquals(34,surge.cooldown);assertEquals(16,surge.skill);assertEquals(5,surge.level);
    }
    @Test public void dragMaskEscapesBookClippingWithoutEnablingOtherOperations(){
        assertTrue((Native950ActionBar.ABILITY_EVENTS&(1<<23))!=0);
        assertEquals(2,Native950ActionBar.ABILITY_EVENTS&0x7fe);
        assertEquals(2,(Native950ActionBar.ABILITY_EVENTS>>>11)&127);
        for(int i=0;i<14;i++)assertEquals(i,Native950ActionBar.barSlot(1436,20+13*i));
        assertEquals(1,Native950ActionBar.bookType(1450,3));assertEquals(6,Native950ActionBar.bookType(1459,1));
    }
    @Test public void surgeTracesEveryStepAndStopsBeforeTheFirstWall(){
        WorldTile from=new WorldTile(100,100,2);
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0){
            WorldTile to=Native950Surge.destination(from,dx,dy,(t,x,y)->true);
            assertEquals(100+10*dx,to.getX());assertEquals(100+10*dy,to.getY());assertEquals(2,to.getPlane());
        }
        assertEquals(103,Native950Surge.destination(from,1,0,(t,x,y)->t.getX()<103).getX());
        assertTrue(from.matches(Native950Surge.destination(from,1,1,(t,x,y)->false)));
        assertTrue(from.matches(Native950Surge.destination(from,0,0,(t,x,y)->true)));
        assertEquals(100,from.getX());
    }
    @Test public void revolutionUsesOrderAvailabilityAndSlotLimitNeverUtility(){
        int[] bar={14726,-1,14682,14727,14701};
        assertEquals(14682,Native950Revolution.select(bar,14,id->true));
        assertEquals(14727,Native950Revolution.select(bar,14,id->id!=14682));
        assertEquals(-1,Native950Revolution.select(bar,3,id->id!=14682));
        assertEquals(-1,Native950Revolution.select(bar,14,id->false));
        assertEquals(-1,Native950Revolution.select(bar,0,id->true));
    }
    @Test public void exitIsModalAndConfirmationSurvivesUntilExplicitCancel(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("exit-test",new WorldTile(3217,3258,0),c);p.setActive(true);
            Native950ExitUi ui=new Native950ExitUi(p,c,()->{},lobby->fail("Unconfirmed logout"));
            assertTrue(ui.handle(click(1477,99,1)));assertTrue(ui.isOpen());
            c.flush();Object packet;boolean modal=false;
            Native950Packets.Packet expected=Native950Packets.openSub(1477,806,1433,false);
            while((packet=c.readOutbound())!=null)if(packet instanceof Native950Packets.Packet){
                Native950Packets.Packet actual=(Native950Packets.Packet)packet;
                modal|=actual.type()==expected.type()&&Arrays.equals(actual.payload(),expected.payload());
            }
            assertTrue(modal);ui.handle(click(1433,72,-1));assertTrue(ui.isOpen());
            ui.handle(click(1433,89,-1));assertTrue(ui.isOpen());
            ui.handle(click(1433,79,-1));assertFalse(ui.isOpen());ui.close();
        }finally{c.finishAndReleaseAll();}
    }
    private static Native950Actions.InterfaceAction click(int face,int component,int slot){
        int hash=face<<16|component;
        return (Native950Actions.InterfaceAction)Native950Actions.decode(18,new byte[]{-1,-1,-1,(byte)(hash>>16),(byte)(hash>>24),(byte)hash,(byte)(hash>>8),(byte)(slot>>8),(byte)slot});
    }
}
