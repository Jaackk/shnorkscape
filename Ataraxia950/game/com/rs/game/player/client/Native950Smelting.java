package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Current950 furnace levels, XP and backpack materials on the original ActionManager. */
public final class Native950Smelting {
    private static final SmeltingBar[] BARS={SmeltingBar.BRONZE,SmeltingBar.IRON,SmeltingBar.STEEL,
            SmeltingBar.SILVER,SmeltingBar.MITHRIL,SmeltingBar.ADAMANT,SmeltingBar.GOLD,SmeltingBar.RUNE,
            SmeltingBar.ORIKALKUM,SmeltingBar.NECRONIUM,SmeltingBar.BANE,SmeltingBar.ELDER_RUNE};
    private static final Properties PINS=readPins();
    private static Object store;
    private static List<Native950Production.Recipe> cached;
    private static final Map<String,Boolean> checked=new HashMap<>();
    private Native950Smelting() { }

    public static boolean isFurnace(WorldObject object) {
        if(object==null||Cache.STORE==null||!Cache.isFlatReadOnly()||object.getType()<0||object.getType()>22)return false;
        ObjectDefinitions d=object.getDefinitions();
        if(!d.loaded||d.transforms!=null||!"Furnace".equalsIgnoreCase(d.name)||d.sizeX<1||d.sizeY<1||d.sizeX>16||d.sizeY>16)return false;
        if(d.options!=null)for(String option:d.options)if("Smelt".equalsIgnoreCase(option))return true;
        return false;
    }
    public static boolean inReach(Player player,WorldObject furnace) {
        return isFurnace(furnace)&&Native950Mining.current(furnace)&&Native950Mining.inReach(player,furnace);
    }
    public static List<Native950Production.Recipe> recipes(Player player,WorldObject furnace) {
        return player!=null&&player.isNative950()&&inReach(player,furnace)?recipes():Collections.emptyList();
    }
    /** Stable immutable rows so a stale/replaced menu cannot substitute an arbitrary recipe. */
    public static synchronized List<Native950Production.Recipe> recipes() {
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return Collections.emptyList();
        if(store!=Cache.STORE){store=Cache.STORE;cached=null;checked.clear();}
        if(cached!=null)return cached;
        List<Native950Production.Recipe> result=new ArrayList<>();
        if(!pin("sequence",32626,20,7))return Collections.emptyList();
        for(SmeltingBar old:BARS) {
            int id=old.getProducedBar().getId();
            if(!item(id))continue;
            ItemDefinitions product=ItemDefinitions.getItemDefinitions(id);
            int level=product.getCSOpcode(2645,0),xp=product.getCSOpcode(2697,-1);
            if(product.getCSOpcode(2640,0)!=14||product.getCSOpcode(2641,0)!=0||product.getCSOpcode(2696,0)!=14||level<1||level>120||xp<0||product.getCSOpcode(2653,1)!=1)continue;
            List<Item> inputs=new ArrayList<>();boolean valid=true;
            for(int parameter=2675;parameter<=2677;parameter++) {
                int struct=product.getCSOpcode(parameter,-1);if(struct<0)continue;
                if(!pin("struct",struct,22,5)){valid=false;break;}
                RS3GeneralRequirementMap requirements=RS3GeneralRequirementMap.getMap(struct);
                int ingredient=requirements.getIntValue(2656),amount=requirements.getIntValue(2666);
                // These paired records independently repeat backpack identity and quantity.
                if(ingredient<1||amount<1||amount>28||requirements.getIntValue(7763)!=ingredient||!item(ingredient)) {valid=false;break;}
                inputs.add(new Item(ingredient,amount));
            }
            if(!valid||inputs.isEmpty())continue;
            result.add(new Native950Production.Recipe(product.getName(),Skills.SMITHING,level,
                    xp/10.0,32626,3,inputs.toArray(new Item[0]),new Item[]{new Item(id,1)}));
        }
        cached=Collections.unmodifiableList(result);return cached;
    }
    public static boolean start(Player player,final WorldObject furnace,final Native950Production.Recipe recipe,int quantity) {
        if(player==null||quantity<1||quantity>10000||!recipes(player,furnace).contains(recipe))return false;
        player.setNextFaceWorldTile(furnace);
        return player.getActionManager().setAction(new Native950ProductionAction(recipe,quantity) {
            @Override protected boolean environment(Player p) {return Native950Smelting.inReach(p,furnace);}
            @Override public boolean start(Player p){if(!super.start(p))return false;setActionDelay(p,smeltingTicks(recipe.produced()[0].getId(),p.getSkills().getLevel(Skills.SMITHING))-1);return true;}
            @Override public int processWithDelay(Player p) {
                int delay=super.processWithDelay(p);
                // ActionManager waits the returned count and completes on the following tick.
                return delay<0?delay:smeltingTicks(recipe.produced()[0].getId(),p.getSkills().getLevel(Skills.SMITHING))-1;
            }
        });
    }
    static int smeltingTicks(int bar,int level){
        int four,three;
        switch(bar){case 2349:four=2;three=5;break;case 2351:four=14;three=17;break;case 2353:four=21;three=23;break;
            case 2359:four=31;three=37;break;case 2361:four=46;three=48;break;case 2363:four=53;three=56;break;
            case 44838:four=61;three=65;break;case 44840:four=75;three=77;break;case 44842:four=86;three=89;break;
            case 44844:four=91;three=94;break;default:return 5;}
        return level>=three?3:level>=four?4:5;
    }
    private static boolean item(int id) {
        if(!pin("item",id,19,8))return false;
        ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
        return d.loaded&&d.getName()!=null&&!"null".equalsIgnoreCase(d.getName())&&!d.isStackable()
                &&d.certTemplateId<0&&d.lendTemplateId<0&&d.bindTemplateId<0&&d.shardTemplateId<0;
    }
    private static boolean pin(String kind,int id,int index,int shift) {
        String key=kind+"."+id;
        if(!checked.containsKey(key)) {
            String expected=PINS.getProperty(key);boolean match=false;
            if(expected!=null)try {
                byte[] bytes=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
                if(bytes!=null){StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",b&255));match=expected.equals(hash.toString());}
            }catch(Exception invalid){match=false;}
            checked.put(key,match);
        }
        return checked.get(key);
    }
    private static Properties readPins() {
        Properties result=new Properties();
        try(InputStream in=Native950Smelting.class.getResourceAsStream("/native950/smelting-assets-950.properties")) {
            if(in==null)throw new IllegalStateException("Missing paired950 smelting bindings");result.load(in);return result;
        }catch(java.io.IOException invalid){throw new IllegalStateException(invalid);}
    }
}
