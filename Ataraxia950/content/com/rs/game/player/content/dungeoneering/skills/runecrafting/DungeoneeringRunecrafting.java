package com.rs.game.player.content.dungeoneering.skills.runecrafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.utils.Utils;

public class DungeoneeringRunecrafting extends Action {

	private final DungeoneeringRunecraftingData data;
	private int amount;

	public DungeoneeringRunecrafting(DungeoneeringRunecraftingData data, int amount) {
		this.data = data;
		this.amount = amount;
	}

	public static final DungeoneeringRunecraftingData getData(int id) {
		for (DungeoneeringRunecraftingData values : DungeoneeringRunecraftingData.values()) {
			if (values.getRuneId() == id)
				return values;
		}
		return null;
	}

	@Override
	public boolean process(Player player) {
		if (amount <= 0)
			return false;
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < data.getRequiredLevel()) {
			player.sendMessage("You need at least level " + data.getRequiredLevel() + " Runecrafting to craft this.");
			return false;
		}
		if (data.ordinal() <= DungeoneeringRunecraftingData.ASTRAL_RUNE.ordinal() && player.getInventory().getAmountOf(17776) == 0) {
			player.sendMessage("You've ran out of rune essence.");
			return false;
		} else if (data.ordinal() > DungeoneeringRunecraftingData.ASTRAL_RUNE.ordinal()) {
			if (!player.getInventory().containsItem(data.getDoubleRunesLevel(), 1)) {
				player.sendMessage("You've ran out of " + ItemDefinitions.getItemDefinitions(data.getDoubleRunesLevel()).getName().toLowerCase() + "s.");
				return false;
			}
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		amount--;
		if (data.ordinal() <= DungeoneeringRunecraftingData.ASTRAL_RUNE.ordinal()) {
			player.setNextAnimation(new Animation(13659));
			player.setNextGraphics(new Graphics(2571));
			int amount = player.getInventory().getAmountOf(17776) > 10 ? 10 : player.getInventory().getAmountOf(17776);
			player.getInventory().deleteItem(17776, amount);
			int levelIncrement = player.getRingOfKinship().getTier(RingOfKinship.ARTISAN);
			int multiplier = 1 + (int) Math.floor((player.getSkills().getLevel(Skills.RUNECRAFTING) + levelIncrement) / data.getDoubleRunesLevel());
			if (data.getDoubleRunesLevel() == -1)
				multiplier = 1;
			player.getSkills().addXp(Skills.RUNECRAFTING, data.getExperience() * amount);
			player.getInventory().addItem(new Item(data.getRuneId(), amount * multiplier));
			return 1;
		}
		player.setNextAnimation(new Animation(13662));
		player.getInventory().deleteItem(data.getDoubleRunesLevel(), 1);
		player.getInventory().addItem(new Item(data.getRuneId(), 1));
		player.getSkills().addXp(Skills.RUNECRAFTING, data.getExperience());
		player.getSkills().addXp(Skills.MAGIC, data.getExperience());
		return 6;
	}

	@Override
	public boolean start(Player player) {
		if (data.ordinal() <= DungeoneeringRunecraftingData.ASTRAL_RUNE.ordinal()) {
			if (player.getInventory().getAmountOf(17776) == 0) {
				player.sendMessage("You need some rune essence to craft runes.");
				return false;
			}
		} else {
			if (!player.getInventory().containsItem(data.getDoubleRunesLevel(), 1)) {
				player.sendMessage("You need a " + ItemDefinitions.getItemDefinitions(data.getDoubleRunesLevel()).getName().toLowerCase() + " to imbue " + Utils.formatAorAn(new Item(data.getRuneId())) + " " + ItemDefinitions.getItemDefinitions(data.getRuneId()).getName().toLowerCase() + ".");
				return false;
			}
		}
        return process(player);
    }

	@Override
	public void stop(Player player) {
	}

}
