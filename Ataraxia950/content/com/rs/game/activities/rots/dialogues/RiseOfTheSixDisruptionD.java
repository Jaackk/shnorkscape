package com.rs.game.activities.rots.dialogues;

import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Kris | 3. sept 2017 : 23:34.11
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class RiseOfTheSixDisruptionD extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(18450, 9827, "Be warned! Some of my faithful servants have taken a sojourn in the Shadow Realm. The shadow has... changed them. I'd advise you to return back but - well - what fun would that be?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("<col=ff0000>WARNING:</col> DO YOU WISH TO UNLEASE THE SHADOW?", "Yes.", "I'm not ready yet.");
			break;
		case 0:
			if (componentId == OPTION_1) {
				if (player.getControlerManager().getControler() instanceof RiseOfTheSixController)
					((RiseOfTheSixController) player.getControlerManager().getControler()).getInstance().initiateFight();
			}
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {

	}

}