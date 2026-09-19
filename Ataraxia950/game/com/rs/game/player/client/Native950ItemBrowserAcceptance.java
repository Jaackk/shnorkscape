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
        require(Native950ItemBrowser.pageCount(95)==3&&Native950ItemBrowser.page(Native950ContentCommands.itemMatches("torva",Integer.MAX_VALUE),0,40).size()<=40,
                "Pagination contract changed");
        require(!Native950ContentCommands.featuredItemBrowserEntries().isEmpty(),"Featured browser view is empty");
        System.out.println("PASS Item Browser V2: paired cache contains the native grid/input/quantity surfaces; ranked name/ID search and pagination are valid.");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
