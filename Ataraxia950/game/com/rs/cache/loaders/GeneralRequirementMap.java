package com.rs.cache.loaders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.filestore.io.InputStream;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;

public final class GeneralRequirementMap {

    private static final ConcurrentHashMap<Integer, GeneralRequirementMap> maps = new ConcurrentHashMap<Integer, GeneralRequirementMap>();
    private HashMap<Long, Object> values;
    private int id;

    public static void main(String[] args) throws IOException {
        Cache.init();
        /*
         * for (int i = 0; i < 10000; i++ ){ GeneralRequirementMap map = getMap(i); if
         * (map != null && map.getValues() != null) { map.getValues().forEach((k, v) ->
         * { Logger.getGlobal().info(k + " - " + v); }); } }
         */
        BufferedWriter writer = new BufferedWriter(new FileWriter("GRMAP.txt"));
        for (int i = 0; i < 100000; i++) {
            GeneralRequirementMap map = getMap(i);
            if (map == null || map.getValues() == null)
                continue;
            writer.write(i + " - " + map.getValues());
            writer.newLine();
        }
        writer.close();
        /*
         * for (int i = 0; i < 100000; i++) { GeneralRequirementMap map = getMap(i); if
         * (map == null) continue;
         * 
         * if (map.getIntValue(17648) != 0) Logger.getGlobal().info(i + " - " +
         * map.getValues().toString()); }
         */
    }

    public static final GeneralRequirementMap getMap(int scriptId) {
        GeneralRequirementMap script = maps.get(scriptId);
        if (script != null)
            return script;
        RS3GeneralRequirementMap rs3Map = RS3GeneralRequirementMap.getMap(scriptId);
        script = new GeneralRequirementMap();
        script.id = scriptId;
        script.values = rs3Map.getValues();
        maps.put(scriptId, script);
        return script;

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
        for (Map.Entry<Long, Object> entry : values.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
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
            return 0;
        Object value = values.get(key);
        if (value == null || !(value instanceof Integer))
            return 0;
        return (Integer) value;
    }

    public String getStringValue(long key) {
        if (values == null)
            return "";
        Object value = values.get(key);
        if (value == null || !(value instanceof String))
            return "";
        return (String) value;
    }

    public int getId() {
        return id;
    }
}