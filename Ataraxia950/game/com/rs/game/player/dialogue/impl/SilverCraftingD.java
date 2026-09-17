package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.crafting.SilverCrafting;
import com.rs.game.player.actions.crafting.SilverCrafting.SmeltingBar;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Silver product Crafting dialogue.
 *
 * @author Noel
 */
public class SilverCraftingD extends Dialogue {

	private WorldObject object;

	@Override
	public void start() {
		object = (WorldObject) parameters[0];
		int[] ids = new int[SmeltingBar.values().length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = SmeltingBar.values()[i].getProducedBar().getId();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many items would you like to make?<br>Choose a number, then click the item to begin.", 28, ids, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				SmeltingBar bar = SmeltingBar.values()[count++];
				if (player.getSkills().getLevel(Skills.CRAFTING) < bar.getLevelRequired())
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + bar.getLevelRequired();
				return name;

			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (SmeltingBar.forId(slot) == null || slot < 0) {
			end();
			return;
		}
		player.getActionManager().setAction(new SilverCrafting(slot, object, SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {
	}
}
