package com.rs.game.item.floor;

import com.rs.game.Graphics;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum LootBeamType {
    NORMAL(-1, 4422, "A golden beam shines over one of your items."),
    RAINBOW(41336, 5053, "A rainbow shines over one of your items."),
    SUNSHINE(41377, 5176, "A beacon of light shines over one of your items"),
    MUSIC(41378, 5235, "The sounds of musicians emit from one of your items."),
    CHRISTMAS(41375, 5300, "A festive beam shines over one of your items.");

    @Getter
    private final int itemId;
    @Getter
    private final Graphics graphics;
    @Getter
    private final String message;

    public static final LootBeamType[] VALUES = values();

    LootBeamType(int itemId, int gfxId, String message) {
        this.itemId = itemId;
        graphics = new Graphics(gfxId);
        this.message = message;
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
