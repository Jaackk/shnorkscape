package com.rs.game.player.content;

import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.pathfinding.PathStrategy;
import com.rs.game.pathfinding.PlayerPathStrategy;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.RouteStrategy;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.route.strategy.FloorItemStrategy;
import com.rs.game.route.strategy.ObjectStrategy;

public class RouteEvent {

	/**
	 * Object to which we are finding the route.
	 */
	private final Object object;
	/**
	 * The event instance.
	 */
	private final Runnable event;
	/**
	 * Whether we also run on alternative.
	 */
	private final boolean alternative;
	/**
	 * Contains last route strategies.
	 */
	private RouteStrategy[] last;

	public RouteEvent(final Object object, final Runnable event) {
		this(object, event, false);
	}

	public RouteEvent(final Object object, final Runnable event, final boolean alternative) {
		this.object = object;
		this.event = event;
		this.alternative = alternative;
	}

	public boolean processEvent(final Player player) {
		// if (CombatDefinitions.hasPolyporeStaff(player)) {
		// Logger.getGlobal().info("Db0");
		// if (player.isUnderCombat() || player.getAttackingDelay() > 0) {
		// Logger.getGlobal().info("Target");
		// int steps1 = 8;
		//// Logger.getGlobal().info(Utils.getDistance(player.getX(), player.getY(),
		// player.get().getX(), player.getAttackedBy().getY()));
		// if (steps1 <= 7) {
		// Logger.getGlobal().info("Damn");
		// RouteStrategy[] strategies = generateStrategies();
		// if (last != null && match(strategies, last) && player.hasWalkSteps())
		// return false;
		// else if (last != null && match(strategies, last) &&
		// !player.hasWalkSteps()) {
		// for (int i = 0; i < strategies.length; i++) {
		// RouteStrategy strategy = strategies[i];
		// int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
		// player.getX(), player.getY(),
		// player.getPlane(), player.getSize(), strategy, i ==
		// (strategies.length - 1));
		// if (steps == -1)
		// continue;
		// if ((!RouteFinder.lastIsAlternative() && steps <= 0) || alternative)
		// {
		// if (alternative)
		// player.getPackets().sendResetMinimapFlag();
		// event.run();
		// return true;
		// }
		// }
		// }
		// }
		// } else {
		// Logger.getGlobal().info("no target");
		// RouteStrategy[] strategies = generateStrategies();
		// if (last != null && match(strategies, last) && player.hasWalkSteps())
		// return false;
		// else if (last != null && match(strategies, last) &&
		// !player.hasWalkSteps()) {
		// for (int i = 0; i < strategies.length; i++) {
		// RouteStrategy strategy = strategies[i];
		// int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
		// player.getX(), player.getY(),
		// player.getPlane(), player.getSize(), strategy, i ==
		// (strategies.length - 1));
		// if (steps == -1)
		// continue;
		// if ((!RouteFinder.lastIsAlternative() && steps <= 0) || alternative)
		// {
		// if (alternative)
		// player.getPackets().sendResetMinimapFlag();
		// event.run();
		// return true;
		// }
		// }
		// }
		// }
		// }
		@SuppressWarnings("unused")
		final
		PathStrategy str = new PlayerPathStrategy(player.getX(), player.getY(), 1, 1);
		//CollisionMap map = 
		//com.rs.game.pathfinding.PathFinder.
		if (!simpleCheck(player)) {
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		}
		final RouteStrategy[] strategies = generateStrategies();
		if (last != null && match(strategies, last) && player.hasWalkSteps()) {
			return false;
		} else if (last != null && match(strategies, last) && !player.hasWalkSteps()) {
			WorldObject obj = null;
			int size = 0;
			for (int i = 0; i < strategies.length; i++) {
				final RouteStrategy strategy = strategies[i];
				final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
						player.getPlane(), player.getSize(), strategy, i == (strategies.length - 1));
				if (steps == -1) {
					continue;
				}
				if (object instanceof WorldObject) {
					obj = (WorldObject) object;
					size = obj.getDefinitions().getSizeX() > obj.getDefinitions().getSizeY() ? obj.getDefinitions().getSizeX() : obj.getDefinitions().getSizeY();
				}
				
				if ((!RouteFinder.lastIsAlternative() && steps <= 0) || alternative || (obj != null && player.withinDistance(obj, size))) {
					if (alternative) {
						player.getPackets().sendResetMinimapFlag();
					}
					if (event != null) {
						event.run();
					}
					return true;
				}
			}
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		} else {
			last = strategies;

			for (int i = 0; i < strategies.length; i++) {
				final RouteStrategy strategy = strategies[i];
				final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
						player.getPlane(), player.getSize(), strategy, i == (strategies.length - 1));
				if (steps == -1) {
					continue;
				}
				if ((!RouteFinder.lastIsAlternative() && steps <= 0)) {
					if (alternative) {
						player.getPackets().sendResetMinimapFlag();
					}
					if (event != null) {
						event.run();
					}
					return true;
				}
				final int[] bufferX = RouteFinder.getLastPathBufferX();
				final int[] bufferY = RouteFinder.getLastPathBufferY();
				final WorldTile last = new WorldTile(bufferX[0], bufferY[0], player.getPlane());
				player.resetWalkSteps();
				player.getPackets().sendMinimapFlag(
						last.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()),
						last.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()));
				if (player.isLocked()) {
					return false;
				}
				for (int step = steps - 1; step >= 0; step--) {
					if (!player.addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
						break;
					}
				}
				return false;
			}
			player.getPackets().sendGameMessage("You can't reach that.");
			player.getPackets().sendResetMinimapFlag();
			return true;
		}
	}

	private boolean simpleCheck(final Player player) {
		if (object instanceof Entity) {
			return player.getPlane() == ((Entity) object).getPlane();
		} else if (object instanceof WorldObject) {
			return player.getPlane() == ((WorldObject) object).getPlane();
		} else if (object instanceof FloorItem) {
			return player.getPlane() == ((FloorItem) object).getTile().getPlane();
		} else if (object instanceof WorldTile) {
			return player.getPlane() == ((WorldTile) object).getPlane();
		} else {
			throw new RuntimeException(object + " is not instanceof any reachable entity.");
		}
	}

	private RouteStrategy[] generateStrategies() {
		if (object instanceof Entity) {
			return new RouteStrategy[] { new EntityStrategy((Entity) object) };
		} else if (object instanceof WorldObject) {
			return new RouteStrategy[] { new ObjectStrategy((WorldObject) object) };
		} else if (object instanceof FloorItem) {
			final FloorItem item = (FloorItem) object;
			return new RouteStrategy[] { new FixedTileStrategy(item.getTile().getX(), item.getTile().getY()),
					new FloorItemStrategy(item) };
		} else if (object instanceof WorldTile) {
			final WorldTile tile = (WorldTile) object;
			return new RouteStrategy[] { new FixedTileStrategy(tile.getX(), tile.getY()) };
		}
		else {
			throw new RuntimeException(object + " is not instanceof any reachable entity.");
		}
	}

	private boolean match(final RouteStrategy[] a1, final RouteStrategy[] a2) {
		if (a1.length != a2.length) {
			return false;
		}
		for (int i = 0; i < a1.length; i++) {
			if (!a1[i].equals(a2[i])) {
				return false;
			}
		}
		return true;
	}

}
