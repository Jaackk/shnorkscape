package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/** Actual-cache diagnostic placement; no legacy spawn subclasses. */
public final class Native950DiagnosticSpawns {
    static final int MAX_TRAINING_DUMMIES=5;
    private static final int[][] TRAINING_DUMMY_OFFSETS={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    private static final java.util.Map<NPC,String> OWNERS = new java.util.IdentityHashMap<>();
    private Native950DiagnosticSpawns() { }
    static String spawnTrainingDummy(Player player) {
        return spawnTrainingDummies(player,1);
    }
    static String spawnTrainingDummies(Player player,int requested) {
        String refusal=refusal(player);if(refusal!=null)return refusal;
        if(player.getNative950Combat()==null)return "Combat is not ready.";
        pruneOwners();
        int existing=trainingDummyCount(player);
        int wanted=trainingDummySpawnCount(requested,existing);
        if(wanted<0)return "Use ;;dummy [1-"+MAX_TRAINING_DUMMIES+"].";
        if(wanted==0)return "You already have the maximum of "+MAX_TRAINING_DUMMIES+" owned training dummies. Use ;;removenpc or ;;clearnpcs first.";
        int spawned=0;
        String failure=null;
        for(int[] offset:TRAINING_DUMMY_OFFSETS) {
            if(spawned>=wanted)break;
            WorldTile tile=new WorldTile(player.getX()+offset[0],player.getY()+offset[1],player.getPlane());
            if(!World.canMoveNPC(tile,1)||!World.checkWalkStep(player.getPlane(),player.getX(),player.getY(),offset[0],offset[1],1))continue;
            NPC npc=null;
            try {
                npc=NPC.createNative950Diagnostic(16027,tile);
                Native950World.getInstance().addDiagnosticNpc(npc);
                player.getNative950Combat().registerTraining(npc);OWNERS.put(npc,player.getUsername());
                spawned++;
            }catch(IllegalArgumentException|IllegalStateException e){
                if(npc!=null&&World.containsNPC(npc))Native950World.getInstance().removeDiagnosticNpc(npc);
                failure=e.getMessage();
            }
        }
        if(spawned==0)return failure==null?"Move to an open tile before placing a training dummy.":"Cannot place a training dummy: "+failure;
        int total=existing+spawned;
        return "Spawned "+spawned+" training dumm"+(spawned==1?"y":"ies")+" ("+total+"/"+MAX_TRAINING_DUMMIES+"). They restore health and award no XP or loot."
                +(spawned<wanted?" Some nearby tiles were blocked.":"");
    }

    static int trainingDummySpawnCount(int requested,int existing) {
        if(requested<1||requested>MAX_TRAINING_DUMMIES||existing<0)return -1;
        return Math.max(0,Math.min(requested,MAX_TRAINING_DUMMIES-existing));
    }
    private static int trainingDummyCount(Player player) {
        int count=0;for(NPC npc:OWNERS.keySet())if(ownedBy(player,npc)&&npc.getId()==16027)count++;
        return count;
    }

    public static String spawnNpc(Player player, int id) {
        return spawnNpcs(player,id,1,false);
    }

    public static String spawnNpcs(Player player,int id,int amount,boolean repeat) {
        String refusal = refusal(player);
        if (refusal != null) return refusal;
        if(amount<1||amount>50)return "Use an NPC amount from 1 to 50.";
        pruneOwners();
        final int size;
        try {
            size=NPC.createNative950Diagnostic(id,new WorldTile(player)).getSize();
        } catch (IllegalArgumentException | IllegalStateException unavailable) {
            return "Cannot spawn NPC " + id + ": " + unavailable.getMessage();
        }
        byte[] facing=Utils.getDirection(player.getDirection());
        int fx=Integer.signum(facing[0]),fy=Integer.signum(facing[1]);
        if(fx==0&&fy==0)fy=1;
        int created=0;
        String lastFailure=null,name=null;
        // A developer command places one compact row, touching footprint edges.
        // It deliberately ignores gameplay clipping; ordinary spawns and dummies
        // keep their own collision policy. Try both sides to cope with world edges
        // and already occupied footprints without scattering NPCs into distant rows.
        for(int position=0;position<256&&created<amount;position++){
                WorldTile tile=compactTile(player,size,fx,fy,position);
                if(!spawnTileAvailable(player,tile,size))continue;
                NPC npc=null;
                try {
                    npc=NPC.createNative950Diagnostic(id,tile);
                    Native950World.getInstance().addDiagnosticNpc(npc);
                    Native950World.getInstance().setDiagnosticRepeat(npc,repeat);if(repeat)REPEATING.add(npc);else REPEATING.remove(npc);
                    OWNERS.put(npc,player.getUsername());name=npc.getName();created++;
                }catch(IllegalArgumentException|IllegalStateException unavailable){
                    if(npc!=null&&World.containsNPC(npc))Native950World.getInstance().removeDiagnosticNpc(npc);
                    lastFailure=unavailable.getMessage();
                }
        }
        if(created==0)return lastFailure==null?"No unoccupied in-world footprint ahead of you.":"Cannot spawn NPC "+id+": "+lastFailure;
        return "Spawned "+created+"/"+amount+" "+name+" (NPC "+id+") in a compact row ahead of you."
                +(repeat?" These test NPCs respawn after death.":" They do not respawn after death.")
                +(created<amount?" Some positions were blocked or the diagnostic limit was reached.":"");
    }

    /** Shared world registration used by the visual editor; full footprint policy is stricter than ;;npc. */
    static NPC placeNpc(Player player,int id,WorldTile tile,boolean repeat) {
        String rejection=refusal(player);if(rejection!=null)throw new IllegalArgumentException(rejection);
        NPC npc=NPC.createNative950Diagnostic(id,tile);
        if(!spawnTileAvailable(player,tile,npc.getSize())||!World.canMoveNPC(tile,npc.getSize()))
            throw new IllegalArgumentException("That NPC footprint is blocked or occupied.");
        registerPlacedNpc(npc,player.getUsername(),repeat);return npc;
    }
    static void registerPlacedNpc(NPC npc,String owner,boolean repeat){
        Native950World.getInstance().addDiagnosticNpc(npc);
        Native950World.getInstance().setDiagnosticRepeat(npc,repeat);if(repeat)REPEATING.add(npc);else REPEATING.remove(npc);OWNERS.put(npc,owner);
    }

    static WorldTile compactTile(WorldTile player,int size,int fx,int fy,int position){
        if(size<1||size>64||position<0)throw new IllegalArgumentException("Invalid diagnostic footprint");
        fx=Integer.signum(fx);fy=Integer.signum(fy);if(fx==0&&fy==0)fy=1;
        int side=position==0?0:((position+1)/2)*(position%2==1?1:-1);
        int x=player.getX()+(fx>0?1:fx<0?-size:-(size-1)/2);
        int y=player.getY()+(fy>0?1:fy<0?-size:-(size-1)/2);
        return new WorldTile(x+fy*side*size,y-fx*side*size,player.getPlane());
    }

    static boolean spawnTileAvailable(Player player,WorldTile tile,int size) {
        if(!fits(tile,size,size))return false;
        if(player!=null&&overlaps(tile,size,player,1,0))return false;
        for(Player other:World.getPlayers())if(other!=null&&!other.hasFinished()&&other.getPlane()==tile.getPlane()
                &&overlaps(tile,size,other,Math.max(1,other.getSize()),0))return false;
        for(NPC other:World.getNPCs())if(other!=null&&!other.hasFinished()&&other.getPlane()==tile.getPlane()
                &&overlaps(tile,size,other,Math.max(1,other.getSize()),0))return false;
        return true;
    }

    static boolean overlaps(WorldTile first,int firstSize,WorldTile second,int secondSize,int clearance) {
        return first.getPlane()==second.getPlane()
                &&first.getX()<second.getX()+secondSize+clearance
                &&second.getX()<first.getX()+firstSize+clearance
                &&first.getY()<second.getY()+secondSize+clearance
                &&second.getY()<first.getY()+firstSize+clearance;
    }

    static java.util.List<NPC> ownedNpcs(Player player){pruneOwners();java.util.List<NPC> result=new java.util.ArrayList<>();for(NPC npc:OWNERS.keySet())if(ownedBy(player,npc))result.add(npc);return result;}
    private static final java.util.Set<NPC> REPEATING=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<NPC,Boolean>());
    static boolean repeating(NPC npc){return REPEATING.contains(npc);}
    static void forget(NPC npc){OWNERS.remove(npc);REPEATING.remove(npc);}

