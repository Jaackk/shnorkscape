package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Native-world-thread ledger. Only explicitly saved records survive restart. */
final class Native950DeveloperWorldEdits {
    static final class Edit {
        final String key,owner,kind;final int id,x,y,plane,type,rotation;final boolean repeat;
        boolean saved;Object actor;
        Edit(String key,String owner,String kind,int id,int x,int y,int plane,int type,int rotation,boolean repeat,boolean saved){
            if(key==null||!key.matches("[a-f0-9-]{36}")||owner==null||!owner.matches("[a-z0-9 _-]{1,32}")
                ||!Arrays.asList("NPC","Object").contains(kind)||id<0||id>200000||!Native950SavedLocations.validTile(x,y,plane)
                ||rotation<0||rotation>3||kind.equals("Object")&&(type<0||type>22))throw new IllegalArgumentException("Invalid world edit");
            this.key=key;this.owner=owner;this.kind=kind;this.id=id;this.x=x;this.y=y;this.plane=plane;this.type=type;this.rotation=rotation;this.repeat=repeat;this.saved=saved;
        }
        WorldTile tile(){return new WorldTile(x,y,plane);}
        String line(){return String.join("\t",key,owner,kind,""+id,""+x,""+y,""+plane,""+type,""+rotation,repeat?"1":"0");}
    }
    private static final Map<String,Edit> EDITS=new LinkedHashMap<>();
    private static boolean restored;
    private static boolean persistenceHealthy=true;
    private static Path file(){return Paths.get(System.getProperty("ataraxia950.worldEditsFile","server-home/developer-world-edits-950.tsv")).toAbsolutePath().normalize();}
    static List<Edit> read(Path path)throws IOException{
        List<Edit> result=new ArrayList<>();if(!Files.exists(path))return result;
        if(!Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)||Files.size(path)>65536)throw new IOException("Invalid world edit file");
        List<String> rows=Files.readAllLines(path,StandardCharsets.UTF_8);
        if(rows.isEmpty()||!rows.get(0).equals("SHNORKSCAPE-WORLD-EDITS-950-1")||rows.size()>257)throw new IOException("Invalid world edit schema/count");
        Set<String> keys=new HashSet<>();
        try{for(String row:rows.subList(1,rows.size())){
            String[] f=row.split("\t",-1);if(f.length!=10||!keys.add(f[0])||!f[9].matches("[01]"))throw new IllegalArgumentException();
            result.add(new Edit(f[0],f[1],f[2],Integer.parseInt(f[3]),Integer.parseInt(f[4]),Integer.parseInt(f[5]),Integer.parseInt(f[6]),Integer.parseInt(f[7]),Integer.parseInt(f[8]),f[9].equals("1"),true));
        }}catch(IllegalArgumentException invalid){throw new IOException("Malformed world edit",invalid);}return result;
    }
    static void write(Path path,Collection<Edit> edits)throws IOException{
        List<String> lines=new ArrayList<>();lines.add("SHNORKSCAPE-WORLD-EDITS-950-1");
        for(Edit e:edits)if(e.saved)lines.add(e.line());if(lines.size()>257)throw new IOException("At most 256 persistent placements are supported");
        byte[] bytes=(String.join("\n",lines)+"\n").getBytes(StandardCharsets.UTF_8);if(bytes.length>65536)throw new IOException("World edit file is full");
        Files.createDirectories(path.toAbsolutePath().getParent());Path tmp=Files.createTempFile(path.toAbsolutePath().getParent(),".world-edit-",".tmp");
        try{Files.write(tmp,bytes);Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}
    }
    /** Called after native content bootstrap, on the world's first initialized tick. */
    static void restore(){
        if(restored||!Cache.isFlatReadOnly()||Native950Bootstrap.lastReport()==null)return;restored=true;
        try{
            List<Edit> saved=read(file());
            for(Edit e:saved){EDITS.put(e.key,e);try{spawn(e);}catch(RuntimeException rejected){System.err.println("[DeveloperWorld] Saved placement "+e.key+" was not restored: "+rejected.getMessage());}}
        }catch(IOException invalid){persistenceHealthy=false;System.err.println("[DeveloperWorld] Persistence unavailable: "+invalid.getMessage());}
    }
    static List<Edit> owned(String owner){List<Edit> result=new ArrayList<>();for(Edit e:EDITS.values())if(e.owner.equals(owner))result.add(e);return result;}
    static List<Edit> record(Player owner,Native950DeveloperPlacement.Request request,List<Object> actors){
        List<Edit> result=new ArrayList<>();for(Object actor:actors){WorldTile tile=(WorldTile)actor;
            Edit e=new Edit(UUID.randomUUID().toString(),owner.getUsername(),request.entry.kind,request.entry.id,tile.getX(),tile.getY(),tile.getPlane(),request.type,request.rotation,request.repeat,false);
            e.actor=actor;EDITS.put(e.key,e);result.add(e);
        }return result;
    }
    /** Command and console placements share the same ownership ledger. */
    static Edit recordObject(Player owner,WorldObject actor){
        if(owned(owner.getUsername()).size()>=200)throw new IllegalArgumentException("Remove placements first (200 per account).");
        Edit e=new Edit(UUID.randomUUID().toString(),owner.getUsername(),"Object",actor.getId(),actor.getX(),actor.getY(),actor.getPlane(),actor.getType(),actor.getRotation(),false,false);
        e.actor=actor;EDITS.put(e.key,e);return e;
    }
    static int clearObjects(Player p,int radius)throws IOException{
        return clearObjects(p.getUsername(),p,radius,file(),Native950DeveloperWorldEdits::removeActor);
    }
    /** Persist the whole deletion before touching the world; failed writes remove nothing. */
    static int clearObjects(String owner,WorldTile origin,int radius,Path path,java.util.function.Consumer<Edit> remove)throws IOException{
        if(radius<0||radius>128)throw new IllegalArgumentException("Radius must be 0-128.");
        List<Edit> selected=new ArrayList<>();
        for(Edit e:owned(owner))if(e.kind.equals("Object")&&e.plane==origin.getPlane()
            &&Math.max(Math.abs(e.x-origin.getX()),Math.abs(e.y-origin.getY()))<=radius)selected.add(e);
        if(selected.isEmpty())return 0;
        if(selected.stream().anyMatch(e->e.saved)){
            if(!persistenceHealthy)throw new IOException("World-edit persistence is unavailable; nothing removed.");
            List<Edit> remaining=new ArrayList<>(EDITS.values());remaining.removeAll(selected);write(path,remaining);
        }
        for(Edit e:selected){remove.accept(e);EDITS.remove(e.key);}
        return selected.size();
    }
    private static void requireOwner(Player p,Edit edit){if(edit==null||EDITS.get(edit.key)!=edit||!edit.owner.equals(p.getUsername()))throw new IllegalArgumentException("That placement is not owned by you.");}
    static void save(Player p,Edit e)throws IOException{
        requireOwner(p,e);if(!persistenceHealthy)throw new IOException("The existing world-edit file needs repair; it will not be overwritten.");if(e.saved)return;if(!alive(e))throw new IllegalArgumentException("That placement no longer exists. Place it again before saving.");
        e.saved=true;try{write(file(),EDITS.values());}catch(IOException failure){e.saved=false;throw failure;}
    }
    static void delete(Player p,Edit e)throws IOException{
        requireOwner(p,e);
        if(e.saved){if(!persistenceHealthy)throw new IOException("World-edit persistence is unavailable.");List<Edit> remaining=new ArrayList<>(EDITS.values());remaining.remove(e);write(file(),remaining);}
        removeActor(e);EDITS.remove(e.key);
    }
    static void cleanup(Player p){for(Edit e:new ArrayList<>(owned(p.getUsername())))if(!e.saved){removeActor(e);EDITS.remove(e.key);}}
    static Edit rotate(Player p,Edit e){
        requireOwner(p,e);if(e.saved||!e.kind.equals("Object")||!alive(e))throw new IllegalArgumentException("Rotate a live temporary object. Duplicate saved objects before editing.");
        Edit next=new Edit(e.key,e.owner,e.kind,e.id,e.x,e.y,e.plane,e.type,(e.rotation+1)%4,e.repeat,false);
        removeActor(e);
        try{spawn(next);}catch(RuntimeException failure){try{spawn(e);}catch(RuntimeException rollback){failure.addSuppressed(rollback);}throw failure;}
        EDITS.put(next.key,next);return next;
    }
    static boolean alive(Edit e){
        if(e.actor instanceof NPC)return World.containsNPC((NPC)e.actor)&&!((NPC)e.actor).hasFinished();
        if(e.actor instanceof WorldObject){WorldObject o=(WorldObject)e.actor;return World.getObjectWithSlot(o,Region.OBJECT_SLOTS[o.getType()])==o;}return false;
    }
    private static void removeActor(Edit e){
        if(alive(e)){if(e.actor instanceof NPC)Native950World.getInstance().removeDiagnosticNpc((NPC)e.actor);else World.removeObject((WorldObject)e.actor);}e.actor=null;
    }
    private static void spawn(Edit e){
        if(e.kind.equals("NPC")){
            NPC npc=NPC.createNative950Diagnostic(e.id,e.tile());
            if(!Native950DiagnosticSpawns.spawnTileAvailable(null,e.tile(),npc.getSize())||!World.canMoveNPC(e.tile(),npc.getSize()))throw new IllegalArgumentException("Saved NPC footprint is occupied or blocked");
            Native950DiagnosticSpawns.registerPlacedNpc(npc,e.owner,e.repeat);e.actor=npc;
        }else e.actor=Native950DiagnosticSpawns.placeObject(e.id,e.type,e.rotation,e.tile());
    }
    /** Temporary-only undo/redo. Saved world changes always require explicit actions. */
    static final class History {
        private final Deque<List<Edit>> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
        void created(List<Edit> edits){if(edits.isEmpty())return;undo.addLast(new ArrayList<>(edits));while(undo.size()>32)undo.removeFirst();redo.clear();}
        void clear(){undo.clear();redo.clear();}
        boolean canUndo(){return !undo.isEmpty();}boolean canRedo(){return !redo.isEmpty();}
        void undo(Player p){
            if(undo.isEmpty())return;List<Edit> batch=undo.peekLast();
            for(Edit e:batch){requireOwner(p,e);if(e.saved)throw new IllegalArgumentException("Saved placements require explicit deletion.");}
            for(Edit e:batch){removeActor(e);EDITS.remove(e.key);}undo.removeLast();redo.addLast(batch);
        }
        void redo(Player p){
            if(redo.isEmpty())return;List<Edit> batch=redo.peekLast(),created=new ArrayList<>();
            try{for(Edit e:batch){if(!e.owner.equals(p.getUsername())||EDITS.containsKey(e.key))throw new IllegalArgumentException("Placement history is stale");spawn(e);created.add(e);}}
            catch(RuntimeException invalid){for(Edit e:created)removeActor(e);throw invalid;}
            for(Edit e:batch)EDITS.put(e.key,e);redo.removeLast();undo.addLast(batch);
        }
    }
}
