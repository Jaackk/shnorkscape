package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950AbilityFoundationTest {
    @Test public void verifiedRotationSeparatesUtilityDamageAndAdrenalineTiers(){
        assertEquals(60,Native950AbilityCatalog.DEFINITIONS.size());
        long melee=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==1).count();
        long ranged=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==5).count();
        long magic=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==6&&d.targetRequired()).count();
        long necromancy=Native950AbilityCatalog.DEFINITIONS.stream().filter(d->d.book==7&&d.targetRequired()).count();
        assertEquals(15,melee);assertEquals(11,ranged);assertEquals(11,magic);assertEquals(6,necromancy);
        Native950AbilityCatalog.Definition surge=Native950AbilityCatalog.get(14726);
        assertFalse(surge.targetRequired());assertEquals(34,surge.cooldown);assertEquals(16,surge.skill);assertEquals(5,surge.level);
        assertEquals(0,Native950AbilityCatalog.get(14682).adrenalineCost());
        assertEquals(25,Native950AbilityCatalog.get(14704).adrenalineCost());
        assertEquals(60,Native950AbilityCatalog.get(14736).adrenalineCost());
        assertTrue(Native950AbilityCatalog.get(14684).offhandRequired);
        assertTrue(Native950AbilityCatalog.get(14685).twoHandedRequired);
    }
    @Test public void pairedModernisedAdrenalineDefinitionsAreNotLegacyThresholdRules(){
        for(Native950AbilityCatalog.Definition d:Native950AbilityCatalog.DEFINITIONS){
            assertEquals(d.name,d.tier==3?50:d.adrenalineCost(),d.adrenalineRequired());
            if(d.tier==1)assertEquals(d.name,d.struct==14679?12:9,d.adrenalineGain());
            else assertEquals(d.name,0,d.adrenalineGain());
        }
        assertEquals(0,Native950AbilityCatalog.get(44244).adrenalineCost());
        assertEquals(0,Native950AbilityCatalog.get(14666).adrenalineCost());
        assertEquals(100,Native950AbilityCatalog.get(14707).adrenalineCost());
        assertEquals(7,Native950AbilityCatalog.get(14726).tier);
        assertEquals(3,Native950AbilityCatalog.get(48296).style());
        assertEquals(60,Native950AbilityCatalog.get(48297).adrenalineCost());
        assertEquals(20,Native950AbilityCatalog.get(48308).adrenalineCost());
        assertEquals(10,Native950AbilityCatalog.get(48311).adrenalineCost());
        assertEquals(60,Native950AbilityCatalog.get(48314).adrenalineCost());
    }
    @Test public void dragMaskEscapesBookClippingWithoutEnablingOtherOperations(){
        assertTrue((Native950ActionBar.ABILITY_EVENTS&(1<<23))!=0);
        assertEquals(2,Native950ActionBar.ABILITY_EVENTS&0x7fe);
        assertEquals(2,(Native950ActionBar.ABILITY_EVENTS>>>11)&127);
        for(int i=0;i<14;i++)assertEquals(i,Native950ActionBar.barSlot(1436,20+13*i));
        assertEquals(1,Native950ActionBar.bookType(1450,3));assertEquals(6,Native950ActionBar.bookType(1885,1));
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
        assertEquals(14707,Native950Revolution.select(new int[]{14726,14707,14682},3,id->true));
        assertEquals(19251,Native950Revolution.select(new int[]{19251},1,id->true));
    }
    @Test public void effectCadenceIsIndependentOfMissingLegacyAnimationFrames(){
        assertEquals(1,Native950AbilityCatalog.animationTicksForMillis(0));
        assertEquals(1,Native950AbilityCatalog.animationTicksForMillis(600));
        assertEquals(2,Native950AbilityCatalog.animationTicksForMillis(601));
        Native950AbilityCatalog.Definition asphyxiate=Native950AbilityCatalog.get(14731);
        assertTrue(asphyxiate.channelled());assertEquals(7,asphyxiate.channelTicks());
        for(int hit=0;hit<4;hit++)assertEquals(hit*2,asphyxiate.hitDelay(hit));
        assertEquals(5,Native950AbilityCatalog.get(14701).hitDelay(2));
        assertFalse(Native950AbilityCatalog.get(14733).channelled());
        assertEquals(1,Native950AbilityCatalog.get(14733).hitDelay(1));
    }
    @Test public void nativeMagicGridSelectsOnlyVerifiedCombatAirSpells(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("spell-test",new WorldTile(3217,3258,0),c);p.setActive(true);
            p.getSkills().set(Skills.MAGIC,1);
            assertEquals("Air Strike selected for native auto-casting.",Native950AutoSpells.choose(p,c,14));
            assertSame(Native950AutoSpells.Spell.STRIKE,Native950AutoSpells.select(p));
            c.flush();assertTrue(hasPacket(packets(c),Native950Packets.varbitSmall(43,14)));
            assertTrue(Native950AutoSpells.choose(p,73).contains("level 81 Magic"));
            p.getSkills().set(Skills.MAGIC,81);
            assertEquals("Air Surge selected for native auto-casting.",Native950AutoSpells.choose(p,73));
            assertSame(Native950AutoSpells.Spell.SURGE,Native950AutoSpells.select(p));
            assertTrue(Native950AutoSpells.choose(p,15).contains("not in the supported"));
            Native950AutoSpells.clear(p);
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void pairedChannelsHaveExplicitCadenceAndBaseUltimatesDoNotAssumeAnIgneousCape(){
        for(int id:new int[]{14684,14670}){
            Native950AbilityCatalog.Definition d=Native950AbilityCatalog.get(id);
            assertEquals(8,d.hits);assertEquals(8,d.channelTicks());
            for(int hit=0;hit<8;hit++)assertEquals(hit,d.hitDelay(hit));
        }
        Native950AbilityCatalog.Definition concentrated=Native950AbilityCatalog.get(19343);
        assertEquals(3,concentrated.hits);assertEquals(3,concentrated.channelTicks());
        assertEquals(2,Native950AbilityCatalog.get(14666).hitDelay(0));
        assertEquals(3,Native950AbilityCatalog.get(14666).channelTicks());
        assertEquals(Native950AbilityCatalog.Effect.DIRECT,Native950AbilityCatalog.get(14674).effect);
        assertEquals(4,Native950AbilityCatalog.get(14674).hits);
        assertEquals(1,Native950AbilityCatalog.get(14736).hits);
    }
    @Test public void overloadFlaskRoutesOnlyThroughTheEstablishedDrinkOwner(){
        assertTrue(Native950Potions.handles(23531,"Drink"));
        assertTrue(Native950Potions.handles(15332,"Drink"));
        assertFalse(Native950Potions.handles(23531,"Drop"));
        assertFalse(Native950Potions.handles(995,"Drink"));
    }
    @Test public void recordedAutocastOptionTwoChangesSelectionAndPublishesIt(){
        EmbeddedChannel c=new EmbeddedChannel();Player p=Player.createNative950("spell-test",new WorldTile(3217,3258,0),c);
        try{
            p.setActive(true);p.getSkills().set(Skills.MAGIC,99);
            p.getInterfaceManager().registerNativeOpen(1461,1477,1);
            assertTrue(p.getNative950ActionBar().button(p,c,click(1461,1,58,122)));
            assertSame(Native950AutoSpells.Spell.WAVE,Native950AutoSpells.select(p));
            c.flush();assertTrue(hasPacket(packets(c),Native950Packets.varbitSmall(43,58)));
            p.getNative950ActionBar().button(p,c,click(1461,1,37,122));
            assertSame(Native950AutoSpells.Spell.BLAST,Native950AutoSpells.select(p));
            p.getInterfaceManager().unregisterNativeOpen(1461);
            p.getNative950ActionBar().button(p,c,click(1461,1,73,122));
            assertSame(Native950AutoSpells.Spell.BLAST,Native950AutoSpells.select(p));
        }finally{Native950AutoSpells.clear(p);c.finishAndReleaseAll();}
    }
    @Test public void spellShortcutAndCommandShareOnePersistedSelection(){
        EmbeddedChannel c=new EmbeddedChannel();Player p=Player.createNative950("spell-test",new WorldTile(3217,3258,0),c);
        Player restored=Player.createNative950("spell-test",new WorldTile(3217,3258,0),c);
        try{
            p.getSkills().set(Skills.MAGIC,99);restored.getSkills().set(Skills.MAGIC,99);
            p.getInterfaceManager().registerNativeOpen(1430,1477,1);
            p.getNative950ActionBar().restore(Collections.singletonMap("actionBar.0",Native950ActionBar.pack(6,14)));
            p.getNative950ActionBar().button(p,c,click(1430,66,-1));
            assertSame(Native950AutoSpells.Spell.STRIKE,Native950AutoSpells.select(p));
            Map<String,Integer> settings=p.nativeSettingsSnapshot();assertTrue(settings.size()<=Native950Save.MAX_SETTINGS);
            restored.applyNativeSettings(settings);
            assertSame(Native950AutoSpells.Spell.STRIKE,Native950AutoSpells.select(restored));
            Native950AutoSpells.choose(restored,c,"wave");
            assertSame(Native950AutoSpells.Spell.WAVE,Native950AutoSpells.select(restored));
        }finally{Native950AutoSpells.clear(p);Native950AutoSpells.clear(restored);c.finishAndReleaseAll();}
    }
    @Test public void drainingMagicCannotSilentlyChangeAnExplicitAutocastSelection(){
        EmbeddedChannel c=new EmbeddedChannel();Player p=Player.createNative950("spell-test",new WorldTile(3217,3258,0),c);
        try{
            p.getSkills().set(Skills.MAGIC,81);Native950AutoSpells.choose(p,73);
            p.getSkills().set(Skills.MAGIC,80);p.setInfiniteCombatRunes(true);
            Native950CombatStyles.Profile staff=new Native950CombatStyles.Profile(2,6,99,4,8,-1,-1,0,true);
            assertSame(Native950AutoSpells.Spell.SURGE,Native950AutoSpells.select(p));
            assertEquals("You need level 81 Magic to cast the selected Air Surge.",staff.costRefusal(p));
            assertFalse(staff.consume(p));
            p.getSkills().set(Skills.MAGIC,81);assertNull(staff.costRefusal(p));
        }finally{Native950AutoSpells.clear(p);c.finishAndReleaseAll();}
    }
    @Test public void bootstrapEnablesPrayerAndCombatSpellGridWithOnlyOptionOne(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            new Native950ActionBar().bootstrap(c);c.flush();
            List<Native950Packets.Packet> packets=packets(c);
            assertTrue(hasPacket(packets,Native950Packets.interfaceEvents(1458,39,0,44,8388610)));
            assertTrue(hasPacket(packets,Native950Packets.interfaceEvents(1885,1,0,Native950ActionBar.BOOK_LAST_SLOT,8617038)));
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(44637,0)));
            assertTrue(hasPacket(packets,Native950Packets.varbitSmall(27344,0)));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void nativePrayerRoutesOnlyThe950ClickableGridChild(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("prayer-grid-test",new WorldTile(3217,3258,0),c);p.setActive(true);
            assertTrue(Native950Prayer.button(p,click(1458,39,0)));
            assertFalse(Native950Prayer.button(p,click(1458,33,0)));
        }finally{c.finishAndReleaseAll();}
    }
    @Test public void exitIsModalAndConfirmationSurvivesUntilExplicitCancel(){
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("exit-test",new WorldTile(3217,3258,0),c);p.setActive(true);
            Native950ExitUi ui=new Native950ExitUi(p,c,()->{},lobby->fail("Unconfirmed logout"));
            assertTrue(ui.handle(click(1477,99,1)));assertTrue(ui.isOpen());
            c.flush();Object packet;boolean modal=false;
            Native950Packets.Packet expected=Native950Packets.openSub(1477,806,1433,true);
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
        return click(face,component,slot,18);
    }
    private static Native950Actions.InterfaceAction click(int face,int component,int slot,int opcode){
        int hash=face<<16|component;
        return (Native950Actions.InterfaceAction)Native950Actions.decode(opcode,new byte[]{-1,-1,-1,(byte)(hash>>16),(byte)(hash>>24),(byte)hash,(byte)(hash>>8),(byte)(slot>>8),(byte)slot});
    }
    private static List<Native950Packets.Packet> packets(EmbeddedChannel channel){
        List<Native950Packets.Packet> packets=new ArrayList<>();Object packet;
        while((packet=channel.readOutbound())!=null)if(packet instanceof Native950Packets.Packet){
            packets.add((Native950Packets.Packet)packet);
        }return packets;
    }
    private static boolean hasPacket(List<Native950Packets.Packet> packets,Native950Packets.Packet expected){
        for(Native950Packets.Packet actual:packets)if(actual.type()==expected.type()&&Arrays.equals(actual.payload(),expected.payload()))return true;
        return false;
    }
}
