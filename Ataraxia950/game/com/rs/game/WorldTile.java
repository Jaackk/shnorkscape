package com.rs.game;

import com.rs.Settings;
import com.rs.utils.MapUtils;
import com.rs.utils.MapUtils.Structure;
import com.rs.utils.Utils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;
import static com.rs.cache.loaders.WorldAreaTypeDefinitions.LOOK_UP;
import static com.rs.cache.loaders.WorldAreaTypeDefinitions.defaultMapped;
public class WorldTile implements Serializable {

    private static final long serialVersionUID = -6567346497259686765L;
    private short x, y;
    private byte plane;

    public WorldTile(final int hash) {
        x = (short) (hash >> 14 & 0x3fff);
        y = (short) (hash & 0x3fff);
        plane = (byte) (hash >> 28);
    }

    public WorldTile(final int x, final int y, final int plane) {
        this.x = (short) x;
        this.y = (short) y;
        this.plane = (byte) plane;
    }

    public WorldTile(final WorldTile tile) {
        x = tile.x;
        y = tile.y;
        plane = tile.plane;
    }

    public WorldTile(int x, int y) {
        this(x, y, 0);
    }

    public WorldTile(final WorldTile tile, final int randomize) {
        x = (short) (tile.x + Utils.random(1, randomize * 2) - randomize);
        y = (short) (tile.y + Utils.random(1, randomize * 2) - randomize);
        plane = tile.plane;
    }

    public WorldTile(final int x, final int y, final int plane, final int randomize) {
        this.x = (short) (x + Utils.random(1, randomize * 2) - randomize);
        this.y = (short) (y + Utils.random(1, randomize * 2) - randomize);
        this.plane = (byte) plane;
    }

    public static final int getCoordFaceX(final int x, final int sizeX, final int sizeY, final int rotation) {
        return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
    }

    public static final int getCoordFaceY(final int y, final int sizeX, final int sizeY, final int rotation) {
        return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
    }

    public int getChunkX() {
        return (x >> 3);
    }

    public int getChunkY() {
        return (y >> 3);
    }

    public int getCoordFaceX(final int sizeX) {
        return getCoordFaceX(sizeX, -1, -1);
    }

    public int getCoordFaceX(final int sizeX, final int sizeY, final int rotation) {
        return x + ((rotation == 1 || rotation == 3 ? sizeY : sizeX) - 1) / 2;
    }

    public int getCoordFaceY(final int sizeY) {
        return getCoordFaceY(-1, sizeY, -1);
    }

    public int getCoordFaceY(final int sizeX, final int sizeY, final int rotation) {
        return y + ((rotation == 1 || rotation == 3 ? sizeX : sizeY) - 1) / 2;
    }

    public int getLocalX() {
        return getLocalX(this);
    }

    public int getLocalX(final WorldTile tile) {
        return getLocalX(tile, 0);
    }

    public int getLocalX(final WorldTile tile, final int mapSize) {
        return x - 8 * (tile.getChunkX() - (Settings.MAP_SIZES[mapSize] >> 4));
    }

    public int getLocalY() {
        return getLocalY(this);
    }

    public int getLocalY(final WorldTile tile) {
        return getLocalY(tile, 0);
    }

    public int getLocalY(final WorldTile tile, final int mapSize) {
        return y - 8 * (tile.getChunkY() - (Settings.MAP_SIZES[mapSize] >> 4));
    }

    public int getPlane() {
        if (plane > 3) {
            return 3;
        }
        return plane;
    }

    public int getRegionHash() {
        return getRegionY() + (getRegionX() << 8) + (plane << 16);
    }

    public int getRegionId() {
        return ((getRegionX() << 8) + getRegionY());
    }

    public static final int getRegionId(final int x, final int y) {
        return (((x >> 6) << 8) + (y >> 6));
    }

    public int getRegionX() {
        return (x >> 6);
    }

    public int getRegionY() {
        return (y >> 6);
    }

