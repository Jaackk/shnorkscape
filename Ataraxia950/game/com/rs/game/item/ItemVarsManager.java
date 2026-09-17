package com.rs.game.item;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.VarBitDefinitions;

public class ItemVarsManager {
    private static final int[] masklookup = new int[32];

    static {
        int i = 2;
        for (int i2 = 0; i2 < 32; i2++) {
            masklookup[i2] = i - 1;
            i += i;
        }
    }
    private final Map<Integer, Integer> values;

    public ItemVarsManager() {
        values = new HashMap<Integer, Integer>();
    }

    public void setVarBit(int id, int value) {
        if (id == -1) // temporarly
            return;
        VarBitDefinitions defs = VarBitDefinitions.getClientVarpBitDefinitions(id);
        int mask = masklookup[defs.endBit - defs.startBit];
        if (value < 0 || value > mask)
            value = 0;
        mask <<= defs.startBit;
        int intialVarValue = !values.containsKey(defs.baseVar) ? 0 : values.get(defs.baseVar);
        int varpValue = (intialVarValue & (mask ^ 0xffffffff) | value << defs.startBit & mask);
        setVar(defs.baseVar, varpValue);
    }

    public void setVar(int id, int value) {
        if (id == -1)
            return;
        values.put(id, value);
    }

    public int getVarsLength() {
        return values.size();
    }

    public int getVarId(int index) {
        try {
            if (index >= values.size())
                return 0;
            return values.keySet().stream().toArray(Integer[]::new)[index];
        } catch (Exception e) {
            return 0;
        }
    }

    public int getValue(int id) {
        if (id == -1)
            return 0;
        return !values.containsKey(id) ? 0 : values.get(id);
    }

}
