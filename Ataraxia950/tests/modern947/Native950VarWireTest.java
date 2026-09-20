package modern947;

import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Literal fixtures for the CONFIRMED native 947-3 variable family, taken from the byte
 * examples in OpenNXT/data/prot/947/generated/native947-3/verified/vars/*.md. Independent
 * of the writers: every expected array is transcribed from the evidence note, not produced
 * by the code under test.
 */
public final class Native950VarWireTest {

    // --- VARP_SMALL, opcode 79 (947: 10), size 3 ----------------------------------------------

    @Test public void smallPlayerVariableLeadsWithThePlainValueThenTheBiasedBigEndianId() {
        // 950: b0 = value as a plain signed byte, b1 = id>>>8, b2 = id+128. 947 was the reverse -
        // little-endian id first, then a NEGATED value byte - so vars/VARP_SMALL.md's example
        // "varp 8971 = 3: 0b 23 fd" describes 947 only; on 950 the same call is 03 23 8b.
        Native950Packets.Packet packet = Native950Packets.varpSmall(8971, 3);
        assertEquals(ServerPacket.VARP_SMALL, packet.type());
        assertArrayEquals(hex("4f 03238b"), packet.frame(() -> 0));
        assertArrayEquals(hex("03238b"), packet.payload());
    }

    @Test public void smallPlayerVariableCoversTheWholeSignedByteRange() {
        assertArrayEquals(hex("000080"), Native950Packets.varpSmall(0, 0).payload());
        assertArrayEquals(hex("7fff7f"), Native950Packets.varpSmall(65535, 127).payload());
        // 950 reads the byte as a plain signed int8, so -128 is wire 0x80 and 1 is wire 0x01.
        assertArrayEquals(hex("800081"), Native950Packets.varpSmall(1, -128).payload());
        assertArrayEquals(hex("010081"), Native950Packets.varpSmall(1, 1).payload());
    }

    @Test public void smallPlayerVariableRejectsWidthsTheClientCannotDecode() {
        rejects(() -> Native950Packets.varpSmall(-1, 0));
        rejects(() -> Native950Packets.varpSmall(65536, 0));
        rejects(() -> Native950Packets.varpSmall(1, 128));
        rejects(() -> Native950Packets.varpSmall(1, -129));
    }

    // --- VARBIT_LARGE, opcode 82 (947: 71), size 6 --------------------------------------------

    @Test public void largeVarbitLeadsWithABigEndianValueAndEndsWithAPlainLittleEndianId() {
        // 950: b0..b3 value big-endian, b4..b5 id little-endian with no bias. Both fields changed
        // position AND endianness, so vars/VARBIT_LARGE.md's "varbit 1668 = 990: 06 04 de 03 00 00"
        // is a 947 row; on 950 the same call is 00 00 03 de 84 06.
        Native950Packets.Packet packet = Native950Packets.varbitLarge(1668, 990);
        assertEquals(ServerPacket.VARBIT_LARGE, packet.type());
        assertArrayEquals(hex("52 000003de8406"), packet.frame(() -> 0));
    }

    @Test public void largeVarbitCarriesTheFullSignedIntAndBothIdBoundaries() {
        assertArrayEquals(hex("000000000000"), Native950Packets.varbitLarge(0, 0).payload());
        assertArrayEquals(hex("ffffffffffff"), Native950Packets.varbitLarge(65535, -1).payload());
        assertArrayEquals(hex("7fffffff0000"), Native950Packets.varbitLarge(0, Integer.MAX_VALUE).payload());
        assertArrayEquals(hex("800000000000"), Native950Packets.varbitLarge(0, Integer.MIN_VALUE).payload());
        rejects(() -> Native950Packets.varbitLarge(-1, 0));
        rejects(() -> Native950Packets.varbitLarge(65536, 0));
    }

