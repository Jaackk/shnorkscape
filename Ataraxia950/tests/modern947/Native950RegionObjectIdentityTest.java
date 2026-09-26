package com.rs.game;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorldObject inherits WorldTile's coordinate-only equals/hashCode, so two distinct object
 * layers sharing a tile (e.g. a wall and a floor decoration) are "equal" to each other. Region
 * must remove the exact resolved instance from spawnedObjects/removedOriginalObjects, never
 * whichever coordinate-equal entry happens to appear first in the ledger.
 */
public final class Native950RegionObjectIdentityTest {
    @Test public void respawningOneRemovedLayerNeverErasesAnotherRemovedLayerOnTheSameTile() {
        Region region = new Region(11084);
        int plane = 0, x = 10, y = 20;
        WorldObject wall = new WorldObject(1, 0, 0, x, y, plane);       // slot 0
        WorldObject floorDeco = new WorldObject(2, 22, 0, x, y, plane); // slot 3; same tile, different layer
        region.spawnObject(wall, plane, x, y, true);
        region.spawnObject(floorDeco, plane, x, y, true);
        // Inserted in this order so an equals-based remove would wrongly match "wall" first.
        region.removeObject(wall, plane, x, y);
        region.removeObject(floorDeco, plane, x, y);
        assertSame(wall, region.getRemovedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[0]));
        assertSame(floorDeco, region.getRemovedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[22]));

        // Respawning the floor decoration must consume only ITS OWN removed record...
        region.spawnObject(new WorldObject(2, 22, 0, x, y, plane), plane, x, y, false);
        assertNull(region.getRemovedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[22]));
        // ...and must never erase the wall's unrelated removed record sharing this tile.
        assertSame(wall, region.getRemovedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[0]));
    }

    @Test public void removingOneSpawnedLayerNeverErasesAnotherSpawnedLayerOnTheSameTile() {
        Region region = new Region(11085);
        int plane = 0, x = 15, y = 30;
        WorldObject wallSpawn = new WorldObject(3, 0, 0, x, y, plane);
        WorldObject decoSpawn = new WorldObject(4, 22, 0, x, y, plane);
        // Inserted in this order so an equals-based remove would wrongly match "wallSpawn" first.
        region.spawnObject(wallSpawn, plane, x, y, false);
        region.spawnObject(decoSpawn, plane, x, y, false);
        assertSame(wallSpawn, region.getSpawnedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[0]));
        assertSame(decoSpawn, region.getSpawnedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[22]));

        // Removing the floor decoration's spawn must consume only ITS OWN spawned record...
        region.removeObject(decoSpawn, plane, x, y);
        assertNull(region.getSpawnedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[22]));
        // ...and must never erase the wall's unrelated spawned record sharing this tile.
        assertSame(wallSpawn, region.getSpawnedObjectWithSlot(plane, x, y, Region.OBJECT_SLOTS[0]));
    }
}
