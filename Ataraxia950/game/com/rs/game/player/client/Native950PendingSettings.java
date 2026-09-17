package com.rs.game.player.client;

import com.rs.cache.Cache;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Paired-cache positive allowlist for rejecting unsupported combat-mode checkboxes. */
public final class Native950PendingSettings {
    private static volatile boolean verified;
    private static final String EXPECTED = "155b7ee0168d46b66af0e2460a24b0c76513d25532536e8b3354b774c8fae130";
    // Re-derived from 950 page membership, defaults and checkbox structs on 2026-09-10.
    // Reproduce with tools/verify_950_pending_settings.py; see protocol-analysis/pending-settings-950.json.
    // Pins cover positive checkbox structs, defaults, scripts and page membership.
    private static final int[][] BINDINGS = {
        {2, 11, 7513},
        {2, 11, 7537},
        {2, 11, 9208},
        {2, 41, 411},
        {2, 41, 1296},
        {2, 41, 1297},
        {2, 41, 1298},
        {2, 41, 1300},
        {2, 41, 1303},
        {2, 41, 1305},
        {2, 41, 1306},
        {2, 41, 1307},
        {2, 41, 1308},
        {2, 41, 1309},
        {2, 41, 1311},
        {2, 41, 1312},
        {2, 41, 1313},
        {2, 41, 1315},
        {2, 41, 1316},
        {2, 41, 1317},
        {2, 41, 1318},
        {2, 41, 1319},
        {2, 41, 1320},
        {2, 41, 1322},
        {2, 41, 1514},
        {2, 41, 1515},
        {2, 41, 2089},
        {2, 41, 3508},
        {2, 41, 3510},
        {2, 41, 3513},
        {2, 41, 3514},
        {2, 41, 3515},
        {2, 41, 3517},
        {2, 41, 3518},
        {2, 41, 3519},
        {2, 41, 3520},
        {2, 41, 3521},
        {2, 41, 3522},
        {2, 41, 3523},
        {2, 41, 3524},
        {2, 41, 3525},
        {2, 41, 3526},
        {2, 41, 3528},
        {2, 41, 3640},
        {2, 41, 3641},
        {2, 41, 3642},
        {2, 41, 3643},
        {2, 41, 3685},
        {2, 41, 3686},
        {2, 41, 3930},
        {2, 41, 4169},
        {2, 41, 4354},
        {2, 41, 4355},
        {2, 41, 4675},
        {2, 41, 4676},
        {2, 41, 4677},
        {2, 41, 5634},
        {2, 41, 5635},
        {2, 41, 5989},
        {2, 41, 6886},
        {2, 41, 7575},
        {2, 41, 12942},
        {2, 41, 12943},
        {2, 41, 14488},
        {2, 41, 14489},
        {2, 41, 14528},
        {2, 41, 14711},
        {2, 41, 17066},
        {2, 41, 17067},
        {2, 41, 17130},
        {2, 41, 17255},
        {2, 41, 17256},
        {2, 41, 17257},
        {2, 41, 17515},
        {12, 2830, 0},
        {12, 2929, 0},
        {12, 2957, 0},
        {12, 2970, 0},
        {12, 2991, 0},
        {12, 2992, 0},
        {12, 5590, 0},
        {12, 7958, 0},
        {12, 7961, 0},
        {12, 18933, 0},
        {12, 20382, 0},
        {12, 20385, 0},
        {12, 20386, 0},
        {17, 36, 150},
        {17, 56, 232},
        {17, 56, 233},
        {22, 199, 3},
        {22, 1299, 30},
        {22, 1299, 31}
    };

    private Native950PendingSettings() { }

    /** Caller must also own visible Gameplay and validate the native event target. */
    public static boolean isPendingCheckbox(int page, int slot) {
        return page == 1 && ((slot >= 10240 && slot <= 10242) || slot == 15872);
    }
    static boolean isManualOrRevolutionChoice(int slot) { return slot == 10240 || slot == 10241; }
    static boolean isRevolutionChoice(int slot) { return slot == 10241; }

    public static synchronized void verify() {
        if (verified) return;
        if (!Cache.isFlatReadOnly())
            throw new IllegalStateException("Pending settings require the paired flat cache");
        final MessageDigest digest;
        try { digest = MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        for (int[] binding : BINDINGS) {
            byte[] data = Cache.STORE.getIndexes()[binding[0]].getFile(binding[1], binding[2]);
            if (data == null) throw new IllegalStateException("Missing pending-settings cache binding "
                    + binding[0] + "/" + binding[1] + "/" + binding[2]);
            digest.update(data);
        }
        StringBuilder actual = new StringBuilder();
        for (byte value : digest.digest()) actual.append(String.format("%02x", value & 255));
        // Through the shared gate, so this pin obeys the same -Dataraxia.native.verifyCache switch
        // as the other five. This one covers all BINDINGS under a single rolling digest, so a
        // failure names the set rather than the file - re-pinning it means re-deriving every entry.
        NativeCacheVerification.requireBinding(
                "Pending-settings", "all bindings", EXPECTED, actual.toString());
        verified = true;
    }
}
