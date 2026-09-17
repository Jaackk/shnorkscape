package modern947;

import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Literal wire fixtures for the native 950-1 client, independent of the packet writer.
 *
 * <p>Every expected byte string below was recomputed by hand from the field list in
 * {@code protocol-analysis/ataraxia-950-port-plan.md} PART 1 (the VERIFIED outbound table) and
 * then re-checked against the 950 parser named in each test. The 947 string it replaces is kept
 * in a comment as the re-derivation baseline: if one of these ever has to be re-derived again,
 * the diff between the two revisions is the evidence, not the writer's current output.
 *
 * <p>Why the exactness matters: the 950 client parses positionally and reports nothing. A field
 * written in the 947 order still decodes - into the wrong variable - and every field after it
 * shifts. There is no error path that catches a mistake here at runtime.
 *
 * <p>All VAs cited are 950 addresses in {@code rs2client.exe}. The 947 and 950 code ranges
 * overlap, so a 947 VA disassembles cleanly in the 950 image and lands on unrelated code; do not
 * carry an address across revisions without re-resolving it.
 */
public final class Native950InventoryWireTest {

    /**
     * UPDATE_INV_FULL, 947 opcode 69 -&gt; 950 opcode 9, still a u16-framed packet.
     *
     * <p>The item field widened from u16 to a big-endian u24 (plan A1). 950 parser 0x1400fd060:
     * the entry loop does {@code add qword [rbx+0x18], 3} at 0x1400fd120 and then assembles
     * {@code (b0<<16)|(b1<<8)|b2} at 0x1400fd131..0x1400fd152, with the {@code lea r8d,[r10-1]}
     * at 0x1400fd1b8 keeping the +1 bias. Every slot therefore gains one byte and the u16 length
     * prefix grows with it; a 947-width body desynchronises everything after the first entry.
     */
    @Test public void fullContainerIncludesEmptyAmountsAndUsesTheWidenedItemField() {
        Native950Packets.Packet p = Native950Packets.inventoryFull(93, false,
                new int[] {-1, 0, 995, 65534}, new int[] {0, 1, 254, 255});
        assertEquals(ServerPacket.UPDATE_INV_FULL, p.type());
        // 947 baseline: 45 0015 005d00 0004 000000 000101 03e4fe ffffff000000ff
        assertArrayEquals(hex("09 0019 005d00 0004 00000000 00000101 0003e4fe 00ffffff000000ff"),
                p.frame(() -> 0));
        // No entries, so nothing widens: header only, byte-identical to 947.
        assertArrayEquals(hex("005d00 0000"),
                Native950Packets.inventoryFull(93, false, new int[0], new int[0]).payload());
    }

    /**
     * UPDATE_INV_PARTIAL, 947 opcode 5 -&gt; 950 opcode 50, u16-framed.
     *
     * <p>950 parser 0x1400fcd60: the smart slot index is unchanged (a byte below 0x80, else a
     * big-endian u16 plus 0x8000, at 0x1400fce15..0x1400fce41), then the same widened BE u24 item
     * field at 0x1400fce45..0x1400fce72, and the amount is still skipped entirely when the u24
     * is zero (the {@code je} at 0x1400fce75 branches on the assembled item value).
     */
    @Test public void sparseContainerOmitsEmptyAmountsAndUsesSmartSlotBoundary() {
        Native950Packets.Packet p = Native950Packets.inventorySlots(1, true,
                new int[] {0, 127, 128, 32767}, new int[] {-1, 0, 995, 65534},
                new int[] {0, 1, 255, Integer.MAX_VALUE});
        assertEquals(ServerPacket.UPDATE_INV_PARTIAL, p.type());
        // 947 baseline: 05 001c 000101 000000 7f000101 808003e4ff000000ff ffffffffff7fffffff
        assertArrayEquals(hex("32 0020 000101 00000000 7f00000101"
                + " 80800003e4ff000000ff ffff00ffffff7fffffff"), p.frame(() -> 0));
    }

