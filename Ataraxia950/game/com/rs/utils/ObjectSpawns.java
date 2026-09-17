package com.rs.utils;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public final class ObjectSpawns {

    private static BufferedReader in;

    public static final void init() {
        if (!new File("data/map/packedSpawns").exists())
            packObjectSpawns();
    }

    public static final void packObjectSpawns() {
        Logger.getGlobal().info(  "Packing object spawns...");
        if (!new File("data/map/packedSpawns").mkdir())
            throw new RuntimeException("Couldn't create packedSpawns directory.");
        try {
            in = new BufferedReader(new FileReader("data/map/unpackedSpawnsList.txt"));
            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;
                if (line.startsWith("//"))
                    continue;
                String[] splitedLine = line.split(" - ");
                if (splitedLine.length != 2)
                    throw new RuntimeException("Invalid Object Spawn line: " + line);
                String[] splitedLine2 = splitedLine[0].split(" ");
                String[] splitedLine3 = splitedLine[1].split(" ");
                if (splitedLine2.length != 3 || splitedLine3.length != 4)
                    throw new RuntimeException("Invalid Object Spawn line: " + line);
                int objectId = Integer.parseInt(splitedLine2[0]);
                int type = Integer.parseInt(splitedLine2[1]);
                int rotation = Integer.parseInt(splitedLine2[2]);

                WorldTile tile = new WorldTile(Integer.parseInt(splitedLine3[0]), Integer.parseInt(splitedLine3[1]), Integer.parseInt(splitedLine3[2]));
                addObjectSpawn(objectId, type, rotation, tile.getRegionId(), tile, Boolean.parseBoolean(splitedLine3[3]));
            }
            in.close();
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    private static final void addObjectSpawn(int objectId, int type, int rotation, int regionId, WorldTile tile, boolean cliped) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream("data/map/packedSpawns/" + regionId + ".os", true));
            out.writeInt(objectId);
            out.writeByte(type);
            out.writeByte(rotation);
            out.writeByte(tile.getPlane());
            out.writeShort(tile.getX());
            out.writeShort(tile.getY());
            out.writeBoolean(cliped);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            Logger.getGlobal().catching(e);
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Add's custom object spawns after world has initialized.
     */
    public static void addCustomSpawns() {
        // singing bowls
        World.spawnObject(new WorldObject(94076, 10, 0, new WorldTile(3591, 3368, 0)), true);
        World.spawnObject(new WorldObject(94076, 10, 0, new WorldTile(2338, 3157, 0)), true);
        // Cooking range at Ports
        World.spawnObject(new WorldObject(81736, 10, 0, new WorldTile(4086, 7269, 0)), true);
        // Stool removal
        World.spawnObject(new WorldObject(3361, 10, 0, new WorldTile(2601, 3420, 0)), true);
        World.spawnObject(new WorldObject(3361, 10, 0, new WorldTile(2644, 5660, 0)), true);

        // Kethsi's world gate
        World.spawnObject(new WorldObject(89742, 10, 0, new WorldTile(2367, 3353, 0)), true);
        World.spawnObject(new WorldObject(89728, 10, 0, 2365, 3351, 0));
        // Lleyta
        World.spawnObject(new WorldObject(55309, 10, 3, new WorldTile(2309, 3172, 0)), true); // interdimensional
                                                                                              // portal
        World.spawnObject(new WorldObject(92120, 10, 0, new WorldTile(2347, 3181, 0)), true); // reaper
                                                                                              // portal
        World.spawnObject(new WorldObject(70765, 10, 0, new WorldTile(2334, 3171, 0)), true); // fire
        World.spawnObject(new WorldObject(70765, 10, 0, new WorldTile(4382, 5918, 0)), true); // fire
        World.spawnObject(new WorldObject(30205, 10, 1, new WorldTile(2354, 3157, 0)), true);
        World.spawnObject(new WorldObject(3166, 10, 1, new WorldTile(2354, 3158, 0)), true);
        World.spawnObject(new WorldObject(112747, 10, 3, new WorldTile(2343, 3172, 1)), true);
        World.spawnObject(new WorldObject(3363, 10, 3, new WorldTile(3873, 6825, 0)), true); // pz agorath head

        // upstairs banks left
        World.spawnObject(new WorldObject(66667, 10, 1, new WorldTile(2348, 3179, 1)), true);
        World.spawnObject(new WorldObject(66666, 10, 1, new WorldTile(2348, 3180, 1)), true);
        World.spawnObject(new WorldObject(66665, 10, 1, new WorldTile(2348, 3181, 1)), true);
        // upstairs banks right
        World.spawnObject(new WorldObject(66667, 10, 1, new WorldTile(2348, 3162, 1)), true);
        World.spawnObject(new WorldObject(66666, 10, 1, new WorldTile(2348, 3163, 1)), true);
        World.spawnObject(new WorldObject(66665, 10, 1, new WorldTile(2348, 3164, 1)), true);
        World.spawnObject(new WorldObject(15477, 10, 0, new WorldTile(2352, 3178, 1)), true);
        // furnace building (downstairs cooking area)
        World.spawnObject(new WorldObject(3044, 10, 3, new WorldTile(2340, 3155, 0)), true);
        World.spawnObject(new WorldObject(2079, 10, 3, new WorldTile(2340, 3180, 0)), true); // ckey
                                                                                             // chest
        World.spawnObject(new WorldObject(31299, 10, 0, new WorldTile(2341, 3181, 0)), true); // ckey
                                                                                              // sign
        World.spawnObject(new WorldObject(8772, 10, 2, new WorldTile(2335, 3157, 0)), true); // bench
        World.spawnObject(new WorldObject(3166, 10, 2, new WorldTile(2334, 3157, 0)), true); // nulls
                                                                                             // to
                                                                                             // take
                                                                                             // up
                                                                                             // space
        World.spawnObject(new WorldObject(3166, 10, 2, new WorldTile(2336, 3157, 0)), true); // nulls
                                                                                             // to
                                                                                             // take
                                                                                             // up
                                                                                             // space

        World.spawnObject(new WorldObject(113258, 10, 1, new WorldTile(2336, 3158, 0)), true); // anvil
        World.spawnObject(new WorldObject(113258, 10, 1, new WorldTile(3889, 6813, 0)), true);
        World.spawnObject(new WorldObject(113258, 10, 1, new WorldTile(2334, 3158, 0)), true); // anvil
        World.spawnObject(new WorldObject(24016, 10, 1, new WorldTile(2334, 3157, 0)), true);

        // new home area - X: 5024, Y: 736, Z: 0

        // crates
        World.spawnObject(new WorldObject(34586, 10, 1, new WorldTile(5025, 753, 1)), true);
        World.spawnObject(new WorldObject(34586, 10, 1, new WorldTile(5024, 753, 1)), true);
        World.spawnObject(new WorldObject(34586, 10, 1, new WorldTile(5023, 753, 1)), true);
        World.spawnObject(new WorldObject(34586, 10, 1, new WorldTile(5022, 753, 1)), true);
        World.spawnObject(new WorldObject(34586, 10, 1, new WorldTile(5021, 753, 1)), true);

        // gim stuff
        World.spawnObject(new WorldObject(3192, 10, 1, new WorldTile(5021, 751, 1)), true);
        World.spawnObject(new WorldObject(54408, 10, 2, new WorldTile(5024, 752, 1)), true);

        // thieving stalls & Bank
        World.spawnObject(new WorldObject(4878, 10, 0, new WorldTile(5025, 719, 1)), true);
        World.spawnObject(new WorldObject(4877, 10, 0, new WorldTile(5024, 719, 1)), true);
        World.spawnObject(new WorldObject(4876, 10, 0, new WorldTile(5023, 719, 1)), true);
        World.spawnObject(new WorldObject(4875, 10, 0, new WorldTile(5022, 719, 1)), true);
        World.spawnObject(new WorldObject(4874, 10, 0, new WorldTile(5021, 719, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5016, 715, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5016, 716, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5016, 717, 1)), true);

        // Misc Objects

        World.spawnObject(new WorldObject(79036, 10, 2, new WorldTile(5023, 726, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 0, new WorldTile(5012, 747, 1)), true);
        World.spawnObject(new WorldObject(2079, 10, 3, new WorldTile(5011, 745, 1)), true);
        World.spawnObject(new WorldObject(31299, 10, 3, new WorldTile(5011, 744, 1)), true);
        World.spawnObject(new WorldObject(2403, 10, 3, new WorldTile(5013, 745, 1)), true);
        World.spawnObject(new WorldObject(15477, 10, 1, new WorldTile(5020, 745, 1)), true);
        World.spawnObject(new WorldObject(70765, 10, 0, new WorldTile(5023, 729, 1)), true);
        World.spawnObject(new WorldObject(100967, 10, 2, new WorldTile(5040, 734, 1)), true);
        World.spawnObject(new WorldObject(112747, 10, 0, new WorldTile(5031, 735, 1)), true);
        World.spawnObject(new WorldObject(92120, 10, 0, new WorldTile(5007, 733, 1)), true);
        World.spawnObject(new WorldObject(3044, 10, 1, new WorldTile(5039, 717, 1)), true);
        World.spawnObject(new WorldObject(113258, 10, 0, new WorldTile(5038, 716, 1)), true);
        World.spawnObject(new WorldObject(93249, 10, 0, new WorldTile(5026, 746, 1)), true);
        World.spawnObject(new WorldObject(30205, 10, 1, new WorldTile(5026, 740, 1)), true);
        World.spawnObject(new WorldObject(51061, 10, 3, new WorldTile(5008, 735, 1)), true);
        World.spawnObject(new WorldObject(7092, 10, 1, new WorldTile(5006, 716, 1)), true);
        World.spawnObject(new WorldObject(38669, 10, 0, new WorldTile(5007, 719, 1)), true);

        // Misc Banks

        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5040, 725, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5040, 726, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5040, 727, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5040, 728, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5040, 729, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5006, 725, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5006, 726, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5006, 727, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5006, 728, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5006, 729, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5041, 708, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5041, 709, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5041, 710, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5041, 711, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5041, 712, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5005, 708, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5005, 709, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5005, 710, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5005, 711, 1)), true);
        World.spawnObject(new WorldObject(36786, 10, 1, new WorldTile(5005, 712, 1)), true);

        World.spawnObject(new WorldObject(12666, 10, 0, new WorldTile(5021, 819, 1)), true);

        // thieving stalls upstairs
        World.spawnObject(new WorldObject(4874, 10, 1, new WorldTile(2352, 3165, 1)), true);
        World.spawnObject(new WorldObject(4875, 10, 1, new WorldTile(2353, 3165, 1)), true);
        World.spawnObject(new WorldObject(4876, 10, 1, new WorldTile(2354, 3165, 1)), true);
        World.spawnObject(new WorldObject(4877, 10, 1, new WorldTile(2355, 3165, 1)), true);
        World.spawnObject(new WorldObject(4878, 10, 1, new WorldTile(2356, 3165, 1)), true);
        // shooting star stuff
        World.spawnObject(new WorldObject(38669, 10, 3, new WorldTile(2323, 3162, 1)), true);
        World.spawnObject(new WorldObject(7092, 10, 0, new WorldTile(2326, 3161, 1)), true);
        // Ashdale
        World.spawnObject(new WorldObject(100248, 10, 3, new WorldTile(2495, 2722, 2)), true); // bank
                                                                                               // booth
        World.spawnObject(new WorldObject(100247, 10, 3, new WorldTile(2501, 2722, 2)), true); // bank
                                                                                               // booth
        World.spawnObject(new WorldObject(76274, 10, 3, new WorldTile(2500, 2722, 2)), true); // bank
                                                                                              // booth
        World.spawnObject(new WorldObject(76274, 10, 3, new WorldTile(2499, 2722, 2)), true); // bank
                                                                                              // booth
        World.spawnObject(new WorldObject(76274, 10, 3, new WorldTile(2498, 2722, 2)), true); // bank
                                                                                              // booth
        World.spawnObject(new WorldObject(76274, 10, 3, new WorldTile(2497, 2722, 2)), true); // bank
                                                                                              // booth
        World.spawnObject(new WorldObject(76274, 10, 3, new WorldTile(2496, 2722, 2)), true); // bank
                                                                                              // booth
                                                                                              // //Player
                                                                                              // ports
        World.spawnObject(new WorldObject(72695, 10, 0, new WorldTile(4070, 7279, 0)), true); // port
                                                                                              // portal
        World.spawnObject(new WorldObject(93249, 10, 1, new WorldTile(2332, 3165, 0)), true); // port
                                                                                              // portal
        World.spawnObject(new WorldObject(70765, 10, 0, new WorldTile(2334, 3172, 0)), true);

        World.spawnObject(new WorldObject(2403, 10, 2, new WorldTile(3593, 3377, 0)), true);
        World.spawnObject(new WorldObject(2403, 10, 2, new WorldTile(2340, 3160, 1)), true);
        
        /**
         * karamja jungle mine (north of shilo village)
         * */
        World.spawnObject(new WorldObject(113050, 11, 3, new WorldTile(2850, 3039, 0)), true);
        World.spawnObject(new WorldObject(113051, 11, 0, new WorldTile(2851, 3038, 0)), true);
        World.spawnObject(new WorldObject(113055, 11, 3, new WorldTile(2846, 3038, 0)), true);
        World.spawnObject(new WorldObject(113053, 11, 2, new WorldTile(2848, 3036, 0)), true);
        World.spawnObject(new WorldObject(113038, 11, 3, new WorldTile(2844, 3036, 0)), true);
        World.spawnObject(new WorldObject(113041, 11, 2, new WorldTile(2846, 3033, 0)), true);
        World.spawnObject(new WorldObject(113044, 11, 2, new WorldTile(2847, 3032, 0)), true);
        
        /**
         * Mining guild dung short cut
         * */
        World.spawnObject(new WorldObject(113050, 11, 3, new WorldTile(1049, 4520, 0)), true);
        World.spawnObject(new WorldObject(113051, 11, 2, new WorldTile(1051, 4516, 0)), true);
        World.spawnObject(new WorldObject(113052, 11, 2, new WorldTile(1050, 4513, 0)), true);
        World.spawnObject(new WorldObject(113054, 11, 2, new WorldTile(1051, 4511, 0)), true);
        World.spawnObject(new WorldObject(113065, 11, 2, new WorldTile(1052, 4510, 0)), true);
        World.spawnObject(new WorldObject(113050, 11, 2, new WorldTile(1058, 4514, 0)), true);
        World.spawnObject(new WorldObject(113054, 11, 2, new WorldTile(1057, 4511, 0)), true);
        World.spawnObject(new WorldObject(113054, 11, 2, new WorldTile(1061, 4515, 0)), true);
        World.spawnObject(new WorldObject(113055, 11, 2, new WorldTile(1060, 4511, 0)), true);
        /**
         * Mining guild
         * */
        World.spawnObject(new WorldObject(113052, 11, 3, new WorldTile(3048, 9735, 0)), true);
        World.spawnObject(new WorldObject(113051, 11, 2, new WorldTile(3048, 9738, 0)), true);
        World.spawnObject(new WorldObject(113043, 11, 2, new WorldTile(3042, 9736, 0)), true);
        World.spawnObject(new WorldObject(113051, 11, 2, new WorldTile(3041, 9735, 0)), true);
        World.spawnObject(new WorldObject(113043, 11, 2, new WorldTile(3040, 9732, 0)), true);
        
        World.spawnObject(new WorldObject(113041, 11, 3, new WorldTile(3030, 9733, 0)), true);
        World.spawnObject(new WorldObject(113043, 11, 2, new WorldTile(3029, 9736, 0)), true);
        World.spawnObject(new WorldObject(113043, 11, 2, new WorldTile(3031, 9742, 0)), true);
        World.spawnObject(new WorldObject(113042, 11, 2, new WorldTile(3036, 9741, 0)), true);
        World.spawnObject(new WorldObject(113042, 11, 2, new WorldTile(3035, 9735, 0)), true);
        World.spawnObject(new WorldObject(113042, 11, 2, new WorldTile(3034, 9733, 0)), true);
        
        World.spawnObject(new WorldObject(113052, 11, 2, new WorldTile(3036, 9761, 0)), true);
        World.spawnObject(new WorldObject(113050, 11, 2, new WorldTile(3036, 9764, 0)), true);
        World.spawnObject(new WorldObject(113051, 11, 2, new WorldTile(3040, 9762, 0)), true);
        /** Sanguine District */

        /*
         * World.spawnObject(new WorldObject(65958, 10, 1, new WorldTile(2339, 3171,
         * 0)), true); World.spawnObject(new WorldObject(65958, 10, 1, new
         * WorldTile(2649, 5670, 0)), true); World.spawnObject(new WorldObject(65958,
         * 10, 1, new WorldTile(5023, 734, 1)), true);
         */

        /*
         * object to be removed home area World.spawnObject(new WorldObject(3361, 10, 0,
         * new WorldTile(2354, 3173, 0))); World.spawnObject(new WorldObject(3361, 10,
         * 0, new WorldTile(2354, 3172, 0))); World.spawnObject(new WorldObject(3361,
         * 10, 0, new WorldTile(2355, 3174, 0))); World.spawnObject(new
         * WorldObject(3361, 10, 0, new WorldTile(2351, 3174, 0)));
         * World.spawnObject(new WorldObject(3361, 10, 0, new WorldTile(2350, 3174,
         * 0))); World.spawnObject(new WorldObject(3361, 10, 0, new WorldTile(2351,
         * 3170, 0))); World.spawnObject(new WorldObject(3361, 10, 0, new
         * WorldTile(2351, 3169, 0))); World.spawnObject(new WorldObject(3361, 10, 0,
         * new WorldTile(2351, 3168, 0))); World.spawnObject(new WorldObject(3361, 10,
         * 0, new WorldTile(2354, 3169, 0))); World.spawnObject(new WorldObject(3361,
         * 10, 0, new WorldTile(2355, 3169, 0))); World.spawnObject(new
         * WorldObject(3361, 10, 0, new WorldTile(2354, 3170, 0)));
         * World.spawnObject(new WorldObject(3361, 10, 0, new WorldTile(2355, 3171,
         * 0)));
         */
    }
}