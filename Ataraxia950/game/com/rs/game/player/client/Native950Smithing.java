package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.ItemDefinitions;import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldObject;import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.defs.ForgingBar;import java.util.*;
/** Anvil forging for the original metal families, using current950 canonical recipes. */
public final class Native950Smithing {
    private Native950Smithing(){}private static Object store;private static List<Native950Production.Recipe> recipes;
    private static final Map<Native950Production.Recipe,Native950SmithingAssets.Row> rows=new IdentityHashMap<>();
    public static void verifyCacheBindings(){Native950SmithingAssets.verifyAll();if(recipes().size()!=702)throw new IllegalStateException("Wrong950 Smithing recipe count");}
    public static synchronized List<Native950Production.Recipe> recipes(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();if(store==Cache.STORE&&recipes!=null)return recipes;
        rows.clear();List<Native950Production.Recipe> list=new ArrayList<>();
        for(Native950SmithingAssets.Row r:Native950SmithingAssets.rows()){
            ForgingBar bar=ForgingBar.forId(r.bar);if(bar==null||bar.getLevel()!=r.tier)throw new IllegalStateException("Unknown original metal family "+r.bar);
            Item[] inputs=new Item[r.inputs.length];for(int i=0;i<inputs.length;i++)inputs[i]=new Item(r.inputs[i].id,r.inputs[i].amount);
            Native950Production.Recipe recipe=new Native950Production.Recipe("Smith: "+r.name,Skills.SMITHING,r.level,r.xp,22143,2,inputs,new Item[]{new Item(r.id,r.quantity)},2347);
            rows.put(recipe,r);list.add(recipe);
        }store=Cache.STORE;recipes=Collections.unmodifiableList(list);return recipes;
    }
    public static boolean isStation(WorldObject o){
        if(o==null||o.getType()<0||o.getType()>22||!Native950SmithingAssets.pin("object."+o.getId()))return false;
        ObjectDefinitions d=o.getDefinitions();if(!d.loaded||!"Anvil".equalsIgnoreCase(d.name)||d.transforms!=null||d.sizeX<1||d.sizeX>16||d.sizeY<1||d.sizeY>16)return false;
        if(d.options!=null)for(String op:d.options)if("Smith".equalsIgnoreCase(op))return true;return false;
    }
    public static boolean accepts(WorldObject o,int option){if(!isStation(o)||option<1||option>5)return false;String[] ops=o.getDefinitions().options;return ops!=null&&option<=ops.length&&"Smith".equalsIgnoreCase(ops[option-1]);}
    public static boolean isInput(int id){
        if(id==2347)return Native950SmithingAssets.item(id);
        for(Native950Production.Recipe recipe:recipes())for(Item input:recipe.consumed())if(input.getId()==id)return true;
        return false;
    }
    public static boolean inReach(Player p,WorldObject o){return p!=null&&p.isNative950()&&isStation(o)&&p.getNextWalkDirection()==-1&&!p.hasWalkSteps()&&!p.hasTeleported()&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);}
    public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){
        List<Native950ProductionMenu.Choice> list=new ArrayList<>();if(!inReach(p,o))return list;
        for(Native950Production.Recipe r:recipes()){
            Native950SmithingAssets.Row row=rows.get(r);
            // Native forge menus show locked and unaffordable products with their requirements.
            // The original action validates level, materials, toolbelt and reach again on Start.
            list.add(new Native950ProductionMenu.Choice(row.name+" (level "+r.level+")",r,n->startStation(p,o,r,n)));
        }return list;
    }
    public static boolean startStation(Player p,WorldObject o,Native950Production.Recipe r,int quantity){
        if(p==null||r==null||quantity<1||quantity>10000||!recipes().contains(r)||!inReach(p,o))return false;
        Native950SmithingAssets.Row row=rows.get(r);int bars=0;for(Item i:r.consumed())if(i.getId()==row.bar)bars+=i.getAmount();
        p.setNextFaceWorldTile(o);final WorldObject captured=new WorldObject(o);
        return p.getActionManager().setAction(new Native950SmithingAction(r,quantity,row.progress,row.tier,bars,row.upgrade){@Override protected boolean environment(Player player){return Native950Smithing.inReach(player,captured);}});
    }
    public static Native950ItemCatalog.Entry itemEntry(int id){
        if(!Native950SmithingAssets.item(id))return null;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return new Native950ItemCatalog.Entry(id,d.name,d.isStackable(),new String[5]);
    }
}
