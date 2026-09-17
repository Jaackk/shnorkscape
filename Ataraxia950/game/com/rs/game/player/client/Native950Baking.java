package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import java.util.*;

/** Bread, pizza and cake on the same ActionManager/inventory boundary as ordinary cooking. */
final class Native950Baking {
    private Native950Baking(){}
    private static Object store;
    private static List<Food> cached;
    static final class Food {
        final Native950Production.Recipe recipe,burnt;
        final int raw,stopBurning;
        Food(Native950Production.Recipe recipe,int burntId,int stopBurning){
            this.recipe=recipe;this.raw=recipe.consumed()[0].getId();this.stopBurning=stopBurning;
            Item[] output=recipe.produced();output[0]=new Item(burntId,1);
            burnt=new Native950Production.Recipe(recipe.label,recipe.skill,recipe.level,0,897,3,recipe.consumed(),output);
        }
    }
    static synchronized List<Food> foods(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();
        if(store==Cache.STORE&&cached!=null)return cached;
        List<Food> list=new ArrayList<>();
        // Bread immunity is documented at37; pizza/cake retain a conservative local99 cap
        // until their exact current client burn curves and all conditional bonuses are mapped.
        int[][] rows={{2309,2311,37},{2289,2305,99},{1891,1903,99}};
        for(int[] row:rows){
            Native950Production.Recipe base=Native950CraftingAssets.product("Bake",row[0],Skills.COOKING,897,3,new Item[0]);
            if(base==null||!Native950CraftingAssets.item(row[1]))continue;
            List<Item> outputs=new ArrayList<>(Arrays.asList(base.produced()));
            if(row[0]==1891){if(!Native950CraftingAssets.item(1887))continue;outputs.add(new Item(1887,1));}
            // Keep the existing server's explicitly configured910 Cooking XP policy.
            Native950Production.Recipe recipe=new Native950Production.Recipe(base.label,Skills.COOKING,base.level,base.xp*0.4,897,3,base.consumed(),outputs.toArray(new Item[0]));
            list.add(new Food(recipe,row[1],row[2]));
        }
        store=Cache.STORE;cached=Collections.unmodifiableList(list);return cached;
    }
    static Food forRaw(int id){for(Food food:foods())if(food.raw==id)return food;return null;}
    static boolean inReach(Player p,WorldObject object){
        return Native950Production.Recipe.ready(p)&&object!=null&&Native950Cooking.isCookingObject(object)
            &&p.getNextWalkDirection()==-1&&Native950Mining.current(object)&&Native950Mining.inReach(p,object);
    }
    static Native950ProductionMenu.Choice choice(Player p,WorldObject object,Food food){return new Native950ProductionMenu.Choice("Bake "+food.recipe.productName()+" (level "+food.recipe.level+")",food.recipe,n->start(p,object,food,n));}
    static boolean start(Player p,WorldObject object,Food food,int count){
        if(food==null||!foods().contains(food)||count<1||count>10000||!inReach(p,object))return false;
        if(object.getDefinitions().name.toLowerCase(Locale.ROOT).contains("fire")){p.sendMessage("Use a cooking range to bake this food.");return false;}
        return p.getActionManager().setAction(new Action(){
            final WorldTile origin=new WorldTile(p);int remaining=count;
            @Override public boolean start(Player p){if(!process(p))return false;p.faceObject(object);p.sendMessage("You prepare to bake the food.");setActionDelay(p,1);return true;}
            @Override public boolean process(Player p){
                if(remaining<1||!p.matches(origin)||!inReach(p,object))return false;
                String refusal=food.recipe.refusal(p);if(refusal!=null){p.sendMessage(refusal);return false;}return true;
            }
            @Override public int processWithDelay(Player p){
                if(!process(p))return -1;
                double chance=burnChance(food,p.getSkills().getLevel(Skills.COOKING));
                boolean burned=Math.random()*100<chance;
                if(!(burned?food.burnt:food.recipe).complete(p)){p.sendMessage("You need more backpack space to continue baking.");return -1;}
                p.faceObject(object);p.setNextAnimation(new Animation(897));remaining--;
                if(burned)p.sendMessage("Oops! You accidentally burn the food.");else{p.addFoodCooked();p.sendMessage("You successfully bake the food.");}
                return 3;
            }
            @Override public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
        });
    }
    static double burnChance(Food food,int level){if(level>=food.stopBurning)return 0;return Math.max(0,Math.min(50,50.0*(food.stopBurning-level)/(food.stopBurning-food.recipe.level)));}
}
