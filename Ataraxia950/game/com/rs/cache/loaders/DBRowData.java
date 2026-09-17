package com.rs.cache.loaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ParamTypeDefinitions.ScriptVarType;
import com.rs.network.io.InputStream;

public class DBRowData {

    private static final ConcurrentHashMap<Integer, DBRowData> DBRows = new ConcurrentHashMap<Integer, DBRowData>();
    public ScriptVarType[][] dataScriptVarTypes;
    public Object[][] data;

    public ScriptVarType getScriptVarTypeInIndex(int index1, int index2) {
        return dataScriptVarTypes[index1][index2];
    }

    public static final DBRowData getDBRowData(int rowId) {
        DBRowData def = DBRows.get(rowId);
        if (def != null && def.dataScriptVarTypes != null)
            return def;
        def = new DBRowData();
        byte[] data = Cache.STORE.getIndexes()[2].getFile(40, rowId);
        if (data != null)
            def.readValueLoop(new InputStream(data));
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
        if (1 == opcode) {
            int i_12_ = stream.readUnsignedByte();
            if (dataScriptVarTypes == null) {
                dataScriptVarTypes = new ScriptVarType[i_12_][];
            }

            for (int index = stream.readUnsignedByte(); 255 != index; index = stream.readUnsignedByte()) {
                int i_3_ = index & 0x7f;
                boolean bool = 0 != (index & 0x80);

                ScriptVarType[] scriptVarTypes = new ScriptVarType[stream.readUnsignedByte()];
                for (int i = 0; i < scriptVarTypes.length; i++) {
                    scriptVarTypes[i] = ScriptVarType.getScriptVarTypeById(stream.readSmart());
                }
                dataScriptVarTypes[i_3_] = scriptVarTypes;

                if (bool) {
                    if (data == null)
                        data = new Object[dataScriptVarTypes.length][];
                    data[i_3_] = DBRow.decodeDataForScriptVarType(stream, scriptVarTypes);
                }
            }
        }
    }

}
