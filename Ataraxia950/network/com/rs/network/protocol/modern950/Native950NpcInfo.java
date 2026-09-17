package com.rs.network.protocol.modern950;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;

/**
 * One viewer's native 950 NPC_INFO encoder (server opcode 80, size -2).
 *
 * <p>This is the N-NPC replacement for the literal single-entity writers
 * {@code Native950Packets.staticNpcAdd}, {@code singleNpcRetain} and
 * {@code singleNpcRemove}. It owns the retained list this connection has
 * published and nothing else: it holds no world state, reads no cache and
 * imports nothing from the game packages, so it is fully unit testable.
 *
 * <p>Every field width and every form is derived from the 950 client. All VAs
 * below are <b>950</b> addresses in {@code rs2client.exe}; the 947 binary has
 * live code at most of the same addresses, so a 947 VA disassembles cleanly
 * here and lands on something unrelated. The body is written in exactly the
 * order the client's outer reader 0x14011f420 consumes it:
 *
 * <ol>
 *   <li>an unsigned 8-bit retained count (0x14011f8b0, read at 0x14011f8c9),</li>
 *   <li>one entry per retained NPC in the client's list order: a changed bit
 *       (0x14011f9d6) and, when it is set, a two-bit selector (0x14011fa14:
 *       00 mask only, 01 walk with a three-bit direction at 0x14011fa86, 10 run
 *       or single step selected by the flag at 0x14011faf7, 11 remove at
 *       0x14011fb47) - unchanged from 947,</li>
 *   <li>zero or more addition records index16 / dy[npcBits] / immediate1 /
 *       facing3 / type16 / plane2 / mask1 / dx[npcBits] (0x14011fd20). <b>This
 *       order changed at 950</b>; see {@code addRecord},</li>
 *   <li>the 16-bit 65535 terminator, but only when a mask section follows (see
 *       below),</li>
 *   <li>byte alignment (0x14011f597..0x14011f5ad), then one mask block per NPC
 *       that asked for an update, in pending-list order: retained-list order
 *       first, then addition order (loop 0x14011f5b1..0x14011f62f).</li>
 * </ol>
 *
 * <p><b>Client list bookkeeping.</b> 0x14011f90d resets the client's retained
 * count to zero and re-appends only the entries this packet kept (0x14011f9ee,
 * 0x14011fa2c, 0x14011fa68, 0x14011fadb), and additions append after them
 * (0x140120088). A selector-11 removal therefore compacts the list while
 * preserving the relative order of the survivors, and a retained count smaller
 * than the previous one queues every old trailing entry for removal
 * (0x14011f8d6..0x14011f903). {@link #localIndices()} mirrors that list exactly;
 * if it ever diverges the rest of the frame desynchronises.
 *
 * <p><b>The terminator.</b> The addition reader only reads another 16-bit index
 * while at least 16 bits remain in the whole buffer (0x14011fd4e on entry,
 * 0x14012049c on the loop back edge). After byte alignment at most seven padding
 * bits remain, so a body with no mask section needs no terminator at all - which
 * is why a body of {@code 01 00} and a body of {@code 00} are both complete. A
 * mask section adds at least three bytes, so the terminator becomes mandatory the
 * moment one NPC asks for an update.
 *
 * <p><b>Offsets.</b> Addition offsets are signed inside the scene's
 * {@code npcBits} width and are added to a base the client derives from the
 * local actor (0x1401203ec/0x1401203f9), so the caller must send PLAYER_INFO
 * before NPC_INFO. That base is <b>not unconditionally the current tile</b>:
 * 0x140120358 loads the local actor's path queue and 0x14012035f/0x140120363
 * branch on its length, so with a non-empty queue the base is the LAST QUEUED
 * PATH STEP (read from {@code [rdx-0x14]} / {@code [rdx-0xC]} at
 * 0x1401203a3/0x1401203a8) and only an empty queue falls through to the virtual
 * position call at 0x140120365..0x140120384. Unchanged from 947, where the same
 * branch is at 0x14011fe70/0x14011fe74.
 *
 * <p>The caller passes the post-movement tile, and today the two coincide,
 * because {@code Native950PlayerInfo} writes every local move with the verified
 * teleport form, which leaves the client's path queue empty. <b>The moment
 * PLAYER_INFO starts emitting the local walk (type 1) or run (type 2) forms, the
 * base these offsets are measured against must move to the queued destination
 * tile in the same change</b>, or every addition in the frame lands on the wrong
 * tile.
 *
 * <p><b>Fail closed.</b> There is no retained teleport form, so an NPC that
 * teleports, changes plane, leaves the view or is replaced by a different NPC at
 * the same world index is removed with selector 11 and is not re-added in the
 * same frame; its index is quarantined for one tick. Masks are only ever the
 * blocks {@link Native950NpcMasks} confirms; a candidate mask cannot reach this
 * encoder because there is no raw-bytes entry point.
 */
