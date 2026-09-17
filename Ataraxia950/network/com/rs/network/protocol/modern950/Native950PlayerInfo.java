package com.rs.network.protocol.modern950;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import static com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;

/**
 * Multi-player PLAYER_INFO (server opcode 27, size -2) and the initial REBUILD_NORMAL player
 * bit stream, for N players in one frame.
 *
 * <p>This is the 947 port of the 910 {@code LocalPlayerUpdate} algorithm. What was kept, and
 * what the evidence forced to change, is spelled out in {@code notes/P6-playerinfo.md}. In
 * short:
 *
 * <ul>
 *   <li><b>Kept</b> (verified bit-compatible in {@code verified/PLAYER_INFO.md} and
 *       {@code player-movement-passes/player-local-movement/player-external-movement}
 *       disassembly): the four passes and their order, the 2-bit skip selector with 5/8/11-bit
 *       counts, the local forms (mask-only / remove / teleport short and long), the external
 *       add form, the three region-hash update forms, and the per-viewer appearance MD5
 *       cache.</li>
 *   <li><b>Changed</b>: the external init record is <b>20</b> bits, not 18 - bits 0..17 are the
 *       910 region hash {@code regionY | regionX&lt;&lt;8 | plane&lt;&lt;16} and bits 18..19 are a
 *       movement-speed index (client {@code 0x140125179} reads 20 bits, {@code 0x140125190}
 *       takes {@code (v&gt;&gt;18)&amp;3} as a table index into {@code 0x140C9C888}, and
 *       {@code 0x140125280} rebuilds the tile from the low 18 bits). The mask header uses the
 *       extension markers 0x1 / 0x4000 / 0x40000, each pending player's mask block is preceded
 *       by two skipped bytes, and the appearance length is {@code (N+128)&amp;255} with the body
 *       copied in normal order. All of that lives in {@link Native950PlayerMasks}, which this
 *       class composes rather than duplicates, so the confirmed-mask allow-list and the client
 *       consumption order are enforced in exactly one place.</li>
 * </ul>
 *
 * <p><b>Movement form.</b> A local player that moved is written with the compact walk form
 * ({@link #FORM_WALK}, 3 direction bits and the extra-step flag) for a one-tile step or the
 * compact run form ({@link #FORM_RUN}, 4 direction bits) for a two-tile step, and otherwise with
 * the absolute form ({@link #FORM_ABSOLUTE}: 15 packed bits when both offsets fit in -16..15,
 * else 3 speed bits + 30 position bits). Both direction tables are verified entry by entry
 * ({@code player-direction-jumptable.txt} and {@code MOVEMENT_TABLES.md} section 1) and agree
 * with 910 {@code Utils.getPlayerWalkingDirection}/{@code getPlayerRunningDirection}; a
 * displacement with no table entry falls back to the absolute form rather than being guessed at.
 * The compact forms do not carry a speed field, so they are only used when the client's per-slot
 * speed token already agrees - see {@link ViewState#speedTokens} and {@link #movementForm}.
 * Emitting less is always safe; a wrong bit desynchronises every following player in the frame.
 *
 * <p><b>Index reuse.</b> The world hands out the lowest free player index, so a slot a viewer
 * still holds can be occupied by a different character a tick later. Each {@link Actor} carries
 * an occupancy {@link Actor#identity}, and a changed identity at a slot this viewer holds
 * locally is written as a removal, with the new occupant added by a later frame - the same rule
 * {@code Native950NpcViewport} applies to reused NPC indices. Without it the new character would
 * be encoded as the previous one continuing to move, and the client would never destroy the old
 * slot object.
 *
 * <p><b>State.</b> {@link ViewState} is one viewer's view of the world (the 910
 * {@code LocalPlayerUpdate} fields plus OpenNXT's {@code Viewport}). It is mutated by
 * {@link #initialScene} and {@link #frame} only, and both must run on the world thread.
 * {@code com.rs.game.player.client.Native950Viewport} owns one per session and enforces that.
 *
 * <p><b>Native addresses.</b> This class was ported from the 947 encoder and most hex VAs in its
 * comments are still <b>947</b> addresses; the two revisions' player code happens to occupy
 * overlapping ranges, so a 947 VA disassembled in the 950 binary lands on unrelated code rather
 * than failing. Comments that cite 950 say so explicitly. What was re-derived against 950: the GPI
 * reader is 0x140125b10 (reached from the PLAYER_INFO dispatch at 0x140143660), it keeps 947's
 * per-slot layout byte for byte - the pass discriminator at record+0x27, the next-tick flag at
 * record+0x26, four movement passes and the end-of-tick rotation - and all three movement forms
 * keep their bit widths and direction tables (walk 3 bits/8 entries at 0x1401262c2, run 4 bits/16
 * entries at 0x140126358, absolute 1+15 or 1+3+30 bits at 0x1401264f2). The bit layout below is
 * therefore unchanged; the masks and the appearance body are not, and those live in
 * {@code Native950PlayerMasks}.
 *
 * <p><b>Input.</b> The world is handed in as an {@link Actor}{@code []} indexed by player index
 * (slot 0 unused), one immutable snapshot per player per tick. Nothing in this class reads the
 * cache, touches a Player, or performs I/O, so it is unit-testable byte for byte.
 */
public final class Native950PlayerInfo {

    /** Player index slots; indices 1..2047 are usable, exactly as the 910 world and the client. */
    public static final int SLOTS = 2048;
    /** Lowest legal player index. Index 0 is never a player. */
    public static final int MIN_INDEX = 1;
    /** Highest legal player index. */
    public static final int MAX_INDEX = 2047;

    /** 910 {@code LocalPlayerUpdate.MAX_PLAYER_ADD}: adds per frame, to bound a crowded add burst. */
    public static final int MAX_PLAYER_ADD = 50;

    /** 910 {@code needsAdd}/{@code needsRemove} radius for a normal (non large-scene) viewport. */
    public static final int VIEW_DISTANCE = 24;

    /**
     * The 2-bit movement-speed index written for an external slot that holds no player. This is
     * the value the shipped, client-verified initial scene has always carried for every slot
     * ({@code Native950Packets.initialSinglePlayerScene} writes {@code 0xC0000} per record).
     */
    public static final int EMPTY_SLOT_SPEED = 3;

    /**
     * 910 {@code Player.getMovementType()} for a player that is not running: the value the
     * verified {@code Native950Packets.singlePlayerWalkStep} bytes carry in the 15-bit form.
     */
    public static final int MOVEMENT_WALK = 2;
    /** 910 {@code Player.getMovementType()} for a running player. */
    public static final int MOVEMENT_RUN = 3;
    /** 910 {@code hasTeleported()} movement type; truncates to 0 in the 2-bit region-hash field. */
    public static final int MOVEMENT_TELEPORT = 4;

    /**
     * The last index the client's movement-speed table can be dereferenced at. The table at
     * {@code 0x140C9C888} is exactly five int32 entries ({@code -1, 0, 1, 2, 3});
     * {@code 0x140C9C89C} already belongs to an unrelated {@code OggS} datum
     * ({@code verified/MOVEMENT_TABLES.md} section 3). The 3-bit speed fields can express 5, 6
     * and 7, which would make the client form a pointer into foreign memory, so no speed field
     * this encoder writes may exceed this.
     */
    public static final int MAX_SPEED_INDEX = 4;

    /**
     * Local movement form 1 ({@code 0x1401259E2}): 3 direction bits ({@code 0x1401259E8}) then
     * the extra-step flag ({@code 0x1401259F8}). One tile per tick.
     */
    public static final int FORM_WALK = 1;
    /**
     * Local movement form 2 ({@code 0x140125BA8}): 4 direction bits ({@code 0x140125BB1}). Two
     * tiles per tick; every one of the 16 entries is a Chebyshev-distance-2 move.
     */
    public static final int FORM_RUN = 2;
    /**
     * Local movement form 3 ({@code 0x140125D42}): the absolute position write, short (15 bits)
     * or long (3 speed bits + 30 position bits). The only form that carries a speed field, and
     * therefore the only one that refreshes the client's per-slot speed token.
     */
    public static final int FORM_ABSOLUTE = 3;

