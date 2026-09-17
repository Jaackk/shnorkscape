package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.crafting.RefinedAnimaCoreCreation;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;

public class RefinedAnimaCoreCreationD extends Dialogue {
	
	private static final int[] ITEMS = new int[] {
			37034, 37037, 37040, 37043, 37046, 37049, 
			37052, 37055, 37058, 37061, 37064, 37067
	};
	
	private final List<Integer> items = new ArrayList<Integer>();
	
	@Override
	public void start() {
		for (int item : ITEMS)
			if (player.getInventory().containsItem(item, 1))
				items.add(item);
		if (items.size() == 0) {
			player.sendMessage("You have no armour to craft with the essence.");
			return;
		}
		int[] ids = new int[items.size() > 10 ? 10 : items.size()];
		for (int i = 0; i < ids.length; i++)
			ids[i] = items.get(i) + 2;
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Which piece would you like to make?", 1, ids, null);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot > items.size() || slot < 0) {
			end();
			return;
		}
		player.getActionManager().setAction(new RefinedAnimaCoreCreation(items.get(slot) + 2));
		end();
	}

	@Override
	public void finish() {
		
	}

}
