package com.rs.game.activities.aod.dialogue;

import com.rs.game.player.dialogue.Dialogue;

/**
 * A dialogue allowing player to select whether to start or join an instance.
 * @author Kris | 30. sept 2017 : 17:10.18
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDInstanceD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option",
				"Start an instance.",
				"Join an instance.",
				"Nothing.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1)
			player.getDialogueManager().startDialogue("AoDInstanceCreationD");
		else if (componentId == OPTION_2)
			player.getDialogueManager().startDialogue("AoDInstanceJoiningD");
	}

	@Override
	public void finish() {
		
	}

}
