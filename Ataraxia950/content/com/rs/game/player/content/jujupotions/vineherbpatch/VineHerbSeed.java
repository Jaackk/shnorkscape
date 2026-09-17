package com.rs.game.player.content.jujupotions.vineherbpatch;

import com.google.common.collect.ImmutableMap;
import com.rs.cache.loaders.ItemDefinitions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashMap;

/**
 * @author lare96 <http://github.com/lare96>
 */
@RequiredArgsConstructor
@Getter
public enum VineHerbSeed {
    ERZILLE(19897, 19984, 58, 87.0, 87.0, 8, 20),
    ARGWAY(19907, 19985, 65, 110.0, 125.0, 8, 17),
    UGUNE(19902, 19986, 70, 135.0, 152.0, 8, 14),
    SHENGO(19912, 19987, 76, 140.5, 160.0, 6, 13),
    SAMADEN(19917, 19988, 80, 170.0, 190.0, 4, 12);

    /**
     * Seed ID -> instance.
     */
    public static final ImmutableMap<Integer, VineHerbSeed> ALL;

    static {
        val map = new HashMap<Integer, VineHerbSeed>();
        for (VineHerbSeed seed : values()) {
            map.put(seed.seedId, seed);
        }
        ALL = ImmutableMap.copyOf(map);
    }

    private final int seedId;
    private final int grimyHerbId;
    private final int level;
    private final double plantXp;
    private final double harvestXp;
    private final int yieldMin;
    private final int yieldMax;
    private String name;


    public String getName() {
        if(name == null) {
            name = ItemDefinitions.getItemDefinitions(seedId).getName();
        }
        return name;
    }
}
