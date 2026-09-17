package com.rs.game.player.actions.mining.defs;

/**
 * Currently not in use until the Mining Classes are clean. Pickaxe.java |
 * 11:50:36 AM
 * 
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum Pickaxe {
    // ID, Level, Animation, baseTime

    BRONZE_PICKAXE(1265, 1, 32540, 1),

    IRON_PICKAXE(1267, 1, 32548, 2),

    STEEL_PICKAXE(1269, 6, 32552, 3),

    BLACK_PICKAXE(0, 11, 0, 4),

    MITHRIL_PICKAXE(1273, 21, 32558, 5),

    ADAMANT_PICKAXE(1271, 31, 32562, 7),

    RUNE_PICKAXE(1275, 41, 32566, 10),

    DRAGON_PICKAXE(15259, 61, 12190, 13),

    INFERNAL_ADZE(13661, 61, 10222, 13),
    SONG(44834, 90, 32618, 100),
    BANE(45154, 80, 32606, 100);

    private final int itemId;

    private final int levelRequirement;

    private final int animation;

    private final int baseTime;

    Pickaxe(int itemId, int levelRequirement, int animation, int baseTime) {
        this.itemId = itemId;
        this.levelRequirement = levelRequirement;
        this.animation = animation;
        this.baseTime = baseTime;
    }

    public int getItemId() {
        return itemId;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public int getAnimation() {
        return animation;
    }

    public int getBaseTime() {
        return baseTime;
    }

}
