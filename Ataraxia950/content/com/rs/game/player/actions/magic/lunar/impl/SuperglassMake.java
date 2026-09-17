package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class SuperglassMake implements DefaultSpell {

	private static final int BUCKET_OF_SAND = 1783;
	private static final int MOLTEN_GLASS = 1775;
	private static final int BUCKET = 1925;
	
	private static final int[][] INGREDIENTS = {
			{ 1781 },
			{ 401 }, 
			{ 10978 }
	};
	
	@Override
	public int getId() {
		return 14845;
	}

	@Override
	public int getLevel() {
		return 77;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(FIRE_RUNE, 6), new Item(AIR_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 4500;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (!player.getInventory().containsItem(BUCKET_OF_SAND, 1)) {
			player.getPackets().sendGameMessage("You do not have all of the required ingredients.");
			return false;
		}
		int tryAmount = 0;
		for (int i = 0; i < INGREDIENTS.length; i++) {
			if (!player.getInventory().containsItem(INGREDIENTS[i][0], 1)) {
				tryAmount++;
				continue;
			}
		}
		if (tryAmount >= INGREDIENTS.length) {
			player.getPackets().sendGameMessage("You do not have all of the required ingredients.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextGraphics(new Graphics(729, 0, 100));
					player.setNextAnimation(new Animation(4413));
				} else if (loop == 1) {
					for (int i = 0; i < INGREDIENTS.length; i++) {
						if (!player.getInventory().containsItem(INGREDIENTS[i][0], 1)) {
							continue;
						}
						while (player.getInventory().containsItem(INGREDIENTS[i][0], 1)
								&& player.getInventory().containsItem(BUCKET_OF_SAND, 1)) {
							int maxAmount = player.getInventory().getFreeSlots() / 2;
							int amount = player.getInventory().getNumberOf(INGREDIENTS[i][0]);
							int amount2 = player.getInventory().getNumberOf(BUCKET_OF_SAND);
							int amountToUse;
							if (amount > amount2)
								amountToUse = amount2;
							else if (amount2 > amount)
								amountToUse = amount;
							else
								amountToUse = amount;
							if (amountToUse > maxAmount)
								amountToUse = maxAmount;
							player.getSkills().addXp(Skills.CRAFTING, amountToUse * 10);
							player.getInventory().deleteItem(BUCKET_OF_SAND, amountToUse);
							player.getInventory().deleteItem(INGREDIENTS[i][0], amountToUse);
							player.getInventory().addItem(MOLTEN_GLASS, amountToUse);
							player.getInventory().addItem(BUCKET, amountToUse);
							if (!player.getInventory().containsItem(INGREDIENTS[i][0], 1)
									|| !player.getInventory().containsItem(BUCKET_OF_SAND, 1))
								break;
						}
					}
					player.getSkills().addXp(Skills.MAGIC, 78);
					player.getPackets().sendGameMessage("The ingredients combine in your inventory, producing molten glass.", true);
					stop();
				}
				loop++;
			}
		}, 0, 6);
		return true;
	}

}
