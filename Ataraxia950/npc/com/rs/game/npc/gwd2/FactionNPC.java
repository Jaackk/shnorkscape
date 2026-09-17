package com.rs.game.npc.gwd2;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.faction.FactionNode;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.utils.Utils;

public class FactionNPC extends NPC {

	private static final long serialVersionUID = -4658367316116362595L;
	
	/**
	 * Three different types of states of the NPC.
	 * Default - default state of the NPC
	 * Running - running to a node.
	 * Under combat running - running to a node stopped by a player attacking, 
	 * continues after the combat finishes.
	 */
	public static final int DEFAULT = 0, RUNNING = 1, UNDER_COMBAT_RUNNING = 2;

	public FactionNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}
	
	protected WorldTile tile;
	private int state;
	private FactionNode location;
	
	public final void setLocation(FactionNode location) {
		this.location = location;
	}
	
	public final void setState(final int state) {
		this.state = state;
	}
	
	public final int getState() {
		return state;
	}
	
	public final FactionNode getLocation() {
		return location;
	}
	
	public final void sendConquering() {
		if (getCombat().process())
			return;
		tile = getTile();
		final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(tile.getX(), tile.getY()), true);
		final int[] bufferX = RouteFinder.getLastPathBufferX();
		final int[] bufferY = RouteFinder.getLastPathBufferY();
		for (int i = steps - 1; i >= 0; i--)
			if (!addWalkSteps(bufferX[i], bufferY[i], 25, true))
				break;
		setState(RUNNING);
	}
	
	@Override
	public void processNPC() {
		if (!getCombat().process()) {
			if (!checkAgressivity()) {
				if (getFreezeDelay() < Utils.currentTimeMillis()) {
					if (!hasWalkSteps()) {
						boolean can = false;
						for (int i = 0; i < 2; i++) {
							if (Math.random() * 1000.0 < 100.0) {
								can = true;
								break;
							}
						}
						if (can) {
							int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
							int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
							resetWalkSteps();
							addWalkSteps(getRespawnTile().getX() + moveX, getRespawnTile().getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
						}
					}
				}
			}
		}
	}
	
	public final WorldTile getTile() {
		if (location == null)
			return null;
		WorldTile tile = location.getTile();
		for (int i = 0; i < 50; i++) {
			tile = new WorldTile(location.getTile(), 15);
			if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), getSize()))
				break;
		}
		return tile;
	}
	
}
