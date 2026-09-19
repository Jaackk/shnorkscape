package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;

import java.security.MessageDigest;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Observational workspace diagnostics for the native client.
 *
 * <p>The client owns window geometry, docking and tab links. This class deliberately
 * does not invent a second layout model: it records the small amount of workspace
 * traffic we can prove, and makes candidate layout frames visible to Bug Test without
 * retaining their raw bytes. It never parses, saves, acknowledges, or replays a frame.
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
     * Records only frame-family counts for all non-chat game input while Bug Test is
     * explicitly active. Potential workspace-upload families retain a short hash, never bytes.
     */
    static void inboundFrame(Player player, int opcode, byte[] payload) {
        if (player == null || payload == null || !Native950BugTest.enabled(player)) return;
        synchronized (STATES) {
            State state = state(player);
            state.recordFrame(opcode, payload);
        }
    }

    /** Retained as the unhandled-frame hook; all recording now occurs at the frame boundary. */
    static void unhandledFrame(Player player, int opcode, byte[] payload) { }

    /**
     * A marker flushes counts since the previous marker. Candidate signatures are bounded and
     * opaque, allowing a controlled live test to distinguish frame families without retaining
     * chat, authentication, or arbitrary UI payload bytes.
     */
    static void marker(Player player, String description) {
        if (player == null) return;
        String counts, candidates;
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null) return;
            counts = state.countSummary();
            candidates = state.candidateSummary();
            state.clearFrameInterval();
        }
        Native950BugTest.event(player, "workspace", "inbound-frame-summary",
                "marker", description == null || description.trim().isEmpty() ? "(no description)" : description.trim(),
                "frames", counts, "candidates", candidates,
                "scope", "non-chat game frames since-previous-marker-or-start",
                "disposition", "opaque-observation-only; no layout state decoded");
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
                    + "; frame capture is active only while Bug Test Mode is enabled.";
        }
    }

    static String compactState(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) return "workspace=unreported";
            return "workspace=" + state.width + "x" + state.height + "/mode" + state.displayMode
                    + ";workspaceFrameCapture=opt-in";
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
        long windowReports;
        final long[] frameCounts = new long[256];
        final long[] frameBytes = new long[256];
        final Map<String, Integer> candidateSignatures = new LinkedHashMap<String, Integer>();

        void recordFrame(int opcode, byte[] payload) {
            if (opcode < 0 || opcode >= frameCounts.length || isChat(opcode)) return;
            frameCounts[opcode]++;
            frameBytes[opcode] += payload.length;
            if (!isWorkspaceCandidate(opcode)) return;
            String key = opcode + "/" + payload.length + "/" + fingerprint(payload);
            Integer count = candidateSignatures.get(key);
            if (count != null) { candidateSignatures.put(key, count + 1); return; }
            // Four candidate families in one short controlled test should not need more than
            // this. The bound protects the diagnostic from unrelated client activity.
            if (candidateSignatures.size() < 24) candidateSignatures.put(key, 1);
        }

        String countSummary() {
            StringBuilder result = new StringBuilder();
            for (int opcode = 0; opcode < frameCounts.length; opcode++) {
                if (frameCounts[opcode] == 0) continue;
                if (result.length() > 0) result.append(',');
                result.append(opcode).append('x').append(frameCounts[opcode]).append('/').append(frameBytes[opcode]);
            }
            return result.length() == 0 ? "none" : result.toString();
        }

        String candidateSummary() {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, Integer> entry : candidateSignatures.entrySet()) {
                if (result.length() > 0) result.append(',');
                result.append(entry.getKey()).append('x').append(entry.getValue());
            }
            return result.length() == 0 ? "none" : result.toString();
        }

        void clearFrameInterval() {
            java.util.Arrays.fill(frameCounts, 0);
            java.util.Arrays.fill(frameBytes, 0);
            candidateSignatures.clear();
        }
    }

    private static boolean isChat(int opcode) { return opcode == 68 || opcode == 69; }
    // Variable-length opaque families retained from 950 static and prior live evidence only.
    private static boolean isWorkspaceCandidate(int opcode) { return opcode == 14 || opcode == 33 || opcode == 74 || opcode == 125; }
}
