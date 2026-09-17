package com.rs.game.player.content.dungeoneering.skills.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.utils.Utils;

public class DungeoneeringCrafting extends Action {

	private static final int[] CLOTH_AMOUNTS = new int[] { 1, 1, 2, 3, 3, 1, 5 };
	private static final int[] LEATHER_AMOUNTS = new int[] { 1, 1, 2, 3, 3, 5 };
	
	public static final boolean isCraftable(Player player, Item item) {
		for (DungeoneeringCraftingData data : DungeoneeringCraftingData.values()) {
			if (data.getCloth() == item.getId()) {
				player.getDialogueManager().startDialogue("DungeoneeringCraftingD", data);
				return true;
			}
		}
		return false;
	}

	public static final boolean isCraftable(Player player, Item first, Item second) {
		for (DungeoneeringCraftingData data : DungeoneeringCraftingData.values()) {
			if (data.getCloth() == first.getId() || data.getCloth() == second.getId()) {
				Item other = data.getCloth() == first.getId() ? second : first;
				if (other.getId() == 17446) {
					player.getDialogueManager().startDialogue("DungeoneeringCraftingD", data);
					return true;
				}
			}
		}
		return false;
	}
	
	public DungeoneeringCrafting(DungeoneeringCraftingData data, int amount, int option) {
		this.data = data;
		this.amount = amount;
		this.option = option;
	}
	
	private final DungeoneeringCraftingData data;
	private int amount;
    private final int option;
    private int animation;
    private int clothsAmount;
	
	@Override
	public boolean process(Player player) {
		if (amount <= 0)
			return false;
		if (!player.getInventory().containsItem(17447, 1)) {
			player.sendMessage("You've ran out of thread.");
			return false;
		}
		if (player.getSkills().getLevel(Skills.CRAFTING) < data.getLevels()[option]) {
			player.sendMessage("You need at least level " + data.getLevels()[option] + " Crafting to craft this.");
			return false;
		}
		if (!player.getInventory().containsItem(data.getCloth(), clothsAmount)) {
			player.sendMessage("You need at least " + clothsAmount + " " + ItemDefinitions.getItemDefinitions(data.getCloth()).getName().toLowerCase() + (clothsAmount > 1 ? "s" : "") + " to craft this.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		amount--;
		player.getSkills().addXp(Skills.CRAFTING, data.getExperience()[option]);
		if (Utils.random(100) < player.getRingOfKinship().getBoost(RingOfKinship.ARTISAN))
			clothsAmount--;
		player.getInventory().deleteItem(data.getCloth(), clothsAmount);
		player.getInventory().deleteItem(17447, 1);
		player.getInventory().addItem(new Item(data.getProducts()[option], 1));
		player.setNextAnimation(new Animation(animation));
		return 2;
	}

	@Override
	public boolean start(Player player) {
		clothsAmount = data.getProducts().length == 7 ? CLOTH_AMOUNTS[option] : LEATHER_AMOUNTS[option];
		if (!player.getInventory().containsItem(data.getCloth(), clothsAmount)) {
			player.sendMessage("You need at least " + clothsAmount + " " + ItemDefinitions.getItemDefinitions(data.getCloth()).getName().toLowerCase() + (clothsAmount > 1 ? "s" : "") + " to craft this.");
			return false;
		}
		if (!player.getInventory().containsItem(17447, 1)) {
			player.sendMessage("You need some thread to craft this.");
			return false;
		}
		animation = data.ordinal() > DungeoneeringCraftingData.SPIRITBLOOM.ordinal() ? 25594 : 13245 + data.ordinal();
		return true;
	}

	@Override
	public void stop(Player player) {}

}
