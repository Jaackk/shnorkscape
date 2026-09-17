package com.rs.game.player.client;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Actor;
import com.rs.network.protocol.modern950.Native950PlayerInfo.Visibility;

import java.util.Objects;

/**
 * One native 947 session's view of the player world: the 947 counterpart of the 910
 * {@code LocalPlayerUpdate} fields and of OpenNXT's {@code Viewport}. It owns the local actor
 * table, the two index lists, the pass-alternation slot flags, the per-slot region hashes and
 * the per-viewer appearance MD5 cache (all inside
 * {@link com.rs.network.protocol.modern950.Native950PlayerInfo.ViewState}), plus the index the
 * world admitted this session on.
 *
 * <p><b>World thread only.</b> Construction and every mutating call are checked against the
 * thread that created the viewport, because the state is read while another session's frame is
 * being built in the same tick. A call from any other thread is a defect, not a race to be
 * tolerated, so it throws.
 *
 * <p>This class holds no reference to a {@code Player} and reads no cache, so it can be
 * exercised without a world. The session (or the world's frame phase) converts each live player
 * into one {@link Actor} snapshot per tick, publishes them in an {@code Actor[2048]} table
 * indexed by player index, and hands that same table to every viewport in the tick; see
 * {@code notes/P6-playerinfo.md} for the recipe.
 *
 * <p>Ordinary use per tick, after all movement has been processed and before masks are reset:
 * <pre>
 *   Actor[] world = ...;                       // one snapshot per live player
 *   channel.write(viewport.frame(world));      // PLAYER_INFO for this session
 * </pre>
 */
public final class Native950Viewport {

    private final Native950PlayerInfo.ViewState state;
    private final Thread owner;
    private long frames;
    private long adds;
    private long removes;

    /** Binds the viewport to the calling thread, which must be the world thread. */
    public Native950Viewport(int localIndex) {
        this(localIndex, Thread.currentThread());
    }

    public Native950Viewport(int localIndex, Thread owner) {
        this.state = new Native950PlayerInfo.ViewState(localIndex);
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    /** The player index the world assigned this session; stable for the life of the session. */
    public int localIndex() {
        return state.localIndex;
    }

    /**
     * Installs the visibility policy. The default is
     * {@link Native950PlayerInfo#DISTANCE_ONLY}; a session that knows which map regions its
     * client currently holds should install the 910 test (radius plus
     * {@code getMapRegionsIds().contains(getRegionId())}), because a player inside the radius
     * but outside the loaded regions has nowhere to be drawn.
     */
    public void setVisibility(Visibility policy) {
        checkThread();
        state.setVisibility(policy);
    }

    /**
     * The session's first frame: the 30-bit tile hash, the 2046 external 20-bit init records and
     * the scene header. Generalises {@code Native950Packets.initialSinglePlayerScene} to the
     * assigned index and to slots that already hold live players.
     */
    public Native950Packets.Packet initialScene(Actor[] world, int npcBits, int areaType,
            int hash1, int hash2) {
        checkThread();
        return Native950PlayerInfo.initialScene(state, world, npcBits, areaType, hash1, hash2);
    }

    /** One tick of PLAYER_INFO for this session. */
    public Native950Packets.Packet frame(Actor[] world) {
        checkThread();
        int before = trackedPlayers();
        Native950Packets.Packet packet = Native950PlayerInfo.frame(state, world);
        int added = state.addedThisTick();
        frames++;
        adds += added;
        // The local list only gains through adds and only loses through removes.
        removes += before + added - trackedPlayers();
        return packet;
    }

    /**
     * Forgets a freed slot so a later occupant of the same index cannot inherit this viewer's
     * cached appearance hash. Call when the world releases the index, not when a player merely
     * walks out of view (the encoder handles that), and never for an index the world has
     * already handed to another character - that case is a removal plus a fresh add, which the
     * encoder produces from the occupancy identity.
     *
     * <p>The cached region hash survives, because the client's per-slot region record survives
     * the removal; see {@code Native950PlayerInfo.ViewState.forget}.
     */
    public void forget(int index) {
        checkThread();
        state.forget(index);
    }

    /** True once {@link #initialScene} has run for this session. */
    public boolean isInitialised() {
        return state.isInitialised();
    }

    /** Players this viewer's client currently holds, the local player included. */
    public int trackedPlayers() {
        return state.localCount();
    }

    /** True when this viewer's client currently holds a player in {@code index}. */
    public boolean tracks(int index) {
        return state.isLocal(index);
    }

    /** Adds emitted by the most recent {@link #frame}. */
    public int addedLastFrame() {
        return state.addedThisTick();
    }

    /** The region hash this viewer's client currently believes {@code index} carries. */
    public int regionHash(int index) {
        return state.regionHash(index);
    }

    /**
     * The movement-speed table index this viewer's client currently holds at {@code slot+0x28}
     * for {@code index}, or {@link Native950PlayerInfo#SPEED_TOKEN_UNKNOWN}.
     *
     * <p>The token is sticky: the compact walk and run forms only read it, so the encoder emits
     * one of them only while this already equals the speed that form needs, and otherwise falls
     * back to the absolute form, which refreshes it. Diagnostics and tests only.
     */
    public int speedToken(int index) {
        return state.speedToken(index);
    }

    /**
     * The pass-alternation flags for {@code index}; bit 0 is "skipped in the previous frame",
     * the byte the client keeps at {@code [record+0x27]}. Diagnostics and tests only.
     */
    public int slotFlags(int index) {
        return state.slotFlags(index);
    }

    /** True when this viewer has already been sent {@code index}'s current appearance body. */
    public boolean hasCachedAppearance(int index) {
        return state.hasCachedAppearance(index);
    }

    /** Diagnostics for the session snapshot. */
    public State snapshot() {
        return new State(state.localIndex, frames, adds, removes, trackedPlayers(),
                state.isInitialised());
    }

    /** Exposed for the encoder's own callers; never hand this to another thread. */
    Native950PlayerInfo.ViewState viewState() {
        checkThread();
        return state;
    }

    private void checkThread() {
        if (Thread.currentThread() != owner)
            throw new IllegalStateException("Native950Viewport for index " + state.localIndex
                    + " is world-thread state; it was touched from " + Thread.currentThread().getName());
    }

    /** Immutable counters; callers never receive the mutable view state. */
    public static final class State {
        public final int localIndex, trackedPlayers;
        public final long frames, adds, removes;
        public final boolean initialised;

        State(int localIndex, long frames, long adds, long removes, int trackedPlayers,
                boolean initialised) {
            this.localIndex = localIndex;
            this.frames = frames;
            this.adds = adds;
            this.removes = removes;
            this.trackedPlayers = trackedPlayers;
            this.initialised = initialised;
        }

        @Override public String toString() {
            return "Viewport[index=" + localIndex + ",tracked=" + trackedPlayers
                    + ",frames=" + frames + ",adds=" + adds + ",removes=" + removes
                    + ",initialised=" + initialised + "]";
        }
    }
}
