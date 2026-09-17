package com.rs.game.player.actions.herblore.herbicide;

import com.google.common.collect.ImmutableSet;
import com.rs.cache.loaders.ItemDefinitions;

import java.util.HashSet;
import java.util.Set;

public enum HerbicideSettings {

    GUAM(5, 3, 199, 249),
    MARRENTILL(7.6, 5, 201, 251),
    TARROMIN(10, 11, 203, 253),
    HARRALANDER(12.6, 20, 205, 255),
    RANARR(15, 25, 207, 257),
    TOADFLAX(16, 30, 3049, 2998),
    SPIRIT_WEED(15.6, 35, 12174, 12172),
    IRIT(17.6, 40, 209, 259),
    WERGALI(19, 41, 14836, 14854),
    AVANTOE(20, 48, 211, 261),
    KWUARM(22.6, 54, 213, 263),
    SNAPDRAGON(23.6, 59, 3051, 3000),
    CADANTINE(25, 65, 215, 265),
    LANTADYME(26.2, 67, 2485, 2481),
    DWARF_WEED(27.6, 70, 217, 267),
    FELLSTALK(33.6, 91, 21626, 21624),
    TORSTOL(30, 75, 219, 269);

    private final ImmutableSet<Integer> herbSet;
    private final int level;
    private final double experience;

    HerbicideSettings(double experience, int level, int... herbIds) {
        this.level = level;
        this.experience = experience;

        Set<Integer> herbs = new HashSet<>();
        for (int id : herbIds) {
            herbs.add(id); // Unnoted herb.
            herbs.add(ItemDefinitions.getItemDefinitions(id).certId); // Noted herb.
        }
        herbSet = ImmutableSet.copyOf(herbs);
    }

    public boolean isHerb(int id) {
        return herbSet.contains(id);
    }

    public double getExperience() {
        return experience;
    }

    public int getLevel() {
        return level;
    }

}
