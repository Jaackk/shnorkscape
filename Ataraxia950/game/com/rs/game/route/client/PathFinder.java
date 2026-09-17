package com.rs.game.route.client;

import com.rs.game.Entity;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;

/**
 * 
 * @author - Taht One Guy
 *
 */

public class PathFinder {
	
	private final int localX;
	private final int localY;

	private final int[][] anIntArrayArray3155;
	
	private final int[] routeFinderYArray;
	private final int[] routeFinderXArray;
	
	private final int[][] anIntArrayArray3076;
	private final int[][] anIntArrayArray3080;
	
	private int anInt1684;
	private int anInt3078;
	
	private final int anInt3151;
	private final int anInt3152;
	
	private int stepsCount;
	private final int[] pathBufferX;
	private final int[] pathBufferY;
	
	private final int size;
	
	private PathFinder(WorldTile tile, int size) {
		anIntArrayArray3155 = new int[110][110];
		anIntArrayArray3076 = new int[128][128];
		anIntArrayArray3080 = new int[128][128];
		routeFinderXArray = new int[4096];
		routeFinderYArray = new int[4096];
		pathBufferX = new int[50];
		pathBufferY = new int[50];
		anInt3151 = -1780317135;
		anInt3152 = 599015853;
		this.size = size;
		tile = tile.transform(-1, -1, 0);
		localX = tile.getLocalX();
		localY = tile.getLocalY();

		clipTheRegion(tile);
	}
	
	
	private void walk(Entity entity, PathStrategy pathStrategy) {
	  	stepsCount = (calculateRoute(localX, localY, size, pathStrategy,
											true, pathBufferX, pathBufferY));

	  	int Xbase = entity.getX()-localX;
		int Ybase = entity.getY()-localY;
		int steps = stepsCount;
		if (steps > 25)
			steps = 25;
		if (entity instanceof Player)
			((Player)entity).stopAll();
		for(int c = stepsCount-1; c >= 0; c--) {

			if (!entity.addWalkSteps(Xbase + routeFinderXArray[c],
					Ybase + routeFinderYArray[c], 25,
					true))
				break;
		}
	}
	
	public static void simpleWalkTo(Entity entity, WorldTile toTile) {
		int moveX = toTile.getX() - entity.getX();
		int moveY = toTile.getY() - entity.getY();
		PathFinder finder = new PathFinder(entity, entity.getSize());
		finder.walk(entity, createExactStrategy(finder.getLocalX()+moveX, finder.getLocalY()+moveY));
	}
	
	private void clipTheRegion(WorldTile tile) {

		for(int x = 1; x < 105; x++)
			for(int y = 1; y < 105; y++) {
				Region region = World.getRegion(new WorldTile(tile.getX()-localX+x, tile.getY()-localY+y, tile.getPlane()).getRegionId());
				if (region.getLoadMapStage() == 0)
					region.quickLoad();
				anIntArrayArray3155[x][y] = World.getMask(tile.getPlane(), tile.getX()-localX+x, tile.getY()-localY+y);

			}
	}
	
	private int getLocalX() {
		return localX;
	}
	
	private int getLocalY() {

		return localY;
	}

	  private static PathStrategy createExactStrategy(int paramInt1, int paramInt2)
	  {
	    try
	    {
              ExactStrategy exactdestinationstrategy = new ExactStrategy();
	      exactdestinationstrategy.toX = (paramInt1 * -760677635);
	      exactdestinationstrategy.toY = (paramInt2 * 167105303);
	      exactdestinationstrategy.sizeX = -1544157451;
	      exactdestinationstrategy.sizeY = -1468199503;
	      return exactdestinationstrategy;
	    }
	    catch (Exception e) {
	    }
		return null;
	  }
	
