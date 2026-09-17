package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AoD sends a Vindicta-like smoke in a + formation, based on two targets. One
 * wall is placed horizontally, the other vertically. Players standing on the
 * fire receive heavy damage. If there are less than two targets in the
 * instance, both smoke walls will target the same player.
 * 
 * @author Kris | 30. sept 2017 : 16:56.13
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class SmokeWall extends AoDAbility {

	public SmokeWall(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	public static final Graphics SMOKE = new Graphics(6535);
	private static final Animation PREPARE = new Animation(30135);
	private int ticks;
	private final Map<Player, Integer> targets = new HashMap<Player, Integer>();
	private final List<WorldTile> tiles = new ArrayList<WorldTile>();
	private WorldTile[] tilesArray;

	@Override
	public void run() {
		if (targets.size() > 0) {
			ArrayList<Player> playersToRemove = new ArrayList<>();
			targets.forEach((t, d) -> {
				if (t.isDead() || t.hasFinished() || !instance.getPlayers().contains(t))
					playersToRemove.add(t);
			});
			for (Player playerToRemove : playersToRemove) {
				targets.remove(playerToRemove);
			}
		}
		switch (ticks++) {
		case 0:
			nex.resetCombat();
			nex.setCannotMove(true);
			nex.setNextAnimation(PREPARE);
			nex.sendMessage("Feel the smoke flow all around you. LET IT CONSUME YOU.");
			for (int i = 0; i < 50; i++) {
				if (targets.size() == 2)
					break;
				if (instance.getPlayers().size() == 0)
					break;
				final Player p = instance.getPlayers().get(Utils.random(instance.getPlayers().size()));
				if (targets.get(p) != null)
					continue;
				targets.put(p, targets.size() == 0 ? (Utils.random(2)) : (Utils.random(2, 4)));
			}
			targets.forEach((t, d) -> t.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>Nex begins to draw smoke from the " + getDirection(d) + " towards you.", true));
			final WorldTile minBoundary = instance.getWorldTile(2831, 1807);
			final WorldTile maxBoundary = instance.getWorldTile(2865, 1841);
			targets.forEach((t, d) -> {
				if (d > 1 && (t.getX() == maxBoundary.getY() || t.getY() == minBoundary.getY()) || d < 2 && (t.getX() == maxBoundary.getX() || t.getY() == minBoundary.getX()))
					return;
				switch (d) {
				case 0:
				case 1:
					tiles.addAll(Utils.calculateLine(t.getX(), d == 1 ? minBoundary.getY() : maxBoundary.getY(), t.getX(), d == 1 ? maxBoundary.getY() : minBoundary.getY(), 2));
					break;
				default:
					tiles.addAll(Utils.calculateLine(d == 2 ? maxBoundary.getX() : minBoundary.getX(), t.getY(), d == 2 ? minBoundary.getX() : maxBoundary.getX(), t.getY(), 2));
					break;
				}
			});
			if (targets.size() == 1) {
				targets.forEach((t, d) -> {
					if (t.getX() == minBoundary.getX() || t.getY() == minBoundary.getY() || t.getX() == maxBoundary.getX() || t.getY() == maxBoundary.getY())
						return;
					switch (d) {
					case 2:
					case 3:
						tiles.addAll(Utils.calculateLine(t.getX(), d == 2 ? minBoundary.getY() : maxBoundary.getY(), t.getX(), d == 2 ? maxBoundary.getY() : minBoundary.getY(), 2));
						break;
					default:
						tiles.addAll(Utils.calculateLine(d == 0 ? maxBoundary.getX() : minBoundary.getX(), t.getY(), d == 0 ? minBoundary.getX() : maxBoundary.getX(), t.getY(), 2));
						break;
					}
				});
			}
			break;
		case 6:
			nex.setCannotMove(false);
			nex.setTarget(nex.getTargetedPlayer());
			instance.addFires(tilesArray = tiles.toArray(new WorldTile[tiles.size()]));
			break;
		case 55:
			instance.removeFires(tilesArray);
			stop();
			break;
		}
	}

	private String getDirection(final int direction) {
		switch (direction) {
		case 0:
			return "north";
		case 1:
			return "south";
		case 2:
			return "east";
		default:
			return "west";
		}
	}

}
