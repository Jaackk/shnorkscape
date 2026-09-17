package com.rs.cache.loaders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.loaders.rs3.RS3ClientScriptMap;

public final class ClientScriptMap {

    private static final ConcurrentHashMap<Integer, ClientScriptMap> interfaceScripts = new ConcurrentHashMap<Integer, ClientScriptMap>();
    public int keyType;
    public int valueType;
    private String defaultStringValue;
    private int defaultIntValue;
    private HashMap<Long, Object> values;

    private ClientScriptMap() {
        defaultStringValue = "null";
    }

    public static void main(String[] args) throws IOException {
        Cache.init();

        BufferedWriter writer = new BufferedWriter(new FileWriter("CSMAP.txt"));
        for (int key = 0; key < 100000; key++) {
            ClientScriptMap map = ClientScriptMap.getMap(key);
            if (map == null || map.values == null)
                continue;

            writer.write(key + " - " + map.values);
            writer.newLine();
        }

        writer.close();

        /**
         * MusicHints.init(); ClientScriptMap names = ClientScriptMap.getMap(1345);
         * ClientScriptMap hint1 = ClientScriptMap.getMap(952);
         * Logger.getGlobal().info(hint1); for (Object v : names.values.values()) { int
         * key = (int) ClientScriptMap.getMap(1345).getKeyForValue(v); int id =
         * ClientScriptMap.getMap(1351).getIntValue(key); String hint =
         * MusicHints.getHint(id); Logger.getGlobal().info(id + ", " + v + "; " + hint +
         * ", "); }
         */

        /** Disabled just incase. **/
    }

    public static final ClientScriptMap getMap(int scriptId) {
        ClientScriptMap script = interfaceScripts.get(scriptId);
        if (script != null)
            return script;
        RS3ClientScriptMap rs3Map = RS3ClientScriptMap.getMap(scriptId);
        script = new ClientScriptMap();
        script.keyType = rs3Map.keyType;
        script.defaultIntValue = rs3Map.getDefaultIntValue();
        script.valueType = rs3Map.valueType;
        script.defaultStringValue = rs3Map.getDefaultStringValue();
        script.values = rs3Map.getValues();
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
        // Safely check if the map is empty/missing before trying to loop through it
        if (values == null) {
            return -1;
        }

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

    public Integer[] getDuplicates(int object) {
        List<Integer> duplicates = new ArrayList<Integer>();
        for (Entry<Long, Object> e : values.entrySet()) {
            if (!(e.getValue() instanceof Integer))
                continue;
            if ((int) e.getValue() == object)
                duplicates.add(e.getKey().intValue());
        }
        return duplicates.toArray(new Integer[duplicates.size()]);
    }

}
