package modern947;

import com.rs.game.player.client.Native950NpcViewport;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950NpcMasks.BitWriter;
import com.rs.network.protocol.modern950.Native950NpcMasks.Update;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.pojos.NPCDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

/**
 * Literal bit and byte fixtures for the retained-entry movement forms and the
 * confirmed NPC update-mask blocks.
 *
 * <p><b>Two groups of fixtures with different provenance live in this file.</b>
 *
 * <p>The <i>retained-entry and framing</i> fixtures - the direction chain, walk, run,
 * step, remove, the pending-mask bit, the addition terminator and the byte alignment -
 * are unchanged at 950. I re-derived the direction chain (950 0x14011fb90) and the
 * retained selector ladder (950 0x14011f8b0) against 947 and found the same widths,
 * the same selectors and the same +/-512.0f per-tile deltas, so these strings carry
 * over verbatim from the 947 evidence.
 *
 * <p>The <i>mask block</i> fixtures were rebuilt for 950 against
 * {@code Native950NpcMasks}, whose constants and per-block byte transforms were read
 * out of the 950 binary. Every expected string below was recomputed from those
 * constants and transforms by hand, never by running the encoder and pasting what it
 * produced, and each one keeps the 947 string it replaces as the re-derivation
 * baseline. What moved:
 * <ul>
 *   <li><b>Bits.</b> SAY 0-&gt;6, TRANSFORM 5-&gt;2, FACE_COORD 3-&gt;7,
 *       FORCE_MOVEMENT 12-&gt;14; NAME 18 and COLOUR_TINT 28 did not move. Every one
 *       of the vacated 947 bits means something else on 950, so no fixture may be
 *       carried over by relabelling.</li>
 *   <li><b>Header markers.</b> Byte 1's marker 0x40 -&gt; <b>0x10</b> (and 0x40 is now
 *       SAY, so a 947 header announces a forced-chat block that is not there), byte
 *       3's 0x400000 -&gt; <b>0x800000</b>, byte 4's 0x1000000 -&gt; <b>0x8000000</b>;
 *       byte 2's 0x2000 did not move.</li>
 *   <li><b>Emit order.</b> 950 consumes say, colourTint, transform, forceMovement,
 *       name, faceEntity, faceCoordinate, spotanims, animation, hits. 947 consumed animation, forceMovement, colourTint,
 *       faceCoordinate, transform, say, name.</li>
 *   <li><b>Transforms.</b> faceCoordinate's x, all six forceMovement bytes bar one and
 *       all three of its shorts, and all six colourTint fields.</li>
 * </ul>
 *
 * <p>The parser tail also proves animation at bit 3 and ordinary hits at bit 5.
 * See protocol-analysis/npc-masks-950-derived.md for the corrected control-flow evidence.
 * Fixtures that use other blocks as framing scaffolding still pin those same properties.
 */
public final class Native950NpcMovementWireTest {

    /**
     * Section 4 of MOVEMENT_TABLES.md: the addition record's 3-bit facing field, driven through
     * the real transform in {@code Native950NpcViewport}.
     *
     * <p>The two sides of this test come from different places and must agree. The expected
     * compass name is the client's, decoded from its own arithmetic in the binary; the input
     * angle is Ataraxia's, from the enum its spawn file is written in. If either moves without
     * the other, every NPC in the world turns.
     *
     * <p>What this pins is the server side only. A live look on 2026-09-07 (the Lumbridge cook,
     * direction 6144, the NORTHWEST row below) matched, but could not confirm the client's
     * absolute compass zero, because the client could have derived the same heading from its own
     * copy of the definition. That question is still open; see
     * `verified/MOVEMENT_TABLES.md` section 5.
     */
    @Test public void facingFieldMapsAtaraxiasCompassOntoTheClientsDocumentedTable() {
        assertEquals("north", 0, Native950NpcViewport.facingField(NPCDirection.NORTH.getValue()));
        assertEquals("north-east", 1, Native950NpcViewport.facingField(NPCDirection.NORTHEAST.getValue()));
        assertEquals("east", 2, Native950NpcViewport.facingField(NPCDirection.EAST.getValue()));
        assertEquals("south-east", 3, Native950NpcViewport.facingField(NPCDirection.SOUTHEAST.getValue()));
        assertEquals("south", 4, Native950NpcViewport.facingField(NPCDirection.SOUTH.getValue()));
        assertEquals("west", 6, Native950NpcViewport.facingField(NPCDirection.WEST.getValue()));
        assertEquals("north-west", 7, Native950NpcViewport.facingField(NPCDirection.NORTHWEST.getValue()));
        // SOUTHWEST is 18432, which is a full turn above the 14-bit range. The spawner normalises
        // with & 0x3FFF before storing, so that is what the field sees.
        assertEquals("south-west", 5, Native950NpcViewport.facingField(NPCDirection.SOUTHWEST.getValue() & 0x3FFF));
        // ...and the transform must survive the un-normalised value too, because nothing stops a
        // caller passing NPC.getDirection() straight through.
        assertEquals("south-west, un-normalised", 5,
                Native950NpcViewport.facingField(NPCDirection.SOUTHWEST.getValue()));
    }

