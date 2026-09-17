package com.rs.game;

import java.util.List;

import com.rs.game.player.Player;

public final class MapBuilder {

    // used by construction preview
    public static final int[] FORCE_LOAD_REGIONS = { 7503, 7759 };

    public static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;
    private static final Object ALGORITHM_LOCK = new Object();
    private static final int MAX_REGION_X = 127;
    private static final int MAX_REGION_Y = 255;

    private MapBuilder() {

    }

    public static void init() {
        for (final int regionId : FORCE_LOAD_REGIONS) {
            World.getRegion(regionId, true);
        }
    }

    public static int[] findEmptyRegionBound(final int widthChunks, final int heightChunks) {
        final int regionHash = findEmptyRegionHash(widthChunks, heightChunks);
        return new int[] { (regionHash >> 8), regionHash & 0xff };
    }

    public static int[] findEmptyChunkBound(final int widthChunks, final int heightChunks) {
        final int[] map = findEmptyRegionBound(widthChunks, heightChunks);
        map[0] *= 8;
        map[1] *= 8;
        return map;
    }

    public static int getRegionId(final int mapX, final int mapY) {
        return (mapX << 8) + mapY;
    }

    public static int findEmptyRegionHash(int widthChunks, int heightChunks) {
        int regionsDistanceX = 1;
        while (widthChunks > 8) {
            regionsDistanceX += 1;
            widthChunks -= 8;
        }
        int regionsDistanceY = 1;
        while (heightChunks > 8) {
            regionsDistanceY += 1;
            heightChunks -= 8;
        }
        synchronized (ALGORITHM_LOCK) {
            for (int regionX = 1; regionX <= MAX_REGION_X - regionsDistanceX; regionX++) {
                skip: for (int regionY = 1; regionY <= MAX_REGION_Y - regionsDistanceY; regionY++) {
                    final int regionHash = getRegionId(regionX, regionY); // map
                    // hash
                    // because
                    // skiping
                    // to next
                    // map up
                    for (int checkRegionX = regionX - 1; checkRegionX <= regionX + regionsDistanceX; checkRegionX++) {
                        for (int checkRegionY = regionY - 1; checkRegionY <= regionY + regionsDistanceY; checkRegionY++) {
                            final int hash = getRegionId(checkRegionX, checkRegionY);
                            if (regionExists(hash)) {
                                continue skip;
                            }

                        }
                    }
                    DynamicArea.addDynamicArea(regionX, regionY, regionsDistanceX, regionsDistanceY);
                    return regionHash;
                }
            }
        }
        return -1;

    }

    public static boolean regionExists(final int mapHash) {
        return DynamicArea.regionExists(mapHash);
    }

    public static void cutChunk(final int chunkX, final int chunkY, final int plane) {
        final DynamicRegion toRegion = createDynamicRegion((((chunkX / 8) << 8) + (chunkY / 8)));
        final int offsetX = (chunkX - ((chunkX / 8) * 8));
        final int offsetY = (chunkY - ((chunkY / 8) * 8));
        toRegion.getRegionCoords()[plane][offsetX][offsetY][0] = 0;
        toRegion.getRegionCoords()[plane][offsetX][offsetY][1] = 0;
        toRegion.getRegionCoords()[plane][offsetX][offsetY][2] = 0;
        toRegion.getRegionCoords()[plane][offsetX][offsetY][3] = 0;
        toRegion.setReloadObjects(plane, offsetX, offsetY);
    }

    public static final void destroyMap(final int chunkX, final int chunkY, int widthRegions, int heightRegions) {
        synchronized (ALGORITHM_LOCK) {
            final int fromRegionX = chunkX / 8;
            final int fromRegionY = chunkY / 8;
            int regionsDistanceX = 1;
            while (widthRegions > 8) {
                regionsDistanceX += 1;
                widthRegions -= 8;
            }
            int regionsDistanceY = 1;
            while (heightRegions > 8) {
                regionsDistanceY += 1;
                heightRegions -= 8;
            }
            for (int regionX = fromRegionX; regionX < fromRegionX + regionsDistanceX; regionX++) {
                for (int regionY = fromRegionY; regionY < fromRegionY + regionsDistanceY; regionY++) {
                    int mapHash = getRegionId(regionX, regionY);
                    DynamicArea.removeDynamicArea(mapHash);
                    destroyRegion(mapHash);
                }
            }
        }
    }

