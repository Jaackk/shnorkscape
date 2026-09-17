package com.rs.game.activites.dnd.eviltree.action;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.entity.EvilWeedsObject;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import com.rs.utils.Logger;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CollectEvilDustAction extends Action {

    private final EvilTree tree;
    private final EvilWeedsObject weedsObject;
    private int freeSlots = -1;

    public CollectEvilDustAction(EvilTree tree, EvilWeedsObject weedsObject) {
        this.tree = tree;
        this.weedsObject = weedsObject;
    }


    @Override
    public boolean process(Player player) {
        return canCollect(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if (freeSlots == -1) {
            Logger.getGlobal().warn("Unexpected: Inventory slots were not computed!");
            return -1;
        }
        player.setNextAnimation(new Animation(830));
        if (freeSlots == 1) {
            player.getInventory().addItem(3325, 1);
        } else {
            player.getInventory().addItem(3325, 2);
        }
        weedsObject.registerTakeDust();
        if (weedsObject.getAshesLeft() <= 0) {
            World.removeObject(weedsObject);
            tree.setWeedsObject(null);
            player.sendMessage("You take the last of the Evil dust from the ash pile!");
        } else {
            player.sendFilteredMessage("You take some Evil dust from the ash pile.");
        }
        freeSlots = -1;
        return 1;
    }

    @Override
    public boolean start(Player player) {
        return canCollect(player);
    }

    @Override
    public void stop(Player player) {

    }

    private boolean canCollect(Player player) {
        if (!weedsObject.isAshes()) {
            return false;
        }
        if (!player.getInventory().containsItem(952, 1) && !player.getToolBelt().contains(952)) {
            player.sendMessage("You need a spade to take the Evil dust.");
            return false;
        }
        freeSlots = player.getInventory().getFreeSlots();
        if (freeSlots == 0) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        if (weedsObject.getAshesLeft() <= 0) {
            return false;
        }
        return tree.getWeedsObject() != null;
    }
}