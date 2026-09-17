package com.rs.utils.data.parsers.npcs;

import com.rs.external.api.json.JsonParser;
import com.rs.utils.DataPaths;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Logger;
import com.rs.utils.data.parsers.npcs.pojos.NPCSpawn;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class NPCSpawnsDataParser {
    /** Relative to DataPaths.root(); resolved when init() runs so the 947 JVM can point at the staged tree. */
    private static final String SPAWNS_FILE_PATH = "npcs/spawns.json";
    private static final Int2ObjectOpenHashMap<ArrayList<NPC>> NPC_SPAWNS = new Int2ObjectOpenHashMap<>();
    /**
     * M4: the same rows as {@link #NPC_SPAWNS}, as plain data instead of legacy {@link NPC}
     * entities. The native 947 world builds its NPCs through {@code NPC.createNative950},
     * which reads no 910 definitions, no combat data and no respawn settings, so it cannot
     * reuse the prototype objects the legacy region loader clones - and it must filter every
     * row through the id validity table before anything is constructed at all.
     */
    private static final Int2ObjectOpenHashMap<ArrayList<Native950Spawn>> NATIVE_947_SPAWNS = new Int2ObjectOpenHashMap<>();
    /** Every distinct 910 npc id the spawn file uses, for the validity report. */
    private static final TreeSet<Integer> NATIVE_947_SPAWN_IDS = new TreeSet<>();

    /**
     * One row of {@code npcs/spawns.json}, keyed by its <b>910</b> npc id. The id is not
     * trusted: {@code Native950World} refuses every id the validity table does not call
     * {@code same} before this ever becomes an entity.
     */
    public static final class Native950Spawn {
        /** 910 npc id, exactly as the data file wrote it. */
        public final int npcId;
        public final int x, y, plane;
        /**
         * Spawn facing in Ataraxia's 1/16384-turn space (the file's compass enum, or the
         * definition's respawn direction when the row does not name one).
         */
        public final int direction;
        /** False for the 35 rows the file freezes in place; those never wander. */
        public final boolean canMove;

        Native950Spawn(int npcId, int x, int y, int plane, int direction, boolean canMove) {
            this.npcId = npcId; this.x = x; this.y = y; this.plane = plane;
            this.direction = direction; this.canMove = canMove;
        }

        public int regionId() { return ((x >> 6) << 8) + (y >> 6); }

        @Override public String toString() {
            return "spawn[npc=" + npcId + " at " + x + "," + y + "," + plane
                    + " dir=" + direction + (canMove ? "" : " frozen") + "]";
        }
    }

    public static void init() {
        loadNPCSpawns();
    }

    /**
     * The spawn rows of one region as plain data, never null. The native world calls this on
     * the world thread the first time a player loads the region.
     */
    public static List<Native950Spawn> native947Spawns(int regionId) {
        ArrayList<Native950Spawn> spawns = NATIVE_947_SPAWNS.get(regionId);
        return spawns == null ? Collections.<Native950Spawn>emptyList() : Collections.unmodifiableList(spawns);
    }

    /** Distinct 910 npc ids across the whole spawn file. */
    public static List<Integer> native947SpawnIds() {
        return Collections.unmodifiableList(new ArrayList<Integer>(NATIVE_947_SPAWN_IDS));
    }

    /** Regions the spawn file has at least one row for. */
    public static int native947SpawnRegions() { return NATIVE_947_SPAWNS.size(); }

    public static void loadNPCSpawns(int regionId) {
        ArrayList<NPC> npcs = NPC_SPAWNS.get(regionId);
        if (npcs != null) {
            for (NPC npc : npcs) {
                NPC newNpc = World.spawnNPC(npc);
                if (npc.spawnFrozen) {
                    newNpc.setFreezeDelay(Integer.MAX_VALUE);
                    newNpc.setRandomWalk(0);
                    newNpc.setCannotMove(true);
                }
                newNpc.setDirection(npc.getSpawnDirection());
            }
        }
    }

    private static void loadNPCSpawns() {
        JsonParser parser = new JsonParser(DataPaths.resolve(SPAWNS_FILE_PATH), NPCSpawn[].class);
        NPCSpawn[] spawns = parser.getFileLoaded();
        int count = 0;
        for (NPCSpawn spawn : spawns) {
            WorldTile tile = new WorldTile(spawn.getLocation().getX(), spawn.getLocation().getY(), spawn.getLocation().getZ());
            NPC npc = new NPC(spawn.getNpcId(), tile, spawn.getMapAreaNameHash(), spawn.isCanBeAttackedFromOutOfArea(), false, false);
            if (spawn.getDirection() != null) {
                npc.setSpawnDirection(spawn.getDirection().getValue());
            } else {
                npc.setSpawnDirection(npc.getRespawnDirection());
            }
            if (!spawn.isCanMove()) {
                npc.spawnFrozen = true;
                npc.setFreezeDelay(Integer.MAX_VALUE);
                npc.setRandomWalk(0);
                npc.setCannotMove(true);
            }
            if (NPC_SPAWNS.get(tile.getRegionId()) == null) {
                NPC_SPAWNS.put(tile.getRegionId(), new ArrayList<>());
            }
            NPC_SPAWNS.get(tile.getRegionId()).add(npc);
            // M4: the same row as plain data for the native world. Built from the prototype
            // rather than re-read from the JSON so both tables can never disagree about a
            // tile or a facing, and the direction default stays the legacy one.
            if (NATIVE_947_SPAWNS.get(tile.getRegionId()) == null) {
                NATIVE_947_SPAWNS.put(tile.getRegionId(), new ArrayList<>());
            }
            NATIVE_947_SPAWNS.get(tile.getRegionId()).add(new Native950Spawn(spawn.getNpcId(),
                    tile.getX(), tile.getY(), tile.getPlane(), npc.getSpawnDirection(), spawn.isCanMove()));
            NATIVE_947_SPAWN_IDS.add(spawn.getNpcId());
            count++;
        }
        Logger.getGlobal().info("Loaded " + count + " NPC spawns.");
    }

   /* public static void main(String[] args) throws IOException {
        Cache.init();
        NPCSpawns.init();
        final File fileDir = new File("data/npcs/packedSpawns");
        try (
                Writer writer = new FileWriter("Output.json")) {
            writer.write("[\n");
            for (final File file : fileDir.listFiles()) {
                try {
                    final RandomAccessFile in = new RandomAccessFile(file, "r");
                    final FileChannel channel = in.getChannel();
                    final ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    val definitionsSize = Utils.getNPCDefinitionsSize();
                    while (buffer.hasRemaining()) {
                        final int npcId = buffer.getShort() & 0xffff;
                        final int plane = buffer.get() & 0xff;
                        final int x = buffer.getShort() & 0xffff;
                        final int y = buffer.getShort() & 0xffff;
                        final boolean hashExtraInformation = buffer.get() == 1;
                        int mapAreaNameHash = -1;
                        boolean canBeAttackFromOutOfArea = true;
                        int direction = NPCDirection.NORTHWEST.getValue();
                        final NPCDefinitions definitions = NPCDefinitions.getNPCDefinitions(npcId);
                        if (definitions != null) {
                            if (definitions.contrast << 32 != 0 && definitions.respawnDirection > 0 && definitions.respawnDirection <= 8) {
                                direction = (4 + definitions.respawnDirection) << 11;
                            }
                        }
                        NPCDirection direction1 = NPCDirection.getDirectionForValue(direction);
                        if (hashExtraInformation) {
                            mapAreaNameHash = buffer.getInt();
                            canBeAttackFromOutOfArea = buffer.get() == 1;
                        }
                        if (npcId < 0 || npcId > definitionsSize) {
                            continue;
                        }
                        writer.write("\t{\n");
                        writer.write("\t\t\"npcId\": " + npcId + ",\n");
                        if (direction1 != NPCDirection.NORTHWEST) {
                            writer.write("\t\t\"direction\": \"" + direction1.toString() +"\",\n");
                        }
                        writer.write("\t\t\"location\": {\n");
                        writer.write("\t\t\t\"x\": " + x + ",\n" +
                                "\t\t\t\"y\": " + y + ",\n" +
                                "\t\t\t\"z\": " + plane + "\n");
                        if (mapAreaNameHash != -1 || !canBeAttackFromOutOfArea) {
                            writer.write("\t\t},\n");
                        } else {
                            writer.write("\t\t}\n");
                        }
                        if (mapAreaNameHash != -1) {
                            if (!canBeAttackFromOutOfArea) {
                                writer.write("\t\t\"mapAreaNameHash\": " + mapAreaNameHash + ",\n");
                            } else {
                                writer.write("\t\t\"mapAreaNameHash\": " + mapAreaNameHash + "\n");
                            }
                        }
                        if (!canBeAttackFromOutOfArea) {
                            writer.write("\t\t\"canBeAttackedFromOutOfArea\": " + canBeAttackFromOutOfArea + "\n");
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