    /**
     * What a spawn row with no {@code direction} actually produces, which is NOT zero.
     *
     * <p>{@code NPCSpawnsDataParser} falls back to {@code NPC.getRespawnDirection()}, which reads
     * the definition, and {@code NPCDefinitions} decodes with the defaults {@code contrast = 32}
     * and {@code respawnDirection = 7} (NPCDefinitions.java:115,120). Almost every NPC in the
     * world therefore faces north-west, exactly as the 910 server made them. Reading the absent
     * field as "direction 0, so south" is the mistake that nearly condemned a correct table on
     * 2026-09-07; this test exists so the next person does not repeat it.
     */
    @Test public void anUnstatedSpawnDirectionIsTheDefinitionDefaultAndFacesNorthWest() {
        int respawnDirection = 7, raw = (4 + respawnDirection) << 11;
        assertEquals("NPC.getRespawnDirection arithmetic", 22528, raw);
        assertEquals("Native950World normalises with & 0x3FFF", 6144, raw & 0x3FFF);
        assertEquals("north-west, which is also what was seen in the live client", 7,
                Native950NpcViewport.facingField(raw & 0x3FFF));
        assertEquals("the enum agrees that 6144 is north-west",
                NPCDirection.NORTHWEST, NPCDirection.getDirectionForValue(6144));
        assertNotEquals("an unstated direction is not south", 4,
                Native950NpcViewport.facingField(raw & 0x3FFF));
    }

    /** Every field the encoder accepts is a distinct compass point, so no two directions collide. */
    @Test public void theEightCompassValuesUseTheEightDistinctFields() {
        java.util.Set<Integer> fields = new java.util.HashSet<Integer>();
        for (NPCDirection direction : NPCDirection.values())
            fields.add(Integer.valueOf(Native950NpcViewport.facingField(direction.getValue() & 0x3FFF)));
        assertEquals("eight directions must occupy eight fields", 8, fields.size());
    }

    // The NPC direction chain, clockwise from north: 950 0x14011fb90, called from the
    // retained walk/run forms at 950 0x14011fa97. It is a compare ladder, not a lookup
    // table, and it is unchanged from 947 0x14011f6d0 - same per-direction pattern, and
    // the same +/-512.0f (one tile) constants, only the addresses moved.
    @Test public void npcDirectionTableIsClockwiseFromNorthAndNotThePlayerTable() {
        assertDirection(0, 0, 1);
        assertDirection(1, 1, 1);
        assertDirection(2, 1, 0);
        assertDirection(3, 1, -1);
        assertDirection(4, 0, -1);
        assertDirection(5, -1, -1);
        assertDirection(6, -1, 0);
        assertDirection(7, -1, 1);
        // The player jump table starts at (-1,-1); sharing one table would misplace every NPC.
        // (947 VA 0x140125fb8; the 950 address of that table was not re-derived for this test.)
        assertNotEquals(-1, Native950NpcMasks.directionDeltaX(0));
        assertEquals(2, Native950NpcMasks.direction(1, 0));
        rejects(() -> Native950NpcMasks.direction(2, 0));
        rejects(() -> Native950NpcMasks.direction(0, 0));
        rejects(() -> Native950NpcMasks.directionDeltaX(8));
        rejects(() -> Native950NpcMasks.directionDeltaY(-1));
    }

    /** NPC_INFO_MASKS.md section 1: "Walk one step north: 01 A0" and "east: 01 A8". */
    @Test public void retainedWalkMatchesTheDocumentedOneStepBitStrings() {
        BitWriter north = new BitWriter();
        Native950NpcMasks.retainedCount(north, 1);
        Native950NpcMasks.retainedWalk(north, 0, false); // 1 changed, 01 walk, 000 north, 0 no mask.
        assertEquals(15, north.bitPosition());
        assertArrayEquals(hex("01 a0"), north.toByteArray());
        assertEquals(0, Native950NpcMasks.directionDeltaX(0));
        assertEquals(1, Native950NpcMasks.directionDeltaY(0));

        BitWriter east = new BitWriter();
        Native950NpcMasks.retainedCount(east, 1);
        Native950NpcMasks.retainedWalk(east, 2, false); // 1 01 010 0
        assertArrayEquals(hex("01 a8"), east.toByteArray());
        assertEquals(1, Native950NpcMasks.directionDeltaX(2));
        assertEquals(0, Native950NpcMasks.directionDeltaY(2));
    }

    /** NPC_INFO_MASKS.md section 1: "Run two steps east: 01 D4 80" (1 10 1 010 010 0). */
    @Test public void retainedRunWritesBothStepsBeforeTheSharedMaskBit() {
        BitWriter out = new BitWriter();
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedRun(out, 2, 2, false);
        assertEquals(19, out.bitPosition());
        assertArrayEquals(hex("01 d4 80"), out.toByteArray());
    }

