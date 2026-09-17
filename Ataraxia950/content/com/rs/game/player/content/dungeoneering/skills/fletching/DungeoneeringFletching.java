package com.rs.game.player.content.dungeoneering.skills.fletching;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class DungeoneeringFletching extends Action {

	public static final boolean isFletchable(Player player, Item item) {
		for (DungeoneeringFletchingData data : DungeoneeringFletchingData.values()) {
			if (data.getLogsId() == item.getId()) {
				player.getDialogueManager().startDialogue("DungeoneeringFletchingD", data);
				return true;
			}
		}
		return false;
	}

	public static final boolean isFletchable(Player player, Item first, Item second) {
		for (DungeoneeringFletchingData data : DungeoneeringFletchingData.values()) {
			if (data.getLogsId() == first.getId() || data.getLogsId() == second.getId()) {
				Item other = data.getLogsId() == first.getId() ? second : first;
				if (data == DungeoneeringFletchingData.HEADLESS_ARROW && other.getId() == 17796 || data.ordinal() >= 11 && data.ordinal() <= 20 && other.getId() == 17747 || data.ordinal() > 20 && other.getId() == 17752 || data.ordinal() < 11 && other.getId() == 17754) {
					player.getDialogueManager().startDialogue("DungeoneeringFletchingD", data);
					return true;
				}
			}
		}
		return false;
	}

	public DungeoneeringFletching(DungeoneeringFletchingData items, int option, int quantity) {
		this.data = items;
		this.option = option;
		this.quantity = quantity;
	}

	private final DungeoneeringFletchingData data;
	private final int option;
	private int quantity;

	@Override
	public boolean process(Player player) {
		if (quantity <= 0)
			return false;
		if (!player.getInventory().containsItem(data.getLogsId(), 1))
			return false;
		if (data == DungeoneeringFletchingData.HEADLESS_ARROW) {
			if (!player.getInventory().containsItem(17796, 1))
				return false;
		} else if (data.ordinal() >= 11 && data.ordinal() <= 20) {
			if (!player.getInventory().containsItem(17747, 1))
				return false;
		} else if (data.ordinal() > 21) {
			if (!player.getInventory().containsItem(17752, 1))
				return false;
		}
		if (!player.getInventory().hasFreeSlots() && data.ordinal() < 21) {
			if (data.ordinal() <= 20 && option == 0 && player.getInventory().containsItem(17742, 1))
				return true;
			player.sendMessage("You don't have enough free inventory space to Fletch this.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		quantity--;
		player.addItemsFletched();
		String name = ItemDefinitions.getItemDefinitions(data.getProducts()[option]).getName().replace("(u)", "");
		player.sendMessage("You successfully fletch " + (name.contains("arrows") || name.contains("shaft") || name.contains("Headless") ? "some " : Utils.getAorAn(name)) + name + (name.contains("Headless") ? "s" : "") + "; " + "items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>.", true);
		if (data.getProducts().length > 1) {
			player.getInventory().deleteItem(data.getLogsId(), 1);
			int amount = (int) (option == 0 ? (data.getExperience()[option] / 0.3) : 1);
			player.getInventory().addItem(new Item(data.getProducts()[option], amount));
			player.getSkills().addXp(Skills.FLETCHING, data.getExperience()[option]);
		} else {
			boolean isStackable = (data == DungeoneeringFletchingData.HEADLESS_ARROW || data.ordinal() < 21);
			int amount = (isStackable ? 15 : 1);
			Item secondaryItem = data == DungeoneeringFletchingData.HEADLESS_ARROW ? new Item(17796, amount) : data.ordinal() < 21 ? new Item(17747, amount) : new Item(17752, 1);
			if (player.getInventory().getAmountOf(secondaryItem.getId()) < amount)
				amount = player.getInventory().getAmountOf(secondaryItem.getId());
			if (player.getInventory().getAmountOf(data.getLogsId()) < amount)
				amount = player.getInventory().getAmountOf(data.getLogsId());
			player.getInventory().deleteItem(data.getLogsId(), amount);
			player.getInventory().deleteItem(new Item(secondaryItem.getId(), amount));
			player.getInventory().addItem(new Item(data.getProducts()[option], amount));
			if (isStackable && amount < 15)
				player.getSkills().addXp(Skills.FLETCHING, (data.getExperience()[option] / 15) * amount);
			else
				player.getSkills().addXp(Skills.FLETCHING, data.getExperience()[option]);
		}
		
		return data.ordinal() >= 10 && data.ordinal() <= 20 ? 1 : 3;
	}

	@Override
	public boolean start(Player player) {
		if (player.getSkills().getLevelForXp(Skills.FLETCHING) < data.getLevels()[option]) {
			player.sendMessage("You need at least level " + data.getLevels()[option] + " Fletching to fletch this.");
			return false;
		}
		if (!(data.ordinal() >= 10 && data.ordinal() <= 20) && !player.getInventory().containsItem(17754, 1) && !player.getDungeoneeringToolbelt().containsTool(17754)) {
			player.sendMessage("You need a knife to Fletch this.");
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {
	}

}
