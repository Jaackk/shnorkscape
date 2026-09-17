package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Lend;

public class EmptyD extends Dialogue {

	@Override
	public void start() {
		sendDialogue(Colors.RED + "<shad=000000>Warning: this will delete all items in your inventory!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Empty inventory?", "Yes.", "No.");
			stage = 0;
			break;
		case 0:
			finish();
			switch (componentId) {
			case OPTION_1:
				Lend lend = LendingManager.getLend(player);
				if (lend != null) {
					Player lender = World.getPlayer(lend.getLendee());
					if (lender != null
							&& lender.getInventory().containsOneItem(lend.getItem().getDefinitions().getLendId()))
						LendingManager.unLend(lend);
				}
				player.getActionManager().forceStop();
				player.getInventory().reset();
				player.sendMessage("Successfully deleted inventory.");
				break;
			}
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}