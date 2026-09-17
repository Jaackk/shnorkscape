package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.fletching.Fletching;
import com.rs.game.player.actions.fletching.defs.Fletchables;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

/**
 * FletchingD.java | 11:46:21 AM
 * 
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class FletchingD extends Dialogue {

	/*
	 * Represents all the fletchable items.
	 */
	private Fletchables items;
	private boolean portable;

	@Override
	public void start() {
		items = (Fletchables) parameters[0];
		portable = (boolean) parameters[1];
		boolean maxQuantityTen = true;
		SkillsDialogue.sendSkillsDialogue(player, maxQuantityTen ? SkillsDialogue.MAKE_NO_ALL_NO_CUSTOM : SkillsDialogue.MAKE, "Choose how many you wish to make,<br>then click on the item to begin.", maxQuantityTen ? 28 : 28, items.getProduct(), maxQuantityTen ? null : new ItemNameFilter() {
			@Override
			public String rename(String name) {
				return name.replace(" (u)", "");
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int option = SkillsDialogue.getItemSlot(componentId);
		if (option > items.getProduct().length) {
			end();
			return;
		}
		int quantity = SkillsDialogue.getQuantity(player);
		int invQuantity = player.getInventory().getItems().getNumberOf(items.getId());
		if (quantity > invQuantity)
			quantity = invQuantity;
		player.getActionManager().setAction(new Fletching(items, option, quantity, portable));
		end();
	}

	@Override
	public void finish() {
	}

}