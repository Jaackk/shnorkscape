package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.BloodReaver;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles the special ability Power from the Elements. AoD teleports to the
 * center and calls an icicle attack in a + formation in the center of the
 * arena, separating all four quadrants. Nex herself faces a certain quadrant
 * when landing from the teleport. The selected quadrant will be the only one
 * not to receive a blood reaver in it. The other three will. Reavers must be
 * defeated before they manage to reach Nex by walking. If they do so, Nex heals
 * and the team takes a punishing attack. Players within the quadrant Nex was
 * facing will receive two stacks of 350 damage which can be halved by using
 * protect from melee prayer.
 * 
 * @author Kris | 28. sept 2017 : 0:43.42
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class PowerFromTheElements extends AoDAbility {

	public PowerFromTheElements(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private final int direction = Utils.random(4);
	private static final Graphics STALAGMITE_GFX = new Graphics(6532);
	private static final Graphics STALAGMITE_HIT_GFX = new Graphics(6536);
	private static final Animation TELEPORT = new Animation(30129);
	private static final Animation LAND = new Animation(30130);
	private static final Animation CHARGE = new Animation(30134);

	private final WorldTile center = instance.CENTER;
	private int ticks;
	private final List<WorldTile> tiles = new ArrayList<WorldTile>();
	private final List<Player> players = new CopyOnWriteArrayList<Player>();

	/**
	 * TODO: Sync projectile with ticks.
	 */
	@Override
	public void run() {
		if (ticks == 0)
		if (players.size() > 0) {
			players.forEach(t -> {
				if (t.isDead() || t.hasFinished() || !instance.getPlayers().contains(t))
					players.remove(t);
			});
		}
		switch (ticks++) {
		case 0:
			nex.resetCombat();
			nex.setCannotMove(true);
			nex.sendMessage("The elements, they call to me..");
			break;
		case 1:
			nex.setNextAnimation(TELEPORT);
			break;
		case 5:
			nex.setNextAnimation(LAND);
			nex.setNextWorldTile(instance.getWorldTile(2847, 1823));
			nex.setNextFaceWorldTile(getCorner());
			break;
		case 6:
			for (int x = 2831; x < 2866; x++)
				tiles.add(instance.getWorldTile(x, 1824));
			for (int y = 1807; y < 1842; y++)
				tiles.add(instance.getWorldTile(2848, y));
			for (WorldTile t : tiles) {
				if (t.withinDistance(instance.CENTER, 1))
					continue;
				instance.sendGraphics(STALAGMITE_GFX, t);
			}
			break;
		case 8:
			for (WorldTile t : tiles) {
				if (t.withinDistance(instance.CENTER, 1))
					continue;
				instance.sendGraphics(STALAGMITE_HIT_GFX, t);
			}
			break;
		case 9:
			for (WorldTile t : tiles) {
				if (t.withinDistance(instance.CENTER, 1)) {
					World.spawnObject(new WorldObject(-1, 10, 0, t));
					continue;
				}
				instance.getPlayers().forEach(p -> {
					if (p.getTileHash() == t.getTileHash())
						p.applyHit(new Hit(p, Utils.random(750, 1750), HitLook.REGULAR_DAMAGE));
				});
				World.spawnObject(new WorldObject(100806 + Utils.random(3), 10, 0, t));
			}
			instance.getPlayers().forEach(p -> {
				if (withinQuadrant(p))
					players.add(p);
			});
			for (int i = 0; i < 4; i++) {
				if (i == direction)
					continue;
				instance.spawnNPC(new BloodReaver(getReaverTile(i), instance));
			}
			break;
		case 10:
			nex.sendMessage("WITNESS THEIR RAW POWER.");
			nex.setNextAnimation(CHARGE);
			break;
		case 12:
		case 15:
		case 17:
			if (players.isEmpty()) {
				nex.addEnrage(instance.getPlayers().size() * 3);
				nex.setCannotMove(false);
				return;
			}
			players.forEach(p -> instance.sendProjectile(new NewProjectile(getCorner(), p, getProjectileId(), 50, 10, 125, 0)));
			break;
		case 18:
			for (WorldTile t : tiles) {
				final WorldObject o = World.getObjectWithType(t, 10);
				if (o == null)
					World.unclipTile(t);
				else
					World.removeObject(o);
			}
		case 13:
		case 16:
			players.forEach(p -> p.applyHit(new Hit(nex, 350, HitLook.MAGIC_DAMAGE)));
			break;
		case 19:
			nex.setCannotMove(false);
			nex.setTarget(nex.getTargetedPlayer());
			stop();
			return;
		}
	}

	private int getProjectileId() {
		switch (direction) {
		case 0:
			return 6524;
		case 1:
			return 3371;
		case 2:
			return 6525;
		default:
			return 6523;
		}
	}

	private WorldTile getCorner() {
		switch (direction) {
		case 0:
			return instance.NORTH_EAST;
		case 1:
			return instance.NORTH_WEST;
		case 2:
			return instance.SOUTH_EAST;
		default:
			return instance.SOUTH_WEST;
		}
	}

	private WorldTile getReaverTile(final int direction) {
		switch (direction) {
		case 0:
			return instance.getWorldTile(2858, 1834);
		case 1:
			return instance.getWorldTile(2838, 1834);
		case 2:
			return instance.getWorldTile(2858, 1814);
		default:
			return instance.getWorldTile(2838, 1814);
		}
	}

	private boolean withinQuadrant(final Player player) {
		switch (direction) {
		case 0:
			return player.withinArea(center.getX(), center.getY(), instance.NORTH_EAST.getX(), instance.NORTH_EAST.getY());
		case 1:
			return player.withinArea(instance.NORTH_WEST.getX(), center.getY(), center.getX(), instance.NORTH_WEST.getY());
		case 2:
			return player.withinArea(center.getX(), instance.SOUTH_EAST.getY(), instance.SOUTH_EAST.getX(), center.getY());
		default:
			return player.withinArea(instance.SOUTH_WEST.getX(), instance.SOUTH_WEST.getY(), center.getX(), center.getY());
		}
	}

}
