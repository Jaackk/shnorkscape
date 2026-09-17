package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.shops.ShopViewer;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * @author Xenthium.
 */

public class SetGrandExchangeItemPrice extends Dialogue {

    private int itemId, newPrice, previousPrice;
    private Item item;

    @Override
    public void start() {
        player.sendInputInteger("Enter the item's ID:", new InputIntegerEvent() {
            @Override
            public void run(Player player) {
                itemId = getInteger();
                if (itemId < 0 || itemId > Utils.getItemDefinitionsSize()) {
                    player.sendMessage(Colors.RED + itemId + " is not a valid item id, input a number between 0 and " + Utils.getItemDefinitionsSize() + ".");
                    return;
                }
                item = new Item(itemId);
                previousPrice = GrandExchange.getPrice(item);
                player.sendInputInteger("Item name: " + item.getName() + ", Item ID: " + item.getId() + ", current price: " + Utils.formatNumber(GrandExchange.getPrice(item)) + " gp.<br>Enter a new Grand Exchange price:", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        newPrice = getInteger();
                        if (newPrice < 0 || newPrice > Integer.MAX_VALUE) {
                            player.sendMessage(Colors.RED + newPrice + " isn't a valid price, input a number between 1 and " + Utils.formatNumber(Integer.MAX_VALUE) + ".");
                            return;
                        }
                        if (item.getId() == ShopViewer.COINS || !ItemConstants.isTradeable(item) || item.getName().contains("charm") || item.getId() == 11640 || item.getId() == 37753) {
                            sendOptionsDialogue("This item isn't available on the Grand Exchange, are you sure you want to do this?", "Yes", "No");
                            return;
                        }
                        setPrice();
                    }
                });
            }
        });
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            setPrice();
        }
        end();
    }

    @Override
    public void finish() {
    }

    private void setPrice() {
        try {
            if (GrandExchange.usesCachePrices()) {
                player.sendMessage(Colors.RED + "Grand Exchange prices now come from the item cache. Edit the item value in your cache editor instead.");
                return;
            }
            GrandExchange.setPrice(itemId, newPrice);
            GrandExchange.savePrices();
            player.sendMessage(Colors.GREEN + "Success, you've set the Grand Exchange price of " + item.getName() + " to " + Utils.formatNumber(newPrice) + " gp.");
            player.sendMessage(Colors.GREEN + "Previous price: " + Utils.formatNumber(previousPrice) + " gp.");
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
            player.sendMessage(Colors.RED + "Something went wrong when attempting to edit the price of that item.");
        }
    }

}
