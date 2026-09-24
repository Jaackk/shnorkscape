package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.data.parsers.npcs.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Isolated actual-cache editor lifecycle probe; no socket, live player or deployment. */
public final class Native950DeveloperWorldAcceptance {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        Path dir=Files.createTempDirectory("dev-world-acceptance-");System.setProperty("ataraxia950.worldEditsFile",dir.resolve("edits.tsv").toString());
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get("cache"));Native950World world=Native950World.getInstance();
        try{
            world.execute(()->{
                NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();EmbeddedChannel c=new EmbeddedChannel();
                Player p=Player.createNative950("editor-probe",new WorldTile(3217,3258,0),c);p.setActive(true);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
                try{
                    Native950DeveloperCatalogue.Entry entry=Native950DeveloperCatalogue.search("NPC","12353").get(0);
                    Native950DeveloperPlacement.Request r=new Native950DeveloperPlacement.Request(entry,1,1,-1,0,false,p,0);
                    WorldTile tile=null;for(int dx=2;dx<15&&tile==null;dx++)for(int dy=2;dy<15;dy++){WorldTile t=new WorldTile(p.getX()+dx,p.getY()+dy,0);if(Native950DiagnosticSpawns.spawnTileAvailable(p,t,entry.width)&&World.canMoveNPC(t,entry.width)){tile=t;break;}}
                    check(tile!=null,"No free NPC tile");
                    List<Object> actors=Native950DeveloperPlacement.commit(r,tile.getX(),tile.getY(),Native950DeveloperPlacement.world(p));
                    List<Native950DeveloperWorldEdits.Edit> edits=Native950DeveloperWorldEdits.record(p,r,actors);
                    NPC npc=(NPC)actors.get(0);check(World.containsNPC(npc)&&Native950DiagnosticSpawns.ownedBy(p,npc),"NPC not registered/owned");
                    Native950DeveloperWorldEdits.History history=new Native950DeveloperWorldEdits.History();history.created(edits);history.undo(p);check(!World.containsNPC(npc),"Undo retained NPC");history.redo(p);check(Native950DeveloperWorldEdits.alive(edits.get(0)),"Redo did not restore NPC");
                    Native950DeveloperWorldEdits.cleanup(p);check(Native950DeveloperWorldEdits.owned(p.getUsername()).isEmpty(),"Logout cleanup failed");
                    entry=Native950DeveloperCatalogue.search("Object","70755").get(0);
                    r=new Native950DeveloperPlacement.Request(entry,2,1,10,0,false,p,0);
                    actors=Native950DeveloperPlacement.commit(r,p.getX(),p.getY(),Native950DeveloperPlacement.world(p));
                    Native950DeveloperWorldEdits.Edit object=Native950DeveloperWorldEdits.record(p,r,actors).get(0);
                    object=Native950DeveloperWorldEdits.rotate(p,object);check(((WorldObject)object.actor).getRotation()==1,"Rotation not published to world");
                    Native950DeveloperWorldEdits.save(p,object);Native950DeveloperWorldEdits.cleanup(p);check(Native950DeveloperWorldEdits.alive(object),"Saved object removed by logout");
                    check(Native950DeveloperWorldEdits.read(dir.resolve("edits.tsv")).size()==1,"Saved object not persisted");
                    Native950DeveloperWorldEdits.delete(p,object);check(!Native950DeveloperWorldEdits.alive(object)&&Native950DeveloperWorldEdits.read(dir.resolve("edits.tsv")).isEmpty(),"Delete did not remove scene and persistent record");
                    // Command placement joins the same ledger; clearing never touches another owner.
                    String reply=Native950DiagnosticSpawns.spawnObject(p,70755,10,0);
                    check(Native950DeveloperWorldEdits.owned(p.getUsername()).size()==1,"Command object not tracked: "+reply);
                    Native950DeveloperWorldEdits.Edit mine=Native950DeveloperWorldEdits.owned(p.getUsername()).get(0);
                    Native950DeveloperWorldEdits.save(p,mine);
                    Player other=Player.createNative950("editor-other",new WorldTile(p.getX()+2,p.getY()+2,0),c);
                    WorldObject theirs=null;
                    for(int dx=2;dx<15&&theirs==null;dx++)try{theirs=Native950DiagnosticSpawns.placeObject(70755,10,0,new WorldTile(p.getX()+dx,p.getY(),0));}catch(IllegalArgumentException occupied){}
                    check(theirs!=null,"No other-owner object tile");
                    Native950DeveloperWorldEdits.Edit theirEdit=Native950DeveloperWorldEdits.recordObject(other,theirs);
                    try{
                        check(Native950DeveloperWorldEdits.clearObjects(p,16)==1,"Owned clear count");
                        check(!Native950DeveloperWorldEdits.alive(mine),"Owned scene object survived clear");
                        check(Native950DeveloperWorldEdits.alive(theirEdit),"Other player's object removed");
                        check(Native950DeveloperWorldEdits.read(dir.resolve("edits.tsv")).isEmpty(),"Saved object would return after restart");
                    }finally{Native950DeveloperWorldEdits.cleanup(other);}
                    Native950DiagnosticSpawns.spawnObject(p,70755,10,0);
                    Native950DeveloperWorldEdits.Edit stale=Native950DeveloperWorldEdits.owned(p.getUsername()).get(0);
                    World.removeObject((WorldObject)stale.actor);
                    WorldObject replacement=Native950DiagnosticSpawns.placeObject(70755,10,0,p);
                    try{
                        check(Native950DeveloperWorldEdits.clearObjects(p,0)==1,"Stale placement record not removed");
                        check(World.getObjectWithSlot(p,Region.OBJECT_SLOTS[10])==replacement,"Stale ownership removed a replacement object");
                    }finally{World.removeObject(replacement);}
                    for(Native950GamevalLookup.Entry symbol:Native950GamevalLookup.search(""))check(symbol.verify().contains("SHA-256 MATCH"),"Symbol pin failed: "+symbol.id+" "+symbol.verify());

                }finally{Native950DeveloperWorldEdits.cleanup(p);World.removeNative950Player(p);c.finishAndReleaseAll();world.clearNativeNpcs();}
                return null;
            }).get(120,TimeUnit.SECONDS);
            System.out.println("PASS: actual-cache chosen-tile NPC/object mutations, ownership, undo/redo, rotation, explicit save, owned object clearing, stale replacement protection, all 1385 symbol pins and persistent delete. Vulkan pending.");
        }finally{Files.deleteIfExists(dir.resolve("edits.tsv"));Files.deleteIfExists(dir);}
    }
}
