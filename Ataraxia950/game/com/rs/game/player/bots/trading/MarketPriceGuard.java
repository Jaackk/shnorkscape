package com.rs.game.player.bots.trading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MarketPriceGuard {

    private static final int MIN_SPREAD_BPS = 200;
    // Widened from [9000, 11000] to [7500, 13000] so the registry can hold
    // genuinely varied prices (rare premiums, low-stock surcharges, etc.)
    // instead of crushing every quote into a 10% band around guide.
    private static final int MIN_GUARDED_BPS = 7500;
    private static final int MAX_GUARDED_BPS = 13000;

    private static final Map<Integer, Integer> LOWEST_SELL_BPS = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> SELL_INTENT_EXPIRES_AT = new ConcurrentHashMap<>();
    // Sell intents expire after an hour with no refresh. Without this, a single
    // unlucky low roll permanently anchors the registry's floor for an item
    // because the previous min-only behavior could only ratchet down. Bots
    // refresh their intents on init and during replenishFromBank (every
    // ~15-35 minutes), so live bots keep their floor; despawned bots' floors
    // age out instead of stamping the registry forever.
    private static final long INTENT_TTL_MS = 60L * 60L * 1000L;

    private MarketPriceGuard() {
    }

    /**
     * Records that some bot has committed to selling {@code itemId} at the
     * given BPS. Other bots' buy quotes will be capped just below this so
     * cross-bot arbitrage stays unprofitable.
     *
     * Call this from real commitment points only — initial profile setup,
     * a sell offer being shown to a player, or a completed sale. Calling it
     * from every internal price quote causes prices to spiral toward the
     * floor over time.
     */
    static void registerSellIntent(int itemId, int sellBps) {
        if (itemId <= 0) {
            return;
        }
        int safeBps = clamp(sellBps);
        long now = System.currentTimeMillis();
        Integer current = LOWEST_SELL_BPS.get(itemId);
        Long expiresAt = SELL_INTENT_EXPIRES_AT.get(itemId);
        boolean expired = expiresAt == null || expiresAt < now;
        if (current == null || expired || safeBps < current) {
            LOWEST_SELL_BPS.put(itemId, safeBps);
        }
        if (current == null || expired || safeBps <= current) {
            SELL_INTENT_EXPIRES_AT.put(itemId, now + INTENT_TTL_MS);
        }
    }

    static int guardedBuyBps(int itemId, int desiredBuyBps) {
        int guarded = clamp(desiredBuyBps);
        Long expiresAt = SELL_INTENT_EXPIRES_AT.get(itemId);
        if (expiresAt != null && expiresAt >= System.currentTimeMillis()) {
            Integer lowestSell = LOWEST_SELL_BPS.get(itemId);
            if (lowestSell != null) {
                guarded = Math.min(guarded, lowestSell - MIN_SPREAD_BPS);
            }
        } else if (expiresAt != null && SELL_INTENT_EXPIRES_AT.remove(itemId, expiresAt)) {
            LOWEST_SELL_BPS.remove(itemId);
        }
        return clamp(guarded);
    }

    /**
     * Returns the clamped sell BPS without registering it. Internal quote-style
     * paths use this so the global lowest-sell registry only changes when a
     * bot actually commits a price (initial setup, offer displayed, or sale).
     */
    static int guardedSellBps(int itemId, int desiredSellBps) {
        return clamp(desiredSellBps);
    }

    private static int clamp(int bps) {
        return Math.max(MIN_GUARDED_BPS, Math.min(MAX_GUARDED_BPS, bps));
    }
}