    // --- CLIENT_SETVARC_SMALL 126/3 (947: 1) and CLIENT_SETVARC_LARGE 119/6 (947: 112) --------

    @Test public void smallClientVariableLeadsWithTheLittleEndianIdAndEndsWith128MinusValue() {
        // 950: b0..b1 id little-endian, b2 = 128 - value. 947 led with (value - 128); note the two
        // transforms coincide only at value 0, so this is not a pure reorder.
        // vars/CLIENT_SETVARC_SMALL.md: "varc 6709 = 0: 80 35 1a".
        Native950Packets.Packet packet = Native950Packets.varcSmall(6709, 0);
        assertEquals(ServerPacket.CLIENT_SETVARC_SMALL, packet.type());
        assertArrayEquals(hex("7e 351a80"), packet.frame(() -> 0));
        assertArrayEquals(hex("000000"), Native950Packets.varcSmall(0, -128).payload());
        assertArrayEquals(hex("ffff01"), Native950Packets.varcSmall(65535, 127).payload());
        rejects(() -> Native950Packets.varcSmall(1, 128));
        rejects(() -> Native950Packets.varcSmall(1, -129));
        rejects(() -> Native950Packets.varcSmall(65536, 0));
    }

    @Test public void largeClientVariableLeadsWithTheBiasedIdThenTheClientsMixedValueOrder() {
        // 950 (parser 0x140141DB0): b0..b1 id ushort128, then the value as b2=v>>>16, b3=v>>>24,
        // b4=v, b5=v>>>8. 947 wrote a plain big-endian value first and the id last.
        // vars/CLIENT_SETVARC_LARGE.md: "varc 6709 = 100000: 00 01 86 a0 b5 1a".
        Native950Packets.Packet packet = Native950Packets.varcLarge(6709, 100000);
        assertEquals(ServerPacket.CLIENT_SETVARC_LARGE, packet.type());
        assertArrayEquals(hex("77 1ab50100a086"), packet.frame(() -> 0));
        assertArrayEquals(hex("008000000000"), Native950Packets.varcLarge(0, 0).payload());
        assertArrayEquals(hex("ff7fffffffff"), Native950Packets.varcLarge(65535, -1).payload());
        assertArrayEquals(hex("008000800000"), Native950Packets.varcLarge(0, Integer.MIN_VALUE).payload());
        rejects(() -> Native950Packets.varcLarge(-1, 0));
        rejects(() -> Native950Packets.varcLarge(65536, 0));
    }

    @Test public void clientVariableHelperPicksTheSmallestFormTheValueFitsIn() {
        assertEquals(ServerPacket.CLIENT_SETVARC_SMALL, Native950Packets.varc(6709, 127).type());
        assertEquals(ServerPacket.CLIENT_SETVARC_SMALL, Native950Packets.varc(6709, -128).type());
        assertEquals(ServerPacket.CLIENT_SETVARC_LARGE, Native950Packets.varc(6709, 128).type());
        assertEquals(ServerPacket.CLIENT_SETVARC_LARGE, Native950Packets.varc(6709, -129).type());
        assertArrayEquals(Native950Packets.varcSmall(6709, 0).payload(), Native950Packets.varc(6709, 0).payload());
        assertArrayEquals(Native950Packets.varcLarge(6709, 100000).payload(),
                Native950Packets.varc(6709, 100000).payload());
        rejects(() -> Native950Packets.varc(65536, 0));
    }

    // --- CLIENT_SETVARCBIT_SMALL 48/3 (947: 115) and CLIENT_SETVARCBIT_LARGE 87/6 (947: 55) ---

