package com.rs.game.player.content;

import com.google.common.collect.ImmutableMap;
import lombok.val;

/**
 * @author lare96
 */
public enum Firelighter {
    RED(7329, 11404),
    GREEN(7330, 11405),
    BLUE(7331, 11406),
    PURPLE(10326, 20001),
    WHITE(10327, 20000);

    public static final ImmutableMap<Integer, Firelighter> values;
    static {
        val bldr = ImmutableMap.<Integer, Firelighter>builder();
        for(Firelighter fl : values()) {
            bldr.put(fl.itemId, fl);
        }
        values = bldr.build();
    }
    public final int itemId;
    public final int fireId;

    Firelighter(int itemId, int fireId) {
        this.itemId = itemId;
        this.fireId = fireId;
    }
}