    /** {@link ViewState#speedToken(int)} for a slot whose client-side speed token is unknown. */
    public static final int SPEED_TOKEN_UNKNOWN = -1;

    /**
     * The verified 3-bit walk direction table, indexed {@code [dy + 1][dx + 1]}.
     *
     * <p>{@code verified/player-direction-jumptable.txt}, jump targets
     * {@code 0x140125B25..0x140125B5F} re-dumped in {@code MOVEMENT_TABLES-constants.txt}
     * section 5: 0=(-1,-1) 1=(0,-1) 2=(+1,-1) 3=(-1,0) 4=(+1,0) 5=(-1,+1) 6=(0,+1) 7=(+1,+1) -
     * the perimeter of the 3x3 square in row-major order. The centre has no encoding.
     *
     * <p>910 {@code Utils.getPlayerWalkingDirection} (Utils.java:1160-1185) returns exactly these
     * eight numbers for exactly these eight deltas, which is the independent second source this
     * table is checked against.
     */
    private static final int[][] WALK_DIRECTIONS = {
        { 0,  1,  2},
        { 3, -1,  4},
        { 5,  6,  7},
    };

    /**
     * The verified 4-bit run direction table, indexed {@code [dy + 2][dx + 2]}.
     *
     * <p>{@code verified/MOVEMENT_TABLES.md} section 1: the run branch is a literal
     * {@code cmp}/{@code jne} chain at {@code 0x140125C0C..0x140125CFA}, so all 16 arms were read
     * out of the instruction stream. Every entry is a Chebyshev-distance-2 move - the perimeter
     * of the 5x5 square in row-major order, the same ordering convention as the walk table one
     * radius out. The interior has no encoding, which is why a two-tile run that turns a corner
     * cannot use this form.
     *
     * <p>910 {@code Utils.getPlayerRunningDirection} (Utils.java:1104-1152) returns exactly these
     * sixteen numbers for exactly these sixteen deltas.
     */
    private static final int[][] RUN_DIRECTIONS = {
        { 0,  1,  2,  3,  4},
        { 5, -1, -1, -1,  6},
        { 7, -1, -1, -1,  8},
        { 9, -1, -1, -1, 10},
        {11, 12, 13, 14, 15},
    };

    /**
     * Appearance budget, ported from 910 {@code needAppearenceUpdate}:
     * {@code (ProtocolSet.PROTOCOL_LIMIT - 500) / 2} with PROTOCOL_LIMIT = 15000. Once the mask
     * section is this big, appearance blocks are held back to the next frame rather than risking
     * an oversized PLAYER_INFO.
     */
    public static final int APPEARANCE_BUDGET = (15000 - 500) / 2;

    /** PLAYER_INFO is a variable-short packet; a longer body could not be framed. */
    private static final int MAX_BODY = 65535;

    /** 30 tile-hash bits + 2046 external 20-bit records, padded to a byte boundary. */
    private static final int INITIAL_BIT_STREAM_BYTES = (30 + 2046 * 20 + 7) / 8;

    private Native950PlayerInfo() { }

    // ------------------------------------------------------------------ actor

    /**
     * Non-appearance mask content for one actor in one tick. The implementation returns a fresh
     * {@link Native950PlayerMasks.Builder} on every call, because the same actor can be encoded
     * into more than one viewer's frame and one of those may also need the appearance block.
     *
     * <p>A CANDIDATE mask is refused here exactly as it is everywhere else: calling
     * {@code Native950PlayerMasks.Builder.candidateMask} throws
     * {@link UnsupportedOperationException}, and this encoder lets it out unchanged.
     *
     * <p>An actor that declares a mask source must set at least one confirmed mask on the
     * builder it returns; an empty builder throws out of {@code build()} rather than emitting a
     * pending-mask bit with nothing behind it. Set the source to null for "no masks this tick".
     */
    public interface MaskSource {
        Native950PlayerMasks.Builder masks();
        /** Per-recipient presentation of an already captured mask snapshot. */
        default Native950PlayerMasks.Builder masks(int viewerIndex) { return masks(); }
    }

    /** Whether {@code other} belongs in {@code viewer}'s local list this tick. */
    public interface Visibility {
        boolean visible(Actor viewer, Actor other);
    }

    /**
     * The 910 radius test with no map-region membership check: same plane, Chebyshev distance
     * within {@link #VIEW_DISTANCE}. A session that knows which map regions its client holds
     * should install a stricter policy (910 {@code needsAdd} also requires
     * {@code player.getMapRegionsIds().contains(p.getRegionId())}).
     */
    public static final Visibility DISTANCE_ONLY = new Visibility() {
        @Override public boolean visible(Actor viewer, Actor other) {
            return viewer.plane == other.plane
                    && Math.abs(viewer.x - other.x) <= VIEW_DISTANCE
                    && Math.abs(viewer.y - other.y) <= VIEW_DISTANCE;
        }
    };

    /**
     * One player as the encoder sees it for one tick: position now, position before this tick's
     * movement, whether it moved, its movement-speed index, its appearance body plus the hash
     * that drives the per-viewer suppression, and its non-appearance masks.
     *
     * <p>Immutable. Build one per player per tick and hand the same instance to every viewer.
     */
    public static final class Actor {
        public final int index;
        public final int x, y, plane;
        public final int lastX, lastY, lastPlane;
        /** 910 {@code hasTeleported() || getNextWalkDirection() != -1}. */
        public final boolean moved;
        /** Generation whose force mask is queued this frame; zero includes stationary cancellation. */
        public final long forceMovementGeneration;
        /** An engine waypoint arrival belonging to the current forced movement. */
        public final long forceMovementArrivalGeneration;
        private final int forceFinalX, forceFinalY;
        /**
         * 910 {@code Player.getMovementType()} ({@link #MOVEMENT_WALK} / {@link #MOVEMENT_RUN}),
         * or {@link #MOVEMENT_TELEPORT} when the player teleported. Written as 3 bits in the long
         * teleport form, as bits 12..14 of the 15-bit short form, and as bits 18..19 (masked to
         * two bits, exactly as 910 does) of the 20-bit init and region-hash records.
         */
        public final int movementType;
        /** 910 {@code isRunning() && !hasFinished()}: false hides the player from every viewer. */
        public final boolean visible;
        /**
         * Which occupancy of {@link #index} this snapshot belongs to. The world reuses the
         * lowest free player index, so the same slot number can carry a different character a
         * tick after the previous one left. A viewer that still holds the old occupant must be
         * sent a removal before the new one is added, and the only way to notice is a serial
         * that changes with the character rather than with the slot. Zero means "not tracked"
         * (unit fixtures and any caller that never reuses an index); a non-zero value that
         * differs from the one this viewer last cached at the same slot is a removal.
         */
        public final long identity;
        private final byte[] appearance;
        private final byte[] appearanceHash;
        private final MaskSource masks;

        private Actor(Builder builder) {
            this.index = builder.index;
            this.x = builder.x; this.y = builder.y; this.plane = builder.plane;
            this.lastX = builder.lastX; this.lastY = builder.lastY; this.lastPlane = builder.lastPlane;
            this.moved = builder.moved;
            this.forceMovementGeneration = builder.forceMovementGeneration;
            this.forceMovementArrivalGeneration = builder.forceMovementArrivalGeneration;
            this.forceFinalX = builder.forceFinalX; this.forceFinalY = builder.forceFinalY;
            this.movementType = builder.movementType;
            this.visible = builder.visible;
            this.identity = builder.identity;
            this.appearance = builder.appearance;
            this.appearanceHash = builder.appearanceHash;
            this.masks = builder.masks;
        }

