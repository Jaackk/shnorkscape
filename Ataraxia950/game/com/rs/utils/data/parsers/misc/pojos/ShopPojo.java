package com.rs.utils.data.parsers.misc.pojos;

import lombok.Data;

@Data
public class ShopPojo {
    private int shopId;
    private final String name;
    private final int[] npcIds;
    private final ShopCurrency currency;
    private final ShopItem[] items;
    private final boolean isGeneralStore;
    private final boolean isSellingBlocked;

    public ShopPojo() {
        shopId = -1;
        npcIds = new int[0];
        name = null;
        currency = ShopCurrency.GP;
        items = null;
        isGeneralStore = false;
        isSellingBlocked = false;
    }

    private ShopPojo(ShopPojo other) {
        shopId = other.shopId;
        name = other.name;
        npcIds = other.npcIds;
        currency = other.currency;
        items = new ShopItem[other.items.length];
        isSellingBlocked = other.isSellingBlocked;
        System.arraycopy(other.items, 0, items, 0, items.length);
        isGeneralStore = other.isGeneralStore;
    }

    @Override
    public Object clone() {
        return new ShopPojo(this);
    }
}
