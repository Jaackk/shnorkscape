package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 3:17:37 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class PlayerPathStrategy extends PathStrategy {
	
	/**
	 * Construct a new {@link PlayerPathStrategy} object instance.
	 * 
	 * @param x
	 *            the destination x position.
	 * @param y
	 *            the destination y position.
	 * @param sizeX
	 *            the destination size x.
	 * @param sizeY
	 *            the destination size y.
	 */
	public PlayerPathStrategy(int x, int y, int sizeX, int sizeY) {
		super(x, y, sizeX, sizeY);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs2.server.game.map.path.strategy.PathStrategy#canExit(int,
	 * int, int, com.rs2.server.game.map.CollisionMap)
	 */
	@Override
	public boolean canExit(int size, int x, int y, CollisionMap collision) {
		return collision.canPlayerExit(x, y, size, size, this.x, this.y, this.sizeX, this.sizeY, 0);
	}
	
}
