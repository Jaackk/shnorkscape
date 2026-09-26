package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.AnimationDefinitions;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Data-driven NPC animation bindings generated from all authored combat rows.
 * Only a selected sequence is checked against the running 950 cache. The
 * generated resource replaces per-NPC/per-sequence Java lists and needs no old
 * cache at runtime. Config identity alone does not prove rendered model identity.
 */
public final class Native950NpcCombatAnimations {
    public static final String RESOURCE = "/native950/npc-combat-animations-950.properties";
    private static final Map<Integer, Binding> BINDINGS = loadBindings();
    private static final VerificationCache VERIFIED = new VerificationCache();

    public interface DefinitionSource { byte[] definition(int sequenceId); }

    private Native950NpcCombatAnimations() { }

    /** Null means absent (-1), unbound, changed, unavailable, or undecodable. */
    public static Sequence resolve(int sequenceId) {
        final Store store = Cache.STORE;
        if (store == null || !Cache.isFlatReadOnly() || !hasCatalogBinding(sequenceId)) return null;
        return VERIFIED.resolve(store, sequenceId, new DefinitionSource() {
            @Override public byte[] definition(int id) {
                Index[] indexes = store.getIndexes();
                return indexes.length > 20 && indexes[20] != null
                        ? indexes[20].getFile(id >>> 7, id & 127) : null;
            }
        });
    }

    /** Deterministic adapter used by cache probes and tests; never caches a mutable source. */
    public static Sequence resolve(int sequenceId, DefinitionSource definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Binding binding = BINDINGS.get(sequenceId);
        if (binding == null) return null;
        try {
            byte[] raw = definitions.definition(sequenceId);
            if (raw == null || !binding.sha256.equals(sha256(raw))) return null;
            AnimationDefinitions decoded = AnimationDefinitions.decodeStrict947(sequenceId, raw, null);
            long cycles = decoded.modernInt26b;
            if (decoded.anIntArray2153 != null)
                for (int duration : decoded.anIntArray2153) cycles += duration;
            if (cycles <= 0 || cycles != binding.sequence.durationCycles) return null;
            return binding.sequence;
        } catch (RuntimeException invalidOrUnavailableDefinition) {
            return null;
        }
    }

    /** The null animation ID is a deliberate clear; other negatives are invalid. */
    public static boolean acceptedFromRunningCache(int sequenceId) {
        return sequenceId == -1 || resolve(sequenceId) != null;
    }

    /** Raw 20ms client cycles, or -1 when no verified animation is available. */
    public static int durationCycles(int sequenceId) {
        Sequence sequence = resolve(sequenceId);
        return sequence == null ? -1 : sequence.durationCycles;
    }

    /**
     * Reject the known legacy-frame-on-animaya mismatch. A matching family is only a necessary
     * condition, never proof that a sequence binds the creature's model/skeleton.
     *
     * <p>Adopted from Artaven (Stage B Phase 4). Shnorkscape's own combat resolution
     * ({@code Native950NpcCombatCatalog}) is unchanged by this method; only the Developer Console's
     * NPC preview consults it, so this can only ever withhold a preview animation, never a live one.
     */
    public static boolean compatibleWithRender(int renderId,int sequenceId) {
        if(sequenceId<0||renderId<0)return true;
        try {
            byte[] raw=Cache.STORE.getIndexes()[2].getFile(32,renderId);
            if(raw==null)return false;
            int stand=com.rs.cache.loaders.RenderAnimDefinitions.decodeStrict947(renderId,raw,null).standAnimation;
            if(stand<0)return true;
            byte[] standing=Cache.STORE.getIndexes()[20].getFile(stand>>>7,stand&127);
            byte[] combat=Cache.STORE.getIndexes()[20].getFile(sequenceId>>>7,sequenceId&127);
            if(standing==null||combat==null)return false;
            return compatibleFamilies(AnimationDefinitions.decodeStrict947(stand,standing,null),
                    AnimationDefinitions.decodeStrict947(sequenceId,combat,null));
        }catch(RuntimeException unreadable){return false;}
    }
    static boolean compatibleFamilies(AnimationDefinitions stand,AnimationDefinitions combat) {
        boolean animayaStand=(stand.anIntArray2153==null||stand.anIntArray2153.length==0)&&stand.modernInt26b>0;
        boolean legacyCombat=combat.anIntArray2153!=null&&combat.anIntArray2153.length>0;
        return !animayaStand||!legacyCombat;
    }

