package com.rs.game.player.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950NpcInfo;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.Utils;

import io.netty.channel.Channel;

/**
 * One connection's view of the world's NPCs: the game-side adapter that turns
 * world {@link NPC} entities into {@link Native950NpcInfo.NpcState} records and
 * writes one NPC_INFO frame per tick.
 *
 * <p>It is the N-NPC replacement for {@code Native950NpcView}, which owned a
 * single banker and one of the three literal single-entity writers. Everything
 * bit-level lives in {@link Native950NpcInfo}; this class only decides which
 * NPCs a viewer can see, what each of them did this tick, and which of them may
 * send a mask.
 *
 * <p><b>Index authority.</b> The world owns NPC indices: an NPC is reported with
 * whatever index {@code EntityList} gave it, unchanged. EntityList reuses the
 * lowest free index, so a viewer that still holds a removed NPC could otherwise
 * be handed a different NPC under the same number. Each visible NPC therefore
 * gets a per-viewport identity serial, and the encoder treats a changed identity
 * at a known index as a removal followed by a fresh addition on a later tick.
 *
 * <p><b>Ordering.</b> Offsets are relative to the viewer's post-movement tile,
 * so this must run after the world's movement phase and after PLAYER_INFO has
 * been written for the same tick.
 *
 * <p><b>Forced movement.</b>950 addition offsets use the last queued path step
 * (0x140120358..3A8), or actor+0x270 through virtual+b0/0x140321900 when the queue
 * is empty. Forced interpolation copies its FINAL endpoint to+0x270 even before
 * arrival (0x140320ACD,0x140320BF9), while rendering interpolates separately.
 * New/re-added NPC records are therefore deferred while the viewer's force plan
 * is active, including its original mask tick. Retained masks/movement and removals
 * continue normally. Terminal arrival or cancellation clears the active flag and
 * restores ordinary additions against the authoritative post-movement tile. This
 * deliberately does not guess the client packet/interpolation order inside the
 * first force tick; newly visible NPCs can appear after that movement completes.
 *
 * <p><b>Masks.</b> A {@link MaskSource} can only build blocks that
 * {@link Native950NpcMasks} confirms - the candidate blocks throw rather than
 * encode. P6 shipped with a source that sent nothing; M4 installs
 * {@link Native950EntityMasks} as the default, which reads the NPC's real 910
 * state (animation, face coordinate, transform, forced speech) and stays silent
 * for everything whose evidence is CANDIDATE. A stationary NPC that did nothing
 * this tick still produces no mask at all, so the P6 static-banker frames are
 * unchanged.
 */
public final class Native950NpcViewport {

    /**
     * Chooses the confirmed mask blocks one NPC should send to one viewer this
     * tick, or null for no update. It may only use the builders on
     * {@link Native950NpcMasks.Update}; the candidate ones refuse.
     */
    public interface MaskSource {
        Native950NpcMasks.Update update(Player viewer, NPC npc, boolean added);
    }

    /** Sends nothing at all; kept for a caller that wants the P6 silence back. */
    public static final MaskSource NO_MASKS = new MaskSource() {
        @Override public Native950NpcMasks.Update update(Player viewer, NPC npc, boolean added) { return null; }
    };

    /**
     * The M4 default: the NPC's own 910 state, translated by
     * {@link Native950EntityMasks#npcMasks(NPC, boolean, Player)}. Combat hit appearance is selected per
     * viewer; shared entity state is not consumed while building a frame. Returns null when no confirmed mask
     * applies.
     */
    public static final MaskSource ENTITY_STATE = new MaskSource() {
        @Override public Native950NpcMasks.Update update(Player viewer, NPC npc, boolean added) {
            return Native950EntityMasks.npcMasks(npc, added, viewer);
        }
    };

    private final Thread owner = Thread.currentThread();
    private final Native950NpcInfo encoder;
    private final MaskSource masks;
    private final Map<NPC, Long> identities = new IdentityHashMap<NPC, Long>();
    private long nextIdentity = 1;
    private long frames;
    /** NPCs this viewer was not shown because their current id is not a "same" 947 id. */
    private volatile long unclassified;

    public Native950NpcViewport() {
        this(Native950NpcInfo.DEFAULT_VIEW_DISTANCE, Native950NpcInfo.MAX_LOCAL_NPCS, null);
    }

    /** A null {@code masks} installs {@link #ENTITY_STATE}, the M4 default. */
    public Native950NpcViewport(int viewDistance, int localLimit, MaskSource masks) {
        this.encoder = new Native950NpcInfo(viewDistance, localLimit);
        this.masks = masks == null ? ENTITY_STATE : masks;
    }

    /**
     * Writes this tick's NPC_INFO for the viewer, collecting candidates from the
     * regions the viewer has loaded. Call after the movement phase.
     */
    public void synchronize(Player viewer, Channel channel, int npcBits, boolean rebuilt) {
        synchronize(viewer, channel, npcBits, rebuilt, nearbyNpcs(viewer));
    }

