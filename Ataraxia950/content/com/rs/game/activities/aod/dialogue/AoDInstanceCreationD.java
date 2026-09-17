package com.rs.game.activities.aod.dialogue;

import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputStringEvent;

/**
 * A dialogue to create an instance of AoD. Allows setting as password in lower case letters.
 * @author Kris | 30. sept 2017 : 17:09.49
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDInstanceCreationD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Start an Instance?",
				"Yes.",
				"No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage++) {
		case -1:
			if (componentId == OPTION_2) {
				end();
				return;
			} else if (componentId == OPTION_1) {
				sendOptionsDialogue("Set a password?",
						"Yes.",
						"No.");
			}
			break;
		case 0:
			if (componentId == OPTION_1) {
				player.sendInputString("Enter a password:", new InputStringEvent() {
					@Override
					public void run(Player player) {
						final String password = getString();
						if (password.equals("")) {
							end();
							return;
						}
						player.lock(2);
						new AngelOfDeath(player, password);
						end();
					}
				});
			} else if (componentId == OPTION_2) {
				new AngelOfDeath(player, null);
				end();
				return;
			}
		}
	}

	@Override
	public void finish() {
		
	}

}
