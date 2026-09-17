package modern947;

import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950NpcInfo;
import com.rs.network.protocol.modern950.Native950NpcInfo.NpcState;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950NpcMasks.Update;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

/**
 * Literal byte fixtures for the multi-NPC NPC_INFO encoder (950 opcode 80, size -2).
 * Cache free: the encoder owns no world state.
 *
 * <p><b>Where the addition-record fixtures come from.</b> Every expected string that
 * contains an addition record was recomputed bit by bit from the 950 field list -
 * index16, dy[npcBits], immediate1, facing3, type16, plane2, mask1, dx[npcBits] (950
 * 0x14011fd20; plan A7) - and not by running the encoder. The 947 string each one
 * replaces is kept next to it as the re-derivation baseline. That baseline matters
 * more than usual here: the 947 record was index16, dy, plane2, dx, type16,
 * immediate1, facing3, mask1, which has the <b>same total width</b>, so a body still
 * on the 947 order frames correctly, keeps its terminator and its byte alignment,
 * and every mask block after it still lands in the right place. Nothing desyncs and
 * nothing errors - the client simply reads the type id's high bits as the plane and
 * puts every NPC on the wrong tile. These fixtures are the only thing that catches it.
 *
 * <p>The retained section, the terminator rule, the byte alignment and the
 * pending-mask ordering did not change at 950, so those fixtures are unchanged.
 *
 * <p>Corroboration for the headline vector: the port plan's own Stage 5 acceptance
 * criterion, written from a separate pass, is "prove it with 00 00 01 FB 00 3D C0 10
 * - an NPC of type 494 must appear at exactly (+2,-3) from the viewer", which is
 * exactly what {@link #firstAdditionMatchesTheEvidenceRecordWithoutAnUnneededTerminator}
 * asserts.
 */
public final class Native950NpcInfoTest {

    private static final int VIEWER_X = 3200, VIEWER_Y = 3200, PLANE = 0, NPC_BITS = 7;
    private static final int BANKER = 494;

