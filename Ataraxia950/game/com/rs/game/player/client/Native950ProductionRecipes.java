package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.item.Item;
import com.rs.game.player.Skills;
import java.util.*;

/** Additional ordinary recipe chains; the paired cache supplies level, materials and base XP. */
final class Native950ProductionRecipes {
    private Native950ProductionRecipes(){}
    private static Object store;
    private static List<Native950Production.Recipe> cached;
    static synchronized List<Native950Production.Recipe> appendTo(List<Native950Production.Recipe> base){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return base;
        if(store==Cache.STORE&&cached!=null)return cached;
        List<Native950Production.Recipe> rows=new ArrayList<>(base);
        for(int id:new int[]{10142,10143,10144,10145})add(rows,"Mix tar",id,Skills.HERBLORE,363,new Item[0],233);
        for(int id:new int[]{45,9187,46,9188,9189,9190,9191,9192,9193,9194,31867})add(rows,"Cut bolt tips",id,Skills.FLETCHING,886,new Item[0],1755);
        // The old BoltTips family also accepts the four-pearl oyster item. Its quantity differs,
        // while the paired pearl-tip output carries the same current per-cut XP and level.
        Native950Production.Recipe pearl=product("Cut bolt tips",46,Skills.FLETCHING,886,new Item[0],1755);
        if(pearl!=null&&Native950CraftingAssets.item(413))rows.add(new Native950Production.Recipe("Cut bolt tips: oyster pearls",Skills.FLETCHING,pearl.level,pearl.xp,886,2,new Item[]{new Item(413,1)},new Item[]{new Item(46,24)},1755));
        // Each water-container variant returns that exact empty vessel, plus the empty flour pot.
        for(int id:new int[]{2307,1953,2283})for(int water:new int[]{1937,1929,1921}){
            Native950Production.Recipe r=product("Prepare",id,Skills.COOKING,-1,new Item[]{new Item(water,1)});
            if(r!=null)addReturns(rows,r,1931,water==1937?1935:water==1929?1925:1923);
        }
        for(int id:new int[]{2315,2321,2317,2285,2287})add(rows,"Prepare",id,Skills.COOKING,-1,new Item[0]);
        for(int meat:new int[]{2142,2140})add(rows,"Prepare",2319,Skills.COOKING,-1,new Item[]{new Item(meat,1)});
        Native950Production.Recipe cake=product("Prepare",1889,Skills.COOKING,-1,new Item[0]);
        if(cake!=null)addReturns(rows,cake,1931,1925);
        store=Cache.STORE;cached=Collections.unmodifiableList(rows);return cached;
    }
    private static Native950Production.Recipe product(String group,int id,int skill,int animation,Item[] preferred,int... tools){
        return Native950CraftingAssets.product(group,id,skill,animation,2,preferred,tools);
    }
    private static void add(List<Native950Production.Recipe> rows,String group,int id,int skill,int animation,Item[] preferred,int... tools){
        Native950Production.Recipe r=product(group,id,skill,animation,preferred,tools);if(r!=null)rows.add(r);
    }
    private static void addReturns(List<Native950Production.Recipe> rows,Native950Production.Recipe r,int... returns){
        List<Item> output=new ArrayList<>(Arrays.asList(r.produced()));
        for(int id:returns){if(!Native950CraftingAssets.item(id))return;output.add(new Item(id,1));}
        rows.add(new Native950Production.Recipe(r.label,r.skill,r.level,r.xp,r.animation,r.delay,r.consumed(),output.toArray(new Item[0]),r.tools()));
    }
}
