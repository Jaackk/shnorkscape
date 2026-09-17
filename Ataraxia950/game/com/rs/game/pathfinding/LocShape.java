package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 1:42:43 PM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public enum LocShape {
	/* walls */
	STRAIGHT_WALL(0, 0),
	DIAGONAL_WALL_XOFFSET(1, 0),
	CORNER_WALL(2, 0),
	DIAGONAL_WALL_ZOFFSET(3, 0),
	/* wall decorations */
	UNKNOWN(9, 2),
	WALL_DECOR_STRAIGHT_XOFFSET(4, 1),
	WALL_DECOR_STRAIGHT_ZOFFSET(5, 1),
	WALL_DECOR_DIAGONAL_XOFFSET(6, 1),
	WALL_DECOR_DIAGONAL_ZOFFSET(7, 1),
	WALL_DECOR_DIAGONAL_INTERIOR(8, 1),
	/* roof tops */
	ROOF_TOP_SIDE(12, 2),
	ROOF_TOP_CORNER_FLAT(13, 2),
	ROOF_TOP_FLAT_DOWNWARD_CREASE(14, 2),
	ROOF_TOP_SLANTED_UPWARD_CREASE(15, 2),
	ROOF_TOP_SLANTED_DOWNWARD_CREASE(16, 2),
	ROOF_TOP_FLAT(17, 2),
	/* roof edges */
	ROOF_EDGE(18, 2),
	ROOF_EDGE_CORNER_FLAT(19, 2),
	ROOF_CONNECTING_EDGE(20, 2),
	ROOF_EDGE_CORNER_POINTED(21, 2),
	/* ground objects */
	GROUND_OBJECT(10, 2),
	GROUND_RECTANGULAR_OBJECT(11, 2),
	GROUND_DECORATION(22, 3);
	
	/**
	 * The shape id.
	 */
	private final int id;
	
	/**
	 * The shape slot.
	 */
	private final int slot;
	
	/**
	 * Construct a new {@link LocShape} object instance.
	 * 
	 * @param id
	 *            the shape id.
	 * @param slot
	 *            the shape slot.
	 */
    LocShape(int id, int slot) {
		this.id = id;
		this.slot = slot;
	}
	
	/**
	 * Gets the shape id.
	 * 
	 * @return the shape id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the shape slot.
	 * 
	 * @return the shape slot.
	 */
	public int getSlot() {
		return slot;
	}
	
	/**
	 * Gets the location shape by the specified {@code id}.
	 * 
	 * @param id
	 *            the shape id.
	 * @return the {@link LocShape} with the specified id.
	 */
	public static LocShape forId(int id) {
		for(LocShape shape : values())
			if(shape.id == id)
				return shape;
		return null;
	}
	
	/**
	 * Checks if the shape is a roof top shape or not.
	 * 
	 * @param shape
	 *            the shape to check.
	 * @return <code>true</code> if it was otherwise <code>false</code>.
	 */
	public static boolean isRoofTop(LocShape shape) {
		return shape.ordinal() >= ROOF_TOP_SIDE.ordinal() && shape.ordinal() <= ROOF_TOP_FLAT.ordinal();
	}
	
	/**
	 * Checks if the shape is a roof edge shape or not.
	 * 
	 * @param shape
	 *            the shape to check.
	 * 
	 * @return <code>true</code> if it was otherwise <code>false</code>.
	 */
	public static boolean isRoofEdge(LocShape shape) {
		return shape.ordinal() >= ROOF_EDGE.ordinal() && shape.ordinal() <= ROOF_EDGE_CORNER_POINTED.ordinal();
	}
	
	/**
	 * Checks if the shape is a wall decoration shape or not.
	 * 
	 * @param shape
	 *            the shape to check.
	 * @return <code>true</code> if it was otherwise <code>false</code>.
	 */
	public static boolean isWallDecor(LocShape shape) {
		return shape.ordinal() >= WALL_DECOR_STRAIGHT_XOFFSET.ordinal() && shape.ordinal() <= WALL_DECOR_DIAGONAL_INTERIOR.ordinal();
	}
	
	/**
	 * Checks if the given shape is a ground location shape or not.
	 * 
	 * @param shape
	 *            the shape to check.
	 * @return <code>true</code> if it was otherwise <code>false</code>.
	 */
	public static boolean isGroundLoc(LocShape shape) {
		return shape.ordinal() >= GROUND_OBJECT.ordinal() && shape.ordinal() <= GROUND_RECTANGULAR_OBJECT.ordinal();
	}
	
}
