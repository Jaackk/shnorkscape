package com.rs.game.player.client.ui;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.VarBitDefinitions;

/**
 * The narrow cache surface {@link Native950Bindings} validates against.
 *
 * Everything the binding table needs from the 950 flat cache is a reference-table
 * question (does a group/file exist, how many files does a group hold), a file
 * digest for SHA-256 pinning, or an enum/struct/varbit lookup through the existing
 * RS3 loaders. Keeping that behind one interface lets the JUnit tests validate a
 * table against a scripted fake without the 22 GB cache, exactly as the Kotlin
 * {@code Native950CacheContent.verifyFile(readFile)} tests do.
 */
public interface Native950CacheReader {

    /** Index 3 interface groups, index 12 scripts, index 2 archives. */
    boolean groupExists(int index, int group);

    /** Number of valid files in a group, or -1 when the group is absent. */
    int fileCount(int index, int group);

    boolean fileExists(int index, int group, int file);

    /** Lower-case hex SHA-256 of one logical file, or null when absent. */
    String sha256(int index, int group, int file);

    /** Integer enum value for a key (index 17), or -1 when the key or the enum is missing. */
    int enumInt(int enumId, int key);

    /** Integer struct param (index 22), or -1 when the param or the struct is missing. */
    int structInt(int structId, int param);

    /** Decoded varbit {baseVar, startBit, endBit} from 2/69, or null when the file is absent. */
    int[] varbit(int varbitId);

    /**
     * Reader over {@link Cache#STORE} for the read-only flat 950 cache. Every method
     * is a plain lookup; nothing here writes, and a missing index answers "absent"
     * rather than throwing so the loader can produce one actionable message.
     */
    final class Flat implements Native950CacheReader {
        private static Index index(int id) {
            if (Cache.STORE == null || Cache.STORE.getIndexes() == null) return null;
            Index[] indexes = Cache.STORE.getIndexes();
            return id >= 0 && id < indexes.length ? indexes[id] : null;
        }

        @Override public boolean groupExists(int index, int group) {
            Index i = index(index);
            return i != null && i.archiveExists(group);
        }

        @Override public int fileCount(int index, int group) {
            Index i = index(index);
            return i == null ? -1 : i.getValidFilesCount(group);
        }

        @Override public boolean fileExists(int index, int group, int file) {
            Index i = index(index);
            return i != null && i.fileExists(group, file);
        }

        @Override public String sha256(int index, int group, int file) {
            Index i = index(index);
            byte[] data = i == null ? null : i.getFile(group, file);
            return data == null ? null : Native950Bindings.sha256(data);
        }

        @Override public int enumInt(int enumId, int key) {
            if (!fileExists(17, enumId >>> 8, enumId & 0xff)) return -1;
            Object value = ClientScriptMap.getMap(enumId).getValue(key);
            return value instanceof Integer ? (Integer) value : -1;
        }

        @Override public int structInt(int structId, int param) {
            if (!fileExists(22, structId / 32, structId & 31)) return -1;
            Object value = GeneralRequirementMap.getMap(structId).getValue(param);
            return value instanceof Integer ? (Integer) value : -1;
        }

        @Override public int[] varbit(int varbitId) {
            if (!fileExists(2, 69, varbitId)) return null;
            VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(varbitId);
            return new int[] { defs.baseVar, defs.startBit, defs.endBit };
        }
    }
}
