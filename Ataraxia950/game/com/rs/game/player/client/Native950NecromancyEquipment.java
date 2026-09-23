package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;

/** Exact950 CS2660/7473: conduit admission is server varp11218, not item param9107. */
final class Native950NecromancyEquipment {
    static final int CONDUIT_VAR=11218;
    private Native950NecromancyEquipment() { }
    static boolean conduit(ItemDefinitions item) {
        return item!=null && item.certTemplateId<0 && item.equipSlot==5
                && item.getCSOpcode(8898)==1 && item.getCSOpcode(8899)==1;
    }
    static boolean conduit(Player player) {
        int id=player.getEquipment().getShieldId();
        return id>=0 && Cache.STORE!=null && conduit(Native950CacheItems.definition(id));
    }
    static void publish(Player player) {
        player.getVarsManager().sendVar(CONDUIT_VAR,conduit(player)?1:0);
    }
}
