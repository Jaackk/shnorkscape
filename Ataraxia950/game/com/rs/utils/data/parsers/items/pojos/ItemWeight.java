package com.rs.utils.data.parsers.items.pojos;

import lombok.Data;

@Data
public class ItemWeight {
    private final String itemName;
    private final double weight;

    public ItemWeight(String itemName, double weight) {
        this.itemName = itemName;
        this.weight = weight;
    }
}
