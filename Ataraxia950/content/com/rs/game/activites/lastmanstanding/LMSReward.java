package com.rs.game.activites.lastmanstanding;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

public class LMSReward {
    private final String name;
    private final Item[] items;

    LMSReward(int itemId, int amount) {
        Item item = new Item(itemId, amount);
        name = item.getName();
        items = new Item[] {item};
    }

    LMSReward(String name, int itemId, int amount) {
        this.name = name;
        Item item = new Item(itemId, amount);
        items = new Item[] {item};
    }

    LMSReward(String name, Item... items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void giveReward(Player player) {
        for (Item item : items) {
            player.addItem(item);
        }
    }
}
