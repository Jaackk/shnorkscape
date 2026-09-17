package com.rs.cache.loaders;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * Hand-built definition blobs (no cache, no Cache.STORE) proving the strict 947
 * decoders consume exactly their bytes, fail closed on unknown opcodes, trailing
 * bytes and truncation, and that the tolerant 910 item path is unchanged.
 */
public class DefinitionDecoderTest {

    /** A 947-style item: name, stackable, 181 int64, 69 int32, 3 string, 178 flag, option, equip slot. */
    private static byte[] item947() {
        Blob b = new Blob();
        b.u8(2).str("Coins");
        b.u8(11);
        b.u8(181).u64(187500L);
        b.u8(69).u32(0x01020304);
        b.u8(3).str("Examine me");
        b.u8(178);
        b.u8(35).str("Add to pouch");
        b.u8(13).u8(3);
        b.u8(0);
        return b.bytes();
    }

    @Test public void strictItemDecodesThe947OpcodesAndConsumesExactlyItsBytes() {
        StringBuilder trace = new StringBuilder();
        ItemDefinitions def = ItemDefinitions.decodeStrict947(995, item947(), trace);
        assertEquals("Coins", def.name);
        // 950 opcode 178 writes 0 to the SAME stack-mode byte opcode 11 writes 1 to
        // (950 0x14036feb1 vs 950 0x14036edab), so the later 178 in this blob wins. No item in
        // the shipped 950 cache carries both, so this only shows up on hand-built blobs.
        assertEquals(0, def.stackable);
        assertEquals(187500L, def.value64);
        assertEquals(187500, def.value);
        assertEquals(0x01020304, def.unknownInt69);
        assertEquals("Examine me", def.description);
        assertTrue(def.flag178);
        assertEquals("Add to pouch", def.inventoryOptions[0]);
        assertEquals("drop", def.inventoryOptions[4]);
        assertEquals(3, def.equipSlot);
        assertNull(def.decodeFailure);
        assertTrue(def.loaded);
        // Offsets prove each payload width: 2@0 (Coins\0 = 6) 11@7 181@8 (+8) 69@17 (+4) 3@22 (+11) 178@34 35@35 (+13) 13@49 (+1)
        assertEquals("2@0 11@7 181@8 69@17 3@22 178@34 35@35 13@49", trace.toString().trim());
    }

    @Test public void strictItemClampsAnInt64ValueAboveTheIntRange() {
        Blob b = new Blob();
        b.u8(181).u64(10000000000L).u8(0);
        ItemDefinitions def = ItemDefinitions.decodeStrict947(1, b.bytes(), null);
        assertEquals(10000000000L, def.value64);
        assertEquals(Integer.MAX_VALUE, def.value);
    }

