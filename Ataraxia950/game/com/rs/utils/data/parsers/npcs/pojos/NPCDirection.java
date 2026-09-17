package com.rs.utils.data.parsers.npcs.pojos;

public enum NPCDirection {
    NORTH(new int[] { 8192 }),
    SOUTH(new int[] { 0 }),
    EAST(new int[] { 12288 }),
    WEST(new int[] { 4096, 20480 }),
    NORTHEAST(new int[] { 10240 }),
    SOUTHEAST(new int[] { 14366, 14336 }),
    NORTHWEST(new int[] { 6144, 22528 }),
    SOUTHWEST(new int[] { 18432 });

    private final int[] values;
    private final int value;

    NPCDirection(int[] values) {
        this.value = values[0];
        this.values = values;
    }

    public int getValue() {
        return value;
    }

    public static NPCDirection getDirectionForValue(int value) {
        for (NPCDirection npcDirection : values()) {
            for (int i : npcDirection.values) {
                if (i == value) {
                    return npcDirection;
                }
            }
        }
        return null;
    }

    public int[] getValues() {
        return values;
    }
}
