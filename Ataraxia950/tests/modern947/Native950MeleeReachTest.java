package com.rs.game.player.client;

import com.rs.game.WorldTile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/** Footprint and wall regressions without the global world registry or cache. */
public class Native950MeleeReachTest {
    @Test public void sizeTwoCreatureOffersEveryCardinalContactAndNoCorners() {
        Grid grid = new Grid();
        WorldTile creature = tile(100, 100);
        List<WorldTile> contacts = Native950MeleeReach.contactTiles(creature, 2, grid);
        assertEquals(set("102,100", "102,101", "100,102", "101,102", "99,100", "99,101", "100,99", "101,99"), coordinates(contacts));
        assertEquals(8, contacts.size());
        for (WorldTile player : contacts) {
            assertTrue(Native950MeleeReach.canReach(player, 1, creature, 2, grid));
            assertTrue(Native950MeleeReach.canReach(creature, 2, player, 1, grid));
        }
        assertFalse(Native950MeleeReach.canReach(tile(102, 102), 1, creature, 2, grid));
    }

    @Test public void sizeThreeFarEdgesAndMiddleContactsUseTheFootprintNotItsAnchor() {
        Grid grid = new Grid();
        WorldTile creature = tile(100, 100);
        assertEquals(set("103,100", "103,101", "103,102", "100,103", "101,103", "102,103", "99,100", "99,101", "99,102", "100,99", "101,99", "102,99"),
                coordinates(Native950MeleeReach.contactTiles(creature, 3, grid)));
        assertTrue(Native950MeleeReach.canReach(tile(103, 102), 1, creature, 3, grid));
        assertTrue(Native950MeleeReach.canReach(tile(101, 103), 1, creature, 3, grid));
        grid.blockStep(103, 101, -1, 0);
        assertFalse(Native950MeleeReach.canReach(tile(103, 101), 1, creature, 3, grid));
        assertTrue(Native950MeleeReach.canReach(tile(103, 102), 1, creature, 3, grid));
        assertFalse(coordinates(Native950MeleeReach.contactTiles(creature, 3, grid)).contains("103,101"));
    }

    @Test public void oneSidedWallBlocksHitsInBothDirectionsAndRemovesOnlyThatContact() {
        Grid grid = new Grid();
        WorldTile player = tile(99, 101), creature = tile(100, 100);
        grid.blockStep(100, 101, -1, 0); // Only the reverse direction is blocked.
        assertFalse(Native950MeleeReach.canReach(player, 1, creature, 2, grid));
        assertFalse(Native950MeleeReach.canReach(creature, 2, player, 1, grid));
        List<WorldTile> contacts = Native950MeleeReach.contactTiles(creature, 2, grid);
        assertEquals(7, contacts.size());
        assertFalse(coordinates(contacts).contains("99,101"));
        assertTrue(Native950MeleeReach.clearFootprint(player, 1, grid));
        assertTrue(Native950MeleeReach.clearFootprint(creature, 2, grid));
    }

    @Test public void overlappingActorsAndDiagonalCornersNeverHitEvenOnOpenGround() {
        Grid grid = new Grid();
        WorldTile creature = tile(100, 100);
        assertFalse(Native950MeleeReach.canReach(tile(101, 101), 1, creature, 3, grid));
        assertFalse(Native950MeleeReach.canReach(tile(99, 99), 2, creature, 3, grid));
        assertFalse(Native950MeleeReach.canReach(tile(99, 103), 1, creature, 3, grid));
        assertFalse(Native950MeleeReach.canReach(tile(104, 101), 1, creature, 3, grid));
        assertEquals(0, grid.queries);
    }

    @Test public void sceneryAnywhereUnderTheNpcPreventsUnsafeSpawnAndContact() {
        Grid grid = new Grid();
        grid.blockedFloors.add("101,101");
        WorldTile creature = tile(100, 100);
        assertFalse(Native950MeleeReach.clearFootprint(creature, 3, grid));
        assertFalse(Native950MeleeReach.canReach(tile(99, 100), 1, creature, 3, grid));
        assertTrue(Native950MeleeReach.contactTiles(creature, 3, grid).isEmpty());
        grid.blockedFloors.clear();
        grid.blockedFloors.add("99,100");
        assertTrue(Native950MeleeReach.clearFootprint(creature, 3, grid));
        assertEquals(11, Native950MeleeReach.contactTiles(creature, 3, grid).size());
        assertFalse(Native950MeleeReach.canReach(tile(99, 100), 1, creature, 3, grid));
    }