        /** 910 {@code WorldTile.getRegionHash()}: the low 18 bits of the 20-bit records. */
        public int regionHash() { return Native950PlayerInfo.regionHash(x, y, plane); }
        /** The same hash for the position this actor occupied before this tick's movement. */
        public int previousRegionHash() {
            return Native950PlayerInfo.regionHash(lastX, lastY, lastPlane);
        }
        /** 910 {@code WorldTile.getXInRegion()}; the external add record's 6-bit X. */
        public int xInRegion() { return x & 0x3F; }
        /** 910 {@code WorldTile.getYInRegion()}; the external add record's 6-bit Y. */
        public int yInRegion() { return y & 0x3F; }
        /** 910 {@code WorldTile.getTileHash()}; the 30 leading bits of the initial bit stream. */
        public int tileHash() { return y + (x << 14) + (plane << 28); }
        /** True when this actor has non-appearance mask content this tick. */
        public boolean hasMasks() { return masks != null; }
        /** The appearance body, or null when this actor has none to offer. */
        public byte[] appearance() { return appearance == null ? null : appearance.clone(); }

        public static Builder builder(int index, int x, int y, int plane) {
            return new Builder(index, x, y, plane);
        }

        /** A visible, stationary actor with no appearance and no masks. */
        public static Actor at(int index, int x, int y, int plane) {
            return builder(index, x, y, plane).build();
        }

        @Override public String toString() {
            return "Actor[" + index + " @" + x + "," + y + "," + plane + (moved ? " moved" : "")
                    + (appearance == null ? "" : " appearance=" + appearance.length)
                    + (masks == null ? "" : " masks") + "]";
        }

        /** Mutable builder; every setter validates immediately. */
        public static final class Builder {
            private final int index, x, y, plane;
            private int lastX, lastY, lastPlane;
            private boolean moved;
            private long forceMovementGeneration, forceMovementArrivalGeneration;
            private int forceFinalX = -1, forceFinalY = -1;
            private int movementType = MOVEMENT_WALK;
            private boolean visible = true;
            private long identity;
            private byte[] appearance;
            private byte[] appearanceHash;
            private MaskSource masks;

            private Builder(int index, int x, int y, int plane) {
                if (index < MIN_INDEX || index > MAX_INDEX)
                    throw new IllegalArgumentException("Player index must be " + MIN_INDEX + ".." + MAX_INDEX);
                coordinate(x, "x"); coordinate(y, "y");
                if (plane < 0 || plane > 3) throw new IllegalArgumentException("plane must be 0..3");
                this.index = index; this.x = x; this.y = y; this.plane = plane;
                this.lastX = x; this.lastY = y; this.lastPlane = plane;
            }

            /** The tile this actor stood on before this tick's movement (910 {@code getLastWorldTile}). */
            public Builder previous(int previousX, int previousY, int previousPlane) {
                coordinate(previousX, "previousX"); coordinate(previousY, "previousY");
                if (previousPlane < 0 || previousPlane > 3)
                    throw new IllegalArgumentException("previousPlane must be 0..3");
                this.lastX = previousX; this.lastY = previousY; this.lastPlane = previousPlane;
                return this;
            }

            /** 910 {@code hasTeleported() || getNextWalkDirection() != -1}. */
            public Builder moved(boolean value) { this.moved = value; return this; }

            /** Merely tagging a snapshot does not mean a viewer was sent the force mask. */
            public Builder forceMovement(long generation) {
                if (generation < 0) throw new IllegalArgumentException("Negative force generation");
                this.forceMovementGeneration = generation;
                this.forceFinalX = this.forceFinalY = -1;
                return this;
            }

            /** The actual queued mask's absolute final XY, whose logical base the client adopts. */
            public Builder forceMovement(long generation, int finalX, int finalY) {
                forceMovement(generation);
                coordinate(finalX, "force finalX"); coordinate(finalY, "force finalY");
                this.forceFinalX = finalX; this.forceFinalY = finalY;
                return this;
            }

            /** Native interpolation owns XY for this waypoint; it does not change actor plane. */
            public Builder forceMovementArrival(long generation) {
                if (generation < 0) throw new IllegalArgumentException("Negative force arrival generation");
                this.forceMovementArrivalGeneration = generation;
                return this;
            }

            /**
             * {@link #MOVEMENT_WALK}, {@link #MOVEMENT_RUN} or {@link #MOVEMENT_TELEPORT}.
             *
             * <p>The 3-bit speed fields could carry 5, 6 or 7, but the client's speed table has
             * only the five entries at {@code 0x140C9C888} and the client forms
             * {@code &table[index]} without a bounds check, so anything above
             * {@link #MAX_SPEED_INDEX} would hand it a pointer into an unrelated datum. Refused
             * here rather than at the wire.
             */
            public Builder movementType(int value) {
                if (value < 0 || value > MAX_SPEED_INDEX)
                    throw new IllegalArgumentException("movementType must be a speed-table index 0.."
                            + MAX_SPEED_INDEX + "; the table at 0x140C9C888 has no further entries");
                this.movementType = value;
                return this;
            }

            /** False keeps the actor out of every viewer's local list (logged out, not yet running). */
            public Builder visible(boolean value) { this.visible = value; return this; }

            /**
             * The occupancy serial for {@link Actor#identity}. Callers that hand out reusable
             * player indices must supply a value that changes whenever the character behind an
             * index changes, and must never reuse one; 0 means "no identity tracking".
             */
            public Builder identity(long value) {
                if (value < 0) throw new IllegalArgumentException("An occupancy identity must not be negative");
                this.identity = value;
                return this;
            }

            /** Appearance body with an MD5 hash computed here. 1..255 bytes. */
            public Builder appearance(byte[] body) {
                return appearance(body, md5(body));
            }

            /**
             * Appearance body plus the hash the per-viewer cache compares. Pass the same hash the
             * 910 {@code Appearence.getMD5AppeareanceDataHash()} produced so an unchanged body is
             * suppressed across a reconnect-free session.
             */
            public Builder appearance(byte[] body, byte[] hash) {
                Objects.requireNonNull(body, "appearance");
                Objects.requireNonNull(hash, "appearanceHash");
                if (body.length < 1 || body.length > 255)
                    throw new IllegalArgumentException("Appearance requires 1..255 bytes");
                if (hash.length == 0) throw new IllegalArgumentException("Appearance hash must not be empty");
                this.appearance = body.clone();
                this.appearanceHash = hash.clone();
                return this;
            }

            /** Non-appearance masks for this tick, composed through {@link Native950PlayerMasks}. */
            public Builder masks(MaskSource source) { this.masks = source; return this; }

            public Actor build() { return new Actor(this); }
        }
    }

    // ------------------------------------------------------------------ view state

