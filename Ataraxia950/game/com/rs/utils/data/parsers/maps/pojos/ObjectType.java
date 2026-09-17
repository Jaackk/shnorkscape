package com.rs.utils.data.parsers.maps.pojos;

import lombok.Getter;

/**
 * Object types, CBA to add all when only 10 is really used.
 *
 * @author Arham 4
 */
public enum ObjectType {
    STRAIGHT_WALL(0),
    ENTIRE_WALL(2),
    DIAGONAL_WALL(9),
    REGULAR(10),
    GROUND_OBJECT(11),
    GROUND_DECORATION(22);

    @Getter
    private final int value;

    ObjectType(int value) {
        this.value = value;
    }

    public static ObjectType getTypeFromValue(int value) {
        for (ObjectType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}
