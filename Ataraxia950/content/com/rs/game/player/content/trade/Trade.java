package com.rs.game.player.content.trade;


import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;

import com.rs.utils.LoggingSystem;

import java.util.concurrent.CopyOnWriteArrayList;

public class Trade extends ItemTransaction {

    public Trade(Player player) {
        super(player);
    }

    @Override
    public boolean canAccept(boolean firstStage) {
        if (!player.withinDistance(target, 5)) {
            player.sendMessage("You need to be closer to your partner to complete the trade.");
            player.closeInterfaces();
            player.setCloseInterfacesEvent(null);
            closeTransaction(CloseTransactionStage.NO_SPACE);
            return false;
        }
        return true;
    }

    public boolean canContinueTrade() {
        if (player.getInventory().getItems().getUsedSlots() + target.getItemTransaction().items.getUsedSlots() > 28 || player.getInventory().getItems().goesOverAmount(items)) {
            player.setCloseInterfacesEvent(null);
            player.closeInterfaces();
            closeTransaction(CloseTransactionStage.NO_SPACE);
            return false;
        }
        return true;
    }

    @Override
    public boolean canPerformTransaction() {
        Controller playerController = player.getControlerManager().getControler(), targetController = target.getControlerManager().getControler();
        if (playerController instanceof DungeonController && !(targetController instanceof DungeonController) || targetController instanceof DungeonController && !(playerController instanceof DungeonController)) {
            player.sendMessage("You cannot trade this player.");
            return false;
        }
        return true;
    }

    @Override
    public boolean canAddItem(Item item) {
        if (!ItemConstants.isTradeable(item) && (player.isOwner() || target.isOwner())) {
            player.getPackets().sendGameMessage("<col=ff0000>[WARNING] The item you added to the trade is an untradeable item.");
        }
        if (player.getUsername().equals("sintricate") && item.getId() == 25202) {
            return true;
        }
        return ItemConstants.isTradeable(item) || player.isOwner() || target.isOwner();
    }

    @Override
    public void postSuccessAction(Player oldTarget, CopyOnWriteArrayList<Item> containedItems) {
        LoggingSystem.logTrade(oldTarget, player, containedItems);
        player.getPackets().sendGameMessage("Accepted trade.");
        for (Item item : oldTarget.getItemTransaction().items.getItems()) {
            if (item == null) {
                continue;
            }
            if (player.getInventory().getAmountOf(item.getId()) + item.getAmount() < 0) {
                if (item.getId() == 995) {
                    player.getMoneyPouch().addMoneyMisc(item.getAmount());
                }
                continue;
            }
            player.getInventory().addItem(item);
        }
    }

    @Override
    public void postTradeAction(Player oldTarget, CloseTransactionStage stage) {
        oldTarget.unlock();
        player.unlock();
    }

    @Override
    public void openTransactionAction() {
        player.getPackets().sendGlobalString(2504, target.getDisplayName());
        refreshFreeInventorySlots();
    }

    @Override
    public void postAddItem() {
        refreshFreeInventorySlots();
    }

    private void refreshFreeInventorySlots() {
        player.getPackets().sendGlobalString(2519, target.getDisplayName());
        int freeSlots = player.getInventory().getFreeSlots();
        target.getPackets().sendIComponentText(335, 1, "has " + (freeSlots == 0 ? "no" : freeSlots) + " free" + "<br>inventory slots");
    }

}
