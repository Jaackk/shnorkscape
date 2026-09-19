package com.rs.game.player.client;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.security.MessageDigest;
import com.rs.cache.Cache;

/**
 * Immutable, cache-pinned set of integer permanent variables that the 950
 * workspace scripts can safely own.  It is deliberately not a universal varc
 * registry and is not consulted by login or protocol handling yet.
 */
public final class Native950WorkspaceIntegerDescriptor {
    private static final String RESOURCE = "native950/workspace-integer-descriptor-950.properties";
    private static final Properties DATA = load();
    private static final Set<Integer> IDS = ids(DATA.getProperty("ids"));

    private Native950WorkspaceIntegerDescriptor() { }

    public static int version() { return integer("version"); }
    public static int size() { return IDS.size(); }
    public static Set<Integer> ids() { return IDS; }
    public static boolean contains(int id) { return IDS.contains(id); }
    public static String evidenceFingerprint() { return required("fingerprint.sha256"); }
    public static String property(String key) { return required(key); }

    /**
     * Explicit verification seam for build tooling and the opt-in diagnostic.
     * It is intentionally not called from login/bootstrap.
     */
    public static void verifyLoadedCacheEvidence() {
        if (Cache.STORE == null) throw new IllegalStateException("Cache must be initialized before verifying workspace descriptor evidence");
        try {
            for (String token : required("script.ids").split(",")) {
                int id = Integer.parseInt(token);
                verify("script." + id + ".sha256", Cache.STORE.getIndexes()[12].getFile(id, 0));
            }
            for (String token : required("varbit.ids").split(",")) {
                int id = Integer.parseInt(token);
                verify("varbit." + id + ".sha256", Cache.STORE.getIndexes()[2].getFile(69, id));
                if (!"2".equals(required("varbit." + id + ".domain"))) throw new IllegalStateException("Workspace varbit is not domain 2: " + id);
            }
        } catch (Exception error) {
            throw new IllegalStateException("Native-950 workspace descriptor cache evidence changed; regenerate and review it before use", error);
        }
    }

    private static Properties load() {
        try (InputStream stream = Native950WorkspaceIntegerDescriptor.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (stream == null) throw new IllegalStateException("Missing native-950 workspace integer descriptor: " + RESOURCE);
            Properties properties = new Properties();
            properties.load(stream);
            if (integer(properties, "version") != 1) throw new IllegalStateException("Unsupported workspace descriptor version");
            if (!"950".equals(properties.getProperty("revision"))) throw new IllegalStateException("Workspace descriptor is not revision 950");
            if (properties.getProperty("fingerprint.sha256", "").length() != 64) throw new IllegalStateException("Workspace descriptor fingerprint is missing");
            return properties;
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load native-950 workspace integer descriptor", error);
        }
    }

    private static Set<Integer> ids(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException("Workspace descriptor has no IDs");
        Set<Integer> ids = new LinkedHashSet<Integer>();
        for (String token : value.split(",")) {
            int id = Integer.parseInt(token.trim());
            if (id < 0 || id > 65535 || !ids.add(id)) throw new IllegalStateException("Invalid or duplicate workspace permanent variable " + id);
        }
        return Collections.unmodifiableSet(ids);
    }

    private static int integer(String key) { return integer(DATA, key); }
    private static int integer(Properties properties, String key) {
        try { return Integer.parseInt(required(properties, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException("Invalid workspace descriptor " + key, error); }
    }
    private static String required(String key) { return required(DATA, key); }
    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException("Workspace descriptor missing " + key);
        return value.trim();
    }
    private static void verify(String key, byte[] raw) throws Exception {
        if (raw == null || !required(key).equals(hex(MessageDigest.getInstance("SHA-256").digest(raw))))
            throw new IllegalStateException("Pinned evidence mismatch for " + key);
    }
    private static String hex(byte[] bytes) { StringBuilder result = new StringBuilder(); for (byte value : bytes) result.append(String.format("%02x", value & 255)); return result.toString(); }
}
