package com.rs.game.player.actions.mining.defs;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * RockDefinitions.java | 11:48:51 AM
 *
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum RockDefinitions {

    CLAY(1, 5, 434, 7, 0, 11552, 5, 3, 10),

    SOFT_CLAY(1, 5, 1761, 7, 0, 11552, 5, 3, 10),

    Copper_Ore(1, 17.5, 436, 7, 0, 11552, 5, 1, 10),

    Tin_Ore(1, 17.5, 438, 7, 0, 11552, 5, 1, 10),

    Iron_Ore(10, 35, 440, 10, 0, 11552, 5, 5, 10),

    Sandstone_Ore(35, 30, 6971, 15, 0, 11552, 5, 5, 10),

    Silver_Ore(20, 40, 442, 15, 0, 11552, 5, 5, 10),

    Coal_Ore(20, 50, 453, 20, 0, 11552, 5, 5, 10),

    Granite_Ore(45, 50, 6979, 20, 0, 11552, 5, 5, 10),

    Gold_Ore(40, 60, 444, 25, 0, 11554, 10, 5, 10),

    Mithril_Ore(30, 80, 447, 30, 0, 11552, 20, 6, 10),

    Adamant_Ore(40, 95, 449, 35, 0, 11552, 25, 7, 10),

    Runite_Ore(50, 125, 451, 40, 0, 11552, 40, 8, 10),

    LRC_Coal_Ore(77, 50, 453, 25, 0, -1, -1, -1, 10),

    LRC_Gold_Ore(80, 60, 444, 25, 0, -1, -1, -1, 10),

    Red_Sandstone(81, 15, 23194, 25, 0, -1, -1, -1, 10),

    BANE_ORE(80, 90, 21778, 45, 0, 11552, 40, 2, 10),

    Donor_Ore(1, -1, -1, 80, 0, -1, -1, -1, 10),

    Protean_Ore(80, 362.8, 31350, 150, 0, -1, -1, -1, 10),

    DZ_SANDSTONE(81, 150, 23193, 41, 0, -1, -1, -1, 10),

    DZ_CRYSTAL(81, 150, 32845, 41, 0, -1, -1, -1,10),

    GEM_ROCK(40, 125, 1625, 40, 0, 11554, 45, 1,5),
    LUMINITE_ORE(40, 761.7, 44820, 30, 0, 113017, -1, -1, 12),
    DRAKOLITH_ORE(60, 761.7, 44824, 30, 0, 113017, -1, -1, 12),
    ORICHALCITE_ORE(60, 761.7, 44822, 30, 0, 113017, -1, -1, 12),
    NECRITE_ORE(70, 761.7, 44826, 30, 0, 113017, -1, -1, 12),
    PHASMATITE_ORE(70, 761.7, 44828, 30, 0, 113017, -1, -1, 12),
    CRYSTAL_SANDSTONE(81, 15, 32847, 30, 0, -1, -1, -1, 20),

    SEREN_STONE(89, 296.7, 32262, 50, 0, 92716, 200, 50, 20),
    LIGHT_ORE(90, 761.7, 44830, 10, 0, 113017, -1, -1, 12),

    DARK_ORE(90, 761.7, 44832, 10, 0, 113017, -1, -1, 12);


    public static final ImmutableMap<Integer, RockDefinitions> ALL;

    static {
        Map<Integer, RockDefinitions> map = new HashMap<>();
        for (RockDefinitions def : values()) {
            map.put(def.oreId, def);
        }
        ALL = ImmutableMap.copyOf(map);
    }

    private final int level;
    private final double xp;
    private final int oreId;
    private final int oreBaseTime;
    private final int oreRandomTime;
    private final int emptySpot;
    private final int respawnDelay;
    private final int oreLife;
    private final int orePercentage;


    RockDefinitions(int level, double xp, int oreId, int oreBaseTime, int oreRandomTime, int emptySpot, int respawnDelay, int oreLife, int orePercentage) {
        this.level = level;
        this.xp = xp;
        this.oreId = oreId;
        this.oreBaseTime = oreBaseTime;
        this.oreRandomTime = oreRandomTime;
        this.emptySpot = emptySpot != -1 ? 224 : emptySpot;
        this.respawnDelay = respawnDelay;
        this.oreLife = oreLife;
        this.orePercentage = orePercentage;
    }

    public static RockDefinitions get(int id) {
        return ALL.get(id);
    }

    public int getEmptyId() {
        return emptySpot;
    }

    public int getLevel() {
        return level;
    }

    public int getOreBaseTime() {
        return oreBaseTime;
    }

    public int getOreId() {
        return oreId;
    }

    public int getOreRandomTime() {
        return oreRandomTime;
    }

    public int getOreLife() {
        return oreLife;
    }

    public int getRespawnDelay() {
        return respawnDelay;
    }

    public double getXp() {
        return xp;
    }

    public int orePercentage() {
        return orePercentage;
    }
}