public final class Native950NpcInfo {

    /** The retained count is an unsigned byte, so a viewer can hold 255 NPCs at most. */
    public static final int MAX_LOCAL_NPCS = 255;

    /** Matches the view distance the single-NPC {@code Native950NpcView} used. */
    public static final int DEFAULT_VIEW_DISTANCE = 15;

    private final int viewDistance;
    private final int localLimit;
    private final List<Entry> local = new ArrayList<Entry>();
    private long additions, removals, limitDrops, widthDrops;

    public Native950NpcInfo() {
        this(DEFAULT_VIEW_DISTANCE, MAX_LOCAL_NPCS);
    }

    public Native950NpcInfo(int viewDistance, int localLimit) {
        if (viewDistance < 1 || viewDistance > 16383)
            throw new IllegalArgumentException("View distance must fit 1..16383 tiles");
        if (localLimit < 0 || localLimit > MAX_LOCAL_NPCS)
            throw new IllegalArgumentException("Local NPC limit must fit 0.." + MAX_LOCAL_NPCS);
        this.viewDistance = viewDistance;
        this.localLimit = localLimit;
    }

    public int viewDistance() { return viewDistance; }

    public int localLimit() { return localLimit; }

    /** How many NPCs the client currently holds, i.e. the next retained count. */
    public int localCount() { return local.size(); }

    /** The client's retained list in its exact order; the encoder's only view state. */
    public int[] localIndices() {
        int[] indices = new int[local.size()];
        for (int slot = 0; slot < indices.length; slot++) indices[slot] = local.get(slot).index;
        return indices;
    }

    public boolean contains(int npcIndex) { return slotOf(npcIndex) >= 0; }

    public long additions() { return additions; }

    public long removals() { return removals; }

    /** Candidates skipped because the viewer already holds {@link #localLimit()} NPCs. */
    public long limitDrops() { return limitDrops; }

    /** Candidates inside the view distance whose offsets did not fit the scene's npcBits. */
    public long widthDrops() { return widthDrops; }

    /**
     * Forgets the published list without emitting anything. The next body then
     * starts with retained count 0, which queues every old client entry for
     * removal; NPC_INFO.md states that a zero-retained-count add is safe after
     * either kind of scene rebuild.
     */
    public void invalidate() { local.clear(); }

    /** An unframed NPC_INFO packet; frame it once on the owning connection. */
    public Native950Packets.Packet encode(int viewerX, int viewerY, int viewerPlane, int npcBits,
                                          boolean rebuilt, Collection<NpcState> candidates) {
        return Native950Packets.packet(ServerPacket.NPC_INFO,
                body(viewerX, viewerY, viewerPlane, npcBits, rebuilt, candidates));
    }