    /**
     * IF_SETEVENTS, 947 opcode 35 -&gt; 950 opcode 24, fixed 12 bytes. All four fields moved.
     *
     * <p>950 parser 0x140107970, read in cursor order:
     * <ul>
     * <li>b0..b3 the settings mask as intv1 - 0x140107994..0x1401079be assembles
     *     {@code (b2<<24)|(b3<<16)|(b0<<8)|b1}. In 947 the mask was intv2 and came LAST.</li>
     * <li>b4..b5 toSlot, plain big-endian u16 (0x1401079cf {@code movzx eax, word} plus the
     *     {@code rol ax,8} byte-swap under the endianness check).</li>
     * <li>b6..b7 fromSlot as ushort128 - 0x1401079eb applies {@code add eax,-0x80} to the LOW
     *     byte b7, so the +128 bias on that byte is new in 950; 947 sent it unbiased.</li>
     * <li>b8..b11 the component hash as intle (0x140107a0e..0x140107a3c).</li>
     * </ul>
     * The 0xFFFF-to--1 folds at 0x140107a42 and 0x140107a4d are what make the self sentinel
     * work, and they are why the -1 fromSlot has to be written as {@code ff 7f}: the client
     * subtracts 128 from the low byte before comparing, so an unbiased {@code ff ff} would fold
     * to 0xFF7F and address a real slot instead of the component itself.
     */
    @Test public void interfaceEventsLeadWithTheIntv1MaskAndSelfSentinel() {
        // 947 baseline: 23 3412 5678 0500c105 23016745
        assertArrayEquals(hex("18 45670123 5678 12b4 0500c105"),
                Native950Packets.interfaceEvents(1473, 5, 0x1234, 0x5678, 0x01234567).frame(() -> 0));
        // 947 baseline: ffffffff 0500c105 00000000
        assertArrayEquals(hex("00000000 ffff ff7f 0500c105"),
                Native950Packets.interfaceEvents(1473, 5, -1, -1, 0).payload());
    }

    /**
     * IF_CLOSESUB, 947 opcode 33 -&gt; 950 opcode 69, fixed 4 bytes.
     *
     * <p>The hash is no longer little-endian: 950 parser 0x140107b00 assembles
     * {@code (b1<<24)|(b0<<16)|(b3<<8)|b2} at 0x140107b22..0x140107b4a, i.e. the intv2
     * permutation, so the server writes {@code h>>>16, h>>>24, h, h>>>8}. Sending the 947 order
     * closes whatever component that permuted hash happens to name, silently.
     */
    @Test public void closesAttachedInterfaceByIntv2PermutedParentHash() {
        // 947 baseline: 21 b702c505
        assertArrayEquals(hex("45 c505b702"), Native950Packets.closeSub(1477, 695).frame(() -> 0));
        rejects(() -> Native950Packets.closeSub(1477, 65536));
    }

    /**
     * IF_SETTEXT, 947 opcode 2 -&gt; 950 opcode 115. Only the opcode moved.
     *
     * <p>950 parser 0x140107690 reads a 4-byte dword and byte-swaps it (0x1401076ce..0x1401076ef,
     * the standard {@code _byteswap_ulong} idiom) - a plain big-endian u32 hash - and then a
     * NUL-terminated string, exactly as 947 did.
     */
    @Test public void interfaceTextUsesBigEndianHashAndCp1252Terminator() {
        // 947 baseline: 02 000c 05c10005 436f696e73208000
        assertArrayEquals(hex("73 000c 05c10005 436f696e73208000"),
                Native950Packets.interfaceText(1473, 5, "Coins \u20ac").frame(() -> 0));
        rejects(() -> Native950Packets.interfaceText(1473, 5, "bad\0text"));
        rejects(() -> Native950Packets.interfaceText(1473, 5, "\uD83D\uDE00"));
    }

    /**
     * MESSAGE_GAME, 947 opcode 105 -&gt; 950 opcode 33. Only the opcode moved.
     *
     * <p>950 parser 0x1400f1ed0: the smart type is still a byte below 0x80, otherwise a
     * big-endian u16 with 0x8000 added back in 16-bit arithmetic (0x1400f1f14..0x1400f1f4a),
     * so {@code type + 32768} on the wire still decodes to {@code type}. Then a big-endian i32
     * and the sender flag byte, unchanged.
     */
    @Test public void gameMessageUsesByteLengthAndSmartType() {
        // 947 baseline: 69 0b 00 00000000 00 46756c6c00
        assertArrayEquals(hex("21 0b 00 00000000 00 46756c6c00"),
                Native950Packets.gameMessage(0, "Full").frame(() -> 0));
        assertArrayEquals(hex("8080 00000000 00 00"), Native950Packets.gameMessage(128, "").payload());
        rejects(() -> Native950Packets.gameMessage(32768, ""));
        rejects(() -> Native950Packets.gameMessage(0, new String(new char[250]).replace('\0', 'a')));
    }

