package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.nio.file.Paths;

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
        require(Native950ContentCommands.itemMatches("blood",Native950ItemBrowser.nativeCapacityForTests()).size()>40,
                "Blood search still has the retired 40-result ceiling");
        require(Native950ContentCommands.itemMatches("soul",Native950ItemBrowser.nativeCapacityForTests()).size()>40,
                "Soul search still has the retired 40-result ceiling");
        require(Native950ContentCommands.testingKitItemBrowserEntries().size()==19,
                "Testing Kit lost a cache-derived Combat Alpha item");
        for(Native950ContentCommands.ItemSearchEntry entry:Native950ContentCommands.testingKitItemBrowserEntries())
            require(Cache.STORE.getIndexes()[19].getFile(entry.id>>>8,entry.id&255)!=null,
                    "Testing Kit item is absent from paired cache: "+entry.id);
        System.out.println("PASS Item Browser: native grid/input/option surfaces exist; broad search exceeds 40 and the 19-item Combat Alpha kit is cache-backed.");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
