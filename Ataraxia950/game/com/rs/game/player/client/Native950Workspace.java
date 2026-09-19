package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
     * Stores complete bytes only for the two fixed-size workspace candidates isolated by the
     * controlled live trace. This is enabled solely by Bug Test Mode and never accepts chat,
     * login, credentials, or arbitrary unknown payloads.
     */
    static void inboundFrame(Player player, int opcode, byte[] payload) {
        if (player == null || payload == null || !Native950BugTest.enabled(player)) return;
        if (!isWorkspaceMutationCandidate(opcode)) return;
        synchronized (STATES) {
            State state = state(player);
            state.recordPayload(opcode, payload);
        }
    }

    /** Retained as the unhandled-frame hook; all recording now occurs at the frame boundary. */
    static void unhandledFrame(Player player, int opcode, byte[] payload) { }

    /**
     * A marker flushes the ordered 54/65 byte sequences from the preceding controlled action.
     * The session-local JSONL is deliberately the only retention point.
     */
    static void marker(Player player, String description) {
        if (player == null) return;
        String payloads;
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null) return;
            payloads = state.payloadSummary();
            state.clearPayloadInterval();
        }
        Native950BugTest.event(player, "workspace", "mutation-payloads",
                "marker", description == null || description.trim().isEmpty() ? "(no description)" : description.trim(),
                "frames", payloads, "scope", "opcode-54-and-opcode-65-only since-previous-marker-or-start",
                "disposition", "session-local raw diagnostic; no layout state decoded");
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
                    + "; 54/65 payload capture is active only while Bug Test Mode is enabled.";
        }
    }

    static String compactState(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            if (state == null || state.windowReports == 0) return "workspace=unreported";
            return "workspace=" + state.width + "x" + state.height + "/mode" + state.displayMode
                    + ";workspacePayloadCapture=54,65-opt-in";
        }
    }

    static String pendingPayloads(Player player) {
        synchronized (STATES) {
            State state = STATES.get(player);
            return state == null ? "none" : state.payloadSummary();
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
        final List<Payload> payloads = new ArrayList<Payload>();

        void recordPayload(int opcode, byte[] payload) {
            payloads.add(new Payload(opcode, payload));
        }

        String payloadSummary() {
            if (payloads.isEmpty()) return "none";
            StringBuilder result = new StringBuilder();
            for (Payload payload : payloads) {
                if (result.length() > 0) result.append(',');
                result.append(payload.opcode).append('/').append(payload.bytes.length).append('/').append(hex(payload.bytes));
            }
            return result.toString();
        }

        void clearPayloadInterval() { payloads.clear(); }

        private static String hex(byte[] bytes) {
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format("%02x", value & 255));
            return result.toString();
        }

        private static final class Payload {
            final int opcode;
            final byte[] bytes;
            Payload(int opcode, byte[] bytes) {
                this.opcode = opcode;
                this.bytes = bytes.clone();
            }
        }
    }

    private static boolean isWorkspaceMutationCandidate(int opcode) { return opcode == 54 || opcode == 65; }
}
