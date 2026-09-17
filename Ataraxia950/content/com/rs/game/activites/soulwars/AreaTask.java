package com.rs.game.activites.soulwars;

import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Savions Sw
 */
public class AreaTask extends FixedLengthRunnable {

	private final ArrayList<Player> players = new ArrayList<Player>(500);

	@Override
	public boolean repeat() {
		try {
			if (World.soulWars.decrementMinute()) {
				for (Iterator<Player> it = players.iterator(); it.hasNext();) {
					Player player = it.next();
					if (player != null && !player.hasFinished()
							&& player.getControlerManager().getControler() instanceof AreaController)
						player.getControlerManager().sendInterfaces();
					else
						it.remove();
				}
			}
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return true;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}
}