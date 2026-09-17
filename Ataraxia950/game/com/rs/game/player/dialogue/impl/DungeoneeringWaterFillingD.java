package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.actions.WaterFilling;
import com.rs.game.player.actions.WaterFilling.Fill;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class DungeoneeringWaterFillingD extends Dialogue {

	private Fill fill;

	@Override
	public void start() {
		this.fill = (Fill) parameters[0];
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select how many vials you wish to fill.", new int[] { fill.getEmpty() });
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > 1) {
			end();
			return;
		}
		if (amount == -1) {
			player.sendInputInteger("How many would you like to fill?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new WaterFilling(fill, getInteger()));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new WaterFilling(fill, amount));
		end();
	}

	@Override
	public void finish() {
	}
}