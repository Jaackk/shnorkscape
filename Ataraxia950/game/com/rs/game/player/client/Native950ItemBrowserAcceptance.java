package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Read-only paired-cache acceptance for the browser's native surfaces and search index. */
public final class Native950ItemBrowserAcceptance {
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Pass the flat revision-950 cache path");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950ItemBrowser.verifyCacheBindings();
        require(!Native950ContentCommands.itemMatches("torva",60).isEmpty(),"Name search is empty");
        require(Native950ContentCommands.itemMatches("20135",60).stream().anyMatch(e->e.id==20135),"ID search missed Torva helm");
        require(Native950ContentCommands.itemMatches("torva",3).size()<=3,"Search limit was ignored");
        require(Native950ContentCommands.itemMatches("torva full helm",60).get(0).id==20135,"Useful base item was not ranked first");
        require(Native950ContentCommands.itemMatches("torva",Integer.MAX_VALUE).size()>5,
                "Broad search was silently truncated");
        require(Native950ItemBrowser.nativeCapacityForTests()==40,"Native browser visual capacity changed");
        require(Native950ContentCommands.itemMatches("blood",Native950ItemBrowser.nativeCapacityForTests()).size()==40,
                "Blood search does not fill the safe visual result set");
        require(Native950ContentCommands.itemMatches("soul",Native950ItemBrowser.nativeCapacityForTests()).size()==40,
                "Soul search does not fill the safe visual result set");
        List<Native950ContentCommands.ItemSearchEntry> kit=Native950ContentCommands.testingKitItemBrowserEntries();
        require(kit.size()==40,
                "Testing Kit lost a cache-derived Combat Alpha item");
        Set<Integer> unique=new HashSet<Integer>();
        for(Native950ContentCommands.ItemSearchEntry entry:kit) {
            require(Cache.STORE.getIndexes()[19].getFile(entry.id>>>8,entry.id&255)!=null,
                    "Testing Kit item is absent from paired cache: "+entry.id);
            ItemDefinitions definition=Native950CacheItems.definition(entry.id);
            require(definition!=null&&definition.loaded&&definition.decodeFailure==null&&!definition.noted,
                    "Testing Kit item does not resolve to a safe base definition: "+entry.id);
            require(entry.name.equals(definition.getName())&&Native950CacheItems.entry(entry.id)!=null,
                    "Testing Kit search identity differs from the paired cache: "+entry.id);
            Item spawned=new Item(entry.id,1);
            require(spawned.getCharges()==0&&spawned.getAttributes()==null&&spawned.getInventionData()==null,
                    "Testing Kit item requires unsupported spawn metadata: "+entry.id);
            require(unique.add(entry.id),"Testing Kit repeats item "+entry.id);
        }
        for(int id:new int[]{52533,52081,51848,42574,55145,55109})
            require(supportedCombatProfile(id),
                    "Testing Kit weapon is not recognized by Combat Alpha: "+id);
        require(Native950CombatStyles.classify(Native950CacheItems.definition(52083))==Native950CombatStyles.MELEE,
                "Testing Kit melee off-hand is not recognized by Combat Alpha");
        require(Native950CombatStyles.classify(Native950CacheItems.definition(42582))==Native950CombatStyles.MAGIC,
                "Testing Kit magic off-hand is not recognized by Combat Alpha");
        require(Native950CombatStyles.classify(Native950CacheItems.definition(55114))==Native950CombatStyles.RANGED,
                "Testing Kit ranged off-hand is not recognized by Combat Alpha");
        for(int id:new int[]{36294,38240,38242,52028,51090,51470,
                52083,42991,43119,43121,52036,51092,51467,42582,
                55045,55051,55056,52032,51088,9244,50465,55114})
            require(Native950EquipmentTypes.resolve(id)!=null,
                    "Testing Kit equipment is not recognized by the paired cache: "+id);
        System.out.println("PASS Item Browser: native grid/input surfaces exist; broad searches use the safe 40-result visual cap and all 40 Combat Alpha kit entries are cache-backed, safely spawnable and combat-compatible.");
    }
    private static boolean supportedCombatProfile(int id) {
        try{return Native950CombatStyles.profile(id)!=null;}
        catch(IllegalArgumentException unsupported){return false;}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
