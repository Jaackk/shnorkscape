package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Observational workspace diagnostics for the native client.
 *
 * <p>The client owns window geometry, docking and tab links. This class deliberately
 * does not invent a second layout model: it records the small amount of workspace
 * traffic we can prove. It never parses, saves, acknowledges, or replays a frame.
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
     * A marker flushes ID-only permanent-variable changes from the preceding controlled action.
     * The literal {@code baseline} marker additionally snapshots the in-memory values, so a
     * real native preset save can be compared with the state immediately before it. Values
     * never leave this process; the timeline contains IDs and classifications only.
     */
    static void marker(Player player, String description) {
        if (player == null) return;
        String marker = description == null || description.trim().isEmpty() ? "(no description)" : description.trim();
        String payloads;
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null) return;
            payloads = state.uploadSummary();
            if ("baseline".equalsIgnoreCase(marker)) state.captureMarkerBaseline();
            state.clearUploadInterval();
        }
        Native950BugTest.event(player, "workspace", "permanent-variable-upload-summary",
                "marker", marker,
                "uploads", payloads, "scope", "opcode-14 IDs-only since-previous-marker-or-start",
                "anchors", "Custom1=slot6;activeLayout=slot8;nativePresetScripts=8372,8373,8374",
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
        final Map<Integer, Integer> markerBaseline = new LinkedHashMap<Integer, Integer>();
        final StringBuilder uploads = new StringBuilder();
        boolean hasBaseline;
        boolean hasMarkerBaseline;

        void recordPayload(int opcode, byte[] payload) {
            if (payload.length < 1 || ((payload.length - 1) % 6) != 0) {
                append("malformed(length=" + payload.length + ")");
                return;
            }
            int completion = payload[0] & 255;
            if (completion != 0 && completion != 1) { append("malformed(completion=" + completion + ")"); return; }
            Set<Integer> ids = new LinkedHashSet<Integer>();
            Set<Integer> changed = new LinkedHashSet<Integer>();
            Set<Integer> changedFromMarkerBaseline = new LinkedHashSet<Integer>();
            Set<Integer> valueEqualsSix = new LinkedHashSet<Integer>();
            Set<Integer> valueEqualsEight = new LinkedHashSet<Integer>();
            Set<Integer> covered = new LinkedHashSet<Integer>();
            Set<Integer> outsideDescriptor = new LinkedHashSet<Integer>();
            Set<Integer> initial = new LinkedHashSet<Integer>();
            int records = (payload.length - 1) / 6;
            for (int offset = 1; offset < payload.length; offset += 6) {
                int id = ((payload[offset] & 255) << 8) | (payload[offset + 1] & 255);
                int value = ((payload[offset + 2] & 255) << 24) | ((payload[offset + 3] & 255) << 16) | ((payload[offset + 4] & 255) << 8) | (payload[offset + 5] & 255);
                Integer previous = lastValues.put(id, value);
                ids.add(id);
                if (!hasBaseline) initial.add(id);
                if (previous != null && previous.intValue() != value) changed.add(id);
                if (hasMarkerBaseline && (!markerBaseline.containsKey(id) || markerBaseline.get(id).intValue() != value)) changedFromMarkerBaseline.add(id);
                // The save target is known to be slot 6 and the active layout is known to use
                // slot 8. These are candidate correlations only, not a claim that an ID's type
                // has been decoded. Values remain local and are never included in telemetry.
                if (value == 6) valueEqualsSix.add(id);
                if (value == 8) valueEqualsEight.add(id);
                if (Native950WorkspaceIntegerDescriptor.contains(id)) covered.add(id); else outsideDescriptor.add(id);
            }
            append("records=" + records + ";completion=" + completion + ";"
                    + "ids=" + ids(ids) + ";"
                    + (hasBaseline ? "changedIds=" + ids(changed) : "baselineIds=" + ids(initial)) + ";"
                    + "changedFromMarkerBaselineIds=" + (hasMarkerBaseline ? ids(changedFromMarkerBaseline) : "not-captured") + ";"
                    + "valueEquals6CandidateIds=" + ids(valueEqualsSix) + ";"
                    + "valueEquals8CandidateIds=" + ids(valueEqualsEight) + ";"
                    + "descriptorCoveredIds=" + ids(covered) + ";outsideDescriptorIds=" + ids(outsideDescriptor));
            hasBaseline = true;
        }

        String uploadSummary() { return uploads.length() == 0 ? "none" : uploads.toString(); }
        void clearUploadInterval() { uploads.setLength(0); }
        void captureMarkerBaseline() { markerBaseline.clear(); markerBaseline.putAll(lastValues); hasMarkerBaseline = true; }
        void append(String value) { if (uploads.length() > 0) uploads.append('|'); uploads.append(value); }
        private static String ids(Set<Integer> ids) {
            if (ids.isEmpty()) return "none";
            StringBuilder value = new StringBuilder();
            for (Integer id : ids) { if (value.length() > 0) value.append(','); value.append(id); }
            return value.toString();
        }

    }
}