    /**
     * Builds one NPC_INFO body and advances this viewer's retained list.
     *
     * @param viewerX      the viewer's x after this tick's movement
     * @param viewerY      the viewer's y after this tick's movement
     * @param viewerPlane  the viewer's plane after this tick's movement
     * @param npcBits      the offset width from the most recent scene header
     * @param rebuilt      true when a REBUILD_NORMAL was sent this tick
     * @param candidates   every NPC that could be visible, in a stable order
     */
    public byte[] body(int viewerX, int viewerY, int viewerPlane, int npcBits,
                       boolean rebuilt, Collection<NpcState> candidates) {
        coordinate(viewerX, "Viewer x");
        coordinate(viewerY, "Viewer y");
        if (viewerPlane < 0 || viewerPlane > 3) throw new IllegalArgumentException("Viewer plane must fit 0..3");
        if (npcBits < 1 || npcBits > 15) throw new IllegalArgumentException("npcBits must fit 1..15");
        Objects.requireNonNull(candidates, "candidates");

        List<NpcState> visible = new ArrayList<NpcState>(candidates.size());
        Set<Integer> seen = new LinkedHashSet<Integer>();
        for (NpcState state : candidates) {
            Objects.requireNonNull(state, "candidate NPC state");
            if (!seen.add(Integer.valueOf(state.index)))
                throw new IllegalArgumentException("World index " + state.index + " appears twice in one frame");
            visible.add(state);
        }

        // A rebuild invalidates the client's scene state, so the whole list is
        // republished; the shrinking retained count does the removal for us.
        if (rebuilt) local.clear();

        Native950NpcMasks.BitWriter out = new Native950NpcMasks.BitWriter(64);
        List<byte[]> maskBlocks = new ArrayList<byte[]>();
        Set<Integer> quarantined = new LinkedHashSet<Integer>();

        Native950NpcMasks.retainedCount(out, local.size());
        for (Iterator<Entry> it = local.iterator(); it.hasNext();) {
            Entry entry = it.next();
            NpcState state = find(visible, entry.index);
            boolean keep = state != null && state.identity == entry.identity && !state.teleported
                    && inView(state, viewerX, viewerY, viewerPlane);
            if (!keep) {
                Native950NpcMasks.retainedRemove(out);
                it.remove();
                quarantined.add(Integer.valueOf(entry.index));
                removals++;
                continue;
            }
            byte[] block = state.maskBlock;
            boolean pendingMask = block != null;
            switch (state.movement) {
                case NONE:
                    if (pendingMask) Native950NpcMasks.retainedMaskOnly(out);
                    else Native950NpcMasks.retainedEntry(out);
                    break;
                case WALK:
                    Native950NpcMasks.retainedWalk(out, state.direction1, pendingMask);
                    break;
                case STEP:
                    Native950NpcMasks.retainedStep(out, state.direction1, pendingMask);
                    break;
                default:
                    Native950NpcMasks.retainedRun(out, state.direction1, state.direction2, pendingMask);
                    break;
            }
            if (pendingMask) maskBlocks.add(block);
        }

        for (NpcState state : visible) {
            if (contains(state.index) || quarantined.contains(Integer.valueOf(state.index))) continue;
            if (state.teleported || !inView(state, viewerX, viewerY, viewerPlane)) continue;
            int dx = state.x - viewerX;
            int dy = state.y - viewerY;
            if (!fitsWidth(dx, npcBits) || !fitsWidth(dy, npcBits)) { widthDrops++; continue; }
            if (local.size() >= localLimit) { limitDrops++; continue; }
            addRecord(out, state, dx, dy, npcBits);
            local.add(new Entry(state.index, state.identity));
            additions++;
            if (state.maskBlock != null) maskBlocks.add(state.maskBlock);
        }

        // Only a mask section can leave 16 or more bits in front of the addition
        // reader; padding alone never can, so the terminator is written exactly
        // when it is needed and the empty bodies stay 01 00 and 00.
        if (!maskBlocks.isEmpty()) Native950NpcMasks.additionsEnd(out);
        out.align();
        for (byte[] block : maskBlocks) out.bytes(block);
        return out.toByteArray();
    }

