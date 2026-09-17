package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.World;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.Pots.Pot;

import java.util.List;

public class StatRestorePotShare implements ItemSpell {

	@Override
	public int getId() {
		return 14852;
	}

	@Override
	public int getLevel() {
		return 81;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(EARTH_RUNE, 10), new Item(WATER_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player, Item item) {
		int affectedPeopleCount = 0;
		Pot pot = Pots.getPot(item.getId());
		if (pot == null) {
			player.sendMessage("You can only cast this spell on stat-restoring potions.");
			return false;
		}
		int doses2 = Pots.getDoses(pot, item) - 1;
		for (int regionId : player.getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes == null)
				continue;
			for (int playerIndex : playerIndexes) {
				final Player p2 = World.getPlayers().get(playerIndex);
				if (p2 == null || p2 == player || p2.isDead() || !p2.isActive() 
						|| p2.hasFinished() || !p2.isAcceptingAid() || !p2.withinDistance(player, 4))
					continue;
				else if (p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof DuelArena)
					continue;
				if (affectedPeopleCount >= doses2)
					break;
				Pots.shareEffect(player, p2, item, false);
				affectedPeopleCount++;
			}
		}
		Pots.shareEffect(player, item, player.getInventory().getItems().getThisItemSlot(item), affectedPeopleCount, false);
		return true;
	}

}
