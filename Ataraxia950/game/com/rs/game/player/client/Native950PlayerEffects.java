package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Colour;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950PlayerMasks;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Converts the four legacy player graphics fields into 950's single spotanim list.
 * Definition identities come from an exact comparison of both caches, then are checked
 * against the running cache before use. That proves config identity, not identical rendered
 * models or animation assets. See protocol-analysis/player-effects-950.md.
 *
 * <p>Nonzero legacy colour overlays still lack a proven HSL conversion. Transparent overlays
 * can safely clear the tint without interpreting their unused colour bytes.
 */
public final class Native950PlayerEffects {
    public static final String RESOURCE = "/native950/player-effect-identities-950.properties";
    private static final Map<Integer, String> VERIFIED = loadIdentities();

    /** Returns the raw index21 definition, or null if the cache cannot supply it. */
    public interface DefinitionSource { byte[] definition(int id); }

    private static final DefinitionSource RUNNING_CACHE = new DefinitionSource() {
        @Override public byte[] definition(int id) {
            Store store = Cache.STORE;
            if (store == null) return null;
            Index[] indexes = store.getIndexes();
            if (indexes.length <= 21 || indexes[21] == null) return null;
            return indexes[21].getFile(id >>> 8, id & 255);
        }
    };

    private Native950PlayerEffects() { }

    /** Appends effects only; animation, appearance and other existing builder blocks survive. */
    public static Result append(Player player, Native950PlayerMasks.Builder builder) {
        return append(player, builder, RUNNING_CACHE);
    }

    /** Same entity snapshot with an explicit definition source for deterministic tests. */
    public static Result append(Player player, Native950PlayerMasks.Builder builder, DefinitionSource definitions) {
        Objects.requireNonNull(builder, "builder");
        if (player == null) return new Result(0, false, 0);
        return append(new Graphics[] {player.getNextGraphics1(), player.getNextGraphics2(),
                player.getNextGraphics3(), player.getNextGraphics4()}, player.getNextColour(), builder, definitions);
    }

    /**
     * Pure source adapter, also used by cache-free tests. Array positions are the legacy slots;
     * a null means no change, whereas a graphics ID of -1 clears exactly that slot. Neither
     * null nor a clear is turned into a definition-ID removal, which has different semantics.
     */
    public static Result append(Graphics[] graphics, Colour colour,
            Native950PlayerMasks.Builder builder, DefinitionSource definitions) {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(builder, "builder");
        Objects.requireNonNull(definitions, "definitions");
        if (graphics.length != 4) throw new IllegalArgumentException("Exactly four legacy graphics slots are required");
        List<Native950PlayerMasks.Spotanim> additions = new ArrayList<Native950PlayerMasks.Spotanim>(4);
        int refused = 0;
        for (int slot = 0; slot < graphics.length; slot++) {
            Graphics graphic = graphics[slot];
            if (graphic == null) continue;
            if (graphic.getId() == -1) {
                // The native null-ID branch clears the slot before using the other fields.
                additions.add(Native950PlayerMasks.Spotanim.of(slot, -1, 0, 0, 0, 0, 0));
                continue;
            }
            // The legacy rotation is explicitly modulo 8. Remaining bits encode customValue
            // and forceRefresh; their correspondence to 950's three flags is not established.
            int options = graphic.getSettings2Hash();
            if ((options & ~7) != 0 || !isVerifiedGraphic(graphic.getId(), definitions)) {
                refused++;
                continue;
            }
            try {
                // Both wire formats carry height in high16. 950 converts it to render units
                // as (packed >> 14) & ~3, i.e. height*4; do not multiply here as well.
                // 950 reserves delay bit15, so the typed writer rejects delay >=32768.
                additions.add(Native950PlayerMasks.Spotanim.of(slot, graphic.getId(), graphic.getSpeed(),
                        graphic.getHeight(), options & 7, 0, 0));
            } catch (IllegalArgumentException outOfRange) {
                refused++;
            }
        }
        if (!additions.isEmpty())
            builder.spotanims(Native950PlayerMasks.SpotanimList.of(new int[0], additions));

        boolean clearedColour = false;
        if (colour != null) {
            int strength = colour.getColours() >>> 24;
            if (strength != 0) {
                // Actual content uses (70,110,50,130). Neither dividing raw bytes nor masking
                // them to HSL ranges proves the old intended colour; retain an explicit fence.
                refused++;
            } else {
                try {
                    builder.colourOverlay(Native950PlayerMasks.ColourOverlay.of(0, 0, 0, 0,
                            colour.getDelay(), colour.getDuration()));
                    clearedColour = true;
                } catch (IllegalArgumentException outOfRange) {
                    refused++;
                }
            }
        }
        return new Result(additions.size(), clearedColour, refused);
    }

