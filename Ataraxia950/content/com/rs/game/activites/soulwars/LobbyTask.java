package com.rs.game.activites.soulwars;

import com.rs.cores.FixedLengthRunnable;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Savions Sw
 */
public class LobbyTask extends FixedLengthRunnable {

	private final ArrayList<Player> players = new ArrayList<Player>(500);

	@Override
	public boolean repeat() {
		try {
			for (Iterator<Player> it = players.iterator(); it.hasNext();) {
				Player player = it.next();
				if (player != null && player.getControlerManager().getControler() instanceof LobbyController)
					player.getControlerManager().sendInterfaces();
				else
					it.remove();
			}
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return true;
	}

	public ArrayList<Player> getPlayers(Teams team) {
		ArrayList<Player> members = new ArrayList<Player>(players.size());
		for (Player player : players) {
			if (player != null) {
				final int cape = player.getEquipment().getCapeId() - SoulWarsManager.TEAM_CAPE_INDEX;
				if (cape < 0 || cape > 1)
					continue;
				if (Teams.values()[cape].equals(team))
					members.add(player);
			}
		}
		return members;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}
}