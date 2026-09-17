package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 10:40:47 AM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public abstract class PathStrategy {
	
	/**
	 * The destination x position.
	 */
	protected int x;
	
	/**
	 * The destination y position.
	 */
	protected int y;
	
	/**
	 * The destination x size.
	 */
	protected int sizeX;
	
	/**
	 * The destination y size.
	 */
	protected int sizeY;
	
	/**
	 * Construct a new {@link PathStrategy} object instance.
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
	public PathStrategy(int x, int y, int sizeX, int sizeY) {
		this.x = x;
		this.y = y;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	/**
	 * Checks if we can exit from the specified {@code point}.
	 * 
	 * @param size
	 *            the point size.
	 * @param x
	 *            the point position x.
	 * @param y
	 *            the point position y.
	 * @param collision
	 *            the region collision map.
	 * @return <code>true</code> if we can exit from the given {@code point}
	 *         otherwise <code>false</code>.
	 */
	public abstract boolean canExit(int size, int x, int y, CollisionMap collision);
	
	/**
	 * Checks if the given point can exit from the specified rectangular area or
	 * not.
	 * 
	 * @param srcX
	 *            the source position x.
	 * @param srcY
	 *            the source position y.
	 * @param sizeX
	 *            the source size x.
	 * @param sizeY
	 *            the source size y.
	 * @param dstX
	 *            the destination position x.
	 * @param dstY
	 *            the destination position y.
	 * @param dstSizeX
	 *            the destination position x.
	 * @param dstSizeY
	 *            the destination position y.
	 * @return <code>true</code> if can exit otherwise <code>false</code>.
	 */
	public static boolean canExitRectangle(int srcX, int srcY, int sizeX, int sizeY, int dstX, int dstY, int dstSizeX, int dstSizeY) {
		if (srcX >= dstX + dstSizeX || dstX >= srcX + sizeX) {
			return false;
		}
        return srcY < dstY + dstSizeY && dstY < srcY + sizeY;
    }
	
	/**
	 * Gets the destination x position.
	 * 
	 * @return the destination x position.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Gets the destination y position.
	 * 
	 * @return the destination y position.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Gets the destination x size.
	 * 
	 * @return the destination x size.
	 */
	public int getSizeX() {
		return sizeX;
	}
	
	/**
	 * Gets the destination y size.
	 * 
	 * @return the the destination y size.
	 */
	public int getSizeY() {
		return sizeY;
	}
	
}
