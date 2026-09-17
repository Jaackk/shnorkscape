package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.fistofguthix.FOGManager;
import com.rs.game.player.controllers.FOGController;
import com.rs.game.player.dialogue.Dialogue;

/**
 * The dialogue for the fist of guthix minigame.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 30, 2017 at 8:42:01 PM.
 */
public class LeaveFistOfGuthix extends Dialogue {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#start()
	 */
	@Override
	public void start() {
		sendOptionsDialogue("Are you sure you want to leave?", "Yes please!", "No I am not.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#run(int, int)
	 */
	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			switch (componentId) {
			case OPTION_1:
				if (player.getControlerManager().getControler() != null)
					((FOGController) player.getControlerManager().getControler()).exit(true);
				if (FOGManager.get().getFOGInstance().getTeam(player) != null) {
					FOGManager.get().getFOGInstance().getTeam(player).forfeit(player);
				}
				end();
				break;
			case OPTION_2:
				end();
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
