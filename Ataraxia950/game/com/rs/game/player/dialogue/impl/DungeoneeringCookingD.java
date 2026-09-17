package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeoneeringSkillsDialogue;
import com.rs.game.player.content.dungeoneering.skills.cooking.DungeoneeringCooking;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class DungeoneeringCookingD extends Dialogue {

	private int[] items;
	private int objectId;

	@SuppressWarnings("unchecked")
	@Override
	public void start() {
		ArrayList<Integer> fish = (ArrayList<Integer>) parameters[0];
		this.objectId = (int) parameters[1];
		Integer[] item = fish.toArray(new Integer[fish.size()]);
		items = Arrays.stream(item).mapToInt(Integer::intValue).toArray();
		DungeoneeringSkillsDialogue.sendSkillsDialogue(player, "Select the item you wish to cook.", items);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		int amount = DungeoneeringSkillsDialogue.getAmount(componentId);
		int option = DungeoneeringSkillsDialogue.getItemSlot(amount == 1 ? componentId : amount == 5 ? componentId + 1 : amount == 10 ? componentId + 2 : componentId + 3);
		if (option > items.length) {
			end();
			return;
		}
		if (amount == -1) {
			player.sendInputInteger("How many would you like to cook?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					player.getActionManager().setAction(new DungeoneeringCooking(DungeoneeringCooking.getData(items[option]), getInteger(), objectId));
				}
			});
			end();
			return;
		}
		player.getActionManager().setAction(new DungeoneeringCooking(DungeoneeringCooking.getData(items[option]), amount, objectId));
		end();
	}

	@Override
	public void finish() {}

}