package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;

import java.util.List;

public class CureGroup implements DefaultSpell {

	@Override
	public int getId() {
		return 14839;
	}

	@Override
	public int getLevel() {
		return 74;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 2) };
	}

	@Override
	public int getDelay() {
		return 3500;
	}

	@Override
	public boolean spellEffect(Player player) {
		int affectedPeopleCount = 0;
		for (int regionId : player.getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes == null)
				continue;
			for (int playerIndex : playerIndexes) {
				Player p2 = World.getPlayers().get(playerIndex);
				if (p2 == null || p2.isDead() || p2.hasFinished() || !p2.withinDistance(player, 1))
					continue;
				if (!p2.isAcceptingAid() && !p2.equals(player))
					continue;
				player.setNextGraphics(new Graphics(736, 0, 150));
				if (p2.getPoison().isPoisoned()) {
					p2.getPackets().sendGameMessage("You have been cured of all illnesses!");
					p2.getPoison().reset();
					affectedPeopleCount++;
				}
			}
		}
		player.setNextAnimation(new Animation(4411));
		if (affectedPeopleCount > 0)
			player.getPackets().sendGameMessage("The spell affected " + affectedPeopleCount + " nearby people.");
		player.getSkills().addXp(Skills.MAGIC, 74);
		return true;
	}

}
