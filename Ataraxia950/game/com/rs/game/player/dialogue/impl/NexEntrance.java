package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.controllers.ZGDController;
import com.rs.game.player.dialogue.Dialogue;

public final class NexEntrance extends Dialogue {

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(int interfaceId, int componentId) {
//		int players = ZarosGodwars.getPlayers().size();
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue("What would you like to do?", "Climb down.", "Stay here.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				player.setNextWorldTile(new WorldTile(player.getX() + 2, player.getY(), player.getPlane()));
				player.getControlerManager().startControler(ZGDController.class.getSimpleName(), new WorldTile(2925, 5203, 0), false, true);
			}
			end();
		}

	}

	@Override
	public void start() {
		sendDialogue("The room beyond this point is a prison!", "There is no way out other than death or teleport.",
				"Only those who endure dangerous encounters should proceed.");
	}

}
