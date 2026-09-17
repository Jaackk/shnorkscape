package com.rs.game.pathfinding;

/**
 * Created at: Jan 21, 2017 10:43:57 AM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class CollisionMap {
	
	/**
	 * The object is facing the west direction.
	 */
	public static final int FACE_WEST = 0;
	
	/**
	 * The object is facing the north direction.
	 */
	public static final int FACE_NORTH = 1;
	
	/**
	 * The object is facing the east direction.
	 */
	public static final int FACE_EAST = 2;
	
	/**
	 * The object is facing the south direction.
	 */
	public static final int FACE_SOUTH = 3;
	
	/**
	 * The regular point object flag.
	 */
	public static final int FLAG_OBJECT = 0x100;
	
	/**
	 * The blocked flag.
	 */
	public static final int FLAG_BLOCKED = 0x200000;
	
	/**
	 * The blocked object flag.
	 */
	public static final int FLAG_OBJECT_BLOCK = 0x40000000;
	
	/**
	 * The decoration object flag.
	 */
	public static final int FLAG_DECORATION = 0x40000;
	
	/**
	 * The object is a collision object.
	 */
	public static final int FLAG_OBJECT_COLLISION = FLAG_OBJECT_BLOCK | FLAG_BLOCKED | FLAG_DECORATION;
	
	/**
	 * The projectile block flag.
	 */
	public static final int FLAG_PROJECTILE_BLOCK = 0x20000;
	
	/**
	 * The west wall object flag.
	 */
	public static final int FLAG_WALL_WEST = 0x80;
	
	/**
	 * The north wall object flag.
	 */
	public static final int FLAG_WALL_NORTH = 0x2;
	
	/**
	 * The east wall object flag.
	 */
	public static final int FLAG_WALL_EAST = 0x8;
	
	/**
	 * The south wall object flag.
	 */
	public static final int FLAG_WALL_SOUTH = 0x20;
	
	/**
	 * The west diagonal wall flag.
	 */
	public static final int FLAG_DIAGONAL_WALL_WEST = 0x1;
	
	/**
	 * The north diagonal wall flag.
	 */
	public static final int FLAG_DIAGONAL_WALL_NORTH = 0x4;
	
	/**
	 * The east diagonal wall flag.
	 */
	public static final int FLAG_DIAGONAL_WALL_EAST = 0x10;
	
	/**
	 * The south diagonal wall flag.
	 */
	public static final int FLAG_DIAGONAL_WALL_SOUTH = 0x40;
	
	/**
	 * The solid wall north flag.
	 */
	public static final int FLAG_SOLID_WALL_NORTH = 0x400;
	
	/**
	 * The solid wall east flag.
	 */
	public static final int FLAG_SOLID_WALL_EAST = 0x1000;
	
	/**
	 * The solid wall south flag.
	 */
	public static final int FLAG_SOLID_WALL_SOUTH = 0x4000;
	
	/**
	 * The solid wall west flag.
	 */
	public static final int FLAG_SOLID_WALL_WEST = 0x10000;
	
	/**
	 * The solid diagonal wall west flag.
	 */
	public static final int FLAG_SOLID_DIAGONAL_WALL_WEST = 0x200;
	
	/**
	 * The solid diagonal wall north flag.
	 */
	public static final int FLAG_SOLID_DIAGONAL_WALL_NORTH = 0x800;
	
	/**
	 * The solid diagonal wall east flag.
	 */
	public static final int FLAG_SOLID_DIAGONAL_WALL_EAST = 0x2000;
	
	/**
	 * The solid wall south flag.
	 */
	public static final int FLAG_SOLID_DIAGONAL_WALL_SOUTH = 0x8000;
	
	/**
	 * The wall north block flag.
	 */
	public static final int FLAG_BLOCK_WALL_NORTH = 0x800000;
	
	/**
	 * The wall east block flag.
	 */
	public static final int FLAG_BLOCK_WALL_EAST = 0x2000000;
	
	/**
	 * The wall south block flag.
	 */
	public static final int FLAG_BLOCK_WALL_SOUTH = 0x8000000;
	
	/**
	 * The wall west block flag.
	 */
	public static final int FLAG_BLOCK_WALL_WEST = 0x20000000;
	
	/**
	 * The diagonal wall north block flag.
	 */
	public static final int FLAG_BLOCK_DIAGONAL_WALL_NORTH = 0x1000000;
	
	/**
	 * The diagonal wall east block flag.
	 */
	public static final int FLAG_BLOCK_DIAGONAL_WALL_EAST = 0x4000000;
	
	/**
	 * The diagonal wall south block flag.
	 */
	public static final int FLAG_BLOCK_DIAGONAL_WALL_SOUTH = 0x10000000;
	
	/**
	 * The diagonal wall west block flag.
	 */
	public static final int FLAG_BLOCK_DIAGONAL_WALL_WEST = 0x400000;
	
	/**
	 * The route north block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_NORTH = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_NORTH;
	
	/**
	 * The route east block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_EAST = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_EAST;
	
	/**
	 * The route south block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_SOUTH = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_SOUTH;
	
	/**
	 * The route west block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_WEST = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_WEST;
	
	/**
	 * The route diagonal north block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_DIAGONAL_NORTH = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_ROUTE_BLOCK_NORTH | FLAG_ROUTE_BLOCK_EAST | FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_EAST | FLAG_BLOCK_DIAGONAL_WALL_NORTH;
	
	/**
	 * The route diagonal east block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_DIAGONAL_EAST = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_ROUTE_BLOCK_SOUTH | FLAG_ROUTE_BLOCK_EAST | FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_EAST | FLAG_BLOCK_DIAGONAL_WALL_EAST;
	
	/**
	 * The route diagonal south block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_ROUTE_BLOCK_SOUTH | FLAG_ROUTE_BLOCK_WEST | FLAG_BLOCK_WALL_WEST | FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_DIAGONAL_WALL_SOUTH;
	
	/**
	 * The route diagonal west block flag.
	 */
	public static final int FLAG_ROUTE_BLOCK_DIAGONAL_WEST = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_ROUTE_BLOCK_NORTH | FLAG_ROUTE_BLOCK_WEST | FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_WEST | FLAG_BLOCK_DIAGONAL_WALL_WEST;
	
	// TODO: refactor the below ones.
	public static final int FLAG_UNKNOWN = 0x80000;
	public static final int FLAG_UNKNOWN_NORTH = FLAG_OBJECT | FLAG_BLOCKED | FLAG_DECORATION | FLAG_WALL_NORTH | FLAG_UNKNOWN;
	public static final int FLAG_UNKNOWN_EAST = FLAG_OBJECT | FLAG_BLOCKED | FLAG_DECORATION | FLAG_WALL_EAST | FLAG_UNKNOWN;
	public static final int FLAG_UNKNOWN_SOUTH = FLAG_OBJECT | FLAG_BLOCKED | FLAG_DECORATION | FLAG_WALL_SOUTH | FLAG_UNKNOWN;
	public static final int FLAG_UNKNOWN_WEST = FLAG_OBJECT | FLAG_BLOCKED | FLAG_DECORATION | FLAG_WALL_WEST | FLAG_UNKNOWN;
	public static final int anInt3226 = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_EAST | FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_DIAGONAL_WALL_NORTH | FLAG_BLOCK_DIAGONAL_WALL_EAST | FLAG_ROUTE_BLOCK_NORTH | FLAG_ROUTE_BLOCK_EAST | FLAG_ROUTE_BLOCK_SOUTH | FLAG_ROUTE_BLOCK_DIAGONAL_NORTH | FLAG_ROUTE_BLOCK_DIAGONAL_EAST;
	public static final int anInt3225 = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_WEST | FLAG_ROUTE_BLOCK_NORTH | FLAG_ROUTE_BLOCK_SOUTH | FLAG_ROUTE_BLOCK_WEST | FLAG_BLOCK_DIAGONAL_WALL_SOUTH | FLAG_BLOCK_DIAGONAL_WALL_WEST | FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH | FLAG_ROUTE_BLOCK_DIAGONAL_WEST;
	public static final int anInt3228 = FLAG_BLOCKED | FLAG_DECORATION | FLAG_OBJECT_BLOCK | FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_EAST | FLAG_BLOCK_WALL_WEST | FLAG_BLOCK_DIAGONAL_WALL_SOUTH | FLAG_BLOCK_DIAGONAL_WALL_EAST | FLAG_ROUTE_BLOCK_SOUTH | FLAG_ROUTE_BLOCK_EAST | FLAG_ROUTE_BLOCK_WEST | FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH | FLAG_ROUTE_BLOCK_DIAGONAL_EAST;
	public static final int anInt3231 = FLAG_BLOCKED | FLAG_ROUTE_BLOCK_WEST | FLAG_BLOCK_DIAGONAL_WALL_NORTH | FLAG_DECORATION | FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_DIAGONAL_WALL_WEST | FLAG_BLOCK_WALL_EAST | FLAG_BLOCK_WALL_WEST | FLAG_OBJECT_BLOCK | FLAG_ROUTE_BLOCK_NORTH | FLAG_ROUTE_BLOCK_DIAGONAL_NORTH | FLAG_ROUTE_BLOCK_EAST | FLAG_ROUTE_BLOCK_DIAGONAL_WEST;
	
	/**
	 * The map width.
	 */
	private final int width;
	
	/**
	 * The map height.
	 */
	private final int height;
	
	/**
	 * The map flags.
	 */
	private final int[][] flags;
	
	/**
	 * Construct a new {@link CollisionMap} object instance.
	 * 
	 * @param width
	 *            the map width.
	 * @param height
	 *            the map height.
	 */
	public CollisionMap(int width, int height) {
		this.width = width;
		this.height = height;
		this.flags = new int[width][height];
		reset();
	}
	
	/**
	 * Adds the {@link #FLAG_BLOCKED} flag to the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x to add to.
	 * @param y
	 *            the coordinate y to add to.
	 */
	public void flagSolid(int x, int y) {
		flag(x, y, FLAG_BLOCKED);
	}
	
	/**
	 * Removes the {@link #FLAG_BLOCKED} flag from the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x to remove from.
	 * @param y
	 *            the coordinate y to remove from.
	 */
	public void unflagSolid(int x, int y) {
		unflag(x, y, FLAG_BLOCKED);
	}
	
	/**
	 * Flags an object at the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @param sizeX
	 *            the object x-size.
	 * @param sizeY
	 *            the object y-size.
	 * @param blockProjectile
	 *            does the object block the going through projectiles?
	 * @param blockAlternative
	 *            does the object block the alternative mode?
	 */
	public void flagObject(int x, int y, int sizeX, int sizeY, boolean blockProjectile, boolean blockAlternative) {
		int flag = FLAG_OBJECT;
		if (blockProjectile) {
			flag |= FLAG_PROJECTILE_BLOCK;
		}
		if (blockAlternative) {
			flag |= FLAG_OBJECT_BLOCK;
		}
		for (int placeX = x; placeX < x + sizeX; placeX++) {
			if (placeX >= 0 && placeX < width) {
				for (int placeY = y; placeY < y + sizeY; placeY++) {
					if (placeY >= 0 && placeY < height) {
						flag(placeX, placeY, flag);
					}
				}
			}
		}
	}
	
	/**
	 * Removes the object flag at the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @param sizeX
	 *            the object x-size.
	 * @param sizeY
	 *            the object y-size.
	 * @param blockProjectile
	 *            does the object block the going through projectiles?
	 * @param blockAlternative
	 *            does the object block the alternative mode?
	 */
	public void unflagObject(int x, int y, int sizeX, int sizeY, int face, boolean blockProjectile, boolean blockAlternative) {
		int flag = FLAG_OBJECT;
		if (blockProjectile) {
			flag |= FLAG_PROJECTILE_BLOCK;
		}
		if (blockAlternative) {
			flag |= FLAG_OBJECT_BLOCK;
		}
		if (face == FACE_NORTH || face == FACE_SOUTH) {
			int _sizeX = sizeX;
			sizeX = sizeY;
			sizeY = _sizeX;
		}
		for (int placeX = x; placeX < x + sizeX; placeX++) {
			if (placeX >= 0 && placeX < width) {
				for (int placeY = y; placeY < y + sizeY; placeY++) {
					if (placeY >= 0 && placeY < height) {
						unflag(placeX, placeY, flag);
					}
				}
			}
		}
	}
	
	/**
	 * Flags a decoration object at the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 */
	public void flagDecoration(int x, int y) {
		flag(x, y, FLAG_DECORATION);
	}
	
	/**
	 * Removes the decoration object flag at the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 */
	public void unflagDecoration(int x, int y) {
		unflag(x, y, FLAG_DECORATION);
	}
	
	/**
	 * Flags wall at the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @param shape
	 *            the wall shape.
	 * @param face
	 *            the wall face rotation.
	 * @param solid
	 *            is solid wall
	 * @param alternative
	 *            has alternative mode?
	 */
	public void flagWall(int x, int y, LocShape shape, int face, boolean solid, boolean alternative) {
		if (shape == LocShape.STRAIGHT_WALL) {
			if (face == FACE_WEST) {
				flag(x, y, FLAG_WALL_WEST);
				flag(x - 1, y, FLAG_WALL_EAST);
			}
			if (face == FACE_NORTH) {
				flag(x, y, FLAG_WALL_NORTH);
				flag(x, y + 1, FLAG_WALL_SOUTH);
			}
			if (face == FACE_EAST) {
				flag(x, y, FLAG_WALL_EAST);
				flag(x + 1, y, FLAG_WALL_WEST);
			}
			if (face == FACE_SOUTH) {
				flag(x, y, FLAG_WALL_SOUTH);
				flag(x, y - 1, FLAG_WALL_NORTH);
			}
		}
		if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
			if (face == FACE_WEST) {
				flag(x, y, FLAG_DIAGONAL_WALL_WEST);
				flag(x - 1, y + 1, FLAG_DIAGONAL_WALL_EAST);
			}
			if (face == FACE_NORTH) {
				flag(x, y, FLAG_DIAGONAL_WALL_NORTH);
				flag(x + 1, y + 1, FLAG_DIAGONAL_WALL_SOUTH);
			}
			if (face == FACE_EAST) {
				flag(x, y, FLAG_DIAGONAL_WALL_EAST);
				flag(x + 1, y - 1, FLAG_DIAGONAL_WALL_WEST);
			}
			if (face == FACE_SOUTH) {
				flag(x, y, FLAG_DIAGONAL_WALL_SOUTH);
				flag(x - 1, y - 1, FLAG_DIAGONAL_WALL_NORTH);
			}
		}
		if (shape == LocShape.CORNER_WALL) {
			if (face == FACE_WEST) {
				flag(x, y, FLAG_WALL_NORTH | FLAG_WALL_WEST);
				flag(x - 1, y, FLAG_WALL_EAST);
				flag(x, y + 1, FLAG_WALL_SOUTH);
			}
			if (face == FACE_NORTH) {
				flag(x, y, FLAG_WALL_NORTH | FLAG_WALL_EAST);
				flag(x, y + 1, FLAG_WALL_SOUTH);
				flag(x + 1, y, FLAG_WALL_WEST);
			}
			if (face == FACE_EAST) {
				flag(x, y, FLAG_WALL_SOUTH | FLAG_WALL_EAST);
				flag(x + 1, y, FLAG_WALL_WEST);
				flag(x, y - 1, FLAG_WALL_NORTH);
			}
			if (face == FACE_SOUTH) {
				flag(x, y, FLAG_WALL_SOUTH | FLAG_WALL_WEST);
				flag(x, y - 1, FLAG_WALL_NORTH);
				flag(x - 1, y, FLAG_WALL_EAST);
			}
		}
		if (solid) {
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_SOLID_WALL_WEST);
					flag(x - 1, y, FLAG_SOLID_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_SOLID_WALL_NORTH);
					flag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_SOLID_WALL_EAST);
					flag(x + 1, y, FLAG_SOLID_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_SOLID_WALL_SOUTH);
					flag(x, y - 1, FLAG_SOLID_WALL_NORTH);
				}
			}
			if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_SOLID_DIAGONAL_WALL_WEST);
					flag(x - 1, y + 1, FLAG_SOLID_DIAGONAL_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_SOLID_DIAGONAL_WALL_NORTH);
					flag(x + 1, y + 1, FLAG_SOLID_DIAGONAL_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_SOLID_DIAGONAL_WALL_EAST);
					flag(x + 1, y - 1, FLAG_SOLID_DIAGONAL_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_SOLID_DIAGONAL_WALL_SOUTH);
					flag(x - 1, y - 1, FLAG_SOLID_DIAGONAL_WALL_NORTH);
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_SOLID_WALL_NORTH | FLAG_SOLID_WALL_WEST);
					flag(x - 1, y, FLAG_SOLID_WALL_EAST);
					flag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_SOLID_WALL_NORTH | FLAG_SOLID_WALL_EAST);
					flag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
					flag(x + 1, y, FLAG_SOLID_WALL_WEST);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_SOLID_WALL_SOUTH | FLAG_SOLID_WALL_EAST);
					flag(x + 1, y, FLAG_SOLID_WALL_WEST);
					flag(x, y - 1, FLAG_SOLID_WALL_NORTH);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_SOLID_WALL_SOUTH | FLAG_SOLID_WALL_WEST);
					flag(x, y - 1, FLAG_SOLID_WALL_NORTH);
					flag(x - 1, y, FLAG_SOLID_WALL_EAST);
				}
			}
		}
		if (alternative) {
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_BLOCK_WALL_WEST);
					flag(x - 1, y, FLAG_BLOCK_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_BLOCK_WALL_NORTH);
					flag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_BLOCK_WALL_EAST);
					flag(x + 1, y, FLAG_BLOCK_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_BLOCK_WALL_SOUTH);
					flag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
				}
			}
			if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_BLOCK_DIAGONAL_WALL_WEST);
					flag(x - 1, y + 1, FLAG_BLOCK_DIAGONAL_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_BLOCK_DIAGONAL_WALL_NORTH);
					flag(x + 1, y + 1, FLAG_BLOCK_DIAGONAL_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_BLOCK_DIAGONAL_WALL_EAST);
					flag(x + 1, y - 1, FLAG_BLOCK_DIAGONAL_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_BLOCK_DIAGONAL_WALL_SOUTH);
					flag(x - 1, y - 1, FLAG_BLOCK_DIAGONAL_WALL_NORTH);
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					flag(x, y, FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_WEST);
					flag(x - 1, y, FLAG_BLOCK_WALL_EAST);
					flag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
				}
				if (face == FACE_NORTH) {
					flag(x, y, FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_EAST);
					flag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
					flag(x + 1, y, FLAG_BLOCK_WALL_WEST);
				}
				if (face == FACE_EAST) {
					flag(x, y, FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_EAST);
					flag(x + 1, y, FLAG_BLOCK_WALL_WEST);
					flag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
				}
				if (face == FACE_SOUTH) {
					flag(x, y, FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_WEST);
					flag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
					flag(x - 1, y, FLAG_BLOCK_WALL_EAST);
				}
			}
		}
	}
	
	/**
	 * Removes the wall flag from the specified coordinate.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @param shape
	 *            the wall shape.
	 * @param face
	 *            the wall face rotation.
	 * @param solid
	 *            is solid wall
	 * @param alternative
	 *            has alternative mode?
	 */
	public void unflagWall(int x, int y, LocShape shape, int face, boolean solid, boolean alternative) {
		if (shape == LocShape.STRAIGHT_WALL) {
			if (face == FACE_WEST) {
				unflag(x, y, FLAG_WALL_WEST);
				unflag(x - 1, y, FLAG_WALL_EAST);
			}
			if (face == FACE_NORTH) {
				unflag(x, y, FLAG_WALL_NORTH);
				unflag(x, y + 1, FLAG_WALL_SOUTH);
			}
			if (face == FACE_EAST) {
				unflag(x, y, FLAG_WALL_EAST);
				unflag(x + 1, y, FLAG_WALL_WEST);
			}
			if (face == FACE_SOUTH) {
				unflag(x, y, FLAG_WALL_SOUTH);
				unflag(x, y - 1, FLAG_WALL_NORTH);
			}
		}
		if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
			if (face == FACE_WEST) {
				unflag(x, y, FLAG_DIAGONAL_WALL_WEST);
				unflag(x - 1, y + 1, FLAG_DIAGONAL_WALL_EAST);
			}
			if (face == FACE_NORTH) {
				unflag(x, y, FLAG_DIAGONAL_WALL_NORTH);
				unflag(x + 1, y + 1, FLAG_DIAGONAL_WALL_SOUTH);
			}
			if (face == FACE_EAST) {
				unflag(x, y, FLAG_DIAGONAL_WALL_EAST);
				unflag(x + 1, y - 1, FLAG_DIAGONAL_WALL_WEST);
			}
			if (face == FACE_SOUTH) {
				unflag(x, y, FLAG_DIAGONAL_WALL_SOUTH);
				unflag(x - 1, y - 1, FLAG_DIAGONAL_WALL_NORTH);
			}
		}
		if (shape == LocShape.CORNER_WALL) {
			if (face == FACE_WEST) {
				unflag(x, y, FLAG_WALL_NORTH | FLAG_WALL_WEST);
				unflag(x - 1, y, FLAG_WALL_EAST);
				unflag(x, y + 1, FLAG_WALL_SOUTH);
			}
			if (face == FACE_NORTH) {
				unflag(x, y, FLAG_WALL_NORTH | FLAG_WALL_EAST);
				unflag(x, y + 1, FLAG_WALL_SOUTH);
				unflag(x + 1, y, FLAG_WALL_WEST);
			}
			if (face == FACE_EAST) {
				unflag(x, y, FLAG_WALL_SOUTH | FLAG_WALL_EAST);
				unflag(x + 1, y, FLAG_WALL_WEST);
				unflag(x, y - 1, FLAG_WALL_NORTH);
			}
			if (face == FACE_SOUTH) {
				unflag(x, y, FLAG_WALL_SOUTH | FLAG_WALL_WEST);
				unflag(x, y - 1, FLAG_WALL_NORTH);
				unflag(x - 1, y, FLAG_WALL_EAST);
			}
		}
		if (solid) {
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_SOLID_WALL_WEST);
					unflag(x - 1, y, FLAG_SOLID_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_SOLID_WALL_NORTH);
					unflag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_SOLID_WALL_EAST);
					unflag(x + 1, y, FLAG_SOLID_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_SOLID_WALL_SOUTH);
					unflag(x, y - 1, FLAG_SOLID_WALL_NORTH);
				}
			}
			if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_SOLID_DIAGONAL_WALL_WEST);
					unflag(x - 1, y + 1, FLAG_SOLID_DIAGONAL_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_SOLID_DIAGONAL_WALL_NORTH);
					unflag(x + 1, y + 1, FLAG_SOLID_DIAGONAL_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_SOLID_DIAGONAL_WALL_EAST);
					unflag(x + 1, y - 1, FLAG_SOLID_DIAGONAL_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_SOLID_DIAGONAL_WALL_SOUTH);
					unflag(x - 1, y - 1, FLAG_SOLID_DIAGONAL_WALL_NORTH);
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_SOLID_WALL_WEST | FLAG_SOLID_WALL_NORTH);
					unflag(x - 1, y, FLAG_SOLID_WALL_EAST);
					unflag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_SOLID_WALL_NORTH | FLAG_SOLID_WALL_EAST);
					unflag(x, y + 1, FLAG_SOLID_WALL_SOUTH);
					unflag(x + 1, y, FLAG_SOLID_WALL_WEST);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_SOLID_WALL_SOUTH | FLAG_SOLID_WALL_EAST);
					unflag(x + 1, y, FLAG_SOLID_WALL_WEST);
					unflag(x, y - 1, FLAG_SOLID_WALL_NORTH);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_SOLID_WALL_SOUTH | FLAG_SOLID_WALL_WEST);
					unflag(x, y - 1, FLAG_SOLID_WALL_NORTH);
					unflag(x - 1, y, FLAG_SOLID_WALL_EAST);
				}
			}
		}
		if (alternative) {
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_BLOCK_WALL_WEST);
					unflag(x - 1, y, FLAG_BLOCK_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_BLOCK_WALL_NORTH);
					unflag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_BLOCK_WALL_EAST);
					unflag(x + 1, y, FLAG_BLOCK_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_BLOCK_WALL_SOUTH);
					unflag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
				}
			}
			if (shape == LocShape.DIAGONAL_WALL_XOFFSET || shape == LocShape.DIAGONAL_WALL_ZOFFSET) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_BLOCK_DIAGONAL_WALL_WEST);
					unflag(x - 1, y + 1, FLAG_BLOCK_DIAGONAL_WALL_EAST);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_BLOCK_DIAGONAL_WALL_NORTH);
					unflag(x + 1, y + 1, FLAG_BLOCK_DIAGONAL_WALL_SOUTH);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_BLOCK_DIAGONAL_WALL_EAST);
					unflag(x + 1, y - 1, FLAG_BLOCK_DIAGONAL_WALL_WEST);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_BLOCK_DIAGONAL_WALL_SOUTH);
					unflag(x - 1, y - 1, FLAG_BLOCK_DIAGONAL_WALL_NORTH);
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					unflag(x, y, FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_WEST);
					unflag(x - 1, y, FLAG_BLOCK_WALL_EAST);
					unflag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
				}
				if (face == FACE_NORTH) {
					unflag(x, y, FLAG_BLOCK_WALL_NORTH | FLAG_BLOCK_WALL_EAST);
					unflag(x, y + 1, FLAG_BLOCK_WALL_SOUTH);
					unflag(x + 1, y, FLAG_BLOCK_WALL_WEST);
				}
				if (face == FACE_EAST) {
					unflag(x, y, FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_EAST);
					unflag(x + 1, y, FLAG_BLOCK_WALL_WEST);
					unflag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
				}
				if (face == FACE_SOUTH) {
					unflag(x, y, FLAG_BLOCK_WALL_SOUTH | FLAG_BLOCK_WALL_WEST);
					unflag(x, y - 1, FLAG_BLOCK_WALL_NORTH);
					unflag(x - 1, y, FLAG_BLOCK_WALL_EAST);
				}
			}
		}
	}
	
	/**
	 * Adds the given {@code flag} to the specified coordinate.
	 * 
	 * @param x
	 *            the position x to add at.
	 * @param y
	 *            the position y to add at.
	 * @param flag
	 *            the flag to add.F
	 */
	public void flag(int x, int y, int flag) {
		if(x < 0 ||x  >= 64 || y < 1 || y >= 64)
			return;
		flags[x][y] |= flag;
	}
	
	/**
	 * Removes the {@code flag} from the specified coordinate.
	 * 
	 * @param x
	 *            the position x to remove from.
	 * @param y
	 *            the position y to remove from.
	 * @param flag
	 *            the flag to remove.
	 */
	public void unflag(int x, int y, int mask) {
		flags[x][y] &= ~mask;
	}
	
	/**
	 * Gets the flag of the specified point.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @return the flag of the given point.
	 */
	public int get(int x, int y) {
		return flags[x][y];
	}
	
	/**
	 * Sets the flag of the specified coordiante.
	 * 
	 * @param x
	 *            the coordinate x position.
	 * @param y
	 *            the coordinate y position.
	 * @param flag
	 *            the flag to set.
	 */
	public void set(int x, int y, int flag) {
		flags[x][y] = flag;
	}
	
	/**
	 * Checks if we can exit at the specified location object.
	 * 
	 * @param x
	 *            the location position x.
	 * @param y
	 *            the location position y.
	 * @param size
	 *            the location size.
	 * @param dstX
	 *            the destination position x.
	 * @param dstY
	 *            the destination position y.
	 * @param shape
	 *            the location shape.
	 * @param face
	 *            the location face rotation.
	 * @return <code>true</code> if we can exit otherwise <code>false</code>.
	 */
	public boolean canLocationExit(int x, int y, int size, int dstX, int dstY, LocShape shape, int face) {
		if (1 == size) {
			if (x == dstX && y == dstY) {
				return true;
			}
		} else if (dstX >= x && dstX <= size + x - 1 && dstY >= dstY && dstY <= size + dstY - 1) {
			return true;
		}
		if (size == 1) {
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					if (dstX - 1 == x && y == dstY) {
						return true;
					}
					if (x == dstX && 1 + dstY == y && (flags[x][y] & FLAG_UNKNOWN_SOUTH) == 0) {
						return true;
					}
					if (x == dstX && dstY - 1 == y && (flags[x][y] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_NORTH) {
					if (dstX == x && 1 + dstY == y) {
						return true;
					}
					if (dstX - 1 == x && dstY == y && (flags[x][y] & FLAG_UNKNOWN_EAST) == 0) {
						return true;
					}
					if (dstX + 1 == x && y == dstY && 0 == (flags[x][y] & FLAG_UNKNOWN_WEST)) {
						return true;
					}
				} else if (face == FACE_EAST) {
					if (x == dstX + 1 && dstY == y) {
						return true;
					}
					if (dstX == x && 1 + dstY == y && 0 == (flags[x][y] & FLAG_UNKNOWN_SOUTH)) {
						return true;
					}
					if (dstX == x && dstY - 1 == y && (flags[x][y] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_SOUTH) {
					if (x == dstX && y == dstY - 1) {
						return true;
					}
					if (x == dstX - 1 && dstY == y && (flags[x][y] & FLAG_UNKNOWN_EAST) == 0) {
						return true;
					}
					if (x == dstX + 1 && y == dstY && (flags[x][y] & FLAG_UNKNOWN_WEST) == 0) {
						return true;
					}
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					if (x == dstX - 1 && dstY == y) {
						return true;
					}
					if (dstX == x && y == 1 + dstY) {
						return true;
					}
					if (dstX + 1 == x && dstY == y && (flags[x][y] & FLAG_UNKNOWN_WEST) == 0) {
						return true;
					}
					if (x == dstX && y == dstY - 1 && 0 == (flags[x][y] & FLAG_UNKNOWN_NORTH)) {
						return true;
					}
				} else if (face == FACE_NORTH) {
					if (dstX - 1 == x && y == dstY && 0 == (flags[x][y] & FLAG_UNKNOWN_EAST)) {
						return true;
					}
					if (x == dstX && 1 + dstY == y) {
						return true;
					}
					if (x == dstX + 1 && dstY == y) {
						return true;
					}
					if (dstX == x && y == dstY - 1 && (flags[x][y] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_EAST) {
					if (dstX - 1 == x && dstY == y && (flags[x][y] & FLAG_UNKNOWN_EAST) == 0) {
						return true;
					}
					if (x == dstX && y == 1 + dstY && 0 == (flags[x][y] & FLAG_UNKNOWN_SOUTH)) {
						return true;
					}
					if (x == dstX + 1 && y == dstY) {
						return true;
					}
					if (dstX == x && y == dstY - 1) {
						return true;
					}
				} else if (face == FACE_SOUTH) {
					if (x == dstX - 1 && dstY == y) {
						return true;
					}
					if (x == dstX && 1 + dstY == y && (flags[x][y] & FLAG_UNKNOWN_SOUTH) == 0) {
						return true;
					}
					if (dstX + 1 == x && dstY == y && (flags[x][y] & FLAG_UNKNOWN_WEST) == 0) {
						return true;
					}
					if (x == dstX && y == dstY - 1) {
						return true;
					}
				}
			}
			if (shape == LocShape.UNKNOWN) {
				if (dstX == x && y == dstY + 1 && 0 == (flags[x][y] & FLAG_WALL_SOUTH)) {
					return true;
				}
				if (x == dstX && y == dstY - 1 && (flags[x][y] & FLAG_WALL_NORTH) == 0) {
					return true;
				}
				if (dstX - 1 == x && dstY == y && (flags[x][y] & FLAG_WALL_EAST) == 0) {
					return true;
				}
                return x == dstX + 1 && y == dstY && 0 == (flags[x][y] & FLAG_WALL_WEST);
			}
		} else {
			int endX = x + size - 1;
			int endY = y + size - 1;
			if (shape == LocShape.STRAIGHT_WALL) {
				if (face == FACE_WEST) {
					if (x == dstX - size && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == 1 + dstY && (flags[dstX][y] & FLAG_UNKNOWN_SOUTH) == 0) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY - size && (flags[dstX][endY] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_NORTH) {
					if (dstX >= x && dstX <= endX && y == 1 + dstY) {
						return true;
					}
					if (x == dstX - size && dstY >= y && dstY <= endY && (flags[endX][dstY] & FLAG_UNKNOWN_EAST) == 0) {
						return true;
					}
					if (x == dstX + 1 && dstY >= y && dstY <= endY && (flags[x][dstY] & FLAG_UNKNOWN_WEST) == 0) {
						return true;
					}
				} else if (face == FACE_EAST) {
					if (dstX + 1 == x && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY + 1 && 0 == (flags[dstX][y] & FLAG_UNKNOWN_SOUTH)) {
						return true;
					}
					if (dstX >= x && dstX <= endX && dstY - size == y && 0 == (flags[dstX][endY] & FLAG_UNKNOWN_NORTH)) {
						return true;
					}
				} else if (face == FACE_SOUTH) {
					if (dstX >= x && dstX <= endX && y == dstY - size) {
						return true;
					}
					if (x == dstX - size && dstY >= y && dstY <= endY && 0 == (flags[endX][dstY] & FLAG_UNKNOWN_EAST)) {
						return true;
					}
					if (x == dstX + 1 && dstY >= y && dstY <= endY && 0 == (flags[x][dstY] & FLAG_UNKNOWN_WEST)) {
						return true;
					}
				}
			}
			if (shape == LocShape.CORNER_WALL) {
				if (face == FACE_WEST) {
					if (x == dstX - size && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && 1 + dstY == y) {
						return true;
					}
					if (dstX + 1 == x && dstY >= y && dstY <= endY && 0 == (flags[x][dstY] & FLAG_UNKNOWN_WEST)) {
						return true;
					}
					if (dstX >= x && dstX <= endX && dstY - size == y && (flags[dstX][endY] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_NORTH) {
					if (x == dstX - size && dstY >= y && dstY <= endY && 0 == (flags[endX][dstY] & FLAG_UNKNOWN_EAST)) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY + 1) {
						return true;
					}
					if (x == dstX + 1 && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY - size && (flags[dstX][endY] & FLAG_UNKNOWN_NORTH) == 0) {
						return true;
					}
				} else if (face == FACE_EAST) {
					if (x == dstX - size && dstY >= y && dstY <= endY && (flags[endX][dstY] & FLAG_UNKNOWN_EAST) == 0) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY + 1 && 0 == (flags[dstX][y] & FLAG_UNKNOWN_SOUTH)) {
						return true;
					}
					if (x == dstX + 1 && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY - size) {
						return true;
					}
				} else if (face == FACE_SOUTH) {
					if (dstX - size == x && dstY >= y && dstY <= endY) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY + 1 && 0 == (flags[dstX][y] & FLAG_UNKNOWN_SOUTH)) {
						return true;
					}
					if (x == dstX + 1 && dstY >= y && dstY <= endY && 0 == (flags[x][dstY] & FLAG_UNKNOWN_WEST)) {
						return true;
					}
					if (dstX >= x && dstX <= endX && y == dstY - size) {
						return true;
					}
				}
			}
			if (shape == LocShape.UNKNOWN) {
				if (dstX >= x && dstX <= endX && y == dstY + 1 && (flags[dstX][y] & FLAG_UNKNOWN_SOUTH) == 0) {
					return true;
				}
				if (dstX >= x && dstX <= endX && dstY - size == y && (flags[dstX][endY] & FLAG_UNKNOWN_NORTH) == 0) {
					return true;
				}
				if (dstX - size == x && dstY >= y && dstY <= endY && (flags[endX][dstY] & FLAG_UNKNOWN_EAST) == 0) {
					return true;
				}
                return dstX + 1 == x && dstY >= y && dstY <= endY && (flags[x][dstY] & FLAG_UNKNOWN_WEST) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the NPC can exit at specific point.
	 * 
	 * @param x
	 *            the NPC position x.
	 * @param y
	 *            the NPC position y.
	 * @param sizeX
	 *            the NPC size
	 * @param dstX
	 *            the destination position x.
	 * @param dstY
	 *            the destination position y.
	 * @param dstSizeX
	 *            the destination size x.
	 * @param dstSizeY
	 *            the destination size y.
	 * @param flag
	 *            the
	 * @return <code>true</code> if it can exit otherwise <code>false</code>.
	 */
	public boolean canNPCExit(int x, int y, int size, int dstX, int dstY, int dstSizeX, int dstSizeY, int flag) {
		if (size > 1) {
			if (PathStrategy.canExitRectangle(x, y, size, size, dstX, dstY, dstSizeX, dstSizeY)) {
				return true;
			}
			return canPlayerExit(x, y, size, size, dstX, dstY, dstSizeX, dstSizeY, flag);
		}
		int endX = dstX + dstSizeX - 1;
		int endY = dstY + dstSizeY - 1;
		if (x >= dstX && x <= endX && y >= dstY && y <= endY) {
			return true;
		}
		if (x == dstX - 1 && y >= dstY && y <= endY && (flags[x][y] & FLAG_WALL_EAST) == 0 && 0 == (flag & FLAG_WALL_EAST)) {
			return true;
		}
		if (endX + 1 == x && y >= dstY && y <= endY && (flags[x][y] & FLAG_WALL_WEST) == 0 && (flag & FLAG_WALL_NORTH) == 0) {
			return true;
		}
		if (dstY - 1 == y && x >= dstX && x <= endX && 0 == (flags[x][y] & FLAG_WALL_NORTH) && (flag & FLAG_DIAGONAL_WALL_NORTH) == 0) {
			return true;
		}
        return y == endY + 1 && x >= dstX && x <= endX && 0 == (flags[x][y] & FLAG_WALL_SOUTH) && (flag & FLAG_DIAGONAL_WALL_WEST) == 0;
    }
	
	/**
	 * Checks if the player can exit at specific point.
	 * 
	 * @param x
	 *            the player position x.
	 * @param y
	 *            the player position y.
	 * @param sizeX
	 *            the player size x.
	 * @param sizeY
	 *            the player size y.
	 * @param dstX
	 *            the destination position x.
	 * @param dstY
	 *            the destination position y.
	 * @param dstSizeX
	 *            the destination size x.
	 * @param dstSizeY
	 *            the destination size y.
	 * @param flag
	 *            the
	 * @return <code>true</code> if it can exit otherwise <code>false</code>.
	 */
	public boolean canPlayerExit(int x, int y, int sizeX, int sizeY, int dstX, int dstY, int dstSizeX, int dstSizeY, int flag) {
		int startX = sizeX + x;
		int startY = sizeY + y;
		int endX = dstX + dstSizeX;
		int endY = dstY + dstSizeY;
		if (x == endX && (flag & FLAG_WALL_NORTH) == 0) {
			int maxY = y > dstY ? y : dstY;
			for (int pos = startY < endY ? startY : endY; maxY < pos; maxY++) {
				if ((flags[endX - 1][maxY] & FLAG_WALL_EAST) == 0) {
					return true;
				}
			}
		} else if (dstX == startX && (flag & FLAG_WALL_EAST) == 0) {
			int maxY = y > dstY ? y : dstY;
			for (int pos = startY < endY ? startY : endY; maxY < pos; maxY++) {
				if ((flags[dstX][maxY] & FLAG_WALL_WEST) == 0) {
					return true;
				}
			}
		} else if (y == endY && (flag & FLAG_DIAGONAL_WALL_WEST) == 0) {
			int maxX = x > dstX ? x : dstX;
			for (int pos = startX < endX ? startX : endX; maxX < pos; maxX++) {
				if ((flags[maxX][endY - 1] & FLAG_WALL_NORTH) == 0) {
					return true;
				}
			}
		} else if (startY == dstY && (flag & FLAG_DIAGONAL_WALL_NORTH) == 0) {
			int maxX = x > dstX ? x : dstX;
			for (int pos = startX < endX ? startX : endX; maxX < pos; maxX++) {
				if ((flags[maxX][dstY] & FLAG_WALL_SOUTH) == 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Resets the flags
	 */
	public void reset() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x >= width - 5 || y >= height - 5) {
					flags[x][y] = -1;
				} else {
					flags[x][y] = FLAG_BLOCKED;
				}
			}
		}
	}
	
	/**
	 * Gets the collision flags.
	 * 
	 * @return the collision flags.
	 */
	public int[][] getFlags() {
		return flags;
	}
}