    static java.util.List<String> matchingNpcIds(Player player,String query){
        java.util.List<String> result=new java.util.ArrayList<>();
        for(String row:Native950ContentCommands.search(true,query)){
            int separator=row.indexOf(':');
            if(separator<1)continue;
            try {
                int id=Integer.parseInt(row.substring(0,separator));
                NPC.createNative950Diagnostic(id,new WorldTile(player));
                result.add(row);
            }catch(IllegalArgumentException|IllegalStateException unavailable){/* Not a concrete visible 950 NPC. */}
        }
        return result;
    }

    static boolean ownedBy(Player player,NPC npc) {
        return npc!=null && npc.isNative950DiagnosticDefinition() && player.getUsername().equals(OWNERS.get(npc));
    }
    private static void pruneOwners() {
        OWNERS.keySet().removeIf(npc -> npc.hasFinished() || !World.containsNPC(npc));REPEATING.retainAll(OWNERS.keySet());
    }
    static java.util.List<String> manage(Player player,String command,int value) {
        String refusal=refusal(player);
        if(refusal!=null)return java.util.Collections.singletonList(refusal);
        pruneOwners();
        java.util.List<String> lines=new java.util.ArrayList<>();int count=0;
        for(NPC npc:new java.util.ArrayList<>(Native950World.getInstance().nativeNpcs())) {
            // ;;clearnpcs is a world-cleanup command.  It removes only explicit
            // diagnostic spawns, including another local developer's, never map or
            // data-driven NPCs.  Single-index removal remains owner-scoped below.
            if(command.equals("clearnpcs") ? !npc.isNative950DiagnosticDefinition() : !ownedBy(player,npc))continue;
            int distance=Math.max(Math.abs(player.getX()-npc.getX()),Math.abs(player.getY()-npc.getY()));
            if(command.equals("npcs")) {
                if(npc.getPlane()==player.getPlane()&&distance<=48&&count++<10)
                    lines.add("Index "+npc.getIndex()+": "+npc.getName()+" (ID "+npc.getId()+") at "+npc.getX()+","+npc.getY());
            } else {
                if(command.equals("clearnpcs") ? value>=0&&(npc.getPlane()!=player.getPlane()||distance>value) : npc.getIndex()!=value)continue;
                Native950World.getInstance().removeDiagnosticNpc(npc);OWNERS.remove(npc);count++;
            }
        }
        lines.add(command.equals("npcs")?count+" nearby test NPC(s); showing up to 10."
                :"Removed "+count+" diagnostic test NPC(s).");
        return lines;
    }

