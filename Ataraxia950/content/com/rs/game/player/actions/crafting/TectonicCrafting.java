package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles Tectonic Scale crafting into Tectonic.
 *
 * @author Zero.
 */
public class TectonicCrafting extends Action {

	/**
	 * Sirenic Armour. Will rename everything once more customs like this gets
	 * added and tables will be useful.
	 */
	public static final Tectonic[] ARMOUR = new Tectonic[] { Tectonic.TECTONIC_MASK, Tectonic.TECTONIC_ROBE_TOP,
			Tectonic.TECTONIC_ROBE_BOTTOM };
	public Tectonic energy;
	public int ticks;

	public TectonicCrafting(Tectonic energy, int ticks) {
		this.energy = energy;
		this.ticks = ticks;
	}

	@Override
	public boolean start(Player player) {
        return process(player);
    }

	@Override
	public boolean process(Player player) {
		if (energy == null || player == null)
			return false;
		if (ticks <= 0)
			return false;
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < energy.getLevelRequired()) {
			player.sendMessage("You need a Runecrafting level of at least " + energy.getLevelRequired()
					+ " to create a " + energy.getProduceEnergy().getDefinitions().getName());
			return false;
		}
		int amount = energy.getItemsRequired()[0].getAmount();
		if (!player.getInventory().containsItem(energy.getItemsRequired()[0].getId(), amount)) {
			player.sendMessage("You need " + (amount > 1 ? "x" + amount + " of" : "a") + " "
					+ energy.getItemsRequired()[0].getDefinitions().getName() + " to create "
					+ energy.getProduceEnergy().getDefinitions().getName() + ".");
			return false;
		}
		if (energy.getItemsRequired().length > 0) {
			amount = energy.getItemsRequired()[1].getAmount();
			if (!player.getInventory().containsItem(energy.getItemsRequired()[1].getId(), amount)) {
				player.sendMessage("You need " + (amount > 1 ? "x" + amount + " of" : "a") + " "
						+ energy.getItemsRequired()[1].getDefinitions().getName() + " to create "
						+ energy.getProduceEnergy().getDefinitions().getName() + ".");
				return false;
			}
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		double xp = energy.getExperience();
		for (Item required : energy.getItemsRequired())
			player.getInventory().deleteItem(required.getId(), required.getAmount());
		int amount = energy.getProduceEnergy().getAmount();
		player.getInventory().addItem(energy.getProduceEnergy().getId(), amount);
		player.getSkills().addXp(Skills.CRAFTING, xp);
		player.addItemsMade();
		player.sendMessage(
				"You make a " + energy.getProduceEnergy().getDefinitions().getName().toLowerCase() + "; "
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

	public enum Tectonic {
		// Tectonic Energy //Stone of Binding //Armour piece
		TECTONIC_MASK(91, 500, new Item[] { new Item(28627, 14), new Item(28628, 1) },
				new Item(28608)), TECTONIC_ROBE_TOP(93, 1500, new Item[] { new Item(28627, 42), new Item(28628, 3) },
						new Item(28611)), TECTONIC_ROBE_BOTTOM(92, 1000,
								new Item[] { new Item(28627, 28), new Item(28628, 2) }, new Item(28614));

		private final int levelRequired;
		private final double experience;
		private final Item[] itemsRequired;
		private final Item energyProduce;

		Tectonic(int levelRequired, double experience, Item[] itemsRequired, Item energyProduce) {
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