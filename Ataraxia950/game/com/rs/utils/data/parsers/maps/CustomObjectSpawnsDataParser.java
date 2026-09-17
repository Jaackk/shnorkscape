package com.rs.utils.data.parsers.maps;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.maps.pojos.CustomObjectSpawn;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;

public class CustomObjectSpawnsDataParser {
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String SPAWNS_FILE_PATH = "map/customObjectSpawns.json";
    private static final Int2ObjectOpenHashMap<ArrayList<WorldObject>> OBJECT_SPAWNS = new Int2ObjectOpenHashMap<>();

    public static void init() {
        loadObjectSpawns();
    }

    private static void loadObjectSpawns() {
        JsonParser parser = new JsonParser(DataPaths.resolve(SPAWNS_FILE_PATH), CustomObjectSpawn[].class);
        CustomObjectSpawn[] spawns = parser.getFileLoaded();
        int count = 0;
        for (CustomObjectSpawn spawn : spawns) {
            WorldTile tile = new WorldTile(spawn.getLocation().getX(), spawn.getLocation().getY(), spawn.getLocation().getZ());
            WorldObject object = new WorldObject(spawn.getObjectId(), spawn.getType().getValue(),
                    spawn.getRotation().getValue(), tile.getX(), tile.getY(), tile.getPlane());
            if (OBJECT_SPAWNS.get(tile.getRegionId()) == null) {
                OBJECT_SPAWNS.put(tile.getRegionId(), new ArrayList<>());
            }
            OBJECT_SPAWNS.get(tile.getRegionId()).add(object);
            count++;
        }
        Logger.getGlobal().info("Loaded " + count + " custom object spawns.");
    }

    public static void loadObjectSpawns(int regionId) {
        ArrayList<WorldObject> objects = OBJECT_SPAWNS.get(regionId);
        if (objects != null) {
            for (WorldObject object : objects) {
                World.spawnObject(object);
            }
        }
    }

    /*public static void main(String[] args) throws IOException {
        Cache.init();
        ObjectSpawns.init();
        final File fileDir = new File("data/map/packedSpawns");
        try (Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            for (final File file : fileDir.listFiles()) {
                try {
                    final RandomAccessFile in = new RandomAccessFile(file, "r");
                    final FileChannel channel = in.getChannel();
                    final ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    val definitionsSize = Utils.getNPCDefinitionsSize();
                    while (buffer.hasRemaining()) {
                        int objectId = buffer.getInt() & 0xffffff;
                        int type = buffer.get() & 0xff;
                        int rotation = buffer.get() & 0xff;
                        int plane = buffer.get() & 0xff;
                        int x = buffer.getShort() & 0xffff;
                        int y = buffer.getShort() & 0xffff;
                        @SuppressWarnings("unused")
                        boolean cliped = buffer.get() == 1;

                        if (ObjectRotation.getRotationFromValue(rotation) == null) {
                            Logger.getGlobal().info(objectId + " " + type + " " + rotation + " - " + x + " " + y + " " + plane + " " + cliped);
                        }

                        writer.write("\t{\n");
                        writer.write("\t\t\"objectId\": " + objectId + ",\n");
                        if (ObjectType.getTypeFromValue(type) != ObjectType.REGULAR) {
                            writer.write("\t\t\"type\": \"" + ObjectType.getTypeFromValue(type).toString() + "\",\n");
                        }
                        writer.write("\t\t\"rotation\": \"" + ObjectRotation.getRotationFromValue(rotation).toString() + "\",\n");
                        writer.write("\t\t\"location\": {\n");
                        writer.write("\t\t\t\"x\": " + x + ",\n" +
                                "\t\t\t\"y\": " + y + ",\n" +
                                "\t\t\t\"z\": " + plane + "\n");
                        if (!cliped) {
                            writer.write("\t\t},\n");
                        } else {
                            writer.write("\t\t}\n");
                        }
                        if (!cliped) {
                            writer.write("\t\t\"isClipped\": " + cliped + "\n");
                        }
                        writer.write("\t},\n");
                    }
                    channel.close();
                    in.close();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }

            writer.write("]");
        } catch (
                IOException e) {
            Logger.getGlobal().catching(e);
        }
    }*/
}
