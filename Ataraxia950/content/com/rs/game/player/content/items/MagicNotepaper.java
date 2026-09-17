package com.rs.game.player.content.items;


import com.rs.cache.filestore.io.InputStream;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Colors;



/**
 * Handles the Magic notepaper item.
 *
 * @author Noel
 */
public class MagicNotepaper {

    public static void noteViaPaper (Player player, Item usedWith, Item itemUsed) {
        int amountUsed = player.getInventory().getAmountOf(usedWith.getId());
        int paperHeld = player.getInventory().getAmountOf(itemUsed.getId());
        if (usedWith.getDefinitions().isNoted() || usedWith.getDefinitions().isStackable() || usedWith.getDefinitions().getCertId() == -1) {
            player.sm("You can't note this item.");
            return;
        }
        else if ( paperHeld < amountUsed) {

            if (usedWith.getDefinitions().getCertId() != -1) {
                if (!player.getInventory().containsItem(usedWith.getId(), 1))



                    return;
                if (player.getInventory().getFreeSlots() >= 1 || player.getInventory().contains(usedWith.getDefinitions().getCertId())) {
                    player.getInventory().deleteItem(usedWith.getId(), paperHeld);
                    player.getInventory().deleteItem(30372, paperHeld);
                    player.getInventory().addItem((usedWith.getDefinitions().getCertId()), paperHeld);
                    player.sendMessage("You did not have enough magic notepaper to note all of your" +usedWith.getName()+"!");

                } else
                    player.sm("You need at least one free inventory space to do this.");
            }


        }


        else if (usedWith.getDefinitions().getCertId() != -1) {
            if (!player.getInventory().containsItem(usedWith.getId(), 1))



            return;
            if (player.getInventory().getFreeSlots() >= 1 || player.getInventory().contains(usedWith.getDefinitions().getCertId())) {
                player.getInventory().deleteItem(usedWith.getId(), amountUsed);
                player.getInventory().deleteItem(30372, amountUsed);
                player.getInventory().addItem((usedWith.getDefinitions().getCertId()), amountUsed);

                player.sendMessage("Your items have been noted!");
            } else
                player.sm("You need at least one free inventory space to do this.");
        }

    }

    public static void noteViaItem (Player player, Item usedWith, Item itemUsed) {
        int paperHeld = player.getInventory().getAmountOf(usedWith.getId());
        int amountUsed = player.getInventory().getAmountOf(itemUsed.getId());
        if (itemUsed.getDefinitions().isNoted() || itemUsed.getDefinitions().isStackable() || itemUsed.getDefinitions().getCertId() == -1) {
            player.sm("You can't note this item.");
            return;
        }
        else if ( paperHeld < amountUsed) {

            if (itemUsed.getDefinitions().getCertId() != -1) {
                if (!player.getInventory().containsItem(itemUsed.getId(), 1))



                    return;
                if (player.getInventory().getFreeSlots() >= 1 || player.getInventory().contains(itemUsed.getDefinitions().getCertId())) {
                    player.getInventory().deleteItem(itemUsed.getId(), paperHeld);
                    player.getInventory().deleteItem(30372, paperHeld);
                    player.getInventory().addItem((itemUsed.getDefinitions().getCertId()), paperHeld);
                    player.sendMessage("You did not have enough magic notepaper to note all of your " + itemUsed.getName()+"!");

                } else
                    player.sm("You need at least one free inventory space to do this.");
            }


        }


        else if (itemUsed.getDefinitions().getCertId() != -1 || player.getInventory().contains(itemUsed.getDefinitions().getCertId())) {
            if (!player.getInventory().containsItem(itemUsed.getId(), 1))



                return;
            if (player.getInventory().getFreeSlots() >= 1 || player.getInventory().contains(itemUsed.getDefinitions().getCertId())) {
                player.getInventory().deleteItem(itemUsed.getId(), paperHeld);
                player.getInventory().deleteItem(30372, amountUsed);
                player.getInventory().addItem((itemUsed.getDefinitions().getCertId()), amountUsed);
                player.sendMessage("Your items have been noted!");

            } else
                player.sm("You need at least one free inventory space to do this.");
        }




    }
}