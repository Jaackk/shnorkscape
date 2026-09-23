package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Equipment;

/** Client equipment projection for the existing infinite-ammunition supply.
 * Never inserts an item into authoritative equipment, inventory or saves. */
final class Native950DevelopmentAmmo {
    private Native950DevelopmentAmmo() { }

    static int supplied(int weaponId, int ammoId, boolean enabled) {
        if (!enabled || weaponId < 0) return -1;
        ItemDefinitions weapon = Native950CacheItems.definition(weaponId);
        if (weapon == null || weapon.getCSOpcode(2826) != 1) return -1;
        // Exact950 CS2660: weapon param21 must equal equipped ammo's category.
        // Self-ammunition weapons have no required category.
        int category = weapon.getCSOpcode(21, -1);
        if (category != 62 && category != 63) return -1;
        ItemDefinitions actual = ammoId < 0 ? null : Native950CacheItems.definition(ammoId);
        if (actual != null && actual.itemCategory == category
                && (weaponId != 8880 || ammoId == 8882)) return -1;
        int id = category == 62 ? 882 : weaponId == 8880 ? 8882 : 877;
        ItemDefinitions supply = Native950CacheItems.definition(id);
        if (supply == null || supply.itemCategory != category || supply.equipSlot != Equipment.SLOT_ARROWS)
            throw new IllegalStateException("Current-cache development ammunition contract changed");
        return id;
    }
}
