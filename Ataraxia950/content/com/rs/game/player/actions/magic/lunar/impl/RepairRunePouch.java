package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class RepairRunePouch implements ItemSpell {

	private static final int[][] POUCHES = new int[][] {
		{ 5515, 5514 },
		{ 5513, 5512 },
		{ 5511, 5510 }
	};
	
	@Override
	public int getId() {
		return 14840;
	}

	@Override
	public int getLevel() {
		return 75;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 1), new Item(LAW_RUNE, 1) };
	}

	@Override
	public int getDelay() {
		return 2000;
	}

	@Override
	public boolean spellEffect(Player player, Item item) {
		boolean pouch = false;
		for (int[] pouchSet : POUCHES) {
			if (item.getId() == pouchSet[1]) {
				player.sendMessage("You can only cast this spell on a degraded rune pouch.");
				return false;
			} else if (item.getId() == pouchSet[0]) {
				pouch = true;
				break;
			}
		}
		if (!pouch) {
			player.sendMessage("You can only cast this spell on rune pouches.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextAnimation(new Animation(4136));
					player.getSkills().addXp(Skills.MAGIC, 75);
				} else if (ticks == 1) {
					player.getInventory().deleteItem(item);
					player.sendMessage("You repair the rune pouch.");
					player.getInventory().addItem(new Item(item.getId() - 1, 1));
					stop();
				}
				ticks++;
			}
		}, 0, 1);
		return true;
	}

}
