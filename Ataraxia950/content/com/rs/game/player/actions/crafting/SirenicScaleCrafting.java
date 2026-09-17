package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Sirenic Scale crafting into Sirenic armour.
 *
 * @author Noel.
 */
public class SirenicScaleCrafting extends Action {

	/**
	 * Sirenic Armour. Will rename everything once more customs like this gets
	 * added and tables will be useful.
	 */
	public static final Sirenic[] ARMOUR = new Sirenic[] { Sirenic.SIRENIC_MASK, Sirenic.SIRENIC_HAUBERK,
			Sirenic.SIRENIC_CHAPS };
	public Sirenic scale;
	public int ticks;

	public SirenicScaleCrafting(Sirenic scale, int ticks) {
		this.scale = scale;
		this.ticks = ticks;
	}

	@Override
	public boolean start(Player player) {
        return process(player);
    }

	@Override
	public boolean process(Player player) {
		if (scale == null || player == null)
			return false;
		if (ticks <= 0)
			return false;
		if (player.getSkills().getLevel(Skills.CRAFTING) < scale.getLevelRequired()) {
			player.sendMessage("You need a Crafting level of at least " + scale.getLevelRequired() + " to create a "
					+ scale.getProduceEnergy().getDefinitions().getName());
			return false;
		}
		int amount = scale.getItemsRequired()[0].getAmount();
		if (!player.getInventory().containsItem(scale.getItemsRequired()[0].getId(), amount)) {
			player.sendMessage("You need " + (amount > 1 ? "x" + amount + " of" : "a") + " "
					+ scale.getItemsRequired()[0].getDefinitions().getName() + " to create "
					+ scale.getProduceEnergy().getDefinitions().getName() + ".");
			return false;
		}
		if (scale.getItemsRequired().length > 0) {
			amount = scale.getItemsRequired()[1].getAmount();
			if (!player.getInventory().containsItem(scale.getItemsRequired()[1].getId(), amount)) {
				player.sendMessage("You need " + (amount > 1 ? "x" + amount + " of" : "a") + " "
						+ scale.getItemsRequired()[1].getDefinitions().getName() + " to create "
						+ scale.getProduceEnergy().getDefinitions().getName() + ".");
				return false;
			}
		}
		if (!player.getInventory().containsOneItem(1733)) {
			player.sendMessage("You need a needle to craft Sirenic scale.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		double xp = scale.getExperience();
		for (Item required : scale.getItemsRequired())
			player.getInventory().deleteItem(required.getId(), required.getAmount());
		int amount = scale.getProduceEnergy().getAmount();
		player.getInventory().addItem(scale.getProduceEnergy().getId(), amount);
		player.getSkills().addXp(Skills.CRAFTING, xp);
		player.addItemsMade();
		player.sendMessage(
				"You make a " + scale.getProduceEnergy().getDefinitions().getName().toLowerCase() + "; "
						+ "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.",
				true);
		player.setNextAnimation(new Animation(1249));
		if (ticks > 0)
			return 1;
		return -1;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

	public enum Sirenic {
		// Sirenic scale //Algarum thread //Armour piece
		SIRENIC_MASK(91, 500, new Item[] { new Item(29863, 14), new Item(29864, 1) }, new Item(29854)), SIRENIC_HAUBERK(
				93, 1500, new Item[] { new Item(29863, 42), new Item(29864, 3) }, new Item(29857)), SIRENIC_CHAPS(92,
						1000, new Item[] { new Item(29863, 28), new Item(29864, 2) }, new Item(29860));

		private final int levelRequired;
		private final double experience;
		private final Item[] itemsRequired;
		private final Item energyProduce;

		Sirenic(int levelRequired, double experience, Item[] itemsRequired, Item energyProduce) {
			this.levelRequired = levelRequired;
			this.experience = experience;
			this.itemsRequired = itemsRequired;
			this.energyProduce = energyProduce;
		}

		public Item[] getItemsRequired() {
			return itemsRequired;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public Item getProduceEnergy() {
			return energyProduce;
		}

		public double getExperience() {
			return experience;
		}
	}
}