    /** Prefer ordinary scenery shape10; walls/decorations use their first actual cache shape. */
    public static String spawnObject(Player player, int id) {
        return spawnObject(player, id, -1, 0);
    }

    /** requestedType=-1 selects a supported cache shape; explicit types must have a world model. */
    public static String spawnObject(Player player, int id, int requestedType, int rotation) {
        String refusal=refusal(player);if(refusal!=null)return refusal;
        try {
            if(Native950DeveloperWorldEdits.owned(player.getUsername()).size()>=200)return "Remove placements first (200 per account).";
            WorldObject object=placeObject(id,requestedType,rotation,new WorldTile(player));
            Native950DeveloperWorldEdits.recordObject(player,object);
            return "Placed owned object "+id+". Use ;;clearobjects [radius] or Developer Console > Spawns to remove it.";
        }catch(IllegalArgumentException|IllegalStateException invalid){return "Cannot spawn object "+id+": "+invalid.getMessage();}
    }

    static String spawnObjectAt(Player player,int id,int requestedType,int rotation,WorldTile tile) {
        String refusal=refusal(player);if(refusal!=null)return refusal;
        try {
            WorldObject object=placeObject(id,requestedType,rotation,tile);
            return "Spawned "+ObjectDefinitions.getObjectDefinitions(id).name+" (object "+id+", type "+object.getType()+", rotation "+rotation+") at "+tile.getX()+", "+tile.getY()+".";
        }catch(IllegalArgumentException|IllegalStateException unavailable){return "Cannot spawn object "+id+": "+unavailable.getMessage();}
    }

