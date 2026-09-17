package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

public class SilverhawkBootsChanging extends Dialogue {

	private static final String[] TIERS = new String[] {
			"Tier 1", "Tier 20", "Tier 40", "Tier 50", "Tier 60"
	};
	
	private Item item;
	private int[] intTiers;
	private String[] tiers;
	
	@Override
	public void start() {
		item = (Item) parameters[0];
		tiers = new String[4];
		intTiers = new int[4];
		int x = 0, y = 0;
		for (String tier : TIERS) {
			if (item.getId() - 30920 != x) {
				tiers[y] = tier;
				intTiers[y] = 30920 + x;
				y++;
			}
			x++;
		}
		sendOptionsDialogue("Select the level tier to which you wish to change the boots.", tiers);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (player.getInventory().containsItem(item)) {
			switch (componentId) {
			case OPTION_1:
				item.setId(intTiers[0]);
				player.sendMessage("You change the Silverhawk boots to " + tiers[3].toLowerCase() + ".");
				break;
			case OPTION_2:
				item.setId(intTiers[1]);
				player.sendMessage("You change the Silverhawk boots to " + tiers[2].toLowerCase() + ".");
				break;
			case OPTION_3:
				item.setId(intTiers[2]);
				player.sendMessage("You change the Silverhawk boots to " + tiers[1].toLowerCase() + ".");
				break;
			case OPTION_4:
				item.setId(intTiers[3]);
				player.sendMessage("You change the Silverhawk boots to " + tiers[0].toLowerCase() + ".");
				break;
			}
			player.getInventory().refresh();
		}
		end();
	}

	@Override
	public void finish() {
		
	}

}
