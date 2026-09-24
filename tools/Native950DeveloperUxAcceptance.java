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
public final class Native950DeveloperUxAcceptance {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        Path dir=Files.createTempDirectory("dev-world-acceptance-");System.setProperty("ataraxia950.worldEditsFile",dir.resolve("edits.tsv").toString());
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args.length==0?"cache":args[0]));
        java.lang.reflect.Method verify=Native950DeveloperConsole.class.getDeclaredMethod("verify");verify.setAccessible(true);verify.invoke(null);Native950World world=Native950World.getInstance();
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
                    // Command NPCs previously never entered the editor. Adopt once, preserve repeat policy, delete the actual actor.
                    Native950DiagnosticSpawns.spawnNpcs(p,12353,2,true);
                    List<NPC> spawned=Native950DiagnosticSpawns.ownedNpcs(p);check(spawned.size()==2,"Command NPC setup");
                    Native950DeveloperWorldEdits.adopt(p);Native950DeveloperWorldEdits.adopt(p);
                    check(Native950DeveloperWorldEdits.owned(p.getUsername()).size()==2,"Adoption duplicated command actors");
                    check(Native950DeveloperWorldEdits.owned(p.getUsername()).stream().allMatch(e->e.repeat),"Repeat policy was lost");
                    Native950DeveloperWorldEdits.Edit removed=Native950DeveloperWorldEdits.owned(p.getUsername()).get(0);NPC removedActor=(NPC)removed.actor;
                    Native950DeveloperWorldEdits.delete(p,removed);check(!World.containsNPC(removedActor),"Editor Delete retained command NPC");
                    check(Native950DiagnosticSpawns.ownedNpcs(p).size()==1,"Delete removed wrong owned actor");
                    other.setActive(true);World.addNative950Player(other,2);World.updateEntityRegion(other);other.loadMapRegions();other.setClientHasLoadedMapRegion();
                    try{
                        Native950DiagnosticSpawns.spawnNpcs(other,12353,1,false);List<NPC> otherActors=Native950DiagnosticSpawns.ownedNpcs(other);check(otherActors.size()==1,"Second player setup");
                        check(Native950DeveloperWorldEdits.clearOwned(p,"NPC",false)==1,"Clear My NPCs count");
                        check(World.containsNPC(otherActors.get(0)),"Clear My NPCs removed another player's actor");
                        Native950DeveloperWorldEdits.adopt(other);check(Native950DeveloperWorldEdits.owned(other.getUsername()).size()==1,"Other ledger lost actor");
                    }finally{Native950DeveloperWorldEdits.clearOwned(other,"",true);World.removeNative950Player(other);}
                    // Boss test actors belong to the same ledger, including their bodyguards.
                    boolean encounter=false;
                    for(int ax=3200;ax<3260&&!encounter;ax+=10)for(int ay=3260;ay<3310;ay+=10){
                        p.setNextWorldTile(null);p.setLocation(new WorldTile(ax,ay,0));World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
                        if(Native950BossCatalogue.spawn(p,6260).startsWith("Created")){encounter=true;break;}
                    }
                    check(encounter,"No boss test arena available");Native950DeveloperWorldEdits.adopt(p);Native950DeveloperWorldEdits.adopt(p);
                    check(Native950DeveloperWorldEdits.owned(p.getUsername()).size()==4,"Encounter/bodyguards not adopted exactly once");
                    check(Native950DeveloperWorldEdits.owned(p.getUsername()).stream().allMatch(e->!Native950DeveloperWorldEdits.encounter(e).isEmpty()),"Encounter labels lost");
                    List<NPC> encounterActors=Native950DiagnosticSpawns.ownedNpcs(p);check(Native950DeveloperWorldEdits.clearOwned(p,"",true)==4,"Temporary encounter clear count");
                    for(NPC actor:encounterActors)check(!World.containsNPC(actor),"Encounter actor survived editor clear");
                    check(Native950BossCatalogue.spawn(p,6260).startsWith("Created"),"Editor clear left boss ownership locked");Native950DeveloperWorldEdits.clearOwned(p,"NPC",false);
                    long start=System.nanoTime();List<Native950DeveloperCatalogue.Entry> first=Native950DeveloperCatalogue.search("NPC","vorago");long cold=System.nanoTime()-start;
                    start=System.nanoTime();for(int n=0;n<1000;n++)check(first==Native950DeveloperCatalogue.search("NPC","VoRaGo"),"Read-only result reuse");long warm=System.nanoTime()-start;
                    System.out.println("SEARCH PROFILE: Vorago "+first.size()+" rows, first lookup "+cold/1000+" us, 1000 cached lookups "+warm/1000+" us");
                    for(Native950GamevalLookup.Entry symbol:Native950GamevalLookup.search(""))check(symbol.verify().contains("SHA-256 MATCH"),"Symbol pin failed: "+symbol.id+" "+symbol.verify());

                }finally{Native950DeveloperWorldEdits.cleanup(p);World.removeNative950Player(p);c.finishAndReleaseAll();world.clearNativeNpcs();}
                return null;
            }).get(120,TimeUnit.SECONDS);
            System.out.println("PASS: actual-cache chosen-tile NPC/object mutations, ownership, undo/redo, rotation, explicit save, owned object clearing, stale replacement protection, all audited symbol pins; command/repeating NPC adoption, actual editor deletion and two-player clearing and persistent delete. Vulkan pending.");
        }finally{Files.deleteIfExists(dir.resolve("edits.tsv"));Files.deleteIfExists(dir);}
    }
}