    /** NPC_INFO_MASKS.md section 1: "Single step with speed constant 0, north: 01 C0". */
    @Test public void retainedSingleStepUsesTheSelectorTenFormWithTheFlagClear() {
        BitWriter out = new BitWriter();
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedStep(out, 0, false); // 1 10 0 000 0
        assertArrayEquals(hex("01 c0"), out.toByteArray());
    }

    /** Selector 11 queues removal (950 0x14011fb47); changed=0 keeps the NPC untouched. */
    @Test public void retainedRemoveAndUnchangedEntriesMatchTheExistingBodies() {
        BitWriter remove = new BitWriter();
        Native950NpcMasks.retainedCount(remove, 1);
        Native950NpcMasks.retainedRemove(remove); // 1 11
        assertArrayEquals(hex("01 e0"), remove.toByteArray());

        BitWriter unchanged = new BitWriter();
        Native950NpcMasks.retainedCount(unchanged, 1);
        Native950NpcMasks.retainedEntry(unchanged);
        assertArrayEquals(hex("01 00"), unchanged.toByteArray());
        // Identical to the already shipped Native950Packets.singleNpcRetain body.
        assertArrayEquals(Native950Packets.singleNpcRetain().payload(), unchanged.toByteArray());

        BitWriter empty = new BitWriter();
        Native950NpcMasks.retainedCount(empty, 0);
        assertArrayEquals(Native950Packets.singleNpcRemove().payload(), empty.toByteArray());
    }

    @Test public void pendingMaskBitIsWrittenOnlyForTheFormsThatReadIt() {
        BitWriter walking = new BitWriter();
        Native950NpcMasks.retainedCount(walking, 1);
        Native950NpcMasks.retainedWalk(walking, 0, true); // 1 01 000 1
        assertArrayEquals(hex("01 a2"), walking.toByteArray());

        BitWriter running = new BitWriter();
        Native950NpcMasks.retainedCount(running, 1);
        Native950NpcMasks.retainedRun(running, 0, 0, true); // 1 10 1 000 000 1
        assertArrayEquals(hex("01 d0 20"), running.toByteArray());

        // Selector 00 always joins the pending list, so it carries no extra bit.
        BitWriter maskOnly = new BitWriter();
        Native950NpcMasks.retainedCount(maskOnly, 1);
        Native950NpcMasks.retainedMaskOnly(maskOnly); // 1 00
        assertEquals(11, maskOnly.bitPosition());
        assertArrayEquals(hex("01 80"), maskOnly.toByteArray());
    }


    /**
     * The actual parser tail tests bit 3 at 0x1401232C1 after its mask register changes.
     * Four smart2or4null IDs are inlined there, followed by a plain delay byte. A previous
     * bounded traversal missed that tail and incorrectly treated this proven block as absent.
     */
    @Test public void animationTailEncodesFourSmartIdsAndPlainDelay() {
        assertArrayEquals(hex("0000 08 0357 7fff 7fff 7fff 00"),
                Native950NpcMasks.maskBlock(new Update().animation(855, -1, -1, -1, 0)));
        // 32766 is the last short ID; 32767 and 65536 require four-byte smart forms.
        assertArrayEquals(hex("0000 08 7ffe 0000 80007fff 80010000 ff"),
                Native950NpcMasks.maskBlock(new Update().animation(32766, 0, 32767, 65536, 255)));
        assertEquals(1L << 3, Native950NpcMasks.MASK_ANIMATION);
        assertTrue(Native950NpcMasks.isSupportedMaskBit(3));
    }

    /**
     * Bit 2 (947: bit 5) reuses the addition record's definition setter at virtual slot 0x218.
     * {@code smart2or4null} (950 {@code 0x1400FEEA0}) consumes no selector byte and is
     * instruction-identical to 947's, so only the mask byte moved: 947 asserted
     * {@code 0000 20 01ee}, {@code 0000 20 80007fff}, {@code 0000 20 7fff}, {@code 0000 20 0000}.
     * Bit 2 sits in header byte 0 with nothing above it, so no extension marker is pulled in.
     */
    @Test public void transformMaskBlockCarriesOneSmartDefinitionId() {
        assertArrayEquals(hex("0000 04 01ee"), Native950NpcMasks.maskBlock(new Update().transform(494)));
        // 0x7FFF would decode as -1 as a short, so it takes the four byte form with bit 31 set.
        assertArrayEquals(hex("0000 04 80007fff"), Native950NpcMasks.maskBlock(new Update().transform(0x7FFF)));
        // 0x7FFE is the largest id the two-byte form can still carry.
        assertArrayEquals(hex("0000 04 7ffe"), Native950NpcMasks.maskBlock(new Update().transform(0x7FFE)));
        assertArrayEquals(hex("0000 04 7fff"), Native950NpcMasks.maskBlock(new Update().transform(-1)));
        assertArrayEquals(hex("0000 04 0000"), Native950NpcMasks.maskBlock(new Update().transform(0)));
    }