	final boolean method4593(int i, int i_13_, int i_14_,
			PathStrategy class336, byte i_15_) {
		try {
			int i_16_ = i;
			int i_17_ = i_13_;
			int i_18_ = 64;
			int i_19_ = 64;
			int i_20_ = i - i_18_;
			int i_21_ = i_13_ - i_19_;
			anIntArrayArray3076[i_18_][i_19_] = 99;
			anIntArrayArray3080[i_18_][i_19_] = 0;
			int i_22_ = 0;
			int i_23_ = 0;
			routeFinderXArray[i_22_] = i_16_;
			routeFinderYArray[i_22_++] = i_17_;
			//int[][] is = anIntArrayArray3155;
			while_101_: while (i_23_ != i_22_) {
				i_16_ = routeFinderXArray[i_23_];
				i_17_ = routeFinderYArray[i_23_];
				i_23_ = i_23_ + 1 & 0xfff;
				i_18_ = i_16_ - i_20_;
				i_19_ = i_17_ - i_21_;
				int i_24_ = i_16_ - anInt3151 * 1487776559;
				int i_25_ = i_17_ - anInt3152 * 1415525851;
				if (class336.method4090(0, i_16_, i_17_, 0)) {
					anInt3078 = i_16_ * 1021042197;
					anInt1684 = -575994451 * i_17_;
					return true;
				}
				int i_26_ = anIntArrayArray3080[i_18_][i_19_] + 1;
				while_94_: do {
					if (i_18_ > 0) {
						if (anIntArrayArray3076[i_18_ - 1][i_19_] != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if (0 != (anIntArrayArray3155[i_24_ - 1][i_25_] & 0x43a40000)) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else if (0 != (anIntArrayArray3155[i_24_ - 1][i_14_ + i_25_ - 1] & 0x4e240000)) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_27_ = 1; i_27_ < i_14_ - 1; i_27_++) {
								if (0 != (anIntArrayArray3155[i_24_ - 1][i_25_ + i_27_] & 0x4fa40000)) {
									if (i_15_ <= 36)
										throw new IllegalStateException();
									break while_94_;
								}
							}
							routeFinderXArray[i_22_] = i_16_ - 1;
							routeFinderYArray[i_22_] = i_17_;
							i_22_ = 1 + i_22_ & 0xfff;
							anIntArrayArray3076[i_18_ - 1][i_19_] = 2;
							anIntArrayArray3080[i_18_ - 1][i_19_] = i_26_;
						}
					}
				} while (false);
				while_95_: do {
					if (i_18_ < 128 - i_14_) {
						if (0 != (anIntArrayArray3076[1 + i_18_][i_19_])) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else if (0 != (anIntArrayArray3155[i_24_ + i_14_][i_25_] & 0x60e40000)) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if ((anIntArrayArray3155[i_14_ + i_24_][i_14_ + i_25_ - 1] & 0x78240000) != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_28_ = 1; i_28_ < i_14_ - 1; i_28_++) {
								if (0 != (anIntArrayArray3155[i_14_ + i_24_][i_25_ + i_28_] & 0x78e40000)) {
									if (i_15_ <= 36)
										throw new IllegalStateException();
									break while_95_;
								}
							}
							routeFinderXArray[i_22_] = 1 + i_16_;
							routeFinderYArray[i_22_] = i_17_;
							i_22_ = 1 + i_22_ & 0xfff;
							anIntArrayArray3076[i_18_ + 1][i_19_] = 8;
							anIntArrayArray3080[i_18_ + 1][i_19_] = i_26_;
						}
					}
				} while (false);
				while_96_: do {
					if (i_19_ > 0) {
						if (0 != (anIntArrayArray3076[i_18_][i_19_ - 1])) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else if ((anIntArrayArray3155[i_24_][i_25_ - 1] & 0x43a40000) != 0) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else if (0 != (anIntArrayArray3155[i_24_ + i_14_ - 1][i_25_ - 1] & 0x60e40000)) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_29_ = 1; i_29_ < i_14_ - 1; i_29_++) {
								if (0 != (anIntArrayArray3155[i_29_ + i_24_][i_25_ - 1] & 0x63e40000)) {
									if (i_15_ <= 36) {
										/* empty */
									}
									break while_96_;
								}
							}
							routeFinderXArray[i_22_] = i_16_;
							routeFinderYArray[i_22_] = i_17_ - 1;
							i_22_ = 1 + i_22_ & 0xfff;
							anIntArrayArray3076[i_18_][i_19_ - 1] = 1;
							anIntArrayArray3080[i_18_][i_19_ - 1] = i_26_;
						}
					}
				} while (false);
				while_97_: do {
					if (i_19_ < 128 - i_14_) {
						if (anIntArrayArray3076[i_18_][i_19_ + 1] != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if ((anIntArrayArray3155[i_24_][i_14_ + i_25_] & 0x4e240000) != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if (0 != (anIntArrayArray3155[i_24_ + i_14_ - 1][i_25_ + i_14_] & 0x78240000)) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_30_ = 1; i_30_ < i_14_ - 1; i_30_++) {
								if (0 != (anIntArrayArray3155[i_30_ + i_24_][i_14_ + i_25_] & 0x7e240000)) {
									if (i_15_ <= 36)
										throw new IllegalStateException();
									break while_97_;
								}
							}
							routeFinderXArray[i_22_] = i_16_;
							routeFinderYArray[i_22_] = i_17_ + 1;
							i_22_ = i_22_ + 1 & 0xfff;
							anIntArrayArray3076[i_18_][1 + i_19_] = 4;
							anIntArrayArray3080[i_18_][i_19_ + 1] = i_26_;
						}
					}
				} while (false);
				while_98_: do {
					if (i_18_ > 0 && i_19_ > 0) {
						if (anIntArrayArray3076[i_18_ - 1][i_19_ - 1] != 0) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else if ((anIntArrayArray3155[i_24_ - 1][i_25_ - 1] & 0x43a40000) != 0) {
							if (i_15_ <= 36)
								throw new IllegalStateException();
						} else {
							for (int i_31_ = 1; i_31_ < i_14_; i_31_++) {
								if ((anIntArrayArray3155[i_24_ - 1][i_25_ - 1 + i_31_] & 0x4fa40000) != 0) {
									if (i_15_ <= 36) {
										/* empty */
									}
									break while_98_;
								}
								if ((anIntArrayArray3155[i_24_ - 1 + i_31_][i_25_ - 1] & 0x63e40000) != 0) {
									if (i_15_ <= 36) {
										/* empty */
									}
									break while_98_;
								}
							}
							routeFinderXArray[i_22_] = i_16_ - 1;
							routeFinderYArray[i_22_] = i_17_ - 1;
							i_22_ = i_22_ + 1 & 0xfff;
							anIntArrayArray3076[i_18_ - 1][i_19_ - 1] = 3;
							anIntArrayArray3080[i_18_ - 1][i_19_ - 1] = i_26_;
						}
					}
				} while (false);
				while_99_: do {
					if (i_18_ < 128 - i_14_ && i_19_ > 0) {
						if (anIntArrayArray3076[1 + i_18_][i_19_ - 1] != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if ((anIntArrayArray3155[i_14_ + i_24_][i_25_ - 1] & 0x60e40000) != 0) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_32_ = 1; i_32_ < i_14_; i_32_++) {
								if (0 != (anIntArrayArray3155[i_24_ + i_14_][i_25_ - 1 + i_32_] & 0x78e40000)) {
									if (i_15_ <= 36)
										throw new IllegalStateException();
									break while_99_;
								}
								if ((anIntArrayArray3155[i_24_ + i_32_][i_25_ - 1] & 0x63e40000) != 0) {
									if (i_15_ <= 36) {
										/* empty */
									}
									break while_99_;
								}
							}
							routeFinderXArray[i_22_] = i_16_ + 1;
							routeFinderYArray[i_22_] = i_17_ - 1;
							i_22_ = i_22_ + 1 & 0xfff;
							anIntArrayArray3076[i_18_ + 1][i_19_ - 1] = 9;
							anIntArrayArray3080[i_18_ + 1][i_19_ - 1] = i_26_;
						}
					}
				} while (false);
				while_100_: do {
					if (i_18_ > 0 && i_19_ < 128 - i_14_) {
						if (0 != (anIntArrayArray3076[i_18_ - 1][1 + i_19_])) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else if (0 != (anIntArrayArray3155[i_24_ - 1][i_14_ + i_25_] & 0x4e240000)) {
							if (i_15_ <= 36) {
								/* empty */
							}
						} else {
							for (int i_33_ = 1; i_33_ < i_14_; i_33_++) {
								if ((anIntArrayArray3155[i_24_ - 1][i_33_ + i_25_] & 0x4fa40000) != 0) {
									if (i_15_ <= 36)
										throw new IllegalStateException();
									break while_100_;
								}
								if (0 != (anIntArrayArray3155[i_33_ + (i_24_ - 1)][i_14_ + i_25_] & 0x7e240000)) {
									if (i_15_ <= 36) {
										/* empty */
									}
									break while_100_;
								}
							}
							routeFinderXArray[i_22_] = i_16_ - 1;
							routeFinderYArray[i_22_] = 1 + i_17_;
							i_22_ = 1 + i_22_ & 0xfff;
							anIntArrayArray3076[i_18_ - 1][1 + i_19_] = 6;
							anIntArrayArray3080[i_18_ - 1][1 + i_19_] = i_26_;
						}
					}
				} while (false);
				if (i_18_ < 128 - i_14_ && i_19_ < 128 - i_14_) {
					if (0 != (anIntArrayArray3076[1 + i_18_][1 + i_19_])) {
						if (i_15_ <= 36) {
							/* empty */
						}
					} else if ((anIntArrayArray3155[i_14_ + i_24_][i_25_ + i_14_] & 0x78240000) != 0) {
						if (i_15_ <= 36)
							throw new IllegalStateException();
					} else {
						for (int i_34_ = 1; i_34_ < i_14_; i_34_++) {
							if (0 != (anIntArrayArray3155[i_24_ + i_34_][i_25_ + i_14_] & 0x7e240000)) {
								if (i_15_ <= 36) {
									/* empty */
								}
								continue while_101_;
							}
							if (0 != (anIntArrayArray3155[i_24_ + i_14_][i_34_ + i_25_] & 0x78e40000)) {
								if (i_15_ <= 36) {
									/* empty */
								}
								continue while_101_;
							}
						}
						routeFinderXArray[i_22_] = i_16_ + 1;
						routeFinderYArray[i_22_] = 1 + i_17_;
						i_22_ = i_22_ + 1 & 0xfff;
						anIntArrayArray3076[i_18_ + 1][1 + i_19_] = 12;
						anIntArrayArray3080[i_18_ + 1][i_19_ + 1] = i_26_;
					}
				}
			}
			anInt3078 = 1021042197 * i_16_;
			anInt1684 = -575994451 * i_17_;
			return false;
		} catch (Exception e) {
		}
		return false;
	}
	
	final boolean method2120(int i, int i_8_, PathStrategy class336,
			 int i_9_) {
		try {
			int i_10_ = i;
			int i_11_ = i_8_;
			int i_12_ = 64;
			int i_13_ = 64;
			int i_14_ = i - i_12_;
			int i_15_ = i_8_ - i_13_;
			anIntArrayArray3076[i_12_][i_13_] = 99;
			anIntArrayArray3080[i_12_][i_13_] = 0;
			int i_16_ = 0;
			int i_17_ = 0;
			routeFinderXArray[i_16_] = i_10_;
			routeFinderYArray[i_16_++] = i_11_;
			//int[][] is = anIntArrayArray3155;
			while (i_17_ != i_16_) {
				i_10_ = routeFinderXArray[i_17_];
				i_11_ = routeFinderYArray[i_17_];
				i_17_ = i_17_ + 1 & 0xfff;
				i_12_ = i_10_ - i_14_;
				i_13_ = i_11_ - i_15_;
				int i_18_ = i_10_ - 1487776559 * anInt3151;
				int i_19_ = i_11_ - 1415525851 * anInt3152;
				if (class336.method4090(0, i_10_, i_11_, 0)) {
					anInt3078 = 1021042197 * i_10_;
					anInt1684 = -575994451 * i_11_;
					return true;
				}
				int i_20_ = anIntArrayArray3080[i_12_][i_13_] + 1;
				if (i_12_ > 0) {
					if (0 != anIntArrayArray3076[i_12_ - 1][i_13_]) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if (0 != (anIntArrayArray3155[i_18_ - 1][i_19_] & 0x43a40000)) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ - 1][1 + i_19_] & 0x4e240000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_ - 1;
						routeFinderYArray[i_16_] = i_11_;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_ - 1][i_13_] = 2;
						anIntArrayArray3080[i_12_ - 1][i_13_] = i_20_;
					}
				}
				if (i_12_ < 126) {
					if (0 != anIntArrayArray3076[1 + i_12_][i_13_]) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ + 2][i_19_] & 0x60e40000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if (0 != (anIntArrayArray3155[2 + i_18_][1 + i_19_] & 0x78240000)) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_ + 1;
						routeFinderYArray[i_16_] = i_11_;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[1 + i_12_][i_13_] = 8;
						anIntArrayArray3080[i_12_ + 1][i_13_] = i_20_;
					}
				}
				if (i_13_ > 0) {
					if (0 != anIntArrayArray3076[i_12_][i_13_ - 1]) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if (0 != (anIntArrayArray3155[i_18_][i_19_ - 1] & 0x43a40000)) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ + 1][i_19_ - 1] & 0x60e40000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_;
						routeFinderYArray[i_16_] = i_11_ - 1;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_][i_13_ - 1] = 1;
						anIntArrayArray3080[i_12_][i_13_ - 1] = i_20_;
					}
				}
				if (i_13_ < 126) {
					if (anIntArrayArray3076[i_12_][1 + i_13_] != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if (0 != (anIntArrayArray3155[i_18_][2 + i_19_] & 0x4e240000)) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if (0 != (anIntArrayArray3155[i_18_ + 1][i_19_ + 2] & 0x78240000)) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else {
						routeFinderXArray[i_16_] = i_10_;
						routeFinderYArray[i_16_] = 1 + i_11_;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_][i_13_ + 1] = 4;
						anIntArrayArray3080[i_12_][1 + i_13_] = i_20_;
					}
				}
				if (i_12_ > 0 && i_13_ > 0) {
					if (anIntArrayArray3076[i_12_ - 1][i_13_ - 1] != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ - 1][i_19_] & 0x4fa40000) != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ - 1][i_19_ - 1] & 0x43a40000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if (0 != (anIntArrayArray3155[i_18_][i_19_ - 1] & 0x63e40000)) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_ - 1;
						routeFinderYArray[i_16_] = i_11_ - 1;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_ - 1][i_13_ - 1] = 3;
						anIntArrayArray3080[i_12_ - 1][i_13_ - 1] = i_20_;
					}
				}
				if (i_12_ < 126 && i_13_ > 0) {
					if (0 != (anIntArrayArray3076[1 + i_12_][i_13_ - 1])) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ + 1][i_19_ - 1] & 0x63e40000) != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ + 2][i_19_ - 1] & 0x60e40000) != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[2 + i_18_][i_19_] & 0x78e40000) != 0) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else {
						routeFinderXArray[i_16_] = i_10_ + 1;
						routeFinderYArray[i_16_] = i_11_ - 1;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_ + 1][i_13_ - 1] = 9;
						anIntArrayArray3080[1 + i_12_][i_13_ - 1] = i_20_;
					}
				}
				if (i_12_ > 0 && i_13_ < 126) {
					if (0 != (anIntArrayArray3076[i_12_ - 1][1 + i_13_])) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_ - 1][1 + i_19_] & 0x4fa40000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if (0 != (anIntArrayArray3155[i_18_ - 1][2 + i_19_] & 0x4e240000)) {
						if (i_9_ != -1327541421)
							throw new IllegalStateException();
					} else if ((anIntArrayArray3155[i_18_][2 + i_19_] & 0x7e240000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_ - 1;
						routeFinderYArray[i_16_] = i_11_ + 1;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_ - 1][i_13_ + 1] = 6;
						anIntArrayArray3080[i_12_ - 1][1 + i_13_] = i_20_;
					}
				}
				if (i_12_ < 126 && i_13_ < 126) {
					if (0 != (anIntArrayArray3076[i_12_ + 1][1 + i_13_])) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if ((anIntArrayArray3155[1 + i_18_][2 + i_19_] & 0x7e240000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if ((anIntArrayArray3155[2 + i_18_][2 + i_19_] & 0x78240000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else if ((anIntArrayArray3155[i_18_ + 2][1 + i_19_] & 0x78e40000) != 0) {
						if (i_9_ != -1327541421) {
							/* empty */
						}
					} else {
						routeFinderXArray[i_16_] = i_10_ + 1;
						routeFinderYArray[i_16_] = 1 + i_11_;
						i_16_ = i_16_ + 1 & 0xfff;
						anIntArrayArray3076[i_12_ + 1][1 + i_13_] = 12;
						anIntArrayArray3080[1 + i_12_][1 + i_13_] = i_20_;
					}
				}
			}
			anInt3078 = 1021042197 * i_10_;
			anInt1684 = -575994451 * i_11_;
			return false;
		} catch (Exception e) {
		}
		return false;
	}
	
	final boolean method4006(int i, int i_6_, PathStrategy class336,
			 int i_7_) {
		try {
			int i_8_ = i;
			int i_9_ = i_6_;
			int i_10_ = 64;
			int i_11_ = 64;
			int i_12_ = i - i_10_;
			int i_13_ = i_6_ - i_11_;
			anIntArrayArray3076[i_10_][i_11_] = 99;
			anIntArrayArray3080[i_10_][i_11_] = 0;
			int i_14_ = 0;
			int i_15_ = 0;
			routeFinderXArray[i_14_] = i_8_;
			routeFinderYArray[i_14_++] = i_9_;
			//int[][] is = anIntArrayArray3155;
			while (i_15_ != i_14_) {
				i_8_ = routeFinderXArray[i_15_];
				i_9_ = routeFinderYArray[i_15_];
				i_15_ = 1 + i_15_ & 0xfff;
				i_10_ = i_8_ - i_12_;
				i_11_ = i_9_ - i_13_;
				int i_16_ = i_8_ - 1487776559 * anInt3151;
				int i_17_ = i_9_ - 1415525851 * anInt3152;
				if (class336.method4090(0, i_8_, i_9_, 0)) {
					anInt3078 = i_8_ * 1021042197;
					anInt1684 = -575994451 * i_9_;
					return true;
				}
				int i_18_ = anIntArrayArray3080[i_10_][i_11_] + 1;
				if (i_10_ > 0
						&& 0 == anIntArrayArray3076[i_10_ - 1][i_11_]
						&& (anIntArrayArray3155[i_16_ - 1][i_17_] & 0x42240000) == 0) {
					routeFinderXArray[i_14_] = i_8_ - 1;
					routeFinderYArray[i_14_] = i_9_;
					i_14_ = 1 + i_14_ & 0xfff;
					anIntArrayArray3076[i_10_ - 1][i_11_] = 2;
					anIntArrayArray3080[i_10_ - 1][i_11_] = i_18_;
				}
				if (i_10_ < 127
						&& 0 == anIntArrayArray3076[i_10_ + 1][i_11_]
						&& (anIntArrayArray3155[1 + i_16_][i_17_] & 0x60240000) == 0) {
					routeFinderXArray[i_14_] = i_8_ + 1;
					routeFinderYArray[i_14_] = i_9_;
					i_14_ = i_14_ + 1 & 0xfff;
					anIntArrayArray3076[1 + i_10_][i_11_] = 8;
					anIntArrayArray3080[i_10_ + 1][i_11_] = i_18_;
				}
				if (i_11_ > 0
						&& anIntArrayArray3076[i_10_][i_11_ - 1] == 0
						&& 0 == (anIntArrayArray3155[i_16_][i_17_ - 1] & 0x40a40000)) {
					routeFinderXArray[i_14_] = i_8_;
					routeFinderYArray[i_14_] = i_9_ - 1;
					i_14_ = 1 + i_14_ & 0xfff;
					anIntArrayArray3076[i_10_][i_11_ - 1] = 1;
					anIntArrayArray3080[i_10_][i_11_ - 1] = i_18_;
				}
				if (i_11_ < 127
						&& 0 == anIntArrayArray3076[i_10_][i_11_ + 1]
						&& (anIntArrayArray3155[i_16_][1 + i_17_] & 0x48240000) == 0) {
					routeFinderXArray[i_14_] = i_8_;
					routeFinderYArray[i_14_] = 1 + i_9_;
					i_14_ = 1 + i_14_ & 0xfff;
					anIntArrayArray3076[i_10_][i_11_ + 1] = 4;
					anIntArrayArray3080[i_10_][i_11_ + 1] = i_18_;
				}
				if (i_10_ > 0
						&& i_11_ > 0
						&& anIntArrayArray3076[i_10_ - 1][i_11_ - 1] == 0
						&& (anIntArrayArray3155[i_16_ - 1][i_17_ - 1] & 0x43a40000) == 0
						&& (anIntArrayArray3155[i_16_ - 1][i_17_] & 0x42240000) == 0
						&& 0 == (anIntArrayArray3155[i_16_][i_17_ - 1] & 0x40a40000)) {
					routeFinderXArray[i_14_] = i_8_ - 1;
					routeFinderYArray[i_14_] = i_9_ - 1;
					i_14_ = i_14_ + 1 & 0xfff;
					anIntArrayArray3076[i_10_ - 1][i_11_ - 1] = 3;
					anIntArrayArray3080[i_10_ - 1][i_11_ - 1] = i_18_;
				}
				if (i_10_ < 127
						&& i_11_ > 0
						&& 0 == anIntArrayArray3076[1 + i_10_][i_11_ - 1]
						&& (anIntArrayArray3155[i_16_ + 1][i_17_ - 1] & 0x60e40000) == 0
						&& (anIntArrayArray3155[i_16_ + 1][i_17_] & 0x60240000) == 0
						&& (anIntArrayArray3155[i_16_][i_17_ - 1] & 0x40a40000) == 0) {
					routeFinderXArray[i_14_] = i_8_ + 1;
					routeFinderYArray[i_14_] = i_9_ - 1;
					i_14_ = i_14_ + 1 & 0xfff;
					anIntArrayArray3076[1 + i_10_][i_11_ - 1] = 9;
					anIntArrayArray3080[i_10_ + 1][i_11_ - 1] = i_18_;
				}
				if (i_10_ > 0
						&& i_11_ < 127
						&& anIntArrayArray3076[i_10_ - 1][1 + i_11_] == 0
						&& (anIntArrayArray3155[i_16_ - 1][1 + i_17_] & 0x4e240000) == 0
						&& (anIntArrayArray3155[i_16_ - 1][i_17_] & 0x42240000) == 0
						&& 0 == (anIntArrayArray3155[i_16_][1 + i_17_] & 0x48240000)) {
					routeFinderXArray[i_14_] = i_8_ - 1;
					routeFinderYArray[i_14_] = i_9_ + 1;
					i_14_ = i_14_ + 1 & 0xfff;
					anIntArrayArray3076[i_10_ - 1][i_11_ + 1] = 6;
					anIntArrayArray3080[i_10_ - 1][1 + i_11_] = i_18_;
				}
				if (i_10_ < 127
						&& i_11_ < 127
						&& anIntArrayArray3076[1 + i_10_][1 + i_11_] == 0
						&& 0 == (anIntArrayArray3155[1 + i_16_][1 + i_17_] & 0x78240000)
						&& 0 == (anIntArrayArray3155[1 + i_16_][i_17_] & 0x60240000)
						&& 0 == (anIntArrayArray3155[i_16_][i_17_ + 1] & 0x48240000)) {
					routeFinderXArray[i_14_] = 1 + i_8_;
					routeFinderYArray[i_14_] = 1 + i_9_;
					i_14_ = i_14_ + 1 & 0xfff;
					anIntArrayArray3076[i_10_ + 1][1 + i_11_] = 12;
					anIntArrayArray3080[i_10_ + 1][1 + i_11_] = i_18_;
				}
			}
				anInt3078 = 1021042197 * i_8_;
				anInt1684 = -575994451 * i_9_;
				return false;
		} catch (Exception e) {
		}
		return false;
		}
	
	private int calculateRoute(int startX, int startY, int size, PathStrategy pathStrategy,
			boolean alternativeRoute, int[] calculatedPositionXsArray, int[] calculatedPositionYsArray) {
		try {
			for (int i_4_ = 0; i_4_ < 128; i_4_++) {
				for (int i_5_ = 0; i_5_ < 128; i_5_++) {
					anIntArrayArray3076[i_4_][i_5_] = 0;
					anIntArrayArray3080[i_4_][i_5_] = 99999999;
				}
			}
			boolean bool_6_;
			if (size == 1) {
				bool_6_ = method4006(startX, startY, pathStrategy,
						-369838027);
			} else if (2 == size)
				bool_6_ = method2120(startX, startY, pathStrategy,
						-1327541421);
			else
				bool_6_ = method4593(startX, startY, size, pathStrategy,
						(byte) 58);
			int i_7_ = startX - 64;
			int i_8_ = startY - 64;
			int i_9_ = -1035599555 * anInt3078;
			int i_10_ = 1111664165 * anInt1684;
			if (!bool_6_) {
				if (alternativeRoute) {
					int i_11_ = 2147483647;
					int i_12_ = 2147483647;
					int i_13_ = 10;
					int i_14_ = pathStrategy.toX * -1331662251;
					int i_15_ = 1517720743 * pathStrategy.toY;
					int i_16_ = pathStrategy.sizeX * -1900284579;
					int i_17_ = 772610897 * pathStrategy.sizeY;
					for (int i_18_ = i_14_ - i_13_; i_18_ <= i_13_ + i_14_; i_18_++) {
						for (int i_19_ = i_15_ - i_13_; i_19_ <= i_15_ + i_13_; i_19_++) {
							int i_20_ = i_18_ - i_7_;
							int i_21_ = i_19_ - i_8_;
							if (i_20_ >= 0
									&& i_21_ >= 0
									&& i_20_ < 128
									&& i_21_ < 128
									&& (anIntArrayArray3080[i_20_][i_21_] < 100)) {
								int i_22_ = 0;
								if (i_18_ < i_14_)
									i_22_ = i_14_ - i_18_;
								else if (i_18_ > i_16_ + i_14_ - 1)
									i_22_ = i_18_ - (i_16_ + i_14_ - 1);
								int i_23_ = 0;
								if (i_19_ < i_15_)
									i_23_ = i_15_ - i_19_;
								else if (i_19_ > i_15_ + i_17_ - 1)
									i_23_ = i_19_ - (i_17_ + i_15_ - 1);
								int i_24_ = i_22_ * i_22_ + i_23_ * i_23_;
								if (i_24_ < i_11_
										|| (i_24_ == i_11_ && (anIntArrayArray3080[i_20_][i_21_]) < i_12_)) {
									i_11_ = i_24_;
									i_12_ = (anIntArrayArray3080[i_20_][i_21_]);
									i_9_ = i_18_;
									i_10_ = i_19_;
								}
							}
						}
					}
					if (i_11_ == 2147483647)
						return -1;
				} else
					return -1;
			}
			if (startX == i_9_ && i_10_ == startY)
				return 0;
			int i_25_ = 0;
			routeFinderXArray[i_25_] = i_9_;
			routeFinderYArray[i_25_++] = i_10_;
			int i_27_;
			int i_26_ = (i_27_ = anIntArrayArray3076[i_9_ - i_7_][i_10_
					- i_8_]);
			while (startX != i_9_ || startY != i_10_) {
				if (i_27_ != i_26_) {
					i_27_ = i_26_;
					routeFinderXArray[i_25_] = i_9_;
					routeFinderYArray[i_25_++] = i_10_;
				}
				if (0 != (i_26_ & 0x2))
					i_9_++;
				else if (0 != (i_26_ & 0x8))
					i_9_--;
				if ((i_26_ & 0x1) != 0)
					i_10_++;
				else if ((i_26_ & 0x4) != 0)
					i_10_--;
				i_26_ = anIntArrayArray3076[i_9_ - i_7_][i_10_ - i_8_];
			}
			int steps = 0;
			while_8_: do {
				do {
					if (i_25_-- <= 0)
						break while_8_;
					calculatedPositionXsArray[steps] = routeFinderXArray[i_25_];
					calculatedPositionYsArray[steps++] = routeFinderYArray[i_25_];
				} while (steps < calculatedPositionXsArray.length);
			} while (false);
			return steps;
		} catch (Exception e) {
		}
		return size;
	}
	
}