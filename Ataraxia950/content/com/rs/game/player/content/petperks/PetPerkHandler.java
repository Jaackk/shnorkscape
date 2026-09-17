package com.rs.game.player.content.petperks;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 21/07/2019
 * #Shnek6969
 */
public class PetPerkHandler {

    /**
     * handles the extra arms perk
     * @param player
     * @param item
     */
    public static void handleExtraArms(Player player, Item item){
        if(!PetPerkUtils.petHasPerk(player.getCurrentPet(), PetPerk.EXTRA_ARMS)) {
            return;
        }
        int tier = PetPerkUtils.getPerkTier(PetPerk.EXTRA_ARMS, player.getCurrentPet());
        int proMultiplier = tier == 1 ? 2 : tier == 2 ? 4 : 8;
        int conMultiplier = tier == 1 ? 2 : tier == 2 ? 1 : 0;
        if(Utils.random(100) < proMultiplier) {
           player.getBank().addItem(new Item(item.getId(), item.getAmount()), true);
           player.sendMessage(Colors.RED+"Your Extra arms perk gave you an extra resource, it has been added to your bank.", true);
        }
        if(Utils.random(100) < conMultiplier){
            player.getInventory().deleteItem(item.getId(),item.getAmount());
            player.sm(Colors.RED+"Your Extra arms perk removed a resource.");
        }
    }

    /**
     * handles the efficiency perk
     * @param player
     */
    public static boolean handleEfficiencyExpert(Player player, Item... ingredients){
        if(!PetPerkUtils.petHasPerk(player.getCurrentPet(), PetPerk.EFFICIENCY_EXPERT)) {
            return false;
        }
        int tier = PetPerkUtils.getPerkTier(PetPerk.EFFICIENCY_EXPERT, player.getCurrentPet());
        int proMultiplier = tier == 1 ? 2 : tier == 2 ? 4 : 8;
        int conMultiplier = tier == 1 ? 1 : tier == 2 ? 1 : 0;
        if(Utils.random(100) < proMultiplier) {
            player.sm(Colors.GREEN+"Your Efficiency perk saves you some ingredients!");
            return true;
        }
        if(Utils.random(100) < conMultiplier){
            for(Item next : ingredients)
                player.getInventory().deleteItem(new Item(next.getId(), next.getAmount() *2));
            player.sm(Colors.RED+"Your Efficiency perk makes you use twice the ingredients!");
            return true;
        }
        return false;
    }
}