    public int getTileHash() {
        return y + (x << 14) + (plane << 28);
    }

    public int getX() {
        return x;
    }

    public int getXInRegion() {
        return x & 0x3F;
    }

    public int getY() {
        return y;
    }

    public int getYInRegion() {
        return y & 0x3F;
    }

    /**
     * Checks if this world tile's coordinates match the other world tile.
     *
     * @param other The world tile to compare with.
     * @return {@code True} if so.
     */
    public boolean matches(final WorldTile other) {
        return x == other.x && y == other.y && plane == other.plane;
    }

    public void moveLocation(final int xOffset, final int yOffset, final int planeOffset) {
        x += xOffset;
        y += yOffset;
        plane += planeOffset;
    }

    public final void setLocation(final int x, final int y, final int plane) {
        this.x = (short) x;
        this.y = (short) y;
        this.plane = (byte) plane;
    }

    public final void setLocation(final WorldTile tile) {
        setLocation(tile.x, tile.y, tile.plane);
    }

    public WorldTile transform(final int x, final int y, final int plane) {
        return new WorldTile(this.x + x, this.y + y, this.plane + plane);
    }

    public boolean withinDistance(final WorldTile tile) {
        if (tile.plane != plane) {
            return false;
        }
        return Math.abs(tile.x - x) <= 14 && Math.abs(tile.y - y) <= 14;
    }

    public boolean withinDistance(@Nullable final WorldTile tile, final int distance) {
        if (tile == null) {
            return false;
        }
        if (tile.plane != plane) {
            return false;
        }
        final int deltaX = tile.x - x, deltaY = tile.y - y;
        return deltaX <= distance && deltaX >= -distance && deltaY <= distance && deltaY >= -distance;
    }

    public int getDistance(final WorldTile from) {
        final int distanceX = Math.abs(from.getX() - getX());
        final int distanceY = Math.abs(from.getY() - getY());
        return (int) Math.ceil((distanceX + distanceY) / 2);
    }

    public int getFurthestDistance(final WorldTile from) {
        final int distanceX = Math.abs(from.getX() - getX());
        final int distanceY = Math.abs(from.getY() - getY());
        return distanceX > distanceY ? distanceX : distanceY;
    }

    public int getHash() {
        return y | x << 14 | plane << 28;
    }

    public boolean withinArea(final int a, final int b, final int c, final int d) {
        return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
    }

    public int getXInChunk() {
        return x & 0x7;
    }

    public int getYInChunk() {
        return y & 0x7;
    }

    @Override
    public String toString() {
        return "[ " + x + ", " + y + ", " + plane + " ]";
    }

    public static final int getHash(final int x, final int y, final int plane) {
        return y | x << 14 | plane << 28;
    }

    public int getXInScene(Entity entity) {
        return getX() - MapUtils.decode(Structure.CHUNK, entity.getSceneBaseChunkId())[0] * 8;
    }

    public int getYInScene(Entity entity) {
        return getY() - MapUtils.decode(Structure.CHUNK, entity.getSceneBaseChunkId())[1] * 8;
    }

    @Override
    public int hashCode() {
        return Objects.hash(plane, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof WorldTile) {
            WorldTile other = (WorldTile) obj;
            return plane == other.plane && x == other.x && y == other.y;
        }
        return false;
    }
    
    public int getRegionKey() {
        int mapId = getRegionX() | (getRegionY() << 7);
        int[] mapped = !LOOK_UP.containsKey(mapId) ? defaultMapped : LOOK_UP.get(mapId);
        int x = getChunkX() % 8;
        int y = getChunkY() % 8;
        return mapped[x * 8 + y];
    }
    
    public static int getRegionKey(int regionX, int regionY, int chunkX, int chunkY) {
        int mapId = regionX | (regionY << 7);
        int[] mapped = !LOOK_UP.containsKey(mapId) ? defaultMapped : LOOK_UP.get(mapId);
        int x = chunkX % 8;
        int y = chunkY % 8;
        return mapped[x * 8 + y];
    }
}