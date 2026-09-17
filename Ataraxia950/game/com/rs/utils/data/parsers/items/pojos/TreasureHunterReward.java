package com.rs.utils.data.parsers.items.pojos;

import java.io.Serializable;

import lombok.Data;

@Data
public class TreasureHunterReward implements Serializable {

    private static final long serialVersionUID = -3780102501481949121L;
    private int itemId;
    private int minAmount;
    private int maxAmount;
    private int rarity;
    private String category;
    private int convertAmount;

    public TreasureHunterReward(int itemId, int minAmount, int maxAmount, int rarity, int convertAmount, String category) {
        this.itemId = itemId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rarity = rarity;
        this.category = category;
        this.convertAmount = convertAmount;
    }

}
