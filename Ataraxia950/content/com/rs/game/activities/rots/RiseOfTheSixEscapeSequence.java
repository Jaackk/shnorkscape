package com.rs.game.activities.rots;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 3. sept 2017 : 23:33.35
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class RiseOfTheSixEscapeSequence implements Runnable {

	private final RiseOfTheSix instance;
	
	public RiseOfTheSixEscapeSequence(final RiseOfTheSix instance) {
		this.instance = instance;
	}

	@Override
	public void run() {
		sendInterfaces();
		releaseSmokePits();
		transformMap();
		initiateCollapseSequence();
	}
	
	private final void initiateCollapseSequence() {
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			private final List<WorldTile> rocks = new ArrayList<WorldTile>();
			@Override
			public void run() {
				int alivePlayers = 0;
				for (Player p : instance.getPlayers()) {
					if (p == null || p.isDead() || p.hasFinished() || !p.withinDistance(instance.getWorldTile(0, 0), 200))
						continue;
					alivePlayers++;
				}
				if (instance.getPlayers().size() == 0 || alivePlayers == 0) {
					stop();
					return;
				}
				if (ticks >= 50) {
					instance.getPlayers().forEach(p -> {
						if (!p.isDead())
							p.applyHit(new Hit(null, Utils.random(150, 200), HitLook.REGULAR_DAMAGE));
					});
				}
				instance.getSmokePits().forEach(buff -> {
					instance.getPlayers().forEach(player -> {
						if (player.withinDistance(buff, 1) && !player.isDead())
							player.applyHit(new Hit(null, Utils.random(50, 100), HitLook.REGULAR_DAMAGE));
					});
				});
				if (!rocks.isEmpty()) {
					for (WorldTile t : rocks)
						World.spawnObject(new WorldObject(-1, 9, 0, t));
					rocks.clear();
				}
				if (Utils.random(5) == 0) {
					final WorldTile tile = new WorldTile(instance.getPlayers().get(Utils.random(instance.getPlayers().size())));
					if (!tile.withinDistance(new WorldTile(3542, 3311, 0), 25)) {
						if (World.canMoveNPC(tile, 5)) {
							World.spawnObject(new WorldObject(88117, 10, 0, tile));
							for (int x = tile.getX(); x < tile.getX() + 4; x++)
								for (int y = tile.getY(); y < tile.getY() + 4; y++) {
									final WorldTile t = new WorldTile(x, y, 0);
									if (!tile.matches(t))
										rocks.add(t);
								}

						} else if (World.canMoveNPC(tile, 3)) {
							World.spawnObject(new WorldObject(88118, 10, 0, tile));
							for (int x = tile.getX(); x < tile.getX() + 2; x++)
								for (int y = tile.getY(); y < tile.getY() + 2; y++) {
									final WorldTile t = new WorldTile(x, y, 0);
									if (!tile.matches(t))
										rocks.add(t);
								}
						}
					}
				}
				if (Utils.random(17 - ((ticks + 1) / 10)) == 1) {
					final WorldTile tile = new WorldTile(instance.getPlayers().get(Utils.random(instance.getPlayers().size())));
					if (!tile.withinDistance(new WorldTile(3540, 3311, 0), 50)) {
						World.sendGraphics(null, new Graphics(4412), tile);
						instance.getPlayers().forEach(player -> {
							if (player.withinDistance(tile, 2) && !player.isDead())
								player.applyHit(new Hit(null, Utils.random(150), HitLook.REGULAR_DAMAGE));
						});
					}
				}
				ticks++;
			}
		}, 0, 0);
	}
	
	private final void releaseSmokePits() {
		for (int regions : instance.getAllMapRegionIds()) {
			if (World.getRegion(regions) == null)
				continue;
			if (World.getRegion(regions).getAllObjects() == null)
				continue;
			World.getRegion(regions).getAllObjects().forEach(obj -> {
				if (obj == null)
					return;
				if (obj.getId() == 88068) {
					WorldObject pit = new WorldObject(88067, obj.getType(), obj.getRotation(), obj.getX(), obj.getY(), obj.getPlane());
					World.spawnObject(pit);
					instance.addSmokePit(pit);
				}
			});
		}
	}
	
	/**
	 * Transforms all the bridges in the minigame into broken ones and spawns different obstacles near them
	 * so players can get past the bridges. Ledge has a 100% rate, vine and stone have a 50% rate each to spawn.
	 */
	private final void transformMap() {
		instance.getMap().getBridges().forEach((k, v) -> {
			loop : for (RiseOfTheSixEscapeObject objects : RiseOfTheSixEscapeObject.values()) {
				if (!objects.getRoomName().equalsIgnoreCase(k.substring(0, k.length() - 1)))
					continue loop;
				if (objects.getRoomRotation() != v[2])
					continue loop;
				if (k.startsWith("hall3") && objects.getRoomName().startsWith("hall2") || k.startsWith("hall2") && objects.getRoomName().startsWith("hall3"))
					continue loop;
				final int CX = v[0] * 8;
				final int CY = v[1] * 8;
				World.spawnObject(new WorldObject(88060, 10, objects.getBridgeCoordinates().getPlane(), objects.getBridgeCoordinates().getX() + CX, objects.getBridgeCoordinates().getY() + CY, 0));
				World.spawnObject(new WorldObject(88061, 10, objects.getLedgeCoordinates().getPlane(), objects.getLedgeCoordinates().getX() + CX, objects.getLedgeCoordinates().getY() + CY, 0));
				if (Utils.randomBool())
					World.spawnObject(new WorldObject(88063, 10, objects.getVineCoordinates().getPlane(), objects.getVineCoordinates().getX() + CX, objects.getVineCoordinates().getY() + CY, 0));
				if (Utils.randomBool())
					World.spawnObject(new WorldObject(88065, 10, objects.getPillarCoordinates().getPlane(), objects.getPillarCoordinates().getX() + CX, objects.getPillarCoordinates().getY() + CY, 0));
				if (objects.getUnclipTiles() != null && objects.getUnclipTiles().length > 0) {
					for (WorldTile t : objects.getUnclipTiles())
						World.unclipTile(new WorldTile(CX + t.getX(), CY + t.getY(), 0));
				}
				break loop;
			}
		});
	}
	
	/**
	 * Shakes everyones screen and sends a timer interface with a delay of 30 seconds.
	 */
	private final void sendInterfaces() {
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			private int time = 30;

			@Override
			public boolean repeat() {
				instance.getPlayers().forEach(player -> {
					if (time == 30) {
						player.getPackets().sendCameraShake(3, 25, 50, 25, 50);
						player.getInterfaceManager().sendOverlay(1073, false);
						player.getPackets().sendIComponentText(1073, 2, "Total collapse:");
					}
					player.getPackets().sendIComponentText(1073, 3, time + " seconds");
				});
				return time-- != 0;
			}
		}, 0, 1);
	}
	
}
