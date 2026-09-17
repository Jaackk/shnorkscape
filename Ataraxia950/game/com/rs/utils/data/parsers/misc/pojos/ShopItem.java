package com.rs.utils.data.parsers.misc.pojos;

import com.rs.cache.loaders.ItemDefinitions;
import lombok.Getter;
import lombok.Setter;

public class ShopItem {
    @Getter
    private final int id;
    @Getter @Setter
    private int amount;
    @Getter @Setter
    private int price;

    public ShopItem(int id, int amount, int price) {
        this.id = id;
        this.amount = amount;

        int cachePrice = getDefinitions().getValue();
        if (price != cachePrice) {
            this.price = price;
        } else {
            this.price = price;
        }
    }

    public ShopItem(int id, int amount) {
        this.id = id;
        this.amount = amount;
        this.price = getDefinitions().getValue();
    }

    public ItemDefinitions getDefinitions() {
        return ItemDefinitions.getItemDefinitions(id);
    }

    @Override
    public String toString() {
        return "ShopItem [id=" + id + ", amount=" + amount + "]";
    }

}
