package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.DivinationTransmute;
import com.rs.game.player.actions.divination.DivinationTransmute.Transmutations;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.dialogue.Dialogue;

import java.util.Arrays;
import java.util.List;

public class DivinationTransmuteD extends Dialogue {
	private List<Transmutations> availableTransmutations;

	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		availableTransmutations = (List<Transmutations>) parameters[0];
		int[] productIds = availableTransmutations.stream().mapToInt(Transmutations::getProductId).toArray();
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would like to weave?<br>Choose a number, then click the item to begin.", 28, productIds, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				Transmutations transmutation = availableTransmutations.get(count++);
				if (player.getSkills().getLevel(Skills.DIVINATION) < transmutation.getLevelToMake())
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + transmutation.getLevelToMake();
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int slot = SkillsDialogue.getItemSlot(componentId);
		if (slot >= availableTransmutations.size() || availableTransmutations.get(slot) == null || slot < 0) {
			end();
			return;
		}
		Transmutations transmutation = availableTransmutations.get(slot);
		boolean isDivineLocation = Arrays.asList(DivinationTransmute.DIVINE_LOCATIONS).contains(transmutation);
		player.getActionManager().setAction(new DivinationTransmute(transmutation, isDivineLocation ? 1 : SkillsDialogue.getQuantity(player), isDivineLocation));
		end();
	}

	@Override
	public void finish() {
	}
}
