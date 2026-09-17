package com.rs.game.player.content;

import com.google.common.collect.ImmutableBiMap;

import java.util.Arrays;

/**
 * Use this when you want "quick actions" on your interface. Use "Find usages..." on register for an example.
 *
 * @author lare96 <http://github.com/lare96>
 */
public class KeyActionMapper {

    static {
        String[] allKeys = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z", "SHIFT"};
        ImmutableBiMap.Builder<Integer, String> keyMap = ImmutableBiMap.builder();
        int index = 0;
        for (String key : allKeys) {
            keyMap.put(index++, key);
        }
        KEYS = keyMap.build();
    }

    public static final ImmutableBiMap<String, Integer> INDEX_MAP = ImmutableBiMap.<String, Integer>builder().
            put("1", 16).
            put("2", 17).
            put("3", 18).
            put("4", 19).
            put("5", 20).
            put("6", 21).
            put("7", 22).
            put("8", 23).
            put("9", 24).
            put("A", 48).
            put("B", 68).
            put("C", 66).
            put("D", 50).
            put("E", 34).
            put("F", 51).
            put("G", 52).
            put("H", 53).
            put("I", 39).
            put("J", 54).
            put("K", 55).
            put("L", 56).
            put("M", 70).
            put("N", 69).
            put("O", 40).
            put("P", 41).
            put("Q", 32).
            put("R", 35).
            put("S", 49).
            put("T", 36).
            put("U", 38).
            put("V", 67).
            put("W", 33).
            put("X", 65).
            put("Y", 37).
            put("Z", 64).
            put("SHIFT", 81).build();

    private static final ImmutableBiMap<Integer, String> KEYS;

    private final Runnable[] keyMap;
    public final int interfaceId;
    private int index;

    public KeyActionMapper(int interfaceId) {
        this.interfaceId = interfaceId;
        keyMap = new Runnable[KEYS.size()];
    }

    public String register(Runnable action) {
        if (index >= KEYS.size()) {
            throw new IllegalStateException("Too many key registrations!");
        }
        keyMap[index] = action;
        return KEYS.get(index++);
    }

    public boolean listenForKey(int value) {
        String key = INDEX_MAP.inverse().get(value);
        if (key == null) {
            return false;
        }
        int index = KEYS.inverse().get(key);
        Runnable action = keyMap[index];
        if (action != null) {
            reset();
            action.run();
            return true;
        }
        return false;
    }

    public void reset() {
        Arrays.fill(keyMap, null);
    }
}