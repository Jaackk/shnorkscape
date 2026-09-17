package com.rs.utils.data.parsers.items.pojos;

import lombok.Data;

@Data
public class ItemExamine {
    private final String itemName;
    private final String description;

    public ItemExamine(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }
}
