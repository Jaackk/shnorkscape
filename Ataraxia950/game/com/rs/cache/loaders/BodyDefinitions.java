package com.rs.cache.loaders;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Logger;

public class BodyDefinitions {

    public static int anInt5276;
    public static int anInt5280;
    public static int[] disabledSlots;
    public static int[] anIntArray5281;
    public static int[] anIntArray5282;

    public static void init() {
        loadBodyDefinitions(); // in case rs stops using it for just this
    }

    public static int getEquipmentContainerSize() {
        return disabledSlots.length;
    }

    /*
     * suposely in rsclient its an instance but its only used for this archive lol
     */
    private static void loadBodyDefinitions() {
        setDefaultsVariableValues();

        com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[28];
        byte[] data = null;

        // 1. Safely check if the index exists and extract the file
        if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
            try {
                data = index.getFile(6);
            } catch (Exception e) {
                data = null;
            }
        }

        // 2. If we found the data, read it normally
        if (data != null) {
            readOpcodeValues(new InputStream(data));
        } else {
            // 3. If missing (921+), skip it and inject a safe fallback to prevent login crashes
            Logger.getGlobal().info("BodyDefinitions: Index 28, File 6 missing. Using safe fallback.");

            // Standard RS equipment size (usually 15-18 slots depending on auras/pocket slots)
            // Initializing this prevents NPEs when checking 'disabledSlots.length' later!
            disabledSlots = new int[18];
            anIntArray5281 = new int[0];
            anIntArray5282 = new int[0];
        }
    }

    private static void setDefaultsVariableValues() {
        anInt5280 = -1;
        anInt5276 = -1;
    }

    private static void readOpcodeValues(InputStream stream) {
        while (true) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private static void readValues(InputStream stream, int opcode) {
        if (opcode == 1) {
            int containerSize = stream.readUnsignedByte();
            disabledSlots = new int[containerSize];
            for (int i_2_ = 0; i_2_ < disabledSlots.length; i_2_++) {
                disabledSlots[i_2_] = stream.readUnsignedByte();
                if (disabledSlots[i_2_] != 0 && disabledSlots[i_2_] != 2) {
                    /* empty */
                }
            }
        } else if (3 == opcode)
            anInt5280 = stream.readUnsignedByte();
        else if (opcode == 4)
            anInt5276 = stream.readUnsignedByte();
        else if (5 == opcode) {
            anIntArray5281 = new int[stream.readUnsignedByte()];
            for (int i_3_ = 0; i_3_ < anIntArray5281.length; i_3_++)
                anIntArray5281[i_3_] = stream.readUnsignedByte();
        } else if (6 == opcode) {
            anIntArray5282 = new int[stream.readUnsignedByte()];
            for (int i_4_ = 0; i_4_ < anIntArray5282.length; i_4_++)
                anIntArray5282[i_4_] = stream.readUnsignedByte();
        }
    }
}