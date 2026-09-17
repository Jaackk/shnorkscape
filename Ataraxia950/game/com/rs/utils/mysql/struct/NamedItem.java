package com.rs.utils.mysql.struct;

import com.rs.cache.loaders.ItemDefinitions;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class NamedItem {

    private final int id;
    private final int amount;
    private final String name;

    public NamedItem(final int id, final int amount) {
        this.id = id;
        this.amount = amount;
        this.name = ItemDefinitions.getItemDefinitions(id).getName();
    }

}
