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

/** Actual-cache diagnostic placement at the exact player tile; no legacy spawn subclasses. */
public final class Native950DiagnosticSpawns {
    private static final java.util.Map<NPC,String> OWNERS = new java.util.IdentityHashMap<>();
    private Native950DiagnosticSpawns() { }
    static String spawnTrainingDummy(Player player) {
        String refusal=refusal(player);if(refusal!=null)return refusal;
        if(player.getNative950Combat()==null)return "Combat is not ready.";
        pruneOwners();
        for(NPC existing:OWNERS.keySet())if(ownedBy(player,existing)&&existing.getId()==16027)
            return "You already have a training dummy (index "+existing.getIndex()+"). Use ;;removenpc or ;;clearnpcs first.";
        for(int[] offset:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            WorldTile tile=new WorldTile(player.getX()+offset[0],player.getY()+offset[1],player.getPlane());
            if(!World.canMoveNPC(tile,1)||!World.checkWalkStep(player.getPlane(),player.getX(),player.getY(),offset[0],offset[1],1))continue;
            NPC npc=null;
            try {
                npc=NPC.createNative950Diagnostic(16027,tile);
                Native950World.getInstance().addDiagnosticNpc(npc);
                player.getNative950Combat().registerTraining(npc);OWNERS.put(npc,player.getUsername());
                return "Training dummy spawned beside you (index "+npc.getIndex()+"). It restores health and awards no XP or loot.";
            }catch(IllegalArgumentException|IllegalStateException e){
                if(npc!=null&&World.containsNPC(npc))Native950World.getInstance().removeDiagnosticNpc(npc);
                return "Cannot place training dummy: "+e.getMessage();
            }
        }
        return "Move to an open tile before placing a training dummy.";
    }

    public static String spawnNpc(Player player, int id) {
        String refusal = refusal(player);
        if (refusal != null) return refusal;
        pruneOwners();
        try {
            NPC npc = NPC.createNative950Diagnostic(id, new WorldTile(player));
            Native950World.getInstance().addDiagnosticNpc(npc);
            OWNERS.put(npc,player.getUsername());
            return "Spawned " + npc.getName() + " (NPC " + id + ", index " + npc.getIndex() + ") at your tile."
                    + (npc.getNative950CombatProfile() == null ? " Combat is not available for this NPC." : "");
        } catch (IllegalArgumentException | IllegalStateException unavailable) {
            return "Cannot spawn NPC " + id + ": " + unavailable.getMessage();
        }
    }

    static boolean ownedBy(Player player,NPC npc) {
        return npc!=null && npc.isNative950DiagnosticDefinition() && player.getUsername().equals(OWNERS.get(npc));
    }
    private static void pruneOwners() {
        OWNERS.keySet().removeIf(npc -> npc.hasFinished() || !World.containsNPC(npc));
    }
    static java.util.List<String> manage(Player player,String command,int value) {
        String refusal=refusal(player);
        if(refusal!=null)return java.util.Collections.singletonList(refusal);
        pruneOwners();
        java.util.List<String> lines=new java.util.ArrayList<>();int count=0;
        for(NPC npc:new java.util.ArrayList<>(Native950World.getInstance().nativeNpcs())) {
            if(!ownedBy(player,npc))continue;
            int distance=Math.max(Math.abs(player.getX()-npc.getX()),Math.abs(player.getY()-npc.getY()));
            if(command.equals("npcs")) {
                if(npc.getPlane()==player.getPlane()&&distance<=48&&count++<10)
                    lines.add("Index "+npc.getIndex()+": "+npc.getName()+" (ID "+npc.getId()+") at "+npc.getX()+","+npc.getY());
            } else {
                if(command.equals("clearnpcs") ? value>=0&&(npc.getPlane()!=player.getPlane()||distance>value) : npc.getIndex()!=value)continue;
                Native950World.getInstance().removeDiagnosticNpc(npc);OWNERS.remove(npc);count++;
            }
        }
        lines.add(command.equals("npcs")?count+" nearby test NPC(s); showing up to 10.":"Removed "+count+" of your test NPC(s).");
        return lines;
    }

    /** Prefer ordinary scenery shape10; walls/decorations use their first actual cache shape. */
    public static String spawnObject(Player player, int id) {
        return spawnObject(player, id, -1, 0);
    }

    /** requestedType=-1 selects a supported cache shape; explicit types must have a world model. */
    public static String spawnObject(Player player, int id, int requestedType, int rotation) {
        String refusal = refusal(player);
        if (refusal != null) return refusal;
        if (id < 0) return "Object ID must be nonnegative.";
        if (rotation < 0 || rotation > 3) return "Object rotation must be 0-3.";
        try {
            Index[] indexes = Cache.STORE.getIndexes();
            Index index = indexes.length <= 16 ? null : indexes[16];
            if (index == null || (id >>> 8) > index.getLastArchiveId()
                    || index.getFile(id >>> 8, id & 255) == null)
                return "That object ID is missing from the 950 cache.";
            ObjectDefinitions definition = ObjectDefinitions.getObjectDefinitions(id);
            if (!definition.loaded) return "That object definition could not be loaded.";
            if (definition.transforms != null)
                return "That object has variable forms; use a concrete form's object ID.";
            int type = selectShape(definition.shapes, definition.models, requestedType);
            int width = (rotation & 1) == 0 ? definition.sizeX : definition.sizeY;
            int height = (rotation & 1) == 0 ? definition.sizeY : definition.sizeX;
            WorldTile tile = new WorldTile(player);
            if (!fits(tile, width, height)) return "That object's footprint is unavailable at your tile.";
            Region region = World.getRegion(tile.getRegionId(), true);
            int slot = Region.OBJECT_SLOTS[type];
            // Region.spawnObject restores a removed map original before accepting a new override.
            // Refuse both live and removed occupants so a requested diagnostic never replaces a tree,
            // fire or pending regrowth, and the feedback cannot claim an ID the world did not retain.
            if (region.getObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot) != null
                    || region.getRemovedObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot) != null)
                return "An object already occupies that scene slot. Move to an empty tile.";
            if (type >= 9 && type <= 21) {
                for (int x = tile.getX(); x < tile.getX() + width; x++)
                    for (int y = tile.getY(); y < tile.getY() + height; y++) {
                        WorldTile part = new WorldTile(x, y, tile.getPlane());
                        Region footprint = World.getRegion(part.getRegionId(), true);
                        if ((width > 1 || height > 1) && !World.isWallsFree(part.getPlane(), x, y))
                            return "That multi-tile object's footprint touches a wall. Move to an area clear of walls.";
                        if (!World.isFloorFree(part.getPlane(), x, y)
                                || World.getObjectWithSlot(part, Region.OBJECT_SLOT_FLOOR) != null
                                || footprint.getRemovedObjectWithSlot(part.getPlane(), part.getXInRegion(),
                                        part.getYInRegion(), Region.OBJECT_SLOT_FLOOR) != null)
                            return "That object's footprint overlaps scenery. Move to a clear area.";
                    }
            }
            WorldObject object = new WorldObject(id, type, rotation, tile);
            World.spawnObject(object); // Region owns collision and each session publishes its existing mutation ledger.
            if (World.getObjectWithSlot(tile, slot) != object)
                throw new IllegalStateException("The region did not retain the requested object.");
            return "Spawned " + definition.name + " (object " + id + ", type " + type
                    + ", rotation " + rotation + ") at your tile.";
        } catch (IllegalArgumentException | IllegalStateException unavailable) {
            return "Cannot spawn object " + id + ": " + unavailable.getMessage();
        }
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

    private static String refusal(Player player) {
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
