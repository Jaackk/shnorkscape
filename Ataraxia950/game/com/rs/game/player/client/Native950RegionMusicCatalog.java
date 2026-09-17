package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Region;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

/** Regional names from the 910 world, resolved against the paired 947 music catalogue. */
public final class Native950RegionMusicCatalog {
    private static final String NAMES_SHA = "1e1f1f6a22fb1c924776b3c9b5e331eaec2130777b0b91d56a212b81ed191247";
    private static final String ARCHIVES_SHA = "d61cf981396174075389c4a5f7770defc5f617226f870ccdc024d69cf6cba844";
    private static volatile Installed shared;
    private final Data injected;

    /** No cache reads or per-player catalogue copies; data is shared lazily. */
    public Native950RegionMusicCatalog() { injected = null; }

    @FunctionalInterface interface FileReader { byte[] read(int index, int group, int file); }

    Native950RegionMusicCatalog(FileReader reader, IntPredicate archiveAvailable) {
        injected = load(reader, archiveAvailable);
    }

    public static final class Track {
        public final String name;
        public final int trackId, archiveId;

        public Track(String name, int trackId, int archiveId) {
            this.name = Objects.requireNonNull(name, "name");
            if (name.isEmpty() || trackId < 0 || archiveId < 0)
                throw new IllegalArgumentException("Music tracks require a name and nonnegative IDs");
            this.trackId = trackId;
            this.archiveId = archiveId;
        }
    }

    /** Unknown regions, unresolved names and unavailable resources have no regional selection. */
    public Track lookup(int regionId) {
        if (regionId < 0 || regionId > 65535) return null;
        Data data = injected != null ? injected : installed();
        if (data == null) return null;
        return data.regions.computeIfAbsent(regionId,
                id -> Optional.ofNullable(data.resolve(Region.getMusicNames(id)))).orElse(null);
    }

    /** Strict startup check; ordinary cache-free session construction remains safe. */
    public static void verify() {
        if (installed() == null) throw new IllegalStateException("Regional music requires the paired flat cache");
    }

    private static Data installed() {
        Store store = Cache.STORE;
        if (store == null || !store.isReadOnly()) return null;
        Installed result = shared;
        if (result != null && result.store == store) return result.data;
        synchronized (Native950RegionMusicCatalog.class) {
            if (shared != null && shared.store == store) return shared.data;
            Data loaded = load((i, g, f) -> {
                Index index = index(store, i);
                return index == null ? null : index.getFile(g, f);
            }, archive -> {
                Index index = index(store, 40);
                if (index == null || !index.archiveExists(archive)) return false;
                // Read the compressed root resource once, without retaining audio in an Index file cache.
                byte[] bytes = index.getMainFile().getArchiveData(archive);
                return bytes != null && bytes.length > 5;
            });
            shared = new Installed(store, loaded);
            return loaded;
        }
    }

    private static final class Installed {
        final Store store;
        final Data data;
        Installed(Store store, Data data) { this.store = store; this.data = data; }
    }

    private static Index index(Store store, int id) {
        Index[] indexes = store.getIndexes();
        return indexes != null && id < indexes.length ? indexes[id] : null;
    }

    private static Data load(FileReader reader, IntPredicate archiveAvailable) {
        Objects.requireNonNull(reader, "reader");
        byte[] names = pinned(reader, 1345, NAMES_SHA);
        byte[] archives = pinned(reader, 1351, ARCHIVES_SHA);
        return new Data(enumValues(names, true), enumValues(archives, false),
                Objects.requireNonNull(archiveAvailable, "archiveAvailable"));
    }

