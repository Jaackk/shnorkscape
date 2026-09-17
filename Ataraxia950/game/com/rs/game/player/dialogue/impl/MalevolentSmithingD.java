package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.MalevolentSmithing;
import com.rs.game.player.actions.smithing.MalevolentSmithing.Malevolent;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Sirenic Scale crafting skills dialogue.
 *
 * @author Noel
 */
public class MalevolentSmithingD extends Dialogue {

	private WorldObject object;

	@Override
	public void start() {
		object = (WorldObject) parameters[0];
		int count = 0;
		int[] ids = new int[Malevolent.values().length];
		for (Malevolent m : Malevolent.values())
			ids[count++] = m.getProduceEnergy().getId();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Which armour piece would you like to create?", 1, ids, new ItemNameFilter() {
			private int count = 0;

			@Override
			public String rename(String name) {
				Malevolent piece = Malevolent.values()[count++];
				if (player.getSkills().getLevel(Skills.SMITHING) < piece.getLevelRequired())
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + piece.getLevelRequired();
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int idx = SkillsDialogue.getItemSlot(componentId);
		if (idx > Malevolent.values().length || idx < 0) {
			end();
			return;
		}
		player.getActionManager().setAction(new MalevolentSmithing(object, Malevolent.values()[idx]));
		end();
	}

	@Override
	public void finish() {
	}
}