    /**
     * One viewer's view of the world: the local actor table, the two index lists, the slot flags
     * that alternate the passes, the per-slot region hashes, the per-viewer appearance MD5 cache
     * and the assigned local index.
     *
     * <p>World thread only. {@code Native950Viewport} is the session-facing owner.
     */
    public static final class ViewState {
        /** The index this session was admitted on; 1..2047 and never reused while it is attached. */
        public final int localIndex;
        private final Actor[] localActors = new Actor[SLOTS];
        private final int[] localIndexes = new int[SLOTS];
        private final int[] outIndexes = new int[SLOTS];
        private final int[] regionHashes = new int[SLOTS];
        /** Generation actually sent to this viewer, tied to the current actor occupancy. */
        private final long[] emittedForceGenerations = new long[SLOTS];
        private final int[] forceBaseX = new int[SLOTS], forceBaseY = new int[SLOTS];
        private final boolean[] forceBaseKnown = new boolean[SLOTS];
        /**
         * The speed-table index this viewer's client currently holds at {@code slot+0x28} for
         * each slot, or {@link #SPEED_TOKEN_UNKNOWN}.
         *
         * <p>The token is <b>sticky</b> ({@code verified/MOVEMENT_TABLES.md} section 3): the walk
         * form ({@code 0x140125B96}) and the run form ({@code 0x140125D30}) only read it and hand
         * it to the position-queue push {@code 0x140319130}; they never write it. Only the init
         * record ({@code 0x140125220}), the two absolute forms ({@code 0x140125E6D} short,
         * {@code 0x140125F90} long), the external region-hash type 3 ({@code 0x14012647D}) and
         * the walk form's extra-step branch ({@code 0x140125A7F}, hard-coded to entry 3) set it.
         *
         * <p>So a compact form renders at whatever speed the slot was last told, and this
         * encoder may only use one when the token already equals the speed it wants. Anything
         * else falls back to {@link #FORM_ABSOLUTE}, which both moves the player and refreshes
         * the token, so at most one frame per speed change pays for the wider form.
         */
        private final int[] speedTokens = new int[SLOTS];
        private final byte[] slotFlags = new byte[SLOTS];
        private final byte[][] appearanceHashes = new byte[SLOTS][];
        private int localCount;
        private int outCount;
        private int addedThisTick;
        private boolean initialised;
        private Visibility visibility = DISTANCE_ONLY;

        public ViewState(int localIndex) {
            if (localIndex < MIN_INDEX || localIndex > MAX_INDEX)
                throw new IllegalArgumentException("Player index must be " + MIN_INDEX + ".." + MAX_INDEX);
            this.localIndex = localIndex;
            Arrays.fill(speedTokens, SPEED_TOKEN_UNKNOWN);
        }

        /** Replaces the visibility policy; the session installs the map-region aware one. */
        public void setVisibility(Visibility policy) {
            this.visibility = Objects.requireNonNull(policy, "visibility");
        }

        /** True once {@link #initialScene} has built this viewer's first frame. */
        public boolean isInitialised() { return initialised; }
        /** Players currently in this viewer's local list, the local player included. */
        public int localCount() { return localCount; }
        /** Slots currently outside this viewer's local list. */
        public int outCount() { return outCount; }
        /** Adds emitted by the most recent {@link #frame}. */
        public int addedThisTick() { return addedThisTick; }
        /** True when this viewer's client currently holds a player in {@code index}. */
        public boolean isLocal(int index) { return localActors[index] != null; }
        /** The region hash this viewer's client currently believes {@code index} carries. */
        public int regionHash(int index) { return regionHashes[index]; }
        /** Diagnostic: latest force plan actually emitted for this actor occupancy. */
        public long emittedForceGeneration(int index) { return emittedForceGenerations[index]; }
        /** XY the client's logical path base currently holds, including native interpolation. */
        public int logicalX(int index) {
            if (localActors[index] == null) throw new IllegalArgumentException("No local actor at " + index);
            return forceBaseKnown[index] ? forceBaseX[index] : localActors[index].x;
        }
        public int logicalY(int index) {
            if (localActors[index] == null) throw new IllegalArgumentException("No local actor at " + index);
            return forceBaseKnown[index] ? forceBaseY[index] : localActors[index].y;
        }
        /**
         * The speed-table index this viewer's client currently holds at {@code slot+0x28} for
         * {@code index}, or {@link #SPEED_TOKEN_UNKNOWN} when nothing has told it one yet. A
         * compact walk or run form is only emitted when this already equals the speed that form
         * needs; see {@link #speedTokens}.
         */
        public int speedToken(int index) { return speedTokens[index]; }
        /** The pass-alternation flags for {@code index}; bit 0 is "skipped in the previous frame". */
        public int slotFlags(int index) { return slotFlags[index] & 0xFF; }
        /** True when this viewer has already been sent {@code index}'s current appearance. */
        public boolean hasCachedAppearance(int index) { return appearanceHashes[index] != null; }

        /**
         * Forgets the caches that died with the client's slot object for {@code index}. Call
         * when the world frees the slot, so a later occupant cannot inherit the previous
         * player's appearance cache.
         *
         * <p>The per-slot <b>region record is deliberately kept</b>. The client's local-remove
         * path at {@code 0x140125906..0x14012596A} clears only the entity handle
         * ({@code [record+0x30]} and {@code [record+0x38]}); the plane and the two region bytes
         * at {@code [record+0x00]}, {@code [record+0x04]} and {@code [record+0x08]} - the exact
         * fields the external parser reads and rewrites at {@code 0x140126489} /
         * {@code 0x140126494} / {@code 0x1401264A1} - are never touched, and the record object
         * itself survives (the end-of-tick loop at {@code 0x1401257F0} re-files it on the
         * external list). {@code regionHashes[index]} mirrors that client-side record, so
         * zeroing it here would make the next add at this index emit a delta computed from 0
         * while the client applies it on top of the hash it still holds, and the new occupant
         * would be drawn in the wrong region. The appearance cache is different: it belongs to
         * the slot object the client destroyed at {@code 0x140125906}, so it must go.
         *
         * <p>The <b>speed token is kept for the same reason as the region record</b>. It lives at
         * {@code slot+0x28} - on the slot object {@code [r14+0x10][index]}, not on the entity the
         * removal destroys ({@code rbx = [slot+0x38]}) - and the external parser writes it back
         * through that same slot pointer at {@code 0x14012647D}. Dropping it here would only cost
         * a wider form on the next move, but keeping it is what the client actually does.
         *
         * <p>The index lists are rebuilt whenever the local table changes, because
         * {@link #rotate} only runs at the end of a frame and the movement passes require
         * {@code localIndexes} and {@code localActors} to agree.
         */
        public void forget(int index) {
            if (index == localIndex) throw new IllegalArgumentException("A viewer cannot forget its own slot");
            appearanceHashes[index] = null;
            emittedForceGenerations[index] = 0;
            forceBaseKnown[index] = false;
            if (localActors[index] != null) {
                localActors[index] = null;
                rebuildIndexLists();
            }
        }

        /** Refills the two pass lists from the local table, in ascending index order. */
        private void rebuildIndexLists() {
            localCount = 0;
            outCount = 0;
            for (int index = MIN_INDEX; index < SLOTS; index++) {
                if (localActors[index] == null) outIndexes[outCount++] = index;
                else localIndexes[localCount++] = index;
            }
        }
    }

    // ------------------------------------------------------------------ initial scene

