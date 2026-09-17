package com.rs.game.player.client.ui;

import com.rs.cache.Cache;
import java.nio.file.Paths;

/**
 * Loads and validates the 950 UI binding table against the real read-only flat
 * cache, then exercises the identity resolver on the ids the backlog names.
 * Exits non-zero when the table is rejected so the check can gate a build.
 *
 * Usage: Native950BindingsProbe &lt;flat-cache-directory&gt;
 */
public final class Native950BindingsProbe {
    /** 910 ids with no group in index 3 of the 950 cache. */
    private static final int[] ABSENT = { 1578, 1607, 1628, 1680, 1929 };
    /**
     * 910 ids the 950 cache does hold, under other content. 1530 is the 910
     * Invention interface and 3/1530 exists on 950 as a neighbourhood-invite
     * popup, so it must be rejected without being called absent.
     */
    private static final int[] REASSIGNED = { 1530 };
    /**
     * A varp id with no file in 2/60 of the 950 cache. 947 stopped at 12797, so
     * the old probe used 12798; the 950 archive runs to 13542 and 12798 is a real
     * varp there, which would have made this assertion pass for the wrong reason.
     * 90 is a hole inside the 950 archive's own id range.
     */
    private static final int ABSENT_VARP = 90;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950BindingsProbe <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]).toAbsolutePath().normalize());
        Native950Bindings bindings;
        try {
            bindings = Native950Bindings.tryLoad();
        } catch (IllegalStateException rejected) {
            System.out.println(rejected.getMessage());
            System.out.println("RESULT: FAIL (table rejected)");
            System.exit(1);
            return;
        }
        if (bindings == null) { System.out.println("RESULT: FAIL (cache not flat)"); System.exit(1); return; }
        for (String slot : bindings.slotNames()) {
            Native950Bindings.Slot s = bindings.slot(slot);
            System.out.println("slot " + slot + " key " + s.enumKey + " struct " + s.structId + " attach " + Native950Bindings.hashText(s.attach)
                    + " wrapper " + Native950Bindings.hashText(s.wrapper));
        }
        for (String name : bindings.interfaceNames()) {
            Native950Bindings.Interface i = bindings.iface(name);
            System.out.println("interface " + name + " = " + i.id + " components " + i.components + (i.sha256 == null ? " (unpinned)" : " pinned"));
        }
        for (String name : bindings.varNames()) {
            Native950Bindings.Var v = bindings.var(name);
            System.out.println("var " + name + " " + v.kind + " " + v.id + " max " + v.maxValue);
        }
        for (String name : bindings.opNames()) {
            Native950Bindings.Op op = bindings.ops(name);
            System.out.println("op " + name + " steps " + op.steps.size() + " packets " + op.packetsRequired + (op.available ? "" : " UNAVAILABLE " + op.missingPackets));
        }
        Native950Bindings.Resolver resolver = bindings.resolver();
        Native950Bindings.Resolver allow = bindings.allowListResolver();
        boolean ok = true;
        for (int id : ABSENT) ok &= resolver.interfaceId(id) == -1;
        // Reassigned ids must be rejected by BOTH policies. The permissive one is the
        // interesting case: the group is there, so only the declaration stops it.
        for (int id : REASSIGNED) ok &= resolver.interfaceId(id) == -1 && allow.interfaceId(id) == -1;
        ok &= resolver.interfaceId(1477) == 1477 && resolver.componentId(1473, 5) == 5 && resolver.componentId(1473, 23) == -1
                && resolver.varp(8971) == 8971 && resolver.varbit(45189) == 45189 && resolver.script(1362) == 1362 && resolver.container(94) == 94
                && resolver.varp(ABSENT_VARP) == -1;
        // hitpoints is a varp on 950 (13537), not varbit 1668; 1668 still exists in the
        // cache but is undeclared now, so the allow list must turn it down.
        ok &= allow.varp(13537) == 13537 && allow.varbit(1668) == -1;
        System.out.println(resolver.report());
        System.out.println(allow.report());
        System.out.println("scripts pinned: " + bindings.scriptNames().size() + ", varp capacity from cache: " + com.rs.game.player.VarsManager.capacity());
        System.out.println(ok ? "RESULT: OK" : "RESULT: FAIL (resolver expectations)");
        if (!ok) System.exit(1);
    }

    private Native950BindingsProbe() { }
}
