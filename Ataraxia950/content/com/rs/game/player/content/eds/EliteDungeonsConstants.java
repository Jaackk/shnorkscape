package com.rs.game.player.content.eds;

import com.rs.game.WorldTile;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import lombok.Getter;

public class EliteDungeonsConstants {
    public static final int TEMPLE_OF_AMINISHI = 1;
    public static final int DRAGONKIN_LABORATORY = 2;
    public static final int SHADOW_REEF = 3;

    public static WorldTile[] OUTSIDE_TILES = {
            new WorldTile(2094, 11354, 0),
            new WorldTile(3371, 3885, 0),
            new WorldTile(3511, 3693, 0)
    };

    public static String getDungeonName(int type) {
        switch (type) {
        case TEMPLE_OF_AMINISHI:
            return "Temple of Aminishi";
        case DRAGONKIN_LABORATORY:
            return "Dragonkin Laboratory";
        case SHADOW_REEF:
            return "Shadow Reef";
        default:
            return "Elite Dungeon";
        }
    }

    public static WorldTile getOutsideTile(int type) {
        int index = type - 1;
        if (index < 0 || index >= OUTSIDE_TILES.length)
            return OUTSIDE_TILES[0];
        return OUTSIDE_TILES[index];
    }

    public enum DungeonRooms {
        ELITE_DUNGEON_1_1(new WorldTile(4483, 9178, 0), new WorldTile(2094, 11354, 0), new int[] { 560, 1144 }, new int[] { 2, 2 }), // 4483 9180 0
        ELITE_DUNGEON_1_2(new WorldTile(4736, 9265, 1), new WorldTile(4605, 9209, 0), new int[] { 587, 1140 }, new int[] { 2, 3 }), // 4737 9265 1
        ELITE_DUNGEON_1_3(new WorldTile(4488, 9147, 1), new WorldTile(4743, 9127, 1), new int[] { 560, 1128 }, new int[] { 3, 2 }), // 4490 9147 1
        ELITE_DUNGEON_1_4(new WorldTile(4644, 9156, 1), new WorldTile(4669, 9106, 1), new int[] { 576, 1144 }, new int[] { 1, 2 }), // 4642 9156 1
        ELITE_DUNGEON_1_5(new WorldTile(4737, 9083, 1), new WorldTile(4639, 9255, 1), new int[] { 586, 1120 }, new int[] { 2, 2 }), // 4738 9083 1

        ELITE_DUNGEON_2_1(new WorldTile(5024, 9260, 0), new WorldTile(3371, 3885, 0), new int[] { 628, 1152 }, new int[] { 4, 4 }),
        ELITE_DUNGEON_2_2(new WorldTile(5060, 9260, 0), new WorldTile(5048, 9260, 0), new int[] { 628, 1152 }, new int[] { 4, 4 }),
        ELITE_DUNGEON_2_3(new WorldTile(5115, 9260, 0), new WorldTile(5100, 9260, 0), new int[] { 628, 1152 }, new int[] { 4, 4 }),

        ELITE_DUNGEON_3_1(new WorldTile(5537, 9117, 3), new WorldTile(3511, 3693, 0), new int[] { 672, 1136 }, new int[] { 4, 4 }),
        ELITE_DUNGEON_3_2(new WorldTile(5496, 9085, 3), new WorldTile(5480, 9117, 3), new int[] { 672, 1136 }, new int[] { 4, 4 }),
        ELITE_DUNGEON_3_3(new WorldTile(5530, 9052, 3), new WorldTile(5528, 9052, 3), new int[] { 672, 1136 }, new int[] { 4, 4 });

        @Getter
        private final WorldTile startTile;
        @Getter
        private final WorldTile exitTile;
        @Getter
        private final int[] roomChunks;
        @Getter
        private final int[] roomSizes;

