package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 3:14:58 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class ItemPathStrategy extends PathStrategy {
	
	/**
	 * Construct a new {@link ItemPathStrategy} object instance.
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
	public ItemPathStrategy(int x, int y, int sizeX, int sizeY) {
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
		return canExitRectangle(x, y, size, size, this.x, this.y, this.sizeX, this.sizeY);
	}
	
}
