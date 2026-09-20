package com.rs.cache.loaders.rs3;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;

public final class RS3ClientScriptMap {

    public int keyType;
    public int valueType;
    private boolean valueTypeUsesId;

    /** Opcode 2 uses the legacy character; opcode 102 uses ScriptVarType's numeric ID. */
    public boolean hasStructValues() { return valueType == (valueTypeUsesId ? 73 : 74); }
    private String defaultStringValue;
    private int defaultIntValue;
    private HashMap<Long, Object> values;

    private static final ConcurrentHashMap<Integer, RS3ClientScriptMap> interfaceScripts = new ConcurrentHashMap<Integer, RS3ClientScriptMap>();

    public static void main(String[] args) throws IOException {

    }

    public static final RS3ClientScriptMap getMap(int scriptId) {
        RS3ClientScriptMap script = interfaceScripts.get(scriptId);
        if (script != null)
            return script;

        script = new RS3ClientScriptMap();

        try {
            com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[17];
            byte[] data = null;

            // Safely verify Index 17 exists before asking for the music/script file
            if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
                data = index.getFile(scriptId >>> 0xba9ed5a8, scriptId & 0xff);
            }

            // Only read the data if we successfully grabbed it
            if (data != null) {
                script.readValueLoop(new InputStream(data));
            }
        } catch (Exception e) {
            // Silently catch any missing files or index out-of-bounds errors.
            // We do not log this because Region loading asks for thousands of these!
        }

        interfaceScripts.put(scriptId, script);
        return script;
    }

    public int getDefaultIntValue() {
        return defaultIntValue;
    }

    public String getDefaultStringValue() {
        return defaultStringValue;
    }

    public HashMap<Long, Object> getValues() {
        return values;
    }

    public Object getValue(long key) {
        if (values == null)
            return null;
        return values.get(key);
    }

    public long getKeyForValue(Object value) {
        for (Long key : values.keySet()) {
            if (values.get(key).equals(value))
                return key;
        }
        return -1;
    }

    public int getSize() {
        if (values == null)
            return 0;
        return values.size();
    }

    public int getIntValue(long key) {
        if (values == null)
            return defaultIntValue;
        Object value = values.get(key);
        if (value == null || !(value instanceof Integer))
            return defaultIntValue;
        return (Integer) value;
    }

    public int getKeyIndex(long key) {
        if (values == null)
            return -1;
        int i = 0;
        for (long k : values.keySet()) {
            if (k == key)
                return i;
            i++;
        }
        return -1;
    }

    public int getIntValueAtIndex(int i) {
        if (values == null)
            return -1;
        return (int) values.values().toArray()[i];
    }

    public String getStringValue(long key) {
        if (values == null)
            return defaultStringValue;
        Object value = values.get(key);
        if (value == null || !(value instanceof String))
            return defaultStringValue;
        return (String) value;
    }

    private void readValueLoop(InputStream stream) {
        for (;;) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    private void readValues(InputStream stream, int opcode) {
        if (opcode == 1) {
            keyType = stream.readByte();// Utils.method2782((byte)
                                        // stream.readByte());
        } else if (opcode == 2) {
            valueType = stream.readByte();// /Utils.method2782((byte)
            valueTypeUsesId = false;
                                          // stream.readByte());
        } else if (opcode == 3)
            defaultStringValue = stream.readString();
        else if (opcode == 4)
            defaultIntValue = stream.readInt();
        else if (opcode == 5 || opcode == 6 || opcode == 7 || opcode == 8) {
            int count = stream.readUnsignedShort();
            int loop = opcode == 7 || opcode == 8 ? stream.readUnsignedShort() : count;
            if (values == null)
                values = new HashMap<Long, Object>(Utils.getHashMapSize(count));
            for (int i = 0; i < loop; i++) {
                int key = opcode == 7 || opcode == 8 ? stream.readUnsignedShort() : stream.readInt();
                Object value = opcode == 5 || opcode == 7 ? stream.readString() : stream.readInt();
                values.put((long) key, value);
            }
        } else if (opcode == 101) {
            keyType = stream.readSmart();
        } else if (opcode == 102) {
            valueType = stream.readSmart();
            valueTypeUsesId = true;
        } else {
            System.err.println("Missing Opcode: " + opcode);
        }
    }

    private RS3ClientScriptMap() {
        defaultStringValue = "null";
    }

}
