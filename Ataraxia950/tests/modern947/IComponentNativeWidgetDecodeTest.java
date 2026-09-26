package modern947;

import com.rs.cache.loaders.IComponentDefinitions;
import com.rs.network.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Cacheless decode of synthetic version-9/11 records for the native widget types 11/12/13/15/16
 * (TOP50 #2). Before the layouts were added every one of these records stopped at
 * "Unsupported component type", so the option and hook assertions here fail on the old decoder.
 */
public final class IComponentNativeWidgetDecodeTest {

    private static byte[] bytes(int... values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) out[i] = (byte) values[i];
        return out;
    }

    private static void string(ByteArrayOutputStream o, String s) {
        for (int i = 0; i < s.length(); i++) o.write(s.charAt(i));
        o.write(0);
    }

    private static void int32(ByteArrayOutputStream o, int v) {
        o.write(v >>> 24); o.write(v >>> 16); o.write(v >>> 8); o.write(v);
    }

    /** 19-byte common header for an unnamed component (format 9/11), then the caller's type block. */
    private static ByteArrayOutputStream header(int format, int type) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(format); o.write(type);
        o.write(0); o.write(0);          // contentType
        o.write(0); o.write(10);         // x
        o.write(0); o.write(6);          // y
        o.write(0); o.write(20);         // w
        o.write(0); o.write(25);         // h
        o.write(1); o.write(0); o.write(0); o.write(0); // aspect modes
        o.write(0); o.write(14);         // parent
        o.write(2);                      // settings: noClickThrough
        return o;
    }

    /** Five-byte version-9/11 extension, then the common tail with one option, an onLoad hook and a varp list. */
    private static void tail(ByteArrayOutputStream o, String option, int loadScript, String loadText, int[] varps) {
        tail(o, option, loadScript, loadText, varps, 0);
    }

    /** As above, with an explicit high byte for the 32-bit option mask that formats 6/9/11 carry. */
    private static void tail(ByteArrayOutputStream o, String option, int loadScript, String loadText, int[] varps, int maskHigh) {
        o.write(bytes(0xff, 0xff, 0xff, 0xff, 0), 0, 5);      // modernFormatExtension
        o.write(maskHigh); o.write(0); o.write(0); o.write(option == null ? 0 : 2); // 32-bit option mask
        o.write(0);                                            // no option keys
        o.write(0);                                            // opBase ""
        if (option == null) o.write(0); else { o.write(1); string(o, option); }
        o.write(0);                                            // pauseText ""
        o.write(0); o.write(0); o.write(0);                    // drag dead zone/time/render
        o.write(0);                                            // targetVerb ""
        o.write(0xff); o.write(0xff);                          // mouseOverCursor -1
        o.write(0); o.write(0);                                // no int/string params
        o.write(loadText == null ? 2 : 3); o.write(0); int32(o, loadScript); o.write(0); int32(o, -2147483645);
        if (loadText != null) { o.write(1); string(o, loadText); }
        for (int hook = 1; hook < 25; hook++) o.write(0);      // remaining typed hooks (21 named + 4 extra)
        o.write(varps.length); for (int v : varps) int32(o, v);
        o.write(0); o.write(0); o.write(0); o.write(0);        // inv/stat/varc/varcstr lists
    }

    private static IComponentDefinitions decode(byte[] raw) {
        IComponentDefinitions d = new IComponentDefinitions();
        d.ihash = (190 << 16) | 17;
        d.decode(new InputStream(raw, true));
        return d;
    }

    private static byte[] type12(String label) {
        ByteArrayOutputStream o = header(11, 12);
        for (int i = 0; i < 33; i++) o.write(i == 0 ? 1 : 0);
        o.write(0x7f); o.write(0xff);            // font bigsmart -1
        o.write(1);                              // flag
        string(o, label);
        for (int i = 0; i < 10; i++) o.write(0x7d - i);
        tail(o, "Select", 10642, null, new int[0]);
        return o.toByteArray();
    }

    @Test public void type12LabelOptionsAndHooksDecodeExactly() {
        IComponentDefinitions d = decode(type12("Show Locked"));
        assertFalse(d.decodeFailureReason, d.decodeIncomplete);
        assertEquals(12, d.type);
        assertEquals("Show Locked", d.nativeLabelText);
        assertEquals(-1, d.nativeLabelFont);
        assertEquals(1, d.nativeLabelFlag);
        assertEquals(33, d.nativeWidgetPrefix.length);
        assertEquals(10, d.nativeWidgetSuffix.length);
        assertEquals(0x7d, d.nativeWidgetSuffix[0] & 0xff);
        assertArrayEquals(new String[]{"Select"}, d.ops);
        assertArrayEquals(new Object[]{10642, -2147483645}, d.onLoadHook);
        assertArrayEquals(bytes(0xff, 0xff, 0xff, 0xff, 0), d.modernFormatExtension);
        assertTrue(d.noClickThrough);
        assertEquals((190 << 16) | 14, d.parentLayer);
    }

    @Test public void type12EmptyLabelMovesTheBoundaryByExactlyTheLabelLength() {
        assertEquals(type12("Show Locked").length - "Show Locked".length(), type12("").length);
        IComponentDefinitions d = decode(type12(""));
        assertFalse(d.decodeFailureReason, d.decodeIncomplete);
        assertEquals("", d.nativeLabelText);
        assertArrayEquals(new String[]{"Select"}, d.ops);
    }

    @Test public void type16CountedArraysAndFont() {
        ByteArrayOutputStream o = header(11, 16);
        o.write(bytes(1, 0, 1, 0x0a, 0x64), 0, 5);
        int32(o, 2); o.write(7); o.write(9);        // n = 2 bytes
        o.write(0); o.write(1); int32(o, 0x12345678); // m = 1 int
        for (int i = 0; i < 87; i++) o.write(0);
        o.write(0); o.write(0xcf);                   // font 207
        o.write(1);
        string(o, "");
        for (int i = 0; i < 86; i++) o.write(0);
        tail(o, null, 3391, null, new int[]{461, 11456});
        IComponentDefinitions d = decode(o.toByteArray());
        assertFalse(d.decodeFailureReason, d.decodeIncomplete);
        assertEquals(16, d.type);
        assertArrayEquals(bytes(7, 9), d.nativeType16Bytes);
        assertArrayEquals(new int[]{0x12345678}, d.nativeType16Ints);
        assertEquals(87, d.nativeType16Middle.length);
        assertEquals(207, d.nativeLabelFont);
        assertEquals("", d.nativeLabelText);
        assertEquals(86, d.nativeWidgetSuffix.length);
        assertNull(d.ops);
        assertArrayEquals(new Object[]{3391, -2147483645}, d.onLoadHook);
        assertArrayEquals(new int[]{461, 11456}, d.varpTransmitList);
    }

    @Test public void type13PrefixFontLabelSuffix() {
        ByteArrayOutputStream o = header(11, 13);
        for (int i = 0; i < 39; i++) o.write(i);
        o.write(0); o.write(0xcf); o.write(1); string(o, "");
        for (int i = 0; i < 86; i++) o.write(0);
        tail(o, "Enter Text", 10642, "event_text", new int[0]);
        IComponentDefinitions d = decode(o.toByteArray());
        assertFalse(d.decodeFailureReason, d.decodeIncomplete);
        assertEquals(39, d.nativeWidgetPrefix.length);
        assertEquals(38, d.nativeWidgetPrefix[38]);
        assertEquals(207, d.nativeLabelFont);
        assertArrayEquals(new String[]{"Enter Text"}, d.ops);
        assertArrayEquals(new Object[]{10642, -2147483645, "event_text"}, d.onLoadHook);
    }

    @Test public void opaqueTypes11And15InBothFormats() {
        for (int format : new int[]{9, 11}) {
            ByteArrayOutputStream o = header(format, 11);
            o.write(bytes(0, 0, 0, 0, 1, 5), 0, 6);
            tail(o, null, 16791, null, new int[0]);
            IComponentDefinitions d = decode(o.toByteArray());
            assertFalse(d.decodeFailureReason, d.decodeIncomplete);
            assertArrayEquals(bytes(0, 0, 0, 0, 1, 5), d.nativeWidgetBlock);
            assertArrayEquals(new Object[]{16791, -2147483645}, d.onLoadHook);
            o = header(format, 15);
            o.write(bytes(0, 0, 0x27, 0x10, 8, 0, 0xb0, 0, 0xb0, 1), 0, 10);
            tail(o, null, 18270, null, new int[]{461, 11456, 11471});
            d = decode(o.toByteArray());
            assertFalse(d.decodeFailureReason, d.decodeIncomplete);
            assertEquals(10, d.nativeWidgetBlock.length);
            assertEquals(0xb0, d.nativeWidgetBlock[8] & 0xff);
            assertArrayEquals(new int[]{461, 11456, 11471}, d.varpTransmitList);
        }
    }

    /**
     * The 950 client reads the option mask as a 32-bit big-endian integer for format >= 6
     * (rs2client.exe 0x14034057f) and as a 24-bit one below that (0x1403405be). Ten real
     * components of this cache (types 13 and 16, listed in
     * reports/r3-decoder3-optionmask-950.txt) set bit 24 and nothing else, so the previous
     * 24-bit reading returned 0 for them and put the set byte at the end of the extension.
     * This fails on the old decoder: it saw a six-byte extension and an option mask of 2.
     */
    @Test public void optionMaskKeepsItsHighByteOnVersionedFormats() {
        ByteArrayOutputStream o = header(11, 12);
        for (int i = 0; i < 33; i++) o.write(i == 0 ? 1 : 0);
        o.write(0x7f); o.write(0xff); o.write(1); string(o, "Show Locked");
        for (int i = 0; i < 10; i++) o.write(0x7d - i);
        tail(o, "Select", 10642, null, new int[0], 1);
        IComponentDefinitions d = decode(o.toByteArray());
        assertFalse(d.decodeFailureReason, d.decodeIncomplete);
        assertEquals(5, d.modernFormatExtension.length);
        assertEquals(0x01000002, d.activeProperties.settings);
        assertArrayEquals(new String[]{"Select"}, d.ops);
    }

    @Test public void unprovenFormatTruncationTrailingAndBadCountsStayExplicitlyPartial() {
        byte[] raw = type12("Show Locked");
        byte[] format6 = raw.clone(); format6[0] = 6;
        IComponentDefinitions d = decode(format6);
        assertTrue(d.decodeIncomplete);
        assertTrue(d.decodeFailureReason, d.decodeFailureReason.contains("Unsupported native widget format 6"));
        for (int cut : new int[]{19, 40, 52, 60, raw.length - 1})
            assertTrue("cut " + cut, decode(Arrays.copyOf(raw, cut)).decodeIncomplete);
        assertTrue(decode(Arrays.copyOf(raw, raw.length + 1)).decodeIncomplete);
        ByteArrayOutputStream o = header(11, 16);
        o.write(bytes(1, 0, 1, 0x0a, 0x64), 0, 5);
        int32(o, 0x7fffffff);
        d = decode(o.toByteArray());
        assertTrue(d.decodeIncomplete);
        assertTrue(d.decodeFailureReason, d.decodeFailureReason.contains("type-16 byte count"));
        assertFalse(decode(raw).decodeIncomplete);
    }
}
