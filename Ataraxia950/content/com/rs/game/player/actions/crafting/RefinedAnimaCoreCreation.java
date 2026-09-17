package com.rs.game.player.actions.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

public class RefinedAnimaCoreCreation extends Action {
	
	private final int id;
	
	public RefinedAnimaCoreCreation(final int id) {
		this.id = id - 2;
	}
	
	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		for (int i = 37030; i <= 37033; i++)
			player.getInventory().deleteItem(i, 1);
		player.getInventory().deleteItem(id, 1);
		Item creation = new Item(id + 2, 1);
		creation = player.getChargesManagerNew().createDegradeableItem(creation, 37030, 37031, 37032, 37033);
		player.getInventory().addItem(creation);
		player.sendMessage("You successfully combine the essence with the " + ItemDefinitions.getItemDefinitions(id).getName() + " and craft a " + ItemDefinitions.getItemDefinitions(id + 2).getName() + ".");
		player.getSkills().addXp(Skills.CRAFTING, 500);
		return -1;
	}

	@Override
	public boolean start(Player player) {
		if (player.getSkills().getLevel(Skills.CRAFTING) < 80) {
			player.sendMessage("You need a Crafting level of at least 80 to craft a " + ItemDefinitions.getItemDefinitions(id).getName() + ".");
			return false;
		}
		StringBuilder required = new StringBuilder(" ");
		for (int i = 37030; i <= 37033; i ++)
			if (!player.getInventory().containsItem(i, 1))
				required.append(ItemDefinitions.getItemDefinitions(i).getName()).append(i == 37027 ? " " : ", ");
		if (required.length() > 1) {
			player.sendMessage("You need a" + required.substring(0, required.length() - 2) + " to craft a " + ItemDefinitions.getItemDefinitions(id).getName() + ".");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {}

	
	
}