        DungeonRooms(WorldTile startTile, WorldTile exitTile, int[] roomChunks, int[] roomSizes) {
            this.startTile = startTile;
            this.exitTile = exitTile;
            this.roomChunks = roomChunks;
            this.roomSizes = roomSizes;
        }

        public int getType() {
            return Integer.parseInt(name().split("_")[2]);
        }

        public int getRoomIndex() {
            return Integer.parseInt(name().split("_")[3]) - 1;
        }

        public static DungeonRooms getRoomByIndex(int type, int roomIndex) {
            for (DungeonRooms room : DungeonRooms.values())
                if (room.getType() == type && room.getRoomIndex() == roomIndex)
                    return room;
            return null;
        }

        public static int getRoomsCount(int type) {
            int count = 0;
            for (DungeonRooms room : DungeonRooms.values())
                if (room.getType() == type)
                    count++;
            return count;
        }

    }

    public enum EliteDungeonsTeleports {
        ELITE_DUNGEON_1_TELEPORTS(new EliteDungeonTeleport("Temple Entrance", new WorldTile(4482, 9182, 0), 0),

                new EliteDungeonTeleport("Chamber of the Guardian", new WorldTile(4743, 9167, 1), 1),

                new EliteDungeonTeleport("Throne Room", new WorldTile(4621, 9241, 1), 3),

                new EliteDungeonTeleport("The Azure Prison", new WorldTile(4743, 9037, 1), 4)),

        ELITE_DUNGEON_2_TELEPORTS(new EliteDungeonTeleport("Laboratory Entrance", new WorldTile(5024, 9260, 0), 0),

                new EliteDungeonTeleport("Astellarn's Chamber", new WorldTile(5060, 9260, 0), 1),

                new EliteDungeonTeleport("Black Stone Dragon", new WorldTile(5115, 9260, 0), 2)),

        ELITE_DUNGEON_3_TELEPORTS(new EliteDungeonTeleport("Shadow Reef Entrance", new WorldTile(5537, 9117, 3), 0),

                new EliteDungeonTeleport("Crassian Leviathan", new WorldTile(5496, 9085, 3), 1),

                new EliteDungeonTeleport("The Ambassador", new WorldTile(5530, 9052, 3), 2)),;

        @Getter
        private final EliteDungeonTeleport[] teleports;

        EliteDungeonsTeleports(EliteDungeonTeleport... teleports) {
            this.teleports = teleports;
        }

        public int getType() {
            return Integer.parseInt(name().split("_")[2]);
        }

        public static EliteDungeonsTeleports getTeleportsByDungonType(int type) {
            for (EliteDungeonsTeleports teleports : EliteDungeonsTeleports.values())
                if (teleports.getType() == type)
                    return teleports;
            return null;
        }

        public static EliteDungeonTeleport getTeleportByDungonTypeAndRoomIndex(int type, int roomIndex) {
            for (EliteDungeonsTeleports teleports : EliteDungeonsTeleports.values())
                if (teleports.getType() == type) {
                    for (EliteDungeonTeleport teleport : teleports.teleports) {
                        if (teleport.getRoomIndex() == roomIndex)
                            return teleport;
                    }
                }
            return null;
        }
    }

    // ed1 room 4 hidden staircase id=111546, type=10, rot=2, tile= [ 4633, 9257, 1
    // ] replace by 111547
    public enum Doors {
        ED1_1_0(TEMPLE_OF_AMINISHI, 0, 111710, null, false),

        ED1_1_1(TEMPLE_OF_AMINISHI, 0, 111711, DungeonRooms.ELITE_DUNGEON_1_2, false),

        ED1_2_0(TEMPLE_OF_AMINISHI, 1, 111712, DungeonRooms.ELITE_DUNGEON_1_1, true),

        ED1_2_1(TEMPLE_OF_AMINISHI, 1, 111713, DungeonRooms.ELITE_DUNGEON_1_3, false),

        ED1_3_0(TEMPLE_OF_AMINISHI, 2, 111714, DungeonRooms.ELITE_DUNGEON_1_2, true),

