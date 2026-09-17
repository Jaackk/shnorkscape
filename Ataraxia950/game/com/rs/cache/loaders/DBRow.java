package com.rs.cache.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ParamTypeDefinitions.ScriptVarType;
import com.rs.network.io.InputStream;

import lombok.Getter;

public class DBRow {

    private static final ConcurrentHashMap<Integer, DBRow> DBRows = new ConcurrentHashMap<Integer, DBRow>();
    @Getter
    public Object[][] data;
    public ScriptVarType[][] dataScriptVarTypes;

    public static void main(String[] args) throws IOException {
        Cache.init();
        DBRow dbrow = DBRow.getDBRow(1300);
        int length = dbrow.data.length;
        for (int i = 0; i < length; i++) {
            if (dbrow.data[i] == null)
                continue;

            for (int j = 0; j < dbrow.data[i].length; j++) {
                System.out.println("i=" + i + ", j=" + j + ", data=" + dbrow.data[i][j] + ", type="
                        + dbrow.dataScriptVarTypes[i][j % dbrow.dataScriptVarTypes[i].length]
                                .formatForDisplay(dbrow.data[i][j]));
            }
        }
        System.out.println(GeneralRequirementMap.getMap(4195).getValues());
    }

    public static final DBRow getDBRow(int rowId) {
        DBRow def = DBRows.get(rowId);
        if (def != null && def.data != null)
            return def;
        def = new DBRow();
        byte[] data = Cache.STORE.getIndexes()[2].getFile(41, rowId);
        if (data != null)
            def.readValueLoop(new InputStream(data));
        DBRows.put(rowId, def);
        return def;
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
        if (3 == opcode) {
            int i_12_ = stream.readUnsignedByte();
            if (data == null) {
                data = new Object[i_12_][];
                dataScriptVarTypes = new ScriptVarType[i_12_][];
            }
            for (int index = stream.readUnsignedByte(); 255 != index; index = stream.readUnsignedByte()) {
                int length = stream.readUnsignedByte();
                ScriptVarType[] scriptVarTypes = new ScriptVarType[length];
                for (int i = 0; i < length; i++)
                    scriptVarTypes[i] = ScriptVarType.getScriptVarTypeById(stream.readSmart());
                data[index] = decodeDataForScriptVarType(stream, scriptVarTypes);
                dataScriptVarTypes[index] = scriptVarTypes;
            }
        }
    }

    public Object[] getDataInIndex(int index) {
        if (data == null)
            return null;
        return index >= data.length ? null : data[index];
    }

    public static Object[] decodeDataForScriptVarType(InputStream stream, ScriptVarType[] dataScriptVarTypes) {
        int length = stream.readSmart();
        Object[] objects = new Object[length * dataScriptVarTypes.length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < dataScriptVarTypes.length; j++) {
                int index = dataScriptVarTypes.length * i + j;
                objects[index] = dataScriptVarTypes[j].getBaseType().decode(stream);
            }
        }
        return objects;
    }

    public static int method2724(int i) {
        return i >>> 8;
    }

    public static int method15132(int i) {
        return i & 0xff;
    }

    static final Object[] getDBFieldData(int databaseId, int rowId, int fieldId) {
        int i_5_ = method2724(rowId);
        int i_6_ = method15132(rowId);
        DBRow class714 = DBRow.getDBRow(databaseId);
        DBRowData class436 = DBRowData.getDBRowData(i_5_);
        ScriptVarType[] class544s = class436.dataScriptVarTypes[i_6_];
        Object[] objects = class714.getDataInIndex(i_6_);
        if (null == objects && null != class436.data)
            objects = class436.data[i_6_];
        if (null == objects) {
            List<Object> out = new ArrayList<Object>();
            for (int i_7_ = 0; i_7_ < class544s.length; i_7_++) {
                ScriptVarType class544 = class544s[i_7_];
                if (class544 == ScriptVarType.STRING)
                    out.add("");
                else if (class544 == ScriptVarType.INT || ScriptVarType.BOOLEAN == class544)
                    out.add(0);
                else
                    out.add(-1);
            }
            return out.toArray(new Object[out.size()]);
        } else {
            int i_8_ = objects.length / class544s.length;
            if (fieldId < 0 || fieldId >= i_8_)
                throw new RuntimeException();
            List<Object> out = new ArrayList<Object>();
            for (int i_9_ = 0; i_9_ < class544s.length; i_9_++) {
                int i_10_ = i_9_ + fieldId * class544s.length;
                if (ScriptVarType.STRING == class544s[i_9_]) {
                    out.add(objects[i_10_]);
                } else {
                    out.add(((Integer) objects[i_10_]).intValue());
                }
            }
            return out.toArray(new Object[out.size()]);
        }
    }

}
