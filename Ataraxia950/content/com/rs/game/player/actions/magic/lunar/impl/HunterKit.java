package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class HunterKit implements DefaultSpell {

	@Override
	public int getId() {
		return 14835;
	}

	@Override
	public int getLevel() {
		return 71;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 2) };
	}

	@Override
	public int getDelay() {
		return 4000;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.getInventory().getFreeSlots() < 1) {
			player.sendMessage("You do not have enough inventory space to perform this spell.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(6303));
					player.setNextGraphics(new Graphics(1074));
				} else if (loop == 1) {
					player.getSkills().addXp(Skills.MAGIC, 70);
					player.getInventory().addItem(11159, 1);
					player.sendMessage("A hunter kit was added to your inventory.");
					stop();
				}
				loop++;
			}
		}, 0, 6);
		return true;
	}

}
