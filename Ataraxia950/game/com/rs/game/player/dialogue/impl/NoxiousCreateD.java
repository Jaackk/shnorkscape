package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.player.content.items.AraxCrafting;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Webbed entrance.
 *
 * @author Noel
 */
public class NoxiousCreateD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option", "Noxious Scythe", " Noxious Staff", "Noxious Longbow");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				if (AraxCrafting.canCraftWeapon(player, 31722)) {
					player.getInventory().deleteItem(31722, 1);
					player.getInventory().deleteItem(31721, 1);
					player.getInventory().addItem(31725, 1);
					sendItemDialogue(31725, 1, "You've successfully made a Noxious Scythe!");
					player.setNextAnimation(new Animation(24108));
					stage = 10;
				} else
					finish();
				break;
			case OPTION_2:
				if (AraxCrafting.canCraftWeapon(player, 31723)) {
					player.getInventory().deleteItem(31723, 1);
					player.getInventory().deleteItem(31721, 1);
					player.getInventory().addItem(31729, 1);
					sendItemDialogue(31729, 1, "You've successfully made a Noxious Staff!");
					player.setNextAnimation(new Animation(24109));
					stage = 10;
				} else
					finish();
				break;
			case OPTION_3:
				if (AraxCrafting.canCraftWeapon(player, 31724)) {
					player.getInventory().deleteItem(31724, 1);
					player.getInventory().deleteItem(31721, 1);
					player.getInventory().addItem(31733, 1);
					sendItemDialogue(31733, 1, "You've successfully made a Noxious Longbow!");
					player.setNextAnimation(new Animation(24110));
					stage = 10;
				} else
					finish();
				break;
			}
			break;

		case 10:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

}