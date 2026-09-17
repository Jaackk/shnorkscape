package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Home Altar.
 *
 * @author Noel
 */
public class AltarSwapD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option", "Swap Prayer Books", "Swap Spellbook "+(player.getCombatDefinitions().getSpellBook() != 2 ? "(Lunar)" : "(Modern)")+".",
				"Swap Spellbook "+(player.getCombatDefinitions().getSpellBook() != 1 ? "(Ancients)" : "(Modern)")+".");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				if (!player.getPrayer().isAncientCurses()) {

					sendDialogue("The altar fills your head with dark thoughts, purging the "
							+ "prayers from your memory and leaving only Curses in their place.");
					player.getPrayer().setPrayerBook(true);
				} else {
					sendDialogue("The altar eases its grip on your mind. The Curses slip from "
							+ "your memory and you recall the prayers you used to know.");
					player.getPrayer().setPrayerBook(false);
				}
				player.setNextAnimation(new Animation(12563));
				break;
			case OPTION_2:
				if (player.getCombatDefinitions().getSpellBook() != 2) {

					sendDialogue("Your mind clears and you switch to the Lunar spellbook.");
					player.getCombatDefinitions().setSpellBook(2);
				} else {
					sendDialogue("Your mind clears and you switch to the Modern spellbook.");
					player.getCombatDefinitions().setSpellBook(0);
				}
				break;
			case OPTION_3:
				if (player.getCombatDefinitions().getSpellBook() != 1) {

					sendDialogue("Your mind clears and you switch to the Ancient spellbook.");
					player.getCombatDefinitions().setSpellBook(1);
				} else {
					sendDialogue("Your mind clears and you switch to the Modern spellbook.");
					player.getCombatDefinitions().setSpellBook(0);
				}
				break;
			}
			stage++;
			break;
		case 99:
		default:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}