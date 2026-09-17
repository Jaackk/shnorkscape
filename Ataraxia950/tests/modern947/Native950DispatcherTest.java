package modern947;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.client.Native950IdMap;
import com.rs.game.player.client.Native950PacketDispatcher;
import com.rs.network.io.OutputStream;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.packet.PacketDispatcher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The facade must (a) override every public 910 send method, (b) emit exactly the
 * verified 947 bytes for the REAL tier, (c) only count the NO-OP tier, (d) throw or
 * drop the STRICT tier according to the flag and (e) turn writer range errors into
 * counted drops. Framing uses a zero ISAAC stream, so bytes are opcode, size, body.
 */
public final class Native950DispatcherTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950PacketDispatcher packets;

    @Before
    public void attach() {
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset();
        channel = new EmbeddedChannel(new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread()));
        player = Player.createNative950("dispatcher-test", new WorldTile(3222, 3222, 0), channel);
        assertTrue(player.getPackets() instanceof Native950PacketDispatcher);
        assertSame(player.getPackets(), player.getPackets());
        packets = (Native950PacketDispatcher) player.getPackets();
    }

    @After
    public void detach() {
        Native950PacketDispatcher.setStrict(true);
        Native950IdMap.reset();
        channel.finishAndReleaseAll();
    }

    @Test
    public void everyPublicInstanceMethodOfTheLegacyDispatcherIsOverridden() {
        List<String> missing = new ArrayList<String>();
        int checked = 0;
        for (Method method : PacketDispatcher.class.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || method.isSynthetic() || method.isBridge()) continue;
            if (method.getName().equals("getPlayer")) continue;
            checked++;
            try {
                Native950PacketDispatcher.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException absent) {
                missing.add(method.toString());
            }
        }
        assertTrue("Facade must override: " + missing, missing.isEmpty());
        assertTrue("Expected the full 910 surface, saw " + checked, checked >= 200);
    }

    @Test
    public void legacyEgressIsSealed() throws Exception {
        Method write = Native950PacketDispatcher.class.getDeclaredMethod("write", OutputStream.class);
        write.setAccessible(true);
        try {
            write.invoke(packets, new OutputStream(4));
            fail("Legacy bytes must never leave");
        } catch (InvocationTargetException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        }
        assertNull(flushAndRead());
    }

    @Test
    public void realTierEmitsVerifiedFramedBytes() {
        packets.sendIComponentText(1477, 28, "Hi");
        assertArrayEquals(hex("73000705c5001c486900"), flushAndRead());

        packets.sendHideIComponent(1477, 28, true);
        assertArrayEquals(hex("43001c05c501"), flushAndRead());

        packets.sendConfig(1000, 70000);
        assertArrayEquals(hex("04680311700001"), flushAndRead());

        // Every varbit rides VARBIT_LARGE 71 now; VARBIT_SMALL 50 has no Protocol S
        // verdict (PROTOCOL-S-SUMMARY.md section 3), so the facade never writes it.
        packets.sendConfigByFile(18797, 1);
        assertArrayEquals(hex("52000000016d49"), flushAndRead());

        packets.sendGameMessage("Hello");
        assertArrayEquals(hex("210c00000000000048656c6c6f00"), flushAndRead());

        packets.sendInterface(true, 0x11223344, 0x3456);
        assertArrayEquals(hex("6411223344000000000000000000000000d6347f00000000"), flushAndRead());

        packets.sendWindowsPane(0x3456, 1);
        assertArrayEquals(hex("0156340000000000000000000000000000000000"), flushAndRead());

        packets.closeInterface(1477, 695);
        assertArrayEquals(hex("45c505b702"), flushAndRead());

        packets.sendIComponentSettings(1477, 695, 0, 27, 0x40000);
        assertArrayEquals(hex("1800000004001b0080b702c505"), flushAndRead());

        packets.sendExecuteScript(0x11223344, 0x01020304, "abc", 0x05060708);
        byte[] script = hex("230014" + "6973690005060708616263000102030411223344");
        assertArrayEquals(script, flushAndRead());
        // sendRunScript reverses like 910 did, so the reversed call yields identical bytes.
        packets.sendRunScript(0x11223344, 0x05060708, "abc", 0x01020304);
        assertArrayEquals(script, flushAndRead());

        packets.sendItems(93, new Item[] {new Item(1511, 1), null, new Item(995, 300)});
        assertArrayEquals(hex("090015" + "005d000003" + "0005e801" + "00000000" + "0003e4ff0000012c"), flushAndRead());

        packets.sendUpdateItems(93, new Item[] {new Item(1511, 1), null, new Item(995, 300)}, 2, 7);
        assertArrayEquals(hex("32000c" + "005d00" + "020003e4ff0000012c"), flushAndRead());

        packets.sendMessage(100, "wishes to trade with you.", player);
        byte[] withSender = flushAndRead();
        assertEquals(0x21, withSender[0] & 0xff);
        assertEquals(100, withSender[2] & 0xff);
        assertEquals(3, withSender[7] & 0xff); // flag bits 0 and 1: sender name and second name

        assertEquals(14, packets.counters().totalSent());
        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalStrictHits());
        assertNull(flushAndRead());
    }

    @Test
    public void idMapRebindsBeforeTheWriterRuns() {
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return interfaceId == 1473 ? 1473 : interfaceId; }
            @Override public int componentId(int interfaceId, int componentId) { return interfaceId == 1473 && componentId == 7 ? 5 : componentId; }
            @Override public int varp(int id) { return id + 1; }
            @Override public int varbit(int id) { return id; }
            @Override public int varc(int id) { return id; }
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        });
        packets.sendHideIComponent(1473, 7, false);
        assertArrayEquals(hex("43000505c100"), flushAndRead());
        packets.sendConfig(999, 70000);
        assertArrayEquals(hex("04680311700001"), flushAndRead());
    }

    /**
     * The drop census names the panel, not just the method.
     *
     * <p>Bucketing discarded traffic by method alone answers "a call failed" and never "which
     * panel is broken", so a login-and-walk session produced an impression rather than a backlog.
     * Every interface-shaped dispatcher method already takes (interface, component) first, so the
     * pair is recorded alongside the existing id histogram.
     */
    /** A resolver that binds nothing, so every id-carrying call lands in the counted-drop tier. */
    private static void installRejectingResolver() {
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return -1; }
            @Override public int componentId(int interfaceId, int componentId) { return -1; }
            @Override public int varp(int id) { return -1; }
            @Override public int varbit(int id) { return -1; }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return -1; }
            @Override public int container(int id) { return -1; }
        });
    }

    @Test
    public void discardedTrafficIsBucketedByInterfaceAndComponent() {
        installRejectingResolver();
        // Unbound ids, so the writer rejects and each call lands in the counted-drop tier.
        packets.sendIComponentText(9999, 735, "a");
        packets.sendIComponentText(9999, 735, "b");
        packets.sendIComponentText(9999, 300, "c");
        java.util.Map<String, Long> text = packets.counters().discardedTargets("sendIComponentText");
        assertEquals("the same component twice is one target counted twice",
                Long.valueOf(2), text.get("9999:735"));
        assertEquals(Long.valueOf(1), text.get("9999:300"));
        assertEquals("two distinct components, not one method", 2, text.size());
        String report = packets.counters().discardedTargetReport();
        assertTrue(report, report.contains("sendIComponentText{9999:735=2, 9999:300=1}"));
        // The pre-existing id histogram is untouched: this is additive, and the M2b unmatched-id
        // inventory reads that one.
        assertEquals(Long.valueOf(3),
                packets.counters().discardedIds("sendIComponentText").get(Integer.valueOf(9999)));
    }

    /**
     * The var family also takes two ints, but the second is a VALUE. Pairing them would invent a
     * target that does not exist and would grow a histogram row per value ever written, so the
     * pair form is restricted to an explicit list of component-shaped methods.
     */
    @Test
    public void aVarWriteIsNeverRecordedAsAnInterfaceAndComponent() {
        installRejectingResolver();
        packets.sendVarBit(41524, 300);
        packets.sendVarBit(41524, 301);
        java.util.Map<String, Long> targets = packets.counters().discardedTargets("sendVarBit");
        assertEquals("both writes attribute to the varbit itself", 1, targets.size());
        assertEquals(Long.valueOf(2), targets.get("41524"));
        assertNull("a value must never be read as a component", targets.get("41524:300"));
    }

    @Test
    public void noopTierOnlyCounts() {
        packets.sendCameraLook(1, 2, 3);
        packets.sendCameraLook(1, 2, 3, 4, 5);
        packets.sendServerTickEndPacket();
        packets.sendNoTimeOut();
        packets.sendLocalPlayersUpdate();
        packets.sendLocalNPCsUpdate();
        packets.sendMapRegion();
        packets.sendMusicEffect(90);
        // sendRunEnergy left this tier in M3: UPDATE_RUNENERGY (116) is verified.
        assertFalse(packets.canSendNxtMusicArchive(1));
        assertEquals(2, packets.counters().noops("sendCameraLook"));
        assertEquals(1, packets.counters().noops("sendServerTickEndPacket"));
        assertEquals(1, packets.counters().noops("sendMapRegion"));
        assertEquals(1, packets.counters().noops("canSendNxtMusicArchive"));
        assertEquals(9, packets.counters().totalNoops());
        assertEquals(0, packets.counters().totalSent());
        assertNull(flushAndRead());
    }

    @Test
    public void strictTierThrowsThenDropsWhenDisabled() {
        // Raw legacy zone streams remain sealed even after native ground-item packets are
        // derived; only the session projection may send the new native bodies.
        try {
            packets.createWorldTileStream(new WorldTile(3222, 3222, 0));
            fail("legacy raw zone streams must remain sealed");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("947"));
            assertTrue(expected.getMessage().contains("createWorldTileStream"));
        }
        assertEquals(1, packets.counters().strictHits("createWorldTileStream"));
        assertEquals(0, packets.counters().dropped("createWorldTileStream"));

        Native950PacketDispatcher.setStrict(false);
        OutputStream sink = packets.createWorldTileStream(new WorldTile(3222, 3222, 0));
        assertNotNull(sink);
        assertEquals(2, packets.counters().strictHits("createWorldTileStream"));
        assertEquals(1, packets.counters().dropped("createWorldTileStream"));
        assertNull(flushAndRead());
    }

    /**
     * M3 promotes the stat, vitals, run-energy and client-variable families from the
     * P4 "counted NO-OP" tier to REAL writers: UPDATE_STAT 66, VARBIT_LARGE 71,
     * CLIENT_SETVARC_SMALL 1 / _LARGE 112, CLIENT_SETVARCSTR_SMALL 67 / _LARGE 15,
     * UPDATE_RUNENERGY 116 and UPDATE_RUNWEIGHT 108 are all verified in Protocol S.
     * The bytes below are the ones {@code Native950StatWireTest} and
     * {@code Native950VarWireTest} pin for the writers, produced here through the
     * whole facade and framed by the real transport.
     *
     * <p>This test replaces the two pre-M3 tests that asserted the opposite contract
     * ({@code statAndVarcFamiliesAreCountedNoOpsInBothModes} and
     * {@code largeVarbitValuesAreStrictUntilVarbitLargeIsVerified}); the behaviour
     * change is the milestone itself, and the fail-closed half of both is preserved
     * by {@link #unboundIdsAreStillCountedDropsAfterThePromotion}.
     */
    @Test
    public void m3StatVitalAndVarcFamiliesEmitVerifiedFramedBytes() {
        // The module default (Native950IdMap.IDENTITY) rejects every varc, because
        // no 910 varc id has a verified 947 counterpart and the binding table
        // declares none; see unboundIdsAreStillCountedDropsAfterThePromotion and
        // theDefaultResolverRejectsEveryVarcSoAnUninstalledTableStillFailsClosed for
        // that half of the contract. This test is about the WRITERS, so it installs
        // an explicit permissive resolver and asserts the bytes they produce.
        Native950IdMap.install(permissive());

        // UPDATE_STAT is opcode 92 on 950 and the three fields REVERSED: [-skill][-level][exp BE].
        // 947 sent experience first, then (128 - level), then (skill + 128); both bias transforms
        // became a plain arithmetic negation. Derived from the 950 parser at 0x140141290 (this is
        // a 950 VA - the same address in the 947 image is unrelated code), re-read for this fix:
        //   0x1401412c1 movzx eax,[r10+rax]; 0x1401412c6 neg al  -> ebp, and ebp then scales the
        //               24-byte stat entry (lea rcx,[rbp*2]; add rcx,rbp; lea rbx,[rcx*8] at
        //               0x140141306..0x140141311)                         => byte 0 is the SKILL
        //   0x1401412cf movzx eax,[r9+r10]; 0x1401412d4 neg al  -> r14d, stored at entry+0x14
        //               (0x14014132e)                                      => byte 1 is the LEVEL
        //   0x1401412e4 lea rax,[r8+4]; 0x1401412ec mov edx,[r8+r10]; 0x1401412f2 bswap edx
        //                                                    => bytes 2..5 are EXPERIENCE, BE u32
        // Matches plan appendix A4. Getting this wrong does not error: the client writes the
        // experience's top byte into the stat index, so an unrelated skill's entry is overwritten
        // and the skills tab shows garbage with no diagnostic.
        // A fresh native player is level 1 / 0 xp everywhere except Constitution (level 10, 1155
        // xp), so skill 3 sends (-3)&0xff = 0xfd, (-10)&0xff = 0xf6 and 1,155 = 0x00000483.
        // The live stat table the parser writes carries entry flag 0 (built at
        // 0x1400CDF25), so setter 0x140369C10 neither divides by ten nor uses the
        // 2,000,000,000 clamp; STAT_DEFINITIONS.md 3b.1 corrects UPDATE_STAT.md here.
        packets.sendSkillLevel(0);
        assertArrayEquals(hex("5c00ff00000000"), flushAndRead());
        packets.sendSkillLevel(3);
        assertArrayEquals(hex("5cfdf600000483"), flushAndRead());
        assertEquals(2, packets.counters().sent("sendSkillLevel"));

        // Skills.getLevel is the BOOSTED level and Skills.getLevelForXp the base; the
        // packet must carry the boosted one, because the client derives the base from
        // the experience itself (skills_cell_build 8489 reads op 0x517 / 0x4D8 / 0x14).
        player.getSkills().setLevelWithoutRefresh(3, 15); // set() would refresh and emit a second packet
        packets.sendSkillLevel(3);
        byte[] boosted = flushAndRead();
        // 947 baseline, kept as the re-derivation record: the level used to be the SIXTH framed
        // byte under a 128-minus bias, because experience came first.
        //     assertEquals(15, (128 - (boosted[5] & 0xff)) & 0xff);
        // On 950 the level is framed byte 2 ([opcode][-skill][-level][exp BE]) and the transform
        // is negation. framed[5] is now an experience byte, so the old index silently read 0x04
        // out of 0x00000483 and reported "level 124".
        assertEquals(0x5c, boosted[0] & 0xff);
        assertEquals(0xfd, boosted[1] & 0xff);                 // (-3) & 0xff, the skill index
        assertEquals(0xf1, boosted[2] & 0xff);                 // (-15) & 0xff, the boosted level
        assertEquals(15, (-(boosted[2] & 0xff)) & 0xff);
        assertEquals(10, player.getSkills().getLevelForXp(3)); // the base is unchanged and unsent

        // Every varbit goes out on VARBIT_LARGE, opcode 82 on 950 (947 opcode 71) - the only
        // varbit opcode with a Protocol S verdict; its 32-bit value covers HP (varbit 1668, life
        // points x 100) and prayer (16736, points x 10), which routinely exceed a byte.
        // 950 body (plan PART 1): b0..3 value big-endian, b4 = id, b5 = id>>>8. 947 wrote the id
        // first as ushort128 and the value little-endian, so both halves moved.
        packets.sendConfigByFile(18797, 255);
        assertArrayEquals(hex("52000000ff6d49"), flushAndRead());
        packets.sendConfigByFile(1668, 990);
        assertArrayEquals(hex("52000003de8406"), flushAndRead());
        packets.sendConfigByFile(10900, -1);
        assertArrayEquals(hex("52ffffffff942a"), flushAndRead());
        packets.sendVarBit(1668, 256);
        assertArrayEquals(hex("52000001008406"), flushAndRead());
        assertEquals(0, packets.counters().totalStrictHits());

        // CLIENT_SETVARC_SMALL 126 / _LARGE 119 on 950 (947: 1 and 112), picked by value range
        // exactly as 910 did. SMALL is b0=id, b1=id>>>8, b2=128-value; LARGE is b0=id>>>8,
        // b1=id+128 then the value under the client's mixed order b2=v>>>16, b3=v>>>24, b4=v,
        // b5=v>>>8 - so 6709 = 0x1a35 with 100000 = 0x000186a0 is 1a b5 01 00 a0 86.
        packets.sendGlobalConfig(6709, 0);
        assertArrayEquals(hex("7e351a80"), flushAndRead());
        packets.sendGlobalConfig(6709, 100000);
        assertArrayEquals(hex("771ab50100a086"), flushAndRead());
        packets.sendGlobalConfigSmall(6709, 0);
        assertArrayEquals(hex("7e351a80"), flushAndRead());
        packets.sendGlobalConfigLarge(6709, 100000);
        assertArrayEquals(hex("771ab50100a086"), flushAndRead());
        packets.sendCSVarInteger(6709, 100000);
        assertArrayEquals(hex("771ab50100a086"), flushAndRead());

        // CLIENT_SETVARCSTR_SMALL is opcode 30 on 950 with an unsigned-BYTE length (947: opcode
        // 15, and there it was the unsigned-SHORT form - the two frame widths swapped with the
        // opcodes, plan A3). 950 puts the id FIRST, big-endian with +128 on the low byte, then
        // the NUL-terminated string: 2508 = 0x09cc -> 09 4c. null becomes "" as in 910.
        // The short frame (opcode 81) is the mirror image - string first, then the id plain
        // little-endian with no bias (plan A2) - and is only reachable past 253 payload bytes,
        // which is why nothing here exercises it.
        packets.sendGlobalString(2508, "Hi");
        assertArrayEquals(hex("1e" + "05" + "094c" + "486900"), flushAndRead());
        packets.sendCSVarString(2508, null);
        assertArrayEquals(hex("1e" + "03" + "094c" + "00"), flushAndRead());

        // UPDATE_RUNENERGY 21 and UPDATE_RUNWEIGHT 7 on 950 (947: 116 and 108); both bodies are
        // unchanged - a plain byte and a big-endian signed short.
        packets.sendRunEnergy();
        assertArrayEquals(hex("15" + "64"), flushAndRead()); // a fresh native player starts at 100
        packets.refreshWeight();
        assertArrayEquals(hex("07" + "0000"), flushAndRead());

        assertEquals(0, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalNoops());
        assertEquals(0, packets.counters().totalStrictHits());
        assertNull(flushAndRead());
    }

    /**
     * The whole point of the promotion is that a VERIFIED PACKET IS NOT A LICENCE TO
     * WRITE A 910 ID. Every promoted method still resolves its id through
     * {@link Native950IdMap}, so a varbit, varp or varc the binding table does not
     * declare is a counted drop with nothing on the wire - the same fail-closed
     * outcome the pre-M3 NO-OP and STRICT tiers produced, now under the drop tier.
     */
    @Test
    public void unboundIdsAreStillCountedDropsAfterThePromotion() {
        Native950IdMap.install(new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return interfaceId; }
            @Override public int componentId(int interfaceId, int componentId) { return componentId; }
            @Override public int varp(int id) { return id == 463 ? id : -1; }
            @Override public int varbit(int id) { return id == 1668 ? id : -1; }
            @Override public int varc(int id) { return -1; } // no varc is bound in M3
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        });

        // Bound ids still reach the wire.
        packets.sendConfigByFile(1668, 990);
        assertArrayEquals(hex("52000003de8406"), flushAndRead());
        // 1 fits a signed byte, so the run toggle rides the CONFIRMED VARP_SMALL 10
        // (id little-endian, value negated); only a wider value falls back to 111.
        packets.sendConfig(463, 1);
        assertArrayEquals(hex("4f01014f"), flushAndRead());

        // Unbound ones do not, in every promoted family.
        packets.sendConfigByFile(16736, 990);   // prayer varbit, unbound in this resolver
        packets.sendVarBit(41524, 300);
        packets.sendConfig(462, 1);
        packets.sendGlobalConfig(6709, 2);
        packets.sendGlobalConfigSmall(779, 1);
        packets.sendGlobalConfigLarge(2911, 100000);
        packets.sendCSVarInteger(5886, 3);
        packets.sendGlobalString(2508, "Hi");
        packets.sendCSVarString(2390, "text");
        assertNull("not one unbound id may reach the wire", flushAndRead());
        assertEquals(1, packets.counters().dropped("sendConfigByFile"));
        assertEquals(1, packets.counters().dropped("sendVarBit"));
        assertEquals(1, packets.counters().dropped("sendConfig"));
        assertEquals(1, packets.counters().dropped("sendGlobalConfig"));
        assertEquals(1, packets.counters().dropped("sendGlobalConfigSmall"));
        assertEquals(1, packets.counters().dropped("sendGlobalConfigLarge"));
        assertEquals(1, packets.counters().dropped("sendCSVarInteger"));
        assertEquals(1, packets.counters().dropped("sendGlobalString"));
        assertEquals(1, packets.counters().dropped("sendCSVarString"));
        assertEquals(9, packets.counters().totalDropped());
        assertEquals(2, packets.counters().totalSent());
        assertEquals(0, packets.counters().totalStrictHits());

        // A skill index outside the range enum 680 shares with Ataraxia is a drop too:
        // the client does not bounds-check the index and its live stat table holds
        // exactly one entry per stat definition, so an id past the last one writes off
        // the end of the vector. The shared range is now the cache's full 0..28 - 27
        // Archaeology and 28 Necromancy are modelled stats - so the out-of-range ids
        // are 29 and -1.
        packets.sendSkillLevel(Skills.SKILL_COUNT);
        packets.sendSkillLevel(-1);
        assertEquals(2, packets.counters().dropped("sendSkillLevel"));
        assertEquals(0, packets.counters().sent("sendSkillLevel"));
        assertNull(flushAndRead());
    }

    /**
     * "Unknown = counted AND logged": traffic that never reaches the wire must still
     * name the id it discarded, otherwise every varc and every stat is
     * indistinguishable in the counters and the unmatched-id inventory M2b acceptance
     * (c) checks cannot be produced from a run. M3 moved these families from the
     * counted NO-OP tier to the counted drop tier, so the histogram now covers both.
     */
    @Test
    public void discardedTrafficRecordsTheIdItDiscarded() {
        Native950IdMap.install(unboundVars());
        packets.sendGlobalConfig(6709, 2);
        packets.sendGlobalConfig(6709, 3);
        packets.sendGlobalConfig(779, 1);
        packets.sendCSVarString(2390, "text");
        packets.sendSkillLevel(Skills.SKILL_COUNT + 1); // 30: past the last stat the cache defines

        Map<Integer, Long> varcs = packets.counters().discardedIds("sendGlobalConfig");
        assertEquals(2, varcs.size());
        assertEquals(Long.valueOf(2), varcs.get(6709));
        assertEquals(Long.valueOf(1), varcs.get(779));
        assertEquals(varcs, packets.counters().noopIds("sendGlobalConfig")); // the pre-M3 accessor name
        assertEquals(Long.valueOf(1), packets.counters().discardedIds("sendCSVarString").get(2390));
        assertEquals(Long.valueOf(1),
                packets.counters().discardedIds("sendSkillLevel").get(Skills.SKILL_COUNT + 1));
        // Cosmetic families that carry no id keep the argument-less form.
        packets.sendServerTickEndPacket();
        assertTrue(packets.counters().discardedIds("sendServerTickEndPacket").isEmpty());
        assertTrue(packets.counters().discardedIdsByMethod().containsKey("sendGlobalConfig"));
        assertTrue(packets.counters().noopIdsByMethod().containsKey("sendGlobalConfig"));
        assertEquals(0, packets.counters().totalSent());
        assertNull(flushAndRead());
    }

    /**
     * Identity for every kind INCLUDING varcs. {@link Native950IdMap#IDENTITY}
     * deliberately rejects varcs, so a byte-fixture test that wants to exercise the
     * CLIENT_SETVARC writers has to say so explicitly.
     */
    private static Native950IdMap.Resolver permissive() {
        return new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return interfaceId; }
            @Override public int componentId(int interfaceId, int componentId) { return componentId; }
            @Override public int varp(int id) { return id; }
            @Override public int varbit(int id) { return id; }
            @Override public int varc(int id) { return id; }
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        };
    }

    /**
     * The module default rejects varcs and nothing else, so a build tree, probe or
     * smoke with no installed binding table cannot write a 910 varc id to the 947
     * wire. Before M3 the varc families were unconditional counted NO-OPs, which is
     * what made identity safe for them; the promotion has to replace that guarantee
     * rather than rely on a table happening to be installed.
     */
    @Test
    public void theDefaultResolverRejectsEveryVarcSoAnUninstalledTableStillFailsClosed() {
        assertEquals(-1, Native950IdMap.varc(6709));
        assertEquals(-1, Native950IdMap.varc(0));
        assertEquals(1668, Native950IdMap.varbit(1668));
        assertEquals(463, Native950IdMap.varp(463));

        packets.sendGlobalConfig(6709, 2);
        packets.sendGlobalConfigSmall(779, 1);
        packets.sendGlobalConfigLarge(2911, 100000);
        packets.sendCSVarInteger(5886, 3);
        packets.sendGlobalString(2508, "Hi");
        packets.sendCSVarString(2390, "text");
        assertNull("no varc may reach the wire under the default resolver", flushAndRead());
        assertEquals(6, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalSent());
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /** A resolver that binds no var of any kind, which is the M3 state for varcs. */
    private static Native950IdMap.Resolver unboundVars() {
        return new Native950IdMap.Resolver() {
            @Override public int interfaceId(int interfaceId) { return interfaceId; }
            @Override public int componentId(int interfaceId, int componentId) { return componentId; }
            @Override public int varp(int id) { return -1; }
            @Override public int varbit(int id) { return -1; }
            @Override public int varc(int id) { return -1; }
            @Override public int script(int id) { return id; }
            @Override public int container(int id) { return id; }
        };
    }

    @Test
    public void writerRejectionsBecomeCountedDropsInsteadOfPropagating() {
        packets.sendIComponentText(70000, 1, "too big");
        packets.sendConfig(-1, 5);
        packets.sendExecuteScript(5, Long.valueOf(3));
        packets.sendItems(93, new Item[] {new Item(0xffffff, 1)});
        assertEquals(1, packets.counters().dropped("sendIComponentText"));
        assertEquals(1, packets.counters().dropped("sendConfig"));
        assertEquals(1, packets.counters().dropped("sendExecuteScript"));
        assertEquals(1, packets.counters().dropped("sendItems"));
        assertEquals(4, packets.counters().totalDropped());
        assertEquals(0, packets.counters().totalSent());
        assertTrue(channel.isActive());
        assertNull(flushAndRead());
    }

    @Test
    public void gameBarStagesSendsOnlyTheVerifiedVarbit() {
        packets.sendGameBarStages();
        byte[] frame = flushAndRead();
        // VARBIT_LARGE 71, the only varbit opcode with a Protocol S verdict:
        // varbit 18797 big-endian with the +128 low-byte transform, value 1 little-endian.
        assertArrayEquals(hex("52000000016d49"), frame);
        assertEquals(0x52, frame[0] & 0xff);
        assertEquals(7, frame.length);
        assertNull(flushAndRead());
        assertEquals(1, packets.counters().sent("sendGameBarStages"));
        assertEquals(1, packets.counters().dropped("sendGameBarStages"));
        assertEquals(0, packets.counters().totalStrictHits());
    }

    /**
     * Player.createNative950 always has a channel, but Native950World adds the game
     * transport on the event loop AFTER the player exists: a REAL write in that window
     * must be a counted drop, never a "sent" packet that vanished into the login pipeline.
     */
    @Test
    public void writesBeforeTheTransportIsAttachedAreCountedDropsNotSent() {
        EmbeddedChannel bare = new EmbeddedChannel(); // active, but no Native950GameTransport handler
        try {
            Player early = Player.createNative950("early-write", new WorldTile(3222, 3222, 0), bare);
            Native950PacketDispatcher facade = (Native950PacketDispatcher) early.getPackets();
            assertTrue(bare.isActive());
            facade.sendGameMessage("too early");
            assertEquals(1, facade.counters().dropped("sendGameMessage"));
            assertEquals(0, facade.counters().sent("sendGameMessage"));
            assertEquals(1, facade.counters().totalDropped());
            assertEquals(0, facade.counters().totalSent());
            bare.flush();
            assertTrue("nothing may reach the login pipeline", bare.outboundMessages().isEmpty());

            // Once the transport is in the pipeline the same call is REAL.
            bare.pipeline().addLast(new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread()));
            facade.sendGameMessage("Hello");
            assertEquals(1, facade.counters().sent("sendGameMessage"));
            bare.flush();
            assertEquals(1, bare.outboundMessages().size());

            // A closed channel is "not attached" again: counted drop, no exception.
            bare.close();
            facade.sendGameMessage("after close");
            assertEquals(2, facade.counters().dropped("sendGameMessage"));
            assertEquals(1, facade.counters().sent("sendGameMessage"));
        } finally { bare.finishAndReleaseAll(); }
    }

    /** A write the transport rejects after acceptance is counted as a drop from the future, not lost. */
    @Test
    public void failedWritesAreCountedAsDropsFromTheChannelFuture() {
        EmbeddedChannel broken = new EmbeddedChannel(new Native950GameTransport(() -> 0, () -> {
            throw new IllegalStateException("outgoing cipher exhausted");
        }, Thread.currentThread()));
        try {
            Player player = Player.createNative950("failed-write", new WorldTile(3222, 3222, 0), broken);
            Native950PacketDispatcher facade = (Native950PacketDispatcher) player.getPackets();
            facade.sendGameMessage("Hello");
            assertEquals("the write was handed to the channel", 1, facade.counters().sent("sendGameMessage"));
            assertEquals("and its failure was observed", 1, facade.counters().dropped("sendGameMessage"));
            assertFalse("the transport closes the channel on a framing failure", broken.isActive());
            assertTrue(broken.outboundMessages().isEmpty());
        } finally {
            broken.releaseInbound();
            broken.releaseOutbound(); // finish() would rethrow the recorded framing exception
        }
    }

    @Test
    public void logoutFlushesPendingOutputThenClosesTheChannel() {
        packets.sendGameMessage("Bye");
        packets.sendLogout();
        ByteBuf pending = channel.readOutbound();
        assertNotNull("Pending chat must precede logout",pending);
        pending.release();
        ByteBuf logout = channel.readOutbound();
        assertNotNull("The native client needs an explicit full logout frame",logout);
        try {
            // 950 server opcodes >=128 use the two-byte smart opcode form.
            assertEquals("Smart opcode202 has no size prefix or payload",2,logout.readableBytes());
            assertEquals(128,logout.readUnsignedByte());
            assertEquals(202,logout.readUnsignedByte());
        } finally { logout.release(); }
        assertNull(channel.readOutbound());
        assertFalse(channel.isActive());
        assertFalse(player.isActive());
        assertEquals(1, packets.counters().sent("sendLogout"));
    }

    private byte[] flushAndRead() {
        channel.flush();
        ByteBuf output = channel.readOutbound();
        if (output == null) return null;
        try {
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        } finally {
            output.release();
        }
    }

    private static byte[] hex(String text) {
        byte[] out = new byte[text.length() / 2];
        for (int i = 0; i < out.length; i++) out[i] = (byte) Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
        return out;
    }
}
