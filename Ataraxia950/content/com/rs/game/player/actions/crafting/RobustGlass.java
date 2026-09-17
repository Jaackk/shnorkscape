package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;

/**
 * Handles the Robust Glass Machine object.
 *
 * @author Noel
 */
public class RobustGlass {

    public static void addSandstone(Player player) {
        int redAmount = player.getInventory().getAmountOf(23194);
        int crystalAmount = player.getInventory().getAmountOf(32847);
        if (crystalAmount == 0 && redAmount == 0) {
            player.sendMessage("You don't have any red or crystal-flecked sandstone with you.");
            return;
        }

        sendObjectAnimation(player);

        if(crystalAmount > 0) {
            player.getInventory().deleteItem(32847, crystalAmount);
            player.getInventory().addItem(32845, crystalAmount);
            player.sendMessage("You add crystal-flecked sandstone to the machine and make some crystal glass.", true);
            player.setNextAnimation(new Animation(25120));
        }
        if(redAmount > 0) {
            player.getInventory().deleteItem(23194, redAmount);
            player.getInventory().addItem(23193, redAmount);
            player.sendMessage("You add red sandstone to the machine and make some robust glass.", true);
            player.setNextAnimation(new Animation(25120));
        }
    }

    private static void sendObjectAnimation(Player player) {
        WorldObject object = new WorldObject(94067, 10, 0, 2139, 3350, 1);
        WorldObject object2 = new WorldObject(67968, 10, 0, 2584, 2854, 0);
        if (player.getPlane() == 0)
            World.sendObjectAnimation(object2, new Animation(25041));
        else
            World.sendObjectAnimation(object, new Animation(25041));
    }
}