    /**
     * The same frame from an explicit candidate list, so a caller that already
     * knows the visible set (or a test) does not pay for a region scan. The list
     * order decides addition order and therefore mask-section order.
     */
    public void synchronize(Player viewer, Channel channel, int npcBits, boolean rebuilt, List<NPC> candidates) {
        checkThread();
        Objects.requireNonNull(channel, "channel");
        channel.write(frame(viewer, npcBits, rebuilt, candidates));
    }

    /** The unframed packet, for callers that batch their own writes. */
    public Native950Packets.Packet frame(Player viewer, int npcBits, boolean rebuilt, List<NPC> candidates) {
        checkThread();
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(candidates, "candidates");
        List<Native950NpcInfo.NpcState> states =
                new ArrayList<Native950NpcInfo.NpcState>(candidates.size());
        for (NPC npc : candidates) {
            // A rebuild republishes the whole list, so every NPC is an addition again.
            Native950NpcInfo.NpcState state = describe(viewer, npc, rebuilt);
            if (state != null) states.add(state);
        }
        Native950Packets.Packet packet = encoder.encode(viewer.getX(), viewer.getY(), viewer.getPlane(),
                npcBits, rebuilt, states);
        forgetUnseen(candidates);
        frames++;
        return packet;
    }

    /**
     * Describes one NPC for this viewer, or null when it must not appear at all.
     * A finished, hidden-dead or unregistered NPC is skipped, which makes the encoder
     * remove it from the retained list on this very tick.
     */
    private Native950NpcInfo.NpcState describe(Player viewer, NPC npc, boolean rebuilt) {
        if (npc == null || npc.hasFinished()
                || (npc.isDead() && !(npc.isNative950() && npc.isNative950DeathVisible()))) return null;
        int index = npc.getIndex();
        if (index < 0 || index >= 65535) return null;
        if (World.getNPCs().get(index) != npc) return null;
        // The addition record carries this id as a 947 definition id, and getId() is mutable:
        // 910 content that transforms an NPC (NPC.setNextNPCTransformation / transformIntoNPC,
        // both of which call setNPC(id)) replaces it with a raw 910 id that never went through
        // the spawner's validity gate. An id the table does not call "same" is dropped from the
        // published list rather than added under a definition this port cannot vouch for.
        if (!npc.isNative950DiagnosticDefinition() && !npc.isNative950Conjure()
                && !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC, npc.getId())) {
            unclassified++;
            return null;
        }
        boolean added = rebuilt || !encoder.contains(index);
        if (added && viewer.isNative950ForceMovementActive()) return null;
        Native950NpcInfo.NpcState.Builder builder = Native950NpcInfo.NpcState.npc(
                index, identityOf(npc), npc.getId(), npc.getX(), npc.getY(), npc.getPlane());
        builder.facing(facing(npc));
        if (npc.hasTeleported()) builder.teleported();
        else applyMovement(builder, npc);
        Native950NpcMasks.Update update = masks.update(viewer, npc, added);
        if (update != null) builder.update(update);
        return builder.build();
    }

    /**
     * Maps the world's walk and run steps onto the retained forms. The 910 walk
     * and run direction indices are translated through the verified NPC direction
     * table, which is clockwise from north and is NOT the player table; deriving
     * the selector from the tile delta makes a bad direction fail loudly here
     * instead of misplacing the NPC on the client.
     */
    private static void applyMovement(Native950NpcInfo.NpcState.Builder builder, NPC npc) {
        int walk = npc.getNextWalkDirection();
        if (walk == -1) return;
        int run = npc.getNextRunDirection();
        if (run == -1) builder.walk(selector(walk));
        else builder.run(selector(walk), selector(run));
    }

    /**
     * The addition record's three-bit facing field, from the NPC's own direction.
     *
     * <p>Ataraxia keeps facings in 1/16384 of a turn and its 910 {@code LocalNPCUpdate} writes
     * {@code (direction >> 11) - 4} into the same three bits. That transform is CONFIRMED
     * against this client (`verified/MOVEMENT_TABLES.md` section 4): the four the server
     * subtracts and the 180 degrees the client's decode adds cancel exactly, so the field
     * round-trips the direction truncated to a multiple of 2048 units with no residue. The
     * subtraction can go negative, and 910 relied on {@code writeBits} truncating it, so the
     * mask is explicit here - the builder validates 0..7 and would otherwise throw.
     *
     * <p>Before this, every NPC was added with 0, which is not a "no facing" sentinel: a
     * three-bit field has no such value, and 0 means north. So the whole world faced north.
     */
    private static int facing(NPC npc) {
        return facingField(npc.getDirection());
    }

    /**
     * The transform itself, split out so a test can drive it without an NPC.
     *
     * <p>The arithmetic is CONFIRMED from the binary. Which world direction the client's angle
     * zero points at is NOT: it is taken from Ataraxia's own compass. A live look on 2026-09-07
     * (NPC 278 "Cook", direction 6144, field 7) matched the table but cannot confirm it, because
     * the client could derive the same heading from its own copy of the definition. See
     * `verified/MOVEMENT_TABLES.md` section 5 for the test that discriminates.
     *
     * @param direction Ataraxia's 1/16384-turn angle; values a full turn or more above the range
     *                  are handled by the mask, because {@code NPC.getRespawnDirection} returns
     *                  {@code (4 + respawnDirection) << 11}, which reaches 24576 for the highest
     *                  respawn direction the decoder accepts.
     */
    public static int facingField(int direction) {
        return ((direction >> 11) - 4) & 7;
    }

    private static int selector(int legacyDirection) {
        if (legacyDirection < 0 || legacyDirection >= Utils.DIRECTION_DELTA_X.length)
            throw new IllegalArgumentException("Unknown 910 movement direction " + legacyDirection);
        return Native950NpcMasks.direction(Utils.DIRECTION_DELTA_X[legacyDirection],
                Utils.DIRECTION_DELTA_Y[legacyDirection]);
    }

    /**
     * Every NPC in the viewer's loaded regions, in region then index order. This
     * mirrors {@code LocalNPCUpdate.addInScreenNPCs} without its familiar, pet and
     * combat-definition branches, none of which a native NPC can answer.
     */
    public static List<NPC> nearbyNpcs(Player viewer) {
        Objects.requireNonNull(viewer, "viewer");
        List<NPC> nearby = new ArrayList<NPC>();
        // NPC inherits coordinate-only WorldTile.equals, so a coordinate-based contains()
        // check would drop a second, distinct NPC sharing a tile with an already-added one
        // (companions, familiars, targets sharing a square). Dedupe by identity instead;
        // this only guards against the same index appearing in more than one loaded region.
        Set<NPC> seen = Collections.newSetFromMap(new IdentityHashMap<NPC, Boolean>());
        for (int regionId : viewer.getMapRegionsIds()) {
            List<Integer> indexes = World.getRegion(regionId).getNPCsIndexes();
            if (indexes == null) continue;
            for (int npcIndex : new ArrayList<Integer>(indexes)) {
                NPC npc = World.getNPCs().get(npcIndex);
                if (npc != null && seen.add(npc)) nearby.add(npc);
            }
        }
        return nearby;
    }

    private long identityOf(NPC npc) {
        Long identity = identities.get(npc);
        if (identity == null) {
            identity = Long.valueOf(nextIdentity++);
            identities.put(npc, identity);
        }
        return identity.longValue();
    }

    /**
     * Keeps the identity map bounded by the NPCs that are still candidates. An NPC
     * that stops being a candidate is removed from the encoder's list in the same
     * frame, so its identity is no longer needed; if it comes back it is a fresh
     * addition and a fresh serial is exactly the right answer.
     */
    private void forgetUnseen(List<NPC> candidates) {
        Map<NPC, Long> kept = new IdentityHashMap<NPC, Long>();
        for (NPC npc : candidates) {
            Long identity = identities.get(npc);
            if (identity != null) kept.put(npc, identity);
        }
        identities.clear();
        identities.putAll(kept);
    }

    /**
     * Drops the published list without emitting anything, so the next frame
     * republishes every visible NPC. Used when the scene is rebuilt outside the
     * normal tick.
     */
    public void invalidate() {
        checkThread();
        encoder.invalidate();
    }

    /** Releases the viewer's view state; the connection is closing, so nothing is sent. */
    public void close() {
        checkThread();
        encoder.invalidate();
        identities.clear();
    }

    public State snapshot() {
        checkThread();
        return new State(encoder.localIndices(), encoder.additions(), encoder.removals(),
                encoder.limitDrops(), encoder.widthDrops(), frames);
    }

    /**
     * How many times this viewport refused to publish an NPC because its current definition id
     * is not one the 910 -&gt; 947 validity table calls "same". A non-zero count is a deliberate
     * silence (a transformed NPC, almost always), never a dropped frame.
     */
    public long unclassifiedDrops() { return unclassified; }

    /** The packet type this viewport writes, exposed so callers can assert on it. */
    public static ServerPacket packetType() { return ServerPacket.NPC_INFO; }

    public static final class State {
        public final int[] indices;
        public final long additions, removals, limitDrops, widthDrops, frames;

        State(int[] indices, long additions, long removals, long limitDrops, long widthDrops, long frames) {
            this.indices = indices;
            this.additions = additions;
            this.removals = removals;
            this.limitDrops = limitDrops;
            this.widthDrops = widthDrops;
            this.frames = frames;
        }

        @Override public String toString() {
            return "npcs=" + indices.length + ",additions=" + additions + ",removals=" + removals
                    + ",limitDrops=" + limitDrops + ",widthDrops=" + widthDrops + ",frames=" + frames;
        }
    }

    private void checkThread() {
        if (Thread.currentThread() != owner)
            throw new IllegalStateException("NPC view state belongs to the world thread");
    }
}