    @Test public void strictItemRejectsAnUnknownOpcode() {
        // 250 is above the 950 ObjType decoder's bounds check (950 0x14036eb4d accepts 1..249),
        // so the client would ignore it; the strict path refuses it instead, because silently
        // ignoring an opcode is indistinguishable from decoding a misaligned stream.
        // (This test used to use 167, which the 950 client DOES handle - as a payload-less flag.)
        Blob b = new Blob();
        b.u8(2).str("Odd").u8(250).u8(0);
        try {
            ItemDefinitions.decodeStrict947(29492, b.bytes(), null);
            fail("Unknown opcode 250 was accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("opcode 250"));
            assertTrue(expected.getMessage(), expected.getMessage().contains("29492"));
        }
    }

    /**
     * Pins the widths of the 950-only ObjType opcodes that the 947 table had no case for. The
     * trace offsets are the actual assertion: they prove each payload consumed exactly the number
     * of bytes read out of the 950 decoder at 950 0x14036eb30.
     */
    @Test public void strictItemDecodesThe950OnlyOpcodeWidths() {
        Blob b = new Blob();
        b.u8(9).u8(2).u16(4000).u16(4001);   // model list: count then 2 bigSmarts (2 bytes each)
        b.u8(182).u8(0).u8(1).u8(2);         // 24-bit payload
        b.u8(190).u8(0).u8(0).u8(5).u16(10); // 24-bit id + 16-bit amount
        b.u8(201).u8(0).u8(0).u8(7);         // 24-bit certId
        b.u8(202).u8(0).u8(0).u8(8);         // 24-bit certTemplateId
        b.u8(167);                           // no payload
        b.u8(0);
        StringBuilder trace = new StringBuilder();
        ItemDefinitions def = ItemDefinitions.decodeStrict947(3, b.bytes(), trace);
        assertEquals("9@0 182@6 190@10 201@16 202@20 167@24", trace.toString().trim());
        assertEquals(2, def.inventoryModels.length);
        assertEquals(4000, def.inventoryModels[0]);
        assertEquals(4001, def.inventoryModels[1]);
        assertEquals(4000, def.baseModel); // opcode 1 never occurs in the 950 cache; 9 feeds it
        assertEquals(0x000102, def.unknownInt182);
        assertEquals(5, def.stackIds[0]);
        assertEquals(10, def.stackAmounts[0]);
        assertEquals(7, def.certId);
        assertEquals(8, def.certTemplateId);
        assertTrue(def.flag167);
        assertNull(def.decodeFailure);
    }

    @Test public void strictItemRejectsTrailingBytes() {
        byte[] valid = item947();
        byte[] trailing = new byte[valid.length + 1];
        System.arraycopy(valid, 0, trailing, 0, valid.length);
        trailing[valid.length] = 5;
        try {
            ItemDefinitions.decodeStrict947(995, trailing, null);
            fail("Trailing byte was accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Trailing"));
        }
    }

