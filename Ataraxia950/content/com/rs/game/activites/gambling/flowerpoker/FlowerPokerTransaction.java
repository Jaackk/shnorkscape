package com.rs.game.activites.gambling.flowerpoker;

import com.rs.game.activites.gambling.GamblingAreaController;
import com.rs.game.activites.gambling.flowerpoker.session.FlowerPokerGame;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.trade.ItemTransaction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class FlowerPokerTransaction extends ItemTransaction {

    private boolean transactionEnded;

    public FlowerPokerTransaction(Player player) {
        super(player);
    }

    @Override
    public boolean canAccept(boolean firstStage) {
        for (Item item : items.getItems()) {
            if (item != null) {
                long otherCount = target.getItemTransaction().getItemsContainer().getNumberOf(item);
                long total = item.getAmount() + otherCount;
                if (total > Integer.MAX_VALUE) {
                    player.sendMessage(item.getName() + " goes over the limit of " + NumberFormat.getInstance(Locale.US).format(Integer.MAX_VALUE) + " when added together.");
                    return false;
                }
                long total2 = player.getTotalItemCount(item) + total;
                if (total2 > Integer.MAX_VALUE) {
                    player.sendMessage(item.getName() + " goes over the limit of " + NumberFormat.getInstance(Locale.US).format(Integer.MAX_VALUE) + " for you.");
                    return false;
                }
            }
        }
        for (Item item : target.getItemTransaction().getItemsContainer().getItems()) {
            if (item != null) {
                long otherCount = items.getNumberOf(item);
                long total = item.getAmount() + otherCount;
                if (total > Integer.MAX_VALUE) {
                    player.sendMessage(item.getName() + " goes over the limit of " + NumberFormat.getInstance(Locale.US).format(Integer.MAX_VALUE) + " when added together.");
                    return false;
                }
                long total2 = player.getTotalItemCount(item) + total;
                if (total2 > Integer.MAX_VALUE) {
                    player.sendMessage(item.getName() + " goes over the limit of " + NumberFormat.getInstance(Locale.US).format(Integer.MAX_VALUE) + " for " + target.getDisplayName() + ".");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canAddItem(Item item) {
        /*if (item.getId() == 995) {
            player.sendMessage("You can't use coins here; use shards from the Pet shop owner instead!");
            return false;
        }*/
        return ItemConstants.isTradeable(item);
    }

    @Override
    public void postSuccessAction(Player oldTarget, CopyOnWriteArrayList<Item> containedItems) {
        if (oldTarget.getItemTransaction() instanceof FlowerPokerTransaction) {
            FlowerPokerTransaction targetTransaction = (FlowerPokerTransaction) oldTarget.getItemTransaction();
            if (targetTransaction.transactionEnded || transactionEnded) {
                return;
            }
            List<Item> myItems = new CopyOnWriteArrayList<>();
            for (Item item : items.getItems()) {
                if (item != null) {
                    myItems.add(item);
                }
            }
            FlowerPokerSession game = new FlowerPokerGame(player, oldTarget, myItems, containedItems);
            player.setFlowerPokerSession(game);
            oldTarget.setFlowerPokerSession(game);
            game.start();
            transactionEnded = true;
        }
    }

    @Override
    public void postTradeAction(Player oldTarget, CloseTransactionStage stage) {
        if (stage != CloseTransactionStage.DONE) {
            oldTarget.unlock();
            player.unlock();
        }
        player.getTemporaryAttributtes().remove(FlowerPokerSession.BETTING_FLOWER_POKER_KEY);
        oldTarget.getTemporaryAttributtes().remove(FlowerPokerSession.BETTING_FLOWER_POKER_KEY);
    }

    @Override
    public boolean canContinueTrade() {
        return true;
    }

    @Override
    public void openTransactionAction() {
        player.getTemporaryAttributtes().put(FlowerPokerSession.BETTING_FLOWER_POKER_KEY, true);
        player.getPackets().sendGlobalString(2504, target.getDisplayName());
        WorldTasksManager.schedule(new WorldTask() {
            
            @Override
            public void run() {
                player.getPackets().sendExecuteScript(8420, 21954563, 21954565, -1, 21954566, "Gambling With: " + target.getDisplayName(), 21218, 1007);
            }
        });
    }

    @Override
    public boolean canPerformTransaction() {
        return player.getControlerManager().getControler() instanceof GamblingAreaController
                && target.getControlerManager().getControler() instanceof GamblingAreaController;
    }

    @Override
    public void postAddItem() {
    }
}
