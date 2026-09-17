package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class MinigameTeleports extends Dialogue {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#run(int, int)
	 */
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
				if (player.isCanPvp() && player.getAttackedBy() != null)
					player.sendMessage("You cannot teleport while in Player-vs-player combat!", true);
				else {
						Magic.daemonheimTeleport(player, new WorldTile(3447, 3698, 0));
				}
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
				Magic.vineTeleport(player, new WorldTile(5013, 744, 1));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(3325, 3232, 0));
				end();
				end();
			}
			if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose your destination", "Warrior Guild", "Soul Wars", "Dominion Tower", "Artisan's Workshop",/*"Fist of Guthix", */Colors.RED + "More Options..");
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
			/*if (componentId == OPTION_4) {
				player.sendMessage(Colors.RED + "Welcome to the Fist of Guthix minigame-area!");
				Magic.vineTeleport(player, new WorldTile(1681, 5599, 0));
				// Magic.resourcesTeleport(player, new WorldTile(1681, 5599,
				// 0));
				end();
			}*/
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(3032, 3338, 0));
				end();
			}
			if (componentId == OPTION_5) {
			    sendOptionsDialogue("Choose your destination", "Temple of Aminishi", Colors.RED + "More Options..");
			    stage = 4;
			}
		} else if (stage == 3) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3032, 3338, 0));
				end();
			} else {
				stage = -1;
				sendOptionsDialogue("Choose your destination", "Barrows", "Clan Wars", "Pest Control", "Dungeoneering",
						Colors.RED + "More Options..");
			}
		} else if(stage == 4) {
		    if (componentId == OPTION_1) {
	             Magic.vineTeleport(player, new WorldTile(2094, 11348, 0));
	             player.getControlerManager().startControler("EliteDungeonsLobby");
	             end();
            }
		     if (componentId == OPTION_2) {
	             sendOptionsDialogue("Choose your destination", "Barrows", "Clan Wars", "Pest Control", "Dungeoneering",
	                        Colors.RED + "More Options..");
	             stage = -1;
		     }
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#start()
	 */
	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination", "Barrows", "Clan Wars", "Pest Control", "Dungeoneering",
				Colors.RED + "More Options..");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#finish()
	 */
	@Override
	public void finish() {
	}
}