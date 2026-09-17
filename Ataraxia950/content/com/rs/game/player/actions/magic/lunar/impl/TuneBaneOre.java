package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.player.content.Magic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class TuneBaneOre implements ItemSpell {

	private static final int[][] ITEMS = new int[][] {
//		{ 21782, 7979, 7986, 8264 },
//		{ 21781, 7977, 7984, 8262 },
		{ 21779, 534, 536, 243, 1753, 1751, 1749, 1747, 24372, 7980, 7987, 8265 },
//		{ 21780, 6163, 6167, 6165 }
	};
	
	@Override
	public int getId() {
		return 14861;
	}

	@Override
	public int getLevel() {
		return 87;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 4) };
	}

	@Override
	public int getDelay() {
		return 2000;
	}

	@Override
	public boolean spellEffect(Player player, Item item) {
		int ore = 0;
		loop : for (int[] items : ITEMS) {
			for (int i = 1; i < items.length; i++) {
				if (item.getId() == items[i]) {
					ore = items[0];
					break loop;
				}
			}
		}
		if (ore == 0) {
			player.sendMessage("You can only cast this spell on bane-related items.");
			return false;
		}
		if (!player.getInventory().containsItem(21778, 1)) {
			player.sendMessage("You need at least one bane ore to cast this spell.");
			return false;
		}
		final int oreId = ore;
		int amount = player.getInventory().getAmountOf(21778);
		for (Item i : getRunes()) {
			if (amount > player.getInventory().getAmountOf(i.getId()) * i.getAmount() && !Magic.hasInfiniteRunes(i.getId(),player.getEquipment().getWeaponId(), player.getEquipment().getShieldId()))
				amount = (player.getInventory().getAmountOf(i.getId()) * i.getAmount());
		}
		final int finalAmount = amount;
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(new Animation(11706));
				} else if (ticks == 1) {
					player.getSkills().addXp(Skills.MAGIC, 90 * finalAmount);
					for (Item i : getRunes())
						player.getInventory().deleteItem(i.getId(), i.getAmount() * (finalAmount - 1));
					player.sm(""+finalAmount);
					player.getInventory().deleteItem(21778, finalAmount);
					player.getInventory().addItem(oreId, finalAmount);
					player.sendMessage("You tune " + finalAmount + " bane ores into " + ItemDefinitions.getItemDefinitions(oreId).getName().toLowerCase() + ".");
					stop();
				}
				ticks++;
			}
		}, 0, 1);
		return true;
	}

}
