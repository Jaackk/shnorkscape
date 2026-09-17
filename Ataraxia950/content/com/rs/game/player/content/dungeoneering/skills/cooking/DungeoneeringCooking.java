package com.rs.game.player.content.dungeoneering.skills.cooking;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Animation;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class DungeoneeringCooking extends Action {

	public static final int CAVE_POTATO = 18093, GISSEL_MUSHROOM = 17819, EDICAP_MUSHROOM = 17821;

	private final DungeoneeringCookingData data;
	private int amount, objectId;
	
	public static final DungeoneeringCookingData getData(int product) {
		for (DungeoneeringCookingData data : DungeoneeringCookingData.values()) {
			if (data.getId() == product)
				return data;
		}
		return null;
	}
	
	public static final boolean isCookable(Player player, int itemUsed, int objectId) {
		ArrayList<Integer> fish = new ArrayList<Integer>();
		for (DungeoneeringCookingData data : DungeoneeringCookingData.values()) {
			if (data.getIngredients()[0] == itemUsed || data.getIngredients().length > 2 && data.getIngredients()[3] == itemUsed)
				fish.add(data.getId());
		}
		if (fish.size() != 0) {
			player.getDialogueManager().startDialogue("DungeoneeringCookingD", fish, objectId);
			return true;
		}
		return false;
	}

	public DungeoneeringCooking(DungeoneeringCookingData data, int amount, int objectId) {
		this.data = data;
		this.amount = amount;
		this.objectId = objectId;
	}

	@Override
	public boolean process(Player player) {
		if (amount <= 0)
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("You need some more free inventory space to cook " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
			return false;
		}
		if (player.getSkills().getLevel(Skills.COOKING) < data.getLevel()) {
			player.sendMessage("You need at least level " + data.getLevel() + " Cooking to cook " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
			return false;
		}
		if (data.getIngredients().length < 3) {
			for (int i = 0; i < data.getIngredients().length; i++) {
				if (!player.getInventory().containsItem(data.getIngredients()[i], 1)) {
					player.sendMessage("You haven't got the ingredients to cook " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
					return false;
				}
			}
		} else {
			if (player.getInventory().containsItem(data.getIngredients()[0], 1) && player.getInventory().containsItem(data.getIngredients()[1], 1) && player.getInventory().containsItem(data.getIngredients()[2], 1) || player.getInventory().containsItem(data.getIngredients()[2], 1) && player.getInventory().containsItem(data.getIngredients()[3], 1))
				return true;
			else
				player.sendMessage("You haven't got the ingredients to cook " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		amount--;
		if (data.getIngredients().length < 3) {
			for (int i = 0; i < data.getIngredients().length; i++)
				player.getInventory().deleteItem(data.getIngredients()[i], 1);
		} else {
			if (player.getInventory().containsItem(data.getIngredients()[0], 1) && player.getInventory().containsItem(data.getIngredients()[1], 1) && player.getInventory().containsItem(data.getIngredients()[2], 1)) {
				for (int i = 0; i < 3; i++)
					player.getInventory().deleteItem(data.getIngredients()[i], 1);
			} else {
				player.getInventory().deleteItem(data.getIngredients()[2], 1);
				player.getInventory().deleteItem(data.getIngredients()[3], 1);
			}
		}
		int baseChance = 50;
		int burnLevel = (data.getLevel() + 20) - player.getSkills().getLevel(Skills.COOKING);
		if (burnLevel < 0)
			burnLevel = 0;
		
		int logBoost = ((objectId % 10) * objectId > 1000 ? 3 : 5);
		int random = Utils.random(100);
		player.setNextAnimation(new Animation(897));
		boolean success = burnLevel == 0 || random < baseChance - burnLevel + logBoost;
		if (success) {
			player.getSkills().addXp(Skills.COOKING, player.getPerkManager().hasPerkActive(DonationPerk.CONVINCING_COOK) ? data.getExperience() * 1.25 : data.getExperience());
			player.getInventory().addItem(data.getId(), 1);
			player.sendMessage("You successfully cook the " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
		} else {
			player.getInventory().addItem(data.getBurntId(), 1);
			player.sendMessage("You fail to cook the " + ItemDefinitions.getItemDefinitions(data.getId()).getName().toLowerCase() + ".");
		}
		return player.getPerkManager().hasPerkActive(DonationPerk.CONVINCING_COOK) ? 2 : 3;
	}

	@Override
	public boolean start(Player player) {
		if (objectId > 49950) {
			for (int i = objectId; i > objectId - 15; i--) {
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(i);
				if (defs.name.equals("Cooking range (empty)")) {
					objectId = objectId - defs.id - 1;
					break;
				}
			}
		}
		return process(player);
	}

	@Override
	public void stop(Player player) {
	}

}