    /**
     * Say is bit 6 (947: bit 0) and name is bit 18 (unmoved). Both go through selector-free
     * readers - the NUL-terminated string reader {@code 0x140100A90} - so the bodies are
     * unchanged and only the header moved. 947 asserted {@code 0000 01 486900},
     * {@code 0000 402004 426f6200} and {@code 0000 402004 00}.
     */
    @Test public void sayAndNameWriteNulTerminatedCp1252Strings() {
        // Bit 6 is 0x40 in header byte 0, alone, so no extension marker is needed. This is the
        // tail of the port plan's own Stage 5 acceptance vector, 01 9F FF E0 00 00 40 <text> 00.
        assertArrayEquals(hex("0000 40 486900"), Native950NpcMasks.maskBlock(new Update().say("Hi")));
        // Bit 18 needs header bytes 1 and 2, so markers 0x10 and 0x2000 join the mask. 947 used
        // 0x40 for byte 1's marker, and 0x40 is SAY on 950 - a carried-over header would have
        // announced a forced-chat block that is not in the stream.
        assertArrayEquals(hex("0000 102004 426f6200"), Native950NpcMasks.maskBlock(new Update().name("Bob")));
        // An empty name restores the definition name (950 0x1401210FD, [npcDef+0x1B8]).
        assertArrayEquals(hex("0000 102004 00"), Native950NpcMasks.maskBlock(new Update().name("")));
    }

    /**
     * Bit 7 (947: bit 3), tested at 950 {@code 0x140121217} as a sign-bit test. The wire still
     * carries {@code 2*tile+1} for both axes - the sink {@code (v-1)>>1} did not move - but the
     * selector table {@code 0x140B60408} is {@code 00 01}, so x is now a plain big-endian short
     * where 947 wrote it as {@code ushortle128}. y stays little endian.
     *
     * <p>947 asserted {@code 0000 08 8119 1519}.
     */
    @Test public void faceCoordinateDoublesTheTileAndUsesTwoDifferentShortTransforms() {
        byte[] block = Native950NpcMasks.maskBlock(new Update().faceCoordinate(3200, 3210));
        assertArrayEquals(hex("0000 80 1901 1519"), block);
        // Decoded the way the client decodes them, from the wire bytes above.
        assertEquals(3200, (((0x19 << 8) | 0x01) - 1) >> 1);   // selector 00, big endian
        assertEquals(3210, ((0x15 | (0x19 << 8)) - 1) >> 1);   // selector 01, little endian
    }

    /**
     * Bit 14 (947: bit 12). Selector table {@code 0x140B60420} = {@code 02 02 00 00 02 00 01 01
     * 01}: dx1, dy1 and dplane1 are negated, dx2, dy2 and dplane2 are plain, and all three
     * shorts are little endian. 947 was dx1 plain, dy1 {@code +128}, dx2 plain, dy2 negated,
     * dplane1 plain, dplane2 negated with three {@code ushortle128} shorts - five of the six
     * bytes and all three shorts moved, while the field order did not.
     *
     * <p>947 asserted {@code 0000 4010 01 82 03 fc 00 00 8500 8a00 8010}. Note the header
     * reversed as well: bit 14 puts 0x40 in byte 1 behind marker 0x10, where bit 12 put 0x10 in
     * byte 1 behind marker 0x40.
     */
    @Test public void forceMovementUsesTheNativeByteTransformsAndThreeLittleEndianShorts() {
        byte[] block = Native950NpcMasks.maskBlock(
                new Update().forceMovement(1, 2, 3, 4, 0, 0, 5, 10, 0x1000));
        assertArrayEquals(hex("0000 1040 ff fe 03 04 00 00 0500 0a00 0010"), block);
        // Decoded from the wire bytes above, through the selectors that table names. dx2, dy2
        // and dplane2 are selector 00, so their wire bytes are the inputs unchanged (03, 04, 00)
        // and there is nothing to invert; the rows below are the ones that transform.
        assertEquals(1, -(byte) 0xFF);                       // dx1, selector 02 (negate)
        assertEquals(2, -(byte) 0xFE);                       // dy1, selector 02
        assertEquals(5, 0x05 | (0x00 << 8));                 // delay 1, selector 01 (low byte first)
        assertEquals(0x1000, (0x00 | (0x10 << 8)) & 0x3FFF); // angle, 14 bits at 0x1407474B0
    }

    /**
     * Bit 28, unmoved; FIVE of its six fields changed transform. Selector table
     * {@code 0x140B60414} = {@code 01 01 00 00 00 00}: hue and saturation are {@code value+128},
     * luminance and opacity are plain, and both delays are plain big-endian shorts. 947 was
     * {@code 128-hue}, {@code 128-saturation}, {@code -luminance}, {@code 128-opacity}, a
     * little-endian start delay and a big-endian end delay.
     *
     * <p>947 asserted {@code 0000 40204010 767d9c81 0100 0002}. The header moved too: byte 3's
     * marker is 0x800000 on 950 (947: 0x400000) and byte 1's is 0x10 (947: 0x40).
     */
    @Test public void colourTintPacksHslBytesThenTwoBigEndianDelays() {
        byte[] block = Native950NpcMasks.maskBlock(new Update().colourTint(10, 3, 100, 255, 1, 2));
        assertArrayEquals(hex("0000 10208010 8a83 64ff 0001 0002"), block);
        // Decoded from the wire bytes above; the client masks hue/saturation/luminance after
        // the transform, at 0x140120A63..0x140120A77. Opacity and both delays are selector 00,
        // so their wire bytes are the inputs unchanged (ff, 00 01, 00 02).
        assertEquals(10, (0x8A - 128) & 0x3F);  // selector 01, then 6 bits
        assertEquals(3, (0x83 - 128) & 0x7);    // selector 01, then 3 bits
        assertEquals(100, 0x64 & 0x7F);         // selector 00, then 7 bits - 100 is not clipped
    }

