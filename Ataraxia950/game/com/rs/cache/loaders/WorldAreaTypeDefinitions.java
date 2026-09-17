package com.rs.cache.loaders;

import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class WorldAreaTypeDefinitions {
    private static final ConcurrentHashMap<Integer, WorldAreaTypeDefinitions> WorldAreaTypesCached = new ConcurrentHashMap<Integer, WorldAreaTypeDefinitions>();
    public static final ConcurrentHashMap<Integer, Integer> Mapped = new ConcurrentHashMap<Integer, Integer>();
    public static final ConcurrentHashMap<Integer, int[]> LOOK_UP = new ConcurrentHashMap<Integer, int[]>();
    public static final int[] defaultMapped = new int[64];

    public static void init() {
        for (int i = 0; i < defaultMapped.length; i++)
            defaultMapped[i] = 1;
        Mapped.clear();
        LOOK_UP.clear();

        // 1. BULLETPROOF CHECK FOR INDEX 2
        com.rs.cache.filestore.store.Index index2 = Cache.STORE.getIndexes()[2];

        if (index2 != null && index2.getTable() != null && index2.getTable().getArchives() != null
                && index2.getTable().getArchives().length > 83 && index2.getTable().getArchives()[83] != null) {

            for (int fileId : index2.getTable().getArchives()[83].getValidFileIds()) {
                WorldAreaTypeDefinitions defs = WorldAreaTypeDefinitions.getWorldAreaTypeDefinitions(fileId);
                if (defs == null)
                    continue;
                Mapped.put(defs.getKey(), defs.getId());
            }
        } else {
            Logger.getGlobal().info("WorldAreaTypeDefinitions: Index 2, Archive 83 missing (Normal for 921+). Skipping.");
        }

        int count = 0;

        // 2. BULLETPROOF CHECK FOR INDEX 23
        com.rs.cache.filestore.store.Index index23 = Cache.STORE.getIndexes()[23];

        if (index23 != null && index23.getTable() != null && index23.getTable().getArchives() != null
                && index23.getTable().getArchives().length > 3 && index23.getTable().getArchives()[3] != null) {

            for (int fileId : index23.getTable().getArchives()[3].getValidFileIds()) {
                byte[] data = index23.getFile(3, fileId);
                if (data == null)
                    continue;
                ByteBuf buf = Unpooled.wrappedBuffer(data);
                int[] hashes = new int[64];
                int previous = 0;
                int lastArea = 0;

                while (true) {
                    int area = 0;
                    int current = 64;

                    if (buf.isReadable()) {
                        area = buf.readUnsignedMedium();

                        if (buf.isReadable()) {
                            lastArea = area;
                            current = previous + buf.readUnsignedByte();
                        } else {
                            current = 64;
                            lastArea = area;
                        }
                    }

                    if (current > previous) {
                        do {
                            hashes[previous++] = area;
                        } while (current != previous);
                    }

                    if (current > 0x3F)
                        break;
                }

                for (int i = 0; i < 64; i++)
                    if (hashes[i] == 0)
                        hashes[i] = lastArea;

                int[] mapped = new int[64];
                for (int i = 0; i < 64; i++) {
                    mapped[i] = Mapped.getOrDefault(hashes[i], 1);
                }
                LOOK_UP.put(fileId, mapped);
                count++;
            }
        } else {
            Logger.getGlobal().info("WorldAreaTypeDefinitions: Index 23, Archive 3 missing. Skipping.");
        }

        Logger.getGlobal().info("Loaded region keys for " + count + " regions");
    }

    public static WorldAreaTypeDefinitions getWorldAreaTypeDefinitions(int id) {
        WorldAreaTypeDefinitions defs = WorldAreaTypesCached.get(id);
        if (defs != null)// open new txt document
            return defs;
        defs = new WorldAreaTypeDefinitions();
        defs.id = id;
        byte[] data = Cache.STORE.getIndexes()[2].getFile(83, id);
        if (data != null) {
            defs.readValueLoop(new InputStream(data));
            WorldAreaTypesCached.put(id, defs);
            return defs;
        }
        return null;
    }

    @Getter
    private int id, key;

    private void readValueLoop(InputStream stream) {
        for (;;) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private void readValues(InputStream stream, int opcode) {
        if (opcode == 2) {
            key = stream.read24BitInt();
        } else if (opcode == 3) {
            int[] area = new int[2];
            area[0] = stream.readInt();
            area[1] = stream.readInt();
        } else if (opcode == 4) {
            int[] area = new int[2];
            area[0] = stream.readInt();
            area[1] = stream.readInt();
        } else {
            System.out.println("missing opcode=" + opcode);
        }
    }

}
