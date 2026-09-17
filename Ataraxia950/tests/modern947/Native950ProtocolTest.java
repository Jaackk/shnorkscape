package modern947;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950InboundDecoder;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Dependency-free native wire fixtures; run with assertions enabled or disabled. */
public final class Native950ProtocolTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        walkFixtures();
        fragmentedFraming();
        protocolFailures();
        outboundFixtures();
        musicFixtures();
        sceneAndPlayerFixtures();
        if (args.length > 0) compareExtractedSizeTable(args[0]);
        System.out.println("Native950ProtocolTest: " + checks + " checks passed");
    }

    /**
     * The 950 walk frame: ground click opcode 88 (5 bytes), minimap click opcode 78 (18 bytes).
     *
     * b0..b1 y plain big-endian, b2 modifier + 128, b3 (x &amp; 255) + 128, b4 x >>> 8. Three
     * things changed from 947, which sent y as ushort128, x plain big-endian, and the modifier
     * NEGATED as 128 - m. The sign flip is the one that matters most: a 947-shaped decoder reading
     * a 950 frame gets a plausible modifier for the wrong mouse button rather than an error.
     *
     * The same layout is implemented independently in OpenNXT's
     * Native950WorldBootstrap.readWalkRequest950, which has been exercised against a live 950
     * client - so this is one of the few inbound rows with empirical rather than static evidence.
     */
    static void walkFixtures() {
        // 947 sent this click as 12 b4 23 45 7f.
        byte[] prefix = hex("123481c523");
        Native950Actions.WalkRequest ground = Native950Actions.decodeWalk(88, prefix);
        equal(0x2345, ground.x(), "ground X"); equal(0x1234, ground.y(), "ground Y");
        equal(1, ground.modifier(), "modifier"); check(!ground.isMinimap(), "ground kind");
        byte[] minimap = concat(prefix, hex("ffff000039000059123456783f"));
        Native950Actions.WalkRequest map = Native950Actions.decodeWalk(78, minimap);
        equal(ground.x(), map.x(), "minimap X"); equal(ground.y(), map.y(), "minimap Y");
        check(map.isMinimap(), "minimap kind");
        check(Native950Actions.decodeWalk(88, minimap) == null, "ground size rejected");
        check(Native950Actions.decodeWalk(78, prefix) == null, "minimap size rejected");
        check(Native950Actions.decodeWalk(4, prefix) == null, "unrelated opcode rejected");
        // 0x4000 is one past the 14-bit world on each axis.
        check(Native950Actions.decodeWalk(88, hex("4000808000")) == null, "Y overflow rejected");
        check(Native950Actions.decodeWalk(88, hex("0000808040")) == null, "X overflow rejected");
        // 947's own walk opcode is a different packet on 950 and must not decode as a walk.
        check(Native950Actions.decodeWalk(3, prefix) == null, "947 walk opcode rejected");
    }

    static void fragmentedFraming() {
        // Independently assembled stream, rebuilt against 950's own size table: fixed walk
        // (88, 5 bytes), opcode 128 as a REAL five-byte packet rather than 947's two-byte
        // escape, variable-byte (3), variable-short (14), zero-length keepalive (104).
        // Opcode 128 carrying a body is the point of this fixture: under 947 framing its high
        // bit made it an escape prefix, and treating it that way on 950 silently swallows the
        // following packet instead of failing, which is why the split loop runs over every
        // byte boundary rather than only the packet edges.
        byte[] wire = hex("5812b423457f80aabbccddee03036162630e0005010203040568");
        for (int split = 0; split <= wire.length; split++) {
            Native950InboundDecoder decoder = new Native950InboundDecoder(() -> 0);
            List<Native950InboundDecoder.Frame> frames = new ArrayList<Native950InboundDecoder.Frame>();
            frames.addAll(decoder.feed(Arrays.copyOfRange(wire, 0, split)));
            frames.addAll(decoder.feed(Arrays.copyOfRange(wire, split, wire.length)));
            verifyFrames(frames);
        }
        // Different cipher bytes on each opcode byte, including the two halves of opcode128.
        byte[] encrypted = wire.clone();
        int[] opcodeOffsets = {0, 6, 12, 17, 25};
        for (int i = 0; i < opcodeOffsets.length; i++) encrypted[opcodeOffsets[i]] += (byte) (i + 11);
        AtomicInteger sequence = new AtomicInteger(11);
        Native950InboundDecoder decoder = new Native950InboundDecoder(sequence::getAndIncrement);
        List<Native950InboundDecoder.Frame> frames = new ArrayList<Native950InboundDecoder.Frame>();
        for (byte value : encrypted) frames.addAll(decoder.feed(new byte[] {value}));
        verifyFrames(frames);
        // Five opcodes, not 947's six: there is no second opcode byte left to decrypt.
        equal(16, sequence.get(), "ISAAC consumed once per opcode byte across fragmented reads");
        Native950InboundDecoder emptyVariable = new Native950InboundDecoder(() -> 0);
        equal(3, emptyVariable.feed(hex("03000e000068")).size(), "empty variable payloads do not stall");

        // Largest unsigned-short packet fragmented across its entire body.
        ByteArrayOutputStream large = new ByteArrayOutputStream();
        // 65535 is the true bound - the width of the length field. An earlier decoder capped
        // this at 0x2710 on the belief it was "the client's own cap"; the only 0x2710 in the
        // parser range is a 10-second timer (0x140149759 add rax, 0x2710), so that cap would
        // have closed the connection on legitimate traffic. This fixture is what keeps it gone.
        large.write(14); large.write(255); large.write(255);
        byte[] payload = new byte[65535]; Arrays.fill(payload, (byte) 0xA5);
        large.write(payload, 0, payload.length); large.write(104);
        List<Native950InboundDecoder.Frame> largest = new Native950InboundDecoder(() -> 0).feed(large.toByteArray());
        equal(2, largest.size(), "maximum payload preserves following packet");
        bytes(payload, largest.get(0).payload(), "maximum unsigned-short payload");
    }

    private static void verifyFrames(List<Native950InboundDecoder.Frame> frames) {
        equal(5, frames.size(), "all packets framed");
        int[] expectedOpcodes = {88, 128, 3, 14, 104};
        for (int i = 0; i < expectedOpcodes.length; i++) equal(expectedOpcodes[i], frames.get(i).opcode(), "opcode");
        bytes(hex("12b423457f"), frames.get(0).payload(), "walk body");
        bytes(hex("aabbccddee"), frames.get(1).payload(), "opcode 128 is a packet, not an escape");
        bytes(hex("616263"), frames.get(2).payload(), "variable byte body");
        bytes(hex("0102030405"), frames.get(3).payload(), "variable short body");
        equal(0, frames.get(4).length(), "final empty body");
    }

    static void protocolFailures() {
        Native950InboundDecoder decoder = new Native950InboundDecoder(() -> 0);
        // 0x80 is a real 950 opcode now, so the unknown-opcode probe has to sit above the
        // table: 950 describes 0..128 inclusive and nothing beyond it.
        fails(IllegalArgumentException.class, () -> decoder.feed(hex("ff")), "unknown size closes framing");
        fails(IllegalStateException.class, () -> decoder.feed(hex("58")), "failed framing cannot resume guessing");
        equal(Native950Protocol.UNKNOWN_SIZE, Native950Protocol.clientSize(-1), "negative opcode");
        equal(Native950Protocol.UNKNOWN_SIZE, Native950Protocol.clientSize(129), "unmapped opcode");
        equal(5, Native950Protocol.clientSize(128), "opcode 128 is described, not an escape");
        fails(IllegalArgumentException.class, () -> Native950Packets.packet(Native950Protocol.ServerPacket.IF_OPENTOP, new byte[18]), "fixed outbound length");
        fails(IllegalArgumentException.class, () -> Native950Packets.packet(Native950Protocol.ServerPacket.PLAYER_INFO, new byte[65536]), "oversize outbound");
        fails(IllegalArgumentException.class, () -> Native950Packets.singlePlayerWalkStep(0, 0), "empty walk");
        fails(IllegalArgumentException.class, () -> Native950Packets.singlePlayerWalkStep(2, -1), "nonadjacent walk");
        fails(IllegalArgumentException.class, () -> Native950Packets.singlePlayerAppearance(new byte[0]), "empty appearance");
        fails(IllegalArgumentException.class, () -> Native950Packets.singlePlayerAppearance(new byte[256]), "oversize appearance");
        fails(IllegalArgumentException.class, () -> Native950Packets.openTop(65536), "overflow interface");
        fails(IllegalArgumentException.class, () -> Native950Packets.runClientScript(1, Long.valueOf(2)), "unknown script argument");
        fails(IllegalArgumentException.class, () -> Native950Packets.runClientScript(1, "bad\0string"), "NUL string");
    }

    static void outboundFixtures() {
        bytes(hex("56340000000000000000000000000000000000"), Native950Packets.openTop(0x3456).payload(), "native top layout");
        bytes(hex("11223344000000000000000000000000d6347f00000000"),
                Native950Packets.openSub(0x1122, 0x3344, 0x3456, true).payload(), "native sub layout");
        bytes(hex("001c05c501"), Native950Packets.hideInterface(1477, 28, true).payload(), "native hide layout");
        bytes(hex("005c05c500"), Native950Packets.hideInterface(1477, 92, false).payload(), "native show layout");
        bytes(hex("6973690005060708616263000102030411223344"),
                Native950Packets.runClientScript(0x11223344, 0x01020304, "abc", 0x05060708).payload(), "script order and descriptor");
        bytes(hex("80a0"), Native950Packets.tickEnd().frame(() -> 0), "big outbound opcode");
        bytes(hex("80b7"), Native950Packets.keepAlive().frame(() -> 0), "verified keepalive");
        bytes(hex("240003007ff4"), Native950Packets.singlePlayerIdle().frame(() -> 0), "variable-short frame");
        AtomicInteger cipher = new AtomicInteger(1);
        bytes(hex("81a2"), Native950Packets.tickEnd().frame(cipher::getAndIncrement), "ISAAC on both opcode bytes");
        equal(3, cipher.get(), "outbound cipher consumption");
    }

    /**
     * MUSIC on 950: opcode 107, size 5, body = the id as a plain big-endian u32 then 128 - volume.
     *
     * This was fenced for a while because two of the port's documents disagreed: the opcode match
     * rated the 947 parser correspondence "weak", while the plan's table asserted 87 -> 107 with a
     * changed body. The plan wins, and not by preference - its appendix A9 claims every (opcode,
     * size) pair was re-checked against the 950 descriptor tables, and that check reproduces:
     * server opcode 107 is size 5, which is what the plan says MUSIC is. The same holds for all 60
     * rows of its inbound table. The opcode-match note was an earlier, weaker pass.
     *
     * Both fields changed. 947 permuted the id bytes as 1,0,3,2 and sent the volume unbiased, so
     * none of the byte strings below carry over.
     */
    static void musicFixtures() {
        equal(107, Native950Protocol.ServerPacket.MUSIC.opcode(), "verified music opcode");
        equal(5, Native950Protocol.ServerPacket.MUSIC.size(), "verified music body length");
        // 947 asserted 34127856ff for this row - the id permuted, the volume plain.
        bytes(hex("1234567881"), Native950Packets.music(0x12345678, 255).payload(), "music plain big-endian id and 128-minus volume");
        bytes(hex("6b00008ce381"), Native950Packets.music(36067, 255).frame(() -> 0), "Harmony resource frame");
        bytes(hex("ffffffff81"), Native950Packets.music(-1, 255).payload(), "negative-one music stop sentinel");
        bytes(hex("0000000080"), Native950Packets.music(0, 0).payload(), "music zero boundaries");
        bytes(hex("7fffffff00"), Native950Packets.music(Integer.MAX_VALUE, 128).payload(), "music positive int upper bound");
        fails(IllegalArgumentException.class, () -> Native950Packets.music(-2, 255), "noncanonical negative music ID rejected");
        fails(IllegalArgumentException.class, () -> Native950Packets.music(1, -1), "negative music volume rejected");
        fails(IllegalArgumentException.class, () -> Native950Packets.music(1, 256), "music volume overflow rejected");
    }

    static void sceneAndPlayerFixtures() {
        // 950 field order (see Native950Packets.sceneHeader): chunkY big-endian with +128 on
        // the low byte, mapSize 5, npcBits+128, two skipped bytes, chunkX big-endian, areaType,
        // then the two hashes. Not a permutation of 947's - the axes swapped ends.
        byte[] header = hex("011005870000019000031122334455667788");
        bytes(header, Native950Packets.rebuildScene(400, 400, 7, 3, 0x11223344, 0x55667788).payload(), "18-byte native scene header");
        byte[] initial = Native950Packets.initialSinglePlayerScene(1, 3200, 3200, 2, 7, 3, 0x11223344, 0x55667788).payload();
        equal(5137, initial.length, "initial scene total length");
        BitReader bits = new BitReader(initial);
        equal((2 << 28) | (3200 << 14) | 3200, bits.read(30), "local tile hash");
        for (int i = 0; i < 2046; i++) equal(0xC0000, bits.read(20), "external stationary region");
        bits.align(); equal(5119, bits.position(), "initial player prefix length");
        bytes(header, Arrays.copyOfRange(initial, bits.position(), initial.length), "scene header after bit prefix");
        bytes(hex("007ff4"), Native950Packets.singlePlayerIdle().payload(), "idle player passes");
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx == 0 && dy == 0) continue;
            bits = new BitReader(Native950Packets.singlePlayerWalkStep(dx, dy).payload());
            equal(1, bits.read(1), "moving"); equal(0, bits.read(1), "no mask");
            equal(3, bits.read(2), "relative movement kind"); equal(0, bits.read(1), "short form");
            equal(2, bits.read(3), "native WALK speed selector"); equal(0, bits.read(2), "same plane");
            equal(dx & 31, bits.read(5), "signed walk X"); equal(dy & 31, bits.read(5), "signed walk Y");
            bits.align(); equal(0, bits.read(1), "external no-update"); equal(3, bits.read(2), "external skip selector");
            equal(2045, bits.read(11), "external skip count"); bits.align(); equal(5, bits.position(), "walk body consumed");
        }
        for (int size : new int[] {1, 127, 128, 255}) {
            byte[] appearance = new byte[size]; for (int i = 0; i < size; i++) appearance[i] = (byte) i;
            byte[] body = Native950Packets.singlePlayerAppearance(appearance).payload();
            // Mask 0x20, not 947's 0x04: the 950 client tests `test r15b, 0x20` at 0x14012DC31.
            bytes(hex("c07ff4000020"), Arrays.copyOf(body, 6), "appearance-only mask prefix");
            equal(size, (body[6] + 128) & 255, "appearance transformed length");
            // The body is pre-biased b ^ 0x80: 950 installs transform script byte 0x02 at
            // .rdata 0x140B5FEC9 where 947 had 0x00, so the client's copy adds 0x80 on receive.
            // This is the fixture the plan calls the highest-risk item in the port - a wrong
            // framing here does not degrade, it faults the client at 0x140131d53.
            byte[] biased = new byte[size];
            for (int i = 0; i < size; i++) biased[i] = (byte) (appearance[i] ^ 0x80);
            bytes(biased, Arrays.copyOfRange(body, 7, body.length), "appearance body is pre-biased");
        }
    }

    static void compareExtractedSizeTable(String file) throws Exception {
        int count = 0;
        for (String line : Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)) {
            if (!line.matches("\\s*\\d+\\s*=\\s*-?\\d+\\s*")) continue;
            String[] fields = line.split("=");
            equal(Integer.parseInt(fields[1].trim()), Native950Protocol.clientSize(Integer.parseInt(fields[0].trim())), "native extracted size map");
            count++;
        }
        equal(129, count, "all native constructor sizes compared");
    }

    private static final class BitReader {
        private final byte[] bytes; private int bit;
        BitReader(byte[] bytes) { this.bytes = bytes; }
        int read(int count) { int n = 0; while (count-- > 0) { n = (n << 1) | ((bytes[bit >>> 3] >>> (7 - (bit & 7))) & 1); bit++; } return n; }
        void align() { bit = (bit + 7) & ~7; }
        int position() { return bit / 8; }
    }
    private static byte[] concat(byte[] a, byte[] b) { byte[] out = Arrays.copyOf(a, a.length + b.length); System.arraycopy(b, 0, out, a.length, b.length); return out; }
    private static byte[] hex(String value) { byte[] out = new byte[value.length() / 2]; for (int i = 0; i < out.length; i++) out[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16); return out; }
    private static void bytes(byte[] expected, byte[] actual, String label) { check(Arrays.equals(expected, actual), label + " expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual)); }
    private static void equal(int expected, int actual, String label) { check(expected == actual, label + " expected=" + expected + " actual=" + actual); }
    private static void check(boolean result, String label) { checks++; if (!result) throw new AssertionError(label); }
    private static void fails(Class<? extends Throwable> type, Runnable action, String label) { try { action.run(); } catch (Throwable error) { check(type.isInstance(error), label + " wrong exception: " + error); return; } throw new AssertionError(label + " did not fail"); }
}
