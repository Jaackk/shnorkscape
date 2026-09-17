package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.crafting.LeatherCrafting.LeatherData;
import com.rs.game.player.actions.crafting.Spinning.SpinningItem;
import java.util.*;

/** Ordinary910 Crafting families, with the paired950 cache's current product records. */
public final class Native950Crafting {
    private Native950Crafting(){}
    public static void verifyCacheBindings(){Native950CraftingAssets.verifyAll();if(recipes().size()!=117)throw new IllegalStateException("Unexpected950 Crafting recipe count: "+recipes().size());}
    private static Object store;
    private static List<Native950Production.Recipe> cached;
    // Product families from JewellerySmithing and CraftingRs3Dialogue; no legacy interface ids.
    static final int[] GOLD={1635,1637,1639,1641,1643,1645,6575,1654,1656,1658,1660,1662,1664,6577,1673,1675,1677,1679,1681,1683,6579,11069,11072,11076,11085,11092,11115,11130};
    static final int[] GLASS={1919,4527,4525,229,6667,567,4542,10980,48960,48961};
    static final int[] POTTERY={1787,1789,1791,5352},FIRED={1931,2313,1923,5350};
    public static synchronized List<Native950Production.Recipe> recipes(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();
        if(store==Cache.STORE&&cached!=null)return cached;store=Cache.STORE;
        List<Native950Production.Recipe> list=new ArrayList<>();
        for(LeatherData old:LeatherData.values())if(old.getFinalProduct()!=1169)add(list,"Sew",old.getFinalProduct(),25594,2,new Item[]{new Item(old.getLeatherId(),old.getLeatherAmount())},1733,1734);
        for(SpinningItem old:SpinningItem.values())for(int input:old.getBeforeId()){
            Native950Production.Recipe r=Native950CraftingAssets.product("Spin",old.getAfterId(),Skills.CRAFTING,896,2,new Item[]{new Item(input,1)});
            // Sinew/root alternatives become individually selectable recipes where the cache supports them.
            if(r!=null&&!containsProductAndInput(list,r))list.add(r);
        }
        for(int id:GOLD)add(list,"Jewellery",id,32626,2,new Item[]{new Item(2357,1)});
        // Furnace output is unstrung; blessed symbols in the old enum bypassed two separate operations.
        for(int id:new int[]{1714,1720,5525})add(list,"Silver",id,32626,2,new Item[]{new Item(2355,1)});
        for(int id:new int[]{1692,1694,1696,1698,1700,1702,6581,1716})add(list,"String",id,-1,1,new Item[]{new Item(1759,1)});
        for(int id:new int[]{1393,1395,1397,1399})add(list,"Battlestaff",id,-1,2,new Item[0]);
        for(int id:GLASS)add(list,"Glass",id,884,2,new Item[]{new Item(1775,1)},1785);
        for(int id:POTTERY)add(list,"Pottery",id,896,4,new Item[]{new Item(1761,1)});
        for(int id:FIRED)add(list,"Fire pottery",id,32626,4,new Item[0]);
        add(list,"Melt glass",1775,32626,2,new Item[]{new Item(1783,1)});
        cached=Collections.unmodifiableList(list);return cached;
    }
    private static boolean containsProductAndInput(List<Native950Production.Recipe> list,Native950Production.Recipe r){
        for(Native950Production.Recipe old:list)if(old.label.equals(r.label)&&Arrays.toString(old.consumed()).equals(Arrays.toString(r.consumed())))return true;return false;
    }
    private static void add(List<Native950Production.Recipe> list,String group,int id,int anim,int delay,Item[] preferred,int... tools){
        Native950Production.Recipe r=Native950CraftingAssets.product(group,id,Skills.CRAFTING,anim,delay,preferred,tools);if(r!=null)list.add(r);
    }
    private static boolean inventory(Native950Production.Recipe r){return r.label.startsWith("Sew:")||r.label.startsWith("Glass:")||r.label.startsWith("String:")||r.label.startsWith("Battlestaff:");}
    public static List<Native950Production.Recipe> pair(int first,int second){
        List<Native950Production.Recipe> list=new ArrayList<>();if(first==second)return list;
        for(Native950Production.Recipe r:recipes())if(inventory(r)&&endpoint(r,first)&&endpoint(r,second))list.add(r);return list;
    }
    private static boolean endpoint(Native950Production.Recipe r,int id){for(Item input:r.consumed())if(input.getId()==id)return true;for(int tool:r.tools())if(tool==id)return true;return false;}
    public static List<Native950Production.Recipe> inventoryRecipes(int id,String operation){
        List<Native950Production.Recipe> list=new ArrayList<>();if(!"Craft".equalsIgnoreCase(operation))return list;
        for(Native950Production.Recipe r:recipes())if(inventory(r))for(Item input:r.consumed())if(input.getId()==id){list.add(r);break;}return list;
    }
    public static Native950ItemCatalog.Entry itemEntry(int id){
        boolean found=false;for(Native950Production.Recipe r:recipes())if(Native950CraftingAssets.includes(r,id)){found=true;break;}
        if(!found)return null;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);String[] options=new String[5];
        for(int i=0;d.inventoryOptions!=null&&i<Math.min(5,d.inventoryOptions.length);i++)if(!inventoryRecipes(id,d.inventoryOptions[i]).isEmpty())options[i]=d.inventoryOptions[i];
        return Native950CraftingAssets.entry(id,options);
    }
    public static List<Native950ProductionMenu.Choice> inventoryChoices(Player p,List<Native950Production.Recipe> recipes){
        List<Native950ProductionMenu.Choice> list=new ArrayList<>();for(Native950Production.Recipe r:recipes)if(inventory(r))
            list.add(new Native950ProductionMenu.Choice(label(r),r,n->startInventory(p,r,n)));return list;
    }
    public static boolean startInventory(Player p,Native950Production.Recipe r,int quantity){
        if(p==null||r==null||!recipes().contains(r)||!inventory(r)||quantity<1||quantity>10000)return false;
        return p.getActionManager().setAction(new Native950CraftingAction(r,quantity));
    }
    private static String station(WorldObject o){
        if(o==null||o.getType()<0||o.getType()>22||!Native950CraftingAssets.pin("object",o.getId(),16,8))return "";
        ObjectDefinitions d=o.getDefinitions();if(!d.loaded||d.transforms!=null||d.sizeX<1||d.sizeX>16||d.sizeY<1||d.sizeY>16)return "";
        if("Spinning wheel".equalsIgnoreCase(d.name))return "Spin";
        if("Pottery wheel".equalsIgnoreCase(d.name))return "Pottery";
        if("Pottery oven".equalsIgnoreCase(d.name))return "Fire pottery";
        if("Furnace".equalsIgnoreCase(d.name))return "Furnace";
        return "";
    }
    public static boolean isStation(WorldObject o){return !station(o).isEmpty();}
    public static boolean accepts(WorldObject o,int option){
        String kind=station(o);if(kind.isEmpty()||option<1||option>5)return false;
        String[] ops=o.getDefinitions().options;if(ops==null||option>ops.length)return false;String op=ops[option-1];
        return kind.equals("Spin")?"Spin".equalsIgnoreCase(op):kind.equals("Pottery")?"Form".equalsIgnoreCase(op):kind.equals("Fire pottery")?"Fire".equalsIgnoreCase(op):("Smelt".equalsIgnoreCase(op)||"Use".equalsIgnoreCase(op)||"Craft".equalsIgnoreCase(op));
    }
    public static boolean inReach(Player p,WorldObject o){
        return p!=null&&isStation(o)&&p.getNextWalkDirection()==-1&&!p.hasTeleported()&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);
    }
    public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){
        List<Native950ProductionMenu.Choice> list=new ArrayList<>();if(!inReach(p,o))return list;String kind=station(o);
        for(Native950Production.Recipe r:recipes()){
            boolean matches=kind.equals("Furnace")?(r.label.startsWith("Jewellery:")||r.label.startsWith("Silver:")||r.label.startsWith("Melt glass:")):r.label.startsWith(kind+":");
            if(matches&&Native950CraftingAssets.hasPrimary(p,r))list.add(new Native950ProductionMenu.Choice(label(r),r,n->startStation(p,o,r,n)));
        }return list;
    }
    public static boolean startStation(Player p,WorldObject o,Native950Production.Recipe r,int quantity){
        if(p==null||r==null||quantity<1||quantity>10000||!recipes().contains(r)||!inReach(p,o))return false;
        String kind=station(o);boolean matches=kind.equals("Furnace")?(r.label.startsWith("Jewellery:")||r.label.startsWith("Silver:")||r.label.startsWith("Melt glass:")):r.label.startsWith(kind+":");
        if(!matches)return false;p.setNextFaceWorldTile(o);
        return p.getActionManager().setAction(new Native950ProductionAction(r,quantity){@Override protected boolean environment(Player p){return inReach(p,o);}});
    }
    private static String label(Native950Production.Recipe r){return Native950Production.name(r.produced()[0].getId())+" (level "+r.level+")";}
}
