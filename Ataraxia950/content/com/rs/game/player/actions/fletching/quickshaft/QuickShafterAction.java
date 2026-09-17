package com.rs.game.player.actions.fletching.quickshaft;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class QuickShafterAction extends Action {

    private QuickShaftable nextLog;

    @Override
    public boolean process(Player player) {
        return nextLog != null;
    }

    @Override
    public int processWithDelay(Player player) {
        if (nextLog != null && player.getInventory().deleteOneItem(new Item(nextLog.id, 1))) {
            player.setNextAnimation(new Animation(24938));
            player.getSkills().addXp(Skills.FLETCHING, nextLog.exp);
            player.sendFilteredMessage("You fletch the " + nextLog.formattedName + " into " + nextLog.amount + " arrow shafts.");
            player.getInventory().addItem(new Item(QuickShafter.ARROW_SHAFT, nextLog.amount));
            setNextLog(player);
            return 2;
        }
        return -1;
    }

    @Override
    public boolean start(Player player) {
        setNextLog(player);
        if (nextLog == null) {
            player.sendMessage("You do not have any logs that you can quick-shaft.");
            return false;
        }
        if (ItemDefinitions.getItemDefinitions(nextLog.id).isStackable() && player.getInventory().getFreeSlots() == 0) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        return true;
    }

    @Override
    public void stop(Player player) {
    }

    private void setNextLog(Player player) {
        for (Item item : player.getInventory().getItems().getItems()) {
            if (item == null) {
                continue;
            }
            QuickShaftable log = QuickShaftable.ALL.get(item.getId());
            if (log != null) {
                if (player.getSkills().getLevel(Skills.FLETCHING) >= log.level) {
                    nextLog = log;
                    return;
                }
                player.sendMessage("You need a Fletching level of " + log.level + " to quick-shaft " + log.formattedName + ".");
            }
        }
    }
}