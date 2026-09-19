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
        System.out.println("PASS Item Browser: current cache contains text input, shop icon grid and quantity surfaces; name/ID search is indexed.");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