    /**
     * VARBIT_SMALL, 947 opcode 50 -&gt; 950 opcode 28, fixed 3 bytes; the fields swapped ends
     * (plan A5).
     *
     * <p>950 parser 0x1401421e0: the FIRST byte is the value, biased - {@code add al,0x80} at
     * 0x140142202 - and it is the third argument handed to the setter at 0x140142264. The id is
     * the little-endian pair that follows, assembled at 0x14014220f/0x140142215/0x14014223c.
     * 947 sent id-then-value. The byte values are identical modulo 256 (947 wrote value+128, the
     * plan writes value-128), so this row is a pure reorder - and a pure reorder is exactly the
     * kind of change that produces a plausible-looking varbit write to the wrong id.
     *
     * <p>Trap for whoever debugs a "varbit that does nothing": 0x140142226 gates the whole write
     * on {@code cmp dword [rax+0x3c], 4}, the definition provider kind. A wrong provider state
     * no-ops the packet with correct bytes on the wire.
     */
    @Test public void varbitSmallLeadsWithTheBiasedValueThenTheLittleEndianId() {
        // 947 baseline: 32 85b082
        assertArrayEquals(hex("1c 8285b0"), Native950Packets.varbitSmall(45189, 2).frame(() -> 0));
        assertArrayEquals(hex("7fffff"), Native950Packets.varbitSmall(65535, 255).payload()); // 947: ffff7f
        assertArrayEquals(hex("800000"), Native950Packets.varbitSmall(0, 0).payload());       // 947: 000080
        rejects(() -> Native950Packets.varbitSmall(65536, 0));
        rejects(() -> Native950Packets.varbitSmall(1, -1));
        rejects(() -> Native950Packets.varbitSmall(1, 256));
    }

    /**
     * VARP_LARGE, 947 opcode 111 -&gt; 950 opcode 4, fixed 6 bytes.
     *
     * <p>950 parser 0x140142430: the id comes FIRST as ushortle128 - {@code add eax,-0x80} on
     * b0 at 0x140142463, b1 shifted in as the high byte at 0x14014246a - and the value follows
     * at b2..b5 as intv1, reassembled {@code (b4<<24)|(b5<<16)|(b2<<8)|b3} at
     * 0x1401424b1..0x1401424d5.
     *
     * <p>The value permutation is the one field in this packet that did NOT change: 947 also
     * sent intv1. What moved is the id - to the front, and with a +128 bias on its low byte that
     * 947 did not apply. That is the whole diff, and it is why the two revisions' bodies look
     * deceptively similar.
     */
    @Test public void playerVariableLeadsWithTheBiasedIdThenTheIntv1Value() {
        // 947 baseline: 6f 56781234 0b23
        assertArrayEquals(hex("04 8b23 56781234"), Native950Packets.varp(8971, 0x12345678).frame(() -> 0));
        assertArrayEquals(hex("8b23 02580000"), Native950Packets.varp(8971, 600).payload());  // 947: 02580000 0b23
        assertArrayEquals(hex("8000 00000000"), Native950Packets.varp(0, 0).payload());       // 947: 00000000 0000
        assertArrayEquals(hex("7fff ffffffff"), Native950Packets.varp(65535, -1).payload());  // 947: ffffffff ffff
        rejects(() -> Native950Packets.varp(-1, 0));
        rejects(() -> Native950Packets.varp(65536, 0));
    }

    /**
     * Argument validation runs before any framing, on both revisions.
     *
     * <p>The oversized case is still rejected after the u24 widening, and by a wider margin:
     * 10000 entries now cost 5 + 10000 * (3 + 1 + 4) = 80005 body bytes against the 65535 a
     * u16-framed packet can carry, where the 947 u16 item field cost 70005.
     */
    @Test public void itemIdsUseTheWholeNativeUnsigned24BitContainerField() {
        assertArrayEquals(hex("005d 00 0003 000001 01 010000 02 ffffff 03"),
                Native950Packets.inventoryFull(93, false, new int[]{0,65535,0xfffffe}, new int[]{1,2,3}).payload());
    }

    @Test public void invalidSlotsItemsAmountsAndOversizedPacketsFailBeforeFraming() {
        rejects(() -> Native950Packets.inventoryFull(93, false, new int[] {1}, new int[0]));
        rejects(() -> Native950Packets.inventoryFull(93, false, new int[] {-1}, new int[] {1}));
        rejects(() -> Native950Packets.inventoryFull(93, false, new int[] {1}, new int[] {0}));
        rejects(() -> Native950Packets.inventoryFull(93, false, new int[] {0xffffff}, new int[] {1}));
        rejects(() -> Native950Packets.inventoryFull(65536, false, new int[0], new int[0]));
        rejects(() -> Native950Packets.inventorySlots(93, false, new int[] {32768}, new int[] {1}, new int[] {1}));
        rejects(() -> Native950Packets.inventorySlots(93, false, new int[0], new int[] {1}, new int[] {1}));
        rejects(() -> Native950Packets.interfaceEvents(1473, 5, 10, 9, 2));
        rejects(() -> Native950Packets.interfaceEvents(1473, 5, -2, 0, 2));
        rejects(() -> Native950Packets.interfaceEvents(1473, 5, 0, 65535, 2));
        int[] ids = new int[10000], amounts = new int[10000];
        java.util.Arrays.fill(amounts, Integer.MAX_VALUE);
        rejects(() -> Native950Packets.inventoryFull(93, false, ids, amounts));
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid packet accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        byte[] bytes = new byte[compact.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) Integer.parseInt(compact.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
