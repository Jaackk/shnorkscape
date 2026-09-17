/*
package com.rs.game.player.bots.lumbridge;

import com.rs.game.WorldTile;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.bots.BotPersonality;
import com.rs.game.player.bots.BotPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class LumbridgeIronmanIntegration {

    private LumbridgeIronmanIntegration() {
    }

    // Review-only integration notes.
    // This file intentionally contains no active server code while commented.
    // When you are ready to wire the prototype in, move the snippets below into
    // BotManager and Commands, then uncomment the four prototype classes in this package.

    // BotManager import:
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanBotScript;
    // import com.rs.game.player.bots.lumbridge.LumbridgeIronmanData;

    // BotManager spawn helpers:
    //
    // public static BotPlayer spawnLumbridgeIronmanBot(String requestedName, WorldTile tile) {
    //     WorldTile spawnTile = tile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : tile;
    //     return spawnBot(requestedName, spawnTile, new LumbridgeIronmanBotScript());
    // }
    //
    // public static BotPlayer spawnLumbridgeIronmanBot(String requestedName, WorldTile tile,
    //         BotPersonality.Archetype archetype) {
    //     WorldTile spawnTile = tile == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : tile;
    //     BotPersonality personality = archetype == null
    //             ? BotPersonality.random()
    //             : BotPersonality.forArchetype(archetype);
    //     return spawnBot(requestedName, spawnTile, new LumbridgeIronmanBotScript(personality));
    // }
    //
    // public static int spawnLumbridgeIronmanBots(int amount, WorldTile center, int radius) {
    //     int spawned = 0;
    //     WorldTile spawnCenter = center == null ? LumbridgeIronmanData.LUMBRIDGE_SPAWN : center;
    //     int spread = Math.max(0, radius);
    //     for (int index = 0; index < amount; index++) {
    //         int dx = spread == 0 ? 0 : ThreadLocalRandom.current().nextInt(-spread, spread + 1);
    //         int dy = spread == 0 ? 0 : ThreadLocalRandom.current().nextInt(-spread, spread + 1);
    //         spawnLumbridgeIronmanBot(null, spawnCenter.transform(dx, dy, 0));
    //         spawned++;
    //     }
    //     return spawned;
    // }

    // Optional BotManager type check if you want targeted despawning later:
    //
    // public static int despawnLumbridgeIronmanBots() {
    //     int count = 0;
    //     for (BotContext context : BOTS.values()) {
    //         if (!(context.script instanceof LumbridgeIronmanBotScript)) {
    //             continue;
    //         }
    //         BOTS.remove(context.bot.getUsername());
    //         context.script.stop();
    //         context.bot.destroy();
    //         count++;
    //     }
    //     return count;
    // }

    // Commands.java admin command snippets near the existing spawnbot cases:
    //
    // case "spawnlumbot":
    // case "spawnlumbridgebot":
    // case "spawnironbot": {
    //     int amount = 1;
    //     String botName = null;
    //     if (cmd.length > 1 && isInteger(cmd[1])) {
    //         amount = Math.max(1, Math.min(1000, Integer.parseInt(cmd[1])));
    //         botName = cmd.length > 2 ? getRestOfInput(2, cmd) : null;
    //     } else {
    //         botName = cmd.length > 1 ? getRestOfInput(1, cmd) : null;
    //     }
    //     if (amount == 1) {
    //         BotPlayer bot = BotManager.spawnLumbridgeIronmanBot(botName, new WorldTile(player));
    //         player.sendMessage("Spawned Lumbridge ironman bot " + bot.getDisplayName() + ".");
    //     } else {
    //         int spawned = BotManager.spawnLumbridgeIronmanBots(amount, new WorldTile(player), 6);
    //         player.sendMessage("Spawned " + spawned + " Lumbridge ironman bots.");
    //     }
    //     return true;
    // }
    //
    // case "spawnlumbots":
    // case "lumbyfill": {
    //     int amount = cmd.length > 1 && isInteger(cmd[1]) ? Integer.parseInt(cmd[1]) : 25;
    //     amount = Math.max(1, Math.min(1000, amount));
    //     int spawned = BotManager.spawnLumbridgeIronmanBots(amount, LumbridgeIronmanData.LUMBRIDGE_SPAWN, 8);
    //     player.sendMessage("Spawned " + spawned + " Lumbridge ironman bots around Lumbridge.");
    //     return true;
    // }
    //
    // case "clearlumbots":
    // case "clearlumbridgebots": {
    //     player.sendMessage("Despawned " + BotManager.despawnLumbridgeIronmanBots()
    //             + " Lumbridge ironman bots.");
    //     return true;
    // }

    // Recommended first test pass after uncommenting:
    // 1. Spawn one with ::spawnlumbot at Lumbridge.
    // 2. Watch ::botdebug until it picks food, mining, combat, and banking goals.
    // 3. Spawn 25 with ::spawnlumbots 25 and profile WorldThread cycle time.
    // 4. Only then try 250, then 1000.
}
*/