    public static final void repeatMap(final int toChunkX, final int toChunkY, final int widthChunks, final int heightChunks, final int rx, final int ry, final int plane, final int rotation, final int... toPlanes) {
        for (int xOffset = 0; xOffset < widthChunks; xOffset++) {
            for (int yOffset = 0; yOffset < heightChunks; yOffset++) {
                final int nextChunkX = toChunkX + xOffset;
                final int nextChunkY = toChunkY + yOffset;
                final DynamicRegion toRegion = createDynamicRegion((((nextChunkX / 8) << 8) + (nextChunkY / 8)));
                final int regionOffsetX = (nextChunkX - ((nextChunkX / 8) * 8));
                final int regionOffsetY = (nextChunkY - ((nextChunkY / 8) * 8));
                for (int pIndex = 0; pIndex < toPlanes.length; pIndex++) {
                    final int toPlane = toPlanes[pIndex];
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][0] = rx;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][1] = ry;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][2] = plane;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][3] = rotation;
                    toRegion.setReloadObjects(toPlane, regionOffsetX, regionOffsetY);
                }
            }
        }
    }

    public static final void cutMap(final int toChunkX, final int toChunkY, final int widthChunks, final int heightChunks, final int... toPlanes) {
        for (int xOffset = 0; xOffset < widthChunks; xOffset++) {
            for (int yOffset = 0; yOffset < heightChunks; yOffset++) {
                final int nextChunkX = toChunkX + xOffset;
                final int nextChunkY = toChunkY + yOffset;
                final DynamicRegion toRegion = createDynamicRegion((((nextChunkX / 8) << 8) + (nextChunkY / 8)));
                final int regionOffsetX = (nextChunkX - ((nextChunkX / 8) * 8));
                final int regionOffsetY = (nextChunkY - ((nextChunkY / 8) * 8));
                for (int pIndex = 0; pIndex < toPlanes.length; pIndex++) {
                    final int toPlane = toPlanes[pIndex];
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][0] = 0;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][1] = 0;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][2] = 0;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][3] = 0;
                    toRegion.setReloadObjects(toPlane, regionOffsetX, regionOffsetY);
                }
            }
        }
    }

    /*
     * copys a single 8x8 map tile and allows you to rotate it
     */
    public static void copyChunk(final int fromChunkX, final int fromChunkY, final int fromPlane, final int toChunkX, final int toChunkY, final int toPlane, final int rotation) {
        final DynamicRegion toRegion = createDynamicRegion(((toChunkX / 8) << 8) + (toChunkY / 8));
        final int regionOffsetX = toChunkX - ((toChunkX / 8) * 8);
        final int regionOffsetY = toChunkY - ((toChunkY / 8) * 8);
        toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][0] = fromChunkX;
        toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][1] = fromChunkY;
        toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][2] = fromPlane;
        toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][3] = rotation;
        toRegion.setReloadObjects(toPlane, regionOffsetX, regionOffsetY);
    }

    /*
     * copy a exactly square of map from a place to another
     */
    public static final void copyAllPlanesMap(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int ratio) {
        final int[] planes = new int[4];
        for (int plane = 1; plane < 4; plane++) {
            planes[plane] = plane;
        }
        copyMap(fromRegionX, fromRegionY, toRegionX, toRegionY, ratio, ratio, planes, planes);
    }

    /*
     * copy a exactly square of map from a place to another
     */
    public static final void copyAllPlanesMap(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int widthRegions, final int heightRegions) {
        final int[] planes = new int[4];
        for (int plane = 1; plane < 4; plane++) {
            planes[plane] = plane;
        }
        copyMap(fromRegionX, fromRegionY, toRegionX, toRegionY, widthRegions, heightRegions, planes, planes);
    }

    /*
     * copy a square of map from a place to another
     */
    public static final void copyMap(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int ratio, final int[] fromPlanes, final int[] toPlanes) {
        copyMap(fromRegionX, fromRegionY, toRegionX, toRegionY, ratio, ratio, fromPlanes, toPlanes);
    }

    /*
     * copy a rectangle of map from a place to another
     */
    public static final void copyMap(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int widthRegions, final int heightRegions, final int[] fromPlanes, final int[] toPlanes) {
        if (fromPlanes.length != toPlanes.length) {
            throw new RuntimeException("PLANES LENGTH ISNT SAME OF THE NEW PLANES ORDER!");
        }
        for (int xOffset = 0; xOffset < widthRegions; xOffset++) {
            for (int yOffset = 0; yOffset < heightRegions; yOffset++) {
                final int fromThisRegionX = fromRegionX + xOffset;
                final int fromThisRegionY = fromRegionY + yOffset;
                final int toThisRegionX = toRegionX + xOffset;
                final int toThisRegionY = toRegionY + yOffset;
                final int regionId = ((toThisRegionX / 8) << 8) + (toThisRegionY / 8);
                final DynamicRegion toRegion = createDynamicRegion(regionId);
                final int regionOffsetX = (toThisRegionX - ((toThisRegionX / 8) * 8));
                final int regionOffsetY = (toThisRegionY - ((toThisRegionY / 8) * 8));
                for (int pIndex = 0; pIndex < fromPlanes.length; pIndex++) {
                    final int toPlane = toPlanes[pIndex];
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][0] = fromThisRegionX;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][1] = fromThisRegionY;
                    toRegion.getRegionCoords()[toPlane][regionOffsetX][regionOffsetY][2] = fromPlanes[pIndex];
                    toRegion.setReloadObjects(toPlane, regionOffsetX, regionOffsetY);
                }
            }
        }
    }

    /*
     * temporary and used for dungeonnering only
     *
     * //rotation 0 // a b // c d //rotation 1 // c a // d b //rotation2 // d c // b
     * a //rotation3 // b d // a c
     */
    public static final void copy2RatioSquare(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int rotation) {
        if (rotation == 0) {
            copyChunk(fromRegionX, fromRegionY, 0, toRegionX, toRegionY, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY, 0, toRegionX + 1, toRegionY, 0, rotation);
            copyChunk(fromRegionX, fromRegionY + 1, 0, toRegionX, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY + 1, 0, toRegionX + 1, toRegionY + 1, 0, rotation);
        } else if (rotation == 1) {
            copyChunk(fromRegionX, fromRegionY, 0, toRegionX, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY, 0, toRegionX, toRegionY, 0, rotation);
            copyChunk(fromRegionX, fromRegionY + 1, 0, toRegionX + 1, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY + 1, 0, toRegionX + 1, toRegionY, 0, rotation);
        } else if (rotation == 2) {
            copyChunk(fromRegionX, fromRegionY, 0, toRegionX + 1, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY, 0, toRegionX, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX, fromRegionY + 1, 0, toRegionX + 1, toRegionY, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY + 1, 0, toRegionX, toRegionY, 0, rotation);
        } else if (rotation == 3) {
            copyChunk(fromRegionX, fromRegionY, 0, toRegionX + 1, toRegionY, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY, 0, toRegionX + 1, toRegionY + 1, 0, rotation);
            copyChunk(fromRegionX, fromRegionY + 1, 0, toRegionX, toRegionY, 0, rotation);
            copyChunk(fromRegionX + 1, fromRegionY + 1, 0, toRegionX, toRegionY + 1, 0, rotation);
        }
    }

    /*
     * not recommended to use unless you want to make a more complex map
     */
    public static DynamicRegion createDynamicRegion(final int regionId) {
        synchronized (ALGORITHM_LOCK) {
            final Region region = World.getRegions().get(regionId);
            if (region != null) {
                if (region instanceof DynamicRegion) {
                    // lets
                    // keep building it
                    return (DynamicRegion) region;
                } else {
                    destroyRegion(regionId);
                }
            }
            final DynamicRegion newRegion = new DynamicRegion(regionId);
            World.getRegions().put(regionId, newRegion);
            return newRegion;
        }
    }

    /*
     * Safely destroys a dynamic region
     */
    public static void destroyRegion(final int regionId) {
        final Region region = World.getRegions().get(regionId);
        if (region != null) {
            final List<Integer> playerIndexes = region.getPlayerIndexes();
            /*
             * if (region.getGroundItems() != null) region.getGroundItems().clear();
             */
            region.getSpawnedObjects().clear();
            region.getRemovedOriginalObjects().clear();
            World.getRegions().remove(regionId);
            if (playerIndexes != null) {
                for (final int playerIndex : playerIndexes) {
                    final Player player = World.getPlayers().get(playerIndex);
                    if (player == null || player.hasFinished()) {
                        continue;
                    }
                    player.setForceNextMapLoadRefresh(true);
                    player.loadMapRegions();
                }
            }
        }
    }

    public static final void copy2RatioSquare(final int fromRegionX, final int fromRegionY, final int toRegionX, final int toRegionY, final int rotation, final int... planes) {
        for (final int i : planes) { // plane 1 and 2
            if (rotation == 0) {
                copyChunk(fromRegionX, fromRegionY, i, toRegionX, toRegionY, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY, i, toRegionX + 1, toRegionY, i, rotation);
                copyChunk(fromRegionX, fromRegionY + 1, i, toRegionX, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY + 1, i, toRegionX + 1, toRegionY + 1, i, rotation);
            } else if (rotation == 1) {
                copyChunk(fromRegionX, fromRegionY, i, toRegionX, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY, i, toRegionX, toRegionY, i, rotation);
                copyChunk(fromRegionX, fromRegionY + 1, i, toRegionX + 1, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY + 1, i, toRegionX + 1, toRegionY, i, rotation);
            } else if (rotation == 2) {
                copyChunk(fromRegionX, fromRegionY, i, toRegionX + 1, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY, i, toRegionX, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX, fromRegionY + 1, i, toRegionX + 1, toRegionY, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY + 1, i, toRegionX, toRegionY, i, rotation);
            } else if (rotation == 3) {
                copyChunk(fromRegionX, fromRegionY, i, toRegionX + 1, toRegionY, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY, i, toRegionX + 1, toRegionY + 1, i, rotation);
                copyChunk(fromRegionX, fromRegionY + 1, i, toRegionX, toRegionY, i, rotation);
                copyChunk(fromRegionX + 1, fromRegionY + 1, i, toRegionX, toRegionY + 1, i, rotation);
            }
        }
    }
}
