package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 3:21:34 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class LocationPathStrategy extends PathStrategy {
	
	/**
	 * The location shape.
	 */
	private final LocShape shape;
	
	/**
	 * The location face rotation.
	 */
	private final int rotation;
	
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
	 * @param shape
	 *            the location shape.
	 * @param rotation
	 *            the location rotation.
	 */
	public LocationPathStrategy(int x, int y, int sizeX, int sizeY, LocShape shape, int rotation) {
		super(x, y, sizeX, sizeY);
		this.shape = shape;
		this.rotation = rotation;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs2.server.game.map.path.strategy.PathStrategy#canExit(int,
	 * int, int, com.rs2.server.game.map.CollisionMap)
	 */
	@Override
	public boolean canExit(int size, int x, int y, CollisionMap collision) {
		return collision.canLocationExit(x, y, size, this.x, this.y, shape, rotation);
	}
	
	/**
	 * Gets the location shape
	 * 
	 * @return the location shape.
	 */
	public LocShape getShape() {
		return shape;
	}
	
	/**
	 * Gets the location rotation.
	 * 
	 * @return the location rotation.
	 */
	public int getRotation() {
		return rotation;
	}
	
}
