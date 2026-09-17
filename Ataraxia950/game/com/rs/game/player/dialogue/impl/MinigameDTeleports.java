package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class MinigameDTeleports extends Dialogue {

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3563, 3288, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2994, 9679, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(2652, 2655, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.daemonheimTeleport(player, new WorldTile(3972, 5562, 0));
				end();
			}
			if (componentId == OPTION_5) {
				stage = 1;
				sendOptionsDialogue("Choose your destination", "Fight Kiln", "Fight Caves", "Recipe for Disaster",
						"Duel Arena", Colors.RED + "More Options..");
			}
		} else if (stage == 1) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(4743, 5170, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(4613, 5129, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(1866, 5346, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(3325, 3232, 0));
				end();
				end();
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Warrior Guild", "Soul Wars", "Dominion Tower",
						"Artisans WorkShop", Colors.RED + "First Page..");
				stage = 2;
			}
		} else if (stage == 2) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(2879, 3542, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(3081, 3475, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3367, 3083, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(3032, 3338, 0));
				end();
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Barrows", "Clan Wars", "Pest Control", "Dungeoneering",
						Colors.RED + "More Options..");
				stage = -1;
			}
		}
	}

	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination", "Barrows", "Clan Wars", "Pest Control", "Dungeoneering",
				Colors.RED + "More Options..");
	}

	@Override
	public void finish() {
	}
}
