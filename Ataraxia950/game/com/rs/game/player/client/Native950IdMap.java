package com.rs.game.player.client;

import java.util.Objects;

/**
 * Identifier hook between the 910 ids that content code passes to
 * {@code getPackets()} and the ids the 947 cache actually uses.
 *
 * <p>The defaults are identity and they are provisional. Most 910 interface ids
 * still exist in the 947 cache (161 of 167 checked), but component layouts drift:
 * the backpack item grid is 1473:7 in 910 and 1473:5 in 947, and cs2 script ids,
 * varbits and container keys were only pinned for the bank/backpack/equipment
 * slice. Identity therefore produces correct bytes for a verified id and merely
 * counted (never silently corrupted) output otherwise, because the writers still
 * range-check everything they receive.
 *
 * <p>P2 replaces the identity resolver with its binding table through
 * {@link #install(Resolver)}; the facade never caches resolved ids, so the
 * swap takes effect on the next packet.
 */
public final class Native950IdMap {

    /** One resolution strategy for every id family the facade rebinds. */
    public interface Resolver {
        int interfaceId(int interfaceId);
        /** Receives the 910 interface id so a table can key on the pair. */
        int componentId(int interfaceId, int componentId);
        int varp(int id);
        int varbit(int id);
        int varc(int id);
        int script(int id);
        int container(int id);
    }

    /**
     * Passes every id through unchanged, EXCEPT client variables.
     *
     * <p>M3 promoted the client-variable families (CLIENT_SETVARC_SMALL 1 /
     * _LARGE 112, CLIENT_SETVARCSTR_SMALL 67 / _LARGE 15) from the counted NO-OP
     * tier to REAL writers, so from M3 on a varc id that survives this resolver
     * reaches the 947 wire. There is no verified 910-to-947 correspondence for
     * ANY varc id - the validated binding table declares none - so identity is
     * never a defensible answer for that kind, and this resolver is the default
     * whenever no table has been installed (a build tree without the JSON
     * resource, a probe, a smoke that attaches no content). Returning -1 makes
     * the fail-closed property structural instead of resolver-dependent: with or
     * without a table, an undeclared varc is a counted drop in
     * {@link Native950PacketDispatcher}, never bytes.
     *
     * <p>Interface, component, varp, varbit, script and container ids keep
     * identity: those families were already REAL before M3, most 910 ids exist
     * unchanged in the 947 cache, and {@code Native950Bindings}' own permissive
     * resolver re-checks them against the cache when a table is installed.
     */
    public static final Resolver IDENTITY = new Resolver() {
        @Override public int interfaceId(int interfaceId) { return interfaceId; }
        @Override public int componentId(int interfaceId, int componentId) { return componentId; }
        @Override public int varp(int id) { return id; }
        @Override public int varbit(int id) { return id; }
        @Override public int varc(int id) { return -1; }
        @Override public int script(int id) { return id; }
        @Override public int container(int id) { return id; }
    };

    private static volatile Resolver resolver = IDENTITY;

    /** Installs a binding table; the world thread and tests may swap it at any time. */
    public static void install(Resolver next) {
        resolver = Objects.requireNonNull(next, "resolver");
    }

    /** Restores identity resolution (tests). */
    public static void reset() {
        resolver = IDENTITY;
    }

    public static Resolver current() {
        return resolver;
    }

    public static int interfaceId(int interfaceId) { return resolver.interfaceId(interfaceId); }
    public static int componentId(int interfaceId, int componentId) { return resolver.componentId(interfaceId, componentId); }
    public static int varp(int id) { return resolver.varp(id); }
    public static int varbit(int id) { return resolver.varbit(id); }
    public static int varc(int id) { return resolver.varc(id); }
    public static int script(int id) { return resolver.script(id); }
    public static int container(int id) { return resolver.container(id); }

    /** Resolves a 910 interface/component pair into the packed 947 hash used by cs2 arguments. */
    public static int componentHash(int interfaceId, int componentId) {
        return (interfaceId(interfaceId) << 16) | (componentId(interfaceId, componentId) & 0xffff);
    }

    /** Resolves a packed 910 hash ({@code interfaceId << 16 | componentId}) into its 947 interface id. */
    public static int hashInterface(int packedHash) {
        return interfaceId(packedHash >>> 16);
    }

    /** Resolves a packed 910 hash into its 947 component id. */
    public static int hashComponent(int packedHash) {
        return componentId(packedHash >>> 16, packedHash & 0xffff);
    }

    private Native950IdMap() { }
}