    /**
     * The variable-length header reader {@code 0x140120540} adds one byte per extension marker:
     * 0x10 ({@code test r8b, 0x10} at {@code 0x140120558}), 0x2000 ({@code bt r8, 0xd} at
     * {@code 0x140120575}), 0x800000 ({@code bt r8, 0x17} at {@code 0x140120593}) and 0x8000000
     * ({@code bt r8, 0x1b} at {@code 0x1401205B1}). 947's were 0x40, 0x2000, 0x400000, 0x1000000.
     *
     * <p>947 asserted these rows through the animation block; the rows below use other
     * derived blocks that occupy the same header bytes, so the
     * property - "exactly the markers the set bits need, and no more" - is still what is checked.
     * The byte-4 marker cannot be exercised: it needs a bit at 32 or above and no derived block
     * lives there. Its coverage returns with such a block, not before.
     */
    @Test public void headerSelectsExactlyTheExtensionMarkersTheSetBitsNeed() {
        // A bit in byte 0 alone emits one mask byte and no marker (947 used animation, 0x80).
        assertArrayEquals(hex("0000 40"), header(new Update().say("")));
        // Two bits in byte 0 still emit one byte (947: 0x81 from animation plus say).
        assertArrayEquals(hex("0000 44"), header(new Update().say("").transform(494)));
        // Byte 1 pulls in marker 0x10 and nothing above it (947: 4010).
        assertArrayEquals(hex("0000 1040"), header(new Update().forceMovement(0, 0, 0, 0, 0, 0, 0, 0, 0)));
        // Byte 2 drags the byte-1 marker in with it (947: 402004).
        assertArrayEquals(hex("0000 102004"), header(new Update().name("")));
        // Byte 3 drags both (947: 40204010).
        assertArrayEquals(hex("0000 10208010"), header(new Update().colourTint(0, 0, 0, 0, 0, 0)));
        // mask() is the mask BEFORE any marker is added, so it carries only the block bits.
        assertEquals(0x44L, new Update().say("").transform(494).mask());
        assertEquals(0x10000000L, new Update().colourTint(0, 0, 0, 0, 0, 0).mask());
    }

    /**
     * Blocks are emitted in the client's consumption order, which on 950 is neither bit order
     * nor 947's order. The client parses positionally, so this sequence is load-bearing on its
     * own: the address of each bit test inside {@code 0x1401205E0}, ascending, is
     * {@code 0x140120656} say, {@code 0x140120A01} colourTint, {@code 0x140120AA2} transform,
     * {@code 0x140120AEA} forceMovement, {@code 0x140121067} name, {@code 0x140121217}
     * faceCoordinate.
     *
     * <p>947 emitted animation, forceMovement, colourTint, faceCoordinate, transform, say, name
     * and asserted:
     * <pre>
     *   0000 e9304410 0357 7fff 7fff 7fff 80  01 82 03 fc 00 00 8500 8a00 8010
     *                 767d9c81 0100 0002  8119 1519  01ee  486900  426f6200
     * </pre>
     * This fixture covers six blocks; the tail animation block has its own literal fixtures.
     *
     * <p>The setter order below is deliberately neither revision's emit order - it is arbitrary,
     * which is the whole point. If the encoder ever emitted in call order rather than its own,
     * this assertion would fail. (An earlier version of this comment claimed the calls were in
     * 947's emit order; they are not, and it would have been a weaker guarantee if they were.)
     */
    @Test public void everyConfirmedBlockIsEmittedInClientConsumptionOrder() {
        Update update = new Update()
                .name("Bob")
                .say("Hi")
                .colourTint(10, 3, 100, 255, 1, 2)
                .transform(494)
                .faceCoordinate(3200, 3210)
                .forceMovement(1, 2, 3, 4, 0, 0, 5, 10, 0x1000);
        // Mask 0x4 | 0x40 | 0x80 | 0x4000 | 0x40000 | 0x10000000 = 0x100440c4, plus markers
        // 0x10, 0x2000 and 0x800000 -> 0x108460d4, which lays out as d4 60 84 10.
        assertArrayEquals(hex("0000 d4608410"
                + " 486900"                            // bit 6  say
                + " 8a83 64ff 0001 0002"               // bit 28 colour tint
                + " 01ee"                              // bit 2  transform
                + " fffe 0304 0000 0500 0a00 0010"     // bit 14 force movement
                + " 426f6200"                          // bit 18 name
                + " 1901 1519"),                       // bit 7  face coordinate
                Native950NpcMasks.maskBlock(update));
    }