    /**
     * The first frame for a session: 30 tile-hash bits, then one 20-bit record for each of the
     * 2046 external slots, then the REBUILD_NORMAL scene header. Generalises
     * {@code Native950Packets.initialSinglePlayerScene}: the local player's index is whatever the
     * world assigned, and a slot that already holds a live player carries that player's region
     * hash and movement-speed index instead of the empty-slot constant.
     *
     * <p>Also seeds this viewer's state: the local list starts as just the local player, every
     * other index goes on the external list, and {@code regionHashes} starts at the hash this
     * record just published (0 for an empty slot), so the first add either agrees with it or
     * writes a region-hash delta.
     *
     * @param world one snapshot per live player, indexed by player index; slot 0 unused
     */
    public static Native950Packets.Packet initialScene(ViewState view, Actor[] world,
            int npcBits, int areaType, int hash1, int hash2) {
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(world, "world");
        if (world.length != SLOTS) throw new IllegalArgumentException("The world table must hold " + SLOTS + " slots");
        Actor local = world[view.localIndex];
        if (local == null)
            throw new IllegalStateException("The local player must occupy index " + view.localIndex + " before its scene is built");
        if (local.index != view.localIndex)
            throw new IllegalStateException("Actor index " + local.index + " is not in world slot " + view.localIndex);

        BitWriter bits = new BitWriter();
        bits.bits(30, local.tileHash());
        view.localCount = 0;
        view.outCount = 0;
        Arrays.fill(view.localActors, null);
        Arrays.fill(view.appearanceHashes, null);
        Arrays.fill(view.emittedForceGenerations, 0);
        Arrays.fill(view.forceBaseKnown, false);
        Arrays.fill(view.slotFlags, (byte) 0);
        Arrays.fill(view.speedTokens, SPEED_TOKEN_UNKNOWN);
        view.localActors[view.localIndex] = local;
        view.localIndexes[view.localCount++] = view.localIndex;
        view.regionHashes[view.localIndex] = local.regionHash();
        // The local player's own slot gets no 20-bit record - its 30 leading bits are a tile
        // hash with no speed field - so nothing here tells the client a speed for it. Every 950
        // site that assigns slot+0x28 (0x14012622f, 0x1401265f3, 0x14012661d, 0x140126740) sits
        // inside a movement form, and the local slot is never given one by this packet, so its
        // pointer is whatever the slot constructor left. That is why it stays SPEED_TOKEN_UNKNOWN
        // rather than being assumed: the cost is this session's own first move taking the
        // absolute form, and nothing after that.
        for (int index = MIN_INDEX; index < SLOTS; index++) {
            if (index == view.localIndex) continue;
            Actor other = world[index];
            int hash;
            int speed;
            if (other == null || !other.visible) {
                hash = 0;
                speed = EMPTY_SLOT_SPEED;
            } else {
                hash = other.regionHash();
                // Speed index 0 is NOT neutral and is never emitted. On 950 the speed table is at
                // 0x140C6EC88 (947: 0x140C9C888) and each movement form indexes it before storing
                // the entry pointer into slot+0x28: the 15-bit absolute form takes bits 12..14
                // (0x140126518 sar ecx,0xc / and ecx,7 then 0x14012651e lea rdi,[rcx*4] +
                // 0x14012652b add rdi,r13), and the 30-bit form reads 3 dedicated bits
                // (0x140126639 mov edx,3 then 0x140126654 lea r13,[r13+rax*4]).
                //
                // Index 0 is the entry the client treats as "no interpolation": 0x14012661a
                // compares the selected entry against entry 0 and 0x140126628 sete's the result
                // into the update call's boolean argument, and index 4 is folded onto the same
                // entry outright (0x1401265d6 cmp against &table[4], 0x1401265f3 stores &table[0]
                // and forces the argument to 1). NOTE the 947 comment this replaces claimed that
                // sete lands in [record+0x27]; on 950 it does not - it is passed to 0x14031d9c0.
                // [record+0x27] is instead the pass discriminator the four movement passes test
                // (0x140125b8f / 0x140125c9f / 0x140125db3 / 0x140125ea3), refilled at end of tick
                // from [record+0x26] by 0x140125fac..0x140125fbc, i.e. exactly this encoder's
                // slotFlags bit 0. An index of 0 would put the slot in the "previous skip set"
                // pass on the client while slotFlags starts at 0 here, and
                // the whole bit stream would desynchronise from the next frame on. Index 0 is also
                // the one value the evidence never pins - the shipped, client-verified scene
                // carries 3 in every record - so a live slot whose movement type truncates to 0
                // (MOVEMENT_TELEPORT & 3) is written with the pinned empty-slot index instead.
                speed = other.movementType & 3;
                if (speed == 0) speed = EMPTY_SLOT_SPEED;
            }
            bits.bits(20, hash | (speed << 18));
            // slotFlags was filled with zeros above, which is what the client derives for every
            // non-zero speed index; the clamp guarantees no record can disagree with it.
            view.regionHashes[index] = hash;
            // 0x140125190 takes (record>>18)&3 as the table index and 0x140125220 stores the
            // pointer it builds into slot+0x28, so this record is what the slot's speed token
            // holds until something writes it again.
            view.speedTokens[index] = speed;
            view.appearanceHashes[index] = null;
            view.outIndexes[view.outCount++] = index;
        }
        bits.align();
        byte[] stream = bits.bytes();
        if (stream.length != INITIAL_BIT_STREAM_BYTES)
            throw new IllegalStateException("Initial player bit stream must be " + INITIAL_BIT_STREAM_BYTES + " bytes");

        byte[] header = Native950Packets.rebuildScene(local.x >>> 3, local.y >>> 3,
                npcBits, areaType, hash1, hash2).payload();
        byte[] body = new byte[stream.length + header.length];
        System.arraycopy(stream, 0, body, 0, stream.length);
        System.arraycopy(header, 0, body, stream.length, header.length);
        view.initialised = true;
        view.addedThisTick = 0;
        return Native950Packets.packet(ServerPacket.REBUILD_NORMAL, body);
    }

    // ------------------------------------------------------------------ frame

    /**
     * One tick of PLAYER_INFO for one viewer: the four movement passes, then every pending
     * player's mask block in the order the passes registered them (the dispatch at
     * {@code 0x140106560} walks the pending index list and skips two bytes before each block,
     * which {@link Native950PlayerMasks#encodeWithSkippedPrefix} writes).
     *
     * @param world one snapshot per live player, indexed by player index; slot 0 unused
     */
    public static Native950Packets.Packet frame(ViewState view, Actor[] world) {
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(world, "world");
        if (world.length != SLOTS) throw new IllegalArgumentException("The world table must hold " + SLOTS + " slots");
        if (!view.initialised)
            throw new IllegalStateException("initialScene must build this viewer's first frame before any update frame");
        Actor viewer = world[view.localIndex];
        if (viewer == null)
            throw new IllegalStateException("The local player left index " + view.localIndex + " while its session was attached");

        BitWriter bits = new BitWriter();
        ByteArrayOutputStream blocks = new ByteArrayOutputStream();
        view.addedThisTick = 0;
        processLocal(view, world, viewer, bits, blocks, true);
        processLocal(view, world, viewer, bits, blocks, false);
        processOutside(view, world, viewer, bits, blocks, true);
        processOutside(view, world, viewer, bits, blocks, false);

        byte[] movement = bits.bytes();
        byte[] masks = blocks.toByteArray();
        if (movement.length + masks.length > MAX_BODY)
            throw new IllegalStateException("PLAYER_INFO body exceeds the variable-short frame limit");
        byte[] body = new byte[movement.length + masks.length];
        System.arraycopy(movement, 0, body, 0, movement.length);
        System.arraycopy(masks, 0, body, movement.length, masks.length);
        rotate(view);
        return Native950Packets.packet(ServerPacket.PLAYER_INFO, body);
    }

    /**
     * The tail of 910 {@code createPacketAndProcess}: shift each slot's flags right so this
     * frame's "was skipped" becomes the next frame's "previous skip", then rebuild the two index
     * lists from the local table.
     */
    private static void rotate(ViewState view) {
        for (int index = MIN_INDEX; index < SLOTS; index++)
            view.slotFlags[index] = (byte) ((view.slotFlags[index] & 0xFF) >> 1);
        view.rebuildIndexLists();
    }

    // ------------------------------------------------------------------ passes 1 and 2

