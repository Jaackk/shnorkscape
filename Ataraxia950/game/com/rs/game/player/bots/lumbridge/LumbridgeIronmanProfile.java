package com.rs.game.player.bots.lumbridge;

import com.rs.game.WorldTile;
import com.rs.game.player.bots.BotPersonality;
import com.rs.game.player.bots.BotPlayer;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LumbridgeIronmanProfile implements Serializable {

    private static final long serialVersionUID = 5445235161228372041L;
    public static final int CURRENT_VERSION = 1;

    private final int version;
    private final String username;
    private String displayName;
    private WorldTile spawnTile;
    private WorldTile lastKnownTile;
    private String archetypeName;
    private boolean enabled;
    private boolean respawnOnStartup;
    private long createdAt;
    private long lastSavedAt;
    private long lastLoadedAt;
    private ScriptState scriptState;

    public LumbridgeIronmanProfile(String username, String displayName, WorldTile spawnTile,
            BotPersonality.Archetype archetype) {
        this.version = CURRENT_VERSION;
        this.username = Utils.formatPlayerNameForProtocol(username);
        this.displayName = displayName == null ? Utils.formatPlayerNameForDisplay(this.username) : displayName;
        this.spawnTile = copy(spawnTile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : spawnTile);
        this.lastKnownTile = copy(this.spawnTile);
        this.archetypeName = archetype == null ? null : archetype.name();
        this.enabled = true;
        this.respawnOnStartup = true;
        this.createdAt = System.currentTimeMillis();
        this.lastSavedAt = this.createdAt;
        this.scriptState = new ScriptState();
        this.scriptState.setArchetype(archetype);
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public WorldTile getSpawnTile() {
        return copy(spawnTile);
    }

    public WorldTile getLastKnownTile() {
        return copy(lastKnownTile);
    }

    public WorldTile getBestStartupTile() {
        return copy(lastKnownTile == null ? spawnTile : lastKnownTile);
    }

    public BotPersonality.Archetype getArchetype() {
        return archetype(archetypeName, scriptState == null ? null : scriptState.getArchetypeName());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRespawnOnStartup() {
        return respawnOnStartup;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastSavedAt() {
        return lastSavedAt;
    }

    public long getLastLoadedAt() {
        return lastLoadedAt;
    }

    public ScriptState getScriptState() {
        if (scriptState == null) {
            scriptState = new ScriptState();
            scriptState.setArchetype(getArchetype());
        }
        return scriptState;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        markSaved();
    }

    public void setRespawnOnStartup(boolean respawnOnStartup) {
        this.respawnOnStartup = respawnOnStartup;
        markSaved();
    }

    public void markLoaded() {
        lastLoadedAt = System.currentTimeMillis();
    }

    public void updateFrom(BotPlayer bot, LumbridgeIronmanBotScript script) {
        if (bot == null) {
            return;
        }
        displayName = bot.getDisplayName();
        lastKnownTile = copy(bot);
        if (script != null) {
            scriptState = script.snapshotState();
            if (scriptState.getArchetypeName() != null) {
                archetypeName = scriptState.getArchetypeName();
            }
        }
        markSaved();
    }

    public void markSaved() {
        lastSavedAt = System.currentTimeMillis();
    }

    public static BotPersonality.Archetype archetype(String primary, String fallback) {
        BotPersonality.Archetype value = archetypeOrNull(primary);
        if (value != null) {
            return value;
        }
        value = archetypeOrNull(fallback);
        return value == null ? BotPersonality.Archetype.GRINDER : value;
    }

    public static LumbridgeIronmanData.ActivityKind activityKind(String value,
            LumbridgeIronmanData.ActivityKind fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return LumbridgeIronmanData.ActivityKind.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static BotPersonality.Archetype archetypeOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BotPersonality.Archetype.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static WorldTile copy(WorldTile tile) {
        return tile == null ? null : new WorldTile(tile);
    }

    public static final class ScriptState implements Serializable {

        private static final long serialVersionUID = -3437690057703219783L;

        private String archetypeName;
        private String currentKindName = LumbridgeIronmanData.ActivityKind.WANDER.name();
        private String currentReason = "spawn";
        private String currentActivityKindName;
        private String currentActivityDebug;
        private int goalTicksRemaining;
        private int ticksAlive;
        private int failedStarts;
        private int activityStarts;
        private int thinkingCooldown;
        private int lastBankTick = -9999;
        private int lastChatTick = -9999;
        private int lastLootTick = -9999;
        private Map<String, Integer> failedLocations = new HashMap<String, Integer>();
        private Map<String, Integer> recentActivitiesByName = new HashMap<String, Integer>();

        public String getArchetypeName() {
            return archetypeName;
        }

        public BotPersonality.Archetype getArchetype() {
            return archetype(archetypeName, null);
        }

        public void setArchetype(BotPersonality.Archetype archetype) {
            this.archetypeName = archetype == null ? null : archetype.name();
        }

        public LumbridgeIronmanData.ActivityKind getCurrentKindOrDefault() {
            return activityKind(currentKindName, LumbridgeIronmanData.ActivityKind.WANDER);
        }

        public void setCurrentKind(LumbridgeIronmanData.ActivityKind currentKind) {
            this.currentKindName = currentKind == null ? LumbridgeIronmanData.ActivityKind.WANDER.name()
                    : currentKind.name();
        }

        public String getCurrentReason() {
            return currentReason;
        }

        public void setCurrentReason(String currentReason) {
            this.currentReason = currentReason;
        }

        public void setCurrentActivityKind(LumbridgeIronmanData.ActivityKind currentActivityKind) {
            this.currentActivityKindName = currentActivityKind == null ? null : currentActivityKind.name();
        }

        public String getCurrentActivityKindName() {
            return currentActivityKindName;
        }

        public String getCurrentActivityDebug() {
            return currentActivityDebug;
        }

        public void setCurrentActivityDebug(String currentActivityDebug) {
            this.currentActivityDebug = currentActivityDebug;
        }

        public int getGoalTicksRemaining() {
            return goalTicksRemaining;
        }

        public void setGoalTicksRemaining(int goalTicksRemaining) {
            this.goalTicksRemaining = goalTicksRemaining;
        }

        public int getTicksAlive() {
            return ticksAlive;
        }

        public void setTicksAlive(int ticksAlive) {
            this.ticksAlive = ticksAlive;
        }

        public int getFailedStarts() {
            return failedStarts;
        }

        public void setFailedStarts(int failedStarts) {
            this.failedStarts = failedStarts;
        }

        public int getActivityStarts() {
            return activityStarts;
        }

        public void setActivityStarts(int activityStarts) {
            this.activityStarts = activityStarts;
        }

        public int getThinkingCooldown() {
            return thinkingCooldown;
        }

        public void setThinkingCooldown(int thinkingCooldown) {
            this.thinkingCooldown = thinkingCooldown;
        }

        public int getLastBankTick() {
            return lastBankTick;
        }

        public void setLastBankTick(int lastBankTick) {
            this.lastBankTick = lastBankTick;
        }

        public int getLastChatTick() {
            return lastChatTick;
        }

        public void setLastChatTick(int lastChatTick) {
            this.lastChatTick = lastChatTick;
        }

        public int getLastLootTick() {
            return lastLootTick;
        }

        public void setLastLootTick(int lastLootTick) {
            this.lastLootTick = lastLootTick;
        }

        public Map<String, Integer> getFailedLocations() {
            return failedLocations == null ? Collections.<String, Integer>emptyMap() : failedLocations;
        }

        public void setFailedLocations(Map<String, Integer> failedLocations) {
            this.failedLocations = copyMap(failedLocations);
        }

        public Map<String, Integer> getRecentActivitiesByName() {
            return recentActivitiesByName == null
                    ? Collections.<String, Integer>emptyMap()
                    : recentActivitiesByName;
        }

        public void setRecentActivitiesByName(Map<String, Integer> recentActivitiesByName) {
            this.recentActivitiesByName = copyMap(recentActivitiesByName);
        }

        private static Map<String, Integer> copyMap(Map<String, Integer> source) {
            Map<String, Integer> values = new HashMap<String, Integer>();
            if (source == null) {
                return values;
            }
            for (Map.Entry<String, Integer> entry : source.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
            return values;
        }
    }

    public static final class Roster implements Serializable {

        private static final long serialVersionUID = -2361567195412988847L;

        private int version = CURRENT_VERSION;
        private int nextSequence = 1;
        private Map<String, LumbridgeIronmanProfile> profiles =
                new LinkedHashMap<String, LumbridgeIronmanProfile>();

        public int getVersion() {
            return version;
        }

        public Collection<LumbridgeIronmanProfile> all() {
            return profiles.values();
        }

        public LumbridgeIronmanProfile get(String username) {
            return profiles.get(Utils.formatPlayerNameForProtocol(username));
        }

        public void put(LumbridgeIronmanProfile profile) {
            if (profile != null) {
                profiles.put(profile.getUsername(), profile);
            }
        }

        public void remove(String username) {
            profiles.remove(Utils.formatPlayerNameForProtocol(username));
        }

        public int size() {
            return profiles.size();
        }

        public String nextPersistentUsername() {
            String username;
            do {
                username = "lumby_iron_" + nextSequence++;
            } while (profiles.containsKey(username));
            return username;
        }
    }
}