    private static byte[] pinned(FileReader reader, int id, String expected) {
        String path = "17/" + (id >>> 8) + "/" + (id & 255);
        byte[] bytes = reader.read(17, id >>> 8, id & 255);
        if (bytes == null) throw new IllegalStateException("Missing regional music cache file " + path);
        try {
            StringBuilder actual = new StringBuilder();
            for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes))
                actual.append(String.format("%02x", value & 255));
            // Through the shared gate, so this pin obeys the same -Dataraxia.native.verifyCache
            // switch as the other six. It was the last one that did not, and it was found the
            // expensive way - by a login failing on it after every other verifier had been fixed.
            // The sweep that was supposed to find them all grepped for the MESSAGE text of the
            // pins already known ("revalidate its binding", "cache binding changed"), and this one
            // says "Regional music cache file changed". Searching for the STRUCTURE instead -
            // every pin computes a SHA-256, so MessageDigest is the real signature - finds all
            // seven at once.
            NativeCacheVerification.requireBinding("Regional music", path, expected, actual.toString());
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        return bytes;
    }

    /** Only the formats and types in the pinned logical files are admitted. */
    private static Map<Integer, Object> enumValues(byte[] bytes, boolean strings) {
        ByteBuffer in = ByteBuffer.wrap(bytes);
        Map<Integer, Object> values = new HashMap<Integer, Object>();
        while (true) {
            int tag = in.get() & 255;
            if (tag == 0) {
                if (in.hasRemaining()) throw new IllegalStateException("Trailing music enum bytes");
                return values;
            }
            if (tag == 101 || tag == 102) {
                int type = in.get() & 255;
                int expected = tag == 101 ? 0 : strings ? 36 : 11;
                if (type != expected) throw new IllegalStateException("Unexpected music enum type");
            } else if (strings && tag == 3) {
                string(in); // Enum default metadata (" "), never a regional name entry.
            } else if (!strings && tag == 4) {
                in.getInt(); // Enum default resource 2167; absent keys must still resolve to null.
            } else if (tag == (strings ? 7 : 8)) {
                in.getShort(); // Allocated range, distinct from the actual entry count.
                int count = in.getShort() & 65535;
                for (int i = 0; i < count; i++) {
                    int key = in.getShort() & 65535;
                    Object value = strings ? string(in) : Integer.valueOf(in.getInt());
                    if (values.put(key, value) != null) throw new IllegalStateException("Duplicate music enum key");
                }
            } else throw new IllegalStateException("Unexpected music enum tag " + tag);
        }
    }

    private static String string(ByteBuffer in) {
        int start = in.position();
        while (in.get() != 0) { }
        return new String(in.array(), start, in.position() - start - 1, Charset.forName("windows-1252"));
    }

    private static final class Data {
        final Map<String, List<Integer>> idsByName = new HashMap<String, List<Integer>>();
        final Map<Integer, Object> archives;
        final IntPredicate available;
        final Map<Integer, Boolean> availability = new ConcurrentHashMap<Integer, Boolean>();
        final Map<Integer, Optional<Track>> regions = new ConcurrentHashMap<Integer, Optional<Track>>();

        Data(Map<Integer, Object> names, Map<Integer, Object> archives, IntPredicate available) {
            this.archives = archives;
            this.available = available;
            for (Map.Entry<Integer, Object> entry : names.entrySet())
                idsByName.computeIfAbsent((String) entry.getValue(), ignored -> new ArrayList<Integer>()).add(entry.getKey());
            for (List<Integer> ids : idsByName.values()) Collections.sort(ids);
        }

        Track resolve(String[] regionalNames) {
            if (regionalNames.length == 0) return null;
            // Preserve the first regional choice; never fall back to the unlocked-music shuffle.
            String name = regionalNames[0].trim();
            if (name.equals("Barbarianims")) name = "Barbarianism";
            else if (name.equals("Sea Shanty2")) name = "Sea Shanty II";
            else if (name.equals("Wandar")) name = "Wander";
            List<Integer> ids = idsByName.get(name);
            if (ids == null || ids.isEmpty()) return null;
            int trackId = ids.get(0);
            Object value = archives.get(trackId);
            if (!(value instanceof Integer) || (Integer) value < 0) return null;
            int archiveId = (Integer) value;
            if (ids.size() > 1 && !name.equals("Far Away")) {
                // Distant Land has two logical IDs, but both select the same music resource.
                for (int id : ids) if (!Integer.valueOf(archiveId).equals(archives.get(id))) return null;
            }
            // Far Away292 is the old Region lookup's first ID;582 is a different resource.
            if (name.equals("Far Away") && trackId != 292) return null;
            if (!availability.computeIfAbsent(archiveId, available::test)) return null;
            return new Track(name, trackId, archiveId);
        }
    }
}