    @Test public void strictItemRejectsATruncatedPayload() {
        Blob b = new Blob();
        b.u8(2).str("Cut").u8(181).u8(0).u8(0); // int64 needs 8 bytes, only 2 present
        try {
            ItemDefinitions.decodeStrict947(2, b.bytes(), null);
            fail("Truncated int64 was accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Truncated"));
        }
    }

    @Test public void legacyItemPathStillToleratesTheSameBlobAsBefore() {
        // 910 behaviour: unknown opcode 181 consumes nothing, so the first 0x00 of its
        // payload terminates the loop. Nothing throws and nothing after it is read.
        ItemDefinitions def = ItemDefinitions.decodeLegacy(995, item947());
        assertEquals("Coins", def.name);
        assertEquals(1, def.stackable);
        assertEquals(1, def.value);
        assertEquals(-1L, def.value64);
        assertEquals(-1, def.equipSlot);
        assertNull(def.description);
        assertFalse(def.flag178);
        assertNull(def.decodeFailure);
        assertTrue(def.loaded);
        assertNull(ItemDefinitions.getDecodeFailure(995));
    }

    @Test public void strictNpcDecodesKnownOpcodesAndRejectsTheUnconfirmedOnes() {
        Blob b = new Blob();
        b.u8(2).str("Banker").u8(30).str("Talk-to").u8(95).u16(0).u8(12).u8(1).u8(0);
        StringBuilder trace = new StringBuilder();
        NPCDefinitions def = NPCDefinitions.decodeStrict947(494, b.bytes(), trace);
        assertEquals("Banker", def.name);
        assertEquals("Talk-to", def.menuOptions[0]);
        assertEquals(0, def.combatLevel);
        assertEquals(1, def.size);
        assertNull(def.decodeFailure);
        assertEquals("2@0 30@8 95@17 12@20", trace.toString().trim());
        for (int opcode : new int[] { 184, 185, 186, 253 }) {
            Blob bad = new Blob();
            bad.u8(2).str("X").u8(opcode).u8(0);
            try {
                NPCDefinitions.decodeStrict947(7, bad.bytes(), null);
                fail("NPC opcode " + opcode + " was guessed");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("opcode " + opcode));
            }
        }
        Blob trailing = new Blob();
        trailing.u8(2).str("X").u8(0).u8(9);
        try {
            NPCDefinitions.decodeStrict947(8, trailing.bytes(), null);
            fail("Trailing NPC byte was accepted");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Trailing"));
        }
    }

    @Test public void strictAnimationDecodesKnownOpcodesAndRejectsTheUnconfirmedOnes() {
        Blob b = new Blob();
        b.u8(2).u16(99).u8(5).u8(7).u8(14);
        b.u8(6).u16(0xffff).u8(7).u16(0x092b); // 947: hand items are u16, 0xFFFF = none
        b.u8(0);
        StringBuilder trace = new StringBuilder();
        AnimationDefinitions def = AnimationDefinitions.decodeStrict947(3, b.bytes(), trace);
        assertEquals(99, def.anInt2136);
        assertEquals(7, def.anInt2142);
        assertTrue(def.aBoolean2158);
        assertEquals(-1, def.rightHandItem);
        assertEquals(0x092b, def.leftHandItem);
        assertNull(def.decodeFailure);
        assertEquals("2@0 5@3 14@5 6@6 7@9", trace.toString().trim());
        // Opcodes the 950 SeqType ladder has NO case for, so the strict decoder must refuse rather
        // than guess a width. The set changed with the port: 25, 27, 119 and 120 used to be here
        // and are now implemented (they cover 11941 of the 38084 definitions in the 950 cache, so
        // refusing them was refusing a third of the animations). 255 stays: it is not a SeqType
        // opcode on 950 at all, and its appearances in earlier scans were an artefact of reading
        // opcodes 6/7 as bigSmarts, which turns a 0xFFFF "no item" into a bogus opcode byte.
        for (int opcode : new int[] { 4, 17, 21, 28, 255 }) {
            Blob bad = new Blob();
            bad.u8(5).u8(1).u8(opcode).u8(0);
            try {
                AnimationDefinitions.decodeStrict947(4, bad.bytes(), null);
                fail("Animation opcode " + opcode + " was guessed");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("opcode " + opcode));
            }
        }
    }

    @Test public void strictRenderAnimDecodesThe947OpcodesAndRejectsUnknownOnes() {
        Blob b = new Blob();
        b.u8(1).u16(1).u16(2);          // stand / walk as two-byte big smarts
        b.u8(52).u8(2);                 // 947 loop table: 2 x (bigSmart, 2-byte duration field)
        b.u16(0x26e0).u8(0x50).u8(0);
        b.u16(0x26e1).u8(0x14).u8(0);
        b.u8(6).u16(5);                 // run animation (910 table)
        b.u8(0);
        StringBuilder trace = new StringBuilder();
        RenderAnimDefinitions def = RenderAnimDefinitions.decodeStrict947(2699, b.bytes(), trace);
        assertEquals(1, def.standAnimation);
        assertEquals(2, def.walkAnimation);
        assertArrayEquals(new int[] { 0x26e0, 0x26e1 }, def.loopAnimations);
        assertArrayEquals(new int[] { 0x50, 0x14 }, def.loopAnimDurations);
        assertArrayEquals(new int[] { 0, 0 }, def.loopAnimDurationsLow);
        assertEquals(5, def.runAnimation);
        assertEquals(2699, def.id);
        // The 0x14 duration byte must not be taken for an "opcode 20": the loop consumed both bytes.
        assertEquals("1@0 52@5 6@15", trace.toString().trim());
        // The assessment's candidate opcodes were artefacts of the opcode-52 desync; they stay unknown.
        for (int opcode : new int[] { 10, 20, 128, 131, 133, 135, 136, 137, 200 }) {
            Blob bad = new Blob();
            bad.u8(1).u16(1).u16(2).u8(opcode).u8(0);
            try {
                RenderAnimDefinitions.decodeStrict947(1, bad.bytes(), null);
                fail("BAS opcode " + opcode + " was accepted");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("opcode " + opcode));
            }
        }
    }

    /** Big-endian byte builder mirroring the cache encoding. */
    private static final class Blob {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Blob u8(int v) { out.write(v & 0xff); return this; }
        Blob u16(int v) { return u8(v >>> 8).u8(v); }
        Blob u32(int v) { return u16(v >>> 16).u16(v); }
        Blob u64(long v) { return u32((int) (v >>> 32)).u32((int) v); }
        Blob str(String s) {
            for (int i = 0; i < s.length(); i++) u8(s.charAt(i));
            return u8(0);
        }
        byte[] bytes() { return out.toByteArray(); }
    }
}
