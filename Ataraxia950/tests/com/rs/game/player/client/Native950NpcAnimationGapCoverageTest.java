package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Stage B Phase 4: NPC idle/attack animation gap-filling, adopted from Artaven's
 * {@code Native950NpcAttackAnimations}/{@code Native950NpcDrawnWeapons} (cache-derived weapon-family
 * and drawn-model evidence, wiki cross-checked) and {@code compatibleWithRender} (the
 * legacy-frame-on-animaya render gate). Both tables and the gate are consulted ONLY from the
 * Developer Console's NPC preview ({@link Native950DeveloperPreview}); Shnorkscape's live combat
 * resolution ({@link Native950NpcCombatCatalog}) is untouched by this pass. See
 * docs/STAGE-B-PHASE4-NPC-ANIMATIONS-20260926.md.
 */
public final class Native950NpcAnimationGapCoverageTest {
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

    // ------------------------------------------------------------- real cache: end-to-end coverage

    /**
     * Varrock guards 5919/5920/9234 carry no attack option in the inherited 910-era combat table
     * (no authored row at all: {@code Native950NpcCombatCatalog} refuses them), and previously
     * showed no attack preview. They are drawn holding a Bronze sword (struct 14922); the preview
     * now plays its published swing instead.
     */
    @Test public void varrockGuardsGainAPreviewAttackFromTheirDrawnWeapon() {
        for (int id : new int[]{5919, 5920, 9234}) {
            assertNull("no authored combat row for " + id, Native950NpcCombatCatalog.inspectRunningCache(id).profile);
            assertTrue("drawn-weapon row expected for " + id, Native950NpcDrawnWeapons.row(id) != null);
            Native950DeveloperPreview preview = Native950DeveloperPreview.resolve(id);
            assertTrue("preview attack sequence expected for " + id, preview.attack >= 0);
        }
    }

    /** The four elemental wizards (2709-2712) are the same gap the user physically reported. */
    @Test public void elementalWizardsGainAPreviewAttack() {
        for (int id : new int[]{2709, 2710, 2711, 2712}) {
            Native950DeveloperPreview preview = Native950DeveloperPreview.resolve(id);
            assertTrue("preview attack sequence expected for " + id, preview.attack >= 0);
        }
    }

    /**
     * The two tables ship rows at all; the exhaustive per-row re-hash against the live cache
     * ({@code verifyCacheBindings()}) is checked by {@code Native950CachePreflight} instead of
     * here. That sweep decodes every NPC id the tables name through the shared, process-lifetime
     * {@code NPCDefinitions} cache, which never invalidates a stale entry left by an earlier test's
     * synthetic fixture cache in the same JVM - exactly the kind of cross-test interference this
     * suite must not risk on an unrelated id.
     */
    @Test public void bothTablesShipRows() {
        assertFalse(Native950NpcAttackAnimations.rows().isEmpty());
        assertFalse(Native950NpcDrawnWeapons.rows().isEmpty());
    }

    /** An NPC with no row in either table keeps its unchanged -1/refused preview behaviour. */
    @Test public void anUncoveredNpcIsUnaffected() {
        assertNull(Native950NpcAttackAnimations.row(1));
        assertNull(Native950NpcDrawnWeapons.row(1));
    }

    /** The Developer Console's combat/provenance report now names both independent Slayer signals. */
    @Test public void combatInspectorReportsSlayerCrossCheck() {
        java.util.List<String> report = Native950CombatInspector.npc(21502); // Abyssal demon, param50=42
        boolean found = false;
        for (String line : report) if (line.startsWith("Slayer: cache category param50=")) { found = true; assertTrue(line.contains("42")); }
        assertTrue("Slayer cross-check line expected in the combat report", found);
        NPCDefinitions definition = NPCDefinitions.getNPCDefinitions(21502);
        assertEquals("Abyssal demon", definition.name.trim());
    }
}
