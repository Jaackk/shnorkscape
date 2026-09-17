package com.rs.game.player.client;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Packets.Packet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A connection's projection of existing Region object mutations. Region remains the only
 * object/collision ledger and WorldTasksManager remains the only lifetime/respawn scheduler.
 * Cached map objects need no publication until a server mutation replaces or removes them.
 */
public final class Native950ObjectsView {
    private final Thread owner;
    private final Consumer<Packet> send;
    private final Map<Key, Change> published = new LinkedHashMap<Key, Change>();
    private int baseX, baseY, plane, size;
    private boolean initialized;

    public Native950ObjectsView(Thread owner, Consumer<Packet> send) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.send = Objects.requireNonNull(send, "send");
    }

    /** Restore the old scene's map baseline BEFORE its origin changes, then republish after. */
    public void beforeRebuild() {
        checkOwner();
        restorePublished();
        initialized = false;
    }

    public void refresh(Player player) {
        checkOwner();
        WorldTile loaded = player.getLastLoadedMapRegionTile();
        if (loaded == null) return;
        int width = Native950Packets.SCENE_SIZE;
        int originX = (loaded.getChunkX() - (width >> 4)) << 3;
        int originY = (loaded.getChunkY() - (width >> 4)) << 3;
        Map<Key, Change> changes = new LinkedHashMap<Key, Change>();
        for (int id : player.getMapRegionsIds()) {
            Region region = World.getRegions().get(id);
            if (region == null) continue;
            List<WorldObject> mutated = new ArrayList<WorldObject>(region.getRemovedOriginalObjects());
            mutated.addAll(region.getSpawnedObjects());
            for (WorldObject object : mutated) {
                if (!valid(object)) continue;
                Key key = new Key(object);
                WorldObject original = null;
                WorldObject[] originals = region.getAllObjects(key.plane, key.x & 63, key.y & 63);
                if (originals != null) for (WorldObject candidate : originals)
                    if (valid(candidate) && Region.OBJECT_SLOTS[candidate.getType()] == key.slot) {
                        original = candidate;
                        break;
                    }
                WorldObject current = region.getObjectWithSlot(key.plane, key.x & 63, key.y & 63, key.slot);
                if (current == null && original == null) continue;
                changes.put(key, new Change(current, original));
            }
        }
        publish(originX, originY, player.getPlane(), width, changes.values());
    }

    /** Deterministic projection seam: inputs are copied, and no WorldObject is mutated. */
    void publish(int nextBaseX, int nextBaseY, int nextPlane, int nextSize, Iterable<Change> changes) {
        checkOwner();
        if ((nextBaseX & 7) != 0 || (nextBaseY & 7) != 0 || nextPlane < 0 || nextPlane > 3
                || nextSize < 8 || nextSize > 1024 || (nextSize & 7) != 0)
            throw new IllegalArgumentException("Invalid native object scene");
        if (initialized && (baseX != nextBaseX || baseY != nextBaseY || plane != nextPlane || size != nextSize))
            restorePublished();
        baseX = nextBaseX; baseY = nextBaseY; plane = nextPlane; size = nextSize; initialized = true;
        Map<Key, Change> desired = new LinkedHashMap<Key, Change>();
        for (Change change : changes) {
            Key key = change.key;
            if (key.plane != plane || key.x < baseX || key.y < baseY
                    || key.x >= baseX + size || key.y >= baseY + size) continue;
            // A mutation that now equals its original map object needs no persistent override.
            if (!same(change.current, change.original)) desired.put(key, change);
        }
        for (Map.Entry<Key, Change> old : published.entrySet())
            if (!desired.containsKey(old.getKey())) write(old.getKey(), old.getValue().original, old.getValue().current);
        for (Map.Entry<Key, Change> entry : desired.entrySet()) {
            Change old = published.get(entry.getKey());
            if (old != null && same(old.current, entry.getValue().current)) continue;
            write(entry.getKey(), entry.getValue().current, entry.getValue().original);
        }
        published.clear();
        published.putAll(desired);
    }

    public int publishedObjects() { checkOwner(); return published.size(); }

    private void restorePublished() {
        for (Map.Entry<Key, Change> entry : published.entrySet())
            write(entry.getKey(), entry.getValue().original, entry.getValue().current);
        published.clear();
    }

    private void write(Key key, WorldObject desired, WorldObject fallback) {
        send.accept(Native950Packets.zonePartialFollows((key.x - baseX) >> 3, (key.y - baseY) >> 3, key.plane));
        if (desired != null) send.accept(Native950Packets.objectAdd(desired.getId(), desired.getType(),
                desired.getRotation(), key.x & 7, key.y & 7, desired.getNative950MapTransform()));
        else {
            if (fallback == null) throw new IllegalStateException("Missing object removal shape");
            send.accept(Native950Packets.objectRemove(fallback.getType(), fallback.getRotation(), key.x & 7, key.y & 7));
        }
    }

    private static boolean valid(WorldObject object) {
        return object != null && object.getId() >= 0 && object.getType() >= 0 && object.getType() < Region.OBJECT_SLOTS.length
                && object.getRotation() >= 0 && object.getRotation() <= 3 && object.getPlane() >= 0 && object.getPlane() <= 3;
    }

    private static boolean same(WorldObject left, WorldObject right) {
        return left == null ? right == null : right != null && left.getId() == right.getId()
                && left.getType() == right.getType() && left.getRotation() == right.getRotation()
                && java.util.Arrays.equals(left.getNative950MapTransform(), right.getNative950MapTransform());
    }

    private void checkOwner() {
        if (Thread.currentThread() != owner) throw new IllegalStateException("Object publication must run on the native world thread");
    }

    static final class Change {
        final Key key;
        final WorldObject current, original;
        Change(WorldObject current, WorldObject original) {
            if ((current != null && !valid(current)) || (original != null && !valid(original))
                    || (current == null && original == null)) throw new IllegalArgumentException("Invalid object change");
            this.key = new Key(current == null ? original : current);
            if (original != null && !key.equals(new Key(original))) throw new IllegalArgumentException("Object change crosses slots");
            this.current = current == null ? null : new WorldObject(current);
            this.original = original == null ? null : new WorldObject(original);
        }
    }

    private static final class Key {
        final int x, y, plane, slot;
        Key(WorldObject object) { x = object.getX(); y = object.getY(); plane = object.getPlane(); slot = Region.OBJECT_SLOTS[object.getType()]; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return x == key.x && y == key.y && plane == key.plane && slot == key.slot;
        }
        @Override public int hashCode() { return (((x * 31) + y) * 31 + plane) * 31 + slot; }
    }
}
