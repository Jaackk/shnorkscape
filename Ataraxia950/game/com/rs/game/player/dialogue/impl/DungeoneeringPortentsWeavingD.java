package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.divination.DungeoneeringDivinationData;
import com.rs.game.player.content.dungeoneering.skills.divination.DungeoneeringWeaving;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringPortentsWeavingD extends Dialogue {

	private int[] items;
	private int energy;

	@Override
	public void start() {
		energy = (int) parameters[0];
		items = new int[2];
		int x = 0;
		for (DungeoneeringDivinationData data : DungeoneeringDivinationData.values()) {
			if (data.getEnergies().getId() == energy)
				items[x++] = data.getItemId();
		}
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the portent you wish to weave.", items);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > 2) {
			end();
			return;
		}
		DungeoneeringDivinationData data = null;
		for (DungeoneeringDivinationData d : DungeoneeringDivinationData.values()) {
				if (d.getItemId() == items[option]) {
					data = d;
					break;
				}	
		}
		final DungeoneeringDivinationData d = data;
		if (amount == -1) {
			player.sendInputInteger("How many would you like to create?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringWeaving(d, getInteger()));
				}
			});
			end();
			return;
		}
		if (data != null)
			player.getActionManager().setAction(new DungeoneeringWeaving(data, amount));
		end();
	}

	@Override
	public void finish() {}

}