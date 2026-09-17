package com.rs.game.player.client;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.floor.FloorItem;
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
 * One connection's ground-pile projection of the existing Region/FloorItem storage.
 * No alternate item ledger or timer exists here. The world thread publishes the final state once
 * per tick, after movement and ground-item mutations, so old facade broadcasts cannot duplicate
 * items. One visible entry per (plane,tile,ID) makes native remove/count semantics unambiguous.
 */
public final class Native950GroundItemsView {
    private final Thread owner;
    private final Consumer<Packet> send;
    private final Map<Key, Integer> published = new LinkedHashMap<Key, Integer>();
    private int baseX, baseY, plane, sceneSize;
    private String viewer;
    private boolean initialized;

    public Native950GroundItemsView(Thread owner, Consumer<Packet> send) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.send = Objects.requireNonNull(send, "send");
    }

    /**
     * Must run BEFORE REBUILD_NORMAL is queued. The client retains overlapping ground piles
     * when scene bounds change; merely forgetting our projection and adding again duplicates
     * them. Removal uses the previous scene origin, even after the Player has already moved.
     */
    public void beforeRebuild() {
        checkOwner();
        removePublished();
        initialized = false;
    }

    /** Polls only already loaded regions. Reading does not create regions or trigger cache IO. */
    public void refresh(Player player) {
        checkOwner();
        WorldTile loaded = player.getLastLoadedMapRegionTile();
        if (loaded == null) return;
        int width = Native950Packets.SCENE_SIZE;
        // Native REBUILD_NORMAL 0x1400f6fcb..0x1400f6fe2; destination +0x698/+0x69c.
        int originX = (loaded.getChunkX() - (width >> 4)) << 3;
        int originY = (loaded.getChunkY() - (width >> 4)) << 3;
        List<FloorItem> items = new ArrayList<FloorItem>();
        for (int regionId : player.getMapRegionsIds()) {
            Region region = World.getRegions().get(regionId);
            if (region == null || region.getGroundItems() == null) continue;
            items.addAll(region.getGroundItems());
        }
        publish(player.getUsername(), originX, originY, player.getPlane(), width, items);
    }

    /** Pure projection entry point used by tests; FloorItems are never mutated. */
    void publish(String nextViewer, int nextBaseX, int nextBaseY, int nextPlane,
                 int nextSize, Iterable<FloorItem> items) {
        checkOwner();
        Objects.requireNonNull(nextViewer, "viewer");
        Objects.requireNonNull(items, "items");
        if ((nextBaseX & 7) != 0 || (nextBaseY & 7) != 0 || nextSize < 8 || nextSize > 1024
                || (nextSize & 7) != 0 || nextPlane < 0 || nextPlane > 3)
            throw new IllegalArgumentException("Invalid native ground-item scene");
        if (initialized && (baseX != nextBaseX || baseY != nextBaseY || plane != nextPlane
                || sceneSize != nextSize || !viewer.equals(nextViewer))) removePublished();
        baseX = nextBaseX; baseY = nextBaseY; plane = nextPlane; sceneSize = nextSize;
        viewer = nextViewer; initialized = true;

        Map<Key, Integer> desired = new LinkedHashMap<Key, Integer>();
        for (FloorItem item : items) {
            if (item == null || !item.isNative950() || item.getTile() == null || item.getAmount() <= 0
                    || item.getId() < 0 || item.getId() >= 0xffffff || !visibleTo(item, viewer)) continue;
            WorldTile tile = item.getTile();
            if (tile.getPlane() != plane || tile.getX() < baseX || tile.getY() < baseY
                    || tile.getX() >= baseX + sceneSize || tile.getY() >= baseY + sceneSize) continue;
            Key key = new Key(item.getId(), tile.getX(), tile.getY(), tile.getPlane());
            Integer prior = desired.get(key);
            // Long addition avoids overflow, but the authoritative FloorItem amount is untouched.
            int count = (int) Math.min(65535L, (prior == null ? 0L : prior.longValue()) + item.getAmount());
            desired.put(key, count);
        }
        for (Map.Entry<Key, Integer> old : published.entrySet()) {
            if (!desired.containsKey(old.getKey())) {
                zone(old.getKey());
                send.accept(Native950Packets.groundItemRemove(old.getKey().id, old.getKey().x & 7, old.getKey().y & 7));
            }
        }
        for (Map.Entry<Key, Integer> item : desired.entrySet()) {
            Key key = item.getKey();
            Integer old = published.get(key);
            if (old != null && old.equals(item.getValue())) continue;
            zone(key);
            if (old == null) send.accept(Native950Packets.groundItemAdd(key.id, item.getValue(), key.x & 7, key.y & 7));
            else send.accept(Native950Packets.groundItemCount(key.id, old, item.getValue(), key.x & 7, key.y & 7));
        }
        published.clear();
        published.putAll(desired);
    }

    /** Same private-owner rule used for publication and authoritative native pickup. */
    public static boolean visibleTo(FloorItem item, String username) {
        if (item == null) return false;
        if (item.hasOwner() && username != null && username.equalsIgnoreCase(item.getOwner())) return true;
        return !item.isInvisible() && (!item.isNative950() || item.isPublicTransferAllowed());
    }

    public static boolean visibleTo(Player player, FloorItem item) {
        return player != null && visibleTo(item, player.getUsername());
    }

    /**
     * A click may target only a pile this exact viewer was sent in the current scene. The
     * pickup transaction must still re-read Region storage, validate owner/account/controller
     * rules and capacity, and commit on the world thread; this projection cannot grant loot.
     */
    public boolean canTake(Player player, int itemId, int x, int y) {
        checkOwner();
        if (!initialized || player == null || !viewer.equals(player.getUsername())
                || plane != player.getPlane()) return false;
        WorldTile loaded = player.getLastLoadedMapRegionTile();
        if (loaded == null) return false;
        int width = Native950Packets.SCENE_SIZE;
        if (width != sceneSize || baseX != ((loaded.getChunkX() - (width >> 4)) << 3)
                || baseY != ((loaded.getChunkY() - (width >> 4)) << 3)) return false;
        return published.containsKey(new Key(itemId, x, y, plane));
    }

    public int publishedPiles() { checkOwner(); return published.size(); }

    private void removePublished() {
        for (Key key : published.keySet()) {
            zone(key);
            send.accept(Native950Packets.groundItemRemove(key.id, key.x & 7, key.y & 7));
        }
        published.clear();
    }

    private void zone(Key key) {
        send.accept(Native950Packets.zonePartialFollows((key.x - baseX) >> 3, (key.y - baseY) >> 3, key.plane));
    }

    private void checkOwner() {
        if (Thread.currentThread() != owner)
            throw new IllegalStateException("Ground-item publication must run on the native world thread");
    }

    private static final class Key {
        final int id, x, y, plane;
        Key(int id, int x, int y, int plane) { this.id = id; this.x = x; this.y = y; this.plane = plane; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return id == key.id && x == key.x && y == key.y && plane == key.plane;
        }
        @Override public int hashCode() { return (((id * 31) + x) * 31 + y) * 31 + plane; }
    }
}
