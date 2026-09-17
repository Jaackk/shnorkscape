package com.rs.game.player.client;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Whether native cache bindings are enforced or merely reported.
 *
 * The native adapters pin cache files by SHA-256 so a cache swap cannot silently retarget an
 * interface component or a clientscript. Those pins were taken against the 947 cache. The 950 cache
 * recompiled every clientscript, so on 950 they fail closed and the server cannot reach a login
 * prompt at all - which hides every other problem behind one that is already understood.
 *
 * With {@code -Dataraxia.native.verifyCache=false} a failed pin logs once and continues. That is
 * strictly a porting aid: a stale pin means the binding behind it is unverified on this cache, so
 * anything it guards should be treated as unproven until it is re-pinned.
 *
 * Enforcement stays ON by default so a production run never silently drifts.
 */
public final class NativeCacheVerification {

    public static final String PROPERTY = "ataraxia.native.verifyCache";

    private static final Set<String> REPORTED = Collections.synchronizedSet(new LinkedHashSet<>());

    private NativeCacheVerification() {
    }

    public static boolean isEnforced() {
        return Boolean.parseBoolean(System.getProperty(PROPERTY, "true"));
    }

    /**
     * Fails on a mismatched binding, or records it and continues when enforcement is off.
     *
     * @return true when the caller may treat the binding as verified.
     */
    public static boolean requireBinding(String owner, String binding, String expected, String actual) {
        if (expected.equals(actual)) {
            return true;
        }
        String detail = owner + " cache binding changed: " + binding
                + " (expected " + expected + ", found " + actual + ")";
        if (isEnforced()) {
            throw new IllegalStateException(detail
                    + ". Re-pin it against this cache, or set -D" + PROPERTY + "=false to continue"
                    + " with the binding treated as unverified.");
        }
        if (REPORTED.add(owner + '/' + binding)) {
            System.out.println("[native-cache] UNVERIFIED " + detail);
        }
        return false;
    }

    /** Bindings seen to differ from their pin this run, for a summary at the end of a shakeout. */
    public static Set<String> unverified() {
        synchronized (REPORTED) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(REPORTED));
        }
    }
}
