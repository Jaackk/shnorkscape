package com.rs.game.player.controllers.pestcontrol;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import lombok.val;

/**
 * @author Kris | 2. okt 2018 : 11:56:41
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class VoidNPCD extends Dialogue {

	private final int id = 5956;

	@Override
	public void start() {
		npc(id, "Hello there, adventurer. What can I help you with?");
		stage = 0;
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		switch (stage++) {
		case 0:
			sendOptionsDialogue("Select an Option", "I'd like to upgrade my void to elite void.", "I'd like to purchase an armour patch.",
					"Nothing, good bye.");
			return;
		case 1:
			switch (componentId) {
			case OPTION_1:
				player("I'd like to upgrade my void to elite void.");
				stage = 10;
				return;
			case OPTION_2:
				player("I'd like to purchase an armour patch.");
				stage = 20;
				return;
			case OPTION_3:
				player("Nothing, good bye.");
				stage = 30;
				return;
			}
			return;
		case 10:
			npc(id, "Well of course! Just show me the piece you'd like to upgrade. It'll cost you 100 commendation points though.");
			stage = 30;
			return;
		case 20:
			npc(id, "Of course, that'll be 200 commendation points.");
			return;
		case 21:
			player("Sure, here you go.");
			return;
		case 22:
			val points = player.getPestPoints();
			if (points < 200) {
				player("Oh dear, it appears I don't have enough commendation points yet.");
			} else if (!player.getInventory().hasFreeSlots()) {
				player("I'll need to make some space in my inventory to accept this.");
			} else {
				sendItemDialogue(31640, 1, "The Void Knight hands you an armour patch.");
				player.setPestPoints(player.getPestPoints() - 200);
				player.getInventory().addItem(new Item(31640));
			}
			stage = 30;
			return;
		case 30:
		default:
			end();
			return;
		}
	}

	@Override
	public void finish() {

	}

}
