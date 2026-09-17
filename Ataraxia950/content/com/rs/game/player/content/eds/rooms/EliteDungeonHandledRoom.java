package com.rs.game.player.content.eds.rooms;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.player.content.eds.EliteDungeon.Room;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public abstract class EliteDungeonHandledRoom implements Serializable {

    private static final long serialVersionUID = -2070353173803432584L;

    private static final HashMap<EliteDungeonKey, Class<? extends EliteDungeonHandledRoom>> handledRooms = new HashMap<>();

    public static final void add(final Class<? extends EliteDungeonHandledRoom> c) {
        try {
            if (c.isAnonymousClass() || !Modifier.isPublic(c.getModifiers())) {
                return;
            }
            EliteDungeonHandledRoom room = c.newInstance();
            if (room == null)
                return;
            handledRooms.put(new EliteDungeonKey(room.getEliteDungeonType(), room.getRoomIndex()), c);
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static final EliteDungeonHandledRoom getHandledRoom(int type, int roomIndex) {
        EliteDungeonKey key = new EliteDungeonKey(type, roomIndex);
        final Class<? extends EliteDungeonHandledRoom> handledRoom = handledRooms.get(key);
        if (handledRoom == null) {
            return null;
        }
        try {
            return handledRoom.newInstance();
        } catch (final Throwable e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    @AllArgsConstructor
    public static class EliteDungeonKey {
        @Getter
        @Setter
        private int eliteDungeonType, roomIndex;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + eliteDungeonType;
            result = prime * result + roomIndex;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EliteDungeonKey other = (EliteDungeonKey) obj;
            if (eliteDungeonType != other.eliteDungeonType)
                return false;
            return roomIndex == other.roomIndex;
        }

    }

    @Getter
    @Setter
    private transient Room room;
    @Getter
    @Setter
    private List<EliteDungeonNPC> npcs, killedNPCs;
    @Getter
    @Setter
    private transient List<CombinationBlock> blocks;

    public EliteDungeonHandledRoom() {
        npcs = new ArrayList<EliteDungeonNPC>();
        killedNPCs = new ArrayList<EliteDungeonNPC>();
        blocks = new ArrayList<CombinationBlock>();
    }

    public void load() {
        if (blocks == null)
            blocks = new ArrayList<CombinationBlock>();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                checkRemoveBlocks();
            }

        }, 1);
    }

    public void removeAll() {
        for (EliteDungeonNPC n : npcs)
            if (n != null)
                n.finish();
        for (EliteDungeonNPC n : killedNPCs)
            if (n != null)
                n.finish();
        for (CombinationBlock block : blocks)
            if (block != null)
                block.remove();
    }

    public int count = 0;

    public void updateNPC(EliteDungeonNPC n) {
        if (killedNPCs.contains(n)) {
            killedNPCs.remove(n);
            killedNPCs.add(n);
        }
        if (npcs.contains(n)) {
            npcs.remove(n);
            npcs.add(n);
        }
        for (CombinationBlock block : blocks) {
            if (block == null || !block.getLinkedNPCs().contains(n))
                continue;
            block.getLinkedNPCs().remove(n);
            block.getLinkedNPCs().add(n);
        }
    }

    public EliteDungeonNPC addNPC(EliteDungeonNPC n) {
        updateNPC(n);
        if (killedNPCs.contains(n)) {
            if (n instanceof SeiryuTheAzureSerpent) {
                n.spawn();
                ((SeiryuTheAzureSerpent) n).applyDeath();
                checkRemoveBlocks(n);
                return n;
            }
            n.finish();
            checkRemoveBlocks(n);
            return n;
        }
        n.spawn();
        if (!npcs.contains(n)) {
            npcs.add(n);
            count++;
        }
        return n;
    }

    public void refreshNPCs() {
        if (room != null)
            room.refreshNPCs();
    }

    public CombinationBlock addBlock(WorldObject object, List<EliteDungeonNPC> linkedNPCs) {
        return addBlock(object, -1, linkedNPCs);
    }

    public CombinationBlock addBlock(WorldObject object, int toObjectId, List<EliteDungeonNPC> linkedNPCs) {
        CombinationBlock block = new CombinationBlock(object, toObjectId, linkedNPCs);
        if (isUnlocked(block)) {
            for (EliteDungeonNPC n : block.getLinkedNPCs())
                n.finish();
            block.remove();
            return block;
        }
        block.spawn();
        blocks.add(block);
        return block;
    }

    public boolean isUnlocked(CombinationBlock block) {
        for (EliteDungeonNPC n : block.getLinkedNPCs()) {
            if (!killedNPCs.contains(n))
                return false;
        }
        return true;
    }

    public void handleDeath(EliteDungeonNPC n) {
        killedNPCs.add(n);
        checkRemoveBlocks();
    }

    public void checkRemoveBlocks() {
        checkRemoveBlocks(null);
    }

    public void checkRemoveBlocks(EliteDungeonNPC n) {
        for (CombinationBlock block : blocks) {
            if (block == null || (n != null && !block.getLinkedNPCs().contains(n)))
                continue;
            if (isUnlocked(block))
                block.remove();
        }
    }

    public abstract int getEliteDungeonType();

    public abstract int getRoomIndex();

    public abstract boolean isInBossFightArea(WorldTile tile);

    public WorldTile getTile(WorldTile tile) {
        return room.getTile(tile);
    }

    public WorldTile getTile(int x, int y, int plane) {
        return room.getTile(x, y, plane);
    }

    public WorldTile getAbsoluteTile(WorldTile tile) {
        return room.getAbsoluteTile(tile);
    }

    public static class CombinationBlock implements Serializable {

        private static final long serialVersionUID = 7737854201254507444L;

        @Getter
        @Setter
        private WorldObject blockObject;

        @Getter
        @Setter
        private List<EliteDungeonNPC> linkedNPCs;
        private final int originalId;
        private final int toObjectId;

        public CombinationBlock(WorldObject blockObject, int toObjectId, List<EliteDungeonNPC> linkedNPCs) {
            this.blockObject = blockObject;
            this.linkedNPCs = linkedNPCs;
            this.toObjectId = toObjectId;
            this.originalId = blockObject.getId();
            spawn();
        }

        public void spawn() {
            spawn(false);
        }

        public void spawn(boolean useOriginalId) {
            if (useOriginalId)
                blockObject.setId(originalId);
            World.spawnObject(blockObject);
            World.getRegion(blockObject.getRegionId(), true);
        }

        public void remove() {
            if (originalId == -1) {
                blockObject.setId(toObjectId);
                World.spawnObject(blockObject);
                World.executeAfterLoadRegion(blockObject.getRegionId(), new Runnable() {

                    @Override
                    public void run() {
                        blockObject.setId(toObjectId);
                        World.spawnObject(blockObject);
                    }
                });
                return;
            }
            WorldObject block = World.getObjectWithId(blockObject, blockObject.getId());
            if (block != null) {
                World.removeObject(block);
                if (toObjectId != -1) {
                    block.setId(toObjectId);
                    World.spawnObject(block);
                }
            }
            World.executeAfterLoadRegion(blockObject.getRegionId(), new Runnable() {

                @Override
                public void run() {
                    WorldObject block = World.getObjectWithId(blockObject, blockObject.getId());
                    if (block == null)
                        return;
                    if (block != null) {
                        World.removeObject(block);
                        if (toObjectId != -1) {
                            block.setId(toObjectId);
                            World.spawnObject(block);
                        }
                    }
                }
            });
        }

        public boolean isSpawned() {
            WorldObject object = World.getObjectWithId(blockObject, blockObject.getId());
            if (object == null)
                return false;
            return object.getId() == originalId;
        }

        public void addNPC(EliteDungeonNPC n) {
            if (!linkedNPCs.contains(n))
                linkedNPCs.add(n);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((blockObject == null) ? 0 : blockObject.hashCode());
            result = prime * result + ((linkedNPCs == null) ? 0 : linkedNPCs.hashCode());
            result = prime * result + originalId;
            result = prime * result + toObjectId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CombinationBlock other = (CombinationBlock) obj;
            if (blockObject == null) {
                if (other.blockObject != null)
                    return false;
            } else if (blockObject.matches(other.getBlockObject()))
                return false;
            if (originalId != other.originalId)
                return false;
            return toObjectId == other.toObjectId;
        }

    }

}
