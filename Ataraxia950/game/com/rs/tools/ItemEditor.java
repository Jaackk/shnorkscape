package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.IOException;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * <p>
 * Created on Nov 7, 2018.
 */
public class ItemEditor {

    public static void main(String[] args) throws IOException {
        // initiates the cache.
        Cache.init();
        // prints the total number of items in the cache for reference.
        Logger.getGlobal().info("CURRENT ITEMS: " + Utils.getItemDefinitionsSize());

        // the item id of the item to copy for a new item.
        final int ITEM_TO_COPY = 41430;
        // the item id of the new item to add.
        final int NEW_ITEM_ID = -1;

        // edit the item shit here.
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(ITEM_TO_COPY);
        definitions.name = "Dollar";
        definitions.inventoryOptions = new String[]{"Open", null, null, null, "Drop"};
        definitions.groundOptions = new String[]{null, null, "Take"};

        // adds the new item into the cache.
        boolean check = Cache.STORE.getIndexes()[19].putFile(NEW_ITEM_ID >>> 8, 0xff & NEW_ITEM_ID, definitions.writeValues().getBuffer());
        Logger.getGlobal().info("Finished! Added? " + check);
    }

}
