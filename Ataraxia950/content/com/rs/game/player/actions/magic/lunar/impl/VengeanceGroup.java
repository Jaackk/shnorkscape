package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.utils.Utils;

public class VengeanceGroup implements DefaultSpell {

	@Override
	public int getId() {
		return 14871;
	}

	@Override
	public int getLevel() {
		return 95;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 4), new Item(DEATH_RUNE, 3), new Item(EARTH_RUNE, 11) };
	}

	@Override
	public int getDelay() {
		return 0;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.getVengeanceDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getVengeanceDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast a group vengeance.");
			return false;
		} else if (player.isCastVeng()) {
			player.sendMessage("You have already cast a vengeance.");
			return false;
		}
		int count = 0;
		for (Player other : World.getPlayers()) {
			if (other != null && other.withinDistance(player, 4) && other.isAcceptingAid() && !other.equals(player)) {
				other.getPackets().sendGameMessage(player.getDisplayName() + " cast a group vengeance!");
				other.setCastVeng(true);
				other.setNextGraphics(new Graphics(725, 0, 100));
				count++;
			}
		}
		player.getPackets().sendGameMessage("The spell affected " + count + " nearby people.");
		player.getSkills().addXp(Skills.MAGIC, 120);
		player.setVengeanceDelay(30000);
		player.setNextGraphics(new Graphics(725, 0, 100));
		player.setNextAnimation(new Animation(4410));
		player.setCastVeng(true);
		return true;
	}

}