    public static boolean hasCatalogBinding(int sequenceId) { return BINDINGS.containsKey(sequenceId); }
    public static int verifiedDefinitionCount() { return BINDINGS.size(); }

    public static final class Sequence {
        private final int id, durationCycles;
        private Sequence(int id, int durationCycles) { this.id = id; this.durationCycles = durationCycles; }
        public int id() { return id; }
        public int durationCycles() { return durationCycles; }
    }

    /** Immutable snapshots keyed by Store identity, including verified absence. */
    static final class VerificationCache {
        private Object identity;
        private Map<Integer, Sequence> resolutions = Collections.emptyMap();
        synchronized Sequence resolve(Object storeIdentity, int id, DefinitionSource source) {
            Objects.requireNonNull(storeIdentity, "storeIdentity");
            if (identity != storeIdentity) {
                identity = storeIdentity;
                resolutions = Collections.emptyMap();
            }
            if (resolutions.containsKey(id)) return resolutions.get(id);
            Sequence sequence = Native950NpcCombatAnimations.resolve(id, source);
            Map<Integer, Sequence> copy = new HashMap<Integer, Sequence>(resolutions);
            copy.put(id, sequence);
            resolutions = Collections.unmodifiableMap(copy);
            return sequence;
        }
    }

    private static Map<Integer, Binding> loadBindings() {
        Properties properties = new Properties();
        try (InputStream stream = Native950NpcCombatAnimations.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing NPC combat animation bindings: " + RESOURCE);
            properties.load(stream);
        } catch (IOException error) { throw new ExceptionInInitializerError(error); }
        try(InputStream stream=Native950NpcCombatAnimations.class.getResourceAsStream("/native950/boss-animations-950.properties")){
            if(stream==null)throw new IllegalStateException("Missing named boss sequence bindings");
            Properties extra=new Properties();extra.load(stream);
            for(String key:extra.stringPropertyNames()){
                String prior=properties.getProperty(key),value=extra.getProperty(key);
                if(prior!=null&&!prior.split(",")[0].equals(value.split(",")[0]))throw new IllegalStateException("Conflicting boss sequence "+key);
                properties.setProperty(key,value);
            }
        }catch(IOException error){throw new ExceptionInInitializerError(error);}

        Map<Integer, Binding> bindings = new HashMap<Integer, Binding>();
        for (String key : properties.stringPropertyNames()) {
            int id = Integer.parseInt(key);
            String[] fields = properties.getProperty(key).split(",", -1);
            if (id < 0 || fields.length != 3 || !fields[0].matches("[0-9a-f]{64}")
                    || !(fields[2].equals("identical-definition") || fields[2].equals("identical-frame-duration-binding")))
                throw new IllegalStateException("Malformed NPC combat animation binding " + key);
            int duration = Integer.parseInt(fields[1]);
            if (duration <= 0) throw new IllegalStateException("Invalid NPC animation duration " + key);
            bindings.put(id, new Binding(fields[0], new Sequence(id, duration)));
        }
        if (bindings.isEmpty()) throw new IllegalStateException("Empty NPC combat animation bindings");
        return Collections.unmodifiableMap(bindings);
    }

    private static String sha256(byte[] raw) {
        try {
            StringBuilder out = new StringBuilder(64);
            for (byte value : MessageDigest.getInstance("SHA-256").digest(raw))
                out.append(String.format("%02x", value & 255));
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static final class Binding {
        final String sha256;
        final Sequence sequence;
        Binding(String sha256, Sequence sequence) { this.sha256 = sha256; this.sequence = sequence; }
    }
}
