package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.utils.Utils;

public class Vengeance implements DefaultSpell {

	@Override
	public int getId() {
		return 14870;
	}

	@Override
	public int getLevel() {
		return 94;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 4), new Item(DEATH_RUNE, 2), new Item(EARTH_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 0;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.getVengeanceDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getVengeanceDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast a vengeance.");
			return false;
		} else if (player.isCastVeng()) {
			player.sendMessage("You have already cast a vengeance.");
			return false;
		}
		player.getSkills().addXp(Skills.MAGIC, 112);
		player.setVengeanceDelay(30000);
		player.setNextGraphics(new Graphics(726, 0, 100));
		player.setNextAnimation(new Animation(4410));
		player.setCastVeng(true);
		return true;
	}

	public static void reset(final Player player) {
		if (player.getVengeanceDelay() > Utils.currentTimeMillis())
			player.setVengeanceDelay(1);
		if (player.isCastVeng())
			player.setCastVeng(false);
	}

}
