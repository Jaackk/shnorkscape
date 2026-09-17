package com.rs.game.npc.gwd2.faction;

import com.rs.game.WorldTile;
import com.rs.game.npc.gwd2.SliskeFaction;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public class SliskeClusterNPC extends SliskeFaction {

	private static final long serialVersionUID = 5586829991073023121L;
	private final FactionCluster cluster;
	
	public SliskeClusterNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, final FactionCluster cluster) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.cluster = cluster;
	}
	
	@Override
	public void processNPC() {
		if (isDead() || isLocked())
			return;
		if (!getRun() || getCombat().process() || getAttackedBy() != null && getAttackedByDelay() > Utils.currentTimeMillis()) {
			super.processNPC();
			return;
		}
		if (getState() == RUNNING && getNextRunDirection() == -1) {
			if (tile != null && withinDistance(tile, 3)) {
				setState(DEFAULT);
				setRun(false);
			}
		} else if (getState() == DEFAULT && getLocation() != null || getState() == RUNNING && getNextRunDirection() == -1) {
			tile = getTile();
			final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(tile.getX(), tile.getY()), true);
			final int[] bufferX = RouteFinder.getLastPathBufferX();
			final int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int i = steps - 1; i >= 0; i--)
				if (!addWalkSteps(bufferX[i], bufferY[i], -1, true))
					break;
			setState(RUNNING);
		}
	}
	
	@Override
	public boolean checkAgressivity() {
		if (getState() == RUNNING)
			return false;
		return super.checkAgressivity();
	}
	
	@Override
	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				try {
					if (cluster.getNode() != null && cluster.getNode().getType() == HeartOfGielinor.SLISKE)
						spawn();
				} catch (Throwable e) {
					Logger.getGlobal().catching(e);
				}
			}
		}, getCombatDefinitions().getRespawnDelay());
	}

}
