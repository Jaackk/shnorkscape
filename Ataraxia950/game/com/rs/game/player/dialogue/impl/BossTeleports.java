package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;

/**
 * Handles the Boss Teleport dialogue.
 *
 * @author Noel
 */
public class BossTeleports extends Dialogue {

	/**
	 * Handles Godwars Dungeon teleporting.
	 *
	 * @param placeX
	 *            The X coordinate.
	 * @param placeY
	 *            The Y coordinate.
	 * @param placePlane
	 *            The Z coordinate.
	 */
	public static void teleportPlayer(Player player, final int placeX, final int placeY, final int placePlane,
			String controller) {
		Magic.vineTeleport(player, new WorldTile(placeX, placeY, placePlane));
		final WorldTile teleTile = new WorldTile(placeX, placeY, placePlane);
		if (!player.getControlerManager().processMagicTeleport(teleTile))
			return;
		player.lock(4);
		player.stopAll();
		player.setNextGraphics(new Graphics(1229));
		player.setNextAnimation(new Animation(7082));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextAnimation(new Animation(7084));
				player.setNextGraphics(new Graphics(1228));
				player.setNextWorldTile(teleTile);
				player.getControlerManager().magicTeleported(Magic.MAGIC_TELEPORT);
				player.checkMovement(placeX, placeY, placePlane);
				if (player.getControlerManager().getControler() == null)
					Magic.teleControlersCheck(player, teleTile);
				if (controller != null)
					player.getControlerManager().startControler(controller);
				player.unlock();
				stop();
			}
		}, 4);
	}

	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination", "Godwars Dungeon", "Corporeal Beast", "Kalphite Queen",
				"Queen Black Dragon", Colors.RED + "More Options..");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			if (componentId == OPTION_1) {
				teleportPlayer(player, 2882, 5311, 0, "GodWars");
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2966, 4383, 2));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3479, 9488, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(1195, 6499, 0));
				end();
			}
			if (componentId == OPTION_5) {
				stage = 2;
				sendOptionsDialogue("Choose your destination", "King Black Dragon", "Bork", "Barrelchest",
						"Dagannoth kings", Colors.RED + "More Options..");
			}
			break;
		case 2:
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3051, 3518, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(3143, 5545, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3803, 2844, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(1912, 4367, 0));
				end();
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Chaos Elemental " + Colors.RED + "(Deep Wild - Lv 50)",
						"Rise of the Six", "Tormented Demons", "Kalphite King", Colors.RED + "More Options..");
				stage = 4;
			}
			break;
		case 4:
			if (componentId == OPTION_1) {
				player.getDialogueManager().startDialogue("ChaosElementalD");
			}
			if (componentId == OPTION_2) {
				teleportPlayer(player, 3540, 3308, 0, null);
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(2571, 5735, 0));
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(2974, 1654, 0));
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Vorago", "The Heart", "Araxyte Hive", "Giant Mole", Colors.RED + "More Options..");
				stage = 5;
			}
			break;
		case 5:
			switch (componentId) {
			case OPTION_1:
				teleportPlayer(player, 2972, 3430, 0, null);
				break;
			case OPTION_2:
				teleportPlayer(player, 3199, 6939, 1, "HeartOfGielinorController");
				break;
			case OPTION_3:
				Magic.vineTeleport(player, new WorldTile(4512, 6289, 1));
				break;
			case OPTION_4:
				Magic.vineTeleport(player, new WorldTile(2985, 3381, 0));
				break;
            case OPTION_5:
                sendOptionsDialogue("Choose your destination", "The Magister", Colors.RED + "More Options..");
                stage = 6;
                break;
			}
			break;
        case 6:
            switch (componentId) {
            case OPTION_1:
                teleportPlayer(player, 2464, 6729, 1, SophanemSlayerDungeon.class.getSimpleName());
                break;
            case OPTION_2:
                sendOptionsDialogue("Choose your destination", "Godwars Dungeon", "Corporeal Beast", "Kalphite Queen",
                        "Queen Black Dragon", Colors.RED + "More Options..");
                stage = -1;
                break;
            }
            break;
		}
	}

	@Override
	public void finish() {
	}
}