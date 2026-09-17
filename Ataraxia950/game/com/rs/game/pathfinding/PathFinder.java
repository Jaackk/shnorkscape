package com.rs.game.pathfinding;

import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_DIAGONAL_EAST;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_DIAGONAL_NORTH;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_DIAGONAL_WEST;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_EAST;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_NORTH;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_SOUTH;
import static com.rs.game.pathfinding.CollisionMap.FLAG_ROUTE_BLOCK_WEST;
import static com.rs.game.pathfinding.CollisionMap.anInt3225;
import static com.rs.game.pathfinding.CollisionMap.anInt3226;
import static com.rs.game.pathfinding.CollisionMap.anInt3228;
import static com.rs.game.pathfinding.CollisionMap.anInt3231;

/**
 * Created at: Jan 21, 2017 10:33:10 AM
 * 
 * @author Walied-Yassen A.k.A Cody
 */
public class PathFinder {
	
	/**
	 * The north direction flag.
	 */
	public static final int DIR_NORTH = 0x1;
	
	/**
	 * The east direction flag.
	 */
	public static final int DIR_EAST = 0x2;
	
	/**
	 * The south direction flag.
	 */
	public static final int DIR_SOUTH = 0x4;
	
	/**
	 * The west direction flag.
	 */
	public static final int DIR_WEST = 0x8;
	
	/**
	 * The finder graph size.
	 */
	private static final int GRAPH_SIZE = 128;
	
	/**
	 * The queue size.
	 */
	private static final int QUEUE_SIZE = GRAPH_SIZE * GRAPH_SIZE / 4;
	
	/**
	 * The alternative route search range.
	 */
	private static final int ALTERNATIVE_PATH_RANGE = 10;
	
	/**
	 * The alternative route max distance.
	 */
	private static final int ALTERNATIVE_PATH_MAX_DISTANCE = ALTERNATIVE_PATH_RANGE * ALTERNATIVE_PATH_RANGE;
	
	/**
	 * The distances buffer.
	 */
	private static final int[][] distances = new int[GRAPH_SIZE][GRAPH_SIZE];
	
	/**
	 * The directions buffer.
	 */
	private static final int[][] directions = new int[GRAPH_SIZE][GRAPH_SIZE];
	
	/**
	 * The x-position buffer.
	 */
	private static final int[] bufferX = new int[QUEUE_SIZE];
	
	/**
	 * The y-position buffer.
	 */
	private static final int[] bufferY = new int[QUEUE_SIZE];
	
	/**
	 * The exit x position.
	 */
	private static int exitX;
	
	/**
	 * The exit y position.
	 */
	private static int exitY;
	
