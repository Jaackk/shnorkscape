package com.rs.game.player.client;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.Fishing.Fish;
import com.rs.game.player.actions.Fishing.FishingSpots;
import java.util.concurrent.ThreadLocalRandom;

/** Native boundary around the original Fishing Action, catch tables and cadence. */
public final class Native950Fishing {
    private Native950Fishing() { }
    public static boolean isFishingSpot(NPC npc,int option){return definition(npc,option)!=null;}
    public static FishingSpots definition(NPC npc,int option) {
        if(npc==null || (option!=1&&option!=3))return null;
        int legacy=option==3?2:1;
        FishingSpots spot=FishingSpots.forId(npc.getId()|(legacy<<24));
        if(!supports(spot))return null;
        String name=npc.getNative950MenuOption(option);
        return expectedOption(spot).equalsIgnoreCase(name)?spot:null;
    }
    public static boolean start(Player player,NPC npc,int option) {
        FishingSpots spot=definition(npc,option);
        return player!=null&&player.isNative950()&&spot!=null&&player.getActionManager().setAction(new Fishing(spot,npc));
    }
    public static boolean supports(FishingSpots spot) {
        if(spot==null || !Native950FishingCookingAssets.verified("npc",spot.getId())
                || !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,spot.getId())
                || !Native950FishingCookingAssets.verified("sequence",spot.getAnimation().getIds()[0])
                || !Native950FishingCookingAssets.item(spot.getTool())
                || (spot.getBait()>0&&!Native950FishingCookingAssets.item(spot.getBait())))return false;
        for(Fish fish:spot.getFish())if(!Native950FishingCookingAssets.item(fish.getId()))return false;
        return true;
    }
    private static String expectedOption(FishingSpots spot) {
        switch(spot) {
            case CAVEFISH_SHOAL:case ROCKTAIL_SHOAL:case BAIT:case BAIT2:case BAIT3:return "Bait";
            case LURE:case LURE2:return "Lure";
            case CAGE:case CAGE2:return "Cage";
            case HARPOON:case HARPOON2:case SPECIAL2:return "Harpoon";
            default:return "Net";
        }
    }
    static int catchRoll(int level,Fish fish) {
        int roll=(int)Math.floor(fish.getLowChance()+((Math.min(Math.max(level,1),99)-1)*(fish.getHighChance()-fish.getLowChance())/98.0));
        return Math.max(1,Math.min(255,roll));
    }
    static int delay(FishingSpots spot) {
        switch(spot){case CAVEFISH_SHOAL:case ROCKTAIL_SHOAL:case NET2:case HARPOON:case HARPOON2:case GREAT_WHITE:return 5;default:return 4;}
    }
    /** Owned by one original Fishing instance; ActionManager is still the only scheduler. */
    public static final class Journey {
        private final FishingSpots spot;
        private final NPC npc;
        private final WorldTile spotTile;
        private WorldTile origin;private Object controller;private boolean stopped;
        public Journey(FishingSpots spot,NPC npc){this.spot=spot;this.npc=npc;this.spotTile=npc==null?null:new WorldTile(npc);}
        public boolean start(Player player){
            if(!supports(spot)||npc==null||npc.getId()!=spot.getId())return false;
            origin=new WorldTile(player);controller=player.getControlerManager().getControler();
            if(player.getNextWalkDirection()!=-1||player.hasTeleported())return false;
            if(!process(player))return false;
            int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),player.getSize(),new EntityStrategy(npc),false);
            if(route!=0||RouteFinder.lastIsAlternative())return false;
            animate(player);player.sendMessage("You attempt to capture a fish...");return true;
        }
        public boolean process(Player player){
            if(stopped||controller!=player.getControlerManager().getControler()||Native950Thieving.underCombat(player)||origin==null||!player.isActive()||player.hasFinished()||player.isDead()||player.isLocked()
                    ||player.getNextWorldTile()!=null||player.isNative950ForceMovementActive()||player.getNextForceMovement()!=null
                    ||player.hasWalkSteps()||!player.matches(origin)||npc.hasFinished()||npc.isDead()
                    ||npc.isCantInteract()||!npc.matches(spotTile)||npc.getIndex()<1||World.getNPCs().get(npc.getIndex())!=npc
                    ||player.getPlane()!=npc.getPlane()||Math.abs(player.getX()-npc.getX())>1||Math.abs(player.getY()-npc.getY())>1)return false;
            if(definition(npc,spot.getOption()==2?3:1)!=spot)return false;
            int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),player.getSize(),new EntityStrategy(npc),false);
            if(route!=0||RouteFinder.lastIsAlternative())return false;
            if(player.getSkills().getLevel(Skills.FISHING)<spot.getFish()[0].getLevel()){
                player.sendMessage("You need a Fishing level of "+spot.getFish()[0].getLevel()+" to fish here.");return false;}
            if(!player.getInventory().containsItem(spot.getTool(),1)&&!Native950Toolbelt.has(player,spot.getTool())){
                player.sendMessage("You need the fishing tool in your backpack or tool belt.");return false;}
            if(spot.getBait()>0&&!player.getInventory().containsItem(spot.getBait(),1)){
                player.sendMessage("You have run out of fishing bait.");return false;}
            if(!(spot.getBait()>0?Native950Skilling.canExchange(player,ingredients(),new Item[]{new Item(spot.getFish()[0].getId(),1)}):Native950Skilling.hasSpace(player,spot.getFish()[0].getId(),1))){
                player.sendMessage("Your backpack has no room for another catch.");return false;}
            return true;
        }
        private Item[] ingredients(){return spot.getBait()>0?new Item[]{new Item(spot.getBait(),1)}:new Item[0];}
        public int catchFish(Player player){
            if(!process(player))return -1;
            for(int i=spot.getFish().length-1;i>=0;i--){
                Fish fish=spot.getFish()[i];
                if(player.getSkills().getLevel(Skills.FISHING)<fish.getLevel()
                        ||ThreadLocalRandom.current().nextInt(256)>=catchRoll(player.getSkills().getLevel(Skills.FISHING),fish))continue;
                if(!(spot.getBait()>0?Native950Skilling.exchange(player,ingredients(),new Item[]{new Item(fish.getId(),1)}):Native950Skilling.giveItem(player,fish.getId(),1)))return -1;
                player.getSkills().addXp(Skills.FISHING,fish.getXp());player.addFishCaught(1);
                player.sendMessage("You catch "+Native950Skilling.itemType(player,fish.getId()).name.toLowerCase()+".");break;
            }
            animate(player);return Native950Fishing.delay(spot);
        }
        private void animate(Player player){player.setNextAnimation(spot.getAnimation());player.setNextFaceEntity(npc);}
        public int delay(){return Native950Fishing.delay(spot);}
        public void stop(Player player){stopped=true;player.setNextAnimation(new Animation(-1));player.setNextFaceEntity(null);player.fishingDelay=-1;}
    }
}
