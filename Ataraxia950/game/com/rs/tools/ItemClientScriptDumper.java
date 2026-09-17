package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.IOException;

/**
 * Prints out client scripts for all items containing STRING_TO_FIND
 */

public class ItemClientScriptDumper {

    private static final String STRING_TO_FIND = " maul";

    public static void main(String[] args) {
        try {
            Cache.init();
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
            return;
        }

        for (int i = 0; i <= Utils.getItemDefinitionsSize(); i++) {
            Item item = new Item(i);
            ItemDefinitions defs = item.getDefinitions();
            if (defs.getName().toLowerCase().contains(STRING_TO_FIND) && !defs.isNoted()) {
                Logger.getGlobal().info("Item name: " + defs.getName() + ", Item ID: " + defs.getId() + ", Scripts: " + defs.clientScriptData);
            }
        }
    }
}
