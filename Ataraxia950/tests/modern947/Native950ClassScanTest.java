package modern947;

import com.rs.utils.Utils;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Regression cover for the packaged-runtime class scan.
 *
 * <p>{@code CombatScriptsHandler.init} and {@code Scanner.scan} find their content by
 * scanning the class path. FastClasspathScanner 3.0.3 predates JDK 9 and can only
 * enumerate a class loader it recognises, which in practice means
 * {@link URLClassLoader}. From JDK 9 the application loader is
 * {@code jdk.internal.loader.ClassLoaders$AppClassLoader}, so handing it to
 * {@code overrideClassLoaders} made the scanner find nothing at all.
 *
 * <p>That combination - JDK 25 <em>and</em> a jar class path - is exactly how the
 * packaged server runs, and it is the one combination no test exercised: the suite
 * runs on Java 8 from build directories. Measured on the real tree with the packaged
 * jar class path, the override yielded 0 classes on Java 25 against 363 without it,
 * while Java 8 returned 363 either way, so NPC combat would have silently registered
 * 5 of 372 scripts in production.
 *
 * <p>These tests pin the decision rather than the runtime, because a Java 8 JUnit run
 * cannot reproduce a JDK 25 loader. The stand-in is a <em>parentless</em>
 * non-URLClassLoader: the scanner cannot enumerate it and cannot fall back through a
 * parent chain, so overriding with it yields 0 while not overriding yields the whole
 * package. Measured on this tree under Java 8: 0 against 363. A loader that merely
 * delegates to the application loader would NOT discriminate here - the scanner walks
 * the parent and returns 363 either way - so this test deliberately cuts the parent.
 */
public final class Native950ClassScanTest {

    /** A package that exists in this project and is stable enough to assert on. */
    private static final String COMBAT_IMPL = "com.rs.game.npc.combat.impl";

    /**
     * Not a URLClassLoader and deliberately parentless, standing in for the JDK 9+
     * application loader that the scanner cannot introspect. Overriding with this is
     * what the pre-fix code did, and it finds nothing.
     */
    private static final class OpaqueLoader extends ClassLoader {
        private OpaqueLoader() { super(null); }
    }

    @Test
    public void theScanFindsTheCombatScriptPackageAtAll() {
        List<String> names = Utils.scanPackageClassNames(COMBAT_IMPL, null);
        assertFalse("the combat script package must be discoverable on the class path", names.isEmpty());
        assertTrue("expected the full combat script package, saw " + names.size(), names.size() > 300);
        for (String name : names) {
            assertTrue("strictWhitelist must not leak classes outside the package: " + name,
                    name.startsWith(COMBAT_IMPL + "."));
        }
    }

    @Test
    public void aLoaderTheScannerCannotIntrospectDoesNotSuppressTheScan() {
        List<String> withoutLoader = Utils.scanPackageClassNames(COMBAT_IMPL, null);
        List<String> opaque = Utils.scanPackageClassNames(COMBAT_IMPL, new OpaqueLoader());
        // The JDK 9+ application loader takes this branch. Before the fix this returned
        // an empty list, which in the packaged server meant 5 of 372 combat scripts.
        assertEquals("a non-URLClassLoader must fall back to the class path, not scan nothing",
                withoutLoader.size(), opaque.size());
        assertFalse(opaque.isEmpty());
    }

    @Test
    public void aRealUrlClassLoaderIsStillHonoured() {
        ClassLoader context = Native950ClassScanTest.class.getClassLoader();
        List<String> baseline = Utils.scanPackageClassNames(COMBAT_IMPL, null);
        List<String> viaUrlLoader = Utils.scanPackageClassNames(
                COMBAT_IMPL, new URLClassLoader(new URL[0], context));
        assertEquals("a URLClassLoader delegating to the context loader sees the same package",
                baseline.size(), viaUrlLoader.size());
    }

    @Test
    public void getClassesLoadsEveryScannedNameOnce() throws Exception {
        Class<?>[] classes = Utils.getClasses(COMBAT_IMPL);
        assertTrue("expected the full combat script package, saw " + classes.length, classes.length > 300);
        java.util.Set<String> distinct = new java.util.HashSet<String>();
        for (Class<?> found : classes) {
            assertTrue("duplicate class from the directory and jar passes: " + found.getName(),
                    distinct.add(found.getName()));
            assertTrue(found.getName().startsWith(COMBAT_IMPL + "."));
        }
    }
}
