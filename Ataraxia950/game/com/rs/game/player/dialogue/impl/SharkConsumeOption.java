package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Shark head consume-option.
 *
 * @author Noel
 */
public class SharkConsumeOption extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Consume fish while fishing?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				player.consumeFish = true;
				sendItemDialogue(34205, 1, "Wearing a full shark outfit will now eat the fish you'll fish.");
				stage = 1;
				break;
			case OPTION_2:
				player.consumeFish = false;
				sendItemDialogue(34205, 1, "Your shark outfit will no longer eat the fish you've fished.");
				stage = 1;
				break;
			}
			break;

		case 1:
			end();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}