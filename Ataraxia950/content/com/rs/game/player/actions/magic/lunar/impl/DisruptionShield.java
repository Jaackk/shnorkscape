package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.utils.Utils;

public class DisruptionShield implements DefaultSpell {

	@Override
	public int getId() {
		return 14865;
	}

	@Override
	public int getLevel() {
		return 90;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 3), new Item(BLOOD_RUNE, 3), new Item(BODY_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 0;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.getDisruptionDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getDisruptionDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast disruption shield.");
			return false;
		} else if (player.hasDisruption()) {
			player.sendMessage("You already have a disruption shield active.");
			return false;
		}
		player.setDisruption(true);
		player.getSkills().addXp(Skills.MAGIC, 97);
		player.setNextGraphics(new Graphics(1320, 0, 100));
		player.setNextAnimation(new Animation(8770));
		Perk bulwark = player.getInventionManager().hasPerk(Perks.BULWARK);
		int time = 60000;
		if (bulwark != null)
		    time -= bulwark.getRank() * 5000;
		player.setDisruptionDelay(time);
		return true;
	}

}
