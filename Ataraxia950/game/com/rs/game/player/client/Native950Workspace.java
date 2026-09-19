package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Observational workspace diagnostics for the native client.
 *
 * <p>The client owns window geometry, docking and tab links. This class deliberately
 * does not invent a second layout model: it records the small amount of workspace
 * traffic we can prove. It never saves or replays a frame. The proof-stage parser below
 * recognizes only the exact initial synchronization so its owning session can acknowledge it.
 */
final class Native950Workspace {
    static final int INITIAL_UPLOAD_BYTES = 1291;
    static final int INITIAL_UPLOAD_RECORDS = 215;
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
     * Summarises opcode 14 permanent-variable uploads only while Bug Test Mode is on.
     * Values are retained in memory solely to calculate deltas and are never logged or saved.
     */
    static void inboundFrame(Player player, int opcode, byte[] payload) {
        if (player == null || payload == null || !Native950BugTest.enabled(player)) return;
        if (opcode != 14) return;
        synchronized (STATES) {
            State state = state(player);
            state.recordPayload(opcode, payload);
        }
    }

    /** Retained as the unhandled-frame hook; all recording now occurs at the frame boundary. */
    static void unhandledFrame(Player player, int opcode, byte[] payload) { }

    /**
     * Exact proof-stage gate for the initial native upload. Later deltas, partial batches and
     * malformed data deliberately do not match and receive no acknowledgement here.
     */
    static boolean isExactInitialPermanentVariablesUpload(byte[] payload) {
        if (payload == null || payload.length != INITIAL_UPLOAD_BYTES || (payload[0] & 255) != 1)
            return false;
        Set<Integer> expected = Native950WorkspaceIntegerDescriptor.bootstrapIds();
        Set<Integer> observed = new HashSet<Integer>(INITIAL_UPLOAD_RECORDS);
        for (int offset = 1; offset < payload.length; offset += 6) {
            int id = ((payload[offset] & 255) << 8) | (payload[offset + 1] & 255);
            if (!expected.contains(id) || !observed.add(id)) return false;
        }
        return observed.size() == INITIAL_UPLOAD_RECORDS && observed.equals(expected);
    }

    /**
     * A marker flushes ID-only permanent-variable changes from the preceding controlled action.
     */
    static void marker(Player player, String description) {
        if (player == null) return;
        String payloads;
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null) return;
            payloads = state.uploadSummary();
            state.clearUploadInterval();
        }
        Native950BugTest.event(player, "workspace", "permanent-variable-upload-summary",
                "marker", description == null || description.trim().isEmpty() ? "(no description)" : description.trim(),
                "uploads", payloads, "scope", "opcode-14 IDs-only since-previous-marker-or-start",
                "disposition", "session-local diagnostic; no values, persistence, acknowledgement, or replay");
    }

    static String status(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) {
                return "Workspace: native client-owned; no window report received this session. "
                        + "Enable ;;bugtest, move or dock a panel, then run ;;uilayout status.";
            }
            String viewport = state.width + "x" + state.height + " mode " + state.displayMode;
            return "Workspace: native client-owned; viewport " + viewport
                    + "; opcode-14 ID-only capture is active only while Bug Test Mode is enabled.";
        }
    }

    static String compactState(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) return "workspace=unreported";
            return "workspace=" + state.width + "x" + state.height + "/mode" + state.displayMode
                    + ";workspacePayloadCapture=14-ids-only-opt-in";
        }
    }

    static String pendingPayloads(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            return state == null ? "none" : state.uploadSummary();
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

    private static final class State {
        int displayMode, width, height, windowFlag;
        long windowReports;
        final Map<Integer, Integer> lastValues = new LinkedHashMap<Integer, Integer>();
        final StringBuilder uploads = new StringBuilder();
        boolean hasBaseline;

        void recordPayload(int opcode, byte[] payload) {
            if (payload.length < 1 || ((payload.length - 1) % 6) != 0) {
                append("malformed(length=" + payload.length + ")");
                return;
            }
            int completion = payload[0] & 255;
            if (completion != 0 && completion != 1) { append("malformed(completion=" + completion + ")"); return; }
            StringBuilder changed = new StringBuilder(); StringBuilder initial = new StringBuilder(); int records = (payload.length - 1) / 6;
            for (int offset = 1; offset < payload.length; offset += 6) {
                int id = ((payload[offset] & 255) << 8) | (payload[offset + 1] & 255);
                int value = ((payload[offset + 2] & 255) << 24) | ((payload[offset + 3] & 255) << 16) | ((payload[offset + 4] & 255) << 8) | (payload[offset + 5] & 255);
                Integer previous = lastValues.put(id, value);
                if (!hasBaseline) { if (initial.length() > 0) initial.append(','); initial.append(id); }
                if (previous != null && previous.intValue() != value) { if (changed.length() > 0) changed.append(','); changed.append(id); }
            }
            append("records=" + records + ";completion=" + completion + ";"
                    + (hasBaseline ? "changedIds=" + (changed.length() == 0 ? "none" : changed.toString())
                    : "baselineIds=" + initial.toString()));
            hasBaseline = true;
        }

        String uploadSummary() { return uploads.length() == 0 ? "none" : uploads.toString(); }
        void clearUploadInterval() { uploads.setLength(0); }
        void append(String value) { if (uploads.length() > 0) uploads.append('|'); uploads.append(value); }

    }
}
