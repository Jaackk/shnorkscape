package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.AccountInterfaceManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Used for handling the Player Settings dialogue.
 *
 * @author Noel
 */
public class setIplock extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(Colors.CYAN + "Account Lock Settings",
				"Computer Lock: " + (player.iplocked ? Colors.GREEN + "Enabled" : Colors.RED + "Disabled") + "</col>.",
				"Locked with: " + player.lockedwith);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				if (player.iplocked == true) {
					player.iplocked = false;
					player.lockedwith = null;
					player.sendMessage("You have removed your Account Lock.");
					AccountInterfaceManager.sendInterface(player);
					stage = 0;
					break;
				} else {
					player.sendMessage(
							"You have locked your account to this Computer Address: " + player.getCurrentMac() + ".");
					player.iplocked = true;
					player.lockedwith = player.getCurrentMac();
					AccountInterfaceManager.sendInterface(player);
					stage = 0;
					break;
				}
			case OPTION_2:
				stage = -1;
			}

		case 0:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}