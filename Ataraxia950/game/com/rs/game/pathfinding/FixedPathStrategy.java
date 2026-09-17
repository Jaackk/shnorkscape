package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 3:12:50 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class FixedPathStrategy extends PathStrategy {
	
	/**
	 * Construct a new {@link FixedPathStrategy} object instance.
	 * 
	 * @param x
	 *            the destination x position.
	 * @param y
	 *            the destination y position.
	 * @param sizeX
	 *            the destination x size.
	 * @param sizeY
	 *            the destination y size.
	 */
	public FixedPathStrategy(int x, int y) {
		super(x, y, 1, 1);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs2.server.game.map.path.strategy.PathStrategy#canExit(int,
	 * int, int, com.rs2.server.game.map.CollisionMap)
	 */
	@Override
	public boolean canExit(int size, int x, int y, CollisionMap collision) {
		return this.x == x && this.y == y;
	}
	
}
