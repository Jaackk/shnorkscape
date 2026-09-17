package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.network.protocol.modern950.Native950Packets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Real950 encrypted management clicks and outgoing frames; no live account or client needed. */
public final class Native950NavigationAcceptance {
    public static void main(String[] args)throws Exception{
        require(args.length==1,"Usage: Native950NavigationAcceptance <950-flat-cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950SkillGuide.verify();
        Native950World.getInstance().execute(()->{try(Native950ProductionThirdPassAcceptance.Fixture f=new Native950ProductionThirdPassAcceptance.Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
    }
    private static void check(Native950ProductionThirdPassAcceptance.Fixture f)throws Exception{
        java.lang.reflect.Field field=Native950Interactions.class.getDeclaredField("skillGuide");field.setAccessible(true);
        Native950SkillGuide guide=(Native950SkillGuide)field.get(f.input);Native950Navigation nav=guide.navigation();
        com.rs.game.player.client.ui.Native950CacheReader reader=new com.rs.game.player.client.ui.Native950CacheReader.Flat();
        int equipmentHost=reader.structInt(reader.enumInt(7716,3),3505);
        require(equipmentHost>>>16==1477,"Equipment cache parent is not the game root");
        // Exercise the same actual-packet mirror as successful world admission, not invented manager state.
        List<Native950Packets.Packet> bootstrap=Arrays.asList(Native950Packets.openSub(1477,30,1482,true),
                Native950Packets.openSub(equipmentHost>>>16,equipmentHost&65535,1462,true));
        for(Native950Packets.Packet packet:bootstrap)f.channel.write(packet);f.drain();
        Native950World.mirrorBootstrapInterfaces(f.player,bootstrap);
        require(f.player.getInterfaceManager().getInterfaceParentId(1462)==equipmentHost,"Actual bootstrap equipment mount was not mirrored");
        int originalEquipment=f.player.getInterfaceManager().getInterfaceParentId(1462),pages=0;
        for(int actor:new int[]{0,1,2,3,4,6}){
            f.nativeButton(1431,0,actor,-1);f.drain();int menu=Native950Navigation.menuForRibbonActor(actor);
            require(nav.menu()==menu,"Ribbon actor did not select menu "+actor);int shellOpens=mounts(f,1448,1477,715);
            for(Native950Navigation.Page page:Native950Navigation.pages(menu))if(!page.hidden){
                f.nativeButton(1477,714,page.number*4-1,-1);f.drain();pages++;
                require(nav.isPage(menu,page.number),"Header did not select "+menu+":"+page.number);
                int[] faces=page.interfaces();for(int slot=0;slot<faces.length;slot++)if(faces[slot]>=0)
                    require(f.player.getInterfaceManager().getInterfaceParentId(faces[slot])==((1448<<16)|(3+slot*2)),"Wrong page mount "+faces[slot]);
                require(mounts(f,1448,1477,715)==shellOpens,"Switching tabs recreated management shell");
                int before=f.count(ServerPacket.IF_OPENSUB);
                f.nativeButton(1477,714,page.number*4-1,-1);f.drain();
                require(f.count(ServerPacket.IF_OPENSUB)==before,"Repeat tab click remounted content");
                require(f.player.getInterfaceManager().getInterfaceParentId(1482)==((1477<<16)|30),"Navigation detached scene");
            }
        }
        // Default remembered page is server-owned; forged item/actor clicks cannot replace it.
        int unownedMenu=nav.menu(),unownedPage=nav.page();
        f.nativeButton(1433,6,0,-1);f.drain();require(nav.isPage(unownedMenu,unownedPage),"Unowned Quick Options action changed navigation");
        for(int actor=0;actor<5;actor++){
            f.nativeButton(1477,99,1,-1);f.drain();require(!nav.isOpen(),"Exit did not retire management owner");
            f.nativeButton(1433,6,actor,-1);f.drain();require(nav.menu()==Native950Navigation.menuForQuickActor(actor),"Quick menu actor mapping failed "+actor);
        }
        int beforeMenu=nav.menu(),beforePage=nav.page();
        f.nativeButton(1431,0,0,4151);f.nativeButton(1477,714,27,-1);f.drain();
        require(nav.isPage(beforeMenu,beforePage),"Invalid actor/item changed navigation");
        f.nativeButton(1431,0,5,-1);f.drain();require(nav.isPage(beforeMenu,beforePage),"Marketplace replaced local page");
        f.nativeButton(1477,99,1,-1);f.nativeButton(1433,6,5,-1);f.drain();require(!nav.isOpen(),"Quick Marketplace opened a retail page");
        // Header return to Skills attaches its native trigger once; icons then refresh in place.
        f.nativeButton(1466,7,27,-1);f.drain();require(nav.isPage(0,2),"Skill shortcut did not switch to Hero Skills");
        int openCount=f.count(ServerPacket.IF_OPENSUB);f.nativeButton(1218,168,-1,-1);f.drain();
        require(f.count(ServerPacket.IF_OPENSUB)==openCount,"Guide icon remounted shell/page");
        require(f.player.getInterfaceManager().getInterfaceParentId(1217)==(1218<<16),"Skills trigger missing");
        int loadoutStart=f.captured.size();
        f.nativeButton(1477,714,11,-1);f.drain();require(nav.isPage(0,3),"Loadout not selected");
        require(f.player.getInterfaceManager().getInterfaceParentId(1462)==((1448<<16)|7),"Equipment not relocated into Loadout");
        int oldClose=packetIndex(f,loadoutStart,Native950Packets.closeSub(equipmentHost>>>16,equipmentHost&65535));
        int movedOpen=packetIndex(f,loadoutStart,Native950Packets.openSub(1448,7,1462,true));
        int layout=packetIndex(f,loadoutStart,Native950Packets.runClientScript(8283,Native950Navigation.page(0,3).structure,2));
        int refresh=packetIndex(f,loadoutStart,Native950Packets.runClientScript(8471,(1462<<16)|3,94));
        require(oldClose>=loadoutStart&&movedOpen>oldClose,"Old HUD equipment must close before Loadout opens its single copy");
        require(layout>movedOpen&&refresh>layout,"Equipment must refresh after native Loadout dimensions are selected");
        int restoreStart=f.captured.size();
        f.nativeButton(1477,717,1,-1);f.drain();require(!guide.isOpen(),"Close kept owner alive");
        int restoredOpen=packetIndex(f,restoreStart,Native950Packets.openSub(equipmentHost>>>16,equipmentHost&65535,1462,true));
        int restoredRefresh=packetIndex(f,restoreStart,Native950Packets.runClientScript(8471,(1462<<16)|3,94));
        require(restoredOpen>=restoreStart&&restoredRefresh>restoredOpen,"Restored HUD must rebuild its native equipment layout");
        require(f.player.getInterfaceManager().getInterfaceParentId(1462)==originalEquipment,"Equipment HUD mount not restored");
        for(int face:new int[]{1448,1218,1217,1474,1463})require(!f.player.getInterfaceManager().containsInterface(face),"Leaked page "+face);
        int closed=f.count(ServerPacket.IF_CLOSESUB);f.nativeButton(1477,717,1,-1);f.drain();require(f.count(ServerPacket.IF_CLOSESUB)==closed,"Stale close emitted interface closure");
        f.nativeButton(1431,0,0,-1);f.drain();require(nav.isPage(0,3),"Hero did not remember Loadout");
        f.nativeButton(1431,0,7,-1);f.drain();require(!guide.isOpen(),"Settings did not retire navigation");
        require(f.player.getInterfaceManager().getInterfaceParentId(742)==(1426<<16),"Settings content missing after navigation handoff");
        f.nativeButton(1431,0,3,-1);f.drain();require(nav.menu()==3,"Adventure did not replace Settings");
        require(!f.player.getInterfaceManager().containsInterface(742),"Settings child leaked after navigation handoff");
        f.input.walking();f.drain();require(!guide.isOpen(),"Walking did not close navigation");
        f.nativeButton(1477,99,1,-1);f.nativeButton(1433,35,-1,-1);f.drain();
        require(f.player.getInterfaceManager().containsInterface(567),"Owned Quick Ribbon did not open Settings Ribbon");
        require(!f.player.getInterfaceManager().containsInterface(1433),"Exit overlay leaked after Quick Ribbon handoff");
        f.input.walking();f.drain();
        for(Native950ProductionThirdPassAcceptance.Fixture.CapturedPacket p:f.captured)
            if(p.kind==ServerPacket.IF_CLOSESUB)require(closeHash(p.body)!=((1477<<16)|30),"Scene closed on outgoing wire");
        System.out.println("PASS: "+pages+" visible native pages, six ribbon menus, encrypted header/repeat/close events, Skills no-remount, Loadout HUD relocation/restore, Settings handoff and stale/invalid input; "+f.output.size()+" decoded frames.");
        System.out.println("LIMIT: verifies navigation and packet ownership; native drawing and page-specific gameplay remain separate acceptance checks.");
    }
    private static int mounts(Native950ProductionThirdPassAcceptance.Fixture f,int face,int parent,int component){
        int n=0;for(Native950ProductionThirdPassAcceptance.Fixture.CapturedPacket p:f.captured)if(p.kind==ServerPacket.IF_OPENSUB){
            byte[] b=p.body;int hash=(b[0]&255)<<24|(b[1]&255)<<16|(b[2]&255)<<8|b[3]&255;
            int id=((b[16]&255)-128&255)|((b[17]&255)<<8);if(hash==(parent<<16|component)&&id==face)n++;
        }return n;
    }
    private static int packetIndex(Native950ProductionThirdPassAcceptance.Fixture f,int start,Native950Packets.Packet expected){
        for(int i=start;i<f.captured.size();i++){
            Native950ProductionThirdPassAcceptance.Fixture.CapturedPacket p=f.captured.get(i);
            if(p.kind==expected.type()&&Arrays.equals(p.body,expected.payload()))return i;
        }return -1;
    }
    private static int closeHash(byte[] b){return (b[0]&255)<<16|(b[1]&255)<<24|(b[2]&255)|(b[3]&255)<<8;}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
