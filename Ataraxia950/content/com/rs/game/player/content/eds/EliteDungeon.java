package com.rs.game.player.content.eds;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.MapInstance;
import com.rs.game.MapInstance.Stages;
import com.rs.game.WorldTile;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.eds.EliteDungeonsConstants.DungeonRooms;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import lombok.Getter;
import lombok.Setter;

public class EliteDungeon implements Serializable {

    private static final long serialVersionUID = 9173704823649458761L;
    private static final String path = "data/cachedelitedungeons.ced";
    private static HashMap<Object, EliteDungeon> cachedDungeons = new HashMap<Object, EliteDungeon>();

    public static final AtomicLong keyMaker = new AtomicLong();
    private String key;
    @Getter
    private transient EliteDungeonPartyManager party;
    @Getter
    private final int eliteDungeonType;
    @Getter
    private final Room[] rooms;
    @Getter
    private transient long NPCS_BAR_CYCLE;
    private transient boolean initiated;
    @Getter
    @Setter
    private int maxTeleportsSize;

    public static void save() {
        try {
            removeEmpty();
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        try {
            SerializableFilesManager.storeSerializableClass(cachedDungeons, new File(path));
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void removeEmpty() {
        Iterator<Entry<Object, EliteDungeon>> itr = cachedDungeons.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Object, EliteDungeon> e = itr.next();
            if (e == null || e.getValue() == null) {
                itr.remove();
                continue;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void initialize() {
        if (new File(path).exists()) {
            try {
                HashMap<Object, EliteDungeon> dungons = (HashMap<Object, EliteDungeon>) SerializableFilesManager.loadSerializedFile(new File(path));
                if (dungons == null || dungons.isEmpty())
                    cachedDungeons = new HashMap<Object, EliteDungeon>();
                else {
                    cachedDungeons = dungons;
                    Logger.getGlobal().info("loaded " + cachedDungeons.size() + " cached elite dungeons.");
                }
            } catch (ClassNotFoundException e) {
                Logger.getGlobal().catching(e);
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        } else
            cachedDungeons = new HashMap<Object, EliteDungeon>();
    }

    public EliteDungeon(EliteDungeonPartyManager party, int eliteDungeonType) {
        this.party = party;
        this.eliteDungeonType = eliteDungeonType;
        int roomCount = DungeonRooms.getRoomsCount(eliteDungeonType);
        if (roomCount <= 0)
            throw new IllegalArgumentException("No elite dungeon rooms registered for type " + eliteDungeonType);
        rooms = new Room[roomCount];
        initiated = true;
        maxTeleportsSize = 1;
        setDungeon();
        addActionBarTask();
    }

    public void init() {
        if (initiated)
            return;
        initiated = true;
        party = new EliteDungeonPartyManager();
        for (int i = 0; i < rooms.length; i++) {
            if (rooms[i] == null)
                continue;
            rooms[i].init(this, DungeonRooms.getRoomByIndex(eliteDungeonType, i));
        }
        party.setDungeon(this);
        addActionBarTask();
    }

    public boolean enterDungeon(Player player) {
        if (!enterRoom(player, 0))
            return false;
        sendSettings(player);
        return true;
    }

    public boolean enterRoom(Player player, int roomIndex) {
        return enterRoom(player, roomIndex, null, false);
    }

    public boolean enterRoom(Player player, int roomIndex, WorldTile customWorldTile) {
        return enterRoom(player, roomIndex, customWorldTile, false);
    }

    public boolean enterRoom(Player player, int roomIndex, WorldTile customWorldTile, boolean teleport) {
        if (roomIndex < 0 || roomIndex >= rooms.length)
            return false;
        if (rooms[roomIndex] == null) {
            DungeonRooms roomData = DungeonRooms.getRoomByIndex(eliteDungeonType, roomIndex);
            if (roomData == null)
                return false;
            rooms[roomIndex] = new Room(this, roomData);
        }
        if (rooms[roomIndex] == null || rooms[roomIndex].getRoomData() == null)
            return false;
        if (teleport) {
            if (!rooms[roomIndex].forceLoadRoom())
                return false;
            player.stopAll(true, true);
            player.setNextAnimation(new Animation(8939));
            player.setNextGraphics(new Graphics(1576));
            WorldTasksManager.schedule(new WorldTask() {

                boolean removeDamage;

                @Override
                public void run() {
                    if (!removeDamage) {
                        if (!rooms[roomIndex].teleportPlayer(player, customWorldTile)) {
                            stop();
                            return;
                        }
                        for (Room room : rooms)
                            if (room != null && room != rooms[roomIndex])
                                room.removePlayer(player);
                        player.setNextAnimation(new Animation(8941));
                        player.setNextGraphics(new Graphics(1577));
                        removeDamage = true;
                    } else {
                        player.resetReceivedDamage();
                        player.resetReceivedHits();
                        stop();
                    }
                }
            }, 3, 0);
            return true;
        }
        if (!rooms[roomIndex].teleportPlayer(player, customWorldTile))
            return false;
        for (Room room : rooms)
            if (room != null && room != rooms[roomIndex])
                room.removePlayer(player);
        return true;
    }

    public boolean containsPlayers() {
        for (Room room : rooms)
            if (room != null && !room.getPlayers().isEmpty())
                return true;
        return false;
    }

    public boolean isInside(Player player) {
        for (Room room : rooms)
            if (room != null && room.containsPlayer(player))
                return true;
        return false;
    }

    public Room getPlayerRoom(Player player) {
        for (Room room : rooms)
            if (room != null && room.containsPlayer(player))
                return room;
        return null;
    }

    public void removePlayer(Player player) {
        for (Room room : rooms)
            if (room != null)
                room.removePlayer(player);
    }

    public void leaveDungeon(Player player) {
        leaveDungeon(player, false);
    }

    public void leaveDungeon(Player player, boolean notOutside) {
        removePlayer(player);
        player.setForceMultiArea(false);
        player.getInterfaceManager().closeOverlay(true);
        player.getControlerManager().removeControlerWithoutCheck();
        player.getHintIconsManager().removeUnsavedHintIcon();
        player.setLargeSceneView(false);
        player.getLocalNPCUpdate().reset();
        player.getTemporaryAttributtes().remove("waterbuff");
        player.getPackets().sendConfigByFile(44067, 1, true);
        removeMark(player);
        if (!notOutside)
            player.setNextWorldTile(EliteDungeonsConstants.getOutsideTile(eliteDungeonType));
    }

    public void sendSettings(Player player) {
        player.getControlerManager().startControler("EliteDungeonController");
        player.setLargeSceneView(true);
        player.getLocalNPCUpdate().reset();
        player.setForceMultiArea(true);
        player.getPackets().sendConfigByFile(44067, 1, true);
    }

    public static EliteDungeon getDungeonByKey(String key) {
        return cachedDungeons.get(key);
    }

    public int getPartySize() {
        return party.getSize();
    }

    public void setDungeon() {
        key = "ed_" + keyMaker.getAndIncrement();
        while (cachedDungeons.containsKey(key))
            key = "ed_" + keyMaker.getAndIncrement();
        cachedDungeons.put(key, this);
        for (final Player player : party.getTeam()) {
            player.getEliteDungeonsManager().setRejoinKey(key);
        }
    }

    public void destroyRooms() {
        for (Room r : rooms)
            if (r != null)
                r.destroy();
    }

    public void removeDungeon() {
        cachedDungeons.remove(key);
    }

    public void setMark(Entity target, boolean mark) {
        if (mark) {
            for (Player player : party.getTeam())
                player.getHintIconsManager().addHintIcon(6, target, 0, -1, true); // 6th
            // slot
        } else
            removeMark();
        if (target instanceof EliteDungeonNPC)
            ((EliteDungeonNPC) target).setMarked(mark);
    }

    public void removeMark() {
        for (Player player : party.getTeam())
            removeMark(player);
    }

    public void removeMark(Player player) {
        player.getHintIconsManager().removeHintIcon(6);
    }

    public void addActionBarTask() {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                try {
                    if (!cachedDungeons.containsKey(key))
                        return false;
                    NPCS_BAR_CYCLE++;
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = -4390218805917244076L;
        @Getter
        private transient DungeonRooms roomData;
        private transient MapInstance map;
        @Getter
        private transient CopyOnWriteArrayList<Player> players;
        private final EliteDungeonHandledRoom roomContent;
        @Getter
        private transient boolean loaded;
        @Getter
        private transient EliteDungeon dungeon;
        private transient boolean initiated;
        private transient HashMap<WorldTile, WorldTile> absoluteTiles;
        private transient boolean sentDestroyRequest;

        public Room(EliteDungeon dungeon, DungeonRooms roomData) {
            if (roomData == null)
                throw new IllegalArgumentException("Elite dungeon room data is missing.");
            this.dungeon = dungeon;
            this.roomData = roomData;
            absoluteTiles = new HashMap<WorldTile, WorldTile>();
            map = new MapInstance(roomData.getRoomChunks()[0], roomData.getRoomChunks()[1], roomData.getRoomSizes()[0], roomData.getRoomSizes()[1]);
            players = new CopyOnWriteArrayList<Player>();
            roomContent = EliteDungeonHandledRoom.getHandledRoom(roomData.getType(), roomData.getRoomIndex());
            if (roomContent != null)
                roomContent.setRoom(this);
//            else
//                System.out.println("Couldn't find room content for elite dungeon " + roomData.getType() + ", room index=" + roomData.getRoomIndex());
            initiated = true;
        }

        public void init(EliteDungeon dungeon, DungeonRooms roomData) {
            if (initiated)
                return;
            initiated = true;
            this.dungeon = dungeon;
            this.roomData = roomData;
            absoluteTiles = new HashMap<WorldTile, WorldTile>();
            map = new MapInstance(roomData.getRoomChunks()[0], roomData.getRoomChunks()[1], roomData.getRoomSizes()[0], roomData.getRoomSizes()[1]);
            players = new CopyOnWriteArrayList<Player>();
            if (roomContent != null)
                roomContent.setRoom(this);
        }

        public boolean forceLoadRoom() {
            if (map.getStage() == Stages.LOADING || map.getStage() == Stages.DESTROYING)
                return false;
            load(null);
            return true;
        }

        public boolean teleportPlayer(Player player, WorldTile customWorldTile) {
            if (map.getStage() == Stages.LOADING || map.getStage() == Stages.DESTROYING)
                return false;
            WorldTile toTile = customWorldTile != null ? customWorldTile : roomData.getStartTile();
            if (!players.contains(player))
                players.add(player);
            if (loaded)
                player.setNextWorldTile(getTile(toTile));
            else
                load(new Runnable() {
                    @Override
                    public void run() {
                        player.setNextWorldTile(getTile(toTile));
                    }
                });
            return true;
        }

        public boolean containsPlayer(Player player) {
            return players.contains(player);
        }

        public void removePlayer(Player player) {
            players.remove(player);
            if (players.isEmpty())
                destroy();
        }

        public void load(Runnable r) {
            if (map.getStage() != null)
                return;
            map.load(new Runnable() {
                @Override
                public void run() {
                    try {
                        loaded = true;
                        if (roomContent != null)
                            roomContent.load();
                        if (r != null)
                            r.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void destroy() {
            if (map.getStage() != Stages.RUNNING || !players.isEmpty())
                return;
            if (sentDestroyRequest)
                return;
            sentDestroyRequest = true;
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                @Override
                public boolean repeat() {
                    try {
                        if (map.getStage() != Stages.RUNNING || !players.isEmpty()) {
                            sentDestroyRequest = false;
                            return false;
                        }
                        if (roomContent != null)
                            roomContent.removeAll();
                        map.destroy(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sentDestroyRequest = false;
                                    loaded = false;
                                    map = new MapInstance(roomData.getRoomChunks()[0], roomData.getRoomChunks()[1], roomData.getRoomSizes()[0], roomData.getRoomSizes()[1]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return false;
                    } catch (Exception e) {
                        sentDestroyRequest = false;
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }
            }, 10000, 600, TimeUnit.MILLISECONDS);
        }

        public WorldTile getTile(WorldTile tile) {
            return getTile(tile.getX(), tile.getY(), tile.getPlane());
        }

        public WorldTile getTile(int x, int y, int plane) {
            int[] originalPos = map.getOriginalPos();
            WorldTile tile = map.getTile((x - originalPos[0] * 8) % (map.getRatioX() * 64), (y - originalPos[1] * 8) % (map.getRatioY() * 64));
            tile.moveLocation(0, 0, plane);
            absoluteTiles.put(tile, new WorldTile(x, y, plane));
            return tile;
        }

        public WorldTile getAbsoluteTile(WorldTile tile) {
            return absoluteTiles.get(tile);
        }

        public void refreshNPCs() {
            if (players == null || players.isEmpty())
                return;
            for (Player player : players) {
                if (player == null)
                    continue;
                player.getLocalNPCUpdate().reset();
            }
        }

    }

    public static void checkRejoin(final Player player) {
        player.getPackets().sendConfigByFile(44067, 1, true);
        final Object key = player.getEliteDungeonsManager().getRejoinKey();
        if (key == null) {
            return;
        }
        final EliteDungeon dungeon = cachedDungeons.get(key);
        // either doesnt exit / ur m8s moving next floor(reward screen)
        if (dungeon == null || player.isInsideAnyDungParty()) {
            player.getEliteDungeonsManager().setRejoinKey(null);
            return;
        }
        dungeon.init();
        dungeon.rejoinParty(player);
    }

    public void rejoinParty(final Player player) {
        player.stopAll();
        player.lock(2);
        party.add(player);
    }
}
