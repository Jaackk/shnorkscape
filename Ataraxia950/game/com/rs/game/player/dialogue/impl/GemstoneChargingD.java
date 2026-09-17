package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class GemstoneChargingD extends Dialogue {

	private Item item;
	private int type;
	
	@Override
	public void start() {
		item = (Item) parameters[0];
		if (item.getId() == 1631 || item.getId() == 1632)
			type = 0;
		if (item.getId() == 6571 || item.getId() == 6572)
			type = 1;
		if (item.getId() == 31851 || item.getId() == 31852)
			type = 2;
		sendOptionsDialogue("Are you sure you wish to charge the armour?",
				"Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				player.sendInputInteger("How many " + (type == 0 ? "dragonstones" : type == 1 ? "onyxes" : "hydrixes") + " would you like to use?", new InputIntegerEvent() {
					@Override
					public void run(Player player) {
						final int input = getInteger();
						if (input <= 0)
							return;
						int amount = player.getInventory().getAmountOf(item.getId());
						if (input < amount)
							amount = input;
						player.getInventory().deleteItem(item.getId(), amount);
						final int charges = amount * (type == 0 ? 10 : type == 1 ? 100 : 1000);
						player.getGemstoneArmour().addCharges(type, charges);
						sendItemDialogue(item.getDefinitions().isNoted() ? item.getId() - 1 : item.getId(), 1, "You charge the armour with " + charges + " charges. Your armour now has " + player.getGemstoneArmour().getCharges(type) + " " + (type == 0 ? "dragonstone" : type == 1 ? "onyx" : "hydrix") + " charges.");
					}
				});
			} else
				end();
		} else
			end();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