        ED1_3_1(TEMPLE_OF_AMINISHI, 2, 111715, DungeonRooms.ELITE_DUNGEON_1_4, false),

        ED1_4_0(TEMPLE_OF_AMINISHI, 3, 111716, DungeonRooms.ELITE_DUNGEON_1_3, true),

        ED1_4_1(TEMPLE_OF_AMINISHI, 3, 111547, DungeonRooms.ELITE_DUNGEON_1_5, false),

        ED1_5_0(TEMPLE_OF_AMINISHI, 4, 111717, DungeonRooms.ELITE_DUNGEON_1_4, true),

        ED2_1_1(DRAGONKIN_LABORATORY, 0, 111747, DungeonRooms.ELITE_DUNGEON_2_2, false),

        ED2_2_1(DRAGONKIN_LABORATORY, 1, 111763, DungeonRooms.ELITE_DUNGEON_2_3, false),

        ED2_3_0(DRAGONKIN_LABORATORY, 2, 111940, null, false),

        ED3_1_1(SHADOW_REEF, 0, 2101, DungeonRooms.ELITE_DUNGEON_3_2, false),

        ED3_2_1(SHADOW_REEF, 1, 112746, DungeonRooms.ELITE_DUNGEON_3_3, false),

        ED3_3_0(SHADOW_REEF, 2, 5999, null, false);
        @Getter
        private final int dungeonType;
        @Getter
        private final int sourceRoomIndex;
        @Getter
        private final int objectId;
        @Getter
        private final DungeonRooms room;
        @Getter
        private final boolean useExitTile;

        Doors(int dungeonType, int sourceRoomIndex, int objectId, DungeonRooms room, boolean useExitTile) {
            this.dungeonType = dungeonType;
            this.sourceRoomIndex = sourceRoomIndex;
            this.objectId = objectId;
            this.room = room;
            this.useExitTile = useExitTile;
        }

        public boolean matches(int dungeonType, int sourceRoomIndex, int objectId) {
            return this.dungeonType == dungeonType && this.sourceRoomIndex == sourceRoomIndex && this.objectId == objectId;
        }

    }

    public static final NPCDrop[] mobDrops = {

            new NPCDrop(995, 80.00, 10000, 30000),

//            new NPCDrop(40304, 40.00, 1, 1),

            new NPCDrop(29556, 80.00, 1, 1),

            new NPCDrop(34729, 40.00, 1, 1),

            new NPCDrop(25507, 40.00, 1, 1),

            new NPCDrop(37822, 0.36, 1, 1), 

            new NPCDrop(43062, 80.00, 1, 1),

            new NPCDrop(43060, 40.00, 1, 1),

            new NPCDrop(43058, 5.00, 1, 1),

            new NPCDrop(43064, 40, 1, 1),

            new NPCDrop(47294, 40, 1, 1),

            new NPCDrop(47294, 40, 1, 1),

            new NPCDrop(29320, 80, 10, 30),

            new NPCDrop(29316, 40, 10, 30),

            new NPCDrop(29324, 7, 150, 200)

    };

    public static final NPCDrop[] luckyCharmDrops = {

            new NPCDrop(42954, 80.00, 1, 15),

            new NPCDrop(43164, 5.00, 1, 1),

            new NPCDrop(43073, 5.00, 1, 1),

            new NPCDrop(34972, 5.00, 1, 1),

            new NPCDrop(29863, 40.00, 1, 1),

            new NPCDrop(43071, 5.00, 1, 1),

    };

    public static class EliteDungeonTeleport {
        @Getter
        private final String name;
        @Getter
        private final WorldTile location;
        @Getter
        private final int roomIndex;

        public EliteDungeonTeleport(String name, WorldTile location, int roomIndex) {
            this.name = name;
            this.location = location;
            this.roomIndex = roomIndex;
        }

    }

}