    /**
     * Several pending NPCs concatenate their own two skipped bytes and headers.
     *
     * <p>947 used an animation block for the first NPC and asserted
     * {@code 0000 80 0357 7fff 7fff 7fff 80  0000 01 486900}. Transform stands in for it: the
     * point of this fixture is that each NPC re-emits its own prefix and header, which a block
     * with a different header width would hide, so the two blocks below deliberately differ.
     */
    @Test public void maskSectionConcatenatesOneBlockPerPendingNpc() {
        byte[] section = Native950NpcMasks.maskSection(
                new Update().transform(494), new Update().name("Bob"));
        assertArrayEquals(hex("0000 04 01ee  0000 102004 426f6200"), section);
    }

    /**
     * One retained mask-only NPC, the mandatory 16-bit addition sentinel, byte padding, then
     * one mask block. The subject here is the composition - the bit widths, the sentinel and
     * the alignment - not the block's contents, so the 947 animation block was swapped for say,
     * which is the smallest derived block and needs no extension marker.
     *
     * <p>That makes this fixture the port plan's own Stage 5 acceptance vector, from a separate
     * pass: {@code 01 9F FF E0 00 00 40 <text> 00} - retained count 1, changed=1 selector 00,
     * the terminator, byte alignment, the two skipped bytes, then mask byte 0x40.
     *
     * <p>947 asserted {@code 01 9fffe0 0000 80 0357 7fff 7fff 7fff 80}. The bit-addressed
     * prefix is unchanged at 950; only the block after it moved.
     *
     * <p>{@code Native950NpcMasksTest.planAcceptanceVectorForSayIsReproducedBitForBit} pins the
     * same vector from the mask side. What this row owns that that one does not is the bit
     * arithmetic underneath it - twenty-seven bits before alignment, thirty-two after - so a
     * retained form or a sentinel that changed width would fail here first.
     */
    @Test public void documentedMaskOnlyCompositionReproducesTheEvidenceBytes() {
        BitWriter out = new BitWriter();
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedMaskOnly(out);
        Native950NpcMasks.additionsEnd(out);
        assertEquals(27, out.bitPosition());
        out.align();
        assertEquals(32, out.bitPosition());
        out.bytes(Native950NpcMasks.maskBlock(new Update().say("Hi")));
        assertArrayEquals(hex("01 9fffe0 0000 40 486900"), out.toByteArray());
    }

    /**
     * The same composition with the walk form and its pending-mask bit set:
     * count 01, then 1 01 000 1 followed by the sixteen sentinel bits and one pad
     * bit, giving A3 FF FE, then the identical mask block.
     *
     * <p>947 asserted {@code 01 a3fffe 0000 80 0357 7fff 7fff 7fff 80}; the prefix is unchanged
     * and the block follows {@link #documentedMaskOnlyCompositionReproducesTheEvidenceBytes}
     * onto say.
     */
    @Test public void retainedWalkWithPendingMaskCarriesTheSameMaskBlock() {
        BitWriter out = new BitWriter();
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedWalk(out, 0, true);
        Native950NpcMasks.additionsEnd(out);
        assertEquals(31, out.bitPosition());
        out.align();
        out.bytes(Native950NpcMasks.maskBlock(new Update().say("Hi")));
        byte[] body = out.toByteArray();
        assertArrayEquals(hex("01 a3fffe 0000 40 486900"), body);

        // The mask section is byte identical to the mask-only example above.
        assertArrayEquals(hex("0000 40 486900"),
                Arrays.copyOfRange(body, 4, body.length));
    }

