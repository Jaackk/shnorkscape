package com.rs.game.activites.gambling.flowerpoker;

import lombok.Getter;

public enum Flower {
    RED(2981, 0, .1382),
    ORANGE(2985, .1382, .2763),
    YELLOW(2983, .2763, .4361),
    BLUE(2982, .4361, .5904),
    PURPLE(2984, .5904, .742),
    PASTEL(2980, .742, .8436),
    RAINBOW(2986, .8436, .9969),
    WHITE(2987, .9969, .998),
    BLACK(2988, .998, 1),
    ;

    @Getter
    private final int objectId;
    private final double low;
    private final double high;

    Flower(int objectId, double low, double high) {
        this.objectId = objectId;
        this.low = low;
        this.high = high;
    }

    public static Flower getFlowerForChance(double number) {
        for (Flower flower : values()) {
            if (number >= flower.low && number < flower.high) {
                return flower;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().replace("_", " ").toLowerCase();
    }
}
