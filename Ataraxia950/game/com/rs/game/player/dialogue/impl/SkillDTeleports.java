package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.RunespanController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class SkillDTeleports extends Dialogue {

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(2596, 3410, 0));
				end();
			}
			if (componentId == OPTION_2) {
				sendOptionsDialogue("Choose your destination", "Al-kharid Mining", "Karamja Mining",
						"Living rock caverns (LRC)", "Red Sandstone");
				stage = 3;
			}
			if (componentId == OPTION_3) {
				sendOptionsDialogue("Choose your destination", "Gnome Agility Course",
						"Barbarian Outpost Agility Course", "Wilderness Agility Course", "Agility Pyramid");
				stage = 2;
			}
			if (componentId == OPTION_4) {
				sendOptionsDialogue("Choose your destination", "Jungle", "Seer's Village");
				stage = 7;
			}
			if (componentId == OPTION_5) {
				stage = 1;
				sendOptionsDialogue("Choose your destination", "Runecrafting", "Summoning", "Farming", "Hunter",
						Colors.RED + "More Options..");
			}
		} else if (stage == 7) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(2817, 3083, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2726, 3477, 0));
				end();
			}
		} else if (stage == 5) {
			if (componentId == OPTION_1) {
				end();
				if (player.getControlerManager().getControler() != null)
					player.getControlerManager().getControler().forceClose();
				RunespanController.enterRunespan(player);
			}
			if (componentId == OPTION_2) {
				end();
				Magic.vineTeleport(player, new WorldTile(2598, 3157, 0));
			}
		} else if (stage == 1) {
			if (componentId == OPTION_1) {
				stage = 5;
				sendOptionsDialogue("Choose your destination", "Runespan", "Classic Altars");
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2923, 3449, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3052, 3304, 0));
				end();
			}
			if (componentId == OPTION_4) {
				stage = 6;
				sendOptionsDialogue("Choose your destination", "Falconry", "Feldip hills", "Impetuous Impulses",
						"Tree Gnome Stronghold Hunter area", Colors.RED + "More Options..");
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Fishing Guild", "Mining teleports", "Agility teleports",
						"Woodcutting", Colors.RED + "More Options..");
				stage = -1;
			}
		} else if (stage == 6) {
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2526, 2916, 0));
				end();
			}
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(2362, 3623, 0));
				end();
			}
			if (componentId == OPTION_3) {
				player.getControlerManager().startControler("PuroPuro");
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(2457, 3538, 0));
				end();
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Port Phasmatys Hunter area", "Rellekka Hunter area",
						"Desert Quarry Hunter area", Colors.RED + "More Options..");
				stage = 8;
			}
		} else if (stage == 2) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(2470, 3436, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2552, 3563, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(2998, 3911, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(3358, 2828, 0));
				end();
			}
		} else if (stage == 3) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3300, 3312, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2849, 3033, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3652, 5122, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(2590, 2880, 0));
				end();
			}
		} else if (stage == 8) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3660, 3429, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(2729, 3864, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(3169, 2867, 0));
				end();
			}
			if (componentId == OPTION_4) {
				stage = 6;
				sendOptionsDialogue("Choose your destination", "Falconry", "Feldip hills", "Impetuous Impulses",
						"Tree Gnome Stronghold Hunter area", Colors.RED + "More Options..");
			}
		}
	}

	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination", "Fishing Guild", "Mining teleports", "Agility teleports",
				"Woodcutting", Colors.RED + "More Options..");
	}

}
