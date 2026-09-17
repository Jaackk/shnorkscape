package com.rs.game.activities.rots.dialogues;

import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Kris | 3. sept 2017 : 23:34.20
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class RiseOfTheSixLootingD extends Dialogue {

	@Override
	public void start() {
		player.setFinishedRoTS();
		sendNPCDialogue(18450, 9827, "I appreciate you putting the time into testing my new experiments. " + player.getDisplayName() +". Enjoy your reward - if you make it out alive!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		((RiseOfTheSixController) player.getControlerManager().getControler()).takeLoot();
	}

	@Override
	public void finish() {

	}

}
