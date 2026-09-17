package com.rs.game.player.dialogue.impl;

import java.util.ArrayList;
import java.util.List;
import com.rs.cache.loaders.ItemDefinitions;


import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.shops.ShopViewer;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * Allows an admin to set Grand Exchange item price by searching for an item's name.
 * Supports partial name matching and multiple options if more than one item matches.
 *
 * @author ChatGPT
 */
public class SetGrandExchangeItemPriceByName extends Dialogue {

    private int itemId, newPrice, previousPrice;
    private Item item;
    private List<Integer> matchedIds;

    @Override
    public void start() {
        player.sendInputString("Enter part of the item's name:", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String inputName = getString().toLowerCase().trim();
                matchedIds = new ArrayList<>();
                for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
                    String defName = ItemDefinitions.getItemDefinitions(id).getName();


                    if (defName != null && defName.toLowerCase().contains(inputName)) {
                        matchedIds.add(id);
                    }
                }
                if (matchedIds.isEmpty()) {
                    player.sendMessage(Colors.RED + "No items found containing '" + inputName + "'.");
                    end();
                    return;
                }
                if (matchedIds.size() == 1) {
                    selectItem(matchedIds.get(0));
                    return;
                }
                if (matchedIds.size() > 1) {
                    showOptions();
                }
            }
        });
    }

    private void showOptions() {
        int maxOptions = Math.min(5, matchedIds.size()); // You can only show 5 choices at once
        String[] options = new String[maxOptions];
        for (int i = 0; i < maxOptions; i++) {
            int id = matchedIds.get(i);
            String name = ItemDefinitions.getItemDefinitions(id).getName();
            options[i] = name + " (" + id + ")"; // <== Add ID next to the name
        }
        sendOptionsDialogue("Pick an item:", options);
    }


    @Override
    public void run(int interfaceId, int componentId) {
        if (matchedIds == null || matchedIds.isEmpty()) {
            end();
            return;
        }
        int index = componentId - OPTION_1;
        if (index >= 0 && index < matchedIds.size()) {
            selectItem(matchedIds.get(index));
        }
        end();
    }

    private void selectItem(int id) {
        itemId = id;
        item = new Item(itemId);
        previousPrice = GrandExchange.getPrice(item);
        player.sendInputInteger("Item name: " + item.getName() + ", Item ID: " + item.getId() + ", current price: " + Utils.formatNumber(previousPrice) + " gp.<br>Enter a new Grand Exchange price:", new InputIntegerEvent() {
            @Override
            public void run(Player player) {
                newPrice = getInteger();
                if (newPrice < 1 || newPrice > Integer.MAX_VALUE) {
                    player.sendMessage(Colors.RED + newPrice + " isn't a valid price, input a number between 1 and " + Utils.formatNumber(Integer.MAX_VALUE) + ".");
                    return;
                }
                setPrice(); // Immediately set price, no warning
            }
        });
    }


    private void setPrice() {
        try {
            if (GrandExchange.usesCachePrices()) {
                player.sendMessage(Colors.RED + "Grand Exchange prices now come from the item cache. Edit the item value in your cache editor instead.");
                return;
            }
            GrandExchange.setPrice(itemId, newPrice);
            GrandExchange.savePrices();
            player.sendMessage(Colors.GREEN + "Success! Set the Grand Exchange price of " + item.getName() + " to " + Utils.formatNumber(newPrice) + " gp.");
            player.sendMessage(Colors.GREEN + "Previous price was: " + Utils.formatNumber(previousPrice) + " gp.");
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
            player.sendMessage(Colors.RED + "Something went wrong when attempting to edit the price of that item.");
        }
    }

    @Override
    public void finish() {
        // nothing needed here
    }
}
