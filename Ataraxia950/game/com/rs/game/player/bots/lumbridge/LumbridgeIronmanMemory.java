package com.rs.game.player.bots.lumbridge;

import com.rs.game.WorldTile;
import com.rs.game.player.bots.BotPersonality;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class LumbridgeIronmanMemory {

    private static final int FAILED_ROUTE_TTL = 180;
    private static final int RECENTLY_FINISHED_TTL = 40;

    private final BotPersonality personality;
    private final Map<String, Integer> failedLocations = new HashMap<String, Integer>();
    private final Map<LumbridgeIronmanData.ActivityKind, Integer> recentActivities =
            new HashMap<LumbridgeIronmanData.ActivityKind, Integer>();

    private LumbridgeIronmanData.ActivityKind currentKind = LumbridgeIronmanData.ActivityKind.WANDER;
    private String currentReason = "spawn";
    private int goalTicksRemaining;
    private int ticksAlive;
    private int failedStarts;
    private int lastBankTick = -9999;
    private int lastChatTick = -9999;
    private int lastLootTick = -9999;

    LumbridgeIronmanMemory(BotPersonality personality) {
        this.personality = personality == null ? BotPersonality.random() : personality;
        this.goalTicksRemaining = randomGoalTicks();
    }

    LumbridgeIronmanProfile.ScriptState snapshot() {
        LumbridgeIronmanProfile.ScriptState state = new LumbridgeIronmanProfile.ScriptState();
        state.setArchetype(personality.getArchetype());
        state.setCurrentKind(currentKind);
        state.setCurrentReason(currentReason);
        state.setGoalTicksRemaining(goalTicksRemaining);
        state.setTicksAlive(ticksAlive);
        state.setFailedStarts(failedStarts);
        state.setLastBankTick(lastBankTick);
        state.setLastChatTick(lastChatTick);
        state.setLastLootTick(lastLootTick);
        state.setFailedLocations(failedLocations);
        state.setRecentActivitiesByName(toNameMap(recentActivities));
        return state;
    }

    void restore(LumbridgeIronmanProfile.ScriptState state) {
        if (state == null) {
            return;
        }
        currentKind = state.getCurrentKindOrDefault();
        currentReason = state.getCurrentReason() == null ? "restored" : state.getCurrentReason();
        goalTicksRemaining = Math.max(0, state.getGoalTicksRemaining());
        ticksAlive = Math.max(0, state.getTicksAlive());
        failedStarts = Math.max(0, state.getFailedStarts());
        lastBankTick = state.getLastBankTick();
        lastChatTick = state.getLastChatTick();
        lastLootTick = state.getLastLootTick();
        failedLocations.clear();
        failedLocations.putAll(state.getFailedLocations());
        recentActivities.clear();
        for (Map.Entry<String, Integer> entry : state.getRecentActivitiesByName().entrySet()) {
            LumbridgeIronmanData.ActivityKind kind = LumbridgeIronmanProfile.activityKind(entry.getKey(), null);
            if (kind != null && entry.getValue() != null && entry.getValue() > 0) {
                recentActivities.put(kind, entry.getValue());
            }
        }
    }

    BotPersonality personality() {
        return personality;
    }

    void pulse() {
        ticksAlive++;
        if (goalTicksRemaining > 0) {
            goalTicksRemaining--;
        }
        decay(failedLocations);
        decayActivityMemory();
    }

    int ticksAlive() {
        return ticksAlive;
    }

    int goalTicksRemaining() {
        return goalTicksRemaining;
    }

    LumbridgeIronmanData.ActivityKind currentKind() {
        return currentKind;
    }

    String currentReason() {
        return currentReason;
    }

    boolean shouldRethinkGoal() {
        if (goalTicksRemaining <= 0) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < 0.01 + personality.getIdleTendency() * 0.015;
    }

    void startGoal(LumbridgeIronmanData.ActivityKind kind, String reason) {
        currentKind = kind == null ? LumbridgeIronmanData.ActivityKind.WANDER : kind;
        currentReason = reason == null ? "unknown" : reason;
        goalTicksRemaining = randomGoalTicks();
        failedStarts = 0;
    }

    void rememberFinished(LumbridgeIronmanData.ActivityKind kind) {
        if (kind != null) {
            recentActivities.put(kind, RECENTLY_FINISHED_TTL);
        }
    }

    boolean recentlyFinished(LumbridgeIronmanData.ActivityKind kind) {
        Integer ticks = recentActivities.get(kind);
        return ticks != null && ticks > 0;
    }

    void rememberFailedStart() {
        failedStarts++;
        if (failedStarts >= 3) {
            goalTicksRemaining = 0;
        }
    }

    int failedStarts() {
        return failedStarts;
    }

    void rememberFailedRoute(WorldTile tile) {
        if (tile != null) {
            failedLocations.put(key(tile), FAILED_ROUTE_TTL);
        }
    }

    boolean recentlyFailedRoute(WorldTile tile) {
        Integer ttl = tile == null ? null : failedLocations.get(key(tile));
        return ttl != null && ttl > 0;
    }

    void markBanked() {
        lastBankTick = ticksAlive;
    }

    boolean bankedRecently(int ticks) {
        return ticksAlive - lastBankTick <= ticks;
    }

    void markChatted() {
        lastChatTick = ticksAlive;
    }

    boolean mayChat(int cooldownTicks) {
        return ticksAlive - lastChatTick >= cooldownTicks && personality.rollChat();
    }

    void markLooted() {
        lastLootTick = ticksAlive;
    }

    boolean lootedRecently(int ticks) {
        return ticksAlive - lastLootTick <= ticks;
    }

    int randomReactionDelay() {
        return personality.reactionDelay(1, 5);
    }

    int randomShortDelay() {
        return ThreadLocalRandom.current().nextInt(2, 7);
    }

    private int randomGoalTicks() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int base = random.nextInt(120, 620);
        double greed = personality.getGreed();
        double idle = personality.getIdleTendency();
        base += (int) (greed * random.nextInt(40, 260));
        base -= (int) (idle * random.nextInt(20, 120));
        return Math.max(70, Math.min(900, base));
    }

    private static String key(WorldTile tile) {
        return tile.getX() + "," + tile.getY() + "," + tile.getPlane();
    }

    private static void decay(Map<String, Integer> values) {
        Iterator<Map.Entry<String, Integer>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            int next = entry.getValue() - 1;
            if (next <= 0) {
                iterator.remove();
            } else {
                entry.setValue(next);
            }
        }
    }

    private void decayActivityMemory() {
        Iterator<Map.Entry<LumbridgeIronmanData.ActivityKind, Integer>> iterator =
                recentActivities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LumbridgeIronmanData.ActivityKind, Integer> entry = iterator.next();
            int next = entry.getValue() - 1;
            if (next <= 0) {
                iterator.remove();
            } else {
                entry.setValue(next);
            }
        }
    }

    private static Map<String, Integer> toNameMap(Map<LumbridgeIronmanData.ActivityKind, Integer> source) {
        Map<String, Integer> values = new HashMap<String, Integer>();
        for (Map.Entry<LumbridgeIronmanData.ActivityKind, Integer> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                values.put(entry.getKey().name(), entry.getValue());
            }
        }
        return values;
    }
}