    private static void processLocal(ViewState view, Actor[] world, Actor viewer,
            BitWriter out, ByteArrayOutputStream blocks, boolean previousSkipClear) {
        int skip = 0;
        for (int i = 0; i < view.localCount; i++) {
            int index = view.localIndexes[i];
            if (previousSkipClear == ((view.slotFlags[index] & 0x1) != 0)) continue;
            if (skip > 0) {
                skip--;
                Actor skipped = world[index];
                if (skipped != null) {
                    view.localActors[index] = skipped;
                    view.regionHashes[index] = skipped.regionHash();
                }
                view.slotFlags[index] |= 2;
                continue;
            }
            Actor live = world[index];
            Actor cached = view.localActors[index];
            Actor actor = live != null ? live : cached;
            if (actor == null)
                throw new IllegalStateException("Local slot " + index + " lost its actor between frames");
            if (index != view.localIndex && changedOccupant(cached, live)) {
                // Never the viewer's own slot: index reuse is about a slot this viewer WATCHES
                // being handed to someone else, and the local slot object is the one the client
                // draws the camera from. A destroy record there is not recoverable by a later add.
                // frame() already rejects a viewer that left its index, so reaching here with
                // index == localIndex would mean the same index holds a different Player object
                // than the cache - a relog into the reclaimed slot - and the right answer is to
                // fall through and encode it as the viewer, not to destroy it.
                //
                // The world handed this index to a different character while this viewer still
                // held the previous one. The client's slot object belongs to the character that
                // left, so it is destroyed first: 1 needs-update, 0 no mask, 00 type 0
                // (0x140125906), then the region-hash flag clear, because regionHashes[index]
                // still mirrors the record the client keeps for this slot and nothing about it
                // changed. rotate() only moves this index onto the external list at the end of
                // the frame, so the new occupant cannot also be added by this frame's external
                // passes; it is added on the next one.
                out.bits(1, 1);
                out.bits(1, 0);
                out.bits(2, 0);
                out.bits(1, 0);
                view.localActors[index] = null;
                view.appearanceHashes[index] = null;
                view.emittedForceGenerations[index] = 0;
                view.forceBaseKnown[index] = false;
                continue;
            }
            if (needsRemove(view, viewer, index, live)) {
                // 1 needs-update, 0 no mask, 00 type 0 -> the parser destroys the slot object
                // (0x140125906) and then reads one bit; when set it tail-calls the external
                // parser 0x140125FE0, whose 2-bit type carries the region-hash update.
                out.bits(1, 1);
                out.bits(1, 0);
                out.bits(2, 0);
                int last = actor.previousRegionHash();
                int hash = actor.regionHash();
                view.regionHashes[index] = last;
                if (hash == last) {
                    out.bits(1, 0);
                } else {
                    out.bits(1, 1);
                    regionHashUpdate(out, view, index, last, hash, actor.movementType);
                    view.regionHashes[index] = hash;
                }
                view.localActors[index] = null;
                // Appearance and interpolation belong to the destroyed actor, unlike region/speed records.
                view.appearanceHashes[index] = null;
                view.emittedForceGenerations[index] = 0;
                view.forceBaseKnown[index] = false;
                continue;
            }
            // Interpolation writes its final XY into the native logical path base even
            // before its last stage. Remember only what this viewer actually received.
            // Capture the old state before a replacement/cancellation mask changes it.
            boolean matchedForce = arrivalForKnownForce(view, actor);
            boolean knownBase = view.forceBaseKnown[index];
            int baseX = knownBase ? view.forceBaseX[index] : actor.lastX;
            int baseY = knownBase ? view.forceBaseY[index] : actor.lastY;
            view.localActors[index] = actor;
            view.regionHashes[index] = actor.regionHash();
            boolean needAppearance = needsAppearanceUpdate(view, actor, blocks.size());
            boolean needUpdate = needAppearance || actor.hasMasks();
            boolean newForce = needUpdate && appendBlock(view, actor, blocks, needAppearance);
            // An original force mask is rebased to final XY/current plane in appendBlock.
            // With no replacement, the old interpolation owns XY but never plane.
            boolean publishForce = newForce && actor.forceMovementGeneration != 0;
            boolean forceOwnsXY = matchedForce && !newForce;
            int wireX = publishForce ? actor.forceFinalX : actor.x;
            int wireY = publishForce ? actor.forceFinalY : actor.y;
            int xOffset = forceOwnsXY ? 0 : wireX - baseX;
            int yOffset = forceOwnsXY ? 0 : wireY - baseY;
            int planeOffset = actor.plane - actor.lastPlane;
            // The force sink retains one queued destination. Publish the final XY as an
            // ordinary queued step first, then rebase the force mask to that same tile.
            // WALK preserves the rendered origin; TELEPORT would snap before interpolation.
            int movementType = publishForce ? MOVEMENT_WALK : actor.movementType;
            boolean moveOnWire = publishForce || (actor.moved && (!forceOwnsXY || planeOffset != 0));
            if (actor.moved && knownBase && !newForce && !forceOwnsXY) {
                view.forceBaseX[index] = actor.x; view.forceBaseY[index] = actor.y;
            }
            if (moveOnWire) {
                int form = publishForce ? FORM_ABSOLUTE : movementForm(view, actor, xOffset, yOffset, planeOffset);
                out.bits(1, 1);
                out.bits(1, needUpdate ? 1 : 0);
                if (form == FORM_WALK) {
                    // 0x1401259E2 selects form 1; 0x1401259E8 reads 3 direction bits and
                    // 0x1401259F8 the extra-step flag. Neither path writes slot+0x28 unless the
                    // flag is set (0x140125A7F), and movementForm already required the token to
                    // be MOVEMENT_WALK, so the client renders this at the walk speed it holds.
                    out.bits(2, FORM_WALK);
                    out.bits(3, walkDirection(xOffset, yOffset));
                    // The flag is always clear. Setting it queues a SECOND position
                    // (base + cardinal from a further 2 bits at 0x140125A6C, then
                    // base + direction from the 3 bits above) and forces the slot to speed
                    // table entry 3. What that two-position form is for is labelled
                    // interpretation in MOVEMENT_TABLES.md section 2, and Ataraxia never needs
                    // it: Entity.processMovement (Entity.java:1533-1538) already refuses a
                    // second run step whose combined delta has no run-table entry, so every
                    // two-tile move this encoder sees is expressible as form 2.
                    out.bits(1, 0);
                } else if (form == FORM_RUN) {
                    // 0x140125BA8 selects form 2; 0x140125BB1 reads 4 direction bits. The whole
                    // run path only reads slot+0x28 (0x140125D30), so movementForm required the
                    // token to already be MOVEMENT_RUN.
                    out.bits(2, FORM_RUN);
                    out.bits(4, runDirection(xOffset, yOffset));
                } else if (Math.abs(xOffset) < 16 && Math.abs(yOffset) < 16) {
                    // 0x140125D57: 15 bits, y in 0..4, x in 5..9, plane in 10..11, speed in 12..14.
                    out.bits(2, FORM_ABSOLUTE);
                    out.bits(1, 0);
                    int packedX = xOffset < 0 ? xOffset + 32 : xOffset;
                    int packedY = yOffset < 0 ? yOffset + 32 : yOffset;
                    out.bits(15, (packedY + (packedX << 5) + ((planeOffset & 0x3) << 10))
                            | (movementType << 12));
                    // 0x140125E6D stores &table[(v>>12)&7] into slot+0x28 on the ordinary path;
                    // on the teleport index the branch at 0x140125E41 instead resets the token
                    // to &table[0] at 0x140125E43 and repositions through vtable+0x160.
                    view.speedTokens[index] = movementType == MOVEMENT_TELEPORT
                            ? 0 : movementType;
                } else {
                    // 0x140125E89: 3 speed bits then 30 position bits.
                    out.bits(2, FORM_ABSOLUTE);
                    out.bits(1, 1);
                    out.bits(3, movementType);
                    out.bits(30, (yOffset & 0x3FFF) + ((xOffset & 0x3FFF) << 14)
                            + ((planeOffset & 0x3) << 28));
                    // 0x140125F90 stores the token, but only on the branch the teleport index
                    // never reaches (0x140125F59 jumps straight to the vtable+0x160 reposition
                    // at 0x140125F75), so a long-form teleport leaves the token as it was.
                    if (movementType != MOVEMENT_TELEPORT)
                        view.speedTokens[index] = movementType;
                }
            } else if (needUpdate) {
                // 0x1401258E9: type 0 with the pending-mask bit set is a mask-only update.
                out.bits(1, 1);
                out.bits(1, 1);
                out.bits(2, 0);
            } else {
                out.bits(1, 0);
                for (int j = i + 1; j < view.localCount; j++) {
                    int other = view.localIndexes[j];
                    if (previousSkipClear == ((view.slotFlags[other] & 0x1) != 0)) continue;
                    Actor otherLive = world[other];
                    Actor otherCached = view.localActors[other];
                    Actor otherActor = otherLive != null ? otherLive : otherCached;
                    if (otherActor == null || changedOccupant(otherCached, otherLive)
                            || needsRemove(view, viewer, other, otherLive)
                            || requiresMovement(view, otherActor) || otherActor.hasMasks()
                            || needsAppearanceUpdate(view, otherActor, blocks.size())) break;
                    skip++;
                }
                putSkip(out, skip);
                view.slotFlags[index] |= 2;
            }
        }
        out.align();
    }

