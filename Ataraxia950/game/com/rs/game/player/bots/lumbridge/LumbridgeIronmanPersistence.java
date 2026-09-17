/*
package com.rs.game.player.bots.lumbridge;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.bots.BotPersonality;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.bots.BotScript;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;

public final class LumbridgeIronmanPersistence {

    private static final File ROSTER_FILE = new File("data/bots/lumbridge-ironmen.dat");
    private static LumbridgeIronmanProfile.Roster cachedRoster;

    private LumbridgeIronmanPersistence() {
    }

    public static synchronized int loadOnStartup() {
        LumbridgeIronmanProfile.Roster roster = roster();
        int loaded = 0;
        for (LumbridgeIronmanProfile profile : roster.all()) {
            if (!profile.isEnabled() || !profile.isRespawnOnStartup()) {
                continue;
            }
            if (World.containsPlayer(profile.getUsername())) {
                continue;
            }
            try {
                BotPlayer bot = loadOrCreateBot(profile);
                if (bot == null) {
                    continue;
                }
                profile.markLoaded();
                LumbridgeIronmanBotScript script =
                        new LumbridgeIronmanBotScript(profile.getArchetype(), profile.getScriptState());
                BotManager.registerLoadedBot(bot, script);
                loaded++;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }
        saveRoster();
        Logger.getGlobal().info("Loaded " + loaded + " persistent Lumbridge ironman bots.");
        return loaded;
    }

    public static synchronized BotPlayer spawnPersistentBot(String requestedDisplayName, WorldTile tile,
            BotPersonality.Archetype archetype) {
        LumbridgeIronmanProfile.Roster roster = roster();
        String username = roster.nextPersistentUsername();
        String displayName = requestedDisplayName == null || requestedDisplayName.trim().isEmpty()
                ? Utils.formatPlayerNameForDisplay(username)
                : requestedDisplayName.trim().replace('_', ' ');
        WorldTile spawnTile = tile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : new WorldTile(tile);
        BotPersonality.Archetype safeArchetype = archetype == null ? randomArchetype() : archetype;
        LumbridgeIronmanProfile profile =
                new LumbridgeIronmanProfile(username, displayName, spawnTile, safeArchetype);
        LumbridgeIronmanBotScript script =
                new LumbridgeIronmanBotScript(safeArchetype, profile.getScriptState());
        BotPlayer bot = BotManager.spawnPersistentBot(username, displayName, spawnTile, script);
        profile.updateFrom(bot, script);
        roster.put(profile);
        SerializableFilesManager.savePlayer(bot);
        saveRoster();
        return bot;
    }

    public static synchronized int spawnPersistentBots(int amount, WorldTile center, int radius) {
        int spawned = 0;
        int count = Math.max(0, amount);
        WorldTile spawnCenter = center == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : center;
        int spread = Math.max(0, radius);
        for (int index = 0; index < count; index++) {
            spawnPersistentBot(null, randomTileNear(spawnCenter, spread), null);
            spawned++;
        }
        return spawned;
    }

    public static synchronized void saveActiveBots(String reason) {
        int saved = 0;
        for (BotPlayer bot : BotManager.getBots()) {
            if (bot == null || bot.hasFinished()) {
                continue;
            }
            if (saveBot(bot, reason)) {
                saved++;
            }
        }
        if (saved > 0) {
            saveRoster();
        }
    }

    public static synchronized boolean saveBot(BotPlayer bot, String reason) {
        if (bot == null || bot.getUsername() == null) {
            return false;
        }
        BotScript script = BotManager.getScript(bot);
        if (!(script instanceof LumbridgeIronmanBotScript)) {
            return false;
        }
        LumbridgeIronmanProfile.Roster roster = roster();
        LumbridgeIronmanProfile profile = roster.get(bot.getUsername());
        if (profile == null) {
            profile = new LumbridgeIronmanProfile(bot.getUsername(), bot.getDisplayName(),
                    bot.getSpawnTile(), ((LumbridgeIronmanBotScript) script).snapshotState().getArchetype());
            roster.put(profile);
        }
        profile.updateFrom(bot, (LumbridgeIronmanBotScript) script);
        SerializableFilesManager.savePlayer(bot);
        return true;
    }

    public static synchronized void disableProfile(String username) {
        LumbridgeIronmanProfile profile = roster().get(username);
        if (profile != null) {
            profile.setEnabled(false);
            saveRoster();
        }
    }

    public static synchronized int activePersistentCount() {
        int count = 0;
        for (LumbridgeIronmanProfile profile : roster().all()) {
            if (profile.isEnabled() && profile.isRespawnOnStartup()) {
                count++;
            }
        }
        return count;
    }

    private static BotPlayer loadOrCreateBot(LumbridgeIronmanProfile profile) {
        Player loaded = SerializableFilesManager.loadPlayer(profile.getUsername());
        if (loaded == null) {
            return BotPlayer.create(profile.getUsername(), profile.getDisplayName(), profile.getBestStartupTile());
        }
        if (!(loaded instanceof BotPlayer)) {
            Logger.getGlobal().warn("Persistent Lumbridge bot username collides with a non-bot account: "
                    + profile.getUsername());
            return null;
        }
        BotPlayer bot = (BotPlayer) loaded;
        return BotPlayer.restoreLoaded(bot, profile.getBestStartupTile());
    }

    private static LumbridgeIronmanProfile.Roster roster() {
        if (cachedRoster != null) {
            return cachedRoster;
        }
        cachedRoster = readRoster();
        return cachedRoster;
    }

    private static LumbridgeIronmanProfile.Roster readRoster() {
        if (!ROSTER_FILE.exists()) {
            return new LumbridgeIronmanProfile.Roster();
        }
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(ROSTER_FILE))) {
            Object value = input.readObject();
            if (value instanceof LumbridgeIronmanProfile.Roster) {
                return (LumbridgeIronmanProfile.Roster) value;
            }
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
        return new LumbridgeIronmanProfile.Roster();
    }

    private static void saveRoster() {
        try {
            File parent = ROSTER_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(ROSTER_FILE))) {
                output.writeObject(roster());
            }
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    private static BotPersonality.Archetype randomArchetype() {
        BotPersonality.Archetype[] values = BotPersonality.Archetype.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private static WorldTile randomTileNear(WorldTile center, int radius) {
        if (radius <= 0) {
            return new WorldTile(center);
        }
        int dx = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int dy = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        return center.transform(dx, dy, 0);
    }
}
*/
