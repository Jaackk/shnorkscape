package com.rs.game.player.bots.trading;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.trade.ItemTransaction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MarketBotTrade extends ItemTransaction {

    private final MarketBotSession session;

    public MarketBotTrade(Player bot, MarketBotSession session) {
        super(bot);
        this.session = session;
    }

    public boolean setOfferItems(List<Item> offerItems) {
        synchronized (this) {
            ItemsContainer<Item> normalizedOffer = normalizeOfferItems(offerItems);
            if (hasSameOffer(normalizedOffer)) {
                return false;
            }
            Item[] itemsBefore = items.getItemsCopy();
            items.clear();
            for (Item item : normalizedOffer.getItems()) {
                if (item != null) {
                    items.forceAdd(item);
                }
            }
            refreshItems(itemsBefore);
            if (target != null && target.getItemTransaction() != null) {
                cancelAccepted();
            }
            setTransactionModified(true);
            return true;
        }
    }

    private ItemsContainer<Item> normalizeOfferItems(List<Item> offerItems) {
        ItemsContainer<Item> normalized = new ItemsContainer<>(28, false);
        if (offerItems == null) {
            return normalized;
        }
        for (Item item : offerItems) {
            if (item == null || item.getAmount() <= 0) {
                continue;
            }
            int offerItemId = MarketBotProfile.toTradeOfferItemId(item.getId(), item.getAmount());
            normalized.add(new Item(offerItemId, item.getAmount()).setAttributes(item.getAttributes()));
        }
        return normalized;
    }

    private boolean hasSameOffer(ItemsContainer<Item> offerItems) {
        Item[] current = items.getItems();
        Item[] expected = offerItems.getItems();
        for (int slot = 0; slot < current.length; slot++) {
            if (!sameItem(current[slot], expected[slot])) {
                return false;
            }
        }
        return true;
    }

    private boolean sameItem(Item current, Item expected) {
        if (current == expected) {
            return true;
        }
        if (current == null || expected == null) {
            return false;
        }
        return current.getId() == expected.getId() && current.getAmount() == expected.getAmount();
    }

    @Override
    public boolean canAccept(boolean firstStage) {
        return session.canBotAccept(firstStage);
    }

    @Override
    public boolean canAddItem(Item item) {
        return false;
    }

    @Override
    public boolean canContinueTrade() {
        return true;
    }

    @Override
    public boolean nextStage() {
        boolean advanced = super.nextStage();
        if (advanced) {
            session.onTradeStageAdvanced();
        }
        return advanced;
    }

    @Override
    public boolean canPerformTransaction() {
        return session.canPerformTransaction();
    }

    @Override
    public void openTransactionAction() {
        player.getPackets().sendGlobalString(2504, target.getDisplayName());
    }

    @Override
    public void postAddItem() {
        session.refreshTradeScreenInfo();
    }

    @Override
    public void postSuccessAction(Player oldTarget, CopyOnWriteArrayList<Item> containedItems) {
        session.recordLedger(containedItems);
    }

    @Override
    public void postTradeAction(Player oldTarget, CloseTransactionStage stage) {
        // This IS reached on the success path. When both sides have
        // accepted stage 2, the bot's scheduled accept (always after the
        // player's because of getBotAcceptDelayTicks >= 1) is the one that
        // calls closeTransaction(DONE), so the outer close runs from the
        // bot side and the outer postTradeAction is this method — not
        // MarketPlayerTrade.postTradeAction.
        //
        // For player-initiated closes (CANCEL via UI close, NO_SPACE from
        // distance check, expiry, forceCancel) the outer close starts on
        // the player side and MarketPlayerTrade.postTradeAction handles
        // unlock + finish instead. session.finish guards on `closed`, so
        // double-invocation across the two paths is safe.
        //
        // An earlier version of this method was a no-op based on a wrong
        // trace; that left the human player permanently locked
        // (lockDelay = Long.MAX_VALUE) after every successful trade.
        oldTarget.unlock();
        player.unlock();
        session.finish(stage);
    }

    @Override
    protected boolean shouldReturnOfferedItems(CloseTransactionStage stage) {
        return false;
    }
}
