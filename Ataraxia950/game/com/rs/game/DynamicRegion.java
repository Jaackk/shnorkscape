package com.rs.game;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ObjectData;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.network.io.InputStream;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DynamicRegion extends Region {

	/**
	 * Contains render coordinates.
	 */
	private final int[][][][] regionCoords;
	private final boolean[][][] needsReload;
	private boolean recheckReload;

	public DynamicRegion(final int regionId) {
		super(regionId);
		// plane,x,y,(real x, real y,or real plane coord, or rotation)
		regionCoords = new int[4][8][8][4];
		needsReload = new boolean[4][8][8];
		for (int z = 0; z < 4; z++) {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					needsReload[z][x][y] = true;
				}
			}
		}
		recheckReload = false;
	}

	public static int[] translate(final int x, final int y, final int rotation) {
		final int[] coords = new int[2];
		if (rotation == 0) {
			coords[0] = x;
			coords[1] = y;
		} else if (rotation == 1) {
			coords[0] = y;
			coords[1] = 7 - x;
		} else if (rotation == 2) {
			coords[0] = 7 - x;
			coords[1] = 7 - y;
		} else {
			coords[0] = 7 - y;
			coords[1] = x;
		}
		return coords;
	}

	public static int[] translate(final int x, final int y, final int mapRotation, int sizeX, int sizeY, final int objectRotation) {
		final int[] coords = new int[2];
		if ((objectRotation & 0x1) == 1) {
			final int prevSizeX = sizeX;
			sizeX = sizeY;
			sizeY = prevSizeX;
		}
		if (mapRotation == 0) {
			coords[0] = x;
			coords[1] = y;
		} else if (mapRotation == 1) {
			coords[0] = y;
			coords[1] = 7 - x - (sizeX - 1);
		} else if (mapRotation == 2) {
			coords[0] = 7 - x - (sizeX - 1);
			coords[1] = 7 - y - (sizeY - 1);
		} else if (mapRotation == 3) {
			coords[0] = 7 - y - (sizeY - 1);
			coords[1] = x;
		}
		return coords;
	}

	@Override
	public void checkLoadMap() {
		if (recheckReload) {
			setLoadMapStage(0);
			recheckReload = false;
		}
		super.checkLoadMap();
	}

	@Override
	public void loadRegionMap() {
		for (int dynZ = 0; dynZ < 4; dynZ++) {
			for (int dynX = 0; dynX < 8; dynX++) {
				for (int dynY = 0; dynY < 8; dynY++) {
					if (!needsReload[dynZ][dynX][dynY]) {
						continue;
					}
					unloadChunk(dynX, dynY, dynZ);
				}
			}
		}
		for (int dynZ = 0; dynZ < 4; dynZ++) {
			for (int dynX = 0; dynX < 8; dynX++) {
				for (int dynY = 0; dynY < 8; dynY++) {
					if (!needsReload[dynZ][dynX][dynY]) {
						continue;
					}
					needsReload[dynZ][dynX][dynY] = false;
					final int renderChunkX = regionCoords[dynZ][dynX][dynY][0];
					final int renderChunkY = regionCoords[dynZ][dynX][dynY][1];
					final int renderChunkZ = regionCoords[dynZ][dynX][dynY][2];
					final int rotation = regionCoords[dynZ][dynX][dynY][3];
					final int renderLocalChunkX = renderChunkX - ((renderChunkX >> 3) << 3);
					final int renderLocalChunkY = renderChunkY - ((renderChunkY >> 3) << 3);
					if (renderChunkX == 0 && renderChunkY == 0 && renderChunkZ == 0 && rotation == 0) {
						continue;
					}
					final int mapID = (renderChunkX >> 3) << 8 | (renderChunkY >> 3);
					final int archiveId = Utils.getMapArchiveId((mapID >> 8), (mapID & 0xFF));
					final byte[] mapContainerData = Cache.STORE.getIndexes()[5].getFile(archiveId, 3);
					final byte[] landContainerData = Cache.STORE.getIndexes()[5].getFile(archiveId, 0);
					final byte[][][] mapSettings = mapContainerData == null ? null : new byte[4][64][64];
					if (mapContainerData != null) {
						final InputStream stream = new InputStream(mapContainerData);
						for (int plane = 0; plane < 4; plane++) {
							for (int x = 0; x < 64; x++) {
								for (int y = 0; y < 64; y++) {

									final int value = stream.readUnsignedByte();
									if ((value & 0x1) != 0) {
										stream.readUnsignedByte();
										stream.readUnsignedSmart();

									}
									if ((value & 0x2) != 0) {
										mapSettings[plane][x][y] = (byte) stream.readByte();

									}
									if ((value & 0x4) != 0) {
										stream.readUnsignedSmart(); // setted to
																	// 30

									}
									if ((value & 0x8) != 0) {
										stream.readUnsignedByte();

									}
								}
							}
						}
						for (int z = 0; z < 4; z++) {
							for (int x = 0; x < 64; x++) {
								for (int y = 0; y < 64; y++) {
									if ((mapSettings[z][x][y] & 0x1) == 1) {
										int realZ = z;
										if ((mapSettings[1][x][y] & 0x2) == 2) {
											realZ--;
										}
										if (realZ == renderChunkZ && (x >> 3) == renderLocalChunkX
												&& (y >> 3) == renderLocalChunkY) {
											final int[] coords = translate(x & 0x7, y & 0x7, rotation);
											forceGetRegionMap().addFloor(dynZ, (dynX << 3) | coords[0],
													(dynY << 3) | coords[1]);// XXX
																				// change
																				// to
																				// addUnwalkable.
										}
									}
								}
							}
						}
					} else {
						for (int z = 0; z < 4; z++) {
							for (int x = 0; x < 64; x++) {
								for (int y = 0; y < 64; y++) {
									if (z == renderChunkZ && (x >> 3) == renderLocalChunkX
											&& (y >> 3) == renderLocalChunkY) {
										final int[] coords = translate(x & 0x7, y & 0x7, rotation);
										forceGetRegionMap().addFloor(dynZ, (dynX << 3) | coords[0],
												(dynY << 3) | coords[1]);// XXX
																			// change
																			// to
																			// addUnwalkable.
									}
								}
							}
						}
					}

					if (landContainerData != null) {
						final InputStream landStream = new InputStream(landContainerData);
						int objectId = -1;
						int incr;
						while ((incr = landStream.readSmart2()) != 0) {
							objectId += incr;
							int location = 0;
							int incr2;
							while ((incr2 = landStream.readUnsignedSmart()) != 0) {
								location += incr2 - 1;
								final int x = (location >> 6 & 0x3f);
								final int y = (location & 0x3f);
								final int z = location >> 12;
						        ObjectData objectData = new ObjectData(landStream);
								final int type = objectData.type;
								final int rot = objectData.rotation;
								int realZ = z;
								if (mapSettings != null && (mapSettings[1][x][y] & 2) == 2) {
									realZ--;
								}
								if (realZ == renderChunkZ && (x >> 3) == renderLocalChunkX
										&& (y >> 3) == renderLocalChunkY) {
									final ObjectDefinitions definition = ObjectDefinitions.getObjectDefinitions(objectId);
									final int[] coords = translate(x & 0x7, y & 0x7, rotation, definition.sizeX,
											definition.sizeY, rot);
									spawnObject(
											new WorldObject(objectId, type, (rotation + rot) & 0x3,
													(dynX << 3) + coords[0] + ((getRegionId() >> 8) << 6),
													(dynY << 3) + coords[1] + ((getRegionId() & 0xFF) << 6), dynZ),
											dynZ, (dynX << 3) + coords[0], (dynY << 3) + coords[1], true);
								}
							}
						}
					}

				}
			}
		}
	}

	private void unloadChunk(final int chunkX, final int chunkY, final int chunkZ) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				final int fullX = (chunkX << 3) | x;
				final int fullY = (chunkY << 3) | y;
				if (objects != null) {
					for (int slot = 0; slot < 4; slot++) {
						final short hash = (short) (fullX | fullY << 6 | slot << 12 | chunkZ << 14);
						objects.remove(hash);
						//objects[chunkZ][fullX][fullY][slot] = null;
					}
				}
				if (map != null) {
					map.setMask(chunkZ, fullX, fullY, 0);
				}
				if (clipedOnlyMap != null) {
					clipedOnlyMap.setMask(chunkZ, fullX, fullY, 0);
				}

				final List<WorldObject> ro = new ArrayList<WorldObject>(removedOriginalObjects);
				// List<WorldObject> ao = new
				// ArrayList<WorldObject>(spawnedObjects);
				for (final WorldObject removed : ro) {
					if (removed.getPlane() == chunkZ && removed.getChunkX() == chunkX && removed.getChunkY() == chunkY) {
						removedOriginalObjects.remove(removed);
				/*
				 * for (WorldObject added : ro) if (added.getPlane() == chunkZ
				 * && added.getChunkX() == chunkX && added.getChunkY() ==
				 * chunkY) spawnedObjects.remove(ao);
				 */
					}
				}
			}
		}
	}

	@Override
	public int getRotation(final int plane, final int x, final int y) {
		return regionCoords[plane][x][y][3];
	}

	public void setRotation(final int plane, final int x, final int y, final int rotation) {
		regionCoords[plane][x][y][3] = rotation;
		setReloadObjects(plane, x, y);
	}

	public void setReloadObjects(final int plane, final int x, final int y) {
		needsReload[plane][x][y] = true;
		recheckReload = true;
	}

	public int[][][][] getRegionCoords() {
		return regionCoords;
	}
}