	public static final int find(int srcX, int srcY, int size, PathStrategy strategy, CollisionMap collision, boolean alternative, int[] stepsX, int[] stepsY) {
		for (int x = 0; x < GRAPH_SIZE; x++) {
			for (int y = 0; y < GRAPH_SIZE; y++) {
				directions[x][y] = 0;
				distances[x][y] = 99999999;
			}
		}
		boolean success;
		if (size == 1) {
			success = calculateSmall(srcX, srcY, strategy, collision);
		} else if (size == 2) {
			success = calculateMedium(srcX, srcY, strategy, collision);
		} else {
			success = calculateLarge(srcX, srcY, size, strategy, collision);
		}
	
		int graphBaseX = srcX - GRAPH_SIZE / 2;
		int graphBaseY = srcY - GRAPH_SIZE / 2;
		int endX = exitX;
		int endY = exitY;
		if (!success) {
			if (alternative) {
				int lowestCost = Integer.MAX_VALUE;
				int lowestDistance = Integer.MAX_VALUE;
				int searchRange = ALTERNATIVE_PATH_RANGE;
				int dstX = strategy.getX();
				int dstY = strategy.getY();
				int dstSizeX = strategy.getSizeX();
				int dstSizeY = strategy.getSizeY();
				for (int checkX = dstX - searchRange; checkX <= dstX + searchRange; checkX++) {
					for (int checkY = dstY - searchRange; checkY <= searchRange + dstY; checkY++) {
						int graphX = checkX - graphBaseX;
						int graphY = checkY - graphBaseY;
						if (graphX >= 0 && graphY >= 0 && graphX < GRAPH_SIZE && graphY < GRAPH_SIZE && distances[graphX][graphY] < ALTERNATIVE_PATH_MAX_DISTANCE) {
							int deltaX = 0;
							if (checkX < dstX) {
								deltaX = dstX - checkX;
							} else if (checkX > dstSizeX + dstX - 1) {
								deltaX = checkX - (dstSizeX + dstX - 1);
							}
							int deltaY = 0;
							if (checkY < dstY) {
								deltaY = dstY - checkY;
							} else if (checkY > dstSizeY + dstY - 1) {
								deltaY = checkY - (dstY + dstSizeY - 1);
							}
							int cost = deltaY * deltaY + deltaX * deltaX;
							if (cost < lowestCost || lowestCost == cost && distances[graphX][graphY] < lowestDistance) {
								lowestCost = cost;
								lowestDistance = distances[graphX][graphY];
								endX = checkX;
								endY = checkY;
							}
						}
					}
				}
				if (Integer.MAX_VALUE == lowestCost) {
					return -1;
				}
			} else {
				return -1;
			}
		}
		if (srcX == endX && srcY == endY) {
			return 0;
		}
		int steps = 0;
		bufferX[steps] = endX;
		bufferY[steps++] = endY;
		int direction = directions[endX - graphBaseX][endY - graphBaseY];
		int lastDir = direction;
		while (endX != srcX || srcY != endY) {
			if (lastDir != direction) {
				lastDir = direction;
				bufferX[steps] = endX;
				bufferY[steps++] = endY;
			}
			if ((direction & DIR_EAST) != 0) {
				endX++;
			} else if ((direction & DIR_WEST) != 0) {
				endX--;
			}
			if ((direction & DIR_NORTH) != 0) {
				endY++;
			} else if ((direction & DIR_SOUTH) != 0) {
				endY--;
			}
			direction = directions[endX - graphBaseX][endY - graphBaseY];
		}
		
		int offset = 0;
		while (steps-- > 0 && offset < stepsX.length) {
			stepsX[offset] = bufferX[steps];
			stepsY[offset++] = bufferY[steps];
		}
		return offset;
	}
	
