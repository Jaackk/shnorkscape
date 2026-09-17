package com.rs.utils.data.parsers.maps.pojos;

import lombok.Getter;

public enum ObjectRotation {
    NORTH(new int[] {0, 254, 6}),
    EAST(new int[] {1, 255, 7}),
    SOUTH(new int[] {2, 252, 4}),
    WEST(new int[] {3, 253, 5});

    private final int[] values;
    @Getter
    private final int value;

    ObjectRotation(int[] values) {
        this.values = values;
        this.value = values[0];
    }

    public static ObjectRotation getRotationFromValue(int value) {
        for (ObjectRotation objectRotation : values()) {
            for (int i : objectRotation.values) {
                if (i == value) {
                    return objectRotation;
                }
            }
        }
        return null;
    }
}
