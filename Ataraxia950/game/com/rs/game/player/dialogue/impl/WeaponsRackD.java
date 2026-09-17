package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.game.player.dialogue.Dialogue;

public class WeaponsRackD extends Dialogue {

	private static final String[] RACK_NAMES = { "Red Boxing Gloves", "Blue Boxing Gloves", "Wooden Sword",
			"Wooden Shield", "Pugle Stick" };

	@Override
	public void start() {
		int length = (int) this.parameters[0];
		String[] NAMES = new String[length];
		System.arraycopy(RACK_NAMES, 0, NAMES, 0, length);
		sendOptionsDialogue("Select a weapon of your choice.", NAMES);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getInventory()
				.addItem(new Item(HouseConstants.WEAPON_RACK[getOrdinal(componentId)], 1));
		end();
	}

	@Override
	public void finish() {
	}
}