    @Test public void largerFootprintsCannotStraddleInternalWallsButCanTouchOutsideWalls() {
        Grid grid = new Grid();
        WorldTile creature = tile(100, 100);
        grid.blockStep(101, 101, 1, 0); // Across the inside of a size-three body.
        assertFalse(Native950MeleeReach.clearFootprint(creature, 3, grid));
        assertFalse(Native950MeleeReach.canReach(tile(99, 100), 1, creature, 3, grid));
        assertTrue(Native950MeleeReach.contactTiles(creature, 3, grid).isEmpty());
        grid.blockedSteps.clear();
        grid.blockStep(100, 101, -1, 0); // Along its outside boundary instead.
        assertTrue(Native950MeleeReach.clearFootprint(creature, 3, grid));
        assertEquals(11, Native950MeleeReach.contactTiles(creature, 3, grid).size());
        assertTrue(Native950MeleeReach.clearFootprint(tile(100, 101), 1, grid));
    }

    @Test public void largerActorsCanUseAnOpenPartOfTheirSharedEdge() {
        Grid grid = new Grid();
        WorldTile first = tile(100, 100), second = tile(102, 101);
        assertTrue(Native950MeleeReach.canReach(first, 2, second, 3, grid));
        grid.blockStep(101, 101, 1, 0);
        assertFalse(Native950MeleeReach.canReach(first, 2, second, 3, grid));
        WorldTile aligned = tile(102, 100);
        assertTrue(Native950MeleeReach.canReach(first, 2, aligned, 3, grid));
        grid.blockStep(101, 100, 1, 0);
        assertFalse(Native950MeleeReach.canReach(first, 2, aligned, 3, grid));
    }

    @Test public void invalidPlanesSizesAndCoordinatesFailBeforeCollisionReads() {
        Grid grid = new Grid();
        WorldTile creature = tile(100, 100);
        assertFalse(Native950MeleeReach.canReach(new WorldTile(99, 100, 1), 1, creature, 1, grid));
        assertFalse(Native950MeleeReach.clearFootprint(null, 1, grid));
        assertFalse(Native950MeleeReach.clearFootprint(creature, 0, grid));
        assertFalse(Native950MeleeReach.clearFootprint(creature, Native950MeleeReach.MAX_SIZE + 1, grid));
        // WorldTile.getPlane() normalises values above three to plane three.
        // Negative planes remain invalid in the public tile contract.
        assertFalse(Native950MeleeReach.clearFootprint(new WorldTile(100, 100, -1), 1, grid));
        assertFalse(Native950MeleeReach.clearFootprint(tile(-1, 100), 1, grid));
        assertFalse(Native950MeleeReach.clearFootprint(tile(16383, 100), 2, grid));
        assertTrue(Native950MeleeReach.contactTiles(creature, -1, grid).isEmpty());
        assertEquals(0, grid.queries);
    }

    @Test public void mapBoundaryContactsAreClippedWithoutWrappingCoordinates() {
        Grid grid = new Grid();
        assertEquals(set("1,0", "0,1"), coordinates(Native950MeleeReach.contactTiles(tile(0, 0), 1, grid)));
        assertEquals(set("16382,16383", "16383,16382"), coordinates(Native950MeleeReach.contactTiles(tile(16383, 16383), 1, grid)));
        assertEquals(256, Native950MeleeReach.contactTiles(tile(100, 100), 64, grid).size());
    }

    private static WorldTile tile(int x, int y) { return new WorldTile(x, y, 0); }
    private static Set<String> set(String... values) { return new HashSet<>(Arrays.asList(values)); }
    private static Set<String> coordinates(List<WorldTile> tiles) {
        Set<String> result = new HashSet<>();
        for (WorldTile tile : tiles) result.add(tile.getX() + "," + tile.getY());
        return result;
    }
    private static final class Grid implements Native950MeleeReach.Collision {
        final Set<String> blockedFloors = new HashSet<>(), blockedSteps = new HashSet<>();
        int queries;
        void blockStep(int x, int y, int dx, int dy) { blockedSteps.add(x + "," + y + "," + dx + "," + dy); }
        public boolean floorFree(int plane, int x, int y) {
            queries++;
            return !blockedFloors.contains(x + "," + y);
        }
        public boolean step(int plane, int x, int y, int dx, int dy) {
            queries++;
            return !blockedSteps.contains(x + "," + y + "," + dx + "," + dy);
        }
    }
}