    /**
     * The composed body frames as NPC_INFO opcode 80, size -2, under the real outbound
     * ISAAC. 947 used opcode 12; the descriptor size is unchanged, so the frame keeps
     * its two-byte length prefix.
     */
    @Test public void composedBodyFramesThroughTheTransportWithTheRealCipher() {
        int[] loginSeeds = {1, 2, 3, 4};
        int[] outgoingSeeds = {51, 52, 53, 54};
        BitWriter out = new BitWriter();
        Native950NpcMasks.retainedCount(out, 1);
        Native950NpcMasks.retainedWalk(out, 2, true);
        Native950NpcMasks.additionsEnd(out);
        out.align();
        // 947 paired animation with say here, for a two-block mask byte of 0x81. Transform is
        // the derived block that pairs with say inside header byte 0 on 950: 0x40 | 0x4 = 0x44,
        // still one header byte, still two blocks, and say still leads the consumption order.
        out.bytes(Native950NpcMasks.maskBlock(new Update().transform(494).say("Hi")));
        byte[] body = out.toByteArray();
        // 947 baseline: 01 abfffe 0000 81 0357 7fff 7fff 7fff 80 486900
        assertArrayEquals(hex("01 abfffe 0000 44 486900 01ee"), body);

        Native950Packets.Packet packet = Native950Packets.packet(ServerPacket.NPC_INFO, body);
        assertEquals(ServerPacket.NPC_INFO, packet.type());

        Native950GameTransport transport =
                Native950GameTransport.afterAuthentication(loginSeeds, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            channel.writeOutbound(packet);
            IntSupplier cipher = new Native950Isaac(outgoingSeeds);
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


    /** Only client-derived typed blocks are supported; raw masks, including unknown lists, refuse. */
    @Test public void provenMaskSetIsExactAndRawCandidatesStayFenced() {
        for (int bit = 0; bit < 64; bit++) {
            boolean derived = bit == 1 || bit == 2 || bit == 3 || bit == 5 || bit == 6 || bit == 7
                    || bit == 14 || bit == 18 || bit == 24 || bit == 28;
            assertEquals("bit " + bit, derived, Native950NpcMasks.isSupportedMaskBit(bit));
            final int candidate = bit;
            refuses(() -> Native950NpcMasks.candidateMask(candidate), "bit " + candidate + " ");
        }
        assertEquals("face entity", 1L << 1, Native950NpcMasks.MASK_FACE_ENTITY);
        assertEquals("transform", 1L << 2, Native950NpcMasks.MASK_TRANSFORM);
        assertEquals("animation", 1L << 3, Native950NpcMasks.MASK_ANIMATION);
        assertEquals("ordinary hits", 1L << 5, Native950NpcMasks.MASK_HITS);
        assertEquals("say", 1L << 6, Native950NpcMasks.MASK_SAY);
        assertEquals("face coordinate", 1L << 7, Native950NpcMasks.MASK_FACE_COORD);
        assertEquals("force movement", 1L << 14, Native950NpcMasks.MASK_FORCE_MOVEMENT);
        assertEquals("name", 1L << 18, Native950NpcMasks.MASK_NAME);
        assertEquals("spotanims", 1L << 24, Native950NpcMasks.MASK_SPOTANIMS);
        assertEquals("colour tint", 1L << 28, Native950NpcMasks.MASK_COLOUR_TINT);
        // These separate count-prefixed extensions are still unknown, despite the
        // ordinary hit/hitbar block at bit 5 now being independently derived.
        assertFalse(Native950NpcMasks.isSupportedMaskBit(10));
        assertFalse(Native950NpcMasks.isSupportedMaskBit(33));
        // Literal empty hits and clear-all spotanims: headers, counts, signed removal sentinel.
        assertArrayEquals(hex("0000 20 00 00"), Native950NpcMasks.maskBlock(new Update().hits()));
        assertArrayEquals(hex("0000 10208001 01 ffff 80"),
                Native950NpcMasks.maskBlock(new Update().spotanims(new int[]{-1})));
    }

    @Test public void everyBuilderRejectsOutOfRangeFieldsBeforeFraming() {
        rejects(() -> Native950NpcMasks.retainedCount(new BitWriter(), -1));
        rejects(() -> Native950NpcMasks.retainedCount(new BitWriter(), 256));
        rejects(() -> Native950NpcMasks.retainedWalk(new BitWriter(), 8, false));
        rejects(() -> Native950NpcMasks.retainedWalk(new BitWriter(), -1, false));
        rejects(() -> Native950NpcMasks.retainedRun(new BitWriter(), 0, 8, false));
        rejects(() -> Native950NpcMasks.retainedStep(new BitWriter(), 8, false));
        rejects(() -> new BitWriter().bits(0, 0));
        rejects(() -> new BitWriter().bits(33, 0));
        rejects(() -> new BitWriter().bits(3, 8));
        rejects(() -> new BitWriter().bits(3, -5));
        rejects(() -> new BitWriter().bits(1, 1).bytes(new byte[] {1}));

        rejects(() -> Native950NpcMasks.maskBlock(new Update()));
        rejects(() -> new Update().animation(-2, 0, 0, 0, 0));
        rejects(() -> new Update().animation(0, 0, 0, 0, 256));
        rejects(() -> new Update().animation(0, 0, 0, 0, -1));
        rejects(() -> new Update().transform(-2));
        rejects(() -> new Update().faceCoordinate(-1, 0));
        rejects(() -> new Update().faceCoordinate(0, 32768));
        rejects(() -> new Update().forceMovement(128, 0, 0, 0, 0, 0, 0, 0, 0));
        rejects(() -> new Update().forceMovement(0, -129, 0, 0, 0, 0, 0, 0, 0));
        rejects(() -> new Update().forceMovement(0, 0, 0, 0, 0, 0, 65536, 0, 0));
        rejects(() -> new Update().forceMovement(0, 0, 0, 0, 0, 0, 0, 0, 16384));
        rejects(() -> new Update().colourTint(64, 0, 0, 0, 0, 0));
        rejects(() -> new Update().colourTint(0, 8, 0, 0, 0, 0));
        rejects(() -> new Update().colourTint(0, 0, 128, 0, 0, 0));
        rejects(() -> new Update().colourTint(0, 0, 0, 256, 0, 0));
        rejects(() -> new Update().colourTint(0, 0, 0, 0, -1, 0));
        rejects(() -> new Update().say("embedded\0nul"));
        // U+4E2D has no CP1252 encoding; keep the source itself ASCII only.
        rejects(() -> new Update().name(String.valueOf((char) 0x4E2D)));
    }

    /**
     * Boundary values that must still encode, so the ranges are not merely narrow.
     *
     * <p>947 baselines, in the same order: {@code 0000 20 80010000},
     * {@code 0000 80 7ffe 0000 7ffe 0000 7f} (the animation row; its 950 equivalent is
     * covered by {@link #animationTailEncodesFourSmartIdsAndPlainDelay}),
     * {@code 0000 4010 7f 7f 80 80 7f 81 7fff 7fff 7f3f},
     * {@code 0000 40204010 41798100 ffff ffff} and {@code 0000 08 8100 ffff}.
     */
    @Test public void boundaryValuesEncodeAtTheEdgeOfEveryValidatedRange() {
        assertArrayEquals(hex("0000 04 80010000"), Native950NpcMasks.maskBlock(new Update().transform(0x10000)));
        // Every byte at an edge of ITS OWN 950 transform. The inputs are not 947's: the
        // negating selectors moved to dx1, dy1 and dplane1 (table 0x140B60420 = 02 02 00 00 02
        // 00), so 947's tuple no longer put anything interesting through one. -128 is the value
        // that matters there and it is the only one that matters, because negating it overflows a
        // signed byte - -(-128) is 128, which is 0x80 only because the write is masked. Nothing
        // else in the suite exercises that.
        //   dx1 -128 negated -> 80   (the overflow edge)
        //   dy1  127 negated -> 81   dplane1 127 negated -> 81
        //   dx2  127 plain   -> 7f   dy2 -128 plain -> 80   dplane2 -128 plain -> 80
        // Delays take both ends rather than the same end twice, and the angle sits on the 14-bit
        // limit 0x3FFF; all three shorts are little endian on 950.
        assertArrayEquals(hex("0000 1040 80 81 7f 80 81 80 ffff 0000 ff3f"),
                Native950NpcMasks.maskBlock(new Update()
                        .forceMovement(-128, 127, 127, -128, 127, -128, 65535, 0, 16383)));
        // Opacity 255, not 947's 128. 128 was an edge only under 947's 128-opacity transform,
        // where it wrapped the wire byte to 0x00; 950's selector for that field is plain, so 128
        // encodes to an unremarkable 0x80 and the row stopped bounding anything. 255 is the
        // validated maximum. Delays take both ends.
        assertArrayEquals(hex("0000 10208010 bf877fff ffff 0000"),
                Native950NpcMasks.maskBlock(new Update().colourTint(63, 7, 127, 255, 65535, 0)));
        assertArrayEquals(hex("0000 80 0001 ffff"),
                Native950NpcMasks.maskBlock(new Update().faceCoordinate(0, 32767)));

        BitWriter full = new BitWriter(1);
        Native950NpcMasks.retainedCount(full, 255);
        for (int entry = 0; entry < 255; entry++) Native950NpcMasks.retainedEntry(full);
        assertEquals(263, full.bitPosition());
        assertEquals(33, full.toByteArray().length);
    }

    private static byte[] header(Update update) {
        byte[] block = Native950NpcMasks.maskBlock(update);
        int headerBytes = 3;
        long mask = update.mask();
        if ((mask >>> 8) != 0) headerBytes++;
        if ((mask >>> 16) != 0) headerBytes++;
        if ((mask >>> 24) != 0) headerBytes++;
        return Arrays.copyOfRange(block, 0, headerBytes);
    }

    private static void assertDirection(int direction, int dx, int dy) {
        assertEquals("dx of direction " + direction, dx, Native950NpcMasks.directionDeltaX(direction));
        assertEquals("dy of direction " + direction, dy, Native950NpcMasks.directionDeltaY(direction));
        assertEquals(direction, Native950NpcMasks.direction(dx, dy));
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
        try { action.run(); fail("Invalid NPC mask input accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    /**
     * Asserts a block whose 950 layout is not derived refuses, and that the refusal names the
     * evidence gap rather than failing anonymously. Mirrors {@code Native950PlayerMasksTest}'s
     * own {@code refuses} helper.
     *
     * <p>A refusal test is not a placeholder. A block with the right bit and a wrong transform
     * is the worst failure mode this client has - it selects the correct reader, feeds it wrong
     * bytes, and every field after it in the frame shifts - and a block whose bit is never
     * tested at all is worse still, because its bytes stay in the stream unread. These
     * assertions are what keep that unreachable while the layouts are unknown.
     */
    private static void refuses(Runnable action, String... mustName) {
        try {
            action.run();
            fail("A block with no derived 950 layout reached the encoder");
        } catch (UnsupportedOperationException expected) {
            String message = expected.getMessage();
            assertNotNull("a refusal must explain itself", message);
            for (String fragment : mustName)
                assertTrue("refusal should name \"" + fragment + "\": " + message,
                        message.contains(fragment));
        }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