    /**
     * The 950 addition record, in the exact order the client reads it (950
     * 0x14011fd20; the eight calls to the bit reader 0x1400fef30 at 950
     * 0x14011fe08 and 0x1401200aa..0x14012017d):
     *
     * <pre>index16, dy[npcBits], immediate1, facing3, type16, plane2, mask1, dx[npcBits]</pre>
     *
     * <p><b>This is not the 947 order.</b> 947 read index16, dy[npcBits], plane2,
     * dx[npcBits], type16, immediate1, facing3, mask1 (947 0x14011f860, reads at
     * 947 0x14011fbb7..0x14011fc84). Plan appendix A7; both re-derived here.
     *
     * <p><b>Why this must be exact.</b> The two orders have the same total width,
     * 39 + 2*npcBits, so a 947-ordered record does <i>not</i> desynchronise the
     * stream: the terminator, the byte alignment and every mask block after it
     * still frame correctly and the client never errors. It silently mis-selects
     * instead - the type id's high bits get read as the plane, the facing lands
     * inside the offsets, and every NPC in the frame appears as the wrong
     * definition on the wrong tile facing the wrong way. Nothing on either side
     * reports it. This ordering is the only defence.
     *
     * <p><b>Field roles come from the sinks, not from position.</b> The first
     * npcBits read is added to the local actor's Z, i.e. game y, at 950
     * 0x1401203f9, and the last to its X at 950 0x1401203ec - so first is dy and
     * last is dx (947 had the same roles, at 0x14011ff01 and 0x14011fef5). Both
     * are sign extended inside npcBits at 950 0x140120182..0x1401201c5, which is
     * the rule {@code fitsWidth} mirrors. The 16-bit read at 950 0x140120147 is
     * handed to the definition setter at virtual slot 0x218 (950 0x140120151), so
     * it is the type. The 1-bit read at 950 0x1401200c8 is compared with 1 at 950
     * 0x1401203fc and passed as the "place immediately" argument of the position
     * call at 950 0x140120452, so it is the immediate/teleport flag; a re-add
     * always jumps rather than glides, so this encoder always writes 1. The 3-bit
     * read at 950 0x1401200d9 is scaled by pi/4 (950 0x1401200e6; the constants at
     * 0x140bbff30 = pi and 0x140bbfe98 = 0.25 are byte identical to 947's) and
     * applied as a heading at 950 0x140120304, so it is the facing. The final
     * 1-bit read, at 950 0x14012016c, pushes this NPC onto the pending-mask list
     * when it is 1 (950 0x1401201c5..0x1401201e9), so it is the mask flag and the
     * pending list is what fixes the mask-block order.
     */
    private static void addRecord(Native950NpcMasks.BitWriter out, NpcState state, int dx, int dy, int npcBits) {
        out.bits(16, state.index);
        out.bits(npcBits, dy & ((1 << npcBits) - 1));
        // The immediate-position flag; a re-add always jumps rather than glides.
        out.bits(1, 1);
        out.bits(3, state.facing);
        out.bits(16, state.typeId);
        out.bits(2, state.plane);
        out.bits(1, state.maskBlock != null ? 1 : 0);
        out.bits(npcBits, dx & ((1 << npcBits) - 1));
    }

    private boolean inView(NpcState state, int viewerX, int viewerY, int viewerPlane) {
        return state.plane == viewerPlane
                && Math.abs(state.x - viewerX) <= viewDistance
                && Math.abs(state.y - viewerY) <= viewDistance;
    }

    private static boolean fitsWidth(int offset, int npcBits) {
        int ceiling = 1 << (npcBits - 1);
        return offset >= -ceiling && offset < ceiling;
    }

    private int slotOf(int npcIndex) {
        for (int slot = 0; slot < local.size(); slot++)
            if (local.get(slot).index == npcIndex) return slot;
        return -1;
    }

    private static NpcState find(List<NpcState> visible, int npcIndex) {
        for (NpcState state : visible) if (state.index == npcIndex) return state;
        return null;
    }

    private static void coordinate(int value, String field) {
        if (value < 0 || value > 16383) throw new IllegalArgumentException(field + " must fit 14 bits");
    }

    private static final class Entry {
        final int index;
        final long identity;
        Entry(int index, long identity) { this.index = index; this.identity = identity; }
    }

    /** The retained-entry forms of 950 0x14011f8b0, chosen per NPC by the caller. */
    private enum Movement { NONE, WALK, STEP, RUN }

    /**
     * One NPC as this viewer should see it after the world tick: where it is,
     * what it did this tick, and the confirmed mask blocks it wants to send.
     *
     * <p>{@code index} is the NPC's world index, reported exactly as the world
     * assigned it - EntityList hands out 1-based indices and reuses the lowest
     * free one, so {@code identity} distinguishes a reused index from the NPC
     * that used to hold it. Any stable per-instance number does: the encoder
     * only ever compares it with the value it stored when it added the NPC.
     */
    public static final class NpcState {
        private final int index;
        private final long identity;
        private final int typeId;
        private final int x, y, plane, facing;
        private final Movement movement;
        private final int direction1, direction2;
        private final boolean teleported;
        private final byte[] maskBlock;

