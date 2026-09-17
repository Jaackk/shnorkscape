package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class PlankMake implements ItemSpell {
	
	private static final int[][] LOGS_AND_PLANKS = { {1511, 960}, {1521, 8778}, {6333, 8780}, {6332, 8782} };
	private static final int[] COIN_COSTS = { 70, 175, 350, 1050 };
		
	@Override
	public int getId() {
		return 14859;
	}

	@Override
	public int getLevel() {
		return 86;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 15), new Item(NATURE_RUNE, 1) };
	}

	@Override
	public int getDelay() {
		return 2000;
	}

	@Override
	public boolean spellEffect(Player player, Item item) {
		boolean isLog = false;
		for (int[] logs : LOGS_AND_PLANKS) {
			if (item.getId() == logs[0]) {
				isLog = true;
				break;
			}
		}
		if (!isLog) {
			player.sendMessage("You can only transform logs into planks.");
			return false;
		}
		int tries = 0;
		for (int i = 0; i < LOGS_AND_PLANKS.length; i++) {
			if (!player.getInventory().containsItem(LOGS_AND_PLANKS[i][0], 1)) {
				tries++;
				continue;
			}
		}
		if (tries >= LOGS_AND_PLANKS.length) {
			player.getPackets().sendGameMessage("You have no logs capable of being made into planks in your inventory.");
			return false;
		}
		for (int i = 0; i < LOGS_AND_PLANKS.length; i++) {
			if (LOGS_AND_PLANKS[i][0] != item.getId())
				continue;
			if (player.getInventory().getCoinsAmount() < COIN_COSTS[i]) {
				player.getPackets().sendGameMessage("You don't have enough coins to perform this spell.");
				return false;
			}
		}
		WorldTasksManager.schedule(new WorldTask() {
			int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(6298));
					player.setNextGraphics(new Graphics(1063));
				} else if (loop == 1) {
					player.getInventory().deleteItem(item.getId(), 1);
					for (int i = 0; i < LOGS_AND_PLANKS.length; i++) {
						if (LOGS_AND_PLANKS[i][0] != item.getId())
							continue;
						player.getInventory().removeItemMoneyPouch(new Item(995, COIN_COSTS[i]));
						player.getInventory().addItem(LOGS_AND_PLANKS[i][1], 1);
						break;
					}
					player.getSkills().addXp(Skills.MAGIC, 90);
					String plankName = null;
					for (int i = 0; i < LOGS_AND_PLANKS.length; i++) {
						if (LOGS_AND_PLANKS[i][0] != item.getId())
							continue;
						plankName = Utils.formatAorAn(new Item(LOGS_AND_PLANKS[i][0])) + ItemDefinitions.getItemDefinitions(LOGS_AND_PLANKS[i][1]).getName().toLowerCase();
					}
					player.sendFilteredMessage("You transform your " + ItemDefinitions.getItemDefinitions(item.getId()).getName().toLowerCase() + " into " + plankName + ".");
					stop();
				}
			loop++;
			}
		}, 0, 1);
		return true;
	}

}
