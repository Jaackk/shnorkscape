package com.rs.game.player.client;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Cooking.Cookables;
import java.util.*;

/** Original Cooking Action/Cookables/burn curve through950 inventory and scene checks. */
public final class Native950Cooking {
    private Native950Cooking() { }
    public static boolean isCookingObject(WorldObject object) {
        if(object==null||object.getType()<0||object.getType()>22)return false;
        if(Native950Firemaking.isFireObject(object))return true;
        try {
            ObjectDefinitions d=object.getDefinitions();
            if(!d.loaded||d.transforms!=null||d.sizeX<1||d.sizeY<1||d.sizeX>16||d.sizeY>16)return false;
            String name=d.name==null?"":d.name.toLowerCase(Locale.ROOT);
            if(!(name.equals("fire")||name.equals("cooking fire")||name.equals("range")||name.equals("cooking range")||name.equals("stove")))return false;
            if(d.options!=null)for(String option:d.options)if(isCookOption(option))return true;
            return false;
        }catch(RuntimeException missing){return false;}
    }
    public static boolean isCookOption(String option){return "Cook".equalsIgnoreCase(option)||"Cook-at".equalsIgnoreCase(option)||"Cook-on".equalsIgnoreCase(option);}
    public static boolean supports(Cookables c) {
        if(c==null||c.isSpitRoast()||c.isCookingStation()||c==Cookables.BARON_SHARK||c==Cookables.POISON_KARAMBWAN)return false;
        return Native950FishingCookingAssets.verified("sequence",897)
                &&Native950FishingCookingAssets.item(c.getRawItem().getId())&&Native950FishingCookingAssets.item(c.getProduct().getId())
                &&(c.getBurningLvl()<=c.getLvl()||Native950FishingCookingAssets.item(c.getBurntId().getId()));
    }
    public static boolean supportsRaw(int rawId){return supports(Cookables.forId(rawId))||Native950Baking.forRaw(rawId)!=null;}
    public static Native950ProductionMenu.Choice choice(Player player,WorldObject object,int rawId){
        Native950Baking.Food food=Native950Baking.forRaw(rawId);return food!=null?Native950Baking.choice(player,object,food):choice(player,object,Cookables.forId(rawId));
    }
    public static List<Cookables> available(Player player,WorldObject object) {
        if(!isCookingObject(object))return Collections.emptyList();
        List<Cookables> found=new ArrayList<>();
        for(Cookables c:Cookables.values())if(supports(c)&&(!c.isFireOnly()||isFire(object))&&player.getInventory().containsItem(c.getRawItem().getId(),1))found.add(c);
        return found;
    }
    public static Native950ProductionMenu.Choice choice(Player player,WorldObject object,Cookables food){
        Native950Production.Recipe preview=new Native950Production.Recipe("Cook "+Native950Production.name(food.getRawItem().getId()),Skills.COOKING,food.getLvl(),food.getXp()*0.4,897,2,
            new Item[]{new Item(food.getRawItem().getId(),1)},new Item[]{new Item(food.getProduct())});
        return new Native950ProductionMenu.Choice("Cook "+Native950Production.name(food.getRawItem().getId())+" (level "+food.getLvl()+")",preview,n->start(player,object,food.getRawItem().getId(),n));
    }
    public static List<Native950ProductionMenu.Choice> choices(Player player,WorldObject object){
        List<Native950ProductionMenu.Choice> choices=new ArrayList<>();for(Cookables food:available(player,object))choices.add(choice(player,object,food));if(isCookingObject(object))for(Native950Baking.Food food:Native950Baking.foods())if(player.getInventory().containsItem(food.raw,1))choices.add(Native950Baking.choice(player,object,food));return choices;
    }
    public static boolean start(Player player,WorldObject object,int rawId,int quantity) {
        Native950Baking.Food extra=Native950Baking.forRaw(rawId);if(extra!=null)return Native950Baking.start(player,object,extra,quantity);
        return player!=null&&player.isNative950()&&quantity>0&&quantity<=10000&&supports(Cookables.forId(rawId))&&isCookingObject(object)
                &&player.getActionManager().setAction(new Cooking(object,new Item(rawId,1),false,quantity));
    }
    static boolean needsRange(Cookables food){return food!=null&&(food.name().endsWith("_PIE")||food==Cookables.RAW_POTATO);}
    private static boolean isFire(WorldObject object){return object.getDefinitions().name.toLowerCase(Locale.ROOT).contains("fire");}
    static double burnChance(Cookables c,int level,boolean fire){
        if(level>=c.getBurningLvl())return 0;
        double base=fire?55:50;
        return Math.max(0,Math.min(100,base-base*(level-c.getLvl())/(c.getBurningLvl()-c.getLvl())));
    }
    static int delay(Cookables c,int level){return Math.max(1,Math.min(3,3-Math.max(0,level-c.getLvl())/4));}
    /** One original Cooking instance; no second task/timer loop. */
    public static final class Journey {
        private final WorldObject object;
        private final Cookables cook;
        private int remaining;
        private WorldTile origin;
        public Journey(WorldObject object,int rawId,int quantity){this.object=object;this.cook=Cookables.forId(rawId);this.remaining=quantity;}
        public boolean start(Player player){
            if(!supports(cook)||!isCookingObject(object)||remaining<1)return false;
            origin=new WorldTile(player);if(!process(player))return false;
            int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),player.getSize(),new ObjectStrategy(object),false);
            if(route!=0||RouteFinder.lastIsAlternative())return false;
            player.faceObject(object);player.sendMessage("You attempt to cook the food...");if(burnChance(cook,player.getSkills().getLevel(Skills.COOKING),isFire(object))>0)player.sendMessage("At your current Cooking level, some of this food may burn.");return true;
        }
        public boolean process(Player player){
            if(origin==null||remaining<1||!player.isActive()||player.hasFinished()||player.isDead()||player.isLocked()
                    ||player.getNextWorldTile()!=null||player.getNextForceMovement()!=null||player.isNative950ForceMovementActive()
                    ||player.hasWalkSteps()||!player.matches(origin)||!Native950Woodcutting.current(object)
                    ||!Native950Woodcutting.inReach(player,object))return false;
            if(needsRange(cook)&&isFire(object)){player.sendMessage("Use a cooking range to bake this food.");return false;}
            if(cook.isFireOnly()&&!isFire(object)){player.sendMessage("You can only cook this on a fire.");return false;}
            if(player.getSkills().getLevel(Skills.COOKING)<cook.getLvl()){
                player.sendMessage("You need a Cooking level of "+cook.getLvl()+" to cook this.");return false;}
            return player.getInventory().containsItem(cook.getRawItem().getId(),1);
        }
        public int cook(Player player){
            if(!process(player))return -1;
            boolean burnt=burnChance(cook,player.getSkills().getLevel(Skills.COOKING),isFire(object))>Math.random()*100;
            Item result=burnt?cook.getBurntId():cook.getProduct();
            if(!Native950Skilling.exchange(player,new Item[]{new Item(cook.getRawItem().getId(),1)},new Item[]{new Item(result.getId(),result.getAmount())})){
                player.sendMessage("You need more backpack space to continue cooking.");return -1;}
            player.setNextAnimation(new Animation(897));player.faceObject(object);
            if(burnt)player.sendMessage("Oops! You accidentally burn the food.");
            else {
                // Preserve the original910 server's explicit0.4 Cooking XP multiplier.
                player.getSkills().addXp(Skills.COOKING,cook.getXp()*0.4);player.addFoodCooked();
                player.sendMessage("You successfully cook the food.");
            }
            remaining--;return delay(cook,player.getSkills().getLevel(Skills.COOKING));
        }
        public void stop(Player player){player.setNextAnimation(new Animation(-1));player.clickedObject=null;}
    }
}
