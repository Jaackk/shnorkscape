package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewForceMovement;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rs.game.ForceMovement.EAST;
import static com.rs.game.ForceMovement.NORTH;
import static com.rs.game.ForceMovement.SOUTH;
import static com.rs.game.ForceMovement.WEST;

/**
 * @author Kris | 3. sept 2017 : 23:36.00
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class WallSlam extends RoTSEffect {

	public WallSlam(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	private int direction, delay;
	
	private static final int[][] POSSIBLE_WESTERN_LOCATIONS = new int[][] {
		{ 31, 11, WEST }, { 31, 10, WEST }, { 27, 10, NORTH }, { 26, 10, NORTH }, { 21, 10, NORTH }, { 20, 10, NORTH }, { 19, 13, EAST }, { 19, 14, EAST }, 
		{ 19, 15, EAST }, { 19, 16, EAST }, { 19, 21, EAST }, { 19, 22, EAST }, { 19, 23, EAST }, { 19, 24, EAST }, { 19, 29, EAST }, { 20, 30, SOUTH }, 
		{ 24, 30, SOUTH }, { 25, 30, SOUTH }, { 26, 30, SOUTH }, { 31, 29, SOUTH }, 
	};
	
	private static final int[][] POSSIBLE_EASTERN_LOCATIONS = new int[][] {
		{ 36, 28, EAST }, { 36, 29, EAST }, { 37, 30, SOUTH }, { 38, 30, SOUTH }, { 39, 30, SOUTH }, { 48, 17, WEST }, { 48, 16, WEST }, { 49, 14, WEST }, 
		{ 49, 13, WEST }, { 48, 11, WEST }, { 47, 10, NORTH }, { 46, 10, NORTH }, { 45, 10, NORTH }, { 40, 10, NORTH }, { 37, 10, NORTH }, { 36, 11, EAST }, 
		{ 36, 12, EAST }
	};
	
	@Override
	public void start() {	
		final boolean west = npc.getX() < instance.getMap().getTile("center").getX();
		final WorldTile slam = getSlamLocation(west ? POSSIBLE_WESTERN_LOCATIONS : POSSIBLE_EASTERN_LOCATIONS);
		final WorldTile location = new WorldTile(slam.getX(), slam.getY(), 0);
		direction = slam.getPlane();
		if (location.getDistance(npc) > 7 || cancel()) {
			npc.finishEffect();
			return;
		}
		npc.setTarget(null);
		npc.setNextFaceWorldTile(location);
		npc.setCantDoDefenceEmote(true);
		npc.setCannotMove(true);
		final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, npc.getX(), npc.getY(), npc.getPlane(), npc.getSize(), new FixedTileStrategy(location.getX(), location.getY()), true);
		final int[] bufferX = RouteFinder.getLastPathBufferX();
		final int[] bufferY = RouteFinder.getLastPathBufferY();
		for (int i = steps - 1; i >= 0; i--)
			if (!npc.addWalkSteps(bufferX[i], bufferY[i], 25, true))
				break;
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			private boolean initiated;
			private WorldTile landingTile;
			@Override
			public void run() {
				if (cancel() || tick == 4) {
					if (landingTile != null)
						npc.setNextWorldTile(landingTile);
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setCantDoDefenceEmote(false);
					npc.setCannotMove(false);
					npc.setTarget(instance.generateRandomTarget(npc));
					stop();
					return;
				} 
				if (npc.getTileHash() == location.getTileHash() && !initiated) {
					initiated = true;
					tick = 0;
				}
				if (!initiated || initiated && tick == 20) {
					if (!npc.hasWalkSteps()) {
						if (delay++ == 5) {
							npc.finishEffect();
							npc.refreshSpecialDelay();
							npc.setCantDoDefenceEmote(false);
							npc.setCannotMove(false);
							npc.setTarget(instance.generateRandomTarget(npc));
							stop();
							return;
						}
						final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, npc.getX(), npc.getY(), npc.getPlane(), npc.getSize(), new FixedTileStrategy(location.getX(), location.getY()), true);
						final int[] bufferX = RouteFinder.getLastPathBufferX();
						final int[] bufferY = RouteFinder.getLastPathBufferY();
						for (int i = steps - 1; i >= 0; i--)
							if (!npc.addWalkSteps(bufferX[i], bufferY[i], 25, true))
								break;
					}
					return;	
				}
				if (tick == 0) {
					landingTile = getLandingTile();
					final WorldTile min = instance.getWorldTile(19, 10);
					final WorldTile max = instance.getWorldTile(31, 29);
					final WorldTile min2 = instance.getWorldTile(36, 10);
					final WorldTile max2 = instance.getWorldTile(48, 30);
					if (!landingTile.withinArea(min.getX(), min.getY(), max.getX(), max.getY())
							&& !landingTile.withinArea(min2.getX(), min2.getY(), max2.getX(), max2.getY()))
						landingTile = npc.getLastWorldTile();
					npc.setNextAnimation(new Animation(21930));
				} else if (tick == 1) {
					npc.setNextForceMovement(new NewForceMovement(npc, 0, landingTile, 2, Utils.getFaceDirection(npc.getX() - landingTile.getX(), npc.getY() - landingTile.getY())));
				} else if (tick == 3) {
					instance.getPlayers().forEach(player -> {
						if (player.withinDistance(landingTile, 2))
							player.applyHit(new Hit(player, instance.withinShadowRealm() ? Utils.random(400, 600) : Utils.random(300, 400), HitLook.REGULAR_DAMAGE));
					});
				}
				tick++;
			}
		}, 0, 0);
	}
	
	private WorldTile getSlamLocation(final int[][] loc) {
		final List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
		possibleTiles.add(instance.getWorldTile(loc[0][0], loc[0][1]));
		for (int[] l : loc) {
			if (instance.getWorldTile(l[0], l[1]).withinDistance(npc, 1))
				continue;
			final int dir = Utils.getAngle(npc.getX() - instance.getWorldTile(l[0], l[1]).getX(), npc.getY() - instance.getWorldTile(l[0], l[1]).getY());
			if (dir % 4096 == 0 && instance.getWorldTile(l[0], l[1]).getDistance(npc) < 8) {
				final WorldTile t = instance.getWorldTile(l[0], l[1]);
				possibleTiles.add(new WorldTile(t.getX(), t.getY(), l[2]));
			}
		}
		if (possibleTiles.size() == 1)
			return possibleTiles.get(0);
		return possibleTiles.get(Utils.random(1, possibleTiles.size()));
	}
	
	private WorldTile getLandingTile() {
		switch(direction) {
		case EAST:
			return new WorldTile(npc.getX() + Utils.random(3, 7), npc.getY(), npc.getPlane());
		case WEST:
			return new WorldTile(npc.getX() - Utils.random(3, 7), npc.getY(), npc.getPlane());
		case SOUTH:
			return new WorldTile(npc.getX(), npc.getY() - Utils.random(3, 7), npc.getPlane());
			default:
				return new WorldTile(npc.getX(), npc.getY() + Utils.random(3, 7), npc.getPlane());
		}
	}

}
