package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.crafting.AnimaCoreCreation;
import com.rs.game.player.actions.crafting.AnimaCoreCreation.AnimaCoreData;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Tom
 * @date May 4, 2017
 */

public class AnimaCoreCreationD extends Dialogue {

	private int[] ids;

	@Override
	public void start() {
		int count = 0;
		int count2 = 0;
		boolean hasAllItems = false;

		for (AnimaCoreData anima : AnimaCoreData.values()) {
			hasAllItems = true;
			for (int i = 0; i < anima.getMaterial().length; i++)
				if (!player.getInventory().containsItem(anima.getMaterial()[i], 1))
					hasAllItems = false;
			if (hasAllItems)
				count++;
		}
		ids = new int[count];
		for (AnimaCoreData anima : AnimaCoreData.values()) {
			hasAllItems = true;
			for (int i = 0; i < anima.getMaterial().length; i++)
				if (!player.getInventory().containsItem(anima.getMaterial()[i], 1))
					hasAllItems = false;
			if (hasAllItems) {
				ids[count2++] = anima.getProduct();
			}
		}

		if (count != 0) {
			SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Which piece would you like to make?", 1, ids, new ItemNameFilter() {
				int count = 0;

				@Override
				public String rename(String name) {
					@SuppressWarnings("unused")
					AnimaCoreData anima = AnimaCoreData.values()[count++];
					return name;
				}
			});
		} else
			player.sendMessage("You don't have all the necessary materials to make this armour piece.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot > ids.length || slot < 0) {
			end();
			return;
		}
		AnimaCoreData anima = AnimaCoreData.getProduct(ids[slot]);
		if (anima == null) {
			end();
			return;
		}
		player.getActionManager().setAction(new AnimaCoreCreation(anima));
		end();
	}

	@Override
	public void finish() {
	}

}
