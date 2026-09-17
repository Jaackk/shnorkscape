package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Training teleport options dialogue.
 * 
 * @author Noel
 */
public class TrainingDTeleports extends Dialogue {

	@Override
	public void start() {
		if (player.isLocked()) {
			end();
			return;
		}
		sendOptionsDialogue("Choose your destination", "Rock Crabs", "Glacor Cave", "Dwarf Battlefield",
				"Frost Dragons", "Slayer Locations");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				sendOptionsDialogue("Select an Option", "East Rock Crabs", "West Rock Crabs");
				stage = 0;
				break;
			case OPTION_2:
				Magic.vineTeleport(player, new WorldTile(4181, 5726, 0));
				break;
			case OPTION_3:
				Magic.vineTeleport(player, new WorldTile(1519, 4704, 0));
				break;
			case OPTION_4:
				if (player.getSkills().getLevel(Skills.DUNGEONEERING) < 85) {
					sendDialogue("This area requires at least level 85 Dungeoneering to access!");
					stage = 9;
					return;
				}
				Magic.vineTeleport(player, new WorldTile(1298, 4510, 0));
				break;
			case OPTION_5:
				finish();
				player.getDialogueManager().startDialogue("SlayerTeleports");
				break;
			}
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, new WorldTile(2710, 3710, 0));
				break;
			case OPTION_2:
				Magic.vineTeleport(player, new WorldTile(2672, 3710, 0));
				break;
			default:
				finish();
				break;
			}
			break;
		case 9:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}