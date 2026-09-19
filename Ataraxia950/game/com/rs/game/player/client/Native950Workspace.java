package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;

import java.security.MessageDigest;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Observational workspace diagnostics for the native client.
 *
 * <p>The client owns window geometry, docking and tab links. This class deliberately
 * does not invent a second layout model: it records the small amount of workspace
 * traffic we can prove, and makes candidate layout frames visible to Bug Test without
 * retaining their raw bytes.
 */
final class Native950Workspace {
    private static final Map<Player, State> STATES = new IdentityHashMap<Player, State>();

    private Native950Workspace() { }

    static void windowReport(Player player, Native950Actions.WindowReportAction report) {
        if (player == null || report == null) return;
        synchronized (STATES) {
            State state = state(player);
            state.displayMode = report.displayMode();
            state.width = report.width();
            state.height = report.height();
            state.windowFlag = report.flag();
            state.windowReports++;
        }
        Native950BugTest.event(player, "workspace", "window-report",
                "displayMode", report.displayMode(), "width", report.width(), "height", report.height(),
                "flag", report.flag());
    }

    /**
     * A legacy NIS-layout update is a one-byte header followed by six-byte entries.
     * The 950 opcode has not yet been derived, so this only labels a candidate and
     * never parses, saves, acknowledges, or replays it.
     */
    static void unhandledFrame(Player player, int opcode, byte[] payload) {
        if (player == null || payload == null) return;
        int length = payload.length;
        boolean tupleShape = length >= 7 && (length - 1) % 6 == 0 && (payload[0] == 0 || payload[0] == 1);
        if (!tupleShape) return;
        int entries = (length - 1) / 6;
        int minimum = Integer.MAX_VALUE, maximum = Integer.MIN_VALUE;
        for (int offset = 1; offset < length; offset += 6) {
            int id = ((payload[offset] & 255) << 8) | (payload[offset + 1] & 255);
            minimum = Math.min(minimum, id);
            maximum = Math.max(maximum, id);
        }
        String fingerprint = fingerprint(payload);
        synchronized (STATES) {
            State state = state(player);
            state.layoutCandidateFrames++;
            state.lastCandidateOpcode = opcode;
            state.lastCandidateEntries = entries;
            state.lastCandidateMinimumId = minimum;
            state.lastCandidateMaximumId = maximum;
            state.lastCandidateFingerprint = fingerprint;
        }
        Native950BugTest.event(player, "workspace", "layout-frame-candidate",
                "opcode", opcode, "bytes", length, "entries", entries, "minimumId", minimum,
                "maximumId", maximum, "fingerprint", fingerprint,
                "disposition", "observed-only; native-950 contract not yet derived");
    }

    static String status(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) {
                return "Workspace: native client-owned; no window report received this session. "
                        + "Enable ;;bugtest, move or dock a panel, then run ;;uilayout status.";
            }
            String viewport = state.width + "x" + state.height + " mode " + state.displayMode;
            if (state.layoutCandidateFrames == 0)
                return "Workspace: native client-owned; viewport " + viewport
                        + "; no layout-frame candidate captured this session.";
            return "Workspace: native client-owned; viewport " + viewport + "; layout candidates "
                    + state.layoutCandidateFrames + " (latest opcode " + state.lastCandidateOpcode
                    + ", entries " + state.lastCandidateEntries + ", ids " + state.lastCandidateMinimumId
                    + "-" + state.lastCandidateMaximumId + ", fingerprint " + state.lastCandidateFingerprint + ").";
        }
    }

    static String compactState(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) return "workspace=unreported";
            return "workspace=" + state.width + "x" + state.height + "/mode" + state.displayMode
                    + ";layoutCandidates=" + state.layoutCandidateFrames;
        }
    }

    static void close(Player player) {
        synchronized (STATES) { STATES.remove(player); }
    }

    private static State state(Player player) {
        State state = STATES.get(player);
        if (state == null) {
            state = new State();
            STATES.put(player, state);
        }
        return state;
    }

    private static String fingerprint(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder result = new StringBuilder(12);
            for (int index = 0; index < 6; index++) result.append(String.format("%02x", digest[index] & 255));
            return result.toString();
        } catch (Exception unavailable) {
            return "unavailable";
        }
    }

    private static final class State {
        int displayMode, width, height, windowFlag;
        long windowReports, layoutCandidateFrames;
        int lastCandidateOpcode = -1, lastCandidateEntries, lastCandidateMinimumId, lastCandidateMaximumId;
        String lastCandidateFingerprint = "none";
    }
}
