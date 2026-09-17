package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.divination.DivinationConvert;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles memory convert options.
 *
 * @author Noel
 */
public class ConvertMemoriesD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option", "Convert Energy", "Convert Experience", "Convert Enhanced Experience");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (componentId) {
		case OPTION_1:
			player.getActionManager()
					.setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_ENERGY }));
			break;
		case OPTION_2:
			player.getActionManager()
					.setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_XP }));
			break;
		case OPTION_3:
			player.getActionManager()
					.setAction(new DivinationConvert(player, new Object[] { ConvertMode.CONVERT_TO_MORE_XP }));
			break;
		}
		finish();
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}