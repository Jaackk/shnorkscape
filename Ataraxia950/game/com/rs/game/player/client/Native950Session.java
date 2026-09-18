package com.rs.game.player.client;

import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.Native950Actions.WalkRequest;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.Utils;
import io.netty.channel.Channel;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * A modern connection whose authoritative character is an Ataraxia Player.
 *
 * <p>P4/P6: the tick is split into the four world phases {@code Native950World}
 * drives across every session ({@code pumpSchedulers()} has already run the
 * ServiceProvider wheel and WorldTasksManager at the top of the world tick):
 * {@link #tickInput()} drains client input, {@link #tickMove()} runs
 * {@code Player.processEntity()} (managers, timers, controllers) and
 * {@code Player.processEntityUpdate()} (movement, hits, damage) plus the bypass
 * interactions, {@link #tickFrame} takes the sectioned checkpoint and hands the
 * post-movement world to the installed {@link Native950Frames} encoder, and
 * {@link #tickEnd()} resets the masks ({@code Player.resetMasks()}, the Entity
 * path for native players) and flushes the tickEnd packet. Movement is processed
 * exactly once, inside processEntityUpdate. Splitting the phases is what lets a
 * second character exist: every entity has moved before any viewer is encoded.
 *
 * <p>The appearance body comes from {@code GlobalPlayerUpdater}'s native branch
 * (the same bytes {@code Native950Appearance} produced for the verified bronze
 * set, now for any decodable worn item). The session regenerates it when the worn
 * set changes; deciding whether a given viewer still needs it is the encoder's
 * per-viewer MD5 cache.
 */
public final class Native950Session {
    private final Player player;
    private final int playerIndex;
    private final Channel channel;
    private final Native950GameTransport transport;
    private final Native950World.SceneConfig scene;
    private final Native950Interactions interactions;
    private final Native950NpcView npcView;
    private final Native950SaveStore saveStore;
    private final Native950RegionMusic music;
    private final Native950GroundItemsView groundItems;
    private final Native950ObjectsView objects;
    private int[] displayedEquipment;

    private Native950Frames frames;
    private Native950Save lastSaved;
    private boolean ready;
    private boolean closed;
    private boolean rebuiltThisTick;
    private int publishedAreaType;
    private int preMoveX, preMoveY, preMovePlane;
    private long sessionStarted;
    private long ticks;
    private long steps;
    private long rejectedWalks;
    private long sceneRebuilds;
    /** Appearance bodies regenerated because the worn set changed; the encoder decides who gets them. */
    private long appearanceUpdates;
    private long appearanceFallbacks;
    private long actionsDrained;
    private long checkpoints;
    private final EnumMap<Native950Save.Section, Long> sectionWrites = new EnumMap<Native950Save.Section, Long>(Native950Save.Section.class);

    Native950Session(Player player, Channel channel, Native950GameTransport transport,
                     Native950World.SceneConfig scene, Native950Content content,
                     Native950SaveStore saveStore, Native950Save saved,
                     com.rs.game.npc.NPC banker) {
        this.player = player;
        this.playerIndex = player.getIndex();
        this.channel = channel;
        this.groundItems = new Native950GroundItemsView(Thread.currentThread(), packet -> channel.write(packet));
        this.objects = new Native950ObjectsView(Thread.currentThread(), packet -> channel.write(packet));
        this.transport = transport;
        this.scene = scene;
        this.publishedAreaType=Native950MapAreas.areaTypeFor(player.getX(),player.getY(),scene.areaType);
        this.saveStore = saveStore;
        this.lastSaved = saved;
        Native950RegionMusicCatalog musicCatalog = new Native950RegionMusicCatalog();
        this.music = new Native950RegionMusic(musicCatalog::lookup,
                archiveId -> channel.write(Native950Packets.music(archiveId, 255)));
        this.npcView = content == null || content.banker == null || banker == null ? null
                : new Native950NpcView(content.banker, banker);
        this.interactions = content == null ? null : new Native950Interactions(player, channel, content, saved, npcView);
        if (this.interactions != null) this.interactions.attachGroundItems(groundItems);
        // Schema-3 sections (skills, vitals, settings, look, identity) enter the real
        // Player here, before the initial appearance body is built from it.
        if (saved != null) Native950PlayerBinder.restore(player, saved);
        this.displayedEquipment = interactions == null ? null : interactions.equipmentSnapshot().ids;
        this.preMoveX = player.getX();
        this.preMoveY = player.getY();
        this.preMovePlane = player.getPlane();
    }

    void ready(Native950Frames encoder) {
        if (closed || ready || !channel.isActive()) return;
        this.frames = encoder;

        boolean returning = lastSaved != null;
        sessionStarted = Utils.currentTimeMillis();
        player.setLastPacketReceivedTime(sessionStarted);
        checkpoint(); // Commit starter supplies exactly once, before exposing the backpack.
        ready = true;
        player.setRunning(true);
        player.setActive(true);
        // Legacy gates (sendMessage, pin, friends) read hasCompleted(); the hint-icon
        // removal it triggers is a counted NO-OP in the facade.
        player.setCompleted();
        if (interactions != null) interactions.bootstrap();
        // M3: the stat and vital burst runs AFTER the UI bootstrap has attached the
        // panels, because the 947 skills tab has no onLoad hook and is built by its
        // onStatTransmit handler (verified/ui/SKILLS_TAB.md section 2), and the
        // action-bar bars redraw from their varp/varbit transmit hooks.
        sendNative950LoginState();
        music.start(player.getRegionId());
        if (saveStore != null) channel.write(Native950Packets.gameMessage(0,
                returning ? "Your local progress has been loaded." : "Local profile created. Your progress saves automatically."));
    }

    /**
     * Pushes the whole M3 surface once, from the real 910 player state, through the
     * facade (so every id is resolved against the binding table and an unbound one
     * is a counted drop, never a wire write).
     *
     * <ul>
     * <li>{@code Skills.init()} - one UPDATE_STAT per modelled stat plus virtual
     *     levels (varbit 19007) and summoning points (varbit 41524). "Modelled"
     *     is now the 947 cache's 29 stats, so Archaeology (27) and Necromancy (28)
     *     are in the burst like any other stat rather than being left at the
     *     client's stat-table initialiser defaults; the count follows the engine
     *     table, and the facade still turns an id the binding table does not
     *     declare into a counted drop.</li>
     * <li>{@code refreshHitPoints} - varbit 1668, {@code hitpoints * 10}.</li>
     * <li>{@code Prayer.refreshPrayerPoints} - varbit 16736, points x 10.</li>
     * <li>{@code CombatDefinitions.refreshSpecialAttackPercentage} - varp 679
     *     (adrenaline, 0..1000) and {@code refreshAutoRelatie} - varp 462, where
     *     1 means OFF.</li>
     * <li>{@code sendRunEnergy} - UPDATE_RUNENERGY, and {@code sendRunButtonConfig}
     *     - varp 463 (0 walk / 1 run / 3 rest), the var the run orb's script chain
     *     1316 -> varc 119 -> 1741 follows.</li>
     * </ul>
     *
     * <p>Everything after this point is driven by the ordinary 910 code on the
     * native tick: {@code World.addRestoreHitPointsTask} and {@code heal} call
     * {@code refreshHitPoints}, {@code World.addDrainPrayerTask} calls
     * {@code Prayer.drainPrayer}, {@code Player.processEntity} calls
     * {@code restoreRunEnergy} and {@code Entity.processMovement} calls
     * {@code drainRunEnergy}, each of which already refreshes its own var.
     */
    private void sendNative950LoginState() {
        player.getSkills().init();
        Native950XpDrops.open(player);
        player.refreshHitPoints();
        if (player.getPrayer() != null) player.getPrayer().refreshPrayerPoints();
        player.getCombatDefinitions().refreshSpecialAttackPercentage();
        player.getCombatDefinitions().refreshAutoRelatie();
        player.getPackets().sendRunEnergy();
        player.sendRunButtonConfig();
    }

    /** World phase 1a: drain this connection's verified client actions. */
    void tickInput() {
        if (!ready || closed || !channel.isActive() || !player.isActive()) return;
        if (interactions != null) interactions.beginTick();
        int drained = transport.drainActions(action -> {
            // Logout marks inactivity before asynchronous close. Reject later actions in this batch.
            if (closed || !channel.isActive() || !player.isActive()) return;
            if (action instanceof Native950Actions.MusicEndedAction) {
                music.requestReplay(((Native950Actions.MusicEndedAction) action).archiveId());
            } else if (action instanceof WalkRequest) {
                route((WalkRequest) action);
            } else if (interactions != null) interactions.handle(action);
        });
        if (drained > 0) {
            actionsDrained += drained;
            player.setLastPacketReceivedTime(Utils.currentTimeMillis());
        }
    }

    /**
     * World phase 1b: this character's authoritative movement. Runs for every session
     * before any viewer's frame is built, so a frame never describes a world in which
     * some characters have moved and others have not.
     */
    void tickMove() {
        if (!ready || closed || !channel.isActive() || !player.isActive()) return;
        preMoveX = player.getX();
        preMoveY = player.getY();
        preMovePlane = player.getPlane();
        // WorldThread phase 1: managers, timers, controllers. In strict mode a caught
        // tick exception is rethrown (Player.processEntity) and the world closes the
        // session; in lenient mode it is counted in getNativeTickFailures().
        Native950Dungeoneering.tick(player);
        player.processEntity();
        // WorldThread phase 2: the authoritative Entity movement (walk queue +
        // collision recheck), received hits and damage. Movement runs only here.
        player.processEntityUpdate();
        if (interactions != null) interactions.afterMovement();
        music.update(player.getRegionId());
    }

    /**
     * World phase 2: the checkpoint and this viewer's output frames, built from the
     * post-movement position of every entity in {@code characters} and {@code roster}.
     */
    void tickFrame(Native950Frames encoder, java.util.List<com.rs.game.npc.NPC> roster,
                   java.util.List<Player> characters) {
        if (!ready || closed || !channel.isActive() || !player.isActive()) return;
        this.frames = encoder;
        // This development world saves only changed sections. A failed atomic write
        // closes the session before this tick's output is flushed; reconnect resumes
        // the last successfully committed tick.
        checkpoint();
        Native950DevelopmentCommands.advanceAreaSweep(player);
        int areaType=Native950DevelopmentCommands.resolveAreaType(player,
                Native950MapAreas.areaTypeFor(player.getX(),player.getY(),scene.areaType));
        // A change of cache area can occur at an8-tile boundary inside the existing scene.
        if(areaType!=publishedAreaType)player.loadMapRegions();
        rebuiltThisTick = player.consumeNative950MapRefresh();
        if (rebuiltThisTick) {
            groundItems.beforeRebuild();
            objects.beforeRebuild();
            // The area must match where the player now IS, not where the session started: the
            // client refuses to build map squares the declared area does not cover, and only
            // re-reads the area when the value changes.
            channel.write(Native950Packets.rebuildScene(player.getChunkX(), player.getChunkY(),
                    scene.npcBits, areaType, scene.hash1, scene.hash2));
            publishedAreaType=areaType;
            sceneRebuilds++;
        }
        groundItems.refresh(player);
        objects.refresh(player);
        if(rebuiltThisTick||ticks%10==0)Native950Farming.refresh(player);
        refreshAppearanceIfEquipmentChanged();

        encoder.encode(new Native950Frames.Frame(player, channel, characters, roster, npcView,
                rebuiltThisTick, scene.npcBits, preMoveX, preMoveY, preMovePlane));
        if (player.getX() != preMoveX || player.getY() != preMoveY) {
            steps++;
            System.out.println("[Ataraxia950] Player " + player.getIndex() + " moved to "
                    + player.getX() + "," + player.getY() + "," + player.getPlane()
                    + " using Ataraxia collision and movement");
        }
    }

    /**
     * World phase 3: masks reset after every viewer's frame has read them; the Player
     * override skips refreshSpawnedObjects/Items for native players. The tickEnd packet
     * and the flush stay owned by the session.
     */
    void tickEnd() {
        if (!ready || closed || !channel.isActive() || !player.isActive()) return;
        player.resetMasks();
        if (rebuiltThisTick) player.setClientHasLoadedMapRegion();
        rebuiltThisTick = false;
        if (++ticks % 10 == 0) channel.write(Native950Packets.keepAlive());
        channel.writeAndFlush(Native950Packets.tickEnd());
    }

    /**
     * The bypass mutates the Equipment container directly (no generateAppearenceData
     * call), so a changed worn set regenerates the body here; any other regeneration
     * (Skills.refresh, Equipment.refresh through the facade) is picked up by the
     * encoder's per-viewer MD5 comparison.
     */
    private void refreshAppearanceIfEquipmentChanged() {
        if (interactions == null) return;
        int[] equipped = interactions.equipmentSnapshot().ids;
        if (Arrays.equals(displayedEquipment, equipped)) return;
        displayedEquipment = equipped;
        player.getAppearence().generateAppearenceData();
        appearanceUpdates++;
    }

    private void route(WalkRequest request) {
        // The shared Ataraxia pathfinder covers 128x128 tiles around the player.
        // Modifier flags cannot authorize teleportation or change movement speed.
        if ((interactions!=null&&interactions.blocksWorldInput()) || !player.isActive() || player.hasFinished() || player.isDead()
                || player.isLocked() || player.isNative950ForceMovementActive()
                || Math.abs(request.x() - player.getX()) > 48
                || Math.abs(request.y() - player.getY()) > 48) {
            rejectedWalks++;
            return;
        }
        if (interactions != null) interactions.walking();
        player.resetWalkSteps();
        int count = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,
                player.getX(), player.getY(), player.getPlane(), player.getSize(),
                new FixedTileStrategy(request.x(), request.y()), true);
        if (count < 0) {
            rejectedWalks++;
            return;
        }
        int[] pathX = RouteFinder.getLastPathBufferX();
        int[] pathY = RouteFinder.getLastPathBufferY();
        int remaining = 128;
        for (int step = count - 1; step >= 0 && remaining > 0; step--) {
            if (!player.addWalkSteps(pathX[step], pathY[step], remaining, true)) break;
            remaining = 128 - player.getWalkSteps().size();
        }
    }

    void close() {
        if (closed) return;
        Native950BugTest.close(player,"session-close");
        closed = true;
        ready = false;
        music.close();
        try {
            try {
                Native950Dungeoneering.onLogout(player);
                if (interactions != null) checkpoint();
            } finally {
                // Failed persistence must not leave actions or component stores attached.
                if (interactions != null) interactions.close();
            }
        } catch (RuntimeException failure) {
            System.out.println("[Ataraxia950] Native session cleanup/save failed: " + failure);
        } finally {
            try {
                if (npcView != null) npcView.close();
            } finally {
                World.removeNative950Player(player);
            }
        }
    }

    boolean isClosed() { return closed; }

    Player player() { return player; }

    /** The slot reserved before the login response was written; stable across close. */
    int playerIndex() { return playerIndex; }

    /**
     * Sectioned checkpoint: containers and position from the bypass, the remaining
     * binder sections from the Player; nothing is written when no section changed.
     */
    private void checkpoint() {
        if (saveStore == null) return;
        Native950Save next = Native950PlayerBinder.capture(player, interactions.saveSnapshot(), sessionStarted);
        EnumSet<Native950Save.Section> changed = next.changedSections(lastSaved);
        if (changed.isEmpty()) return;
        try {
            saveStore.save(next);
            lastSaved = next;
            checkpoints++;
            for (Native950Save.Section section : changed) {
                Long count = sectionWrites.get(section);
                sectionWrites.put(section, count == null ? 1L : count + 1);
            }
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException("Could not save local 950 progress for " + player.getUsername(), failure);
        }
    }

    /**
     * Body for the login PLAYER_INFO frame: the GlobalPlayerUpdater native body
     * built from the restored containers and look; {@code fallback} (the handoff's
     * bytes) only when the body was withheld (counted in the snapshot).
     */
    byte[] initialAppearance(byte[] fallback) {
        player.getAppearence().generateAppearenceData();
        byte[] body = player.getAppearence().getAppeareanceData();
        byte[] hash = player.getAppearence().getMD5AppeareanceDataHash();
        if (body == null || hash == null) {
            appearanceFallbacks++;

            return fallback;
        }
        if (hash.length == 0) return fallback;
        return body;
    }

    Snapshot snapshot() {
        Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
        Native950TickScheduler scheduler = CoresManager.getNative950Scheduler();
        Native950Bootstrap.Report report = Native950Bootstrap.lastReport();
        return new Snapshot(player.getUsername(), player.getIndex(), player.getX(), player.getY(),
                player.getPlane(), ready && !closed, ticks, steps, rejectedWalks, sceneRebuilds,
                transport.unhandledFrameCount(), transport.lastUnhandledOpcode(), interactions == null ? null : interactions.snapshot(),
                npcView == null ? null : npcView.snapshot(),
                counters.totalSent(), counters.totalNoops(), counters.totalDropped(), counters.totalStrictHits(),
                counters.strictHitsByMethod(), counters.droppedByMethod(), counters.sentByMethod(), counters.noopsByMethod(),
                counters.noopIdsByMethod(),
                player.getNativeTickFailures(), player.getNativeDeferredRefreshes(),
                player.getNativeProcessEntityRuns(),
                scheduler == null ? -1 : scheduler.failed(), scheduler == null ? -1 : scheduler.inlineGets(),
                scheduler == null ? -1 : scheduler.pendingCount(),
                player.getVarsManager().nativeUnsentVarps(),
                player.getAppearence().getNative950WithheldBodies(),
                appearanceUpdates, appearanceFallbacks,
                actionsDrained, checkpoints, new EnumMap<Native950Save.Section, Long>(sectionWrites),
                report == null ? null : Boolean.valueOf(report.allOk()),
                player.getSkills().getLevelsCopy(), player.getSkills().getXpCopy(), player.getHitpoints(),
                player.getPrayer() == null ? 0 : player.getPrayer().getPrayerpoints(),
                player.getRunEnergy(), player.getRun(),
                frames == null ? null : frames.viewport(player),
                music.regionId(), music.archiveId(), music.trackId(), music.trackName(), music.changes());
    }

    Channel channel() { return channel; }

    /** Immutable diagnostic view; callers never receive the mutable Player. */
    public static final class Snapshot {
        public final String username;
        public final int playerIndex, x, y, plane;
        public final boolean active;
        public final long ticks, steps, rejectedWalks, sceneRebuilds, unhandledFrames;
        public final int lastUnhandledOpcode;
        public final Native950Interactions.State interactions;
        final Native950NpcView.State npc;
        /** Facade tier totals (P1 counters) at snapshot time. */
        public final long facadeSent, facadeNoops, facadeDropped, facadeStrictHits;
        public final Map<String, Long> strictHitsByMethod, droppedByMethod, sentByMethod, noopsByMethod;
        /**
         * Ids a counted NO-OP discarded, per method (varc ids from
         * {@code sendGlobalConfig*}/{@code sendCSVar*}, skill ids from
         * {@code sendSkillLevel}). This is the evidence M2b acceptance (c) needs:
         * without it the NO-OP tier would swallow unverified ids with no record.
         */
        public final Map<String, Map<Integer, Long>> noopIdsByMethod;
        /** Player.processEntity exceptions and refreshes deferred for lack of a verified packet. */
        public final long tickFailures, deferredRefreshes;
        /**
         * Player.processEntity entries for this session's player. Equals {@link #ticks}
         * on a healthy native session: tick() calls processEntity unconditionally and
         * increments ticks at its tail, so a shortfall means a tick died mid-body.
         */
        public final long processEntityRuns;
        /** Native950TickScheduler counters, -1 when no wheel is installed. */
        public final long schedulerFailed, schedulerInlineGets;
        public final int schedulerPending;
        /** VarsManager writes cached before a varp sink was installed. */
        public final long varpsUnsent;
        /** Appearance bodies withheld (unverified item), resent (hash changed) and login fallbacks. */
        public final long appearanceWithheld, appearanceUpdates, appearanceFallbacks;
        public final long actionsDrained, checkpoints;
        public final Map<Native950Save.Section, Long> sectionWrites;
        /** Null until Native950Bootstrap ran in this JVM. */
        public final Boolean bootstrapOk;
        /** Current (possibly boosted or drained) levels, exactly as UPDATE_STAT carries them. */
        public final short[] skillLevels;
        /** Experience per modelled skill; the value UPDATE_STAT transmits x10. */
        public final double[] skillXp;
        /** M3 vitals: hitpoints (level x10 units), prayer points (tenths of the bar), energy and run state. */
        public final int hitpoints, prayerPoints, runEnergy;
        public final boolean running;
        /** P6 PLAYER_INFO viewport counters for this viewer; null before the first frame. */
        public final Native950Viewport.State viewport;
        /** Current regional music selection; -1 denotes an unmapped/silent region. */
        public final int musicRegionId, musicArchiveId, musicTrackId;
        public final String musicTrackName;
        public final long musicChanges;

        Snapshot(String username, int playerIndex, int x, int y, int plane, boolean active,
                 long ticks, long steps, long rejectedWalks, long sceneRebuilds,
                 long unhandledFrames, int lastUnhandledOpcode, Native950Interactions.State interactions, Native950NpcView.State npc,
                 long facadeSent, long facadeNoops, long facadeDropped, long facadeStrictHits,
                 Map<String, Long> strictHitsByMethod, Map<String, Long> droppedByMethod,
                 Map<String, Long> sentByMethod, Map<String, Long> noopsByMethod,
                 Map<String, Map<Integer, Long>> noopIdsByMethod,
                 long tickFailures, long deferredRefreshes, long processEntityRuns,
                 long schedulerFailed, long schedulerInlineGets, int schedulerPending,
                 long varpsUnsent, long appearanceWithheld, long appearanceUpdates, long appearanceFallbacks,
                 long actionsDrained, long checkpoints, Map<Native950Save.Section, Long> sectionWrites, Boolean bootstrapOk,
                 short[] skillLevels, double[] skillXp, int hitpoints, int prayerPoints, int runEnergy, boolean running,
                 Native950Viewport.State viewport,
                 int musicRegionId, int musicArchiveId, int musicTrackId, String musicTrackName, long musicChanges) {
            this.username = username; this.playerIndex = playerIndex;
            this.x = x; this.y = y; this.plane = plane; this.active = active;
            this.ticks = ticks; this.steps = steps; this.rejectedWalks = rejectedWalks;
            this.sceneRebuilds = sceneRebuilds; this.unhandledFrames = unhandledFrames;
            this.lastUnhandledOpcode = lastUnhandledOpcode;
            this.interactions = interactions;
            this.npc = npc;
            this.facadeSent = facadeSent; this.facadeNoops = facadeNoops;
            this.facadeDropped = facadeDropped; this.facadeStrictHits = facadeStrictHits;
            this.strictHitsByMethod = Collections.unmodifiableMap(strictHitsByMethod);
            this.droppedByMethod = Collections.unmodifiableMap(droppedByMethod);
            this.sentByMethod = Collections.unmodifiableMap(sentByMethod);
            this.noopsByMethod = Collections.unmodifiableMap(noopsByMethod);
            this.noopIdsByMethod = Collections.unmodifiableMap(noopIdsByMethod);
            this.tickFailures = tickFailures; this.deferredRefreshes = deferredRefreshes;
            this.processEntityRuns = processEntityRuns;
            this.schedulerFailed = schedulerFailed; this.schedulerInlineGets = schedulerInlineGets;
            this.schedulerPending = schedulerPending;
            this.varpsUnsent = varpsUnsent;
            this.appearanceWithheld = appearanceWithheld; this.appearanceUpdates = appearanceUpdates;
            this.appearanceFallbacks = appearanceFallbacks;
            this.actionsDrained = actionsDrained; this.checkpoints = checkpoints;
            this.sectionWrites = Collections.unmodifiableMap(sectionWrites);
            this.bootstrapOk = bootstrapOk;
            this.skillLevels = skillLevels.clone();
            this.skillXp = skillXp.clone();
            this.hitpoints = hitpoints; this.prayerPoints = prayerPoints;
            this.runEnergy = runEnergy; this.running = running;
            this.viewport = viewport;
            this.musicRegionId = musicRegionId;
            this.musicArchiveId = musicArchiveId;
            this.musicTrackId = musicTrackId;
            this.musicTrackName = musicTrackName;
            this.musicChanges = musicChanges;
        }
        @Override public String toString() {
            return "Ataraxia Player[index=" + playerIndex + ",tile=" + x + "," + y + "," + plane
                    + ",active=" + active + ",ticks=" + ticks + ",steps=" + steps
                    + ",rebuilds=" + sceneRebuilds + ",rejectedWalks=" + rejectedWalks
                    + ",processEntityRuns=" + processEntityRuns
                    + ",tickFailures=" + tickFailures + ",strictHits=" + facadeStrictHits
                    + ",facade(sent=" + facadeSent + sentByMethod + ",noops=" + facadeNoops + ",dropped=" + facadeDropped + droppedByMethod + ")"
                    + ",scheduler(failed=" + schedulerFailed + ",inlineGets=" + schedulerInlineGets + ",pending=" + schedulerPending + ")"
                    + ",appearance(updates=" + appearanceUpdates + ",withheld=" + appearanceWithheld + ")"
                    + ",checkpoints=" + checkpoints + ",bootstrapOk=" + bootstrapOk
                    + ",music(region=" + musicRegionId + ",track=" + musicTrackId + ":" + musicTrackName
                    + ",archive=" + musicArchiveId + ",changes=" + musicChanges + ")"
                    + "," + (viewport == null ? "viewport=none" : viewport.toString())
                    + "," + (npc == null ? "npc=none" : npc.toString()) + "]";
        }
    }
}