    /** No NPCs at all is retained count 0 and nothing else, exactly the shipped body. */
    @Test public void emptyViewWritesTheBareRetainedCount() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        byte[] body = tick(encoder, false);
        assertArrayEquals(hex("00"), body);
        assertArrayEquals(Native950Packets.singleNpcRemove().payload(), body);
        assertEquals(0, encoder.localCount());
        assertEquals(0, encoder.additions());
        assertEquals(0, encoder.removals());
    }

    /**
     * index=1 type=494 dx=2 dy=-3 plane=0 bits=7 facing=0, the plan's Stage 5
     * acceptance vector. Field by field: count 00, index 0000000000000001,
     * dy 1111101, immediate 1, facing 000, type 0000000111101110, plane 00,
     * mask 0, dx 0000010 - sixty-one bits, so 00 00 01 FB 00 3D C0 10.
     *
     * <p>947 laid the same eight fields out as 00 00 01 FA 02 01 EE 80 (index, dy,
     * plane, dx, type, immediate, facing, mask). Identical length, different meaning.
     *
     * <p>The trailing nineteen bits of the {@code staticNpcAdd} form are the optional
     * 65535 terminator plus padding: the addition reader only reads another index
     * while sixteen bits remain in the buffer (950 0x14011fd4e on entry, 0x14012049c
     * on the loop back edge), and after byte alignment at most seven can. With no
     * mask section the encoder therefore stops at the record itself, and the first
     * sixty-one bits are identical.
     */
    @Test public void firstAdditionMatchesTheEvidenceRecordWithoutAnUnneededTerminator() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build());
        assertArrayEquals(hex("00 0001 fb00 3dc0 10"), body);

        // 947 baseline: 00 0001 fa02 01ee 87 fff8
        byte[] documented = hex("00 0001 fb00 3dc0 17 fff8");
        assertArrayEquals(Native950Packets.staticNpcAdd(1, BANKER, 2, -3, 0, NPC_BITS, 0).payload(), documented);
        assertArrayEquals(Arrays.copyOf(documented, 7), Arrays.copyOf(body, 7));
        // Byte seven's top five bits are the tail of dx (00010); only the terminator,
        // which starts in its low three bits, differs between the two forms.
        assertEquals(documented[7] & 0xF8, body[7] & 0xF8);
        assertEquals(0x10, body[7] & 0xFF);
        assertEquals(1, encoder.localCount());
        assertArrayEquals(new int[] {1}, encoder.localIndices());
        assertEquals(1, encoder.additions());
    }

    /**
     * A mask makes the terminator mandatory, then the block follows the byte alignment.
     *
     * <p>Setting the mask bit only flips bit 53 of the record, which is byte 6's bit
     * 5: C0 becomes C4. Everything before it is unmoved, which is the point - the mask
     * bit sits between plane and dx on 950 and after facing on 947.
     *
     * <p>The bit-addressed prefix is a literal spec fixture; the mask block itself is
     * taken from {@code Native950NpcMasks}, whose byte transforms are derived and
     * pinned separately (see Native950NpcMovementWireTest). What this test owns is
     * that the block starts at byte 10 - after the record, the mandatory terminator
     * and the padding - not what the block contains.
     */
    @Test public void additionCarryingAConfirmedMaskWritesTheTerminatorAndTheBlock() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        byte[] body = tick(encoder, false,
                banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).update(new Update().say("Hi")).build());
        // 947 baseline: 00 0001 fa02 01ee 8f fff8 <block>
        // A literal for the same reason as maskSectionFollowsRetainedOrderThenAdditionOrder:
        // taking it from the encoder under test would compare that function against itself.
        byte[] block = hex("0000 40 486900");
        assertArrayEquals(concat(hex("00 0001 fb00 3dc4 17 fff8"), block), body);
        assertEquals(10, body.length - block.length);
    }

    /** changed=0 keeps the NPC: the same 01 00 body the single-NPC writer sent. */
    @Test public void retainedUnchangedNpcMatchesTheExistingRetainBody() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build());
        byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build());
        assertArrayEquals(hex("01 00"), body);
        assertArrayEquals(Native950Packets.singleNpcRetain().payload(), body);
        assertEquals(1, encoder.additions());
    }

    /**
     * One walk per direction of the NPC direction chain at 950 0x14011fb90, called
     * from the retained walk and run forms at 950 0x14011fa97 (clockwise from north,
     * NOT the player jump table). The selector is derived from the tile the NPC
     * actually stepped onto, so a wrong table would fail here rather than on the
     * wire. North is 01 A0 and east is 01 A8.
     *
     * <p>The chain is a compare ladder, not a lookup table, and it did not change at
     * 950: both revisions add +/-512.0f (one tile) per axis with the same per-direction
     * pattern - 950 dir 0 adds +512 to z at 0x14011fc15, dir 2 adds +512 to x at
     * 0x14011fc48, and so on, against 947's 0x14011f755 / 0x14011f788. Only the
     * addresses of the code and of the two float constants moved.
     */
    @Test public void eachOfTheEightNpcDirectionsWritesItsOwnWalkSelector() {
        byte[] expected = hex("a0 a4 a8 ac b0 b4 b8 bc");
        for (int direction = 0; direction < 8; direction++) {
            int dx = Native950NpcMasks.directionDeltaX(direction);
            int dy = Native950NpcMasks.directionDeltaY(direction);
            assertEquals(direction, Native950NpcMasks.direction(dx, dy));

            Native950NpcInfo encoder = new Native950NpcInfo();
            tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());
            byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X + dx, VIEWER_Y + dy)
                    .walk(Native950NpcMasks.direction(dx, dy)).build());
            assertArrayEquals("direction " + direction,
                    new byte[] {0x01, expected[direction]}, body);
            assertEquals(1, encoder.localCount());
        }
    }

    /** NPC_INFO_MASKS.md: "Run two steps east: 01 D4 80" (1 10 1 010 010 0). */
    @Test public void runFormCarriesBothStepsInsideOneRetainedEntry() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());
        byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y).run(2, 2).build());
        assertArrayEquals(hex("01 d4 80"), body);

        // The single-step form 10 with the flag clear stays available and distinct.
        Native950NpcInfo crawler = new Native950NpcInfo();
        tick(crawler, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());
        assertArrayEquals(hex("01 c0"),
                tick(crawler, false, banker(1, 1L, VIEWER_X, VIEWER_Y + 1).step(0).build()));
    }

    /** Selector 11 removes; the entry leaves the encoder's list on the same tick. */
    @Test public void leavingTheViewDistanceRemovesTheRetainedEntry() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X + 10, VIEWER_Y).build());
        byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X + 20, VIEWER_Y).build());
        assertArrayEquals(hex("01 e0"), body);
        assertEquals(0, encoder.localCount());
        assertEquals(1, encoder.removals());

        // Still away: nothing is retained and nothing is added.
        assertArrayEquals(hex("00"), tick(encoder, false, banker(1, 1L, VIEWER_X + 20, VIEWER_Y).build()));
        // Back in range on a later tick, so it is added again. 947 baseline: 00 0001 fa02 01ee 80
        assertArrayEquals(hex("00 0001 fb00 3dc0 10"),
                tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build()));
        assertEquals(2, encoder.additions());
    }

    /**
     * There is no retained teleport form (NPC_INFO_MASKS.md section 1), so a jump
     * is a removal, and the re-addition waits a tick rather than mixing a queued
     * removal with a same-index addition in one packet.
     */
    @Test public void teleportRemovesAndDefersTheReadditionByOneTick() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X + 10, VIEWER_Y).build());
        assertArrayEquals(hex("01 e0"),
                tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).teleported().build()));
        assertEquals(0, encoder.localCount());
        // 947 baseline: 00 0001 fa02 01ee 80
        assertArrayEquals(hex("00 0001 fb00 3dc0 10"),
                tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build()));
    }

    /** Two retained NPCs, one walking and one untouched, in one frame. */
    @Test public void twoRetainedNpcsShareOneFrameWhenOnlyOneMoves() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build(),
                banker(2, 2L, VIEWER_X + 5, VIEWER_Y).build());
        assertArrayEquals(new int[] {1, 2}, encoder.localIndices());

        byte[] body = tick(encoder, false,
                banker(1, 1L, VIEWER_X, VIEWER_Y + 1).walk(0).build(),
                banker(2, 2L, VIEWER_X + 5, VIEWER_Y).build());
        assertArrayEquals(hex("02 a0"), body);
        assertArrayEquals(new int[] {1, 2}, encoder.localIndices());
    }

    /**
     * The client resets its retained count to zero and re-appends only the kept
     * entries (950 0x14011f90d, re-appends at 0x14011f9ee / 0x14011fa2c /
     * 0x14011fa68 / 0x14011fadb), so a selector-11 removal compacts the
     * list while the survivors keep their relative order. The encoder's list has
     * to compact the same way or every following entry desynchronises.
     */
    @Test public void removingTheFirstOfTwoCompactsTheListLikeTheClientDoes() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build(),
                banker(2, 2L, VIEWER_X + 5, VIEWER_Y).build());

        byte[] body = tick(encoder, false, banker(2, 2L, VIEWER_X + 5, VIEWER_Y).build());
        assertArrayEquals(hex("02 e0"), body);
        assertArrayEquals(new int[] {2}, encoder.localIndices());
        // The next frame's count is the compacted size, never the old one.
        assertArrayEquals(hex("01 00"), tick(encoder, false, banker(2, 2L, VIEWER_X + 5, VIEWER_Y).build()));
    }

    /**
     * NPC_INFO.md: a normal rebuild does not necessarily empty the client's NPC
     * list, but a zero-retained-count add is safe after either kind, so the view
     * is invalidated and every visible NPC is republished in the same frame.
     */
    @Test public void sceneRebuildRepublishesEveryVisibleNpcInTheSameFrame() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build());
        assertEquals(1, encoder.localCount());

        byte[] body = tick(encoder, true, banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build());
        // 947 baseline: 00 0001 fa02 01ee 80
        assertArrayEquals(hex("00 0001 fb00 3dc0 10"), body);
        assertArrayEquals(new int[] {1}, encoder.localIndices());
        assertEquals(2, encoder.additions());
        // The shrinking count did the removal, so no selector-11 entry was needed.
        assertEquals(0, encoder.removals());

        // invalidate() is the same thing without emitting a frame.
        encoder.invalidate();
        assertEquals(0, encoder.localCount());
    }

    /**
     * EntityList reuses the lowest free index, so a viewer can be handed a
     * different NPC under a known number. The identity is what tells them apart.
     */
    @Test public void reusedWorldIndexIsRemovedBeforeTheNewNpcIsAdded() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(5, 11L, VIEWER_X, VIEWER_Y).build());
        assertArrayEquals(new int[] {5}, encoder.localIndices());

        byte[] body = tick(encoder, false, NpcState.npc(5, 22L, 1, VIEWER_X, VIEWER_Y, PLANE).build());
        assertArrayEquals(hex("01 e0"), body);
        assertEquals(0, encoder.localCount());
        assertEquals(1, encoder.removals());

        // index 5, dy 0000000, immediate 1, facing 000, type 1, plane 00, mask 0, dx 0000000.
        // 947 baseline: 00 0005 0000 0001 80
        byte[] next = tick(encoder, false, NpcState.npc(5, 22L, 1, VIEWER_X, VIEWER_Y, PLANE).build());
        assertArrayEquals(hex("00 0005 0100 0020 00"), next);
        assertArrayEquals(new int[] {5}, encoder.localIndices());
    }

    /**
     * Mask blocks follow the pending list: retained-list order first, then addition
     * order. The client's mask loop is 950 0x14011f5b1..0x14011f62f and it walks the
     * vector at {@code npcMgr+0xc0f0}, which the retained section pushes to at 950
     * 0x14011fa45 and the addition record at 950 0x1401201ce.
     *
     * <p>The bit-addressed prefix here is unaligned in a different place from the
     * other fixtures - the retained mask-only entry costs three bits, so the addition
     * record starts at bit 11 and every field of it straddles a byte boundary. Field
     * by field: count 00000001, changed 1, selector 00, index 0000000000000010,
     * dy 1111101, immediate 1, facing 000, type 0000000111101110, plane 00, mask 1,
     * dx 0000010, terminator - eighty bits exactly, so the mask section starts at
     * byte 10 with no padding.
     *
     * <p>The two mask blocks come from {@code Native950NpcMasks}; this test owns their
     * order and placement, not their bytes.
     *
     * <p>The added NPC carried an animation block on 947. This row uses a name
     * block - deliberately one whose header is
     * three bytes wide where the retained NPC's say header is one, because a placement bug that
     * only mis-sized a block would otherwise still land the second block in the right place.
     * The addition record itself is unaffected either way: it carries a single mask BIT, not
     * the block, so the bit-addressed prefix below is unchanged.
     */
    @Test public void maskSectionFollowsRetainedOrderThenAdditionOrder() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());

        byte[] body = tick(encoder, false,
                banker(1, 1L, VIEWER_X, VIEWER_Y).update(new Update().say("Hi")).build(),
                banker(2, 2L, VIEWER_X + 2, VIEWER_Y - 3)
                        .update(new Update().name("Bob")).build());
        // 947 baseline: 01 8000 5f40 403d d1 ffff <say block> <animation block>
        //
        // The two blocks are LITERALS, not calls to Native950NpcMasks.maskBlock. Building the
        // expected value from the encoder under test would make the block half of this assertion
        // a tautology - the same function on both sides - and this file would silently stop
        // pinning those bytes at all. They are:
        //   say("Hi"):  0000 40 486900          - bit 6, one header byte, NUL-terminated CP1252
        //   name("Bob"): 0000 102004 426f6200   - bit 18, so the header pulls in the byte-1 and
        //                                         byte-2 extension markers (0x10, 0x2000)
        // Both are independently asserted in Native950NpcMovementWireTest; the point HERE is
        // placement and order, and the deliberately different header widths (1 byte vs 3) mean a
        // mis-sized block cannot still land in the right place.
        byte[] retainedBlock = hex("0000 40 486900");
        byte[] addedBlock = hex("0000 102004 426f6200");
        assertArrayEquals(concat(hex("01 8000 5f60 07b8 82 ffff"),
                retainedBlock,   // retained NPC 1
                addedBlock),     // added NPC 2
                body);
        // Eighty bits of bit-addressed prefix, so the mask section starts at byte 10 exactly.
        assertEquals(10, body.length - retainedBlock.length - addedBlock.length);
        assertArrayEquals(new int[] {1, 2}, encoder.localIndices());
    }

    /**
     * Offsets are signed inside npcBits: the client subtracts {@code 1 << npcBits}
     * from any value above {@code (1 << (npcBits - 1)) - 1}, at 950
     * 0x140120182..0x1401201c5, for both offsets. At width 7 the range is -64..63;
     * anything outside it cannot be encoded and is a counted drop rather than a
     * truncated record.
     */
    @Test public void offsetsAtTheSignedLimitsOfNpcBitsEncodeAndBeyondThemAreDropped() {
        // dy -64 (1000000), immediate 1, facing 000, type 494, plane 00, mask 0,
        // dx 63 (0111111). 947 baseline: 00 0001 803f 01ee 80
        Native950NpcInfo wide = new Native950NpcInfo(64, Native950NpcInfo.MAX_LOCAL_NPCS);
        assertArrayEquals(hex("00 0001 8100 3dc1 f8"),
                wide.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false,
                        Collections.singletonList(banker(1, 1L, VIEWER_X + 63, VIEWER_Y - 64).build())));
        assertEquals(0, wide.widthDrops());

        Native950NpcInfo edge = new Native950NpcInfo(64, Native950NpcInfo.MAX_LOCAL_NPCS);
        assertArrayEquals(hex("00"), edge.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false,
                Collections.singletonList(banker(1, 1L, VIEWER_X + 64, VIEWER_Y).build())));
        assertEquals(1, edge.widthDrops());
        assertEquals(0, edge.additions());

        // Outside the view distance is ordinary invisibility, not a width drop.
        Native950NpcInfo narrow = new Native950NpcInfo(15, Native950NpcInfo.MAX_LOCAL_NPCS);
        assertArrayEquals(hex("00"), narrow.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false,
                Collections.singletonList(banker(1, 1L, VIEWER_X + 16, VIEWER_Y).build())));
        assertEquals(0, narrow.widthDrops());

        // A narrower scene moves the limit with it: width 5 is -16..15.
        // dy 15 (01111), immediate 1, facing 000, type 65535, plane 00, mask 0,
        // dx -16 (10000). 947 baseline: 00 0001 790f fff8 00
        Native950NpcInfo five = new Native950NpcInfo(64, Native950NpcInfo.MAX_LOCAL_NPCS);
        assertArrayEquals(hex("00 0001 7c7f ff88 00"), five.body(VIEWER_X, VIEWER_Y, PLANE, 5, false,
                Collections.singletonList(NpcState.npc(1, 1L, 65535, VIEWER_X - 16, VIEWER_Y + 15, PLANE).build())));
        assertEquals(0, five.widthDrops());
    }

    /** A different plane is never visible, whatever the horizontal distance is. */
    @Test public void anotherPlaneIsNeverVisible() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        assertArrayEquals(hex("00"), encoder.body(VIEWER_X, VIEWER_Y, 0, NPC_BITS, false,
                Collections.singletonList(NpcState.npc(1, 1L, BANKER, VIEWER_X, VIEWER_Y, 1).build())));

        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());
        assertArrayEquals(hex("01 e0"), encoder.body(VIEWER_X, VIEWER_Y, 0, NPC_BITS, false,
                Collections.singletonList(NpcState.npc(1, 1L, BANKER, VIEWER_X, VIEWER_Y, 1).build())));
    }

    /** The retained count is an unsigned byte, so the viewer's list is capped. */
    @Test public void theLocalLimitCapsAdditionsAndCountsTheDrops() {
        Native950NpcInfo encoder = new Native950NpcInfo(15, 1);
        byte[] body = tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build(),
                banker(2, 2L, VIEWER_X + 1, VIEWER_Y).build());
        // 947 baseline: 00 0001 0000 01ee 80
        assertArrayEquals(hex("00 0001 0100 3dc0 00"), body);
        assertArrayEquals(new int[] {1}, encoder.localIndices());
        assertEquals(1, encoder.limitDrops());
        assertEquals(Native950NpcInfo.MAX_LOCAL_NPCS, 255);

        List<NpcState> crowd = new ArrayList<NpcState>();
        for (int index = 1; index <= 300; index++)
            crowd.add(banker(index, index, VIEWER_X, VIEWER_Y).build());
        Native950NpcInfo full = new Native950NpcInfo();
        full.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false, crowd);
        assertEquals(255, full.localCount());
        assertEquals(45, full.limitDrops());
        // The next frame's retained count still fits the unsigned byte the client reads.
        assertEquals(255, full.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false, crowd)[0] & 0xFF);
    }


    /** Proven typed updates compose into NPC_INFO; arbitrary raw mask blocks stay fenced. */
    @Test public void provenUpdatesReachTheEncoderWhileRawBlocksRemainFenced() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y).build());
        // Retained mask-only 1/100 + sixteen sentinel bits -> 01 9f ff e0.
        // The tail parser consumes face entity before animation before ordinary hits:
        // mask bits 1|3|5 = 2a, player 0x1234 -> 12 02 34; four BE smart IDs,
        // plain delay zero, and two empty hit/hitbar counts. These are literal bytes.
        assertArrayEquals(hex("01 9f ff e0 00 00 2a 12 02 34 03 57 7f ff 7f ff 7f ff 00 00 00"),
                tick(encoder, false, banker(1, 1L, VIEWER_X, VIEWER_Y)
                        .update(new Update().hits().animation(855, -1, -1, -1, 0).facePlayer(0x1234)).build()));
        for (int bit = 0; bit < 64; bit++) {
            boolean derived = bit == 1 || bit == 2 || bit == 3 || bit == 5 || bit == 6 || bit == 7
                    || bit == 14 || bit == 18 || bit == 24 || bit == 28;
            assertEquals("bit " + bit, derived, Native950NpcMasks.isSupportedMaskBit(bit));
            try {
                Native950NpcMasks.candidateMask(bit);
                fail("Raw candidate bit " + bit + " accepted");
            } catch (UnsupportedOperationException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("bit " + bit + " "));
            }
        }
        rejects(new Runnable() { public void run() {
            banker(1, 1L, VIEWER_X, VIEWER_Y).update(new Update());
        } });
        int updateMethods = 0;
        for (Method method : NpcState.Builder.class.getMethods()) {
            if (!method.getName().equals("update")) continue;
            updateMethods++;
            assertArrayEquals(new Class<?>[] {Update.class}, method.getParameterTypes());
        }
        assertEquals(1, updateMethods);
        for (Method method : NpcState.Builder.class.getMethods()) {
            String name = method.getName().toLowerCase();
            assertFalse("Raw mask entry point " + name,
                    name.contains("raw") || name.contains("hits") || name.contains("graphic"));
        }
    }

    /** Field ranges are checked before any bit is written, so a frame never half forms. */
    @Test public void invalidInputsAreRejectedBeforeAnythingIsEncoded() {
        final Native950NpcInfo encoder = new Native950NpcInfo();
        rejects(new Runnable() { public void run() { new Native950NpcInfo(0, 1); } });
        rejects(new Runnable() { public void run() { new Native950NpcInfo(15, 256); } });
        rejects(new Runnable() { public void run() {
            encoder.body(VIEWER_X, VIEWER_Y, PLANE, 0, false, Collections.<NpcState>emptyList());
        } });
        rejects(new Runnable() { public void run() {
            encoder.body(VIEWER_X, VIEWER_Y, PLANE, 16, false, Collections.<NpcState>emptyList());
        } });
        rejects(new Runnable() { public void run() {
            encoder.body(VIEWER_X, VIEWER_Y, 4, NPC_BITS, false, Collections.<NpcState>emptyList());
        } });
        rejects(new Runnable() { public void run() {
            encoder.body(-1, VIEWER_Y, PLANE, NPC_BITS, false, Collections.<NpcState>emptyList());
        } });
        // 65535 is the addition terminator, so it is not a usable world index.
        rejects(new Runnable() { public void run() { NpcState.npc(65535, 1L, 1, 0, 0, 0); } });
        rejects(new Runnable() { public void run() { NpcState.npc(-1, 1L, 1, 0, 0, 0); } });
        rejects(new Runnable() { public void run() { NpcState.npc(1, 1L, 65536, 0, 0, 0); } });
        rejects(new Runnable() { public void run() { NpcState.npc(1, 1L, 1, 16384, 0, 0); } });
        rejects(new Runnable() { public void run() { NpcState.npc(1, 1L, 1, 0, 0, 4); } });
        rejects(new Runnable() { public void run() { banker(1, 1L, 0, 0).facing(8); } });
        rejects(new Runnable() { public void run() { banker(1, 1L, 0, 0).walk(8); } });
        rejects(new Runnable() { public void run() { banker(1, 1L, 0, 0).step(-1); } });
        rejects(new Runnable() { public void run() { banker(1, 1L, 0, 0).run(0, 8); } });
        // One world index may appear only once in a frame.
        rejects(new Runnable() { public void run() {
            encoder.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false, Arrays.asList(
                    banker(1, 1L, VIEWER_X, VIEWER_Y).build(),
                    banker(1, 2L, VIEWER_X, VIEWER_Y).build()));
        } });
        assertEquals(0, encoder.localCount());
    }

    /**
     * The composed body frames as NPC_INFO opcode 80, size -2, under the real cipher.
     * 947 used opcode 12; the size is unchanged, so the two-byte length prefix stays.
     */
    @Test public void composedBodyFramesThroughTheTransportWithTheRealCipher() {
        Native950NpcInfo encoder = new Native950NpcInfo();
        Native950Packets.Packet packet = encoder.encode(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, false,
                Collections.singletonList(banker(1, 1L, VIEWER_X + 2, VIEWER_Y - 3).build()));
        assertEquals(ServerPacket.NPC_INFO, packet.type());
        assertEquals(80, ServerPacket.NPC_INFO.opcode());
        assertEquals(-2, ServerPacket.NPC_INFO.size());
        byte[] body = packet.payload();
        // 947 baseline: 00 0001 fa02 01ee 80
        assertArrayEquals(hex("00 0001 fb00 3dc0 10"), body);

        Native950GameTransport transport =
                Native950GameTransport.afterAuthentication(new int[] {1, 2, 3, 4}, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeOutbound(packet);
            IntSupplier cipher = new Native950Isaac(new int[] {51, 52, 53, 54});
            ByteArrayOutputStream expected = new ByteArrayOutputStream();
            expected.write((80 + cipher.getAsInt()) & 0xFF);
            expected.write((body.length >>> 8) & 0xFF);
            expected.write(body.length & 0xFF);
            expected.write(body, 0, body.length);
            assertArrayEquals(expected.toByteArray(), readOutput(channel));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static NpcState.Builder banker(int index, long identity, int x, int y) {
        return NpcState.npc(index, identity, BANKER, x, y, PLANE);
    }

    private static byte[] tick(Native950NpcInfo encoder, boolean rebuilt, NpcState... visible) {
        return encoder.body(VIEWER_X, VIEWER_Y, PLANE, NPC_BITS, rebuilt, Arrays.asList(visible));
    }

    private static byte[] readOutput(EmbeddedChannel channel) {
        ByteBuf output = channel.readOutbound();
        assertNotNull(output);
        try {
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        } finally {
            output.release();
        }
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid NPC_INFO input accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }

    /**
     * Joins a literal bit-addressed prefix to one or more mask blocks. The prefix is
     * this test's own spec fixture; the blocks belong to {@code Native950NpcMasks} and
     * are pinned by its own test, so composing rather than inlining them keeps this
     * file asserting NPC_INFO framing instead of silently re-asserting mask transforms.
     */
    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) length += part.length;
        byte[] joined = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, joined, offset, part.length);
            offset += part.length;
        }
        return joined;
    }
}