	/**
	 * Performs a small size calculation.
	 * 
	 * @param srcX
	 *            the source x position.
	 * @param srcY
	 *            the source y position.
	 * @param strategy
	 *            the path strategy.
	 * @param collision
	 *            the collision map.
	 * @return <code>true</code> if the calculation was good otherwise
	 *         <code>false</code>.
	 */
	private static boolean calculateSmall(int srcX, int srcY, PathStrategy strategy, CollisionMap collision) {
		int lastX = srcX;
		int lastY = srcY;
		int baseX = GRAPH_SIZE / 2;
		int baseY = GRAPH_SIZE / 2;
		int graphX = srcX - baseX;
		int graphY = srcY - baseY;
		directions[baseX][baseY] = 99;
		distances[baseX][baseY] = 0;
		int next = 0;
		int current = 0;
		bufferX[next] = lastX;
		bufferY[next++] = lastY;
		int[][] flags = collision.getFlags();
		while (next != current) {
			lastX = bufferX[current];
			lastY = bufferY[current];
			current = current + 1 & 0xfff;
			baseX = lastX - graphX;
			baseY = lastY - graphY;
			int x = lastX;
			int y = lastY;
			if (strategy.canExit(1, lastX, lastY, collision)) {
				exitX = lastX;
				exitY = lastY;
				return true;
			}
			int distance = distances[baseX][baseY] + 1;
			if (baseX > 0 && directions[baseX - 1][baseY] == 0 && (flags[x - 1][y] & FLAG_ROUTE_BLOCK_EAST) == 0) {
				bufferX[next] = lastX - 1;
				bufferY[next] = lastY;
				next = 1 + next & 0xfff;
				directions[baseX - 1][baseY] = 2;
				distances[baseX - 1][baseY] = distance;
			}
			if (baseX < 127 && directions[baseX + 1][baseY] == 0 && (flags[x + 1][y] & FLAG_ROUTE_BLOCK_WEST) == 0) {
				bufferX[next] = lastX + 1;
				bufferY[next] = lastY;
				next = 1 + next & 0xfff;
				directions[baseX + 1][baseY] = 8;
				distances[1 + baseX][baseY] = distance;
			}
			if (baseY > 0 && directions[baseX][baseY - 1] == 0 && (flags[x][y - 1] & FLAG_ROUTE_BLOCK_NORTH) == 0) {
				bufferX[next] = lastX;
				bufferY[next] = lastY - 1;
				next = next + 1 & 0xfff;
				directions[baseX][baseY - 1] = 1;
				distances[baseX][baseY - 1] = distance;
			}
			if (baseY < 127 && directions[baseX][baseY + 1] == 0 && (flags[x][1 + y] & FLAG_ROUTE_BLOCK_SOUTH) == 0) {
				bufferX[next] = lastX;
				bufferY[next] = lastY + 1;
				next = 1 + next & 0xfff;
				directions[baseX][baseY + 1] = 4;
				distances[baseX][baseY + 1] = distance;
			}
			if (baseX > 0 && baseY > 0 && directions[baseX - 1][baseY - 1] == 0 && (flags[x][y - 1] & FLAG_ROUTE_BLOCK_NORTH) == 0 && (flags[x - 1][y] & FLAG_ROUTE_BLOCK_EAST) == 0 && (flags[x - 1][y - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH) == 0) {
				bufferX[next] = lastX - 1;
				bufferY[next] = lastY - 1;
				next = next + 1 & 0xfff;
				directions[baseX - 1][baseY - 1] = 3;
				distances[baseX - 1][baseY - 1] = distance;
			}
			if (baseX < 127 && baseY > 0 && directions[1 + baseX][baseY - 1] == 0 && (flags[x][y - 1] & FLAG_ROUTE_BLOCK_NORTH) == 0 && (flags[x + 1][y] & FLAG_ROUTE_BLOCK_WEST) == 0 && (flags[1 + x][y - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST) == 0) {
				bufferX[next] = 1 + lastX;
				bufferY[next] = lastY - 1;
				next = next + 1 & 0xfff;
				directions[1 + baseX][baseY - 1] = 9;
				distances[baseX + 1][baseY - 1] = distance;
			}
			if (baseX > 0 && baseY < 127 && directions[baseX - 1][1 + baseY] == 0 && (flags[x][1 + y] & FLAG_ROUTE_BLOCK_SOUTH) == 0 && (flags[x - 1][y] & FLAG_ROUTE_BLOCK_EAST) == 0 && (flags[x - 1][y + 1] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) == 0) {
				bufferX[next] = lastX - 1;
				bufferY[next] = 1 + lastY;
				next = next + 1 & 0xfff;
				directions[baseX - 1][1 + baseY] = 6;
				distances[baseX - 1][1 + baseY] = distance;
			}
			if (baseX < 127 && baseY < 127 && directions[1 + baseX][1 + baseY] == 0 && (flags[x + 1][y + 1] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH) == 0 && (flags[x + 1][y] & FLAG_ROUTE_BLOCK_WEST) == 0 && (flags[x][1 + y] & FLAG_ROUTE_BLOCK_SOUTH) == 0) {
				bufferX[next] = lastX + 1;
				bufferY[next] = 1 + lastY;
				next = next + 1 & 0xfff;
				directions[1 + baseX][baseY + 1] = 12;
				distances[1 + baseX][1 + baseY] = distance;
			}
		}
		exitX = lastX;
		exitY = lastY;
		return false;
	}
	
	/**
	 * Performs a medium size calculation.
	 * 
	 * @param srcX
	 *            the source x position.
	 * @param srcY
	 *            the source y position.
	 * @param strategy
	 *            the path strategy.
	 * @param collision
	 *            the collision map.
	 * @return <code>true</code> if the calculation was good otherwise
	 *         <code>false</code>.
	 */
	private static final boolean calculateMedium(int srcX, int srcY, PathStrategy strategy, CollisionMap collision) {
		int i_9_ = srcX;
		int i_10_ = srcY;
		int i_11_ = 64;
		int i_12_ = 64;
		int i_13_ = srcX - i_11_;
		int i_14_ = srcY - i_12_;
		directions[i_11_][i_12_] = 99;
		distances[i_11_][i_12_] = 0;
		int i_15_ = 0;
		int i_16_ = 0;
		bufferX[i_15_] = i_9_;
		bufferY[i_15_++] = i_10_;
		int[][] flags = collision.getFlags();
		while (i_15_ != i_16_) {
			i_9_ = bufferX[i_16_];
			i_10_ = bufferY[i_16_];
			i_16_ = 1 + i_16_ & 0xfff;
			i_11_ = i_9_ - i_13_;
			i_12_ = i_10_ - i_14_;
			int i_17_ = i_9_;
			int i_18_ = i_10_;
			if (strategy.canExit(2, i_9_, i_10_, collision)) {
				PathFinder.exitX = i_9_;
				PathFinder.exitY = i_10_;
				return true;
			}
			int i_19_ = 1 + distances[i_11_][i_12_];
			if (i_11_ > 0) {
				if (directions[i_11_ - 1][i_12_] != 0) {
				} else if ((flags[i_17_ - 1][i_18_] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH) != 0) {
				} else if ((flags[i_17_ - 1][i_18_ + 1] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) != 0) {
				} else {
					bufferX[i_15_] = i_9_ - 1;
					bufferY[i_15_] = i_10_;
					i_15_ = i_15_ + 1 & 0xfff;
					directions[i_11_ - 1][i_12_] = 2;
					distances[i_11_ - 1][i_12_] = i_19_;
				}
			}
			if (i_11_ < 126) {
				if (0 != directions[1 + i_11_][i_12_]) {
				} else if (0 != (flags[2 + i_17_][i_18_] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST)) {
				} else if (0 != (flags[i_17_ + 2][1 + i_18_] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH)) {
				} else {
					bufferX[i_15_] = 1 + i_9_;
					bufferY[i_15_] = i_10_;
					i_15_ = i_15_ + 1 & 0xfff;
					directions[i_11_ + 1][i_12_] = 8;
					distances[1 + i_11_][i_12_] = i_19_;
				}
			}
			if (i_12_ > 0) {
				if (directions[i_11_][i_12_ - 1] != 0) {
				} else if (0 != (flags[i_17_][i_18_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH)) {
				} else if (0 != (flags[1 + i_17_][i_18_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST)) {
				} else {
					bufferX[i_15_] = i_9_;
					bufferY[i_15_] = i_10_ - 1;
					i_15_ = 1 + i_15_ & 0xfff;
					directions[i_11_][i_12_ - 1] = 1;
					distances[i_11_][i_12_ - 1] = i_19_;
				}
			}
			if (i_12_ < 126) {
				if (0 != directions[i_11_][1 + i_12_]) {
				} else if ((flags[i_17_][i_18_ + 2] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) != 0) {
				} else if (0 != (flags[1 + i_17_][i_18_ + 2] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH)) {
				} else {
					bufferX[i_15_] = i_9_;
					bufferY[i_15_] = i_10_ + 1;
					i_15_ = i_15_ + 1 & 0xfff;
					directions[i_11_][1 + i_12_] = 4;
					distances[i_11_][i_12_ + 1] = i_19_;
				}
			}
			if (i_11_ > 0 && i_12_ > 0) {
				if (directions[i_11_ - 1][i_12_ - 1] != 0) {
				} else if (0 != (flags[i_17_ - 1][i_18_] & anInt3226)) {
				} else if (0 != (flags[i_17_ - 1][i_18_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH)) {
				} else if (0 != (flags[i_17_][i_18_ - 1] & anInt3231)) {
				} else {
					bufferX[i_15_] = i_9_ - 1;
					bufferY[i_15_] = i_10_ - 1;
					i_15_ = i_15_ + 1 & 0xfff;
					directions[i_11_ - 1][i_12_ - 1] = 3;
					distances[i_11_ - 1][i_12_ - 1] = i_19_;
				}
			}
			if (i_11_ < 126 && i_12_ > 0) {
				if (0 != directions[1 + i_11_][i_12_ - 1]) {
				} else if (0 != (flags[1 + i_17_][i_18_ - 1] & anInt3231)) {
				} else if (0 != (flags[i_17_ + 2][i_18_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST)) {
				} else if (0 != (flags[2 + i_17_][i_18_] & anInt3225)) {
				} else {
					bufferX[i_15_] = 1 + i_9_;
					bufferY[i_15_] = i_10_ - 1;
					i_15_ = 1 + i_15_ & 0xfff;
					directions[i_11_ + 1][i_12_ - 1] = 9;
					distances[i_11_ + 1][i_12_ - 1] = i_19_;
				}
			}
			if (i_11_ > 0 && i_12_ < 126) {
				if (0 != directions[i_11_ - 1][1 + i_12_]) {
				} else if ((flags[i_17_ - 1][1 + i_18_] & anInt3226) != 0) {
				} else if ((flags[i_17_ - 1][i_18_ + 2] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) != 0) {
				} else if ((flags[i_17_][i_18_ + 2] & anInt3228) != 0) {
				} else {
					bufferX[i_15_] = i_9_ - 1;
					bufferY[i_15_] = 1 + i_10_;
					i_15_ = i_15_ + 1 & 0xfff;
					directions[i_11_ - 1][i_12_ + 1] = 6;
					distances[i_11_ - 1][1 + i_12_] = i_19_;
				}
			}
			if (i_11_ < 126 && i_12_ < 126) {
				if (directions[1 + i_11_][1 + i_12_] != 0) {
				} else if (0 != (flags[1 + i_17_][i_18_ + 2] & anInt3228)) {
				} else if ((flags[i_17_ + 2][2 + i_18_] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH) != 0) {
				} else if ((flags[2 + i_17_][i_18_ + 1] & anInt3225) != 0) {
				} else {
					bufferX[i_15_] = i_9_ + 1;
					bufferY[i_15_] = 1 + i_10_;
					i_15_ = 1 + i_15_ & 0xfff;
					directions[1 + i_11_][1 + i_12_] = 12;
					distances[i_11_ + 1][1 + i_12_] = i_19_;
				}
			}
		}
		PathFinder.exitX = i_9_;
		PathFinder.exitY = i_10_;
		return false;
	}
	
	/**
	 * Performs a large size calculation.
	 * 
	 * @param srcX
	 *            the source x position.
	 * @param srcY
	 *            the source y position.
	 * @param strategy
	 *            the path strategy.
	 * @param collision
	 *            the collision map.
	 * @return <code>true</code> if the calculation was good otherwise
	 *         <code>false</code>.
	 */
	private static final boolean calculateLarge(int srcX, int srcY, int size, PathStrategy strategy, CollisionMap collision) {
		int stepX = srcX;
		int stepY = srcY;
		int i_10_ = 64;
		int i_11_ = 64;
		int i_12_ = srcX - i_10_;
		int i_13_ = srcY - i_11_;
		directions[i_10_][i_11_] = 99;
		distances[i_10_][i_11_] = 0;
		int i_14_ = 0;
		int i_15_ = 0;
		bufferX[i_14_] = stepX;
		bufferY[i_14_++] = stepY;
		int[][] flags = collision.getFlags();
		while_108_: while (i_15_ != i_14_) {
			stepX = bufferX[i_15_];
			stepY = bufferY[i_15_];
			i_15_ = 1 + i_15_ & 0xfff;
			i_10_ = stepX - i_12_;
			i_11_ = stepY - i_13_;
			int i_16_ = stepX;
			int i_17_ = stepY;
			if (strategy.canExit(size, stepX, stepY, collision)) {
				PathFinder.exitX = stepX;
				PathFinder.exitY = stepY;
				return true;
			}
			int i_18_ = distances[i_10_][i_11_] + 1;
			while_101_: do {
				if (i_10_ > 0) {
					if (0 != directions[i_10_ - 1][i_11_]) {
					} else if ((flags[i_16_ - 1][i_17_] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH) != 0) {
					} else if (0 != (flags[i_16_ - 1][size + i_17_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST)) {
					} else {
						for (int i_19_ = 1; i_19_ < size - 1; i_19_++) {
							if ((flags[i_16_ - 1][i_19_ + i_17_] & anInt3226) != 0) {
								break while_101_;
							}
						}
						bufferX[i_14_] = stepX - 1;
						bufferY[i_14_] = stepY;
						i_14_ = 1 + i_14_ & 0xfff;
						directions[i_10_ - 1][i_11_] = 2;
						distances[i_10_ - 1][i_11_] = i_18_;
					}
				}
			} while (false);
			while_102_: do {
				if (i_10_ < 128 - size) {
					if (directions[1 + i_10_][i_11_] != 0) {
					} else if ((flags[i_16_ + size][i_17_] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST) != 0) {
					} else if (0 != (flags[i_16_ + size][i_17_ + size - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH)) {
					} else {
						for (int i_20_ = 1; i_20_ < size - 1; i_20_++) {
							if (0 != (flags[i_16_ + size][i_17_ + i_20_] & anInt3225)) {
								break while_102_;
							}
						}
						bufferX[i_14_] = stepX + 1;
						bufferY[i_14_] = stepY;
						i_14_ = i_14_ + 1 & 0xfff;
						directions[1 + i_10_][i_11_] = 8;
						distances[1 + i_10_][i_11_] = i_18_;
					}
				}
			} while (false);
			while_103_: do {
				if (i_11_ > 0) {
					if (directions[i_10_][i_11_ - 1] != 0) {
					} else if ((flags[i_16_][i_17_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH) != 0) {
					} else if (0 != (flags[size + i_16_ - 1][i_17_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST)) {
					} else {
						for (int i_21_ = 1; i_21_ < size - 1; i_21_++) {
							if (0 != (flags[i_16_ + i_21_][i_17_ - 1] & anInt3231)) {
								break while_103_;
							}
						}
						bufferX[i_14_] = stepX;
						bufferY[i_14_] = stepY - 1;
						i_14_ = i_14_ + 1 & 0xfff;
						directions[i_10_][i_11_ - 1] = 1;
						distances[i_10_][i_11_ - 1] = i_18_;
					}
				}
			} while (false);
			while_104_: do {
				if (i_11_ < 128 - size) {
					if (directions[i_10_][i_11_ + 1] != 0) {
					} else if ((flags[i_16_][size + i_17_] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) != 0) {
					} else if (0 != (flags[size + i_16_ - 1][size + i_17_] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH)) {
					} else {
						for (int i_22_ = 1; i_22_ < size - 1; i_22_++) {
							if ((flags[i_22_ + i_16_][size + i_17_] & anInt3228) != 0) {
								break while_104_;
							}
						}
						bufferX[i_14_] = stepX;
						bufferY[i_14_] = 1 + stepY;
						i_14_ = i_14_ + 1 & 0xfff;
						directions[i_10_][1 + i_11_] = 4;
						distances[i_10_][i_11_ + 1] = i_18_;
					}
				}
			} while (false);
			while_105_: do {
				if (i_10_ > 0 && i_11_ > 0) {
					if (directions[i_10_ - 1][i_11_ - 1] != 0) {
					} else if (0 != (flags[i_16_ - 1][i_17_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_NORTH)) {
					} else {
						for (int i_23_ = 1; i_23_ < size; i_23_++) {
							if (0 != (flags[i_16_ - 1][i_23_ + i_17_ - 1] & anInt3226)) {
								break while_105_;
							}
							if (0 != (flags[i_16_ - 1 + i_23_][i_17_ - 1] & anInt3231)) {
								break while_105_;
							}
						}
						bufferX[i_14_] = stepX - 1;
						bufferY[i_14_] = stepY - 1;
						i_14_ = i_14_ + 1 & 0xfff;
						directions[i_10_ - 1][i_11_ - 1] = 3;
						distances[i_10_ - 1][i_11_ - 1] = i_18_;
					}
				}
			} while (false);
			while_106_: do {
				if (i_10_ < 128 - size && i_11_ > 0) {
					if (0 != directions[1 + i_10_][i_11_ - 1]) {
					} else if ((flags[size + i_16_][i_17_ - 1] & FLAG_ROUTE_BLOCK_DIAGONAL_WEST) != 0) {
					} else {
						for (int i_24_ = 1; i_24_ < size; i_24_++) {
							if ((flags[i_16_ + size][i_24_ + i_17_ - 1] & anInt3225) != 0) {
								break while_106_;
							}
							if (0 != (flags[i_16_ + i_24_][i_17_ - 1] & anInt3231)) {
								break while_106_;
							}
						}
						bufferX[i_14_] = stepX + 1;
						bufferY[i_14_] = stepY - 1;
						i_14_ = 1 + i_14_ & 0xfff;
						directions[i_10_ + 1][i_11_ - 1] = 9;
						distances[i_10_ + 1][i_11_ - 1] = i_18_;
					}
				}
			} while (false);
			while_107_: do {
				if (i_10_ > 0 && i_11_ < 128 - size) {
					if (directions[i_10_ - 1][1 + i_11_] != 0) {
					} else if ((flags[i_16_ - 1][i_17_ + size] & FLAG_ROUTE_BLOCK_DIAGONAL_EAST) != 0) {
					} else {
						for (int i_25_ = 1; i_25_ < size; i_25_++) {
							if ((flags[i_16_ - 1][i_25_ + i_17_] & anInt3226) != 0) {
								break while_107_;
							}
							if (0 != (flags[i_16_ - 1 + i_25_][i_17_ + size] & anInt3228)) {
								break while_107_;
							}
						}
						bufferX[i_14_] = stepX - 1;
						bufferY[i_14_] = 1 + stepY;
						i_14_ = 1 + i_14_ & 0xfff;
						directions[i_10_ - 1][1 + i_11_] = 6;
						distances[i_10_ - 1][1 + i_11_] = i_18_;
					}
				}
			} while (false);
			if (i_10_ < 128 - size && i_11_ < 128 - size) {
				if (0 != directions[i_10_ + 1][1 + i_11_]) {
				} else if ((flags[i_16_ + size][size + i_17_] & FLAG_ROUTE_BLOCK_DIAGONAL_SOUTH) != 0) {
				} else {
					for (int i_26_ = 1; i_26_ < size; i_26_++) {
						if ((flags[i_26_ + i_16_][i_17_ + size] & anInt3228) != 0) {
							continue while_108_;
						}
						if ((flags[i_16_ + size][i_26_ + i_17_] & anInt3225) != 0) {
							continue while_108_;
						}
					}
					bufferX[i_14_] = stepX + 1;
					bufferY[i_14_] = 1 + stepY;
					i_14_ = 1 + i_14_ & 0xfff;
					directions[1 + i_10_][1 + i_11_] = 12;
					distances[i_10_ + 1][1 + i_11_] = i_18_;
				}
			}
		}
		PathFinder.exitX = stepX;
		PathFinder.exitY = stepY;
		return false;
	}
	
}
