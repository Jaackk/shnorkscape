package com.rs.game.player.content.butler;

import com.rs.utils.Utils;

public enum Butler {
    RICK(-1, -1, -1, -1, -1),
    ROYAL_GUARD(-1, -1, -1, -1, -1),
    MAID(-1, -1, -1, -1, -1),
    COOK(-1, -1, -1, -1, -1),
    BUTLER(-1, -1, -1, -1, -1),
    DEMON_BUTLER(-1, -1, -1, -1, -1);

    public final int levelRequired;
    public final int npcId;
    public final int bankCost;
    public final int bankSpeed;
    public final int carryAmount;
    public final String description;

    Butler(int levelRequired, int npcId, int bankCost, int bankSpeed, int carryAmount) {
        this.levelRequired = levelRequired;
        this.npcId = npcId;
        this.bankCost = bankCost;
        this.bankSpeed = bankSpeed;
        this.carryAmount = carryAmount;
        description = Utils.capitalize(name().toLowerCase().replaceAll("_", " ")) + " (Level "+levelRequired+")";
    }
}
