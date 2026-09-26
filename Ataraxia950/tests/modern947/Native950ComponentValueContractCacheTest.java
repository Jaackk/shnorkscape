package modern947;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.IComponentDefinitions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Stage B Phase 3: which real 950 components actually gate the native event that opcode 124
 * (Native950Actions.COMPONENT_VALUE_OPCODE) reports. The 950 event router (950
 * {@code 0x1401a91c0}) looks the target component up (950 {@code 0x1401a1070}, the same
 * interface/component hash lookup {@link IComponentDefinitions#getInterface} decodes) and only
 * proceeds when bit 24 of that component's {@code activeProperties.settings} is set - IF_SETEVENTS.
 * This sweeps the whole pinned 950 cache (depends on Stage B Phase 2's decoder fix: before it, bit
 * 24 was unreachable because the option mask was read as 24 bits) and pins the exact count and
 * identities, so a future cache or decoder change cannot silently add or drop one unnoticed.
 *
 * <p>Skipped, like {@code Native950ComponentDecoderCacheTest}, when the untracked cache/ is absent.
 */
public final class Native950ComponentValueContractCacheTest {
    private static final Path CACHE = Paths.get("..", "cache");
    private static Store previous;
    private static boolean installed;

    @BeforeClass public static void openCache() throws Exception {
        Assume.assumeTrue("local 950 cache not present at " + CACHE.toAbsolutePath(), Files.isDirectory(CACHE.resolve("3")));
        previous = Cache.STORE;
        Cache.STORE = Store.openFlatReadOnly(CACHE);
        installed = true;
    }

    @AfterClass public static void restoreCache() {
        if (installed) Cache.STORE = previous;
    }

    @Test public void exactlyTenComponentsSetIfSetEventsBit24() {
        Map<String, IComponentDefinitions> found = new TreeMap<String, IComponentDefinitions>();
        int size = com.rs.utils.Utils.getInterfaceDefinitionsSize();
        for (int id = 0; id < size; id++) {
            IComponentDefinitions[] defs = IComponentDefinitions.getInterface(id);
            if (defs == null) continue;
            for (IComponentDefinitions d : defs) {
                if (d != null && d.activeProperties != null && (d.activeProperties.settings & 0x01000000) != 0)
                    found.put(id + ":" + (d.ihash & 0xffff), d);
            }
        }
        assertEquals(found.keySet().toString(), 10, found.size());

        // The two type-16 "bar" (native stepper) pairs: exact interface/component ids and the
        // third additional hook (the more plausible source of an opcode-124 value report; the
        // other additional-hook slots are null on every one of these ten records).
        assertEquals(16, found.get("590:7").type);
        assertEquals(16, found.get("590:8").type);
        assertEquals(16, found.get("1438:18").type);
        assertEquals(16, found.get("1438:19").type);
        assertEquals(3553, found.get("590:7").additionalHooks[2][0]);
        assertEquals(3553, found.get("1438:18").additionalHooks[2][0]);
        assertEquals(13155, found.get("590:8").additionalHooks[2][0]);
        assertEquals(13155, found.get("1438:19").additionalHooks[2][0]);

        // The six type-13 native text inputs are a mixed group: three carry an "event_text"
        // additional hook (a string-valued commit, not this opcode's int payload), two carry a
        // plain two-argument hook with no string tag, and one (1475:32) has no additional hooks
        // at all. All six are still bit-24-gated; which of them, if any, ever emits opcode 124
        // is unresolved without a live capture.
        int type13 = 0, eventText = 0;
        for (IComponentDefinitions d : found.values()) {
            if (d.type != 13) continue;
            type13++;
            if (d.additionalHooks[1] != null && d.additionalHooks[1].length > 1
                    && "event_text".equals(d.additionalHooks[1][1])) eventText++;
        }
        assertEquals(6, type13);
        assertEquals(3, eventText);

        // Negative control: the Action Bar Equipment Binding row-select/value protocol
        // (Native950Settings, interface 365 components 19/20) does not set this bit. It already
        // runs over ordinary IF_BUTTON and is unrelated to opcode 124.
        IComponentDefinitions row = IComponentDefinitions.getInterface(365)[19];
        IComponentDefinitions value = IComponentDefinitions.getInterface(365)[20];
        assertTrue(row.activeProperties == null || (row.activeProperties.settings & 0x01000000) == 0);
        assertTrue(value.activeProperties == null || (value.activeProperties.settings & 0x01000000) == 0);
    }
}
