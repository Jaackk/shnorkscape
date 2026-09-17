package com.rs.game.player.bots.trading;

import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.bots.BotLocations;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.bots.IdleCrowdBotScript;
import com.rs.game.player.content.trade.ItemTransaction.CloseTransactionStage;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class BotTrading {

    private static final Map<String, MarketBotProfile> PROFILES = new ConcurrentHashMap<>();
    private static final Map<String, MarketBotSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, String> PENDING_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, Long> PENDING_REQUEST_EXPIRES_AT = new ConcurrentHashMap<>();
    private static final Map<String, Long> TRADE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> QUICK_CLOSES = new ConcurrentHashMap<>();
    private static final Map<String, Long> CHAT_TRADE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<String, Long> BOT_TRADE_REQUEST_EXPIRES_AT = new ConcurrentHashMap<>();
    private static final Map<String, Long> REPLENISHMENT_TOKENS = new ConcurrentHashMap<>();
    private static final Map<String, Long> PROFILE_REROLL_TOKENS = new ConcurrentHashMap<>();
    private static final Map<String, MeetingRequest> MEETINGS = new ConcurrentHashMap<>();
    private static final Map<String, Long> PRIVATE_MEET_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long OPEN_COOLDOWN_MS = 3500L;
    private static final long CHAT_TRADE_COOLDOWN_MS = 18000L;
    private static final long PENDING_REQUEST_TTL_MS = 60000L;
    private static final long PRIVATE_MEET_COOLDOWN_MS = 10000L;
    private static final long BOT_TRADE_REQUEST_TTL_MS = 30000L;
    private static final long STATE_SWEEP_INTERVAL_MS = 60000L;
    private static final long MEETING_WAIT_MS = 30000L;
    private static final long MEETING_TRAVEL_TIMEOUT_MS = 45000L;
    private static final int MEETING_ROUTE_RADIUS = 2;
    private static final int MEETING_ARRIVAL_RADIUS = 2;
    private static final int MEETING_ROUTE_STEP_LIMIT = 25;
    private static final int TRADE_OPEN_RANGE = 8;
    private static final int BOT_TRADE_REQUEST_RANGE = 2;
    private static final int ADVERT_APPROACH_RADIUS = 1;
    private static final int ADVERT_APPROACH_RETRY_TICKS = 2;
    private static final int ADVERT_APPROACH_TIMEOUT_TICKS = 80;
    private static final int ADVERT_APPROACH_MAX_ROUTE_FAILURES = 4;
    private static final int BOT_TRADE_REQUEST_TTL_TICKS = 50;
    private static final int MIN_REPLENISH_TICKS = 500;
    private static final int MAX_REPLENISH_TICKS = 1500;
    private static final int MIN_PROFILE_REROLL_TICKS = 5000;
    private static final int MAX_PROFILE_REROLL_TICKS = 7000;
    private static final int MIN_PROFILE_REROLL_RETRY_TICKS = 200;
    private static final int MAX_PROFILE_REROLL_RETRY_TICKS = 400;
    private static final int INFO_RANGE = 3;
    private static final int CHAT_RESPONSE_RANGE = 24;
    private static final WorldTile[] GE_BANK_TILES = {
            new WorldTile(3164, 3482, 0),
            new WorldTile(3176, 3492, 0),
            new WorldTile(3164, 3502, 0),
            new WorldTile(3152, 3492, 0)
    };
    private static volatile long nextStateSweepAt;
    private static final MeetingSpot[] GE_MEETING_SPOTS = {
            new MeetingSpot("north", new WorldTile(3164, 3510, 0)),
            new MeetingSpot("west", new WorldTile(3145, 3494, 0)),
            new MeetingSpot("south", new WorldTile(3164, 3472, 0)),
            new MeetingSpot("east", new WorldTile(3183, 3491, 0))
    };

    private BotTrading() {
    }

    public static void register(BotPlayer bot, IdleCrowdBotScript.Role role) {
        if (bot == null || bot.getUsername() == null) {
            return;
        }
        if (!MarketBotProfile.isTradingRole(role)) {
            unregister(bot);
            return;
        }
        PROFILES.put(bot.getUsername(), MarketBotProfile.forRole(role));
        scheduleReplenishment(bot);
        scheduleProfileReroll(bot);
    }

    public static void unregister(BotPlayer bot) {
        if (bot == null || bot.getUsername() == null) {
            return;
        }
        String botUsername = bot.getUsername();
        // Per-pair maps are keyed "playerUsername->botUsername". Drop every
        // entry referencing this bot so despawn/respawn cycles don't leak
        // entries forever.
        String suffix = "->" + botUsername;
        // Tear down any active trade BEFORE clearing the maps so the player
        // isn't left staring at a dead trade UI. forceCancel() will cause
        // recordTradeClosed() to remove the session from ACTIVE_SESSIONS;
        // ConcurrentHashMap's iterator is safe with that concurrent removal.
        for (Map.Entry<String, MarketBotSession> entry : ACTIVE_SESSIONS.entrySet()) {
            if (entry.getKey().endsWith(suffix)) {
                entry.getValue().forceCancel();
            }
        }
        PROFILES.remove(botUsername);
        TRADE_COOLDOWNS.keySet().removeIf(key -> key.endsWith(suffix));
        QUICK_CLOSES.keySet().removeIf(key -> key.endsWith(suffix));
        PENDING_REQUESTS.keySet().removeIf(key -> key.endsWith(suffix));
        PENDING_REQUEST_EXPIRES_AT.keySet().removeIf(key -> key.endsWith(suffix));
        ACTIVE_SESSIONS.keySet().removeIf(key -> key.endsWith(suffix));
        PRIVATE_MEET_COOLDOWNS.keySet().removeIf(key -> key.endsWith(suffix));
        BOT_TRADE_REQUEST_EXPIRES_AT.keySet().removeIf(key -> key.endsWith(suffix));
        MEETINGS.remove(botUsername);
        REPLENISHMENT_TOKENS.remove(botUsername);
        PROFILE_REROLL_TOKENS.remove(botUsername);
        // CHAT_TRADE_COOLDOWNS is keyed on player username, not bot — leave alone.
    }

    public static boolean startTrade(Player player, BotPlayer bot) {
        if (player == null || bot == null || player.hasFinished() || bot.hasFinished()) {
            return false;
        }
        MarketBotProfile profile = PROFILES.get(bot.getUsername());
        if (profile == null) {
            player.sendMessage(bot.getDisplayName() + " isn't looking to trade right now.");
            return false;
        }
        long now = Utils.currentTimeMillis();
        maybeSweepExpiredState(now);
        String key = cooldownKey(player, bot);
        BOT_TRADE_REQUEST_EXPIRES_AT.remove(key);
        if (bot.getTemporaryAttributtes().get("TradeTarget") == player) {
            bot.getTemporaryAttributtes().remove("TradeTarget");
        }
        Long cooldownUntil = TRADE_COOLDOWNS.get(key);
        if (cooldownUntil != null && cooldownUntil > now) {
            long seconds = Math.max(1L, (cooldownUntil - now + 999L) / 1000L);
            player.sendMessage(bot.getDisplayName() + " wants a moment before trading again (" + seconds + "s).");
            return false;
        }
        if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            player.sendMessage(bot.getDisplayName() + " is already trading with someone else.");
            return false;
        }
        if (player.getItemTransaction() != null && player.getItemTransaction().isInTransaction()) {
            player.sendMessage("You are already in a trade.");
            return false;
        }
        if (!player.withinDistance(bot, TRADE_OPEN_RANGE)) {
            player.sendMessage("You need to be closer to trade with " + bot.getDisplayName() + ".");
            return false;
        }
        synchronized (player) {
            synchronized (bot) {
                if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
                    player.sendMessage(bot.getDisplayName() + " is already trading with someone else.");
                    return false;
                }
                if (player.getItemTransaction() != null && player.getItemTransaction().isInTransaction()) {
                    player.sendMessage("You are already in a trade.");
                    return false;
                }
                // canTrade has side effects (sends messages, may tear down a
                // stale transaction on the player) so it has to run before
                // we reserve the cooldown — but inside the locks so the
                // bot/player state it inspects can't shift mid-check.
                if (!player.canTrade(bot)) {
                    return false;
                }
                long reservedUntil = now + OPEN_COOLDOWN_MS;
                Long previous = TRADE_COOLDOWNS.putIfAbsent(key, reservedUntil);
                boolean reservationOwned;
                if (previous == null) {
                    reservationOwned = true;
                } else if (previous > now) {
                    long seconds = Math.max(1L, (previous - now + 999L) / 1000L);
                    player.sendMessage(bot.getDisplayName() + " wants a moment before trading again (" + seconds + "s).");
                    return false;
                } else {
                    // Stale cooldown — try to take ownership atomically.
                    reservationOwned = TRADE_COOLDOWNS.replace(key, previous, reservedUntil);
                    if (!reservationOwned) {
                        player.sendMessage(bot.getDisplayName() + " is busy. Try again in a moment.");
                        return false;
                    }
                }
                boolean committed = false;
                MarketPlayerTrade playerTrade = null;
                MarketBotTrade botTrade = null;
                try {
                    MarketBotSession session = new MarketBotSession(player, bot, profile);
                    playerTrade = new MarketPlayerTrade(player, session);
                    botTrade = new MarketBotTrade(bot, session);
                    session.bind(playerTrade, botTrade);
                    player.setItemTransaction(playerTrade);
                    bot.setItemTransaction(botTrade);
                    player.getItemTransaction().openTransaction(bot);
                    bot.getItemTransaction().openTransaction(player);
                    // ItemTransaction.openTransaction sets target *before*
                    // canPerformTransaction and just returns on failure, so
                    // target is always set regardless of outcome — the lock
                    // state is the actual signal. Both sides only get locked
                    // on the success path; on failure neither is locked and
                    // the player would otherwise be stranded with
                    // isInTransaction()==true and no UI.
                    if (!player.isLocked() || !bot.isLocked()) {
                        return false;
                    }
                    session.open();
                    ACTIVE_SESSIONS.put(key, session);
                    String requested = PENDING_REQUESTS.remove(key);
                    PENDING_REQUEST_EXPIRES_AT.remove(key);
                    MeetingRequest meeting = MEETINGS.get(bot.getUsername());
                    if (meeting != null && player.getUsername().equals(meeting.playerUsername)) {
                        if (requested == null) {
                            requested = meeting.requestText;
                        }
                        MEETINGS.remove(bot.getUsername(), meeting);
                    }
                    if (requested != null) {
                        session.requestSellBundle(requested);
                    }
                    committed = true;
                    return true;
                } finally {
                    if (!committed) {
                        // Release the reservation we just placed — without
                        // this the player would wait OPEN_COOLDOWN_MS for
                        // a trade that never opened.
                        if (reservationOwned) {
                            TRADE_COOLDOWNS.remove(key, reservedUntil);
                        }
                        // Tear down any half-opened transaction state from
                        // the silent canPerformTransaction failure path so
                        // the player isn't stranded in isInTransaction().
                        try {
                            if (playerTrade != null && playerTrade.getTarget() != null) {
                                playerTrade.closeTransaction(CloseTransactionStage.CANCEL);
                            } else if (botTrade != null && botTrade.getTarget() != null) {
                                botTrade.closeTransaction(CloseTransactionStage.CANCEL);
                            }
                        } catch (Exception ignored) {
                            // Don't let cleanup failure mask the original
                            // error path — startTrade has already returned
                            // false / is propagating an exception.
                        }
                    }
                }
            }
        }
    }

    public static void recordTradeClosed(Player player, BotPlayer bot, CloseTransactionStage stage) {
        if (player == null || bot == null) {
            return;
        }
        String key = cooldownKey(player, bot);
        ACTIVE_SESSIONS.remove(key);
        long now = Utils.currentTimeMillis();
        if (stage == CloseTransactionStage.DONE) {
            QUICK_CLOSES.remove(key);
            TRADE_COOLDOWNS.put(key, now + 3000L);
            return;
        }
        int closes = 1;
        Integer current = QUICK_CLOSES.get(key);
        if (current != null) {
            closes = current + 1;
        }
        QUICK_CLOSES.put(key, closes);
        long cooldown = Math.min(38000L, 7000L + closes * 5000L
                + ThreadLocalRandom.current().nextLong(0L, 2500L));
        TRADE_COOLDOWNS.put(key, now + cooldown);
        if (closes >= 2 && !bot.hasFinished()) {
            bot.setNextForceTalk(new ForceTalk(closes >= 4 ? "stop wasting time" : "one sec"));
        }
    }

    public static void requestItem(final Player player, final BotPlayer bot) {
        if (!canUseInfoOption(player, bot)) {
            return;
        }
        final MarketBotProfile profile = PROFILES.get(bot.getUsername());
        if (profile == null) {
            sayToPlayer(player, bot, "I'm not looking to trade right now.");
            return;
        }
        player.faceEntity(bot);
        player.sendInputString("What do you want to buy from " + bot.getDisplayName() + "?", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String raw = getString();
                if (raw == null) {
                    return;
                }
                String request = raw.trim();
                if (request.isEmpty()) {
                    return;
                }
                if (!canUseInfoOption(player, bot)) {
                    return;
                }
                String key = cooldownKey(player, bot);
                MarketBotSession session = ACTIVE_SESSIONS.get(key);
                if (session != null && session.requestSellBundle(request)) {
                    return;
                }
                PENDING_REQUESTS.put(key, request);
                PENDING_REQUEST_EXPIRES_AT.put(key, Utils.currentTimeMillis() + PENDING_REQUEST_TTL_MS);
                startTrade(player, bot);
            }
        });
    }

    public static String getTradeChatter(BotPlayer bot, boolean preferBuying) {
        if (bot == null || bot.getUsername() == null) {
            return null;
        }
        MarketBotProfile profile = PROFILES.get(bot.getUsername());
        return profile == null ? null : profile.getTradeChatter(preferBuying);
    }

    public static String getTraderSpecialty(BotPlayer bot) {
        if (bot == null || bot.getUsername() == null) {
            return "none";
        }
        MarketBotProfile profile = PROFILES.get(bot.getUsername());
        return profile == null ? "none" : profile.getSpecialty().name().toLowerCase();
    }

    public static int randomMarketChatEffect() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextInt(100) >= 45) {
            return 0;
        }
        int moveEffect;
        switch (random.nextInt(4)) {
            case 0:
                moveEffect = 2; // wave2
                break;
            case 1:
                moveEffect = 3; // shake
                break;
            case 2:
                moveEffect = 4; // scroll
                break;
            default:
                moveEffect = 1; // wave
                break;
        }
        return moveEffect & 0xff;
    }

    public static void handlePlayerPublicChat(final Player player, String message) {
        if (player == null || player.hasFinished() || player.isBot() || message == null) {
            return;
        }
        if (player.getItemTransaction() != null && player.getItemTransaction().isInTransaction()) {
            handleOpenTradeRequest(player, message);
            return;
        }
        long now = Utils.currentTimeMillis();
        maybeSweepExpiredState(now);
        Long until = CHAT_TRADE_COOLDOWNS.get(player.getUsername());
        if (until != null && until > now) {
            return;
        }
        BotPlayer bestBot = null;
        MarketBotProfile.BuyAdvertMatch bestMatch = null;
        MarketBotProfile bestProfile = null;
        int bestDistance = Integer.MAX_VALUE;
        Collection<BotPlayer> bots = BotManager.getBots();
        for (BotPlayer bot : bots) {
            if (bot == null || bot.hasFinished() || bot.isDead() || !player.withinDistance(bot, CHAT_RESPONSE_RANGE)) {
                continue;
            }
            // Skip bots that are already trading someone — we don't want them
            // shouting offers or pathing to a new partner mid-transaction.
            if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
                continue;
            }
            MarketBotProfile profile = PROFILES.get(bot.getUsername());
            if (profile == null) {
                continue;
            }
            MarketBotProfile.BuyAdvertMatch match = profile.matchPlayerSellingAdvert(message);
            if (match == null) {
                match = profile.matchPlayerBuyingAdvert(message);
            }
            if (match == null) {
                continue;
            }
            int distance = Math.max(Math.abs(player.getX() - bot.getX()), Math.abs(player.getY() - bot.getY()));
            if (distance < bestDistance) {
                bestDistance = distance;
                bestBot = bot;
                bestMatch = match;
                bestProfile = profile;
            }
        }
        if (bestBot == null || bestMatch == null || bestProfile == null) {
            return;
        }
        CHAT_TRADE_COOLDOWNS.put(player.getUsername(), now + CHAT_TRADE_COOLDOWN_MS);
        scheduleAdvertResponse(player, bestBot, bestProfile, bestMatch);
    }

    private static void scheduleAdvertResponse(final Player player, final BotPlayer bot,
            final MarketBotProfile profile, final MarketBotProfile.BuyAdvertMatch match) {
        final int responseDelay = profile.getAdvertResponseDelayTicks();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (player.hasFinished() || bot.hasFinished() || bot.isDead()
                            || !player.withinDistance(bot, CHAT_RESPONSE_RANGE)) {
                        return;
                    }
                    // Bot may have entered a trade during the response delay
                    // (e.g. answered a private meet request). Don't yell at a
                    // second player while mid-transaction.
                    if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
                        return;
                    }
                    bot.faceEntity(player);
                    bot.setNextForceTalk(new ForceTalk(match.getResponse()));
                    if (isPlayerSellingAdvert(match)) {
                        scheduleAdvertTradeRequest(player, bot, profile, match);
                        return;
                    }
                    if (!player.withinDistance(bot, TRADE_OPEN_RANGE)) {
                        startAdvertMeeting(player, bot, match);
                        return;
                    }
                    scheduleAdvertTradeRequest(player, bot, profile, match);
                } finally {
                    stop();
                }
            }
        }, responseDelay);
    }

    private static void scheduleAdvertTradeRequest(final Player player, final BotPlayer bot,
            MarketBotProfile profile, final MarketBotProfile.BuyAdvertMatch match) {
        final int tradeDelay = profile.getAdvertTradeDelayTicks();
        if (isPlayerSellingAdvert(match)) {
            scheduleAdvertApproachTradeRequest(player, bot, match, tradeDelay);
            return;
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (!player.hasFinished() && !bot.hasFinished() && !bot.isDead()
                            && player.withinDistance(bot, TRADE_OPEN_RANGE)) {
                        sendBotTradeRequest(player, bot, match == null ? null : match.getRequestText());
                    }
                } finally {
                    stop();
                }
            }
        }, tradeDelay);
    }

    private static void scheduleAdvertApproachTradeRequest(final Player player, final BotPlayer bot,
            final MarketBotProfile.BuyAdvertMatch match, int initialDelay) {
        WorldTasksManager.schedule(new WorldTask() {
            private int elapsedTicks;
            private int routeFailures;

            @Override
            public void run() {
                try {
                    if (player.hasFinished() || bot.hasFinished() || bot.isDead()) {
                        stop();
                        return;
                    }
                    if ((bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction())
                            || (player.getItemTransaction() != null && player.getItemTransaction().isInTransaction())) {
                        stop();
                        return;
                    }
                    if (!player.withinDistance(bot, CHAT_RESPONSE_RANGE)) {
                        stop();
                        return;
                    }
                    if (player.withinDistance(bot, BOT_TRADE_REQUEST_RANGE)) {
                        sendBotTradeRequest(player, bot, match == null ? null : match.getRequestText());
                        stop();
                        return;
                    }
                    if (elapsedTicks >= ADVERT_APPROACH_TIMEOUT_TICKS
                            || routeFailures >= ADVERT_APPROACH_MAX_ROUTE_FAILURES) {
                        stop();
                        return;
                    }
                    bot.faceEntity(player);
                    if (!bot.hasWalkSteps() && bot.getRouteEvent() == null) {
                        if (routeNear(bot, player, ADVERT_APPROACH_RADIUS)) {
                            routeFailures = 0;
                        } else {
                            routeFailures++;
                        }
                    }
                    elapsedTicks += ADVERT_APPROACH_RETRY_TICKS;
                } catch (Exception ex) {
                    // Periodic task: without this catch, an unexpected throw
                    // would unwind without stop() and the task would re-fire
                    // every ADVERT_APPROACH_RETRY_TICKS ticks into the same
                    // broken state.
                    Logger.getGlobal().warn("scheduleAdvertApproachTradeRequest aborted: " + ex);
                    stop();
                }
            }
        }, Math.max(1, initialDelay), ADVERT_APPROACH_RETRY_TICKS);
    }

    private static boolean sendBotTradeRequest(final Player player, final BotPlayer bot, String requestText) {
        if (player == null || bot == null || player.hasFinished() || bot.hasFinished() || bot.isDead()) {
            return false;
        }
        if (!player.withinDistance(bot, BOT_TRADE_REQUEST_RANGE)) {
            return false;
        }
        if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            return false;
        }
        if (player.getItemTransaction() != null && player.getItemTransaction().isInTransaction()) {
            return false;
        }
        if (!player.canTrade(bot)) {
            return false;
        }
        String key = cooldownKey(player, bot);
        Long cooldownUntil = TRADE_COOLDOWNS.get(key);
        long now = Utils.currentTimeMillis();
        if (cooldownUntil != null && cooldownUntil > now) {
            return false;
        }
        if (requestText != null && !requestText.isEmpty()) {
            PENDING_REQUESTS.put(key, requestText);
            PENDING_REQUEST_EXPIRES_AT.put(key, now + PENDING_REQUEST_TTL_MS);
        }
        final long expiresAt = now + BOT_TRADE_REQUEST_TTL_MS;
        BOT_TRADE_REQUEST_EXPIRES_AT.put(key, expiresAt);
        bot.getTemporaryAttributtes().put("TradeTarget", player);
        bot.faceEntity(player);
        player.getPackets().sendTradeRequestMessage(bot);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    String key = cooldownKey(player, bot);
                    Long currentExpiresAt = BOT_TRADE_REQUEST_EXPIRES_AT.get(key);
                    if (currentExpiresAt == null || currentExpiresAt.longValue() == expiresAt) {
                        if (currentExpiresAt != null) {
                            BOT_TRADE_REQUEST_EXPIRES_AT.remove(key, currentExpiresAt);
                        }
                        if (bot.getTemporaryAttributtes().get("TradeTarget") == player) {
                            bot.getTemporaryAttributtes().remove("TradeTarget");
                        }
                    }
                } finally {
                    stop();
                }
            }
        }, BOT_TRADE_REQUEST_TTL_TICKS);
        return true;
    }

    private static boolean isPlayerSellingAdvert(MarketBotProfile.BuyAdvertMatch match) {
        return match != null && (match.getRequestText() == null || match.getRequestText().isEmpty());
    }

    private static void queueAdvertRequest(Player player, BotPlayer bot, MarketBotProfile.BuyAdvertMatch match) {
        if (match == null || match.getRequestText() == null || match.getRequestText().isEmpty()) {
            return;
        }
        String key = cooldownKey(player, bot);
        PENDING_REQUESTS.put(key, match.getRequestText());
        PENDING_REQUEST_EXPIRES_AT.put(key, Utils.currentTimeMillis() + PENDING_REQUEST_TTL_MS);
    }

    public static boolean handlePrivateMessage(Player player, BotPlayer bot, String message) {
        if (player == null || bot == null || bot.getUsername() == null
                || player.hasFinished() || bot.hasFinished() || player.isBot() || message == null) {
            return false;
        }
        if (PROFILES.get(bot.getUsername()) == null) {
            return false;
        }
        String normalized = message.toLowerCase().trim();
        if (!isMeetRequest(normalized)) {
            return false;
        }
        if (!player.canTrade(bot)) {
            sendBotPrivateMessage(player, bot, "I can't trade you right now.");
            return true;
        }
        long now = Utils.currentTimeMillis();
        maybeSweepExpiredState(now);
        MeetingRequest active = MEETINGS.get(bot.getUsername());
        if (active != null) {
            if (!active.isExpired(now)) {
                sendBotPrivateMessage(player, bot, "I'm already heading to the "
                        + active.spot.name + " side of the GE.");
                return true;
            }
            MEETINGS.remove(bot.getUsername(), active);
        }
        String key = cooldownKey(player, bot);
        Long cooldownUntil = PRIVATE_MEET_COOLDOWNS.get(key);
        if (cooldownUntil != null && cooldownUntil > now) {
            sendBotPrivateMessage(player, bot, "One sec.");
            return true;
        }
        PRIVATE_MEET_COOLDOWNS.put(key, now + PRIVATE_MEET_COOLDOWN_MS);
        if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            sendBotPrivateMessage(player, bot, "I'm trading right now. Try me again in a sec.");
            return true;
        }
        MeetingSpot spot = pickMeetingSpot(normalized);
        MeetingRequest request = new MeetingRequest(player.getUsername(), spot, now + MEETING_TRAVEL_TIMEOUT_MS, null);
        MEETINGS.put(bot.getUsername(), request);
        if (!startMeetingMovement(bot, request)) {
            MEETINGS.remove(bot.getUsername(), request);
            sendBotPrivateMessage(player, bot, "I can't path there right now.");
            return true;
        }
        bot.setNextForceTalk(new ForceTalk("omw " + spot.name + " side"));
        sendBotPrivateMessage(player, bot, "I'll meet you on the " + spot.name + " side of the GE.");
        return true;
    }

    private static boolean startAdvertMeeting(Player player, BotPlayer bot, MarketBotProfile.BuyAdvertMatch match) {
        if (player == null || bot == null || match == null || bot.getUsername() == null) {
            return false;
        }
        if (!player.canTrade(bot)) {
            sendBotPrivateMessage(player, bot, "I can't trade you right now.");
            return true;
        }
        long now = Utils.currentTimeMillis();
        MeetingRequest active = MEETINGS.get(bot.getUsername());
        if (active != null && !active.isExpired(now)) {
            sendBotPrivateMessage(player, bot, match.getResponse() + " I'm already heading to the "
                    + active.spot.name + " side of the GE.");
            return true;
        }
        if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            sendBotPrivateMessage(player, bot, "I'm trading right now. Try me again in a sec.");
            return true;
        }
        MeetingSpot spot = pickMeetingSpot(null);
        MeetingRequest request = new MeetingRequest(player.getUsername(), spot, now + MEETING_TRAVEL_TIMEOUT_MS,
                match.getRequestText());
        MEETINGS.put(bot.getUsername(), request);
        if (!startMeetingMovement(bot, request)) {
            MEETINGS.remove(bot.getUsername(), request);
            sendBotPrivateMessage(player, bot, "I can't path there right now.");
            return true;
        }
        bot.setNextForceTalk(new ForceTalk("omw " + spot.name + " side"));
        sendBotPrivateMessage(player, bot, match.getResponse() + " Meet me on the "
                + spot.name + " side of the GE.");
        return true;
    }

    public static boolean tickMeeting(BotPlayer bot) {
        if (bot == null || bot.getUsername() == null || bot.hasFinished() || bot.isDead()) {
            return false;
        }
        MeetingRequest request = MEETINGS.get(bot.getUsername());
        if (request == null) {
            return false;
        }
        long now = Utils.currentTimeMillis();
        if (request.waitUntil <= 0L && request.isExpired(now)) {
            MEETINGS.remove(bot.getUsername(), request);
            return false;
        }
        if (bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            return true;
        }
        if (!bot.withinDistance(request.spot.tile, MEETING_ARRIVAL_RADIUS)) {
            if (!bot.hasWalkSteps() && bot.getRouteEvent() == null) {
                if (!startMeetingMovement(bot, request)) {
                    request.routeFailures++;
                    if (request.routeFailures >= 3) {
                        Player target = World.getPlayer(request.playerUsername);
                        if (target != null && !target.hasFinished()) {
                            sendBotPrivateMessage(target, bot, "I can't path to the "
                                    + request.spot.name + " side right now.");
                        }
                        MEETINGS.remove(bot.getUsername(), request);
                        return false;
                    }
                } else {
                    request.routeFailures = 0;
                }
            }
            return true;
        }
        if (request.waitUntil <= 0L) {
            request.waitUntil = now + MEETING_WAIT_MS;
            bot.resetWalkSteps();
        }
        Player target = World.getPlayer(request.playerUsername);
        if (request.waitUntil <= now) {
            if (target != null && !target.hasFinished() && target.withinDistance(bot, TRADE_OPEN_RANGE)) {
                bot.faceEntity(target);
                sendBotPrivateMessage(target, bot, "Trade me if you still need it.");
            }
            MEETINGS.remove(bot.getUsername(), request);
            return false;
        }
        if (target != null && !target.hasFinished() && target.withinDistance(bot, TRADE_OPEN_RANGE)) {
            bot.faceEntity(target);
            if (!target.withinDistance(bot, BOT_TRADE_REQUEST_RANGE)) {
                if (!bot.hasWalkSteps() && bot.getRouteEvent() == null) {
                    routeNear(bot, target, ADVERT_APPROACH_RADIUS);
                }
                return true;
            }
            // Drop the "we already asked" flag once the underlying request
            // TTL has elapsed, so a player who lets the first popup expire
            // can still get re-asked while the meeting window is open.
            if (request.tradeRequestSent) {
                String key = cooldownKey(target, bot);
                Long requestExpiresAt = BOT_TRADE_REQUEST_EXPIRES_AT.get(key);
                if (requestExpiresAt == null || requestExpiresAt <= now) {
                    request.tradeRequestSent = false;
                }
            }
            if (!request.tradeRequestSent && sendBotTradeRequest(target, bot, request.requestText)) {
                request.tradeRequestSent = true;
            }
            return true;
        }
        bot.setNextFaceWorldTile(BotLocations.GE_CENTER);
        return now < request.waitUntil;
    }

    public static void sendToBank(final BotPlayer bot) {
        if (bot == null || bot.hasFinished()) {
            return;
        }
        final String username = bot.getUsername();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (bot.hasFinished() || bot.isDead() || bot.isLocked()) {
                        return;
                    }
                    WorldTile tile = GE_BANK_TILES[ThreadLocalRandom.current().nextInt(GE_BANK_TILES.length)];
                    bot.setNextFaceWorldTile(BotLocations.GE_CENTER);
                    bot.resetWalkSteps();
                    bot.addWalkSteps(tile.getX(), tile.getY(), 12, true);
                } finally {
                    stop();
                }
            }
        }, 2);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    MarketBotProfile profile = PROFILES.get(username);
                    if (profile != null) {
                        profile.replenishFromBank();
                    }
                } finally {
                    stop();
                }
            }
        }, ThreadLocalRandom.current().nextInt(10, 18));
    }

    private static boolean canUseInfoOption(Player player, BotPlayer bot) {
        if (player == null || bot == null || player.hasFinished() || bot.hasFinished()) {
            return false;
        }
        if (!player.withinDistance(bot, INFO_RANGE)) {
            player.sendMessage("You need to be closer to talk to " + bot.getDisplayName() + ".");
            return false;
        }
        return true;
    }

    private static void sayToPlayer(Player player, BotPlayer bot, String line) {
        player.faceEntity(bot);
        player.sendMessage(Colors.CYAN + bot.getDisplayName() + ":</col> " + line);
        bot.setNextForceTalk(new ForceTalk(line));
    }

    private static void sendBotPrivateMessage(Player player, BotPlayer bot, String line) {
        if (player == null || bot == null || line == null || line.isEmpty()) {
            return;
        }
        String displayName = bot.getDisplayName();
        player.getPackets().receivePrivateMessage(Utils.formatPlayerNameForDisplay(displayName), displayName, 0, line);
    }

    private static boolean isMeetRequest(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        return normalized.equals("meet")
                || normalized.startsWith("meet ")
                || normalized.contains("meet me")
                || normalized.contains("meet up")
                || normalized.contains("meet at")
                || normalized.contains("come meet")
                || hasMeetQuestion(normalized, "where are you")
                || hasMeetQuestion(normalized, "where r u");
    }

    private static boolean hasMeetQuestion(String normalized, String phrase) {
        return normalized.equals(phrase)
                || normalized.startsWith(phrase + " ")
                || normalized.startsWith("hey " + phrase)
                || normalized.startsWith("yo " + phrase)
                || normalized.contains(", " + phrase)
                || normalized.contains(". " + phrase)
                || normalized.contains("? " + phrase);
    }

    private static MeetingSpot pickMeetingSpot(String normalized) {
        for (MeetingSpot spot : GE_MEETING_SPOTS) {
            if (normalized != null && normalized.contains(spot.name)) {
                return spot;
            }
        }
        return GE_MEETING_SPOTS[ThreadLocalRandom.current().nextInt(GE_MEETING_SPOTS.length)];
    }

    private static boolean startMeetingMovement(BotPlayer bot, MeetingRequest request) {
        if (bot == null || request == null || bot.hasFinished() || bot.isDead() || bot.isLocked()) {
            return false;
        }
        bot.setRouteEvent(null);
        bot.resetWalkSteps();
        bot.setNextFaceWorldTile(request.spot.tile);
        return routeNear(bot, request.spot.tile, MEETING_ROUTE_RADIUS);
    }

    private static boolean routeNear(BotPlayer bot, WorldTile center, int radius) {
        if (bot == null || center == null || bot.isLocked() || bot.isDead() || center.getPlane() != bot.getPlane()) {
            return false;
        }
        int safeRadius = Math.max(0, radius);
        // Iterate rings outward but shuffle each ring so multiple bots
        // heading to the same anchor (e.g. a meeting spot) don't all clump
        // on the same offset tile.
        for (int distance = 0; distance <= safeRadius; distance++) {
            if (distance == 0) {
                if (routeTo(bot, center)) {
                    return true;
                }
                continue;
            }
            List<int[]> ring = new ArrayList<>(8 * distance);
            for (int offsetX = -distance; offsetX <= distance; offsetX++) {
                for (int offsetY = -distance; offsetY <= distance; offsetY++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetY)) != distance) {
                        continue;
                    }
                    ring.add(new int[] {offsetX, offsetY});
                }
            }
            Collections.shuffle(ring, ThreadLocalRandom.current());
            for (int[] offset : ring) {
                if (routeTo(bot, center.transform(offset[0], offset[1], 0))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean routeTo(BotPlayer bot, WorldTile tile) {
        if (bot == null || tile == null || bot.isLocked() || bot.isDead() || tile.getPlane() != bot.getPlane()) {
            return false;
        }
        if (bot.getRunEnergy() < 100) {
            bot.setRunEnergy(100);
        }
        if (!bot.getRun()) {
            bot.setRunHidden(true);
        }
        int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, bot.getX(), bot.getY(), bot.getPlane(),
                bot.getSize(), new FixedTileStrategy(tile.getX(), tile.getY()), true);
        if (steps == -1) {
            return false;
        }
        bot.resetWalkSteps();
        if (steps == 0) {
            return true;
        }
        int[] bufferX = RouteFinder.getLastPathBufferX();
        int[] bufferY = RouteFinder.getLastPathBufferY();
        for (int step = steps - 1; step >= 0; step--) {
            if (!bot.addWalkSteps(bufferX[step], bufferY[step], MEETING_ROUTE_STEP_LIMIT, true)) {
                break;
            }
        }
        return bot.hasWalkSteps() || bot.withinDistance(tile, 0);
    }

    private static void handleOpenTradeRequest(Player player, String message) {
        String normalized = message == null ? "" : message.toLowerCase().trim();
        String prefix = player.getUsername() + "->";
        for (Map.Entry<String, MarketBotSession> entry : ACTIVE_SESSIONS.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                if (normalized.startsWith("request ") || normalized.startsWith("buy ")
                        || normalized.startsWith("i want ") || normalized.startsWith("can i buy ")) {
                    entry.getValue().requestSellBundle(message);
                } else {
                    entry.getValue().handleChatCounterOffer(message);
                }
                return;
            }
        }
    }

    private static void scheduleReplenishment(final BotPlayer bot) {
        if (bot == null || bot.getUsername() == null) {
            return;
        }
        final String username = bot.getUsername();
        final long token = Utils.currentTimeMillis() ^ ThreadLocalRandom.current().nextLong();
        REPLENISHMENT_TOKENS.put(username, token);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    Long currentToken = REPLENISHMENT_TOKENS.get(username);
                    MarketBotProfile profile = PROFILES.get(username);
                    if (currentToken == null || currentToken.longValue() != token || profile == null) {
                        return;
                    }
                    profile.replenishFromBank();
                    scheduleReplenishment(bot);
                } finally {
                    stop();
                }
            }
        }, ThreadLocalRandom.current().nextInt(MIN_REPLENISH_TICKS, MAX_REPLENISH_TICKS + 1));
    }

    private static void scheduleProfileReroll(final BotPlayer bot) {
        scheduleProfileReroll(bot, MIN_PROFILE_REROLL_TICKS, MAX_PROFILE_REROLL_TICKS);
    }

    private static void scheduleProfileRerollRetry(final BotPlayer bot) {
        scheduleProfileReroll(bot, MIN_PROFILE_REROLL_RETRY_TICKS, MAX_PROFILE_REROLL_RETRY_TICKS);
    }

    private static void scheduleProfileReroll(final BotPlayer bot, int minTicks, int maxTicks) {
        if (bot == null || bot.getUsername() == null) {
            return;
        }
        final String username = bot.getUsername();
        final long token = Utils.currentTimeMillis() ^ ThreadLocalRandom.current().nextLong();
        PROFILE_REROLL_TOKENS.put(username, token);
        int min = Math.max(1, minTicks);
        int max = Math.max(min, maxTicks);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    Long currentToken = PROFILE_REROLL_TOKENS.get(username);
                    if (currentToken == null || currentToken.longValue() != token || !PROFILES.containsKey(username)
                            || bot.hasFinished() || bot.isDead()) {
                        return;
                    }
                    if (isBotBusyForProfileReroll(username, bot)) {
                        scheduleProfileRerollRetry(bot);
                        return;
                    }
                    IdleCrowdBotScript script = BotManager.getCrowdScript(bot);
                    IdleCrowdBotScript.Role role = script == null ? null : script.getRole();
                    if (!MarketBotProfile.isTradingRole(role)) {
                        unregister(bot);
                        return;
                    }
                    PROFILES.put(username, MarketBotProfile.forRole(role));
                    String suffix = "->" + username;
                    PENDING_REQUESTS.keySet().removeIf(key -> key.endsWith(suffix));
                    PENDING_REQUEST_EXPIRES_AT.keySet().removeIf(key -> key.endsWith(suffix));
                    scheduleProfileReroll(bot);
                } finally {
                    stop();
                }
            }
        }, ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    private static boolean isBotTrading(String username, BotPlayer bot) {
        if (bot != null && bot.getItemTransaction() != null && bot.getItemTransaction().isInTransaction()) {
            return true;
        }
        String suffix = "->" + username;
        for (String key : ACTIVE_SESSIONS.keySet()) {
            if (key.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBotBusyForProfileReroll(String username, BotPlayer bot) {
        if (isBotTrading(username, bot)) {
            return true;
        }
        long now = Utils.currentTimeMillis();
        MeetingRequest meeting = MEETINGS.get(username);
        if (meeting != null) {
            if (!meeting.isExpired(now)) {
                return true;
            }
            MEETINGS.remove(username, meeting);
        }
        String suffix = "->" + username;
        for (Map.Entry<String, Long> entry : PENDING_REQUEST_EXPIRES_AT.entrySet()) {
            if (!entry.getKey().endsWith(suffix)) {
                continue;
            }
            Long expiresAt = entry.getValue();
            if (expiresAt != null && expiresAt > now) {
                return true;
            }
            PENDING_REQUEST_EXPIRES_AT.remove(entry.getKey(), expiresAt);
            PENDING_REQUESTS.remove(entry.getKey());
        }
        return false;
    }

    private static void maybeSweepExpiredState(long now) {
        if (now < nextStateSweepAt) {
            return;
        }
        nextStateSweepAt = now + STATE_SWEEP_INTERVAL_MS;
        CHAT_TRADE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
        TRADE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
        PRIVATE_MEET_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
        BOT_TRADE_REQUEST_EXPIRES_AT.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= now);
        for (Map.Entry<String, Long> entry : PENDING_REQUEST_EXPIRES_AT.entrySet()) {
            Long expiresAt = entry.getValue();
            if (expiresAt == null || expiresAt <= now) {
                PENDING_REQUEST_EXPIRES_AT.remove(entry.getKey(), expiresAt);
                PENDING_REQUESTS.remove(entry.getKey());
            }
        }
    }

    private static String cooldownKey(Player player, BotPlayer bot) {
        String playerName = player == null || player.getUsername() == null ? "unknown" : player.getUsername();
        String botName = bot == null || bot.getUsername() == null ? "unknown" : bot.getUsername();
        return playerName + "->" + botName;
    }

    private static final class MeetingSpot {
        private final String name;
        private final WorldTile tile;

        private MeetingSpot(String name, WorldTile tile) {
            this.name = name;
            this.tile = tile;
        }
    }

    private static final class MeetingRequest {
        private final String playerUsername;
        private final MeetingSpot spot;
        private final long travelExpiresAt;
        private final String requestText;
        private long waitUntil;
        private int routeFailures;
        private boolean tradeRequestSent;

        private MeetingRequest(String playerUsername, MeetingSpot spot, long travelExpiresAt, String requestText) {
            this.playerUsername = playerUsername;
            this.spot = spot;
            this.travelExpiresAt = travelExpiresAt;
            this.requestText = requestText;
        }

        private boolean isExpired(long now) {
            return waitUntil > 0L ? now >= waitUntil : now >= travelExpiresAt;
        }
    }
}
