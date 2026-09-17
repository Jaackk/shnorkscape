package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.herblore.CombinePotions;
import com.rs.game.player.actions.herblore.CrystalFlask.CrystalPot;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

public class CrystalFlaskD extends Dialogue {

	private int[] ids;

	@Override
	public void start() {
		int count = 0;
		int count2 = 0;
		boolean hasAllItems = false;

		for (CrystalPot cpotion : CrystalPot.values()) {
			hasAllItems = true;
			for (int i = 0; i < cpotion.getRequiredPotion().length; i++) {
				if (!player.getInventory().containsItem(cpotion.getRequiredPotion()[i]))
					hasAllItems = false;
			}

			if (hasAllItems) {
				count++;
			}
		}

		ids = new int[count];

		for (CrystalPot cpotion : CrystalPot.values()) {
			hasAllItems = true;
			for (int i = 0; i < cpotion.getRequiredPotion().length; i++) {
				if (!player.getInventory().containsItem(cpotion.getRequiredPotion()[i]))
					hasAllItems = false;
			}

			if (hasAllItems) {
				ids[count2++] = cpotion.getProducedPotion();
			}
		}

		if (count != 0) {
			SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Which crystal flask would you like to make?", 1, ids, new ItemNameFilter() {
				int count = 0;

				@Override
				public String rename(String name) {
					@SuppressWarnings("unused")
					CrystalPot cpotion = CrystalPot.values()[count++];
					return name;

				}
			});

		} else {
			player.sendMessage("You don't have all the necessary potions to make a crystal flask potion.");
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot > ids.length || slot < 0) {
			end();
			return;
		}
		player.getActionManager().setAction(new CombinePotions(CrystalPot.getCrystalPotion(ids[slot])));
	}

	@Override
	public void finish() {

	}

}
