package com.rs.game.player.combat;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.RouteStrategy;
import com.rs.game.route.strategy.EntityStrategy;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimpleNPCRouteEvent {

	private final NPC npc;
	private final Entity target;
	
	public void execute() {
		final RouteStrategy strategy = new EntityStrategy(target);
		final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, npc.getX(), npc.getY(),
				npc.getPlane(), npc.getSize(), strategy, true);
		if (steps == -1) {
			return;
		}
		if ((!RouteFinder.lastIsAlternative() && steps <= 0)) {
			return;
		}
		final int[] bufferX = RouteFinder.getLastPathBufferX();
		final int[] bufferY = RouteFinder.getLastPathBufferY();
		npc.resetWalkSteps();
		if (npc.isLocked()) {
			return;
		}
		for (int step = steps - 1; step >= 0; step--) {
			if (!npc.addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
				break;
			}
		}
	}
}