    /** Shared validated placement also used for explicitly saved developer edits at startup. */
    static WorldObject placeObject(int id,int requestedType,int rotation,WorldTile tile) {
        if(id<0||rotation<0||rotation>3||!Cache.isFlatReadOnly())throw new IllegalArgumentException("Invalid object placement");
            Index[] indexes = Cache.STORE.getIndexes();
            Index index = indexes.length <= 16 ? null : indexes[16];
            if (index == null || (id >>> 8) > index.getLastArchiveId()
                    || index.getFile(id >>> 8, id & 255) == null)
                throw new IllegalArgumentException("That object ID is missing from the 950 cache.");
            ObjectDefinitions definition = ObjectDefinitions.getObjectDefinitions(id);
            if (!definition.loaded) throw new IllegalArgumentException("That object definition could not be loaded.");
            if (definition.transforms != null)
                throw new IllegalArgumentException("That object has variable forms; use a concrete form's object ID.");
            int type = selectShape(definition.shapes, definition.models, requestedType);
            int width = (rotation & 1) == 0 ? definition.sizeX : definition.sizeY;
            int height = (rotation & 1) == 0 ? definition.sizeY : definition.sizeX;
            if (!fits(tile, width, height)) throw new IllegalArgumentException("That object's footprint is unavailable at your tile.");
            Region region = World.getRegion(tile.getRegionId(), true);
            int slot = Region.OBJECT_SLOTS[type];
            // Region.spawnObject restores a removed map original before accepting a new override.
            // Refuse both live and removed occupants so a requested diagnostic never replaces a tree,
            // fire or pending regrowth, and the feedback cannot claim an ID the world did not retain.
            if (region.getObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot) != null
                    || region.getRemovedObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot) != null)
                throw new IllegalArgumentException("An object already occupies that scene slot. Move to an empty tile.");
            if (type >= 9 && type <= 21) {
                for (int x = tile.getX(); x < tile.getX() + width; x++)
                    for (int y = tile.getY(); y < tile.getY() + height; y++) {
                        WorldTile part = new WorldTile(x, y, tile.getPlane());
                        Region footprint = World.getRegion(part.getRegionId(), true);
                        if ((width > 1 || height > 1) && !World.isWallsFree(part.getPlane(), x, y))
                            throw new IllegalArgumentException("That multi-tile object's footprint touches a wall. Move to an area clear of walls.");
                        if (!World.isFloorFree(part.getPlane(), x, y)
                                || World.getObjectWithSlot(part, Region.OBJECT_SLOT_FLOOR) != null
                                || footprint.getRemovedObjectWithSlot(part.getPlane(), part.getXInRegion(),
                                        part.getYInRegion(), Region.OBJECT_SLOT_FLOOR) != null)
                            throw new IllegalArgumentException("That object's footprint overlaps scenery. Move to a clear area.");
                    }
            }
            WorldObject object = new WorldObject(id, type, rotation, tile);
            World.spawnObject(object); // Region owns collision and each session publishes its existing mutation ledger.
            if (World.getObjectWithSlot(tile, slot) != object)
                throw new IllegalStateException("The region did not retain the requested object.");
            return object;
    }

    static int selectShape(byte[] shapes, int[][] models, int requested) {
        if (requested < -1 || requested > 22)
            throw new IllegalArgumentException("Object type must be 0-22.");
        if (requested >= 0) {
            if (hasShape(shapes, models, requested)) return requested;
            throw new IllegalArgumentException("That cache object has no model for type " + requested + ".");
        }
        if (hasShape(shapes, models, 10)) return 10;
        if (shapes != null) for (byte shape : shapes) {
            int type = shape & 255;
            if (type <= 22 && hasShape(shapes, models, type)) return type;
        }
        throw new IllegalArgumentException("That object has no supported world model.");
    }

    private static boolean hasShape(byte[] shapes, int[][] models, int requested) {
        if (shapes == null || models == null || shapes.length != models.length) return false;
        for (int i = 0; i < shapes.length; i++)
            if ((shapes[i] & 255) == requested && models[i] != null && models[i].length > 0) return true;
        return false;
    }

    static boolean fits(WorldTile tile, int width, int height) {
        return tile != null && width >= 1 && height >= 1 && width <= 64 && height <= 64
                && tile.getX() >= 0 && tile.getY() >= 0 && tile.getPlane() >= 0 && tile.getPlane() <= 3
                && (long)tile.getX() + width <= 16384 && (long)tile.getY() + height <= 16384;
    }

    static String refusal(Player player) {
        if (player == null || !player.isNative950() || !player.isActive() || player.hasFinished()
                || player.isDead() || player.isLocked() || player.isNative950ForceMovementActive()
                || player.getNextForceMovement() != null || player.getNextWorldTile() != null
                || Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting")))
            return "Wait until your character can stand normally before spawning.";
        Native950TickScheduler wheel = CoresManager.getNative950Scheduler();
        if (wheel == null || wheel.owner() != Thread.currentThread())
            return "Diagnostic spawns must run on the native world thread.";
        if (player.getIndex() < 1 || World.getPlayers().get(player.getIndex()) != player)
            return "Your character is not registered in the native world.";
        if (!Cache.isFlatReadOnly()) return "Diagnostic spawns require the paired 950 cache.";
        return null;
    }
}
