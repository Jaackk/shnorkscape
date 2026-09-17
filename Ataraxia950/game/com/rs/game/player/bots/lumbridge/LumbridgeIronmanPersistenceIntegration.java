/*
package com.rs.game.player.bots.lumbridge;

import com.rs.game.WorldTile;
import com.rs.game.player.bots.BotPlayer;

public final class LumbridgeIronmanPersistenceIntegration {

    private LumbridgeIronmanPersistenceIntegration() {
    }

    // Review-only integration guide.
    // The persistence service above is intentionally commented out with the rest
    // of the prototype. When you are ready to test it, wire the snippets below
    // into the live classes after uncommenting the Lumbridge package.

    // BotManager imports:
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanBotScript;
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanData;
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanPersistence;

    // BotManager public helpers:
    //
    // public static BotPlayer spawnLumbridgeIronmanBot(String requestedName, WorldTile tile) {
    //     WorldTile spawnTile = tile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : tile;
    //     return spawnBot(requestedName, spawnTile, new LumbridgeIronmanBotScript());
    // }
    //
    // public static BotPlayer spawnPersistentLumbridgeIronmanBot(String requestedName, WorldTile tile,
    //         BotPersonality.Archetype archetype) {
    //     return LumbridgeIronmanPersistence.spawnPersistentBot(requestedName, tile, archetype);
    // }
    //
    // public static int spawnPersistentLumbridgeIronmanBots(int amount, WorldTile center, int radius) {
    //     return LumbridgeIronmanPersistence.spawnPersistentBots(amount, center, radius);
    // }
    //
    // public static BotPlayer spawnPersistentBot(String username, String displayName, WorldTile tile,
    //         BotScript script) {
    //     if (username == null || username.trim().isEmpty()) {
    //         throw new IllegalArgumentException("username");
    //     }
    //     if (tile == null) {
    //         throw new IllegalArgumentException("tile");
    //     }
    //     if (script == null) {
    //         throw new IllegalArgumentException("script");
    //     }
    //     String protocolName = Utils.formatPlayerNameForProtocol(username);
    //     if (BOTS.containsKey(protocolName) || World.containsPlayer(protocolName)) {
    //         throw new IllegalStateException("Bot already online: " + protocolName);
    //     }
    //     String safeDisplayName = displayName == null || displayName.trim().isEmpty()
    //             ? Utils.formatPlayerNameForDisplay(protocolName)
    //             : displayName.trim().replace('_', ' ');
    //     BotPlayer bot = BotPlayer.create(protocolName, safeDisplayName, tile);
    //     registerLoadedBot(bot, script);
    //     return bot;
    // }
    //
    // public static void registerLoadedBot(BotPlayer bot, BotScript script) {
    //     if (bot == null) {
    //         throw new IllegalArgumentException("bot");
    //     }
    //     if (script == null) {
    //         throw new IllegalArgumentException("script");
    //     }
    //     script.bind(bot);
    //     BOTS.put(bot.getUsername(), new BotContext(bot, script));
    //     ensurePulseTask();
    // }
    //
    // public static BotScript getScript(BotPlayer bot) {
    //     BotContext context = bot == null ? null : BOTS.get(bot.getUsername());
    //     return context == null ? null : context.script;
    // }
    //
    // static void savePersistentBot(BotPlayer bot, String reason) {
    //     LumbridgeIronmanPersistence.saveBot(bot, reason);
    // }

    // BotPlayer lifecycle changes:
    //
    // @Override
    // public void realFinish() {
    //     destroy(true);
    // }
    //
    // public void destroy() {
    //     destroy(false);
    // }
    //
    // private void destroy(boolean savePersistentState) {
    //     if (hasFinished()) {
    //         return;
    //     }
    //     if (savePersistentState) {
    //         BotManager.savePersistentBot(this, "shutdown");
    //     }
    //     BotManager.unregister(this);
    //     try {
    //         stopAll();
    //         if (getControlerManager() != null) {
    //             getControlerManager().logout();
    //         }
    //     } catch (Exception ignored) {
    //     }
    //     setActive(false);
    //     setRunning(false);
    //     setFinished(true);
    //     if (getLastRegionId() > 0) {
    //         World.getRegion(getLastRegionId()).removePlayerIndex(getIndex());
    //     }
    //     World.forceRemovePlayer(this);
    // }
    //
    // public static BotPlayer restoreLoaded(BotPlayer bot, WorldTile fallbackTile) {
    //     if (bot == null) {
    //         throw new IllegalArgumentException("bot");
    //     }
    //     LoginManager.initBot(bot, bot.getUsername());
    //     if (bot.getX() <= 0 || bot.getY() <= 0) {
    //         bot.setLocation(fallbackTile == null ? bot.getSpawnTile() : fallbackTile);
    //     }
    //     bot.startRestored();
    //     return bot;
    // }
    //
    // public void startRestored() {
    //     start(true);
    // }
    //
    // @Override
    // public void start() {
    //     start(false);
    // }
    //
    // private void start(boolean preserveAppearance) {
    //     getAccountPin().resetLocked();
    //     getAccountPin().setPinEntered();
    //     loadMapRegions();
    //     setClientHasLoadedMapRegion();
    //     if (!hasCompleted()) {
    //         setCompleted();
    //     }
    //     setRunning(true);
    //     enableRunMode();
    //     if (!preserveAppearance) {
    //         randomizeAppearance();
    //     }
    //     getAppearence().generateAppearenceData();
    //     getControlerManager().login();
    //     setActive(true);
    //     World.updateEntityRegion(this);
    // }

    // ServerLauncher imports:
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanPersistence;

    // ServerLauncher init hook:
    // Place after World.init and the late world content initializers, shortly
    // before the "Server launched" log line:
    //
    // LumbridgeIronmanPersistence.loadOnStartup();

    // ServerLauncher saveFiles hook:
    // Place after the player save loop so roster and script snapshots are saved
    // on the same two-minute cadence as normal player files:
    //
    // LumbridgeIronmanPersistence.saveActiveBots("autosave");

    // World.safeShutdown:
    // No special hook is required if BotPlayer.realFinish calls destroy(true).
    // The existing safeShutdown loop already calls realFinish on every active
    // player. This keeps shutdown persistence in one bot lifecycle path.

    // Commands.java admin snippets:
    //
    // case "spawnlumpersist":
    // case "spawnpersistentlumbot": {
    //     String name = cmd.length > 1 ? getRestOfInput(1, cmd) : null;
    //     BotPlayer bot = BotManager.spawnPersistentLumbridgeIronmanBot(name, new WorldTile(player), null);
    //     player.sendMessage("Spawned persistent Lumbridge ironman bot " + bot.getDisplayName() + ".");
    //     return true;
    // }
    //
    // case "spawnlumpersistmany":
    // case "lumbyfillpersist": {
    //     int amount = cmd.length > 1 && isInteger(cmd[1]) ? Integer.parseInt(cmd[1]) : 25;
    //     amount = Math.max(1, Math.min(1000, amount));
    //     int spawned = BotManager.spawnPersistentLumbridgeIronmanBots(
    //             amount, LumbridgeIronmanData.LUMBRIDGE_SPAWN, 8);
    //     player.sendMessage("Spawned " + spawned + " persistent Lumbridge ironman bots.");
    //     return true;
    // }
    //
    // case "savelumbots": {
    //     LumbridgeIronmanPersistence.saveActiveBots("command");
    //     player.sendMessage("Saved persistent Lumbridge ironman bots.");
    //     return true;
    // }
    //
    // case "lumbotcount": {
    //     player.sendMessage("Persistent Lumbridge ironman profiles: "
    //             + LumbridgeIronmanPersistence.activePersistentCount() + ".");
    //     return true;
    // }

    // Suggested rollout:
    // 1. Uncomment only the profile and persistence classes.
    // 2. Add BotManager.getScript and registerLoadedBot, then compile.
    // 3. Add BotPlayer.restoreLoaded and destroy(true), then compile.
    // 4. Add ServerLauncher save/load hooks.
    // 5. Spawn one persistent bot, run ::savelumbots, restart, and confirm the
    //    same username, levels, inventory, equipment, bank, location, and goal
    //    memory come back.
}
*/
