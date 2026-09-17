package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/** Normal encrypted753 selection, cache binding and stale ownership checks; no live profile writes. */
public final class Native950BeastsAcceptance {
    public static void main(String[] args)throws Exception{
        require(args.length==1,"Usage: Native950BeastsAcceptance <950 cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950Beasts.verifyCacheBindings();
        Native950World.getInstance().execute(()->{try(Native950ProductionThirdPassAcceptance.Fixture f=new Native950ProductionThirdPassAcceptance.Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
    }
    private static void check(Native950ProductionThirdPassAcceptance.Fixture f)throws Exception{
        Native950Beasts beasts=new Native950Beasts(f.player,f.channel);
        java.lang.reflect.Field field=Native950Interactions.class.getDeclaredField("skillGuide");field.setAccessible(true);
        Native950Navigation nav=((Native950SkillGuide)field.get(f.input)).navigation();
        nav.setPageListener(new Native950Navigation.PageListener(){
            public void opened(int menu,int page){beasts.opened(menu,page);}
            public void closed(int menu,int page){beasts.closed(menu,page);}
            public boolean handle(InterfaceAction action){return beasts.handle(action);}
        });
        f.nativeButton(1431,0,3,-1);f.nativeButton(1477,714,19,-1);f.drain();
        require(beasts.isOpen()&&nav.isPage(3,5),"Beasts was not activated by Adventures page5");
        require(f.scripts.contains(3371)&&f.scripts.contains(3869),"Native list/details initialization missing");
        require(Integer.valueOf(0).equals(f.varps.get(4517)),"BossInfo category not selected");
        com.rs.game.player.client.ui.Native950CacheReader reader=new com.rs.game.player.client.ui.Native950CacheReader.Flat();
        for(int i=0;i<Native950Beasts.size();i++){
            int frames=f.output.size();f.nativeButton(753,5,i,-1);f.drain();
            require(beasts.selectedBoss()==reader.enumInt(9031,i),"Row did not select its current cache boss "+i);
            require(f.output.size()>frames,"Boss selection did not redraw native details");
        }
        int last=beasts.selectedBoss();f.nativeButton(753,5,Native950Beasts.size(),-1);f.nativeButton(753,5,0,4151);f.drain();
        require(beasts.selectedBoss()==last,"Invalid boss ordinal/item altered selection");
        f.nativeButton(753,120,-1,-1);f.nativeButton(1477,896,1,-1);f.drain();
        require(Integer.valueOf(0).equals(f.varps.get(4517))&&beasts.selectedBoss()==last,"Unsupported kill records changed boss browsing state");
        int closed=f.count(ServerPacket.IF_CLOSESUB);f.nativeButton(1477,717,1,-1);f.drain();
        require(!beasts.isOpen()&&f.count(ServerPacket.IF_CLOSESUB)>closed,"Closing page kept boss owner alive");
        f.nativeButton(753,5,0,-1);f.nativeButton(1477,896,2,-1);f.drain();require(beasts.selectedBoss()==last,"Stale boss/dropdown input altered selection");
        f.nativeButton(1431,0,3,-1);f.drain();require(beasts.isOpen()&&beasts.selectedBoss()==last,"Reopen did not restore selected boss");
        f.input.walking();f.drain();require(!beasts.isOpen(),"Walking leaked boss ownership");
        System.out.println("PASS: "+Native950Beasts.size()+" current-cache boss rows through encrypted753:5 clicks, native list/details scripts, category refusal, stale input, remembered selection and closure; "+f.output.size()+" decoded frames.");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