    // ------------------------------------------------------------------ passes 3 and 4

    private static void processOutside(ViewState view, Actor[] world, Actor viewer,
            BitWriter out, ByteArrayOutputStream blocks, boolean previousSkipSet) {
        int skip = 0;
        for (int i = 0; i < view.outCount; i++) {
            int index = view.outIndexes[i];
            if (previousSkipSet == ((view.slotFlags[index] & 0x1) == 0)) continue;
            if (skip > 0) {
                skip--;
                view.slotFlags[index] |= 2;
                continue;
            }
            Actor actor = world[index];
            if (needsAdd(view, viewer, index, actor)) {
                // 0x140125FE0 type 0: optional region-hash update, 6 X bits, 6 Y bits, mask bit.
                out.bits(1, 1);
                out.bits(2, 0);
                int hash = actor.regionHash();
                if (hash == view.regionHashes[index]) {
                    out.bits(1, 0);
                } else {
                    out.bits(1, 1);
                    regionHashUpdate(out, view, index, view.regionHashes[index], hash, actor.movementType);
                    view.regionHashes[index] = hash;
                }
                out.bits(6, actor.xInRegion());
                out.bits(6, actor.yInRegion());
                boolean needAppearance = needsAppearanceUpdate(view, actor, blocks.size());
                boolean needUpdate = needAppearance || actor.hasMasks();
                if (needUpdate) appendBlock(view, actor, blocks, needAppearance);
                out.bits(1, needUpdate ? 1 : 0);
                view.addedThisTick++;
                view.localActors[index] = actor;
                view.slotFlags[index] |= 2;
            } else {
                out.bits(1, 0);
                for (int j = i + 1; j < view.outCount; j++) {
                    int other = view.outIndexes[j];
                    if (previousSkipSet == ((view.slotFlags[other] & 0x1) == 0)) continue;
                    if (needsAdd(view, viewer, other, world[other])) break;
                    skip++;
                }
                putSkip(out, skip);
                view.slotFlags[index] |= 2;
            }
        }
        out.align();
    }

    // ------------------------------------------------------------------ decisions

    /**
     * True when the world slot this viewer holds locally is now occupied by a different
     * character. Both identities must be non-zero: 0 is "no identity tracking", which is what
     * unit fixtures and any caller that never reuses an index carry.
     */
    private static boolean changedOccupant(Actor cached, Actor live) {
        return cached != null && live != null && cached.identity != 0 && live.identity != 0
                && cached.identity != live.identity;
    }

    private static boolean needsRemove(ViewState view, Actor viewer, int index, Actor live) {
        if (index == view.localIndex) return false;
        return live == null || !live.visible || !view.visibility.visible(viewer, live);
    }

    private static boolean needsAdd(ViewState view, Actor viewer, int index, Actor actor) {
        if (index == view.localIndex || actor == null || !actor.visible) return false;
        // A newly created actor has no rendered origin for the queued-final/force sequence.
        // Defer only its original force-mask frame; next tick adds its authoritative tile,
        // and this viewer receives normal waypoint deltas because it never got that plan.
        if (actor.forceMovementGeneration != 0) return false;
        if (view.addedThisTick >= MAX_PLAYER_ADD) return false;
        return view.visibility.visible(viewer, actor);
    }

    /** Only recipients of this generation let interpolation own the waypoint's XY. */
    private static boolean arrivalForKnownForce(ViewState view, Actor actor) {
        return actor.forceMovementArrivalGeneration != 0
                && view.emittedForceGenerations[actor.index] == actor.forceMovementArrivalGeneration;
    }

    private static boolean requiresMovement(ViewState view, Actor actor) {
        return actor.moved && (!arrivalForKnownForce(view, actor) || actor.plane != actor.lastPlane);
    }

    /** 910 {@code needAppearenceUpdate}: unchanged bodies are suppressed per viewer. */
    private static boolean needsAppearanceUpdate(ViewState view, Actor actor, int blockBytes) {
        if (actor.appearance == null) return false;
        if (blockBytes > APPEARANCE_BUDGET) return false;
        byte[] cached = view.appearanceHashes[actor.index];
        return cached == null || !MessageDigest.isEqual(cached, actor.appearanceHash);
    }

    /**
     * Appends one pending player's mask block, two skipped bytes included. Every byte is composed
     * through {@link Native950PlayerMasks}, so the confirmed-mask allow-list, the header extension
     * markers and the client consumption order are applied in exactly one place, and a CANDIDATE
     * mask throws out of here instead of reaching the wire.
     */
    private static boolean appendBlock(ViewState view, Actor actor, ByteArrayOutputStream blocks,
            boolean appearance) {
        Native950PlayerMasks.Builder builder = actor.masks == null
                ? Native950PlayerMasks.builder() : actor.masks.masks(view.localIndex);
        if (builder == null)
            throw new IllegalStateException("Actor " + actor.index + " offered a null mask builder");
        if (appearance) {
            builder.appearance(actor.appearance);
            view.appearanceHashes[actor.index] = actor.appearanceHash.clone();
        }
        Native950PlayerMasks.Update update = builder.build();
        boolean force = (update.maskBits() & Native950PlayerMasks.FORCE_MOVEMENT) != 0;
        if (force && actor.forceMovementGeneration != 0) {
            if (actor.forceFinalX < 0)
                throw new IllegalArgumentException("An emitted force generation needs its verified final XY");
            // Only the force block changes basis; appearance and other masks keep their data.
            update = update.rebaseForceMovement(actor.x - actor.forceFinalX, actor.y - actor.forceFinalY);
        }
        byte[] block = Native950PlayerMasks.encodeWithSkippedPrefix(update);
        blocks.write(block, 0, block.length);
        if (force) {
            // A stationary clear has no live generation and leaves the aligned frame tile
            // as the logical base. An original plan promises its final XY to this viewer.
            view.emittedForceGenerations[actor.index] = actor.forceMovementGeneration;
            view.forceBaseKnown[actor.index] = actor.forceMovementGeneration != 0;
            if (view.forceBaseKnown[actor.index]) {
                view.forceBaseX[actor.index] = actor.forceFinalX;
                view.forceBaseY[actor.index] = actor.forceFinalY;
            }
        }
        return force;
    }

    // ------------------------------------------------------------------ shared bit forms

    /**
     * 910 {@code skipPlayers}: a 2-bit selector then 0, 5, 8 or 11 count bits. The count is the
     * number of <b>subsequent</b> eligible slots the client should leave untouched
     * (PLAYER_INFO.md "Movement prefix").
     */
    private static void putSkip(BitWriter out, int amount) {
        if (amount < 0 || amount > 2047) throw new IllegalArgumentException("Skip run must be 0..2047");
        out.bits(2, amount == 0 ? 0 : amount > 255 ? 3 : (amount > 31 ? 2 : 1));
        if (amount > 0) out.bits(amount > 255 ? 11 : (amount > 31 ? 8 : 5), amount);
    }