    @Test public void smallClientVarbitSends128MinusValueThenAnUnbiasedBigEndianId() {
        // 950: b0 = 128 - value, b1 = id>>>8, b2 = id. 947 negated the value and biased the id's
        // low byte by +128, so vars/CLIENT_SETVARCBIT_SMALL.md's "varcbit 1668 = 5: fb 06 04" is a
        // 947 row; on 950 the same call is 7b 06 84.
        Native950Packets.Packet packet = Native950Packets.varcBitSmall(1668, 5);
        assertEquals(ServerPacket.CLIENT_SETVARCBIT_SMALL, packet.type());
        assertArrayEquals(hex("30 7b0684"), packet.frame(() -> 0));
        assertArrayEquals(hex("800684"), Native950Packets.varcBitSmall(1668, 0).payload());
        assertArrayEquals(hex("010000"), Native950Packets.varcBitSmall(0, 127).payload());
        assertArrayEquals(hex("01ffff"), Native950Packets.varcBitSmall(65535, 127).payload());
    }

    @Test public void smallClientVarbitRefusesValuesTheClientSetterDiscards() {
        // 0x1400CC53B..0x1400CC571 keeps the old word for value < 0 or value > mask.
        rejects(() -> Native950Packets.varcBitSmall(1668, -1));
        rejects(() -> Native950Packets.varcBitSmall(1668, 128));
        rejects(() -> Native950Packets.varcBitSmall(65536, 0));
    }

    @Test public void largeClientVarbitIsAPlainLittleEndianValueThenTheBiasedBigEndianId() {
        // 950: b0..b3 value little-endian, b4 = id>>>8, b5 = id+128. 947 used the intv1 value
        // permutation and a plain little-endian id, so vars/CLIENT_SETVARCBIT_LARGE.md's
        // "varcbit 1668 = 990: 03 de 00 00 84 06" is a 947 row; on 950 it is de 03 00 00 06 84.
        Native950Packets.Packet packet = Native950Packets.varcBitLarge(1668, 990);
        assertEquals(ServerPacket.CLIENT_SETVARCBIT_LARGE, packet.type());
        assertArrayEquals(hex("57 de0300000604"), packet.frame(() -> 0));
        assertArrayEquals(hex("000000000080"), Native950Packets.varcBitLarge(0, 0).payload());
        assertArrayEquals(hex("ffffff7fff7f"), Native950Packets.varcBitLarge(65535, Integer.MAX_VALUE).payload());
        rejects(() -> Native950Packets.varcBitLarge(1668, -1));
        rejects(() -> Native950Packets.varcBitLarge(65536, 0));
    }

    // --- CLIENT_SETVARCSTR_SMALL 30/-1 (947: 67) and CLIENT_SETVARCSTR_LARGE 81/-2 (947: 15) --

    @Test public void clientStringVariableUsesTheByteLengthFormWithTheIdFirst() {
        // 950 byte frame: id first (big-endian, +128 on the low byte), then the terminated string.
        // 947's byte frame was the exact reverse, so the two frames swapped roles between the
        // revisions - which is also why the size column cannot be carried over for this pair.
        Native950Packets.Packet packet = Native950Packets.varcString(2508, "Hi");
        assertEquals(ServerPacket.CLIENT_SETVARCSTR_SMALL, packet.type());
        assertArrayEquals(hex("1e 05 094c 486900"), packet.frame(() -> 0));
        assertArrayEquals(hex("094c 00"), Native950Packets.varcString(2508, "").payload());
    }

    @Test public void clientStringVariableSwitchesToTheShortLengthFormWithTheIdLast() {
        // The 255-byte body is the last one the unsigned-byte length can carry.
        String longest = repeat('a', 252);
        Native950Packets.Packet small = Native950Packets.varcString(2508, longest);
        assertEquals(ServerPacket.CLIENT_SETVARCSTR_SMALL, small.type());
        assertEquals(255, small.payload().length);
        assertArrayEquals(hex("094c"), head(small.payload(), 2));

        Native950Packets.Packet large = Native950Packets.varcString(2508, longest + "a");
        assertEquals(ServerPacket.CLIENT_SETVARCSTR_LARGE, large.type());
        // 950's short frame is string, NUL, then the id PLAIN little-endian - no +128 bias,
        // which is the one transform difference between the two frames on this revision.
        assertArrayEquals(hex("61"), head(large.payload(), 1));
        assertArrayEquals(hex("cc09"), tail(large.payload(), 2));
        assertEquals(256, large.payload().length);
        byte[] framed = large.frame(() -> 0);
        assertArrayEquals(hex("51 0100"), head(framed, 3));
        assertEquals(259, framed.length);
    }

