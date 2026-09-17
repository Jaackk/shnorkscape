package com.rs.game.player.bots;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Short-lived, intentionally imperfect memory for one Stealing Creation bot.
 */
final class StealingCreationBotMemory {

    private static final int FAILED_ROUTE_TICKS = 55;
    private static final int OPPONENT_FORGET_TICKS = 260;
    private static final int FOCUS_TARGET_TICKS = 30;

    private final Map<Integer, Integer> failedRouteTiles = new HashMap<Integer, Integer>();
    private final Map<String, OpponentMemory> opponents = new HashMap<String, OpponentMemory>();

    private WorldTile lastKilnTile;
    private WorldTile lastResourceTile;
    private int lastResourceTier = -1;
    private int lastResourceStyle;
    private String focusTargetUsername;
    private int focusTargetTicks;
    private int ticks;

    void pulse() {
        ticks++;
        decrementFailedRoutes();
        decrementOpponents();
        if (focusTargetTicks > 0 && --focusTargetTicks == 0) {
            focusTargetUsername = null;
        }
    }

    int getTicks() {
        return ticks;
    }

    void rememberKiln(WorldTile tile) {
        if (tile != null) {
            lastKilnTile = new WorldTile(tile);
        }
    }

    WorldTile getLastKilnTile() {
        return lastKilnTile == null ? null : new WorldTile(lastKilnTile);
    }

    void rememberResource(WorldTile tile, int tier, int style) {
        if (tile == null) {
            return;
        }
        lastResourceTile = new WorldTile(tile);
        lastResourceTier = tier;
        lastResourceStyle = style;
    }

    WorldTile getLastResourceTile(int style, int maxTier) {
        if (lastResourceTile == null || lastResourceStyle != style || lastResourceTier < 0 || lastResourceTier > maxTier) {
            return null;
        }
        return new WorldTile(lastResourceTile);
    }

    void rememberFailedRoute(WorldTile tile) {
        if (tile != null) {
            failedRouteTiles.put(tile.getTileHash(), FAILED_ROUTE_TICKS);
        }
    }

    boolean isRecentlyFailedRoute(WorldTile tile) {
        return tile != null && failedRouteTiles.containsKey(tile.getTileHash());
    }

    void rememberSeenEnemy(Player enemy) {
        if (enemy == null) {
            return;
        }
        OpponentMemory memory = getOpponentMemory(enemy);
        memory.lastTile = new WorldTile(enemy);
        memory.lastSeenTicks = OPPONENT_FORGET_TICKS;
    }

    void rememberDeathBy(Player killer) {
        if (killer == null) {
            return;
        }
        OpponentMemory memory = getOpponentMemory(killer);
        memory.killedMe++;
        memory.threatTicks = OPPONENT_FORGET_TICKS;
        memory.lastTile = new WorldTile(killer);
        memory.lastSeenTicks = OPPONENT_FORGET_TICKS;
    }

    void rememberTarget(Player target) {
        if (target == null) {
            return;
        }
        focusTargetUsername = target.getUsername();
        focusTargetTicks = FOCUS_TARGET_TICKS;
        OpponentMemory memory = getOpponentMemory(target);
        memory.lastTargetedTicks = FOCUS_TARGET_TICKS;
    }

    int getThreatScore(Player enemy) {
        if (enemy == null) {
            return 0;
        }
        OpponentMemory memory = opponents.get(enemy.getUsername());
        if (memory == null) {
            return 0;
        }
        int score = memory.killedMe * 18;
        if (memory.threatTicks > 0) {
            score += 20;
        }
        if (memory.lastTargetedTicks > 0) {
            score -= 6;
        }
        return score;
    }

    boolean shouldAvoid(Player enemy, WorldTile fromTile, int radius) {
        if (enemy == null) {
            return false;
        }
        OpponentMemory memory = opponents.get(enemy.getUsername());
        if (memory == null || memory.threatTicks <= 0) {
            return false;
        }
        return fromTile == null || Utils.getDistance(fromTile.getX(), fromTile.getY(), enemy.getX(), enemy.getY()) <= radius;
    }

    boolean isFocusedTarget(Player enemy) {
        return enemy != null && focusTargetUsername != null && focusTargetUsername.equals(enemy.getUsername())
                && focusTargetTicks > 0;
    }

    private OpponentMemory getOpponentMemory(Player enemy) {
        OpponentMemory memory = opponents.get(enemy.getUsername());
        if (memory == null) {
            memory = new OpponentMemory();
            opponents.put(enemy.getUsername(), memory);
        }
        return memory;
    }

    private void decrementFailedRoutes() {
        Iterator<Map.Entry<Integer, Integer>> iterator = failedRouteTiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                iterator.remove();
            } else {
                entry.setValue(ticksLeft);
            }
        }
    }

    private void decrementOpponents() {
        Iterator<Map.Entry<String, OpponentMemory>> iterator = opponents.entrySet().iterator();
        while (iterator.hasNext()) {
            OpponentMemory memory = iterator.next().getValue();
            if (memory.lastSeenTicks > 0) {
                memory.lastSeenTicks--;
            }
            if (memory.threatTicks > 0) {
                memory.threatTicks--;
            }
            if (memory.lastTargetedTicks > 0) {
                memory.lastTargetedTicks--;
            }
            if (memory.lastSeenTicks <= 0 && memory.threatTicks <= 0 && memory.lastTargetedTicks <= 0) {
                iterator.remove();
            }
        }
    }

    private static final class OpponentMemory {
        private WorldTile lastTile;
        private int killedMe;
        private int lastSeenTicks;
        private int threatTicks;
        private int lastTargetedTicks;
    }
}
