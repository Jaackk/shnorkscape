package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.utils.Utils;

public class VengeanceOther implements PlayerSpell {

	@Override
	public int getId() {
		return 14869;
	}

	@Override
	public int getLevel() {
		return 93;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 3), new Item(DEATH_RUNE, 2), new Item(EARTH_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 4000;
	}

	@Override
	public boolean spellEffect(Player player, Player target) {
		player.faceEntity(target);
		if (player.getVengeanceDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getVengeanceDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast a vengeance other.");
			return false;
		} else if (target.isCastVeng()) {
			player.sendMessage("The target already has the power of vengeance.");
			return false;
		} else if (!target.isAcceptingAid()) {
			player.sendMessage("The targeted player is not accepting aid.");
			return false;
		}
		player.getSkills().addXp(Skills.MAGIC, 108);
		player.setVengeanceDelay(30000);
		player.setNextAnimation(new Animation(4411));
		target.setNextGraphics(new Graphics(725, 0, 100));
		target.setCastVeng(true);
		target.sendMessage(player.getDisplayName() + " has cast a vengeance other on you.");
		player.sendMessage("You cast a vengeance other on " + target.getDisplayName() + ".");
		return true;
	}

}
