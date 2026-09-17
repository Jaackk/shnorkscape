package com.rs.game.player.actions.fletching.quickshaft;

import com.google.common.collect.ImmutableMap;
import lombok.val;

/**
 * @author lare96 <http://github.com/lare96>
 */
public enum QuickShaftable {
    NORMAL(1, 1511, 20, 4.5),
    OAK(20, 1521, 25, 7.5),
    WILLOW(35, 1519, 30, 10.5),
    MAPLE(50, 1517, 35, 13.5),
    YEW(65, 1515, 40, 15.5),
    MAGIC(80, 1513, 45, 18.5),
    ELDER(95, 29556, 50, 21.5),
    EVIL_BARK(98, 3239, 75, 35.2, "evil bark");

    public static final ImmutableMap<Integer, QuickShaftable> ALL;
    public final int level;
    public final int id;
    public final int amount;
    public final double exp;
    public final String formattedName;

    static {
        val builder = ImmutableMap.<Integer, QuickShaftable>builder();
        for (QuickShaftable l : values()) {
            builder.put(l.id, l);
        }
        ALL = builder.build();
    }

    QuickShaftable(int level, int id, int amount, double exp, String formattedName) {
        this.level = level;
        this.id = id;
        this.amount = amount;
        this.exp = exp;
        this.formattedName = formattedName == null ? (name().toLowerCase() + " logs") : formattedName;
    }

    QuickShaftable(int level, int id, int amount, double exp) {
        this(level, id, amount, exp, null);
    }
}