package com.rs.game.player.client;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import java.util.ArrayList;
import java.util.List;

/** Shared collision rules for native melee, independent of NPC identity or combat stats. */
public final class Native950MeleeReach {
    // The existing walk route finder has a 128-tile graph centred on its source.
    // Bound footprint work to half that width; the combat catalog may admit less.
    public static final int MAX_SIZE = 64;
    private static final int MAX_COORD = 0x3fff;
    private static final Collision WORLD = new Collision() {
        public boolean floorFree(int plane, int x, int y) {
            return World.isFloorFree(plane, x, y);
        }
        public boolean step(int plane, int x, int y, int dx, int dy) {
            return World.checkWalkStep(plane, x, y, dx, dy, 1);
        }
    };

    private Native950MeleeReach() { }

    /** At least one unobstructed cardinal edge must join the two complete footprints. */
    public static boolean canReach(Entity from, Entity target) {
        return from != null && target != null
                && canReach(from, from.getSize(), target, target.getSize(), WORLD);
    }

    /** Walls restrict their edges; they do not make the entire standing tile unusable. */
    public static boolean clearFootprint(WorldTile tile, int size) {
        return clearFootprint(tile, size, WORLD);
    }

    /** Collision-valid size-one approach destinations, ordered east, north, west, south. */
    public static List<WorldTile> contactTiles(WorldTile target, int size) {
        return contactTiles(target, size, WORLD);
    }

    static boolean canReach(WorldTile from, int fromSize, WorldTile target, int targetSize,
                            Collision collision) {
        if (!valid(from, fromSize) || !valid(target, targetSize)
                || from.getPlane() != target.getPlane()) return false;
        int ax = from.getX(), ay = from.getY();
        int bx = target.getX(), by = target.getY();
        int aRight = ax + fromSize - 1, aTop = ay + fromSize - 1;
        int bRight = bx + targetSize - 1, bTop = by + targetSize - 1;
        int sharedLow, sharedHigh, edgeX = 0, edgeY = 0, dx = 0, dy = 0;
        boolean vertical;
        if (aRight + 1 == bx || bRight + 1 == ax) {
            sharedLow = Math.max(ay, by);
            sharedHigh = Math.min(aTop, bTop);
            edgeX = aRight + 1 == bx ? aRight : ax;
            dx = aRight + 1 == bx ? 1 : -1;
            vertical = true;
        } else if (aTop + 1 == by || bTop + 1 == ay) {
            sharedLow = Math.max(ax, bx);
            sharedHigh = Math.min(aRight, bRight);
            edgeY = aTop + 1 == by ? aTop : ay;
            dy = aTop + 1 == by ? 1 : -1;
            vertical = false;
        } else return false; // Gaps and overlapping footprints cannot exchange melee hits.
        if (sharedLow > sharedHigh) return false; // Diagonal corners share no edge.
        if (!clearFootprint(from, fromSize, collision)
                || !clearFootprint(target, targetSize, collision)) return false;
        for (int coordinate = sharedLow; coordinate <= sharedHigh; coordinate++) {
            int x = vertical ? edgeX : coordinate;
            int y = vertical ? coordinate : edgeY;
            if (openEdge(from.getPlane(), x, y, dx, dy, collision)) return true;
        }
        return false;
    }

    static boolean clearFootprint(WorldTile tile, int size, Collision collision) {
        if (!valid(tile, size)) return false;
        for (int x = tile.getX(); x < tile.getX() + size; x++) {
            for (int y = tile.getY(); y < tile.getY() + size; y++) {
                if (!collision.floorFree(tile.getPlane(), x, y)) return false;
                // A larger body must not straddle an internal wall. Its outside
                // boundary can touch walls; only crossings inside the body matter.
                if (x + 1 < tile.getX() + size
                        && !openEdge(tile.getPlane(), x, y, 1, 0, collision)) return false;
                if (y + 1 < tile.getY() + size
                        && !openEdge(tile.getPlane(), x, y, 0, 1, collision)) return false;
            }
        }
        return true;
    }

    static List<WorldTile> contactTiles(WorldTile target, int size, Collision collision) {
        List<WorldTile> result = new ArrayList<>();
        if (!clearFootprint(target, size, collision)) return result;
        int x = target.getX(), y = target.getY(), plane = target.getPlane();
        for (int offset = 0; offset < size; offset++)
            addContact(result, plane, x + size, y + offset, -1, 0, collision);
        for (int offset = 0; offset < size; offset++)
            addContact(result, plane, x + offset, y + size, 0, -1, collision);
        for (int offset = 0; offset < size; offset++)
            addContact(result, plane, x - 1, y + offset, 1, 0, collision);
        for (int offset = 0; offset < size; offset++)
            addContact(result, plane, x + offset, y - 1, 0, 1, collision);
        return result;
    }

    private static void addContact(List<WorldTile> contacts, int plane, int x, int y,
                                   int dx, int dy, Collision collision) {
        if (x < 0 || y < 0 || x > MAX_COORD || y > MAX_COORD
                || !collision.floorFree(plane, x, y)) return;
        if (openEdge(plane, x, y, dx, dy, collision)) contacts.add(new WorldTile(x, y, plane));
    }

    private static boolean openEdge(int plane, int x, int y, int dx, int dy, Collision collision) {
        // checkWalkStep reads the destination's directional flag. Check the reverse as
        // well so a one-sided wall flag cannot permit a hit in either direction.
        return collision.step(plane, x, y, dx, dy)
                && collision.step(plane, x + dx, y + dy, -dx, -dy);
    }

    private static boolean valid(WorldTile tile, int size) {
        return tile != null && size >= 1 && size <= MAX_SIZE
                && tile.getPlane() >= 0 && tile.getPlane() <= 3
                && tile.getX() >= 0 && tile.getY() >= 0
                && tile.getX() + size - 1 <= MAX_COORD
                && tile.getY() + size - 1 <= MAX_COORD;
    }

    interface Collision {
        boolean floorFree(int plane, int x, int y);
        boolean step(int plane, int x, int y, int dx, int dy);
    }
}