        private NpcState(Builder builder) {
            index = builder.index;
            identity = builder.identity;
            typeId = builder.typeId;
            x = builder.x;
            y = builder.y;
            plane = builder.plane;
            facing = builder.facing;
            movement = builder.movement;
            direction1 = builder.direction1;
            direction2 = builder.direction2;
            teleported = builder.teleported;
            maskBlock = builder.maskBlock;
        }

        public int index() { return index; }
        public long identity() { return identity; }
        public int typeId() { return typeId; }
        public int x() { return x; }
        public int y() { return y; }
        public int plane() { return plane; }

        /** True when this NPC carries at least one confirmed mask block this tick. */
        public boolean hasMask() { return maskBlock != null; }

        /**
         * Starts one NPC record. 65535 is the addition terminator, so it is not a
         * usable world index; the type is the 16-bit definition id the client hands
         * to the definition setter at virtual slot 0x218.
         */
        public static Builder npc(int index, long identity, int typeId, int x, int y, int plane) {
            return new Builder(index, identity, typeId, x, y, plane);
        }

        public static final class Builder {
            private final int index;
            private final long identity;
            private final int typeId;
            private final int x, y, plane;
            private int facing;
            private Movement movement = Movement.NONE;
            private int direction1, direction2;
            private boolean teleported;
            private byte[] maskBlock;

            private Builder(int index, long identity, int typeId, int x, int y, int plane) {
                if (index < 0 || index >= 65535)
                    throw new IllegalArgumentException("NPC world index must fit 0..65534");
                if (typeId < 0 || typeId > 65535)
                    throw new IllegalArgumentException("NPC type must fit an unsigned short");
                coordinate(x, "NPC x");
                coordinate(y, "NPC y");
                if (plane < 0 || plane > 3) throw new IllegalArgumentException("NPC plane must fit 0..3");
                this.index = index;
                this.identity = identity;
                this.typeId = typeId;
                this.x = x;
                this.y = y;
                this.plane = plane;
            }

            /**
             * The three-bit facing selector of the addition record (read at 950
             * 0x1401200d9, and note it moved to fourth in the 950 record - see
             * {@code addRecord}). The client turns it into a heading
             * of {@code field * pi/4} (950 0x1401200e6), and those two constants are
             * byte identical to 947's, so the compass mapping did not move between the
             * revisions. The mapping onto Ataraxia's own compass is pinned by
             * {@code Native950NpcViewport.facingField}, not here; a caller that cannot
             * point at evidence for a value leaves it at 0.
             */
            public Builder facing(int facing) {
                if (facing < 0 || facing > 7) throw new IllegalArgumentException("Facing must fit 0..7");
                this.facing = facing;
                return this;
            }

            /** Selector 01, move-speed constant 1: one step along the NPC direction table. */
            public Builder walk(int direction) {
                movement = Movement.WALK;
                direction1 = checkDirection(direction);
                return this;
            }

            /** Selector 10 with the two-step bit clear, move-speed constant 0: one step. */
            public Builder step(int direction) {
                movement = Movement.STEP;
                direction1 = checkDirection(direction);
                return this;
            }

            /** Selector 10 with the two-step bit set, move-speed constant 2: two steps. */
            public Builder run(int firstDirection, int secondDirection) {
                movement = Movement.RUN;
                direction1 = checkDirection(firstDirection);
                direction2 = checkDirection(secondDirection);
                return this;
            }

            /**
             * Marks a jump the retained forms cannot express. There is no retained
             * teleport form, so the encoder removes the NPC and re-adds it on a later
             * tick rather than inventing one.
             */
            public Builder teleported() {
                teleported = true;
                return this;
            }

            /**
             * Attaches the confirmed mask blocks for this NPC. The block is encoded
             * and validated here, so a candidate mask fails at the call site rather
             * than halfway through a frame.
             */
            public Builder update(Native950NpcMasks.Update update) {
                Objects.requireNonNull(update, "update");
                maskBlock = Native950NpcMasks.maskBlock(update);
                return this;
            }

            public NpcState build() { return new NpcState(this); }

            private static int checkDirection(int direction) {
                if (direction < 0 || direction > 7)
                    throw new IllegalArgumentException("NPC direction must fit 0..7");
                return direction;
            }
        }
    }
}
