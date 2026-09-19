package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Passive, bounded combat flight recorder. No method in this class mutates gameplay state. */
final class Native950CombatQa {
    interface WindowCapturer { Native950WindowCapture.Result capture(File output); }
    private static final int MAX_DELAYED = 32;
    private static final long NORMAL_CAPTURE_MAX_AGE_MS = 2500L;
    private static final long HIGH_CAPTURE_MAX_AGE_MS = 7000L;
    private static final ThreadPoolExecutor LOG = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(4096), daemon("native950-combatqa-log"), new ThreadPoolExecutor.AbortPolicy());
    private static final ThreadPoolExecutor CAPTURE = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(3), daemon("native950-combatqa-capture"), new ThreadPoolExecutor.AbortPolicy());
    private static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1, daemon("native950-combatqa-timer"));
    private static final AtomicInteger DELAYED = new AtomicInteger();
    private static final Map<Player, Session> SESSIONS = new IdentityHashMap<Player, Session>();
    private static volatile WindowCapturer windowCapturer=new WindowCapturer(){public Native950WindowCapture.Result capture(File output){return Native950WindowCapture.capture(output);}};

    static {
        TIMER.setRemoveOnCancelPolicy(true);
        TIMER.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    private Native950CombatQa() { }

    static void setWindowCapturerForTests(WindowCapturer replacement) {
        windowCapturer=replacement==null?new WindowCapturer(){public Native950WindowCapture.Result capture(File output){return Native950WindowCapture.capture(output);}}:replacement;
    }

    static synchronized String start(Player player) {
        Session existing = SESSIONS.get(player);
        if (existing != null) return "Combat QA is already active: " + existing.id + ".";
        Session session = new Session(player);
        SESSIONS.put(player, session);
        session.observe("session", "started", map("player", player.getUsername(), "mode", "observational",
                "persistentStateChanged", false));
        session.storyboard("session-start", session.correlation, true, 0L);
        return "Combat QA started. Play normally; use ;;bug for anything subjective.";
    }

    static synchronized String stop(Player player, String reason) {
        Session session = SESSIONS.remove(player);
        if (session == null) return "Combat QA is not active.";
        session.stop(reason == null ? "command" : reason);
        return "Combat QA stopped. The session index is finalizing in the background.";
    }

    static synchronized String status(Player player) {
        Session session = SESSIONS.get(player);
        return session == null ? "Combat QA is not active."
                : "Combat QA active: " + session.id + "; events " + session.events.get()
                + ", screenshots saved " + session.screenshotsSaved.get() + ", pending "
                + session.outstandingCaptures.get() + ", anomalies " + session.anomalies.get() + ".";
    }

    static synchronized String reset(Player player) {
        Session session = SESSIONS.get(player);
        if (session == null) return "Start Combat QA first with ;;combatqa.";
        session.resetRecorder();
        return "Combat QA recorder reset. Gameplay state was not changed.";
    }

    static String cleanup() {
        try {
            LOG.execute(new Runnable() { public void run() { cleanupReviewedSessions(root()); } });
            return "Combat QA cleanup queued. Only explicitly reviewed redundant/stale evidence is eligible.";
        } catch (RejectedExecutionException full) {
            return "Combat QA cleanup could not be queued; try again after current logging finishes.";
        }
    }

    static synchronized void close(Player player, String reason) {
        Session session = SESSIONS.remove(player);
        if (session != null) session.stop(reason == null ? "session-close" : reason);
    }

    static void marker(Player player, String description) {
        Session session = session(player);
        if (session == null) return;
        String text = description == null || description.trim().isEmpty() ? "(no description)" : description.trim();
        session.manualBugs.incrementAndGet();
        session.observe("marker", "manual-bug", map("description", text, "retention", "high"));
        session.storyboard("manual-bug", session.correlation, true, 0L);
    }

    static void command(Player player, String command, String argument) {
        Session session = session(player);
        if (session != null) session.observe("command", "development",
                map("command", command, "argument", argument == null ? "" : argument));
    }

    static void action(Player player, Native950Actions.Action action) {
        Session session = session(player);
        if (session == null || action == null) return;
        if (action instanceof Native950Actions.InterfaceAction) {
            Native950Actions.InterfaceAction a = (Native950Actions.InterfaceAction) action;
            session.observe("input", "interface", map("interface", a.interfaceId(), "component", a.componentId(),
                    "slot", a.slot(), "option", a.option(), "item", a.itemId()));
            if (combatInterface(a.interfaceId())) session.storyboard("combat-interface", session.correlation, false, 100L);
        } else if (action instanceof Native950Actions.DragAction) {
            Native950Actions.DragAction a = (Native950Actions.DragAction) action;
            session.newCorrelation("drag");
            session.observe("input", "drag", map("source", a.sourceInterfaceId() + ":" + a.sourceComponentId(),
                    "sourceSlot", a.sourceSlot(), "sourceItem", a.sourceItemId(),
                    "target", a.targetInterfaceId() + ":" + a.targetComponentId(),
                    "targetSlot", a.targetSlot(), "targetItem", a.targetItemId()));
            session.storyboard("drag", session.correlation, false, 100L);
        } else if (action instanceof Native950Actions.NpcAction) {
            Native950Actions.NpcAction a = (Native950Actions.NpcAction) action;
            session.newCorrelation("npc");
            session.observe("input", "npc", map("index", a.index(), "option", a.option()));
        } else if (action instanceof Native950Actions.CloseModalAction) {
            session.observe("interface", "close-modal", map());
        } else if (action instanceof Native950Actions.StringDialogueAction) {
            Native950Actions.StringDialogueAction a=(Native950Actions.StringDialogueAction)action;
            session.observe("input","string-dialogue",map("kind",a.isNameDialogue()?"name":"text",
                    "characters",a.text()==null?0:a.text().length(),"content","redacted"));
        } else if (action instanceof Native950Actions.CountDialogueAction) {
            session.observe("input","count-dialogue",map("value",((Native950Actions.CountDialogueAction)action).count()));
        }
    }

    static void event(Player player, String category, String name, Object... fields) {
        Session session = session(player);
        if (session != null) session.observe(category, name, map(fields));
    }

    static void unhandledFrame(Player player, int opcode, int bytes) {
        Session session = session(player);
        if (session != null) session.observe("input", "unhandled-frame",
                map("opcode", opcode, "bytes", Math.max(0, bytes), "payload", "redacted"));
    }

    private static synchronized Session session(Player player) { return SESSIONS.get(player); }

    private static boolean combatInterface(int id) {
        return id == 1430 || id == 1431 || id == 1436 || id == 1449 || id == 1450 || id == 1452
                || id == 1458 || id == 1460 || id == 1461 || id == 1881 || id == 1882
                || id == 1883 || id == 1884 || id == 1885 || id == 1886 || id == 1888;
    }

    private static Map<String, Object> map(Object... values) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (values == null) return result;
        for (int i = 0; i + 1 < values.length; i += 2) result.put(String.valueOf(values[i]), values[i + 1]);
        return result;
    }

    private static final class Session {
        final Player player;
        final String id;
        final File directory, timeline, lifecycle;
        final long startedMillis = System.currentTimeMillis(), startedTick = tick();
        final AtomicLong events = new AtomicLong(), anomalies = new AtomicLong(), droppedEvents = new AtomicLong();
        final AtomicLong droppedCaptures = new AtomicLong(), screenshotsSaved = new AtomicLong(), screenshotFailures = new AtomicLong();
        final AtomicInteger outstandingCaptures = new AtomicInteger();
        final AtomicLong sequence = new AtomicLong();
        final AtomicLong manualBugs = new AtomicLong();
        final Set<String> abilities = new LinkedHashSet<String>(), channels = new LinkedHashSet<String>();
        final Set<String> buffs = new LinkedHashSet<String>(), prayerActions = new LinkedHashSet<String>();
        final Set<String> magicActions = new LinkedHashSet<String>(), potionActions = new LinkedHashSet<String>();
        final Set<String> anomalyKinds = new LinkedHashSet<String>();
        final Map<String, Long> anomalyDebounce = new LinkedHashMap<String, Long>();
        String correlation = "qa-000000";
        String pendingSource = "unknown";
        String lastSafeQaState = "not-sampled";
        long correlationNumber, queuedAt = -1L, lastCombatTick = -100L;
        String queuedStructure = "none";
        boolean stopping, finalized;
        int manualExecutions, revolutionExecutions, storyboardGroups, exceptionCount, disconnectCount;
        long pendingEffectTick = -1L;
        String pendingEffect = null;

        Session(Player player) {
            this.player = player;
            id = "session-" + stamp() + "-" + safe(player.getUsername());
            directory = new File(root(), id);
            directory.mkdirs();
            timeline = new File(directory, "timeline.jsonl");
            lifecycle = new File(directory, "evidence-lifecycle.tsv");
            writeDirect(new File(directory, "session-state.txt"), "ACTIVE\n");
            appendLifecycle("# filename\tstate\tcorrelation\tevent\n");
        }

        synchronized void newCorrelation(String kind) {
            correlation = String.format(Locale.ROOT, "qa-%06d-%s", ++correlationNumber, safe(kind));
        }

        synchronized void resetRecorder() {
            anomalyDebounce.clear(); queuedAt = -1L; queuedStructure = "none";
            pendingEffect = null; pendingEffectTick = -1L;
            newCorrelation("reset");
            observe("session", "recorder-reset", map("gameplayStateChanged", false));
            storyboard("reset-baseline", correlation, true, 0L);
        }

        synchronized void observe(String category, String name, Map<String, Object> fields) {
            if (finalized) return;
            long nowTick = tick();
            if ("combat".equals(category) && ("ability-request".equals(name) || "attack-request".equals(name))) {
                newCorrelation(name); pendingSource = "manual"; lastCombatTick = nowTick;
            }
            if ("ability-queued".equals(name) || "ability-queue-replaced".equals(name)) {
                queuedAt = nowTick; queuedStructure = value(fields, "structure", "unknown");
                String source = value(fields, "source", "");
                if (!source.isEmpty()) pendingSource = source;
            }
            if ("revolution-selected".equals(name)) {
                newCorrelation("revolution"); pendingSource = "revolution"; lastCombatTick = nowTick;
            }
            if (name.startsWith("ability-queue-cancelled") || "ability-executed".equals(name)) {
                queuedAt = -1L; queuedStructure = "none";
            }
            if (queuedAt >= 0L && nowTick - queuedAt > 10L)
                anomaly("queued-ability-pending", map("structure", queuedStructure, "pendingTicks", nowTick - queuedAt));
            if (pendingEffect != null && nowTick > pendingEffectTick) {
                anomaly("effect-without-presentation-attempt", map("effect", pendingEffect, "appliedTick", pendingEffectTick));
                pendingEffect = null;
            }
            if ("status".equals(category) && name.startsWith("timer-")) pendingEffect = null;

            updateSummary(category, name, fields);
            Map<String, Object> complete = new LinkedHashMap<String, Object>();
            complete.put("tick", nowTick);
            complete.put("correlationId", correlation);
            complete.putAll(fields);
            complete.put("qaState", qaState());
            enqueue(json(category, name, complete));
            events.incrementAndGet();
            detect(category, name, fields, nowTick);
            scheduleFor(category, name, fields);
        }

        private void updateSummary(String category, String name, Map<String, Object> fields) {
            if ("ability-executed".equals(name)) {
                String ability = value(fields, "name", value(fields, "structure", "unknown"));
                abilities.add(ability);
                if ("revolution".equals(pendingSource)) revolutionExecutions++; else manualExecutions++;
                if (!"0".equals(value(fields, "channelEndTick", "0"))) channels.add(ability);
                pendingSource = "unknown";
            }
            if (name.contains("channel")) channels.add(value(fields, "name", value(fields, "structure", name)));
            if (name.startsWith("effect-") || "combat".equals(category) && name.contains("buff"))
                buffs.add(value(fields, "effect", name));
            if ("prayer".equals(category)) prayerActions.add(name);
            if ("magic".equals(category)) magicActions.add(name + ":" + value(fields, "spell", ""));
            String lower = (category + " " + name + " " + fields).toLowerCase(Locale.ROOT);
            if (lower.contains("potion") || lower.contains("overload")) potionActions.add(name);
            if (("session".equals(category) && name.contains("failure")) || name.contains("exception")) exceptionCount++;
        }

        private void detect(String category, String name, Map<String, Object> fields, long nowTick) {
            if ("ability-executed".equals(name)) {
                if ("none".equals(value(fields, "animation", "none")))
                    anomaly("supported-ability-no-animation", map("ability", value(fields, "name", "unknown"),
                            "structure", value(fields, "structure", "unknown")));
                String follow = value(fields, "followUpHitTicks", "[]");
                if (collapsedTicks(follow)) anomaly("collapsed-follow-up-hit-ticks", map("ticks", follow,
                        "ability", value(fields, "name", "unknown")));
                int style = integer(fields.get("structure"), -1);
                if (style >= 0) style = Native950MeleeCombat.abilityStyle(style);
                if (missingMagicVisual(style, value(fields, "target", ""),
                        value(fields, "graphic", "none"), value(fields, "targetGraphic", "none"),
                        value(fields, "presentationEvidence", "unknown")))
                    anomaly("magic-ability-no-visual-effect", map("ability", value(fields, "name", "unknown")));
                long gcd = number(fields.get("gcdEndTick"), nowTick);
                if (gcd < nowTick) anomaly("contradictory-gcd", map("gcdEndTick", gcd, "executionTick", nowTick));
            }
            if ("effect-started".equals(name)) {
                pendingEffect = value(fields, "effect", "unknown"); pendingEffectTick = nowTick;
            }
            if ("visual-write-failed".equals(name) || "phase-failure".equals(name))
                anomaly("strict-or-client-write-failure", map("event", name, "detail", fields));
            if ("unhandled-action".equals(name) && "combat".equals(category) && nowTick - lastCombatTick <= 6L)
                anomaly("unhandled-combat-context-input", map("event", name, "detail", fields));
        }

        private void anomaly(String kind, Map<String, Object> detail) {
            long now = tick();
            Long prior = anomalyDebounce.get(kind);
            if (prior != null && now - prior.longValue() < 5L) return;
            anomalyDebounce.put(kind, now);
            anomalies.incrementAndGet(); anomalyKinds.add(kind);
            Map<String, Object> fields = new LinkedHashMap<String, Object>();
            fields.put("tick", now); fields.put("correlationId", correlation); fields.put("kind", kind);
            fields.putAll(detail); fields.put("qaState", qaState());
            enqueue(json("anomaly", "automatic-marker", fields));
            storyBoardForAnomaly(kind);
        }

        private void storyBoardForAnomaly(String kind) { storyboard("anomaly-" + kind, correlation, true, 0L); }

        private void scheduleFor(String category, String name, Map<String, Object> fields) {
            if (stopping) return;
            if ("ability-request".equals(name) || "ability-queued".equals(name)
                    || "ability-queue-replaced".equals(name) || "attack-request".equals(name)) {
                storyboard(name, correlation, false, 0L); return;
            }
            if ("ability-executed".equals(name)) {
                storyboardGroups++;
                storyboard(name, correlation, false, 150L);
                long channelEnd = number(fields.get("channelEndTick"), 0L), now = tick();
                if (channelEnd > now + 2L) storyboard("channel-end", correlation, false,
                        Math.min(8000L, (channelEnd - now) * 600L));
                return;
            }
            boolean meaningfulActionBar = "bound".equals(name) || "cleared".equals(name)
                    || "rearranged".equals(name) || "preset-selection".equals(name)
                    || "visual-write-failed".equals(name);
            boolean transition = "prayer".equals(category) || "magic".equals(category)
                    || meaningfulActionBar || "status".equals(category)
                    || name.startsWith("effect-") || name.contains("potion") || name.contains("overload")
                    || name.contains("channel");
            if (transition) storyboard(category + "-" + name, correlation, false, 120L);
        }

        void storyboard(String event, String correlationId, boolean high, long delayMillis) {
            if (finalized) return;
            final long requestedAt = System.currentTimeMillis();
            final String file = String.format(Locale.ROOT, "%06d-%s-%s-%dms.png", sequence.incrementAndGet(),
                    safe(correlationId), safe(event), delayMillis);
            outstandingCaptures.incrementAndGet();
            appendLifecycle(file + "\tACTIVE\t" + correlationId + "\t" + event + "\n");
            Map<String, Object> planned = map("file", file, "delayMs", delayMillis, "priority", high ? "high" : "normal",
                    "correlationId", correlationId, "event", event, "requestedAt", iso(requestedAt));
            enqueue(json("screenshot", "planned", planned));
            if (delayMillis <= 0L) { submitCapture(new CaptureJob(this, file, event, correlationId, high, requestedAt)); return; }
            if (!reserveDelayed()) { dropCapture(file, "delayed-queue-full"); return; }
            TIMER.schedule(new Runnable() { public void run() {
                DELAYED.decrementAndGet();
                submitCapture(new CaptureJob(Session.this, file, event, correlationId, high, requestedAt));
            }}, delayMillis, TimeUnit.MILLISECONDS);
        }

        void dropCapture(String file, String reason) {
            droppedCaptures.incrementAndGet(); outstandingCaptures.decrementAndGet();
            enqueue(json("screenshot", "dropped", map("file", file, "reason", reason)));
            maybeFinalize();
        }

        void captureFinished(String file, String event, String correlationId, boolean high, long requestedAt,
                             long startedAt,
                             Native950WindowCapture.Result result) {
            if (result.saved) screenshotsSaved.incrementAndGet(); else screenshotFailures.incrementAndGet();
            String life = high ? "UNRESOLVED_EVIDENCE" : "UNREVIEWED";
            appendLifecycle(file + "\t" + life + "\t" + correlationId + "\t" + event + "\n");
            enqueue(json("screenshot", result.saved ? "saved" : "failed", map("file", file,
                    "event", event, "correlationId", correlationId, "exit", result.exitCode,
                     "helper", result.helper, "command", result.command, "output", result.output,
                     "windowEvidence", result.output, "bytes", result.bytes,
                     "errorType", result.errorType, "message", result.errorMessage, "lifecycle", life,
                     "requestedAt", iso(requestedAt), "captureStartedAt", iso(startedAt),
                     "queueLagMs", Math.max(0L, startedAt-requestedAt),
                     "completedAt", iso(System.currentTimeMillis()))));
            outstandingCaptures.decrementAndGet(); maybeFinalize();
        }

        private String qaState() {
            String sampled=state(player);
            if(!sampled.startsWith("state-unavailable:"))lastSafeQaState=sampled;
            return sampled.startsWith("state-unavailable:")
                    ?lastSafeQaState+";sample=last-safe;reason="+sampled.substring("state-unavailable:".length())
                    :sampled;
        }

        synchronized void stop(String reason) {
            if (stopping) return;
            stopping = true;
            if (!"command".equals(reason)) { disconnectCount++; anomaly("session-ended-during-qa", map("reason", reason)); }
            observe("session", "stopped", map("reason", reason, "gameplayStateChanged", false));
            maybeFinalize();
        }

        void maybeFinalize() {
            if (!stopping || finalized || outstandingCaptures.get() != 0) return;
            synchronized (this) {
                if (finalized || outstandingCaptures.get() != 0) return;
                finalized = true;
            }
            try {
                LOG.execute(new Runnable() { public void run() { writeIndex(); } });
            } catch (RejectedExecutionException full) {
                droppedEvents.incrementAndGet();
                writeIndex();
            }
        }

        private synchronized void writeIndex() {
            long ended = System.currentTimeMillis();
            Map<String, Object> summary = new LinkedHashMap<String, Object>();
            summary.put("sessionId", id); summary.put("player", player.getUsername());
            summary.put("gitCommit", gitCommit()); summary.put("deployedJar", deployedJar());
            summary.put("deployedJarSha256", deployedJarSha());
            summary.put("started", iso(startedMillis)); summary.put("ended", iso(ended));
            summary.put("startTick", startedTick); summary.put("endTick", tick());
            summary.put("durationMillis", ended - startedMillis); summary.put("mode", "observational");
            summary.put("persistentStateChanged", false); summary.put("abilities", abilities);
            summary.put("manualAbilityExecutions", manualExecutions); summary.put("revolutionAbilityExecutions", revolutionExecutions);
            summary.put("channels", channels); summary.put("buffsDebuffs", buffs);
            summary.put("prayerActions", prayerActions); summary.put("magicActions", magicActions);
            summary.put("potionActions", potionActions); summary.put("automaticAnomalies", anomalies.get());
            summary.put("anomalyKinds", anomalyKinds); summary.put("manualBugMarkers", manualBugs.get());
            summary.put("storyboardGroups", storyboardGroups); summary.put("screenshotsSaved", screenshotsSaved.get());
            summary.put("screenshotFailures", screenshotFailures.get()); summary.put("exceptions", exceptionCount);
            summary.put("disconnects", disconnectCount); summary.put("events", events.get());
            summary.put("droppedEvents", droppedEvents.get()); summary.put("droppedCaptures", droppedCaptures.get());
            summary.put("timeline", "timeline.jsonl"); summary.put("evidenceLifecycle", "evidence-lifecycle.tsv");
            writeDirect(new File(directory, "session-index.json"), jsonObject(summary) + "\n");
            writeDirect(new File(directory, "session-state.txt"), "UNREVIEWED\n");
            File latest = new File(root(), "latest-completed.txt");
            writeDirect(latest, directory.getAbsolutePath() + "\n");
        }

        void enqueue(final String line) {
            try {
                LOG.execute(new Runnable() { public void run() { append(timeline, line + "\n"); } });
            } catch (RejectedExecutionException full) { droppedEvents.incrementAndGet(); }
        }

        void appendLifecycle(final String text) {
            try {
                LOG.execute(new Runnable() { public void run() { append(lifecycle, text); } });
            } catch (RejectedExecutionException full) { droppedEvents.incrementAndGet(); }
        }
    }

    private static final class CaptureJob implements Runnable {
        final Session session; final String file, event, correlation; final boolean high; final long requestedAt;
        CaptureJob(Session session, String file, String event, String correlation, boolean high, long requestedAt) {
            this.session = session; this.file = file; this.event = event; this.correlation = correlation;
            this.high = high; this.requestedAt=requestedAt;
        }
        public void run() {
            long startedAt=System.currentTimeMillis();
            if(captureIsStale(high,requestedAt,startedAt)){
                session.dropCapture(file,"stale-before-capture:"+(startedAt-requestedAt)+"ms");
                return;
            }
            Native950WindowCapture.Result result = windowCapturer.capture(new File(session.directory, file));
            session.captureFinished(file, event, correlation, high, requestedAt, startedAt, result);
        }
    }

    private static void submitCapture(CaptureJob job) {
        try { CAPTURE.execute(job); }
        catch (RejectedExecutionException full) {
            if (job.high && evictNormalCapture()) {
                try { CAPTURE.execute(job); return; } catch (RejectedExecutionException stillFull) { }
            }
            job.session.dropCapture(job.file, "capture-queue-full");
        }
    }

    private static boolean evictNormalCapture() {
        for (Runnable queued : CAPTURE.getQueue()) if (queued instanceof CaptureJob && !((CaptureJob) queued).high
                && CAPTURE.getQueue().remove(queued)) {
            CaptureJob removed = (CaptureJob) queued;
            removed.session.dropCapture(removed.file, "evicted-for-high-priority-marker");
            return true;
        }
        return false;
    }

    private static boolean reserveDelayed() {
        while (true) {
            int current = DELAYED.get();
            if (current >= MAX_DELAYED) return false;
            if (DELAYED.compareAndSet(current, current + 1)) return true;
        }
    }

    static boolean mayDelete(String sessionState, boolean reviewReportExists, String lifecycleState) {
        if (!reviewReportExists || sessionState == null || "ACTIVE".equalsIgnoreCase(sessionState.trim())) return false;
        String value = lifecycleState == null ? "" : lifecycleState.trim().toUpperCase(Locale.ROOT);
        return value.equals("REDUNDANT") || value.equals("STALE") || value.equals("SUPERSEDED");
    }

    private static void cleanupReviewedSessions(File root) {
        File[] sessions = root.listFiles();
        if (sessions == null) return;
        for (File session : sessions) {
            if (!session.isDirectory() || !session.getName().startsWith("session-")) continue;
            String state = readFirst(new File(session, "session-state.txt"));
            boolean reviewed = new File(session, "review-report.json").isFile();
            File manifest = new File(session, "evidence-lifecycle.tsv");
            if (!reviewed || !manifest.isFile() || "ACTIVE".equalsIgnoreCase(state)) continue;
            Map<String, String> latest = new LinkedHashMap<String, String>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(manifest));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("#")) continue;
                        String[] columns = line.split("\\t", 4);
                        if (columns.length >= 2) latest.put(columns[0], columns[1]);
                    }
                } finally { reader.close(); }
            } catch (Exception ignored) { continue; }
            for (Map.Entry<String, String> entry : latest.entrySet()) if (mayDelete(state, reviewed, entry.getValue())) {
                File image = new File(session, entry.getKey());
                try {
                    if (image.getCanonicalFile().getParentFile().equals(session.getCanonicalFile())
                            && image.getName().toLowerCase(Locale.ROOT).endsWith(".png")) image.delete();
                } catch (Exception ignored) { }
            }
        }
    }

    static boolean collapsedTicks(String value) {
        String cleaned = value == null ? "" : value.replace("[", "").replace("]", "").trim();
        if (cleaned.isEmpty()) return false;
        Set<String> distinct = new LinkedHashSet<String>();
        int count = 0;
        for (String part : cleaned.split(",")) { if (!part.trim().isEmpty()) { count++; distinct.add(part.trim()); } }
        return count > 1 && distinct.size() < count;
    }

    static boolean missingMagicVisual(int style,String target,String graphic,String targetGraphic,String evidence){
        return style==2&&!"self".equals(target)&&"none".equals(graphic)&&"none".equals(targetGraphic)
                &&!"live-verified-animation-integrated".equals(evidence);
    }

    static boolean captureIsStale(boolean high,long requestedAt,long startedAt){
        return startedAt-requestedAt>(high?HIGH_CAPTURE_MAX_AGE_MS:NORMAL_CAPTURE_MAX_AGE_MS);
    }

    private static String state(Player player) {
        try {
            int bar = player.getNative950ActionBar() == null ? -1 : player.getNative950ActionBar().activeBar() + 1;
            boolean revo = player.getNative950ActionBar() != null && player.getNative950ActionBar().isRevolutionEnabled();
            int prayer = player.getPrayer() == null ? -1 : player.getPrayer().getPrayerpoints();
            int adrenaline = player.getCombatDefinitions() == null ? -1 : player.getCombatDefinitions().getSpecialAttackPercentage();
            String combat = player.getNative950Combat() == null ? "unavailable" : player.getNative950Combat().status();
            return "tile=" + player.getX() + "," + player.getY() + "," + player.getPlane()
                    + ";bar=" + bar + ";revolution=" + revo + ";prayer=" + prayer
                    + ";adrenaline=" + adrenaline + ";combat=" + combat;
        } catch (Throwable failure) { return "state-unavailable:" + failure.getClass().getSimpleName(); }
    }

    private static String value(Map<String, Object> fields, String key, String fallback) {
        Object value = fields.get(key); return value == null ? fallback : String.valueOf(value);
    }
    private static int integer(Object value, int fallback) { try { return Integer.parseInt(String.valueOf(value)); } catch (Exception ignored) { return fallback; } }
    private static long number(Object value, long fallback) { try { return Long.parseLong(String.valueOf(value)); } catch (Exception ignored) { return fallback; } }
    private static long tick() { try { return Utils.currentWorldCycle(); } catch (Throwable ignored) { return -1L; } }
    private static String stamp() { return new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date()); }
    private static String iso(long millis) { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).format(new Date(millis)); }
    private static String safe(String value) { return value == null ? "unknown" : value.replaceAll("[^A-Za-z0-9._-]", "_"); }
    private static File root() {
        String override=System.getProperty("ataraxia950.combatQaRoot","").trim();
        if(!override.isEmpty()){File result=new File(override);result.mkdirs();return result;}
        File result = new File(new File(projectRoot(), "logs"), "combatqa");
        result.mkdirs(); return result;
    }
    private static ThreadFactory daemon(final String name) { return new ThreadFactory() { public Thread newThread(Runnable run) { Thread t = new Thread(run, name); t.setDaemon(true); return t; } }; }

    private static String json(String category, String event, Map<String, Object> fields) {
        Map<String, Object> complete = new LinkedHashMap<String, Object>();
        complete.put("time", iso(System.currentTimeMillis())); complete.put("category", category); complete.put("event", event);
        complete.putAll(fields); return jsonObject(complete);
    }
    private static String jsonObject(Map<String, Object> fields) {
        StringBuilder out = new StringBuilder("{"); boolean first = true;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (!first) out.append(','); first = false;
            out.append('"').append(escape(entry.getKey())).append("\":").append(jsonValue(entry.getValue()));
        }
        return out.append('}').toString();
    }
    private static String jsonValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        if (value instanceof Iterable) {
            StringBuilder out = new StringBuilder("["); boolean first = true;
            for (Object item : (Iterable<?>) value) { if (!first) out.append(','); first = false; out.append(jsonValue(item)); }
            return out.append(']').toString();
        }
        return '"' + escape(String.valueOf(value)) + '"';
    }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }
    private static void append(File file, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
            try { writer.write(text); } finally { writer.close(); }
        } catch (Throwable ignored) { }
    }
    private static void writeDirect(File file, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            try { writer.write(text); } finally { writer.close(); }
        } catch (Throwable ignored) { }
    }
    private static String readFirst(File file) {
        if (!file.isFile()) return "";
        try { BufferedReader r = new BufferedReader(new FileReader(file)); try { String s = r.readLine(); return s == null ? "" : s.trim(); } finally { r.close(); } }
        catch (Exception ignored) { return ""; }
    }
    private static File projectRoot() {
        File cwd = new File(System.getProperty("user.dir")).getAbsoluteFile();
        for(File candidate=cwd;candidate!=null;candidate=candidate.getParentFile())
            if(new File(candidate,".git").exists())return candidate;
        return "OpenNXT".equalsIgnoreCase(cwd.getName())&&cwd.getParentFile()!=null?cwd.getParentFile():cwd;
    }
    private static String gitCommit() {
        try {
            File git = new File(projectRoot(), ".git"), head = new File(git, "HEAD"); String value = readFirst(head);
            if (value.startsWith("ref: ")) return readFirst(new File(git, value.substring(5).replace('/', File.separatorChar)));
            return value;
        } catch (Throwable ignored) { return "unknown"; }
    }
    private static File deployedJarFile() {
        String[] paths = System.getProperty("java.class.path", "").split(File.pathSeparator);
        for (String path : paths) { File file = new File(path); if (file.isFile() && path.toLowerCase(Locale.ROOT).endsWith(".jar") && path.toLowerCase(Locale.ROOT).contains("ataraxia")) return file; }
        File[] files = new File(projectRoot(), "OpenNXT" + File.separator + "runtime" + File.separator + "lib").listFiles();
        if (files != null) for (File file : files) if (file.getName().toLowerCase(Locale.ROOT).startsWith("ataraxia-") && file.getName().endsWith(".jar")) return file;
        return null;
    }
    private static String deployedJar() { File file = deployedJarFile(); return file == null ? "unknown" : file.getAbsolutePath(); }
    private static String deployedJarSha() {
        File file = deployedJarFile(); if (file == null) return "unknown";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); InputStream input = new FileInputStream(file);
            try { byte[] buffer = new byte[32768]; int read; while ((read = input.read(buffer)) >= 0) digest.update(buffer, 0, read); }
            finally { input.close(); }
            StringBuilder out = new StringBuilder(); for (byte b : digest.digest()) out.append(String.format("%02x", b & 255)); return out.toString();
        } catch (Throwable ignored) { return "unavailable"; }
    }
}
