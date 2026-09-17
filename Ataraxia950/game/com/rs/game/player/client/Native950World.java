package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import com.rs.cores.WorldThread;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
 * Opt-in modern Ataraxia world. OpenNXT may authenticate and serve cache assets,
 * but only this thread creates/mutates the Players and runs their movement loop.
 * The legacy ServerLauncher and WorldThread must not also be started in this JVM.
 *
 * <p>P6 turned the single 'occupied' flag into an N-slot table. A player index is
 * reserved by {@link #reserve(String)} <em>before</em> the login response is written,
 * because {@code Ataraxia947Handoff} puts that index into {@code GameLoginResponse}
 * while the Player does not yet exist; {@link #release(int)} returns an unclaimed
 * reservation on every failure branch, since a leaked slot is permanent capacity loss.
 * How many slots exist at all is decided by the installed {@link Native950Frames}, so no
 * separate multiplayer gate exists that could drift from what the wire supports.
 *
 * <p>The tick is world-phased ({@link #worldTick()}): every entity moves, then every viewer's
 * frame is built, then every mask is reset. Interleaving those per session was correct while
 * the world held one character and is wrong with two.
 */
public final class Native950World {
    public static final String TRANSPORT_NAME = "ataraxia-native947-game";
    /** PLAYER_INFO addresses slots 1..2047; the same range EntityList hands out here. */
    public static final int MAX_PLAYER_INDEX = World.NATIVE_947_MAX_PLAYER_INDEX;
    private static final long TICK_NANOS = TimeUnit.MILLISECONDS.toNanos(600);
    private final ArrayBlockingQueue<Runnable> commands = new ArrayBlockingQueue<Runnable>(128);
    /** Guards {@link #slots}; taken by login threads (reserve) and the world thread (claim/free). */
    private final Object admission = new Object();
    private final Slot[] slots = new Slot[MAX_PLAYER_INDEX + 1];
    /** World-owned native NPCs; the roster every session's view reads. World thread only. */
    private final List<NPC> npcs = new ArrayList<NPC>();
    private final Thread thread;
    private final Native950MeleeCombat combat;
    private volatile Native950Frames frames = new Native950EntityFrames();

    /** A reserved, and later claimed, player index. */
    private static final class Slot {
        final String username;
        Native950Session session;
        Slot(String username) { this.username = username; }
    }

    private static final class Holder {
        private static final Native950World INSTANCE = new Native950World();
    }

    public static Native950World getInstance() { return Holder.INSTANCE; }

    private Native950World() {
        // The 910 content calls CoresManager.getServiceProvider() from 337 sites; in this JVM
        // that provider is a tick wheel drained by this thread (see Native950TickScheduler).
        thread = new Thread(this::run, "Ataraxia-950-world");
        thread.setDaemon(true);
        combat = new Native950MeleeCombat(thread);
        bindTickWheel(thread);
        thread.start();
        // M4: data-driven spawning is opt-in. Default off, because every real-cache smoke
        // reasons about the exact NPC set in the world; the live server passes
        // -Dataraxia947.npcSpawns=true.
        if (Boolean.getBoolean(SPAWNS_PROPERTY) || Boolean.getBoolean(LEGACY_SPAWNS_PROPERTY))
            setDataSpawnsEnabled(true);
    }

    /**
     * Installs the tick wheel if none exists and binds it to the world thread BEFORE that
     * thread starts, so ownership never depends on which thread drains first. Fails closed
     * (the world never starts) when the provider already present is not a native wheel, or
     * when the wheel has already been drained by another thread: adopting either silently
     * would make every world tick throw and evict any attached session forever.
     */
    private static void bindTickWheel(Thread worldThread) {
        if (CoresManager.getServiceProvider() == null) CoresManager.initNative950(new Native950TickScheduler());
        Native950TickScheduler wheel = CoresManager.getNative950Scheduler();
        if (wheel == null)
            throw new IllegalStateException("CoresManager holds a legacy pool-backed ServiceProvider; the native 947 world cannot share this JVM");
        wheel.bindOwner(worldThread); // throws when another thread already owns the wheel
    }

    // ------------------------------------------------------------------ frame encoder

    /** The encoder that owns every PLAYER_INFO / NPC_INFO bit and the world's capacity. */
    public Native950Frames frames() { return frames; }

    /**
     * Installs the generalised entity encoder. Refused while any session is attached,
     * because a viewer's viewport state lives inside the encoder and swapping it under a
     * live connection would leave the client holding entities the new encoder never added.
     */
    public void installFrameEncoder(Native950Frames encoder) {
        Objects.requireNonNull(encoder, "encoder");
        if (encoder.playerCapacity() < 1 || encoder.playerCapacity() > MAX_PLAYER_INDEX)
            throw new IllegalArgumentException("A native 947 frame encoder must support 1.." + MAX_PLAYER_INDEX + " local players");
        if (encoder.npcCapacity() < 0)
            throw new IllegalArgumentException("A native 947 frame encoder cannot have a negative NPC capacity");
        synchronized (admission) {
            for (int index = 1; index <= MAX_PLAYER_INDEX; index++)
                if (slots[index] != null)
                    throw new IllegalStateException("The frame encoder cannot change while a character is in the world");
            frames = encoder;
        }
    }

    // ------------------------------------------------------------------ admission

    /** Advisory admission check: true while the installed encoder still has a free slot. */
    public boolean canReserve() {
        synchronized (admission) { return used() < capacity(); }
    }

    /** Reserved (pending) plus attached characters. */
    public int reservedSlots() {
        synchronized (admission) { return used(); }
    }

    /** How many characters the installed encoder can describe at once. */
    public int capacity() {
        return Math.min(Math.max(frames.playerCapacity(), 1), MAX_PLAYER_INDEX);
    }

    private int used() {
        int used = 0;
        for (int index = 1; index <= MAX_PLAYER_INDEX; index++) if (slots[index] != null) used++;
        return used;
    }

    /**
     * Allocates the player index that {@code GameLoginResponse} must carry, before the
     * Player exists. Returns 0 when the world is full <em>or when this account already
     * owns a slot</em> - one account may hold exactly one character, because two sessions
     * would checkpoint the same save profile independently. The caller MUST either pass the
     * index into {@link SceneConfig} and then {@code attach}, or {@link #release(int)} it;
     * the reservation is not time-limited, so every failure branch of the login handoff has
     * to release it.
     */
    public int reserve(String username) {
        Objects.requireNonNull(username, "username");
        synchronized (admission) {
            if (holdsUsername(username)) return 0;
            if (used() >= capacity()) return 0;
            for (int index = 1; index <= MAX_PLAYER_INDEX; index++)
                if (slots[index] == null) { slots[index] = new Slot(username); return index; }
            return 0;
        }
    }

    /**
     * True while the world already holds a reservation or an attached session for this account.
     * The login handoff asks before it writes a response, so a duplicate login is refused with
     * {@code LOGGED_IN} rather than admitted into a second slot.
     *
     * <p>Two slots for one account is not merely untidy. Each session loads the same profile by
     * username and checkpoints it independently, so two whole-profile writers interleave over
     * one save file, and {@code World.playerMap} holds a single entry per username, which the
     * first departure would remove out from under the survivor.
     */
    public boolean isOnline(String username) {
        Objects.requireNonNull(username, "username");
        synchronized (admission) { return holdsUsername(username); }
    }

    /** Caller holds {@link #admission}. Compares the way the save store canonicalises names. */
    private boolean holdsUsername(String username) {
        String canonical = canonical(username);
        for (int index = 1; index <= MAX_PLAYER_INDEX; index++) {
            Slot slot = slots[index];
            if (slot != null && canonical.equals(canonical(slot.username))) return true;
        }
        return false;
    }

    /**
     * The name two logins are considered the same account under. {@code Native950Save} decides
     * which profile file a session writes, so "Bob" and "bob" must not be allowed to attach as
     * two characters over one save. A name the save store refuses is compared literally, which
     * is stricter than not comparing it at all.
     */
    private static String canonical(String username) {
        try {
            return Native950Save.canonicalUsername(username);
        } catch (RuntimeException notAProfileName) {
            return username;
        }
    }

    /**
     * Returns a reservation that never became a session. A slot already claimed by an
     * attached session is left alone: that session owns it until it closes, so this is
     * safe to call unconditionally from a login failure path.
     *
     * <p>This form does not check who the reservation belongs to, so it is only safe for
     * a caller that is the sole owner of {@code index} for the whole call - a single
     * threaded test or smoke, which is all that still calls it. Every path inside this
     * class uses {@link #release(int, String)}, because by the time a late failure branch
     * runs the slot may already hold another connection's fresh reservation, and
     * {@code reserve} runs on Netty login threads.
     */
    public void release(int index) {
        synchronized (admission) {
            if (index < 1 || index > MAX_PLAYER_INDEX) return;
            Slot slot = slots[index];
            if (slot != null && slot.session == null) slots[index] = null;
        }
    }

    /**
     * Returns a reservation only while it is still the one {@code username} took. A slot
     * that is claimed, already gone, or reserved by a later login for somebody else is
     * left exactly as it is, so a login failure branch that fires after the world already
     * freed the slot cannot hand a concurrent login's reservation away.
     */
    public void release(int index, String username) {
        Objects.requireNonNull(username, "username");
        synchronized (admission) {
            if (index < 1 || index > MAX_PLAYER_INDEX) return;
            Slot slot = slots[index];
            if (slot != null && slot.session == null && slot.username.equals(username)) slots[index] = null;
        }
    }

    /** Claims a reserved slot for a session, or takes a free one when the caller did not pre-reserve. */
    private boolean claim(int index, String username, Native950Session session) {
        synchronized (admission) {
            Slot slot = slots[index];
            if (slot == null) {
                if (used() >= capacity() || holdsUsername(username)) return false;
                slot = new Slot(username);
                slots[index] = slot;
            } else if (slot.session != null || !slot.username.equals(username)) return false;
            slot.session = session;
            return true;
        }
    }

    private void freeSlot(int index, Native950Session session) {
        synchronized (admission) {
            if (index < 1 || index > MAX_PLAYER_INDEX) return;
            Slot slot = slots[index];
            if (slot != null && slot.session == session) slots[index] = null;
        }
    }

    /** World-thread view of the attached sessions, ordered by player index. */
    private List<Native950Session> sessions() {
        List<Native950Session> live = new ArrayList<Native950Session>();
        synchronized (admission) {
            for (int index = 1; index <= MAX_PLAYER_INDEX; index++)
                if (slots[index] != null && slots[index].session != null) live.add(slots[index].session);
        }
        return live;
    }

    /** {@code -Dataraxia950.bootstrap=lenient} admits players although a bootstrap entry failed. */
    public static final String BOOTSTRAP_PROPERTY = "ataraxia950.bootstrap";

    /** The pre-port spelling, still honoured so an existing -D flag is not silently ignored. */
    public static final String LEGACY_BOOTSTRAP_PROPERTY = "ataraxia947.bootstrap";

    /**
     * Runs {@link Native950Bootstrap} exactly once on the world thread, the first
     * time a player is admitted after the flat cache exists (the Kotlin handoff
     * initialises the cache before attach). A failed entry refuses admission with
     * the report's failure list unless the JVM was started lenient.
     */
    static Native950Bootstrap.Report ensureBootstrap() {
        Native950Bootstrap.Report report = Native950Bootstrap.lastReport();
        if (report == null) {
            report = Native950Bootstrap.run();
            System.out.println("[Ataraxia950] Bootstrap ran during first admission: ok=" + report.count(Native950Bootstrap.Status.OK)
                    + " failed=" + report.count(Native950Bootstrap.Status.FAILED)
                    + " skipped=" + report.count(Native950Bootstrap.Status.SKIPPED) + " in " + report.totalMillis + " ms");
        }
        if (!report.allOk() && !"lenient".equalsIgnoreCase(System.getProperty(BOOTSTRAP_PROPERTY, "strict"))) {
            StringBuilder reasons = new StringBuilder();
            for (Native950Bootstrap.Entry failure : report.failures())
                reasons.append(System.lineSeparator()).append("  ").append(failure);
            throw new IllegalStateException("The 950 content bootstrap failed; admission refused (start with -D"
                    + BOOTSTRAP_PROPERTY + "=lenient to override):" + reasons);
        }
        return report;
    }

    /**
     * Funnels every native varp write (VarsManager, VarBitManager delegation,
     * whole-varp recomputes for varbits) through the facade's {@code sendConfig},
     * so one place applies the id resolver, the tier policy and the counters.
     */
    public static void installVarpSink(final Player player) {
        if (!player.isNative950()) throw new IllegalArgumentException("Native character required");
        player.getVarsManager().setNativeVarpSink(new com.rs.game.player.VarsManager.VarpSink() {
            @Override public void varp(int id, int value) {
                player.getPackets().sendConfig(id, value);
            }
        });
    }

    /** Runs {@code command} on the world thread (smokes and tools); the future completes with its result. */
    public <T> CompletableFuture<T> execute(final java.util.concurrent.Callable<T> command) {
        final CompletableFuture<T> result = new CompletableFuture<T>();
        if (!commands.offer(() -> {
            try { result.complete(command.call()); }
            catch (Throwable failure) { result.completeExceptionally(failure); }
        })) result.completeExceptionally(new IllegalStateException("Native world command queue is full"));
        return result;
    }

    // ------------------------------------------------------------------ world-owned NPCs

    /**
     * Registers an extra world-owned native NPC on the world thread. Every attached
     * session's view sees it at the index {@code EntityList} assigned, and the world-phase
     * move runs its walk queue when {@code wanderRadius} is positive. The roster is cleared
     * when the last character leaves, so a development world never keeps orphan entities.
     */
    public CompletableFuture<NPC> spawnNativeNpc(final int definitionId, final WorldTile tile,
                                                 final int size, final int wanderRadius) {
        return execute(() -> {
            World.getRegion(tile.getRegionId(), true);
            if (!World.isFloorFree(tile.getPlane(), tile.getX(), tile.getY()))
                throw new IllegalStateException("Native 947 NPC tile is blocked or unavailable in the selected cache");
            NPC npc = NPC.createNative950(definitionId, tile, size);
            World.addNative950Npc(npc);
            try {
                World.updateEntityRegion(npc);
                if (wanderRadius > 0) npc.setNative950Wander(wanderRadius);
                combat.register(npc);
            } catch (RuntimeException failure) {
                World.removeNative950Npc(npc);
                npcs.remove(npc);
                throw failure;
            }
            npcs.add(npc);
            return npc;
        });
    }

    /** Synchronous command admission: register once in every world-owned registry, with no legacy AI. */
    void addDiagnosticNpc(NPC npc) {
        if (Thread.currentThread() != thread)
            throw new IllegalStateException("Diagnostic NPCs belong to the native world thread.");
        if (npc == null || !npc.isNative950DiagnosticDefinition())
            throw new IllegalArgumentException("Expected a verified actual 950 diagnostic NPC.");
        int diagnosticCount = 0;
        for (NPC existing : npcs) if (existing.isNative950DiagnosticDefinition()) diagnosticCount++;
        if (diagnosticCount >= 128)
            throw new IllegalStateException("The world already has 128 diagnostic NPCs; use ;;clearnpcs to remove your test spawns.");
        World.getRegion(npc.getRegionId(), true);
        World.addNative950Npc(npc);
        try {
            World.updateEntityRegion(npc);
            combat.register(npc); // Only the already verified generic combat catalog can activate it.
            npcs.add(npc);
        } catch (RuntimeException failure) {
            World.removeNative950Npc(npc);
            npcs.remove(npc);
            throw failure;
        }
    }

    void removeDiagnosticNpc(NPC npc) {
        if(Thread.currentThread()!=thread)throw new IllegalStateException("Diagnostic removal belongs to the native world thread.");
        if(npc==null||!npc.isNative950DiagnosticDefinition()||npcs.stream().noneMatch(existing -> existing==npc))
            throw new IllegalArgumentException("Expected a world-owned diagnostic NPC.");
        combat.unregister(npc);
        removeExactNpc(npcs,npc);World.removeNative950Npc(npc);
    }

    static boolean removeExactNpc(List<NPC> roster,NPC target) {
        // Entity inherits coordinate equality: multiple test NPCs can occupy the same tile.
        return roster.removeIf(existing -> existing==target);
    }

    /** Capture keeps both native registries and the current world's lifetime in agreement. */
    void respawnCapturedButterfly(final NPC captured,final int delayTicks) {
        if(Thread.currentThread()!=thread)throw new IllegalStateException("Butterfly capture belongs to the native world thread");
        if(captured==null||!npcs.contains(captured)||!World.containsNPC(captured)||!Native950Hunter.isCatchable(captured,1)||delayTicks<1)
            throw new IllegalArgumentException("Expected an owned live950 butterfly");
        final long generation=skillNpcGeneration;
        final Object selectedCache=Cache.STORE;
        final int id=captured.getId(),size=captured.getSize(),wander=captured.getNative950Wander();
        final WorldTile home=new WorldTile(captured.getRespawnTile());
        final boolean diagnostic=captured.isNative950DiagnosticDefinition();
        npcs.remove(captured);World.removeNative950Npc(captured);
        com.rs.game.tasks.WorldTasksManager.schedule(new com.rs.game.tasks.WorldTask(){
            @Override public void run(){
                if(generation!=skillNpcGeneration||selectedCache!=Cache.STORE)return;
                NPC replacement=diagnostic?NPC.createNative950Diagnostic(id,home):NPC.createNative950(id,home,size);
                World.addNative950Npc(replacement);
                try {
                    World.updateEntityRegion(replacement);
                    if(wander>0)replacement.setNative950Wander(wander);
                    combat.register(replacement);npcs.add(replacement);
                }catch(RuntimeException failure){World.removeNative950Npc(replacement);npcs.remove(replacement);throw failure;}
            }
        },delayTicks);
    }

    /** The world-owned native NPC roster; world thread only. */
    List<NPC> nativeNpcs() { return npcs; }

    // ------------------------------------------------------------------ data-driven spawns

    /**
     * System property that turns {@code npcs/spawns.json} into world entities. Off by
     * default: the seven real-cache smokes reason about the exact set of NPCs in the world,
     * and a populated Lumbridge would change every one of those counts. The live server
     * opts in with {@code -Dataraxia947.npcSpawns=true}.
     */
    public static final String SPAWNS_PROPERTY = "ataraxia950.npcSpawns";

    /** The pre-port spelling, still honoured so an existing -D flag is not silently ignored. */
    public static final String LEGACY_SPAWNS_PROPERTY = "ataraxia947.npcSpawns";

    /**
     * Regions whose collision has finished loading and whose spawn rows have not been read
     * yet. Written by {@link Region.Native950SpawnListener} (any thread, under the region's
     * own monitor) and drained by the world thread at the head of the tick, so no entity is
     * ever created inside a region's map-load lock.
     */
    private final Set<Integer> pendingSpawnRegions = new LinkedHashSet<Integer>();
    /** Regions this world has already spawned; world thread only. */
    private final Set<Integer> spawnedRegions = new LinkedHashSet<Integer>();
    private long skillNpcGeneration;
    private final SpawnCounters spawnCounters = new SpawnCounters();
    private final Set<Integer> reportedSkips = new HashSet<Integer>();
    private volatile boolean dataSpawns;
    private final Native950SpawnScope spawnScope = Native950SpawnScope.fromProperty();
    private final Region.Native950SpawnListener spawnListener = new Region.Native950SpawnListener() {
        @Override public void regionLoaded(int regionId) {
            synchronized (pendingSpawnRegions) { pendingSpawnRegions.add(Integer.valueOf(regionId)); }
        }
    };

    /**
     * Why a spawn row never became an entity, and how many times. Written by the world
     * thread and read by whoever asks (a smoke, a probe), so every field is touched under
     * this object's monitor and {@link #copy()} hands out a consistent snapshot rather than
     * a live view whose numbers change while they are being compared.
     */
    public static final class SpawnCounters {
        /** Rows the validity table refused, by verdict name. */
        public final Map<String, Integer> refusedByVerdict = new TreeMap<String, Integer>();
        /** Regions spawned, rows read, entities created, and the four ways a row can end. */
        public int regions, rows, spawned, undecodable, blockedTiles, failures;
        /** Distinct 910 npc ids that were refused at least once. */
        public final Set<Integer> refusedIds = new TreeSet<Integer>();

        public synchronized int refused() {
            int total = 0;
            for (Integer count : refusedByVerdict.values()) total += count.intValue();
            return total;
        }

        synchronized void region() { regions++; }
        synchronized void row() { rows++; }
        synchronized void spawn(boolean blockedTile) { spawned++; if (blockedTile) blockedTiles++; }
        synchronized void undecodable() { undecodable++; }
        synchronized void failure() { failures++; }

        synchronized void refuse(String verdict, int npcId) {
            Integer current = refusedByVerdict.get(verdict);
            refusedByVerdict.put(verdict, Integer.valueOf(current == null ? 1 : current.intValue() + 1));
            refusedIds.add(Integer.valueOf(npcId));
        }

        synchronized SpawnCounters copy() {
            SpawnCounters copy = new SpawnCounters();
            copy.refusedByVerdict.putAll(refusedByVerdict);
            copy.refusedIds.addAll(refusedIds);
            copy.regions = regions; copy.rows = rows; copy.spawned = spawned;
            copy.undecodable = undecodable; copy.blockedTiles = blockedTiles; copy.failures = failures;
            return copy;
        }

        @Override public synchronized String toString() {
            return "spawns[regions=" + regions + ",rows=" + rows + ",spawned=" + spawned
                    + ",refused=" + refused() + refusedByVerdict + ",undecodable=" + undecodable
                    + ",blockedTiles=" + blockedTiles + ",failures=" + failures + "]";
        }
    }

    /** A copy of the spawn tallies; safe to read from any thread. */
    public SpawnCounters spawnCounters() { return spawnCounters.copy(); }

    public boolean isDataSpawnsEnabled() { return dataSpawns; }

    /**
     * Turns data-driven spawning on or off. Enabling installs the region listener and
     * re-announces every region already loaded, so a caller that loaded its map first still
     * gets its NPCs; disabling only stops further spawning - the entities already in the
     * world stay, because removing them under a live viewer is the encoder's business.
     */
    public void setDataSpawnsEnabled(boolean enabled) {
        dataSpawns = enabled;
        if (!enabled) {
            if (Region.getNative950SpawnListener() == spawnListener) Region.setNative950SpawnListener(null);
            return;
        }
        // Load and validate the table here rather than inside the first tick that needs it:
        // a world that cannot tell a Sheep from a Bill must refuse to start spawning, and it
        // must say so at the moment somebody asked for spawns.
        Native950IdValidity validity = Native950IdValidity.get();
        System.out.println("[Ataraxia950] data spawns enabled; npc ids " + validity.scanned(Native950IdValidity.Kind.NPC)
                + " (" + validity.coverage(Native950IdValidity.Kind.NPC) + "); scope=" + spawnScope);
        Region.setNative950SpawnListener(spawnListener);
        // Announce is once-per-Region-object, so a region loaded before this call has not
        // told anybody. Touching it makes it announce; loadMapStage 2 is a no-op reload. It
        // runs on the world thread because World.getRegions() is world state and this method
        // is called from wherever the operator asked for spawns.
        execute(() -> {
            for (Integer regionId : new ArrayList<Integer>(World.getRegions().keySet()))
                World.getRegion(regionId.intValue(), true);
            return null;
        });
    }

    /**
     * Spawns every region whose map finished loading since the last tick. Runs first in the
     * tick, before any movement, so an NPC that appears this tick is already in place when
     * the frames are built and no viewer sees it move from nowhere.
     */
    private void drainSpawnRegions() {
        if (!dataSpawns) { synchronized (pendingSpawnRegions) { pendingSpawnRegions.clear(); } return; }
        List<Integer> regions;
        synchronized (pendingSpawnRegions) {
            if (pendingSpawnRegions.isEmpty()) return;
            regions = new ArrayList<Integer>(pendingSpawnRegions);
            pendingSpawnRegions.clear();
        }
        for (Integer regionId : regions) {
            // Region map loading is complete; add starter content on the world thread.
            Native950Archaeology.populateRegion(regionId.intValue());
            Native950Invention.populateRegion(regionId.intValue());
            spawnRegion(regionId.intValue());
        }
    }

    /**
     * Turns one region's {@code spawns.json} rows into native NPCs. Every id goes through
     * {@link Native950IdValidity} first: only {@code same} - the 947 definition exists and
     * still means what the 910 data meant - is spawned. Everything else is counted with its
     * reason and named once in the log. A skipped spawn is a decision, never an omission,
     * and a repurposed id is exactly the case that would otherwise load cleanly and put the
     * wrong creature in the world.
     */
    int spawnRegion(int regionId) {
        if(regionId<0||regionId>65535)return 0;
        final boolean wholeRegion=spawnScope.allows(regionId);
        List<NPCSpawnsDataParser.Native950Spawn> rows = NPCSpawnsDataParser.native947Spawns(regionId);
        if(rows.isEmpty()||(!wholeRegion&&rows.stream().noneMatch(row->Native950SkillNpcPopulation.candidate(row.npcId))))return 0;
        if (!spawnedRegions.add(Integer.valueOf(regionId))) return 0;
        Native950IdValidity validity = Native950IdValidity.get();
        spawnCounters.region();
        int spawned = 0;
        for (NPCSpawnsDataParser.Native950Spawn row : rows) {
            if(!Native950SkillNpcPopulation.allows(spawnScope,regionId,row.npcId))continue;
            spawnCounters.row();
            Native950IdValidity.Verdict verdict = validity.verdict(Native950IdValidity.Kind.NPC, row.npcId);
            if (!verdict.isSafe()) {
                spawnCounters.refuse(verdict.name().toLowerCase(), row.npcId);
                if (reportedSkips.add(Integer.valueOf(row.npcId)))
                    System.out.println("[Ataraxia950] spawn skipped: npc " + row.npcId + " "
                            + validity.reason(Native950IdValidity.Kind.NPC, row.npcId));
                continue;
            }
            NPCDefinitions definitions = NPCDefinitions.getNPCDefinitions(row.npcId);
            if (definitions == null || definitions.decodeFailure != null
                    || definitions.size < 1 || definitions.size > 255) {
                spawnCounters.undecodable();
                if (reportedSkips.add(Integer.valueOf(row.npcId)))
                    System.out.println("[Ataraxia950] spawn skipped: npc " + row.npcId
                            + " has no usable 947 definition to size it");
                continue;
            }
            if(!wholeRegion && (!Native950SkillNpcPopulation.verified(row.npcId,definitions)
                    ||!World.isFloorFree(row.plane,row.x,row.y))) {
                spawnCounters.refuse("unavailableSkillSpawn",row.npcId);continue;
            }
            try {
                if (spawnDataNpc(row, definitions) != null) spawned++;
            } catch (RuntimeException failure) {
                spawnCounters.failure();
                System.err.println("[Ataraxia950] spawn failed: npc " + row.npcId + " at " + row.x + ","
                        + row.y + "," + row.plane + ": " + failure);
            }
        }
        System.out.println("[Ataraxia950] region " + regionId + ": " + spawned + " of " + rows.size()
                + " spawn rows became NPCs");
        return spawned;
    }

    /**
     * One data-driven NPC. Unlike {@link #spawnNativeNpc} this does not refuse a tile whose
     * collision mask is not clear: the 910 data places NPCs behind counters and on top of
     * scenery all over the world and the legacy loader never checked either, so refusing
     * them would silently depopulate the map. The blocked ones are counted instead, and the
     * wander step generator checks collision on every step regardless.
     */
    private NPC spawnDataNpc(NPCSpawnsDataParser.Native950Spawn row, NPCDefinitions definitions) {
        WorldTile tile = new WorldTile(row.x, row.y, row.plane);
        boolean blockedTile = !World.isFloorFree(row.plane, row.x, row.y);
        NPC npc = NPC.createNative950(row.npcId, tile, definitions.size);
        World.addNative950Npc(npc);
        try {
            World.updateEntityRegion(npc);
            // Ataraxia keeps facings in 1/16384 of a turn; NPC.getRespawnDirection can hand
            // back one full turn more than that, and the wire field is three bits of
            // (direction >> 11), so the value is normalised here rather than on the wire.
            npc.setDirection(row.direction & 0x3FFF);
            int wander = wanderRadius(row, definitions);
            if (wander > 0) npc.setNative950Wander(wander);
            combat.register(npc);
        } catch (RuntimeException failure) {
            World.removeNative950Npc(npc);
            npcs.remove(npc);
            throw failure;
        }
        npcs.add(npc);
        spawnCounters.spawn(blockedTile);
        return npc;
    }

    /**
     * How far a spawned NPC may wander, in tiles either side of its spawn tile. Two gates,
     * both from data rather than from taste: the spawn row's own {@code canMove} flag (35
     * rows in the file say no), and the 947 definition's movement capabilities, whose
     * {@code NORMAL_WALK} bit is the same bit {@code NPC.processNPC} tests before its random
     * walk. The radius is 5, which is the span the legacy random walk uses.
     */
    static int wanderRadius(NPCSpawnsDataParser.Native950Spawn row, NPCDefinitions definitions) {
        if (!row.canMove) return 0;
        return (definitions.movementCapabilities & NPC.NORMAL_WALK) != 0 ? DATA_WANDER_RADIUS : 0;
    }

    /** The tile span {@code NPC.processNPC}'s random walk uses (NPC.java:2262-2273). */
    public static final int DATA_WANDER_RADIUS = 5;

    /** Creates the verified banker once, the first time a session that knows one is admitted. */
    private NPC ensureBanker(Native950Content content) {
        if (content == null || content.banker == null) return null;
        for (NPC existing : npcs)
            if (existing.getId() == content.banker.definitionId
                    && existing.getX() == content.banker.x && existing.getY() == content.banker.y
                    && existing.getPlane() == content.banker.plane) return existing;
        // Returning characters may be far from Lumbridge; load the NPC's own
        // collision region independently of the player's restored scene.
        World.getRegion(new WorldTile(content.banker.x, content.banker.y, content.banker.plane).getRegionId(), true);
        if (!World.isFloorFree(content.banker.plane, content.banker.x, content.banker.y))
            throw new IllegalStateException("Verified banker tile is blocked or unavailable");
        NPC banker = NPC.createNative950(content.banker.definitionId,
                new WorldTile(content.banker.x, content.banker.y, content.banker.plane), content.banker.size);
        World.addNative950Npc(banker);
        try {
            World.updateEntityRegion(banker);
        } catch (RuntimeException failure) {
            World.removeNative950Npc(banker);
            throw failure;
        }
        npcs.add(banker);
        return banker;
    }

    void clearNativeNpcs() {
        skillNpcGeneration++;
        combat.clear();
        for (NPC npc : new ArrayList<NPC>(npcs)) World.removeNative950Npc(npc);
        npcs.clear();
        // The roster is emptied when the last character leaves, so the regions must be
        // forgotten with it or the next login would load a world with no NPCs in it. Both
        // halves are needed: this class forgets which regions it spawned, and Region forgets
        // which regions it announced. World.regions is a static map that is never cleared, so
        // without the re-arm the Region objects would keep their one-shot announcement from
        // the previous session and no login could ever repopulate the world again.
        spawnedRegions.clear();
        synchronized (pendingSpawnRegions) { pendingSpawnRegions.clear(); }
        Region.rearmNative950Spawns();
    }

    // ------------------------------------------------------------------ attach

    /**
     * Call after successful login bytes have been flushed and legacy game codecs
     * removed. A preceding temporary inbound gate may buffer bytes until this
     * future completes. Cipher suppliers MUST be the live login cipher streams.
     */
    public CompletableFuture<Native950Session> attach(Channel channel, String username,
            IntSupplier incomingCipher, IntSupplier outgoingCipher, byte[] appearance,
            SceneConfig scene, List<Native950Packets.Packet> interfaceBootstrap) {
        return attach(channel, username, incomingCipher, outgoingCipher, appearance, scene, interfaceBootstrap, null);
    }

    public CompletableFuture<Native950Session> attach(Channel channel, String username,
            IntSupplier incomingCipher, IntSupplier outgoingCipher, byte[] appearance,
            SceneConfig scene, List<Native950Packets.Packet> interfaceBootstrap, Native950Content content) {
        return attach(channel, username, incomingCipher, outgoingCipher, appearance, scene, interfaceBootstrap, content, null);
    }

    /** Persistent local profiles are explicit; standalone probes remain transient. */
    public CompletableFuture<Native950Session> attach(Channel channel, String username,
            IntSupplier incomingCipher, IntSupplier outgoingCipher, byte[] appearance,
            SceneConfig scene, List<Native950Packets.Packet> interfaceBootstrap, Native950Content content,
            Native950SaveStore saveStore) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(incomingCipher, "incomingCipher");
        Objects.requireNonNull(outgoingCipher, "outgoingCipher");
        Objects.requireNonNull(scene, "scene");
        if (saveStore != null) Objects.requireNonNull(content, "Persistent profiles require verified item content");
        final byte[] initialAppearance = Objects.requireNonNull(appearance, "appearance").clone();
        if (initialAppearance.length < 1 || initialAppearance.length > 255)
            throw new IllegalArgumentException("Appearance requires 1..255 bytes");
        final List<Native950Packets.Packet> bootstrap = new ArrayList<Native950Packets.Packet>(
                Objects.requireNonNull(interfaceBootstrap, "interfaceBootstrap"));
        for (Native950Packets.Packet packet : bootstrap) Objects.requireNonNull(packet, "bootstrap packet");
        final CompletableFuture<Native950Session> result = new CompletableFuture<Native950Session>();
        // Adopt the reservation the caller took before writing GameLoginResponse, or take
        // one now for a caller that never reserved. Either way the slot belongs to this
        // username from here on, so every failure path below can release it unambiguously.
        final boolean selfReserved;
        synchronized (admission) {
            Slot slot = slots[scene.playerIndex];
            if (slot == null) {
                if (holdsUsername(username)) {
                    result.completeExceptionally(new IllegalStateException(
                            "The native 947 world already holds a character for this account"));
                    return result;
                }
                if (used() >= capacity()) {
                    result.completeExceptionally(new IllegalStateException("The native 947 world is full ("
                            + used() + "/" + capacity() + " slots; installed encoder "
                            + frames.getClass().getSimpleName() + ")"));
                    return result;
                }
                slots[scene.playerIndex] = new Slot(username);
                selfReserved = true;
            } else if (slot.session != null || !slot.username.equals(username)) {
                result.completeExceptionally(new IllegalStateException(
                        "Native 947 player slot " + scene.playerIndex + " is not reserved for " + username));
                return result;
            } else selfReserved = false;
        }
        if (!commands.offer(() -> attachOnWorld(channel, username, incomingCipher, outgoingCipher,
                initialAppearance, scene, bootstrap, content, saveStore, selfReserved, result))) {
            if (selfReserved) release(scene.playerIndex, username);
            result.completeExceptionally(new IllegalStateException("Native world command queue is full"));
        }
        return result;
    }

    /** Final subinterface ownership in the exact, successfully flushed native login burst. */
    static java.util.Map<Integer,Integer> bootstrapInterfaceParents(List<Native950Packets.Packet> bootstrap) {
        java.util.Map<Integer,Integer> parents = new java.util.LinkedHashMap<>();
        for (Native950Packets.Packet packet : bootstrap) {
            byte[] body=packet.payload();
            switch(packet.type()) {
                case IF_OPENTOP: parents.clear(); break;
                case IF_OPENSUB:
                    if(body.length!=23)break;
                    int parent=((body[0]&255)<<24)|((body[1]&255)<<16)|((body[2]&255)<<8)|(body[3]&255);
                    int face=((body[17]&255)<<8)|((body[16]-128)&255);
                    removeBootstrapSub(parents,parent);
                    parents.put(parent,face);
                    break;
                case IF_CLOSESUB:
                    if(body.length!=4)break;
                    //950 IF_CLOSESUB intv2: bytes16/24/0/8, unlike the open packet's plain BE.
                    int closed=((body[1]&255)<<24)|((body[0]&255)<<16)|((body[3]&255)<<8)|(body[2]&255);
                    removeBootstrapSub(parents,closed);
                    break;
                default: break;
            }
        }
        return java.util.Collections.unmodifiableMap(parents);
    }

    private static void removeBootstrapSub(java.util.Map<Integer,Integer> parents,int parent) {
        Integer old=parents.remove(parent);
        if(old==null)return;
        // Removing/replacing a parent also retires its nested children, as the client does.
        for(Integer child:new java.util.ArrayList<>(parents.keySet()))
            if((child>>>16)==old)removeBootstrapSub(parents,child);
    }

    static boolean bootstrapOpensScene(List<Native950Packets.Packet> bootstrap) {
        return Integer.valueOf(1482).equals(bootstrapInterfaceParents(bootstrap).get((1477<<16)|30));
    }

    static void mirrorBootstrapInterfaces(Player player,List<Native950Packets.Packet> bootstrap) {
        for(java.util.Map.Entry<Integer,Integer> entry:bootstrapInterfaceParents(bootstrap).entrySet())
            player.getInterfaceManager().registerNativeOpen(entry.getValue(),entry.getKey()>>>16,entry.getKey()&65535);
    }

    private void attachOnWorld(Channel channel, String username, IntSupplier incoming, IntSupplier outgoing,
            byte[] appearance, SceneConfig scene, List<Native950Packets.Packet> bootstrap, Native950Content content,
            Native950SaveStore saveStore, boolean selfReserved, CompletableFuture<Native950Session> result) {
        Player player = null;
        Native950Session attached = null;
        boolean claimed = false;
        try {
            if (!channel.isActive()) throw new IllegalStateException("Client disconnected before world admission");
            if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Initialize the modern read-only cache before world admission");
            ensureBootstrap(); // P8 content tables + World core tasks, once per JVM, fail closed
            Native950Save saved = saveStore == null ? null : saveStore.load(username);
            String profileName = saveStore == null ? username : Native950Save.canonicalUsername(username);
            // The first scene is subject to the same rule as every later rebuild, and a fresh
            // login inside a non-mainland zone was observed building empty until its own area was
            // declared, so resolve the areaType from the entry position rather than the config.
            final SceneConfig configured = saved == null ? scene : new SceneConfig(saved.x(), saved.y(), saved.plane(),
                    scene.playerIndex, scene.npcBits, scene.areaType, scene.hash1, scene.hash2);
            Native950MapAreas.verify();
            final int entryArea = Native950MapAreas.areaTypeFor(configured.x, configured.y, configured.areaType);
            final SceneConfig entryScene = entryArea == configured.areaType ? configured
                    : new SceneConfig(configured.x, configured.y, configured.plane, configured.playerIndex,
                            configured.npcBits, entryArea, configured.hash1, configured.hash2);
            if (saved != null) {
                int archive = com.rs.utils.Utils.getMapArchiveId(saved.x() >> 6, saved.y() >> 6);
                if (!Cache.STORE.getIndexes()[5].fileExists(archive, 3)
                        || !Cache.STORE.getIndexes()[5].fileExists(archive, 0))
                    throw new IllegalStateException("Saved position has no map square in the selected modern cache");
            }
            player = Player.createNative950(profileName, new WorldTile(entryScene.x, entryScene.y, entryScene.plane), channel);
            installVarpSink(player);
            player.loadMapRegions();
            if (saved != null && !World.isFloorFree(entryScene.plane, entryScene.x, entryScene.y))
                throw new IllegalStateException("Saved position is blocked or unavailable in the selected modern cache");
            player.consumeNative950MapRefresh();
            World.addNative950Player(player, entryScene.playerIndex);
            if (player.getIndex() != entryScene.playerIndex)
                throw new IllegalStateException("Login player index does not match the Ataraxia world slot");
            World.updateEntityRegion(player);
            NPC banker = ensureBanker(content);
            Native950GameTransport transport = new Native950GameTransport(incoming, outgoing, thread);
            Native950Session session = new Native950Session(player, channel, transport, configured, content,
                    saveStore, saved, banker);
            attached = session;
            combat.attach(player);
            if (!claim(entryScene.playerIndex, username, session))
                throw new IllegalStateException("Native 947 player slot " + entryScene.playerIndex + " was taken during admission");
            claimed = true;
            // Regenerates the appearance body from the restored Player; the encoder's
            // per-viewer cache is empty, so the first frame carries it as mask 0x4.
            session.initialAppearance(appearance);
            final List<Native950Packets.Packet> loginFrames = frames.admit(player, entryScene, players());
            channel.closeFuture().addListener(ignored -> enqueueClose(session));
            channel.eventLoop().execute(() -> {
                try {
                    if (!channel.isActive()) throw new IllegalStateException("Client disconnected during world admission");
                    channel.pipeline().addLast(TRANSPORT_NAME, transport);
                    for (Native950Packets.Packet packet : loginFrames) channel.write(packet);
                    for (Native950Packets.Packet packet : bootstrap) channel.write(packet);
                    channel.writeAndFlush(Native950Packets.tickEnd()).addListener(write -> {
                        if (!write.isSuccess()) {
                            result.completeExceptionally(write.cause());
                            channel.close();
                            enqueueClose(session);
                        } else if (!commands.offer(() -> {
                            if (!channel.isActive() || session.isClosed()) {
                                closeOnWorld(session);
                                result.completeExceptionally(new IllegalStateException("Client disconnected during world admission"));
                            } else {
                                try {
                                    // Mirror every actual mount, including HUD panels that management
                                    // temporarily reuses. Empty probe bootstraps invent no ownership.
                                    mirrorBootstrapInterfaces(session.player(),bootstrap);
                                    session.ready(frames);
                                    System.out.println("[Ataraxia950] Authenticated client attached to " + session.snapshot()
                                            + (saveStore == null ? " (temporary)" : saved == null ? " (new saved profile)" : " (restored profile)"));
                                    result.complete(session);
                                } catch (Throwable failure) {
                                    result.completeExceptionally(failure);
                                    channel.close();
                                    closeOnWorld(session);
                                }
                            }
                        })) {
                            result.completeExceptionally(new IllegalStateException("Native world command queue is full"));
                            channel.close();
                        }
                    });
                } catch (Throwable failure) {
                    result.completeExceptionally(failure);
                    channel.close();
                    enqueueClose(session);
                }
            });
        } catch (Throwable failure) {
            if (attached != null) {
                Player character = attached.player();
                attached.close();
                combat.detach(character);
                if (claimed) frames.release(character);
                freeSlot(scene.playerIndex, attached);
            } else if (player != null && World.getPlayers().contains(player)) {
                World.removeNative950Player(player);
            }
            // Exactly one owner releases a reservation. A slot this attach took itself is
            // released here; a slot the caller reserved before writing GameLoginResponse
            // stays the caller's to release on its own failure branch. The release names the
            // username, because freeSlot above may already have cleared the slot and reserve()
            // runs on Netty login threads: an unchecked release could otherwise hand a
            // concurrent login's fresh reservation away and fail that login with
            // "slot N is not reserved for X".
            if (selfReserved) release(scene.playerIndex, username);
            if (sessions().isEmpty()) clearNativeNpcs();
            result.completeExceptionally(failure);
            channel.close();
        }
    }

    private void enqueueClose(Native950Session session) {
        // Even if the bounded queue is full, the tick sees the closed channel.
        commands.offer(() -> closeOnWorld(session));
    }

    private void closeOnWorld(Native950Session session) {
        boolean wasOpen = !session.isClosed();
        Player character = session.player();
        session.close();
        combat.detach(character);
        if (wasOpen) frames.release(character);
        freeSlot(session.playerIndex(), session);
        if (sessions().isEmpty()) clearNativeNpcs();
        if (wasOpen)
            System.out.println("[Ataraxia950] Local character " + session.playerIndex()
                    + " removed from world; " + (capacity() - reservedSlots()) + " connection slots available");
    }

    /** The lowest-indexed attached session's diagnostic view, or null when the world is empty. */
    public CompletableFuture<Native950Session.Snapshot> snapshot() {
        CompletableFuture<Native950Session.Snapshot> result = new CompletableFuture<Native950Session.Snapshot>();
        if (!commands.offer(() -> {
            List<Native950Session> live = sessions();
            result.complete(live.isEmpty() ? null : live.get(0).snapshot());
        })) result.completeExceptionally(new IllegalStateException("Native world command queue is full"));
        return result;
    }

    /** One attached session's diagnostic view by player index, or null when that slot is empty. */
    public CompletableFuture<Native950Session.Snapshot> snapshot(final int playerIndex) {
        CompletableFuture<Native950Session.Snapshot> result = new CompletableFuture<Native950Session.Snapshot>();
        if (!commands.offer(() -> {
            Native950Session session;
            synchronized (admission) {
                Slot slot = playerIndex >= 1 && playerIndex <= MAX_PLAYER_INDEX ? slots[playerIndex] : null;
                session = slot == null ? null : slot.session;
            }
            result.complete(session == null ? null : session.snapshot());
        })) result.completeExceptionally(new IllegalStateException("Native world command queue is full"));
        return result;
    }

    private void run() {
        long nextTick = System.nanoTime() + TICK_NANOS;
        for (;;) {
            try {
                long remaining = nextTick - System.nanoTime();
                if (remaining > 0) {
                    Runnable command = commands.poll(remaining, TimeUnit.NANOSECONDS);
                    if (command != null) command.run();
                }
                if (System.nanoTime() >= nextTick) {
                    World.currentTime++;
                    pumpSchedulers();
                    worldTick();
                    nextTick += TICK_NANOS;
                    if (nextTick < System.nanoTime() - TICK_NANOS) nextTick = System.nanoTime() + TICK_NANOS;
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                for (Native950Session session : sessions()) {
                    session.channel().close();
                    closeOnWorld(session);
                }
                return;
            } catch (Throwable failure) {
                System.err.println("[Ataraxia950] World operation failed: " + failure);
                failure.printStackTrace();
                for (Native950Session session : sessions()) {
                    session.channel().close();
                    closeOnWorld(session);
                }
            }
        }
    }

    /**
     * P6 world-phase tick, in the WorldThread order: every entity moves, then every
     * viewer's frame is built, then every mask is reset.
     *
     * <p>Interleaving those phases per session was correct while the world held exactly
     * one character. With N sessions it is not: a viewer encoded before another character
     * has moved describes a world that never existed, and because {@code NPC_INFO} offsets
     * are relative to the local actor's post-movement tile
     * (verified/NPC_INFO.md, "Positioning uses the local actor's most recent path
     * position"), a frame built out of phase is wrong on the wire, not merely stale.
     *
     * <p>A failure isolates to its own session: that connection is closed and the
     * remaining viewers still get a complete, consistent frame this tick.
     */
    private void worldTick() {
        for (Native950Session session : sessions())
            if (!session.channel().isActive()) closeOnWorld(session);
        // Phase 0: regions whose collision finished loading since the last tick become
        // entities now, before anything moves, so a viewer never sees an NPC appear
        // mid-step.
        drainSpawnRegions();
        if (sessions().isEmpty() && npcs.isEmpty()) return;
        // Phase 1a: drain client input, so a walk requested this tick is routed before the
        // mover runs - the order the single-session tick() has always used.
        phase("input", Native950Session::tickInput);
        combat.beforeMovement();
        // Phase 1b: every entity moves. Characters first, then the world-owned NPCs, so no
        // frame can mix a pre-move character with a post-move NPC.
        phase("move", Native950Session::tickMove);
        phase("familiars", session -> Native950Familiars.tick(session.player()));
        for (NPC npc : new ArrayList<NPC>(npcs)) {
            try { npc.processNative950Movement(); }
            catch (Throwable failure) {
                System.err.println("[Ataraxia950] Native NPC " + npc.getIndex() + " movement failed: " + failure);
                failure.printStackTrace();
            }
        }
        combat.afterMovement();
        // Phase 2: every viewer's frame is built from the same post-movement world. The
        // encoder takes one immutable snapshot of it here, before the first viewer is
        // encoded, so no two viewers in this tick can disagree about where anyone is.
        final List<NPC> roster = Collections.unmodifiableList(new ArrayList<NPC>(npcs));
        final List<Player> characters = players();
        final Native950Frames encoder = frames;
        encoder.beginFrames(characters);
        phase("frame", session -> session.tickFrame(encoder, roster, characters));
        // Phase 3: masks reset only after every frame has read them. The session keeps
        // ownership of its own tickEnd packet and its flush.
        phase("end", Native950Session::tickEnd);
        for (NPC npc : npcs) npc.resetMasks();
    }

    /**
     * Runs one tick phase across every live session. A failure isolates to its own
     * connection: that session is closed and the remaining viewers still complete the
     * phase, because a half-built frame for one viewer must not cost the others theirs.
     */
    private void phase(String name, java.util.function.Consumer<Native950Session> body) {
        for (Native950Session session : sessions()) {
            if (session.isClosed() || !session.channel().isActive()) continue;
            try {
                body.accept(session);
            } catch (Throwable failure) {
                System.err.println("[Ataraxia950] Native session " + session.playerIndex()
                        + " failed during the " + name + " phase: " + failure);
                failure.printStackTrace();
                session.channel().close();
                closeOnWorld(session);
            }
        }
    }

    /** Every native character in the world this tick, ordered by player index. */
    private List<Player> players() {
        List<Player> characters = new ArrayList<Player>();
        for (Native950Session session : sessions()) if (!session.isClosed()) characters.add(session.player());
        return Collections.unmodifiableList(characters);
    }

    /**
     * Same order as the legacy WorldThread.run() head (WorldThread.java:26-32): advance the
     * cycle counter that content reads alongside World.currentTime, drain the ServiceProvider
     * wheel (game tasks, then due scheduled tasks), then the tick-based WorldTasksManager. Runs
     * every tick even with no session attached so World.init-style tasks keep their cadence.
     */
    static void pumpSchedulers() {
        WorldThread.WORLD_CYCLE++;
        CoresManager.drainNativeTick();
        WorldTasksManager.processTasks();
    }

    /** Exact cache-derived metadata supplied by the pinned OpenNXT frontend. */
    public static final class SceneConfig {
        public final int x, y, plane, playerIndex, npcBits, areaType, hash1, hash2;
        public SceneConfig(int x, int y, int plane, int playerIndex,
                           int npcBits, int areaType, int hash1, int hash2) {
            // P6: any slot PLAYER_INFO can address. Whether the world hands one out is the
            // installed frame encoder's capacity, not a constant here.
            if (playerIndex < 1 || playerIndex > MAX_PLAYER_INDEX)
                throw new IllegalArgumentException("The native 947 world addresses player slots 1.." + MAX_PLAYER_INDEX);
            if (x < 0 || x > 16383 || y < 0 || y > 16383)
                throw new IllegalArgumentException("Scene coordinates must fit the 14-bit world");
            if (plane < 0 || plane > 3) throw new IllegalArgumentException("plane 0..3 required");
            if (npcBits < 1 || npcBits > 15)
                throw new IllegalArgumentException("NPC offset width must fit 1..15 bits");
            if (areaType < 0 || areaType > 65535) throw new IllegalArgumentException("areaType must fit an unsigned short");
            this.x = x; this.y = y; this.plane = plane; this.playerIndex = playerIndex;
            this.npcBits = npcBits; this.areaType = areaType; this.hash1 = hash1; this.hash2 = hash2;
        }
    }
}
