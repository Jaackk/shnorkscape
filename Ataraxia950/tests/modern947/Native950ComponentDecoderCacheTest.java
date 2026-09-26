package modern947;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.IComponentDefinitions;
import com.rs.network.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Stage B Phase 2: every index-3 record of the local 950 cache must decode completely through
 * the production accessor. Ported from Artaven's Native950ComponentDecoderAcceptance (fixture
 * values unchanged) plus Shnorkscape corroborations. The cache is not tracked in git, so this
 * test is skipped when ../cache is absent; the cacheless contract lives in
 * IComponentNativeWidgetDecodeTest.
 */
public final class Native950ComponentDecoderCacheTest {
    private static final Path CACHE = Paths.get("..", "cache");
    private static Store previous;
    private static boolean installed;

    /** SHA-256 of the raw fixture records, so a different cache fails loudly instead of drifting. */
    private static final String[][] PINS = {
            {"190.17", "a01095114074aa144380ba207d384dee4a2526fb773cd84c3522ab1fcd7c11b8"},
            {"1223.17", "4a3cc5357d0f89bc364f41b28f186e63b83f1020b34e4087c2d43f1567c0487b"},
            {"105.225", "373e1ee837adf950bd88ef150ce83a7ec1e2069d70e46217adba3a6e9a23283b"},
            {"1498.6", "fde9164f3a3adae4f639bcfab2d2f6a6a01e59c67de94f11632d102b15ff5050"},
            {"1438.18", "acbc6a9ec8fa61634beb0c9d446282ff8113e25fb920b44bc2860be3112ce202"},
            {"590.7", "e19363676531da6ec8aa75598e7df4546ab7366e747094ab8abafecac9ffa572"},
            {"1475.41", "097611070003bdd8790e60d6c430afa7ce2bbc128681fe60c30a44344eb40f98"},
            {"590.12", "c306a5e1a9ea057425745774f8306089ddf98242b5dc3a57233cbebc66696b14"},
            {"1438.13", "d91aee7c1a05c7cb3ff82aef6f4bdd00885e9d73f073cd561d7e50892ac134d6"},
            {"828.6", "77a05236c0645318e55d6a5a6e270de8576100d74ad9990a0ebb8bec9e9b7739"},
            {"1026.3", "96b586c1f69c21bb13ef2efd55ad1b4e5e4edf871934b1c5c441be81b5cdd8ad"},
            {"1055.11", "c49f1dfcef5fa3ea25a12a90dfd15e6ff85e5e8d509111879e329e00d24426d9"},
            {"280.3", "453737c741a2831bc96a62625ab45f933ea86d5bd33b258eb68da0c49679cb3a"},
            {"300.14", "be7257591f0717dcce98ad76cc47d0d6cf1ae34d0643494d4ac65db2eedf530d"},
    };

    @BeforeClass public static void openCache() throws Exception {
        Assume.assumeTrue("local 950 cache not present at " + CACHE.toAbsolutePath(), Files.isDirectory(CACHE.resolve("3")));
        previous = Cache.STORE;
        Cache.STORE = Store.openFlatReadOnly(CACHE);
        installed = true;
    }

    @AfterClass public static void restoreCache() {
        if (installed) Cache.STORE = previous;
    }

