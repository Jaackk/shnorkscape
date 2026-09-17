package com.rs.game.player.actions.herblore;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.HerbCleaning.Herbs;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class AutoUnfPotion {
    private static final int COST = 10_000;

    public static void toUnf(Player player) {
        int totalAmount = 0;
        int affordAmount = player.getInventory().getCoinsAmount() / COST;
        for (Herbs herb : Herbs.values()) {

            ItemDefinitions herbDef = ItemDefinitions.getItemDefinitions(herb.getCleanId());
            int id = herbDef.certId;
            int amount = player.getInventory().getAmountOf(id);
            if (amount == 0)
                continue;
            if (amount > affordAmount) {
                amount = affordAmount;
                affordAmount = 0;
            } else {
                affordAmount -= amount;
            }

            ItemDefinitions unfDef = ItemDefinitions.getItemDefinitions(herb.getUnf());
            if (player.getInventory().deleteAllCoins(amount * COST)) {
                player.getInventory().deleteItem(new Item(id, amount));
                player.getInventory().addItem(new Item(unfDef.certId, amount));
                totalAmount += amount;
            }

            if (affordAmount == 0) {
                break;
            }
        }

        if (totalAmount > 0) {
            player.sendMessage("He magically transmutes a total of " + Colors.RED + totalAmount + "</col> clean herbs into unfinished potions.", true);
            player.sendMessage("He looks exhausted now.", true);
        } else {
            player.sendMessage("You have no herbs to transmute.");
        }
    }

    public static void checkToUnf(Player player, int itemUsed) {
        for (Herbs herb : Herbs.values()) {
            ItemDefinitions herbDef = ItemDefinitions.getItemDefinitions(herb.getCleanId());
            if (itemUsed == herbDef.certId) {
                toUnf(player);
                return;
            }
        }
        player.sendMessage("The druid looks at you, confused at what you expect him to do with that.");
    }
}