package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Equipment;

/** Client equipment projection for the existing infinite-ammunition supply.
 * Never inserts an item into authoritative equipment, inventory or saves. */
final class Native950DevelopmentAmmo {
    // Ordinary current950 ammunition progression. Special effects are not
    // interchangeable measures of damage, so do not pick a random enchantment.
    private static final int[] ARROWS={882,884,886,888,890,892,63269,63274,63279,63284,58036};
    private static final int[] BOLTS={877,9140,9141,9142,9143,9144,63289,63294,63299,63304,58041};
    private Native950DevelopmentAmmo() { }

    static int supplied(int weaponId, int ammoId, boolean enabled) {
        if (!enabled || weaponId < 0) return -1;
        int id=best(weaponId);
        if(id<0)return -1;
        ItemDefinitions supply=Native950CacheItems.definition(id);
        ItemDefinitions actual=ammoId<0?null:Native950CacheItems.definition(ammoId);
        // Preserve a user's equivalent-tier enchanted ammunition; substitute
        // lower tiers as well as empty/incompatible slots in infinite mode.
        if(actual!=null&&actual.itemCategory==supply.itemCategory
                &&actual.getCSOpcode(23,-1)==supply.getCSOpcode(23,-1)
                &&(weaponId!=8880||ammoId==8882))return -1;
        return id;
    }

    static int best(int weaponId) {
        if(weaponId<0)return -1;
        ItemDefinitions weapon = Native950CacheItems.definition(weaponId);
        if (weapon == null || weapon.getCSOpcode(2826) != 1) return -1;
        // Exact950 CS2660: weapon param21 must equal equipped ammo's category.
        // Self-ammunition weapons have no required category.
        int category = weapon.getCSOpcode(21, -1);
        if(category==38)return verified(4740,38); // Karil's variants require bolt racks.
        if(category!=62&&category!=63)return -1;
        if(weaponId==8880)return verified(8882,category);
        int tier=weapon.getCSOpcode(23,weapon.getCSOpcode(750,1));
        int wanted=tier>=90?99:tier;
        int[] progression=category==62?ARROWS:BOLTS;
        // If a weapon falls between ammo tiers, the next tier reaches its full
        // damage cap. Combat still caps ammo damage to the weapon's own tier.
        for(int id:progression) {
            verified(id,category);
            if(Native950CacheItems.definition(id).getCSOpcode(23,-1)>=wanted)return id;
        }
        return verified(progression[progression.length-1],category);
    }
    private static int verified(int id,int category){
        ItemDefinitions supply = Native950CacheItems.definition(id);
        if (supply == null || supply.itemCategory != category || supply.equipSlot != Equipment.SLOT_ARROWS)
            throw new IllegalStateException("Current-cache development ammunition contract changed");
        return id;
    }
}