    @Test public void fixtureRecordsArePinned() throws Exception {
        for (String[] pin : PINS) {
            String[] f = pin[0].split("\\.");
            byte[] raw = Cache.STORE.getIndexes()[3].getFile(Integer.parseInt(f[0]), Integer.parseInt(f[1]));
            assertNotNull("pinned record present " + pin[0], raw);
            StringBuilder hash = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(raw)) hash.append(String.format("%02x", b & 255));
            assertEquals("pinned record unchanged " + pin[0], pin[1], hash.toString());
        }
    }

    @Test public void everyComponentDecodesCompletelyWithValidHooks() throws Exception {
        Index scripts = Cache.STORE.getIndexes()[12];
        List<Field> hookFields = new ArrayList<Field>();
        for (Field f : IComponentDefinitions.class.getDeclaredFields())
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == Object[].class && f.getName().endsWith("Hook")) hookFields.add(f);
        long total = 0, hooks = 0, extraHooks = 0;
        int wideOptionMasks = 0;
        List<String> partial = new ArrayList<String>(), badHooks = new ArrayList<String>();
        Map<Integer, Integer> nativeTypes = new TreeMap<Integer, Integer>();
        int size = com.rs.utils.Utils.getInterfaceDefinitionsSize();
        for (int id = 0; id < size; id++) {
            IComponentDefinitions[] defs = IComponentDefinitions.getInterface(id);
            if (defs == null) continue;
            for (IComponentDefinitions d : defs) {
                if (d == null) continue;
                total++;
                String key = id + ":" + (d.ihash & 0xffff);
                if (d.decodeIncomplete) partial.add(key + " " + d.decodeFailureReason);
                if (d.type >= 11) nativeTypes.merge(d.type, 1, Integer::sum);
                if (d.activeProperties != null && (d.activeProperties.settings & 0xff000000) != 0) wideOptionMasks++;
                List<Object[]> all = new ArrayList<Object[]>();
                for (Field f : hookFields) all.add((Object[]) f.get(d));
                if (d.additionalHooks != null) for (Object[] h : d.additionalHooks) { if (h != null) extraHooks++; all.add(h); }
                for (Object[] h : all) {
                    if (h == null) continue;
                    hooks++;
                    if (!(h[0] instanceof Integer) || !scripts.archiveExists((Integer) h[0])) badHooks.add(key + " " + Arrays.toString(h));
                }
            }
        }
        assertEquals("partial decodes " + partial.subList(0, Math.min(5, partial.size())), 0, partial.size());
        // Static counters are shared with the malformed-copy test, so completeness is asserted
        // per record: every non-empty record is published (no escaped failure) and none is partial.
        assertEquals(104285, total);
        assertEquals("hooks naming a missing script " + badHooks.subList(0, Math.min(5, badHooks.size())), 0, badHooks.size());
        assertEquals(46272, hooks);
        assertEquals(169, extraHooks);
        assertEquals(10, wideOptionMasks);
        Map<Integer, Integer> expected = new TreeMap<Integer, Integer>();
        expected.put(11, 54); expected.put(12, 16); expected.put(13, 8); expected.put(15, 57); expected.put(16, 8);
        assertEquals(expected, nativeTypes);
    }

    @Test public void nativeWidgetFixtures() {
        IComponentDefinitions quest = fixture(190, 17);
        assertTrue(quest.type == 12 && "Show Locked".equals(quest.nativeLabelText) && quest.nativeLabelFont == -1 && quest.nativeLabelFlag == 1);
        assertArrayEquals(new String[]{"Select"}, quest.ops);
        assertEquals(46, quest.opCursors[0]);
        assertArrayEquals(new Object[]{10642, -2147483645}, quest.onLoadHook);
        assertEquals(8799, quest.onMouseRepeatHook[0]);
        assertEquals("Filter quests you cannot start due to unmet requirements.", quest.onMouseRepeatHook[1]);
        assertTrue(quest.nativeWidgetPrefix.length == 33 && quest.nativeWidgetSuffix.length == 10);
        assertArrayEquals(new byte[]{0, 0, 0, 0x7d, 0}, quest.modernFormatExtension);

        IComponentDefinitions input = fixture(105, 225);
        assertTrue(input.type == 13 && input.nativeLabelFont == 207 && "".equals(input.nativeLabelText)
                && input.nativeWidgetPrefix.length == 39 && input.nativeWidgetSuffix.length == 86);
        assertArrayEquals(new String[]{"Enter Text"}, input.ops);
        assertArrayEquals(new Object[]{15029}, input.onOptHook);
        assertArrayEquals(new Object[]{20894, "event_text", -2147483647}, input.additionalHooks[1]);

        IComponentDefinitions grid = fixture(590, 12);
        assertEquals("00000000050028002801", hex(grid.nativeWidgetBlock));
        assertArrayEquals(new Object[]{18270, -2147483645}, grid.onLoadHook);
        assertArrayEquals(new Object[]{18271, -2147483645}, grid.onVarpTransmitHook);
        assertArrayEquals(new int[]{461, 11456, 11471}, grid.varpTransmitList);
        assertEquals("000027100800b000b001", hex(fixture(828, 6).nativeWidgetBlock));

        IComponentDefinitions bar = fixture(1438, 18);
        assertTrue(bar.type == 16 && bar.nativeType16Bytes.length == 10 && bar.nativeType16Ints.length == 10
                && bar.nativeType16Middle.length == 87 && bar.nativeLabelFont == -1);
        assertArrayEquals(new Object[]{3391, -2147483645}, bar.onLoadHook);
        assertEquals(3553, bar.additionalHooks[2][0]);
        assertEquals(0x01000000, bar.activeProperties.settings);
        IComponentDefinitions plain = fixture(590, 7);
        assertTrue(plain.nativeType16Bytes.length == 0 && plain.nativeType16Ints.length == 0);
        assertEquals(0x01000000, plain.activeProperties.settings);
        assertTrue(fixture(1475, 41).nativeLabelFont == 207 && fixture(1475, 41).nativeType16Ints.length == 10);

        IComponentDefinitions older = fixture(1026, 3);
        assertTrue(older.type == 11 && older.ops == null);
        assertEquals("000000000105", hex(older.nativeWidgetBlock));
        assertEquals(16791, older.onLoadHook[0]);
        assertEquals("000000000002", hex(fixture(280, 3).nativeWidgetBlock));
        assertEquals("00000000000001000300", hex(fixture(300, 14).nativeWidgetBlock));
        assertEquals(16886, fixture(1055, 11).onLoadHook[0]);
    }

    @Test public void ordinaryFixturesAndShnorkscapeCorroborations() {
        IComponentDefinitions prayer = fixture(1458, 40);
        assertArrayEquals(new Object[]{1236, (1458 << 16) | 40}, prayer.onVarpTransmitHook);
        assertArrayEquals(new int[]{12219, 12219}, prayer.varpTransmitList);
        assertEquals("Confirm", fixture(729, 8).text);
        assertEquals(38, fixture(729, 8).nativeTextButtonPrefix.length);
        assertArrayEquals(new Object[]{10896, -2147483645, -1, 2824}, fixture(15, 9).onLoadHook);
        assertEquals(6897, fixture(641, 135).graphicId);
        assertEquals((641 << 16) | 131, fixture(641, 135).parentLayer);
        assertArrayEquals(new Object[]{8181, -2147483645, -2147483644}, fixture(1433, 72).onOptHook);
        assertArrayEquals(new int[]{858}, fixture(37, 20).invTransmitList);

        // Pre-Phase-2 decoder threw on or misread these; values cross-checked against the
        // native UI research (CS4213 on the NPC preview layer) and Artaven's independent decode.
        IComponentDefinitions preview = fixture(1311, 362);
        assertArrayEquals(new Object[]{4213, -2147483645}, preview.onLoadHook);
        assertEquals(0xC0000, preview.activeProperties.settings);
        assertEquals(1165, fixture(753, 40).onMouseRepeatHook[0]);
        // Developer Console shell components are format 4 and were already exact.
        assertEquals(4, raw(1448, 8)[0]);
        assertEquals(4, raw(1448, 24)[0]);
        assertNull(fixture(1448, 8).modernFormatExtension);
    }

    @Test public void malformedCopiesStayExplicitlyPartial() {
        byte[] raw = raw(190, 17);
        for (int cut : new int[]{19, 40, 52, 70, raw.length - 1})
            assertTrue("truncated type-12 " + cut, decode(190, 17, Arrays.copyOf(raw, cut)).decodeIncomplete);
        assertTrue("trailing byte flagged", decode(190, 17, Arrays.copyOf(raw, raw.length + 1)).decodeIncomplete);
        byte[] format6 = raw.clone();
        format6[0] = 6;
        IComponentDefinitions bad = decode(190, 17, format6);
        assertTrue(bad.decodeIncomplete && bad.decodeFailureReason.contains("Unsupported native widget format"));
        byte[] hostile = raw(1438, 18).clone();
        hostile[19 + 5] = 0x7f; hostile[19 + 6] = (byte) 0xff; hostile[19 + 7] = (byte) 0xff; hostile[19 + 8] = (byte) 0xff;
        bad = decode(1438, 18, hostile);
        assertTrue(bad.decodeIncomplete && bad.decodeFailureReason.contains("type-16 byte count"));
        assertFalse("cached record untouched by hostile copy", IComponentDefinitions.getInterface(1438)[18].decodeIncomplete);
    }

    private static byte[] raw(int id, int component) {
        return Cache.STORE.getIndexes()[3].getFile(id, component);
    }

    private static IComponentDefinitions decode(int id, int component, byte[] data) {
        IComponentDefinitions d = new IComponentDefinitions();
        d.ihash = (id << 16) | component;
        d.decode(new InputStream(data, true));
        return d;
    }

    private static IComponentDefinitions fixture(int id, int component) {
        IComponentDefinitions d = IComponentDefinitions.getInterface(id)[component];
        assertNotNull("fixture " + id + ":" + component, d);
        assertFalse("complete fixture " + id + ":" + component + " " + d.decodeFailureReason, d.decodeIncomplete);
        return d;
    }

    private static String hex(byte[] data) {
        StringBuilder b = new StringBuilder();
        for (byte x : data) b.append(String.format("%02x", x & 0xff));
        return b.toString();
    }
}
