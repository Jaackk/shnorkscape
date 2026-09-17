package com.rs.utils;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

import java.util.ArrayList;

/**
 * @author Xenthium/Toby - 12/11/18.
 */

public class DefinitionsSearch {

    private static final int MAXIMUM_RESULTS = 100;

    /**
     * Finds items, npcs or objects containing the search string and prints them to the developer console.
     */
    public static void findMatchesByString(final Player player, final String searchString, final SearchType searchType) {
        final String type = searchType.toString().toLowerCase();
        ArrayList<Item> matches = new ArrayList<>();
        int amount = 0;

        switch (searchType) {
            case ITEM:
                for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
                    Item item = new Item(i);

                    if (item.getDefinitions() == null) {
                        continue;
                    }

                    if (item.getName().toLowerCase().contains(searchString.toLowerCase())) {
                        matches.add(new Item(i));
                        String name = (item.getDefinitions().isNoted() ? "Noted " : "") + item.getName();
                        if (matches.size() <= MAXIMUM_RESULTS) {
                            player.sendConsoleMessage(Colors.wrap(Colors.DEF_SEARCH_CYAN, name) + " - " + Colors.wrap(Colors.GREEN, "ID: " + item.getId()));
                        }
                    }
                }
                break;

            case NPC:
                for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
                    final NPCDefinitions definitions = NPCDefinitions.getNPCDefinitions(i);

                    if (definitions == null) {
                        continue;
                    }

                    if (definitions.getName().toLowerCase().contains(searchString.toLowerCase())) {
                        amount++;
                        if (amount <= MAXIMUM_RESULTS) {
                            player.sendConsoleMessage(Colors.wrap(Colors.DEF_SEARCH_CYAN, definitions.getName()) + " - " + Colors.wrap(Colors.GREEN, "ID: " + i));
                        }
                    }
                }
                break;

            case OBJECT:
                for (int i = 0; i < Utils.getObjectDefinitionsSize(); i++) {
                    final ObjectDefinitions definitions = ObjectDefinitions.getObjectDefinitions(i);

                    if (definitions == null) {
                        continue;
                    }

                    if (definitions.name.toLowerCase().contains(searchString.toLowerCase())) {
                        amount++;
                        if (amount <= MAXIMUM_RESULTS) {
                            player.sendConsoleMessage(Colors.wrap(Colors.DEF_SEARCH_CYAN, definitions.getName()) + " - " + Colors.wrap(Colors.GREEN, "ID: " + definitions.getId()));
                        }
                    }
                }
                break;
        }

        if (searchType.equals(SearchType.ITEM)) {
            amount = matches.size();
        }

        player.sendConsoleMessage((amount > 0 ? Colors.YELLOW : Colors.RED) + "Found " + (amount > 0 ? amount : "no") + " " + type + (amount > 1 || amount == 0 ? "s" : "")
                + " containing the string \"" + searchString + "\".");

        if (matches.size() == 1 && player.getInventory().hasFreeSlots()) {
            player.getInventory().addItem(matches.get(0));
            player.sendConsoleMessage(Colors.YELLOW + "The item has been spawned for you, as it was the only match found.");
        }

        if (amount >= MAXIMUM_RESULTS) {
            player.sendConsoleMessage(Colors.YELLOW + "Only listing the first " + MAXIMUM_RESULTS + " results; try refining your search if you can't find the " + type + "(s) you're looking for.");
        }
    }

    public enum SearchType {
        ITEM, NPC, OBJECT
    }

}