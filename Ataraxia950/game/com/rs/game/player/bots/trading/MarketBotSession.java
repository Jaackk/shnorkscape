package com.rs.game.player.bots.trading;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.ForceTalk;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.trade.ItemTransaction.CloseTransactionStage;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class MarketBotSession {

    private static final int COINS = 995;
    private static final int OFFER_BUILD_MIN_DELAY_TICKS = 1;
    private static final int OFFER_BUILD_MAX_DELAY_TICKS = 2;
    private static final int OFFER_BUILD_LONG_PAUSE_CHANCE = 8;
    private static final int OFFER_BUILD_BURST_CHANCE = 34;
    private static final int BOT_COIN_RESPONSE_MIN_DELAY_TICKS = 2;
    private static final int BOT_COIN_RESPONSE_MAX_DELAY_TICKS = 4;
    private static final int MAX_TRADE_SLOTS = 28;
    private static final long MESSAGE_COOLDOWN_MS = 2500L;
    private static final String[] LOW_COUNTER_LINES = {
            "Too low. I'd do %s.",
            "Nah, %s and it's yours.",
            "Add a bit. I can do %s.",
            "Meet me at %s.",
            "I need %s for that."
    };
    private static final String[] ACCEPT_COUNTER_LINES = {
            "Alright, put up %s and I'll take it.",
            "Yeah, %s works.",
            "Fine, %s and deal.",
            "Ok, %s."
    };
    private static final String[] SOFT_COUNTER_LINES = {
            "Actually, I can do %s.",
            "I'll come down to %s.",
            "Could do %s if you're quick.",
            "Fine, %s."
    };
    private static final String[] LOWBALL_LINES = {
            "nice try.",
            "lol no.",
            "not even close.",
            "come on."
    };

    private final Player player;
    private final BotPlayer bot;
    private final MarketBotProfile profile;

    private MarketPlayerTrade playerTrade;
    private MarketBotTrade botTrade;
    private List<Item> sellBundle = Collections.emptyList();
    private long openedAt;
    private long expiresAt;
    private int offerDurationTicks;
    private String lastMessage;
    private long lastMessageAt;
    private String lastHaggleSignature;
    private boolean lastHaggleAccepted;
    private int agreedSellPrice;
    private long agreedSellPriceExpiresAt;
    private int lastCounterOfferPrice;
    private long lastCounterOfferExpiresAt;
    private String lastSoftCounterSignature;
    private boolean ledgerRecorded;
    private boolean sendToBankAfterClose;
    private int completedTradeValue;
    // Read by WorldTask runs and finish() outside the tradeLock (e.g. from
    // postTradeAction's recursive close). Volatile makes the flip visible
    // without forcing every read into a synchronized block.
    private volatile boolean closed;
    private int offerBuildVersion;
    private int pendingCoinOfferVersion;
    private String pendingCoinOfferSignature;
    private int pendingCoinOfferQuote;
    private int expiryVersion;
    private int acceptVersion;
    private boolean secondStage;

    public MarketBotSession(Player player, BotPlayer bot, MarketBotProfile profile) {
        this.player = player;
        this.bot = bot;
        this.profile = profile;
    }

    void bind(MarketPlayerTrade playerTrade, MarketBotTrade botTrade) {
        this.playerTrade = playerTrade;
        this.botTrade = botTrade;
    }

    void open() {
        openedAt = Utils.currentTimeMillis();
        secondStage = false;
        acceptVersion = 0;
        offerDurationTicks = profile.getOfferDurationTicks();
        expiresAt = openedAt + offerDurationTicks * 600L;
        List<Item> initialSellBundle = profile.chooseSellBundle();
        setSellBundleSlowly(initialSellBundle);
        player.sendMessage(Colors.CYAN + bot.getDisplayName() + "</col> opens trade.");
        if (!initialSellBundle.isEmpty()) {
            player.sendMessage(Colors.CYAN + bot.getDisplayName() + ":</col> Pick what you want from the lot.");
        } else {
            player.sendMessage(Colors.CYAN + bot.getDisplayName() + "</col> has no sell stock right now, but may still buy items.");
        }
        player.sendMessage(Colors.CYAN + bot.getDisplayName() + "</col> may move on in about "
                + Math.max(1, (offerDurationTicks + 99) / 100) + " minute(s).");
        scheduleExpiry();
    }

    boolean canPerformTransaction() {
        return player != null && bot != null && !player.hasFinished() && !bot.hasFinished();
    }

    boolean canPlayerAccept(boolean firstStage, boolean sendMessage) {
        synchronized (tradeLock()) {
            if (!isExpectedStage(firstStage)) {
                return false;
            }
            Evaluation evaluation = evaluate(sendMessage);
            if (!evaluation.acceptable && sendMessage && evaluation.message != null) {
                say(evaluation.message, true);
            }
            return evaluation.acceptable;
        }
    }

    boolean canBotAccept(boolean firstStage) {
        synchronized (tradeLock()) {
            return isExpectedStage(firstStage) && evaluate(false).acceptable;
        }
    }

    void onPlayerOfferChanged() {
        synchronized (tradeLock()) {
            if (closed) {
                return;
            }
            markTradeStateChanged();
            Evaluation result = evaluate(false);
            // Soft-counter scheduling is a "respond to the player's new
            // offer" side effect, not a "respond to a query" side effect.
            // It only fires here so accept-button clicks (canPlayerAccept /
            // canBotAccept) never roll new negotiations on the same offer.
            if (!result.acceptable && result.hasSoftCounterContext()) {
                maybeScheduleSofterCounter(result.softCounterOfferedCoins,
                        result.softCounterPrice, result.softCounterAsk);
            }
        }
    }

    void scheduleBotAccept(final boolean firstStage) {
        final int expectedAcceptVersion;
        synchronized (tradeLock()) {
            if (closed || !isExpectedStage(firstStage)) {
                return;
            }
            expectedAcceptVersion = acceptVersion;
        }
        final int acceptDelay = profile.getBotAcceptDelayTicks(firstStage);
        if (profile.shouldSendAcceptPauseLine(firstStage)) {
            final String pauseLine = profile.getAcceptPauseLine(firstStage);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    try {
                        synchronized (tradeLock()) {
                            if (isAcceptStillCurrent(firstStage, expectedAcceptVersion)) {
                                say(pauseLine, false);
                            }
                        }
                    } finally {
                        stop();
                    }
                }
            }, Math.max(1, acceptDelay / 2));
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
                public void run() {
                    try {
                        synchronized (tradeLock()) {
                            if (isAcceptStillCurrent(firstStage, expectedAcceptVersion)) {
                                botTrade.accept(firstStage);
                            }
                        }
                } finally {
                    stop();
                }
            }
        }, acceptDelay);
    }

    void onTradeStageAdvanced() {
        synchronized (tradeLock()) {
            if (closed || secondStage) {
                return;
            }
            secondStage = true;
            markTradeStateChanged();
        }
    }

    void recordLedger(CopyOnWriteArrayList<Item> playerOffer) {
        synchronized (tradeLock()) {
            if (ledgerRecorded) {
                return;
            }
            ledgerRecorded = true;
            OfferSummary summary = summarize(playerOffer);
            if (summary.hasNonCoins() && summary.coins > 0) {
                Logger.getGlobal().warn("Market bot mixed-offer ledger fallback for player="
                        + player.getUsername() + ", bot=" + bot.getUsername() + ", coins=" + summary.coins
                        + ", items=" + summary.items);
                if (!sellBundle.isEmpty()) {
                    profile.recordSale(summary.coins, sellBundle);
                    markCompletedTradeValue(summary.coins);
                    player.sendMessage(Colors.GREEN + "You bought " + profile.describeStock(sellBundle)
                            + " from " + bot.getDisplayName() + ".");
                } else {
                    int paid = profile.quoteBuyTotal(summary.items);
                    if (paid > 0) {
                        profile.recordPurchase(paid, summary.items);
                        markCompletedTradeValue(paid);
                        player.sendMessage(Colors.GREEN + bot.getDisplayName() + " bought your items for "
                                + Utils.getFormattedNumber(paid) + " coins.");
                    }
                }
                return;
            }
            if (summary.hasNonCoins()) {
                int paid = profile.quoteBuyTotal(summary.items);
                if (paid > 0) {
                    profile.recordPurchase(paid, summary.items);
                    markCompletedTradeValue(paid);
                    player.sendMessage(Colors.GREEN + bot.getDisplayName() + " bought your items for "
                            + Utils.getFormattedNumber(paid) + " coins.");
                }
                return;
            }
            if (summary.coins > 0 && !sellBundle.isEmpty()) {
                profile.recordSale(summary.coins, sellBundle);
                markCompletedTradeValue(summary.coins);
                player.sendMessage(Colors.GREEN + "You bought " + profile.describeStock(sellBundle)
                        + " from " + bot.getDisplayName() + ".");
            } else {
                Logger.getGlobal().warn("Market bot completed trade with no ledger branch for player="
                        + player.getUsername() + ", bot=" + bot.getUsername() + ", coins=" + summary.coins
                        + ", items=" + summary.items + ", sellBundle=" + sellBundle);
            }
        }
    }

    void finish(CloseTransactionStage stage) {
        if (closed) {
            return;
        }
        closed = true;
        BotTrading.recordTradeClosed(player, bot, stage);
        if (stage == CloseTransactionStage.DONE && sendToBankAfterClose) {
            BotTrading.sendToBank(bot);
        }
    }

    /**
     * Tear down the player's side of the trade UI. Called from
     * {@link BotTrading#unregister(BotPlayer)} when a bot is being despawned
     * so the player isn't stranded in a trade screen pointing at a finished
     * bot. Best-effort: any failure must not block bot teardown.
     */
    void forceCancel() {
        if (closed) {
            return;
        }
        Player target = player;
        MarketPlayerTrade trade = playerTrade;
        if (target == null || target.hasFinished() || trade == null) {
            return;
        }
        try {
            // Tell the player what just happened. closeTransaction's stock
            // CANCEL message ("Other player declined trade!") is sent to the
            // OTHER side (the bot, who can't read it), so without this line
            // the player sees their UI vanish with no explanation.
            target.sendMessage(Colors.CYAN + bot.getDisplayName()
                    + "</col> had to leave. Trade canceled.");
            target.setCloseInterfacesEvent(null);
            target.closeInterfaces();
            trade.closeTransaction(CloseTransactionStage.CANCEL);
        } catch (Exception ignored) {
            // Despawn must stay best-effort.
        }
    }

    List<Item> getSellBundle() {
        return sellBundle;
    }

    private void setSellBundleImmediate(List<Item> items) {
        cancelPendingCoinOffer();
        offerBuildVersion++;
        sellBundle = copyOfferItems(items);
        if (botTrade.setOfferItems(sellBundle)) {
            markTradeStateChanged();
        }
        refreshTradeCenterText(null);
    }

    private void setSellBundleSlowly(List<Item> items) {
        cancelPendingCoinOffer();
        final List<Item> pendingItems = copyOfferItems(items);
        final int version = ++offerBuildVersion;
        sellBundle = Collections.emptyList();
        if (botTrade.setOfferItems(sellBundle)) {
            markTradeStateChanged();
        }
        refreshTradeCenterText(null);
        if (pendingItems.isEmpty()) {
            return;
        }
        WorldTasksManager.schedule(new WorldTask() {
            private int index;

            @Override
            public void run() {
                try {
                    synchronized (tradeLock()) {
                        if (closed || botTrade == null || version != offerBuildVersion || !isOpen()) {
                            stop();
                            return;
                        }
                        List<Item> updatedItems = new ArrayList<>(sellBundle);
                        int addedThisTurn = 0;
                        int toAdd = nextOfferBuildBurstSize(pendingItems.size() - index);
                        while (index < pendingItems.size() && addedThisTurn < toAdd) {
                            Item item = pendingItems.get(index++);
                            updatedItems.add(copyOfferItem(item, item.getAmount()));
                            addedThisTurn++;
                        }
                        sellBundle = updatedItems;
                        if (botTrade.setOfferItems(sellBundle)) {
                            markTradeStateChanged();
                        }
                        if (index >= pendingItems.size()) {
                            stop();
                            return;
                        }
                        getTaskInfo().setRemainingTicks(nextOfferBuildDelayTicks(addedThisTurn));
                    }
                } catch (Exception ex) {
                    // Without this catch, an unexpected throw would unwind
                    // the lock without calling stop(), and the periodic task
                    // would re-fire next tick into the same broken state.
                    Logger.getGlobal().warn("setSellBundleSlowly aborted: " + ex);
                    stop();
                }
            }
        }, nextOfferBuildDelayTicks(1), 1);
    }

    private void setBotOfferItems(List<Item> items) {
        if (botTrade.setOfferItems(copyOfferItems(items))) {
            markTradeStateChanged();
        }
    }

    void refreshTradeScreenInfo() {
        if (player == null || player.hasFinished() || profile == null) {
            return;
        }
        refreshTradeCenterText(null);
    }

    private void refreshTradeCenterText(String text) {
        if (player == null || player.hasFinished() || bot == null) {
            return;
        }
        if (text == null || text.isEmpty()) {
            player.getPackets().sendGlobalString(2504, bot.getDisplayName());
            player.getPackets().sendGlobalString(2519, bot.getDisplayName());
            player.getPackets().sendIComponentText(335, 1, "");
            player.getPackets().sendIComponentText(335, 2, bot.getDisplayName());
            return;
        }
        player.getPackets().sendGlobalString(2504, bot.getDisplayName());
        player.getPackets().sendIComponentText(335, 1, text);
        player.getPackets().sendIComponentText(335, 2, "");
    }

    boolean requestSellBundle(String request) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null) {
                return false;
            }
            MarketBotProfile.RequestedBundle requested = profile.chooseRequestedSellBundle(request);
            if (requested.getItems().isEmpty()) {
                say(requested.getMessage(), true);
                return false;
            }
            setSellBundleImmediate(requested.getItems());
            resetNegotiationState();
            resetOfferExpiry();
            int ask = profile.getSellBundlePrice(sellBundle, getTimePressureDiscountBps());
            say(requested.getMessage() + " I'd want " + Utils.getFormattedNumber(ask) + " coins.", true);
            return true;
        }
    }

    /**
     * Right-click "Select" on a bot offer slot. The bot keeps that one entry
     * in the sell bundle and gradually removes everything else, 1-2 entries
     * per game tick, so the trade screen shows the bot dropping items rather
     * than the entire bundle disappearing in one frame.
     */
    boolean selectOnlyOfferSlot(int slot) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null) {
                return false;
            }
            Item item = botTrade.getItemsContainer().get(slot);
            if (item == null || item.getAmount() <= 0 || item.getId() == COINS) {
                return false;
            }
            int keptStockItemId = MarketBotProfile.toStockItemId(item.getId());
            Item keptInBundle = null;
            for (Item offered : sellBundle) {
                if (offered != null && offered.getId() == keptStockItemId) {
                    keptInBundle = offered;
                    break;
                }
            }
            if (keptInBundle == null) {
                say("That one is gone. Check what I'm offering now.", true);
                return true;
            }
            // Prefer the displayed (noted-form) name from the trade UI rather
            // than the un-noted sellBundle name, so the bot's reply matches
            // what the player just clicked. Other slot operations
            // (removeOfferSlot, requestOfferSlotAmount) already use this.
            String displayName = item.getName().toLowerCase();
            int othersToRemove = 0;
            for (Item offered : sellBundle) {
                if (offered != null && offered.getId() != keptStockItemId) {
                    othersToRemove++;
                }
            }
            if (othersToRemove == 0) {
                int ask = profile.getSellBundlePrice(sellBundle, getTimePressureDiscountBps());
                say("Already just the " + displayName
                        + ". I'd want " + Utils.getFormattedNumber(ask) + " coins.", true);
                return true;
            }
            say("Alright, just the " + displayName + " then.", true);
            scheduleSelectOnly(keptStockItemId, displayName);
            return true;
        }
    }

    private void scheduleSelectOnly(final int keptStockItemId, final String displayName) {
        // Bump offerBuildVersion so any in-flight setSellBundleSlowly task
        // halts before fighting us for sellBundle.
        final int version = ++offerBuildVersion;
        // The bundle is shrinking, so any agreed/counter prices keyed on the
        // old composition no longer apply.
        resetNegotiationState();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    synchronized (tradeLock()) {
                        if (closed || version != offerBuildVersion || !isOpen()) {
                            stop();
                            return;
                        }
                        int toRemove = ThreadLocalRandom.current().nextInt(1, 3); // 1 or 2
                        List<Item> updated = new ArrayList<>(sellBundle.size());
                        int removed = 0;
                        for (Item offered : sellBundle) {
                            if (offered == null || offered.getAmount() <= 0) {
                                continue;
                            }
                            if (offered.getId() == keptStockItemId) {
                                updated.add(copyOfferItem(offered, offered.getAmount()));
                            } else if (removed < toRemove) {
                                removed++;
                                // Drop this entry — don't add it to `updated`.
                            } else {
                                updated.add(copyOfferItem(offered, offered.getAmount()));
                            }
                        }
                        sellBundle = updated;
                        if (botTrade.setOfferItems(sellBundle)) {
                            markTradeStateChanged();
                        }
                        boolean done = true;
                        for (Item offered : sellBundle) {
                            if (offered != null && offered.getId() != keptStockItemId) {
                                done = false;
                                break;
                            }
                        }
                        if (done) {
                            finishSelectOnly(keptStockItemId, displayName);
                            stop();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getGlobal().warn("scheduleSelectOnly aborted: " + ex);
                    stop();
                }
            }
        }, 1, 1);
    }

    private void finishSelectOnly(int keptStockItemId, String displayName) {
        // Refresh the offer expiry so the player gets a full window to
        // decide on the narrowed-down bundle, then announce the new price.
        resetOfferExpiry();
        if (sellBundle.isEmpty()) {
            // Defensive: a concurrent path emptied the bundle entirely. Tell
            // the player honestly instead of quoting the 1-coin floor of an
            // empty getSellBundlePrice on top of a "Just the X" line that no
            // longer applies.
            say("Looks like the " + displayName + " is gone too. Sorry.", true);
            return;
        }
        int ask = profile.getSellBundlePrice(sellBundle, getTimePressureDiscountBps());
        say("Just the " + displayName + ". For that I'd want " + Utils.getFormattedNumber(ask) + " coins.", true);
    }

    boolean removeOfferSlot(int slot) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null) {
                return false;
            }
            Item item = botTrade.getItemsContainer().get(slot);
            if (item == null || item.getAmount() <= 0 || item.getId() == COINS) {
                return false;
            }
            int stockItemId = MarketBotProfile.toStockItemId(item.getId());
            List<Item> updatedItems = new ArrayList<>();
            boolean changed = false;
            int removeAmount = Math.max(1, item.getAmount());
            for (Item offered : sellBundle) {
                if (offered == null || offered.getAmount() <= 0) {
                    continue;
                }
                if (!changed && offered.getId() == stockItemId) {
                    changed = true;
                    if (offered.getAmount() > removeAmount) {
                        updatedItems.add(copyOfferItem(offered, offered.getAmount() - removeAmount));
                    }
                    continue;
                }
                updatedItems.add(copyOfferItem(offered, offered.getAmount()));
            }
            if (!changed) {
                say("That one is gone. Check what I'm offering now.", true);
                return true;
            }
            setSellBundleImmediate(updatedItems);
            resetNegotiationState();
            resetOfferExpiry();
            if (sellBundle.isEmpty()) {
                say("Took out the " + item.getName().toLowerCase() + ". Nothing left in that lot.", true);
                return true;
            }
            int ask = profile.getSellBundlePrice(sellBundle, getTimePressureDiscountBps());
            say("Took out the " + item.getName().toLowerCase() + ". For the rest I'd want "
                    + Utils.getFormattedNumber(ask) + " coins.", true);
            return true;
        }
    }

    boolean requestOfferSlotAmount(int slot, int amount) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null || amount <= 0) {
                return false;
            }
            Item item = botTrade.getItemsContainer().get(slot);
            if (item == null || item.getAmount() <= 0 || item.getId() == COINS) {
                return false;
            }
            int stockItemId = MarketBotProfile.toStockItemId(item.getId());
            int selectedAmount = Math.min(amount, item.getAmount());
            List<Item> updatedItems = new ArrayList<>();
            boolean changed = false;
            for (Item offered : sellBundle) {
                if (offered == null || offered.getAmount() <= 0) {
                    continue;
                }
                if (!changed && offered.getId() == stockItemId) {
                    updatedItems.add(copyOfferItem(offered, selectedAmount));
                    changed = true;
                } else {
                    updatedItems.add(copyOfferItem(offered, offered.getAmount()));
                }
            }
            if (!changed) {
                say("That one is gone. Check what I'm offering now.", true);
                return true;
            }
            if (!profile.hasStock(updatedItems)) {
                say("That stock is gone. Check what I'm offering now.", true);
                return true;
            }
            setSellBundleImmediate(updatedItems);
            resetNegotiationState();
            resetOfferExpiry();
            int ask = profile.getSellBundlePrice(sellBundle, getTimePressureDiscountBps());
            String onlyHave = amount > item.getAmount()
                    ? "I've only got " + Utils.getFormattedNumber(item.getAmount()) + " there. "
                    : "";
            say(onlyHave + "Alright, " + Utils.getFormattedNumber(selectedAmount) + " "
                    + item.getName().toLowerCase() + ". For the lot I'd want "
                    + Utils.getFormattedNumber(ask) + " coins.", true);
            return true;
        }
    }

    boolean sendOfferValue(int slot) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null) {
                return false;
            }
            Item item = botTrade.getItemsContainer().get(slot);
            if (item == null || item.getAmount() <= 0 || item.getId() == COINS) {
                return false;
            }
            int stockItemId = MarketBotProfile.toStockItemId(item.getId());
            int amount = Math.max(1, item.getAmount());
            int unitPrice = profile.getSellPrice(stockItemId, amount, getTimePressureDiscountBps());
            int guidePrice = GrandExchange.getPrice(stockItemId);
            refreshTradeCenterText("Bot: " + Utils.getFormattedNumber(unitPrice)
                    + "<br>Guide: " + (guidePrice > 0 ? Utils.getFormattedNumber(guidePrice) : "unknown"));
            return true;
        }
    }

    boolean handleChatCounterOffer(String message) {
        synchronized (tradeLock()) {
            if (closed || botTrade == null || sellBundle.isEmpty()) {
                return false;
            }
            int offeredCoins = MarketBotProfile.parsePriceFromText(message);
            if (offeredCoins <= 0) {
                return false;
            }
            int pressureDiscountBps = getTimePressureDiscountBps();
            int ask = profile.getSellBundlePrice(sellBundle, pressureDiscountBps);
            if (profile.exceedsTradeLimit(Math.max(offeredCoins, ask))) {
                say(profile.getTradeLimitLine(), true);
                return true;
            }
            int counter = Math.min(ask,
                    getFreshCounterPrice(profile.getCounterOfferPrice(sellBundle, offeredCoins, pressureDiscountBps)));
            if (offeredCoins >= counter) {
                agreeAtPrice(Math.min(offeredCoins, ask));
                say(formatTradeLine(ACCEPT_COUNTER_LINES, formatCompactPrice(Math.min(offeredCoins, ask))), true);
                return true;
            }
            rememberCounter(counter);
            say(formatTradeLine(LOW_COUNTER_LINES, formatCompactPrice(counter)), true);
            maybeScheduleSofterCounter(offeredCoins, counter, ask);
            return true;
        }
    }

    private Evaluation evaluate(boolean sendPassiveMessage) {
        if (!isOpen()) {
            return Evaluation.reject(null);
        }
        if (Utils.currentTimeMillis() >= expiresAt) {
            setSellBundleImmediate(Collections.<Item>emptyList());
            return Evaluation.reject("That offer expired. I'm heading to the bank.");
        }
        OfferSummary summary = summarize(playerTrade.getItemsContainer().getItems());
        if (summary.isEmpty()) {
            cancelPendingCoinOffer();
            setBotOfferItems(sellBundle);
            return Evaluation.reject(sellBundle.isEmpty()
                    ? "Show me something from my buy list."
                    : "Offer coins if you want this lot, or offer items if you want me to buy.");
        }
        if (summary.coins > 0 && summary.hasNonCoins()) {
            cancelPendingCoinOffer();
            cancelOfferBuild();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("One deal at a time. Coins for my items, or items for my coins.");
        }
        if (summary.hasNonCoins()) {
            return evaluatePlayerSelling(summary, sendPassiveMessage);
        }
        return evaluatePlayerBuying(summary, sendPassiveMessage);
    }

    private Evaluation evaluatePlayerSelling(OfferSummary summary, boolean sendPassiveMessage) {
        cancelOfferBuild();
        // Check broke first so the player doesn't get a misleading "I'm not
        // buying X right now" — when gpBudget is depleted, quoteBuyPrice
        // returns -1 for every item regardless of whether the bot would
        // ordinarily buy them.
        if (profile.isOutOfCash()) {
            cancelPendingCoinOffer();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("I'm out of cash right now. Try again later.");
        }
        String invalid = findInvalidBuyItem(summary.items);
        if (invalid != null) {
            cancelPendingCoinOffer();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("I'm not buying " + invalid + " right now.");
        }
        // Distinguish "I'd buy this but not that many" from the generic
        // cash/demand failure below, so the player knows to drop the stack
        // size instead of guessing what's wrong.
        String overLimit = findOverLimitItem(summary.items);
        if (overLimit != null) {
            cancelPendingCoinOffer();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("I don't need that many " + overLimit.toLowerCase() + " right now.");
        }
        int quote = profile.quoteBuyTotal(summary.items);
        if (quote < 0) {
            cancelPendingCoinOffer();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("I don't have enough cash for all that.");
        }
        if (profile.exceedsTradeLimit(quote)) {
            cancelPendingCoinOffer();
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject(profile.getTradeLimitLine());
        }
        if (isBotOfferingCoinQuote(quote)) {
            clearPendingCoinOffer();
            return Evaluation.accept();
        }
        scheduleDelayedCoinOffer(summary.items, quote);
        return Evaluation.reject(sendPassiveMessage ? "Give me a second to count that." : null);
    }

    private Evaluation evaluatePlayerBuying(OfferSummary summary, boolean sendPassiveMessage) {
        cancelPendingCoinOffer();
        if (sellBundle.isEmpty()) {
            setBotOfferItems(Collections.<Item>emptyList());
            return Evaluation.reject("I'm not selling anything useful right now.");
        }
        if (!profile.hasStock(sellBundle)) {
            setSellBundleImmediate(profile.chooseSellBundle());
            return Evaluation.reject("That stock is gone. Check the new offer.");
        }
        setBotOfferItems(sellBundle);
        int pressureDiscountBps = getTimePressureDiscountBps();
        int ask = profile.getSellBundlePrice(sellBundle, pressureDiscountBps);
        if (profile.exceedsTradeLimit(ask)) {
            return Evaluation.reject(profile.getTradeLimitLine());
        }
        int effectiveAsk = getEffectiveAsk(ask);
        if (summary.coins >= effectiveAsk) {
            return Evaluation.accept();
        }
        if (isScamSensitiveLowball(summary.coins, ask)) {
            return Evaluation.reject(pickTradeLine(LOWBALL_LINES));
        }
        int haggleChance = profile.getHaggleChanceBps(summary.coins, effectiveAsk);
        if (haggleChance > 0 && rollHaggle(summary.coins, effectiveAsk, haggleChance)) {
            if (sendPassiveMessage) {
                say(formatTradeLine(ACCEPT_COUNTER_LINES, formatCompactPrice(summary.coins)), false);
            }
            return Evaluation.accept();
        }
        int counter = Math.min(effectiveAsk,
                getFreshCounterPrice(profile.getCounterOfferPrice(sellBundle, summary.coins, pressureDiscountBps)));
        rememberCounter(counter);
        // Don't schedule the softer counter here — that's a random roll and
        // would re-fire on every accept-button click. onPlayerOfferChanged is
        // the single owner of negotiation-starting side effects; it reads
        // the context off the returned Evaluation.
        return Evaluation.rejectWithCounter(
                formatTradeLine(LOW_COUNTER_LINES, formatCompactPrice(counter)),
                summary.coins, counter, effectiveAsk);
    }

    private int getEffectiveAsk(int ask) {
        long now = Utils.currentTimeMillis();
        if (agreedSellPrice > 0 && agreedSellPriceExpiresAt >= now) {
            return Math.max(1, Math.min(ask, agreedSellPrice));
        }
        if (agreedSellPrice > 0) {
            agreedSellPrice = 0;
            agreedSellPriceExpiresAt = 0L;
        }
        if (lastCounterOfferPrice > 0 && lastCounterOfferExpiresAt >= now) {
            return Math.max(1, Math.min(ask, lastCounterOfferPrice));
        }
        if (lastCounterOfferPrice > 0) {
            lastCounterOfferPrice = 0;
            lastCounterOfferExpiresAt = 0L;
        }
        return ask;
    }

    private int getFreshCounterPrice(int fallback) {
        long now = Utils.currentTimeMillis();
        if (lastCounterOfferPrice > 0 && lastCounterOfferExpiresAt >= now) {
            return Math.max(1, lastCounterOfferPrice);
        }
        return Math.max(1, fallback);
    }

    private void rememberCounter(int price) {
        lastCounterOfferPrice = Math.max(1, price);
        lastCounterOfferExpiresAt = Utils.currentTimeMillis() + profile.getCounterOfferHoldMillis();
    }

    private void scheduleDelayedCoinOffer(Map<Integer, Integer> offeredItems, final int quote) {
        final String itemSignature = describeSignature(offeredItems);
        final String pendingSignature = quote + ":" + itemSignature;
        if (pendingSignature.equals(pendingCoinOfferSignature) && pendingCoinOfferQuote == quote) {
            return;
        }
        pendingCoinOfferSignature = pendingSignature;
        pendingCoinOfferQuote = quote;
        if (!isBotOfferEmpty()) {
            setBotOfferItems(Collections.<Item>emptyList());
        }
        final int expectedAcceptVersion = acceptVersion;
        final int version = ++pendingCoinOfferVersion;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    synchronized (tradeLock()) {
                        if (closed || !isOpen() || version != pendingCoinOfferVersion
                                || expectedAcceptVersion != acceptVersion
                                || !pendingSignature.equals(pendingCoinOfferSignature)) {
                            return;
                        }
                        OfferSummary current = summarize(playerTrade.getItemsContainer().getItems());
                        if (current.coins > 0 || !current.hasNonCoins()
                                || !itemSignature.equals(describeSignature(current.items))) {
                            return;
                        }
                        int currentQuote = profile.quoteBuyTotal(current.items);
                        if (currentQuote != quote || currentQuote < 0 || profile.exceedsTradeLimit(currentQuote)) {
                            return;
                        }
                        setBotOfferItems(Collections.singletonList(new Item(COINS, quote)));
                        clearPendingCoinOffer();
                    }
                } finally {
                    stop();
                }
            }
        }, nextBotCoinResponseDelayTicks());
    }

    private boolean isBotOfferingCoinQuote(int quote) {
        OfferSummary botOffer = summarize(botTrade.getItemsContainer().getItems());
        return botOffer.coins == quote && !botOffer.hasNonCoins();
    }

    private boolean isBotOfferEmpty() {
        return summarize(botTrade.getItemsContainer().getItems()).isEmpty();
    }

    private void cancelPendingCoinOffer() {
        pendingCoinOfferVersion++;
        clearPendingCoinOffer();
    }

    private void clearPendingCoinOffer() {
        pendingCoinOfferSignature = null;
        pendingCoinOfferQuote = 0;
    }

    private void agreeAtPrice(int price) {
        agreedSellPrice = Math.max(1, price);
        agreedSellPriceExpiresAt = Utils.currentTimeMillis() + profile.getCounterOfferHoldMillis();
        rememberCounter(agreedSellPrice);
        // Surface the agreed price in the trade UI so the player has visible
        // confirmation that the chat-haggle landed. Without this, the screen
        // still showed the bot's display name and the player had to trust
        // that adding the haggled coin amount would be accepted. The text
        // gets overwritten by the next refreshTradeCenterText / sendOfferValue
        // call (e.g. when the player offers coins or clicks Value), which is
        // fine — it only needs to bridge the moment between agreement and
        // the player adjusting their offer.
        refreshTradeCenterText("Agreed at " + formatCompactPrice(agreedSellPrice)
                + ".<br>Offer coins to confirm.");
    }

    private void resetNegotiationState() {
        lastHaggleSignature = null;
        lastHaggleAccepted = false;
        agreedSellPrice = 0;
        agreedSellPriceExpiresAt = 0L;
        lastCounterOfferPrice = 0;
        lastCounterOfferExpiresAt = 0L;
        lastSoftCounterSignature = null;
    }

    private void resetOfferExpiry() {
        openedAt = Utils.currentTimeMillis();
        offerDurationTicks = profile.getOfferDurationTicks();
        expiresAt = openedAt + offerDurationTicks * 600L;
        scheduleExpiry();
    }

    private boolean isScamSensitiveLowball(int offeredCoins, int ask) {
        return offeredCoins > 0 && ask > 0
                && profile.getStyle() == MarketBotProfile.PersonalityStyle.SCAM_SENSITIVE
                && ((long) offeredCoins * 10000L) / ask < 1000L;
    }

    private void maybeScheduleSofterCounter(final int offeredCoins, final int counter, final int ask) {
        if (offeredCoins <= 0 || counter <= offeredCoins + 1 || closed || !isOpen()) {
            return;
        }
        final String signature = offeredCoins + ":" + counter + ":" + describeSignature(sellBundle);
        if (signature.equals(lastSoftCounterSignature)) {
            return;
        }
        // Consume the signature BEFORE rolling, so a player who spam-clicks
        // accept on the same low offer can't re-roll the 10% softer-counter
        // chance until they actually change their offer (which bumps the
        // bundle signature via the player items).
        lastSoftCounterSignature = signature;
        if (ThreadLocalRandom.current().nextInt(100) >= 10) {
            return;
        }
        final int expectedAcceptVersion = acceptVersion;
        int gap = Math.max(1, counter - offeredCoins);
        final int softerCounter = Math.max(offeredCoins + 1,
                counter - Math.max(1, (gap * ThreadLocalRandom.current().nextInt(18, 36)) / 100));
        final int delay = ThreadLocalRandom.current().nextInt(14, 51);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    // All other scheduled tasks in this file run their work
                    // inside synchronized(tradeLock()); the soft-counter task
                    // used to be the only one that didn't, which left
                    // lastSoftCounterSignature / acceptVersion / sellBundle
                    // reads (and the agreeAtPrice / say writes below)
                    // unprotected. On a single-threaded world tick that's
                    // accidentally safe, but the inconsistency is a hazard.
                    synchronized (tradeLock()) {
                        if (!closed && isOpen() && signature.equals(lastSoftCounterSignature)
                                && expectedAcceptVersion == acceptVersion
                                && profile.hasStock(sellBundle) && !profile.exceedsTradeLimit(Math.min(ask, softerCounter))) {
                            agreeAtPrice(Math.min(ask, softerCounter));
                            say(formatTradeLine(SOFT_COUNTER_LINES, formatCompactPrice(Math.min(ask, softerCounter))), true);
                        }
                    }
                } finally {
                    stop();
                }
            }
        }, delay);
    }

    private void scheduleExpiry() {
        final int version = ++expiryVersion;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    synchronized (tradeLock()) {
                        if (!closed && isOpen() && version == expiryVersion
                                && Utils.currentTimeMillis() >= expiresAt) {
                            say("I need to bank. Offer expired.", true);
                            player.setCloseInterfacesEvent(null);
                            player.closeInterfaces();
                            playerTrade.closeTransaction(CloseTransactionStage.CANCEL);
                            BotTrading.sendToBank(bot);
                        }
                    }
                } finally {
                    stop();
                }
            }
        }, Math.max(1, offerDurationTicks));
    }

    private int getTimePressureDiscountBps() {
        if (openedAt <= 0 || expiresAt <= openedAt) {
            return 0;
        }
        return profile.getTimePressureDiscountBps(openedAt, expiresAt);
    }

    private Object tradeLock() {
        // Lock on playerTrade rather than botTrade so the session lock order
        // matches ItemTransaction's convention of "playerTrade outer,
        // botTrade inner". With botTrade as the lock, paths like
        // MarketBotTrade.setOfferItems -> cancelAccepted touched playerTrade
        // fields without holding playerTrade, and scheduleExpiry's
        // playerTrade.closeTransaction(...) call inverted the order. Both go
        // away once tradeLock is playerTrade — either we already hold the
        // outer lock, or we re-acquire it reentrantly through the inner code.
        return playerTrade == null ? this : playerTrade;
    }

    private void cancelOfferBuild() {
        offerBuildVersion++;
    }

    private void markTradeStateChanged() {
        acceptVersion++;
    }

    private boolean isExpectedStage(boolean firstStage) {
        return firstStage != secondStage;
    }

    private boolean isAcceptStillCurrent(boolean firstStage, int expectedAcceptVersion) {
        return !closed && isOpen()
                && expectedAcceptVersion == acceptVersion
                && isExpectedStage(firstStage)
                && evaluate(false).acceptable;
    }

    private void markCompletedTradeValue(int value) {
        completedTradeValue = Math.max(completedTradeValue, Math.max(0, value));
        if (completedTradeValue >= profile.getBigTradeThreshold()) {
            sendToBankAfterClose = true;
        }
    }

    private boolean rollHaggle(int offeredCoins, int askingPrice, int chanceBps) {
        String signature = offeredCoins + ":" + askingPrice + ":" + describeSignature(sellBundle);
        if (!signature.equals(lastHaggleSignature)) {
            lastHaggleSignature = signature;
            lastHaggleAccepted = ThreadLocalRandom.current().nextInt(10000) < chanceBps;
        }
        return lastHaggleAccepted;
    }

    /**
     * Looks for an item the bot won't buy at all (untradeable or not in the
     * bot's buy list). Distinct from {@link #findOverLimitItem} so the player
     * gets a useful message instead of the generic "I don't have enough cash
     * or demand for that many" when the issue is just stack size.
     */
    private String findInvalidBuyItem(Map<Integer, Integer> items) {
        for (Integer itemId : items.keySet()) {
            if (!ItemConstants.isTradeable(new Item(itemId, 1)) || profile.quoteBuyPrice(itemId, 1) < 0) {
                return new Item(itemId, 1).getName();
            }
        }
        return null;
    }

    /**
     * Looks for an item the bot WOULD buy at amount 1, but rejects at the
     * actual offered amount — i.e. the player tried to dump more than the
     * bot's buy limit. Returns null if every item is within its limit.
     */
    private String findOverLimitItem(Map<Integer, Integer> items) {
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int itemId = entry.getKey();
            int amount = entry.getValue();
            if (amount <= 1) {
                continue;
            }
            // findInvalidBuyItem already ruled out "doesn't buy at all"; here
            // we're only flagging items the bot DOES buy but not in this
            // quantity. Calling quoteBuyPrice with amount=1 again is cheap
            // (synchronized map lookup) and keeps the two checks symmetric.
            if (profile.quoteBuyPrice(itemId, 1) >= 0
                    && profile.quoteBuyPrice(itemId, amount) < 0) {
                return new Item(itemId, 1).getName();
            }
        }
        return null;
    }

    private boolean isOpen() {
        return playerTrade != null && botTrade != null
                && playerTrade.getTarget() == bot
                && botTrade.getTarget() == player
                && !player.hasFinished()
                && !bot.hasFinished();
    }

    private void say(String message, boolean force) {
        if (message == null || message.isEmpty()) {
            return;
        }
        long now = Utils.currentTimeMillis();
        if (!force && message.equals(lastMessage) && now - lastMessageAt < MESSAGE_COOLDOWN_MS) {
            return;
        }
        lastMessage = message;
        lastMessageAt = now;
        sendPrivateTradeMessage(message);
        bot.setNextForceTalk(new ForceTalk(message));
    }

    private void sendPrivateTradeMessage(String message) {
        String displayName = bot.getDisplayName();
        player.getPackets().receivePrivateMessage(Utils.formatPlayerNameForDisplay(displayName), displayName, 0, message);
    }

    private static OfferSummary summarize(Item[] items) {
        OfferSummary summary = new OfferSummary();
        if (items == null) {
            return summary;
        }
        for (Item item : items) {
            if (item == null || item.getAmount() <= 0) {
                continue;
            }
            if (item.getId() == COINS) {
                summary.coins = safeAdd(summary.coins, item.getAmount());
            } else {
                int stockItemId = MarketBotProfile.toStockItemId(item.getId());
                Integer current = summary.items.get(stockItemId);
                summary.items.put(stockItemId, safeAdd(current == null ? 0 : current, item.getAmount()));
            }
        }
        return summary;
    }

    private static OfferSummary summarize(List<Item> items) {
        return summarize(items == null ? null : items.toArray(new Item[items.size()]));
    }

    private static int safeAdd(int a, int b) {
        long total = (long) a + b;
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    private static String describeSignature(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Item item : items) {
            if (item != null) {
                parts.add(item.getId() + "x" + item.getAmount());
            }
        }
        Collections.sort(parts);
        return parts.toString();
    }

    private static String describeSignature(Map<Integer, Integer> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                parts.add(entry.getKey() + "x" + entry.getValue());
            }
        }
        Collections.sort(parts);
        return parts.toString();
    }

    private static Item copyOfferItem(Item item, int amount) {
        return new Item(item.getId(), Math.max(1, amount)).setAttributes(item.getAttributes());
    }

    private static int nextOfferBuildBurstSize(int remainingItems) {
        if (remainingItems <= 1 || ThreadLocalRandom.current().nextInt(100) >= OFFER_BUILD_BURST_CHANCE) {
            return 1;
        }
        if (remainingItems >= 3 && ThreadLocalRandom.current().nextInt(100) < 18) {
            return 3;
        }
        return 2;
    }

    private static int nextOfferBuildDelayTicks(int lastBurstSize) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int delay = random.nextInt(OFFER_BUILD_MIN_DELAY_TICKS, OFFER_BUILD_MAX_DELAY_TICKS + 1);
        if (lastBurstSize > 1 && random.nextBoolean()) {
            delay += 1;
        }
        if (random.nextInt(100) < OFFER_BUILD_LONG_PAUSE_CHANCE) {
            delay += random.nextInt(1, 3);
        }
        return delay;
    }

    private static int nextBotCoinResponseDelayTicks() {
        return ThreadLocalRandom.current().nextInt(BOT_COIN_RESPONSE_MIN_DELAY_TICKS,
                BOT_COIN_RESPONSE_MAX_DELAY_TICKS + 1);
    }

    private static List<Item> copyOfferItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Item> copies = new ArrayList<>();
        int usedSlots = 0;
        for (Item item : items) {
            if (item == null || item.getAmount() <= 0) {
                continue;
            }
            int amount = item.getAmount();
            int slots = tradeSlotCount(item.getId(), amount);
            if (usedSlots + slots > MAX_TRADE_SLOTS) {
                int availableSlots = MAX_TRADE_SLOTS - usedSlots;
                if (availableSlots <= 0) {
                    break;
                }
                if (usesOneTradeSlot(item.getId(), amount)) {
                    continue;
                }
                amount = Math.min(amount, availableSlots);
                slots = tradeSlotCount(item.getId(), amount);
            }
            copies.add(copyOfferItem(item, amount));
            usedSlots += slots;
        }
        return copies.isEmpty() ? Collections.<Item>emptyList() : copies;
    }

    private static int tradeSlotCount(int itemId, int amount) {
        return usesOneTradeSlot(itemId, amount) ? 1 : Math.max(1, amount);
    }

    private static boolean usesOneTradeSlot(int itemId, int amount) {
        if (amount <= 1) {
            return true;
        }
        int offerItemId = MarketBotProfile.toTradeOfferItemId(itemId, amount);
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(offerItemId);
        return definitions == null || definitions.isStackable() || definitions.isNoted();
    }

    private static String formatTradeLine(String[] pool, String value) {
        return String.format(pickTradeLine(pool), value);
    }

    private static String pickTradeLine(String[] pool) {
        if (pool == null || pool.length == 0) {
            return "";
        }
        return pool[ThreadLocalRandom.current().nextInt(pool.length)];
    }

    private static String formatCompactPrice(int coins) {
        if (coins >= 1_000_000) {
            long scaled = ((long) coins * 100L + 500_000L) / 1_000_000L;
            long whole = scaled / 100L;
            long fraction = scaled % 100L;
            if (fraction == 0) {
                return whole + "m";
            }
            if (fraction % 10L == 0) {
                return whole + "." + (fraction / 10L) + "m";
            }
            return whole + "." + (fraction < 10 ? "0" : "") + fraction + "m";
        }
        if (coins >= 10_000) {
            long scaled = ((long) coins + 500L) / 1_000L;
            return scaled + "k";
        }
        return Utils.getFormattedNumber(coins) + " coins";
    }

    private static final class OfferSummary {
        private int coins;
        private final Map<Integer, Integer> items = new TreeMap<>();

        private boolean isEmpty() {
            return coins <= 0 && items.isEmpty();
        }

        private boolean hasNonCoins() {
            return !items.isEmpty();
        }
    }

    /**
     * Result of evaluating the trade state. The acceptable / message fields
     * are the verdict; the soft-counter triple is context that the
     * offer-changed path may use to schedule a follow-up softer counter.
     *
     * Carrying the soft-counter context out instead of scheduling inside
     * {@link #evaluatePlayerBuying} means the random-roll lives in exactly
     * one caller — {@link #onPlayerOfferChanged} — and the accept paths
     * (canPlayerAccept / canBotAccept) are guaranteed not to start new
     * negotiations. Earlier this method scheduled directly, so spam-clicking
     * accept could re-roll the 10% soft-counter chance until something
     * landed.
     */
    private static final class Evaluation {
        private final boolean acceptable;
        private final String message;
        private final int softCounterOfferedCoins;
        private final int softCounterPrice;
        private final int softCounterAsk;

        private Evaluation(boolean acceptable, String message,
                int softCounterOfferedCoins, int softCounterPrice, int softCounterAsk) {
            this.acceptable = acceptable;
            this.message = message;
            this.softCounterOfferedCoins = softCounterOfferedCoins;
            this.softCounterPrice = softCounterPrice;
            this.softCounterAsk = softCounterAsk;
        }

        private static Evaluation accept() {
            return new Evaluation(true, null, 0, 0, 0);
        }

        private static Evaluation reject(String message) {
            return new Evaluation(false, message, 0, 0, 0);
        }

        private static Evaluation rejectWithCounter(String message,
                int offeredCoins, int counterPrice, int ask) {
            return new Evaluation(false, message, offeredCoins, counterPrice, ask);
        }

        private boolean hasSoftCounterContext() {
            return softCounterOfferedCoins > 0 && softCounterPrice > 0 && softCounterAsk > 0;
        }
    }
}
