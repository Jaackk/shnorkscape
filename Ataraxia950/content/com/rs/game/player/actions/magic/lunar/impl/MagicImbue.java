package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class MagicImbue implements DefaultSpell {

	@Override
	public int getId() {
		return 14853;
	}

	@Override
	public int getLevel() {
		return 82;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(FIRE_RUNE, 7), new Item(WATER_RUNE, 7) };
	}

	@Override
	public int getDelay() {
		return 4000;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.hasMagicImbue()) {
			player.sendMessage("You already have a magic imbue activated.");
			return false;
		}
		player.sendMessage("You feel a sudden surge flow through you..");
		player.setNextAnimation(new Animation(722));
		player.setMagicImbue(true);
		player.getSkills().addXp(Skills.MAGIC, 86);
		player.setNextGraphics(new Graphics(141, 0, 100));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setMagicImbue(false);
				player.sendMessage("You feel the effects of Magic imbue wear off.");
			}
		}, 20);
		return true;
	}

}
