package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class Humidify implements DefaultSpell {

	private static final int[][] FILLABLES = {
			{ 229, 227 }, { 1923, 1921 }, { 1925, 1929 }, { 1935, 1937 }, { 3734, 3735 }, { 5350, 5354 },
			{ 434, 1761 }, { 5331, 5340 }, { 5339, 5340 }, { 5338, 5340 }, { 5337, 5340 }, { 5336, 5340 },
			{ 5335, 5340 }, { 5334, 5340 }, { 5333, 5340 }, { 6667, 6668 }, { 1831, 1823 }, { 1829, 1823 },
			{ 1827, 1823 }, { 1825, 1823 },
	};
	
	@Override
	public int getId() {
		return 14830;
	}

	@Override
	public int getLevel() {
		return 68;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 1), new Item(FIRE_RUNE, 1), new Item(WATER_RUNE, 3) };
	}

	@Override
	public int getDelay() {
		return 4500;
	}

	@Override
	public boolean spellEffect(Player player) {
		int tryCount = 0;
		for (int i = 0; i < FILLABLES.length; i++) {
			if (!player.getInventory().containsItem(FILLABLES[i][0], 1)) {
				tryCount++;
				continue;
			}
		}
		if (tryCount >= FILLABLES.length) {
			player.getPackets().sendGameMessage("You have no items capable of being filled in your inventory.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(6294));
					player.setNextGraphics(new Graphics(1061));
				} else if (loop == 1) {
					for (int i = 0; i < FILLABLES.length; i++) {
						if (!player.getInventory().containsItem(FILLABLES[i][0], 1)) {
							continue;
						}
						while (player.getInventory().containsItem(FILLABLES[i][0], 1)) {
							int amount = player.getInventory().getNumberOf(FILLABLES[i][0]);
							player.getInventory().deleteItem(FILLABLES[i][0],
									player.getInventory().getNumberOf(FILLABLES[i][0]));
							player.getInventory().addItem(FILLABLES[i][1], amount);
							if (!player.getInventory().containsItem(FILLABLES[i][0], 1))
								break;
						}
					}
					player.getSkills().addXp(Skills.MAGIC, 65);
					player.getPackets().sendGameMessage("Moisture fills your inventory.");
					stop();
				}
				loop++;
			}
		}, 0, 6);
		return true;
	}

}
