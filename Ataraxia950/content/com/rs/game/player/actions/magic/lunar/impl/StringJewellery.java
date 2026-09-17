package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.player.content.Magic;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class StringJewellery implements DefaultSpell {

	public static final int[][] STRINGABLES = new int[][] {
			{ 1673, 1692 },
			{ 1675, 1694 }, 
			{ 1677, 1696 },
			{ 1679, 1698 },
			{ 1681, 1700 },
			{ 1683, 1702 },
			{ 6579, 6581 }
	};
	
	@Override
	public int getId() {
		return 14851;
	}

	@Override
	public int getLevel() {
		return 80;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 10), new Item(WATER_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 4000;
	}

	@Override
	public boolean spellEffect(Player player) {
		int tries = 0;
		for (int i = 0; i < STRINGABLES.length; i++) {
			if (!player.getInventory().containsItem(STRINGABLES[i][0], 1)) {
				tries++;
				continue;
			}
		}
		if (tries >= STRINGABLES.length) {
			player.getPackets().sendGameMessage("You have no items capable of being strung in your inventory.");
			return false;
		}
		player.getSkills().addXp(Skills.MAGIC, 83);
		player.getSkills().addXp(Skills.CRAFTING, 4);
		// TODO Should really be converted to use "Action"
		WorldTask task = new WorldTask() {
			private int loop;
			private boolean continuous;

			@Override
			public void run() {
				if (loop == 0) {
					if (!Magic.checkRunes(player, false, ASTRAL_RUNE, 2, NATURE_RUNE, 1, EARTH_RUNE, 15)) {
						this.stop();
						return;
					}
					player.setNextAnimation(new Animation(4412));
					player.setNextGraphics(new Graphics(730));
				} else if (loop == 1) {
					for (int i = 0; i < STRINGABLES.length; i++) {
						if (!player.getInventory().containsItem(STRINGABLES[i][0], 1))
							continue;
						if (player.getInventory().containsItem(STRINGABLES[i][0], 1)) {
							player.getInventory().deleteItem(STRINGABLES[i][0], 1);
							player.getInventory().addItem(STRINGABLES[i][1], 1);
							if (continuous)
								Magic.checkRunes(player, true, ASTRAL_RUNE, 2, NATURE_RUNE, 1, EARTH_RUNE, 15);
							else
								continuous = true;
							player.getSkills().addXp(Skills.MAGIC, 83);
							player.getSkills().addXp(Skills.CRAFTING, 4);
							break;
						} else {
							this.stop();
							return;
						}
					}
					String ammyName = "", originalName = "";
					for (int i = 0; i < STRINGABLES.length; i++) {
						if (!player.getInventory().containsItem(STRINGABLES[i][1], 1))
							continue;
						if (player.getInventory().containsItem(STRINGABLES[i][1], 1)) {
							ammyName = Utils.formatAorAn(new Item(STRINGABLES[i][0])) + ItemDefinitions.getItemDefinitions(STRINGABLES[i][1]).getName().toLowerCase();
							originalName = ItemDefinitions.getItemDefinitions(STRINGABLES[i][1]).getName().toLowerCase();
						} else {
							this.stop();
							return;
						}
					}
					if (originalName.length() > 1 && ammyName.length() > 1)
						player.getPackets().sendGameMessage("Your transform your unstrung " + originalName + " into " + ammyName + ".");
					else {
						this.stop();
						return;
					}
					int amt = 0;
					for (int i = 0; i < STRINGABLES.length; i++) {
						if (!player.getInventory().containsItem(STRINGABLES[i][0], 1))
							continue;
						amt++;
					}
					if (amt >= 1)
						loop = -1;
					else
						this.stop();
				}
				loop++;
			}
		};
		player.stringJewelleryAction = task;
		WorldTasksManager.schedule(task, 0, 5);
		return true;
	}

}