    @Test public void clientStringVariableRejectsUnencodableAndOversizedText() {
        rejects(() -> Native950Packets.varcString(2508, "bad\0text"));
        rejects(() -> Native950Packets.varcString(2508, "\uD83D\uDE00"));
        rejects(() -> Native950Packets.varcString(65536, "Hi"));
        rejects(() -> Native950Packets.varcString(2508, repeat('a', 65533)));
        try { Native950Packets.varcString(2508, (String) null); fail("null text accepted"); }
        catch (NullPointerException expected) { }
    }

    // --- RESET_CLIENT_VARCACHE, opcode 23 (947: 48), size 0 -----------------------------------

    @Test public void variableCacheResetIsAZeroLengthBody() {
        Native950Packets.Packet packet = Native950Packets.resetClientVarCache();
        assertEquals(ServerPacket.RESET_CLIENT_VARCACHE, packet.type());
        assertEquals(0, packet.payload().length);
        assertArrayEquals(hex("17"), packet.frame(() -> 0));
    }

    // --- Framing through the transport with the real outgoing ISAAC ---------------------------

    @Test public void everyVarWriterFramesThroughTheTransportWithTheRealOutgoingIsaac() {
        Native950GameTransport transport =
                Native950GameTransport.afterAuthentication(new int[] {1, 2, 3, 4}, Thread.currentThread());
        EmbeddedChannel channel = new EmbeddedChannel(transport);
        try {
            // afterAuthentication derives the outgoing stream from the login seeds plus 50.
            Native950Isaac reference = new Native950Isaac(new int[] {51, 52, 53, 54});
            Native950Packets.Packet[] written = {
                Native950Packets.varpSmall(8971, 3),
                Native950Packets.varbitLarge(1668, 990),
                Native950Packets.varcSmall(6709, 0),
                Native950Packets.varcLarge(6709, 100000),
                Native950Packets.varcBitSmall(1668, 5),
                Native950Packets.varcBitLarge(1668, 990),
                Native950Packets.varcString(2508, "Hi"),
                Native950Packets.resetClientVarCache()
            };
            byte[] first = null;
            for (int i = 0; i < written.length; i++) {
                channel.writeOutbound(written[i]);
                byte[] expected = written[i].frame(reference);
                byte[] actual = readOutput(channel);
                assertArrayEquals("framed packet " + i, expected, actual);
                if (i == 0) first = actual;
            }
            // First outgoing ISAAC word for login seeds 1,2,3,4 is 0x21fc9d18 (pinned by
            // Native950TransportTest), so opcode 10 leaves the wire as 0x22.
            assertArrayEquals(hex("67 03238b"), first);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    // --- ServerPacket table vs the promoted name and size tables ------------------------------

    /** Opcode and size of every row promoted to verifiedNames.toml on 2026-09-07. */
    private static Map<String, int[]> promoted() {
        Map<String, int[]> expected = new LinkedHashMap<String, int[]>();
        // 950 opcodes and sizes, from the client's own descriptor table. Note the two
        // CLIENT_SETVARCSTR rows: the frame widths did not move with the names between
        // revisions, so on 950 the -1 byte frame is opcode 30 and the -2 short frame is 81.
        //
        // This map is the VAR/STAT/VITAL promotion set and nothing else. Do NOT add MUSIC
        // (107, 5) or NPC_INFO (80, -2) here just because the 950 regeneration turned them from
        // underived sentinels into real constants: this map feeds
        // serverPacketNamesAgreeWithVerifiedNamesTomlWhenItIsAvailable, which asserts that the
        // evidence tree's verifiedNames.toml carries that exact SYMBOLIC NAME at that opcode.
        // The port plan's GUESS list item 5 says the MESSAGE_GAME / MUSIC / NPC_INFO / inventory
        // *names* are meaning-derived conventions, not recovered symbols - the opcode-to-parser
        // binding is certain, the name is not - so pinning their names against a names table
        // would be asserting a convention. Their opcode and size are already covered: the
        // whole-enum loop below checks them for collisions, and
        // serverPacketSizesAgreeWithTheExtractedConstructorTableWhenAvailable checks 107 = 5 and
        // 80 = -2 against OpenNXT/data/prot/950/serverProtSizes.toml, where both rows exist.
        expected.put("CLIENT_SETVARC_SMALL", new int[] {126, 3});
        expected.put("VARP_SMALL", new int[] {79, 3});
        expected.put("CLIENT_SETVARCSTR_LARGE", new int[] {81, -2});
        expected.put("RESET_CLIENT_VARCACHE", new int[] {23, 0});
        expected.put("UPDATE_REBOOT_TIMER", new int[] {31, 2});
        expected.put("CLIENT_SETVARCBIT_LARGE", new int[] {87, 6});
        expected.put("UPDATE_STAT", new int[] {92, 6});
        expected.put("CLIENT_SETVARCSTR_SMALL", new int[] {30, -1});
        expected.put("VARBIT_LARGE", new int[] {82, 6});
        expected.put("UPDATE_RUNWEIGHT", new int[] {7, 2});
        expected.put("CLIENT_SETVARC_LARGE", new int[] {119, 6});
        expected.put("CLIENT_SETVARCBIT_SMALL", new int[] {48, 3});
        expected.put("UPDATE_RUNENERGY", new int[] {21, 1});
        // Newly derived from exact950 parser 0x140114a40; not the old speculative var-wire use.
        expected.put("MAP_PROJANIM_HALFSQ", new int[] {154, 21});
        return expected;
    }

    @Test public void serverPacketTableCarriesEveryPromotedRowWithItsVerifiedOpcodeAndSize() {
        Map<String, int[]> expected = promoted();
        for (Map.Entry<String, int[]> row : expected.entrySet()) {
            ServerPacket packet = ServerPacket.valueOf(row.getKey());
            assertEquals(row.getKey() + " opcode", row.getValue()[0], packet.opcode());
            assertEquals(row.getKey() + " size", row.getValue()[1], packet.size());
        }
        // No two entries may claim one opcode, and no CANDIDATE or REFUTED opcode may appear.
        int[] forbidden = {175, 215, 170, 161, 99, 120, 25, 213, 185, 163, 191};
        for (ServerPacket packet : ServerPacket.values()) {
            // An underived row has no opcode at all - both accessors throw by design, so that it
            // can never be framed onto a client that parses opcodes positionally - and therefore
            // cannot collide with anything. Skip it, the same way the two evidence cross-checks
            // below do. Before the 950 regeneration MUSIC and NPC_INFO were such sentinels and
            // this loop blew up on them with an UnsupportedOperationException rather than an
            // assertion; both now carry real constants (107/5 and 80/-2) and nothing is skipped.
            if (!packet.isDerived()) continue;
            for (ServerPacket other : ServerPacket.values()) {
                if (packet == other || !other.isDerived()) continue;
                assertNotEquals(packet + " duplicates " + other, packet.opcode(), other.opcode());
            }
            for (int opcode : forbidden)
                assertNotEquals("unverified opcode " + opcode + " implemented", opcode, packet.opcode());
        }
    }

    @Test public void serverPacketNamesAgreeWithVerifiedNamesTomlWhenItIsAvailable() throws Exception {
        Path names = evidence("verified/verifiedNames.toml");
        Assume.assumeTrue("verifiedNames.toml is optional outside the migration workspace",
                names != null && java.nio.file.Files.isRegularFile(names));
        Map<Integer, String> table = readToml(names, Pattern.compile("\\s*(\\d+)\\s*=\\s*\"([A-Z0-9_]+)\"\\s*"));
        for (Map.Entry<String, int[]> row : promoted().entrySet())
            assertEquals("verifiedNames.toml " + row.getValue()[0],
                    row.getKey(), table.get(Integer.valueOf(row.getValue()[0])));
        for (ServerPacket packet : ServerPacket.values()) {
            if (!packet.isDerived()) continue;
            String verified = table.get(Integer.valueOf(packet.opcode()));
            if (verified != null) assertEquals("opcode " + packet.opcode(), verified, packet.name());
        }
    }

    @Test public void serverPacketSizesAgreeWithTheExtractedConstructorTableWhenAvailable() throws Exception {
        Path sizes = evidence("serverProtSizes.toml");
        Assume.assumeTrue("serverProtSizes.toml is optional outside the migration workspace",
                sizes != null && java.nio.file.Files.isRegularFile(sizes));
        Map<Integer, String> table = readToml(sizes, Pattern.compile("\\s*\"?(\\d+)\"?\\s*=\\s*(-?\\d+)\\s*"));
        for (ServerPacket packet : ServerPacket.values()) {
            // An underived packet has no opcode to look up; asking for one throws by design.
            if (!packet.isDerived()) continue;
            String size = table.get(Integer.valueOf(packet.opcode()));
            assertNotNull("no extracted size for opcode " + packet.opcode(), size);
            assertEquals("opcode " + packet.opcode(), Integer.parseInt(size), packet.size());
        }
    }

    /**
     * The 950 protocol tables. This used to point at AstraNXT's native947-3 directory, which made
     * the two cross-checks below compare 950 opcodes against 947's meanings - opcode 1 came back
     * as CLIENT_SETVARC_SMALL because that is what 1 meant on 947, not because anything was wrong
     * with the 950 table.
     */
    private static final String EVIDENCE_ROOT =
            "C:\\Users\\developer\\Desktop\\950RevTest\\OpenNXT\\data\\prot\\950";

    /** Prefers the workspace-relative copy so the suite also runs from the Gradle project root. */
    private static Path evidence(String relative) {
        String override = System.getProperty("modern950.evidenceRoot");
        Path[] candidates = {
            override == null ? null : Paths.get(override, relative),
            Paths.get("../OpenNXT/data/prot/950", relative),
            Paths.get(EVIDENCE_ROOT, relative)
        };
        for (Path candidate : candidates)
            if (candidate != null && java.nio.file.Files.isRegularFile(candidate)) return candidate;
        return null;
    }

    private static Map<Integer, String> readToml(Path file, Pattern row) throws Exception {
        Map<Integer, String> table = new LinkedHashMap<Integer, String>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            Matcher matcher = row.matcher(line);
            if (matcher.matches()) table.put(Integer.valueOf(matcher.group(1)), matcher.group(2));
        }
        assertFalse("empty evidence table " + file, table.isEmpty());
        return table;
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

    private static String repeat(char value, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) text.append(value);
        return text.toString();
    }

    private static byte[] head(byte[] bytes, int count) { return java.util.Arrays.copyOf(bytes, count); }
    private static byte[] tail(byte[] bytes, int count) {
        return java.util.Arrays.copyOfRange(bytes, bytes.length - count, bytes.length);
    }

    private static void rejects(Runnable action) {
        try { action.run(); fail("Invalid packet accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static byte[] hex(String value) {
        String compact = value.replace(" ", "");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int i = 0; i < compact.length(); i += 2)
            bytes.write(Integer.parseInt(compact.substring(i, i + 2), 16));
        return bytes.toByteArray();
    }
}
