package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.Cooking.Cookables;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
/** Actual-cache/action/atomic-inventory probe in a fresh JVM with no save or listening socket. */
public final class Native950FishingCookingAcceptance {
    public static void main(String[] args)throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Usage: Native950FishingCookingAcceptance <cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950World owner=Native950World.getInstance();
        owner.execute(()->{try(Fixture f=new Fixture()){f.check();}return null;}).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
        System.out.println("PASS: paired950 Fishing/Cooking assets, originalActionManager catches/consumption/XP, no-tool/bait/level/full-bag checks, cooking full-bag exchange and scene cancellation");
    }
    private static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel();
        final Player player=Player.createNative950("fishcook",new WorldTile(3217,3258,0),channel);
        final Native950Containers items;
        final NPC npc;
        WorldObject fire;
        Fixture(){
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            items=new Native950Containers(player,new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops());
            Native950Skilling.attach(player,items);
            npc=NPC.createNative950(327,player.transform(1,0,0),1);World.addNative950Npc(npc);World.updateEntityRegion(npc);
        }
        void check()throws Exception{
            checkLifecycle();
            int fishing=0,cooking=0;for(FishingSpots s:FishingSpots.values())if(Native950Fishing.supports(s))fishing++;
            for(Cookables c:Cookables.values())if(Native950Cooking.supports(c))cooking++;
            require(fishing==16,"Expected16 verified original Fishing methods, got "+fishing);require(cooking>=40,"Too few ordinaryCooking recipes: "+cooking);
            System.out.println("PASS: "+fishing+" fishing methods, "+cooking+" cooking recipes admitted by actual950 assets");
            require(Native950Fishing.definition(npc,1)==FishingSpots.NET,"Net slot mismatch");
            require(Native950Fishing.definition(npc,3)==FishingSpots.BAIT&&Native950Fishing.definition(npc,2)==null,"NativeBait slot must be3");
            require(!Native950Fishing.start(player,npc,1),"Fishing started without tool");give(303,1);
            player.getSkills().setLevelWithoutRefresh(Skills.FISHING,99);player.getSkills().setXpWithoutRefresh(Skills.FISHING,13034431);
            require(Native950Fishing.start(player,npc,1),"Net refused");require(player.getActionManager().getAction() instanceof Fishing,"OriginalFishing action not reused");
            double fishingXp=player.getSkills().getXp(Skills.FISHING);
            for(int i=0;i<250&&player.getSkills().getXp(Skills.FISHING)==fishingXp;i++)tick();
            require(player.getSkills().getXp(Skills.FISHING)>fishingXp,"No Fishing XP/catch");
            require(player.getInventory().containsItem(317,1)||player.getInventory().containsItem(321,1),"No fish in authoritative backpack");
            player.getActionManager().forceStop();clear();give(307,1);
            player.getSkills().setLevelWithoutRefresh(Skills.FISHING,1);require(!Native950Fishing.start(player,npc,3),"Bait level gate missing");
            player.getSkills().setLevelWithoutRefresh(Skills.FISHING,99);require(!Native950Fishing.start(player,npc,3),"Missing bait accepted");give(313,1);
            require(Native950Fishing.start(player,npc,3),"Bait refused");
            for(int i=0;i<250&&player.getInventory().containsItem(313,1);i++)tick();
            require(!player.getInventory().containsItem(313,1),"Successful catch did not consume bait");tick();
            require(!player.getActionManager().hasSkillWorking(),"Missing bait did not retire action");
            clear();give(303,1);for(int i=1;i<28;i++)player.getInventory().items.set(i,new Item(1511,1));
            require(!Native950Fishing.start(player,npc,1),"Full backpack accepted Fishing");clear();give(303,1);
            require(Native950Fishing.start(player,npc,1),"Net restart refused");fishingXp=player.getSkills().getXp(Skills.FISHING);
            player.setNextWorldTile(player.transform(2,0,0));tick();
            require(!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(Skills.FISHING)==fishingXp,"Movement did not cancel catch before reward");
            fire=new WorldObject(70755,10,0,npc.getX(),npc.getY(),npc.getPlane());World.spawnObject(fire);
            require(Native950Cooking.isCookingObject(fire),"Actual950 ordinary fire has no admitted Cook menu");
            clear();for(int i=0;i<28;i++)player.getInventory().items.set(i,new Item(317,1));
            player.getSkills().setLevelWithoutRefresh(Skills.COOKING,99);player.getSkills().setXpWithoutRefresh(Skills.COOKING,13034431);
            double xp=player.getSkills().getXp(Skills.COOKING);
            require(Native950Cooking.start(player,fire,317,1),"Full-bag1-for1 Cooking incorrectly refused");
            require(player.getActionManager().getAction() instanceof Cooking,"OriginalCooking action not reused");
            player.getActionManager().setActionDelay(0);for(int i=0;i<5&&player.getActionManager().hasSkillWorking();i++)tick();
            require(player.getInventory().getAmountOf(317)==27&&player.getInventory().getAmountOf(315)==1,"Cooking exchange/quantity wrong");
            require(player.getSkills().getXp(Skills.COOKING)>xp,"Cooking XP missing");
            require(Native950Cooking.start(player,fire,317,5),"Cooking restart refused");xp=player.getSkills().getXp(Skills.COOKING);
            World.removeObject(fire);fire=null;tick();require(!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(Skills.COOKING)==xp,"Removed fire continued cooking");
            Native950Save save=Native950PlayerBinder.capture(player,items.saveSnapshot("fishcook",player.getX(),player.getY(),player.getPlane()),System.currentTimeMillis());
            require(save.skills().xp(Skills.FISHING)==player.getSkills().getXp(Skills.FISHING)&&save.skills().xp(Skills.COOKING)==player.getSkills().getXp(Skills.COOKING),"Skill save capture mismatch");
        }
        void checkLifecycle()throws Exception{
            clear();give(303,1);player.resetMasks();player.getSkills().setLevelWithoutRefresh(Skills.FISHING,99);
            Native950Fishing.Journey cancelled=new Native950Fishing.Journey(FishingSpots.NET,npc);require(cancelled.start(player),"Fishing cancellation setup");cancelled.stop(player);double xp=player.getSkills().getXp(Skills.FISHING);
            require(cancelled.catchFish(player)==-1&&player.getSkills().getXp(Skills.FISHING)==xp,"Stopped fishing journey awarded");
            Native950Fishing.Journey changed=new Native950Fishing.Journey(FishingSpots.NET,npc);require(changed.start(player),"Fishing controller setup");
            java.lang.reflect.Field field=com.rs.game.player.ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(player.getControlerManager(),new com.rs.game.player.controllers.Controller(){public void start(){}});
            require(changed.catchFish(player)==-1&&player.getSkills().getXp(Skills.FISHING)==xp,"Changed controller continued fishing");changed.stop(player);field.set(player.getControlerManager(),null);
            Native950Fishing.Journey attacked=new Native950Fishing.Journey(FishingSpots.NET,npc);require(attacked.start(player),"Fishing combat setup");player.setAttackedBy(npc);player.setAttackedByDelay(com.rs.utils.Utils.currentTimeMillis()+10000);
            require(attacked.catchFish(player)==-1&&player.getSkills().getXp(Skills.FISHING)==xp,"Combat continued fishing");attacked.stop(player);player.setAttackedBy(null);player.setAttackedByDelay(0);clear();player.resetMasks();
            System.out.println("PASS:stopped fishing callback, controller change and combat cannot grant catches or XP");
        }
        void give(int id,int n){require(Native950Skilling.giveItem(player,id,n),"Cannot grant probe item "+id);}
        void clear(){player.getActionManager().forceStop();for(int i=0;i<28;i++)player.getInventory().items.set(i,null);}
        void tick(){player.processEntity();player.processEntityUpdate();player.resetMasks();channel.flush();channel.runPendingTasks();while(channel.readOutbound()!=null){}channel.checkException();}
        public void close(){player.getActionManager().forceStop();if(fire!=null)World.removeObject(fire);World.removeNative950Npc(npc);World.removeNative950Player(player);Native950Skilling.detach(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