    /**
     * Clear all four legacy slots, replacing any effects still queued for this tick. Entity's
     * setter deduplicates equal Graphics, so distinct unused heights keep the four -1 records
     * separate; append canonicalizes those fields before writing each native slot clear.
     */
    public static void queueClear(Player player) {
        Objects.requireNonNull(player, "player");
        for (int slot = 0; slot < 4; slot++) player.setNextGraphics(null);
        for (int slot = 0; slot < 4; slot++) player.setNextGraphics(new Graphics(-1, 0, slot));
    }

    /** The same identity gate is suitable for the NPC graphics bridge. Null ID is a clear. */
    public static boolean isVerifiedGraphic(int id) { return isVerifiedGraphic(id, RUNNING_CACHE); }

    public static boolean isVerifiedGraphic(int id, DefinitionSource definitions) {
        if (id == -1) return true;
        String expected = VERIFIED.get(id);
        if (expected == null || id > 65534) return false;
        try {
            byte[] actual = definitions.definition(id);
            return actual != null && expected.equals(sha256(actual));
        } catch (RuntimeException unavailableCacheDefinition) {
            // A missing/undecodable effect must not disconnect every viewer of the entity.
            return false;
        }
    }

    public static int verifiedDefinitionCount() { return VERIFIED.size(); }

    /** Counts accepted graphics records and refused source records, not packet bytes. */
    public static final class Result {
        private final int graphicsCount, refusedCount;
        private final boolean clearedColour;
        private Result(int graphicsCount, boolean clearedColour, int refusedCount) {
            this.graphicsCount = graphicsCount; this.clearedColour = clearedColour; this.refusedCount = refusedCount;
        }
        public boolean hasMasks() { return graphicsCount != 0 || clearedColour; }
        public int graphicsCount() { return graphicsCount; }
        public boolean clearedColour() { return clearedColour; }
        public int refusedCount() { return refusedCount; }
    }

    private static Map<Integer, String> loadIdentities() {
        Properties properties = new Properties();
        try (InputStream stream = Native950PlayerEffects.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing 950 player effect identities: " + RESOURCE);
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read 950 player effect identities", exception);
        }
        if (!"1".equals(properties.getProperty("format")) || !"910".equals(properties.getProperty("legacyRevision"))
                || !"950".equals(properties.getProperty("revision")) || !"21".equals(properties.getProperty("index")))
            throw new IllegalStateException("Unexpected player effect identity table format or revision");
        Map<Integer, String> result = new HashMap<Integer, String>();
        for (String key : properties.stringPropertyNames()) if (key.startsWith("id.")) {
            int id = Integer.parseInt(key.substring(3));
            String hash = properties.getProperty(key);
            if (id < 0 || id > 65534 || !hash.matches("[0-9a-f]{64}"))
                throw new IllegalStateException("Invalid player effect identity: " + key);
            result.put(id, hash);
        }
        if (result.isEmpty()) throw new IllegalStateException("Empty player effect identity table");
        return Collections.unmodifiableMap(result);
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            char[] chars = new char[digest.length * 2];
            char[] hex = "0123456789abcdef".toCharArray();
            for (int i = 0; i < digest.length; i++) {
                chars[i * 2] = hex[(digest[i] >>> 4) & 15];
                chars[i * 2 + 1] = hex[digest[i] & 15];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError("SHA-256 required by Java", impossible);
        }
    }
}
