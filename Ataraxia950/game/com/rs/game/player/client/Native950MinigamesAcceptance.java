package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/** Current activity enums through normal encrypted list/dropdown inputs; no profiles or live client. */
public final class Native950MinigamesAcceptance {
    public static void main(String[] args)throws Exception{
        require(args.length==1,"Usage: Native950MinigamesAcceptance <950 cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950Minigames.verifyCacheBindings();
        Native950World.getInstance().execute(()->{try(Native950ProductionThirdPassAcceptance.Fixture f=new Native950ProductionThirdPassAcceptance.Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
    }
    private static void check(Native950ProductionThirdPassAcceptance.Fixture f)throws Exception{
        Native950Minigames activities=new Native950Minigames(f.player,f.channel);
        java.lang.reflect.Field field=Native950Interactions.class.getDeclaredField("skillGuide");field.setAccessible(true);
        Native950Navigation nav=((Native950SkillGuide)field.get(f.input)).navigation();
        nav.setPageListener(new Native950Navigation.PageListener(){
            public void opened(int menu,int page){activities.opened(menu,page);}
            public void closed(int menu,int page){activities.closed(menu,page);}
            public boolean handle(InterfaceAction action){return activities.handle(action);}
        });
        f.nativeButton(1431,0,3,-1);f.nativeButton(1477,714,15,-1);f.drain();
        require(activities.isOpen()&&nav.isPage(3,4),"Activity browser not opened");
        require(f.scripts.contains(6743)&&f.scripts.contains(6745),"Native list/details initialization missing");
        require(activities.selectedActivity()==-1&&Integer.valueOf(0).equals(f.varps.get(3233)),"Initial prompt must have no selected activity");
        int rows=0;
        for(int category:new int[]{0,2,3,4,5}){
            filter(f,category);require(activities.category()==category,"Wrong native filter");
            for(int actor=0;actor<Native950Minigames.size(category);actor++){
                f.nativeButton(1344,23,actor,-1);f.drain();rows++;
                require(activities.selectedActivity()==Native950Minigames.activity(category,actor),"Wrong struct selected for filter/actor "+category+":"+actor);
                require(Integer.valueOf(actor+1).equals(f.varps.get(3233)),"Activity key must be one-based");
            }
        }
        filter(f,0);int actor=Native950Minigames.size(0)-1;
        f.nativeButton(1344,23,actor,-1);f.nativeButton(1344,34,-1,-1);f.drain();int chosen=activities.selectedActivity();
        require(activities.favourite(chosen),"Favourite not set by native star");
        filter(f,1);require(activities.selectedActivity()==chosen,"Favourite selection lost when filtering");
        f.nativeButton(1344,23,0,-1);f.drain();require(activities.selectedActivity()==chosen,"Hidden non-favourite row accepted");
        f.nativeButton(1344,23,actor,-1);f.drain();require(activities.selectedActivity()==chosen,"Favourite actor was compacted incorrectly");
        f.nativeButton(1344,34,-1,-1);f.drain();require(!activities.favourite(chosen)&&activities.selectedActivity()==-1,"Removing last favourite did not restore empty prompt");
        filter(f,0);f.nativeButton(1344,23,0,-1);f.drain();int selected=activities.selectedActivity();
        f.nativeButton(1344,23,51,-1);f.nativeButton(1344,23,1,4151);f.nativeButton(1477,896,5,-1);f.drain();
        require(activities.selectedActivity()==selected&&activities.category()==0,"Malformed row or unarmed filter changed state");
        int x=f.player.getX(),y=f.player.getY(),stats=f.count(ServerPacket.UPDATE_STAT);
        f.nativeButton(1344,73,-1,-1);f.nativeButton(1344,56,-1,-1);f.nativeButton(1344,82,-1,-1);f.drain();
        require(f.player.getX()==x&&f.player.getY()==y&&f.player.getNextWorldTile()==null&&f.count(ServerPacket.UPDATE_STAT)==stats,"Browsing invoked participation/transport/progress");
        f.nativeButton(1344,31,-1,-1);f.nativeButton(1477,717,1,-1);f.drain();require(!activities.isOpen(),"Close leaked activity owner");
        f.nativeButton(1344,23,1,-1);f.nativeButton(1477,896,3,-1);f.drain();require(activities.selectedActivity()==selected&&activities.category()==0,"Stale row/dropdown changed state");
        f.nativeButton(1431,0,3,-1);f.drain();require(activities.isOpen()&&activities.selectedActivity()==selected,"Reopen lost remembered selection");
        f.input.walking();f.drain();require(!activities.isOpen(),"Walking leaked activity page");
        System.out.println("PASS: "+rows+" current-cache rows across all five populated categories plus stable-ordinal favourites, native list/detail scripts, one-based keys, guarded dropdown, no participation, stale input and remembered selection; "+f.output.size()+" decoded frames.");
    }
    private static void filter(Native950ProductionThirdPassAcceptance.Fixture f,int category){f.nativeButton(1344,31,-1,-1);f.nativeButton(1477,896,category,-1);f.drain();}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
