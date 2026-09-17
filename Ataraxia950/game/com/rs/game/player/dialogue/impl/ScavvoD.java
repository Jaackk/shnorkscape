package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Scavvo's dialogue.
 *
 * @author Noel
 */
public class ScavvoD extends Dialogue {

	/**
	 * The NPC ID.
	 */
	int npcId, stage;

	@Override
	public void start() {
		if (stage == 1) {
			sendNPCDialogue(npcId, NORMAL, "Hello, what can I do for you?");
			stage = 1;
		} else {
			sendOptionsDialogue("Choose an Option", "Melee Equipment", "Ranged Equipment", "Magic Equipment",
					"Food & Potions");
			stage = 4;
		}
	}
		@Override
		public void run ( int interfaceId, int componentId){
			switch (stage) {
				case 1:
					sendPlayerDialogue(NORMAL, "Hello.. what could you do for me..?");
					stage = 2;
					break;

				case 2:
					sendNPCDialogue(npcId, NORMAL,
							"I offer a wide variety of Equipment that can help your start to " + Settings.SERVER_NAME);
					stage = 3;
					break;

				case 3:
					sendOptionsDialogue("Choose an Option", "Melee Equipment", "Ranged Equipment", "Magic Equipment",
							"Food & Potions");
					stage = 4;
					break;
				case 4:
					switch (componentId) {
						case OPTION_1:
							finish();
							ShopsDataParser.openShop(player, 3);
							break;
						case OPTION_2:
							finish();
							ShopsDataParser.openShop(player, 8);
							break;
						case OPTION_3:
							finish();
							ShopsDataParser.openShop(player, 7);
							break;
						case OPTION_4:
							finish();
							ShopsDataParser.openShop(player, 6);
							break;
					}
					break;
			}
		}
		@Override
		public void finish() {
			player.getInterfaceManager().closeChatBoxInterface();
		}
	}
