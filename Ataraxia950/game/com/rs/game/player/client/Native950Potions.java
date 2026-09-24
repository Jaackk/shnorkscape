package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Pots;

/** Admits only real cache Drink operations to the established timed potion owner. */
final class Native950Potions {
    // Combat Alpha's ordinary vial/flask families. The live execution still verifies the ID
    // against Pots, whose static table intentionally requires the paired cache to be open.
    private static final int[] DRINK_IDS={
        2428,121,123,125,113,115,117,119,2432,133,135,137,2444,169,171,173,3040,3042,3044,3046,
        2434,139,141,143,3024,3026,3028,3030,6685,6687,6689,6691,15328,15329,15330,15331,
        15332,15333,15334,15335,23525,23526,23527,23528,23529,23530,23531,23532,23533,23534,23535,23536
    };
    private Native950Potions() { }

    static boolean handles(int itemId, String operation) {
        if(!"Drink".equalsIgnoreCase(operation))return false;
        for(int id:DRINK_IDS)if(id==itemId)return true;
        return false;
    }

    static boolean drink(Player player, int slot, int itemId) {
        Item item=player.getInventory().getItem(slot);
        return item!=null&&item.getId()==itemId&&Pots.getPot(itemId)!=null&&Pots.pot(player,item,slot);
    }
    static void removeOverloadOnDeath(Player player){
        if(player.getOverloadDelay()<=0)return;
        player.setOverloadDelay(0);
        player.getBuffDebuffTimersManager().removeTimer(com.rs.game.player.BuffDebuffTimersManager.Timer.OVERLOADED);
        for(int skill:new int[]{0,1,2,4,6,com.rs.game.player.Skills.NECROMANCY}){
            int base=player.getSkills().getLevelForXp(skill);
            if(player.getSkills().getLevel(skill)>base)player.getSkills().set(skill,base);
        }
    }
}
