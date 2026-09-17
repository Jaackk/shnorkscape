package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;

public class CureMe implements DefaultSpell {

	@Override
	public int getId() {
		return 14834;
	}

	@Override
	public int getLevel() {
		return 71;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 2) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (!player.getPoison().isPoisoned()) {
			player.sendMessage("You aren't poisoned.");
			return false;
		}
		player.getSkills().addXp(Skills.MAGIC, 69);
		player.setNextAnimation(new Animation(4411));
		player.setNextGraphics(new Graphics(736, 0, 150));
		player.getPoison().reset();
		return true;
	}

}