    /**
     * 910 {@code updateRegionHash}, verified against the external parser: type 1 reads 2 plane
     * bits ({@code 0x14012631B}), type 2 reads 5 bits as {@code plane<<3 | opcode}
     * ({@code 0x140126349}) with the opcode table 0 = (-1,-1) .. 7 = (+1,+1), and type 3 reads
     * 20 bits as {@code y | x<<8 | plane<<16 | speed<<18} ({@code 0x14012641F}).
     *
     * <p>Only type 3 carries a speed field, and only type 3 writes the slot's speed token: the
     * client rebuilds the pointer at {@code 0x140126454..0x14012647D} and stores it to
     * {@code slot+0x28}. Types 1 and 2 leave the token alone, so this method records the change
     * for exactly the one selector that makes it.
     */
    private static void regionHashUpdate(BitWriter out, ViewState view, int index,
            int lastHash, int currentHash, int movementType) {
        int lastRegionX = (lastHash >> 8) & 0xFF;
        int lastRegionY = lastHash & 0xFF;
        int lastPlane = (lastHash >> 16) & 0x3;
        int currentRegionX = (currentHash >> 8) & 0xFF;
        int currentRegionY = currentHash & 0xFF;
        int currentPlane = (currentHash >> 16) & 0x3;
        int planeOffset = currentPlane - lastPlane;
        if (lastRegionX == currentRegionX && lastRegionY == currentRegionY) {
            out.bits(2, 1);
            out.bits(2, planeOffset & 0x3);
            return;
        }
        int dx = currentRegionX - lastRegionX;
        int dy = currentRegionY - lastRegionY;
        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
            int opcode;
            if (dx == -1 && dy == -1) opcode = 0;
            else if (dx == 1 && dy == -1) opcode = 2;
            else if (dx == -1 && dy == 1) opcode = 5;
            else if (dx == 1 && dy == 1) opcode = 7;
            else if (dy == -1) opcode = 1;
            else if (dx == -1) opcode = 3;
            else if (dx == 1) opcode = 4;
            else opcode = 6;
            out.bits(2, 2);
            out.bits(5, ((planeOffset & 0x3) << 3) + (opcode & 0x7));
            return;
        }
        out.bits(2, 3);
        out.bits(20, (dy & 0xFF) + ((dx & 0xFF) << 8) + ((planeOffset & 0x3) << 16)
                + ((movementType & 0x3) << 18));
        view.speedTokens[index] = movementType & 0x3;
    }

    // ------------------------------------------------------------------ movement forms

    /**
     * Which local movement form this actor's net displacement may be written with. This is the
     * 910 decision, taken from the same numbers: 910's own walk/run branch (the block commented
     * out at {@code LocalPlayerUpdate.java:284-300}) sums the deltas of
     * {@code getNextWalkDirection()} and {@code getNextRunDirection()} and asks
     * {@code Utils.getPlayerRunningDirection} when a run step was taken and
     * {@code Utils.getPlayerWalkingDirection} otherwise. That sum <b>is</b>
     * {@code x - lastX, y - lastY}: {@code Entity.processMovement} snapshots
     * {@code lastWorldTile} before the step loop (Entity.java:1469) and each step applies the
     * same {@code DIRECTION_DELTA} pair through {@code moveLocation}. And
     * {@code Player.getMovementType()} (Player.java:3201) is {@link #MOVEMENT_RUN} exactly when a
     * run step was taken (or another is queued), {@link #MOVEMENT_WALK} otherwise, which is the
     * same discriminator 910 used. So a one-tile step is a walk and a two-tile step is a run,
     * decided from the real Player, without this class needing the two direction fields.
     *
     * <p>Four things force the absolute form instead, and each is a fact about the parser:
     *
     * <ul>
     *   <li><b>A teleport.</b> {@link #MOVEMENT_TELEPORT} is speed index 4, which the client
     *       address-compares at {@code 0x140125E26}/{@code 0x140125F59} and answers with an
     *       instant reposition through {@code vtable+0x160}. Only the absolute forms carry a
     *       speed field, so only they can say it.</li>
     *   <li><b>A plane change.</b> Neither compact form touches the record's plane - the walk
     *       form re-writes the current one ({@code 0x140125B6D}) and the run form likewise
     *       ({@code 0x140125D16}) - so a plane offset has no compact encoding.</li>
     *   <li><b>A displacement with no table entry.</b> Chebyshev 1 is the walk table, Chebyshev 2
     *       is the run table, and the run table has no interior; anything else, including a
     *       stationary "moved" actor, falls back rather than guessing.</li>
     *   <li><b>A speed token that does not already agree.</b> The compact forms never write
     *       {@code slot+0x28}, so they render at whatever speed the slot was last told; see
     *       {@link ViewState#speedTokens}.</li>
     * </ul>
     */
    private static int movementForm(ViewState view, Actor actor, int dx, int dy, int planeOffset) {
        if (planeOffset != 0) return FORM_ABSOLUTE;
        if (actor.movementType == MOVEMENT_TELEPORT) return FORM_ABSOLUTE;
        if (view.speedTokens[actor.index] != actor.movementType) return FORM_ABSOLUTE;
        if (actor.movementType == MOVEMENT_WALK && walkDirection(dx, dy) >= 0) return FORM_WALK;
        if (actor.movementType == MOVEMENT_RUN && runDirection(dx, dy) >= 0) return FORM_RUN;
        return FORM_ABSOLUTE;
    }

    /**
     * The verified 3-bit walk direction for a one-tile step, or -1 when the delta has no entry
     * in the client's table. Agrees value for value with 910
     * {@code Utils.getPlayerWalkingDirection}.
     */
    public static int walkDirection(int dx, int dy) {
        if (dx < -1 || dx > 1 || dy < -1 || dy > 1) return -1;
        return WALK_DIRECTIONS[dy + 1][dx + 1];
    }

    /**
     * The verified 4-bit run direction for a two-tile step, or -1 when the delta has no entry in
     * the client's table. Every entry is Chebyshev 2, so a two-tile move that turns a corner
     * returns -1 here and must be written with the absolute form. Agrees value for value with 910
     * {@code Utils.getPlayerRunningDirection}.
     */
    public static int runDirection(int dx, int dy) {
        if (dx < -2 || dx > 2 || dy < -2 || dy > 2) return -1;
        return RUN_DIRECTIONS[dy + 2][dx + 2];
    }

    // ------------------------------------------------------------------ helpers

    /** 910 {@code WorldTile.getRegionHash()}. */
    public static int regionHash(int x, int y, int plane) {
        return (y >> 6) + ((x >> 6) << 8) + (plane << 16);
    }

    /** MD5 of an appearance body, for callers with no 910 {@code Appearence} to ask. */
    public static byte[] md5(byte[] body) {
        Objects.requireNonNull(body, "body");
        try {
            return MessageDigest.getInstance("MD5").digest(body);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("MD5 is required by every Java 8 runtime", impossible);
        }
    }

    private static void coordinate(int n, String field) {
        if (n < 0 || n > 16383) throw new IllegalArgumentException(field + " must fit 14 bits");
    }

    /**
     * Big-endian bit writer. Every pass rounds up to a byte boundary when it ends
     * (PLAYER_INFO.md: "Every pass starts at byte cursor * 8 and rounds back up to a byte
     * boundary afterwards"), which is what {@link #align()} does; the padding bits are zero and
     * the parser never reads them.
     */
    private static final class BitWriter {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private int pending;
        private int pendingBits;

        void bits(int count, int value) {
            if (count < 1 || count > 32) throw new IllegalArgumentException("Bit count must be 1..32");
            for (int bit = count - 1; bit >= 0; bit--) {
                pending = (pending << 1) | ((value >>> bit) & 1);
                if (++pendingBits == 8) {
                    out.write(pending);
                    pending = 0;
                    pendingBits = 0;
                }
            }
        }

        void align() {
            if (pendingBits == 0) return;
            out.write(pending << (8 - pendingBits));
            pending = 0;
            pendingBits = 0;
        }

        byte[] bytes() {
            align();
            return out.toByteArray();
        }
    }
}
