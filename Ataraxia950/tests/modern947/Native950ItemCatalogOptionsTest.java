package com.rs.game.player.client;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950ItemCatalogOptionsTest {
    @Test public void pinnedSeedGetsCompleteCurrentMenuAndKeepsVerifiedEquipment() {
        Native950ItemCatalog.Entry pinned = entry("Bronze sword", new String[]{null,"Wield",null,null,null}, 3, 2);
        Native950ItemCatalog.Entry current = entry("Bronze sword", new String[]{null,"Wield","Check",null,"drop"}, -1, 0);
        int[] calls = {0};
        Native950ItemCatalog catalog = new Native950ItemCatalog(Collections.singleton(pinned))
                .withCurrentCache(id -> { calls[0]++; return current; });
        Native950ItemCatalog.Entry resolved = catalog.get(1277);
        assertEquals("Wield", resolved.option(2));
        assertEquals("Check", resolved.option(3));
        assertEquals("drop", resolved.option(5));
        assertEquals(3, resolved.equipSlot);
        assertEquals(2, resolved.equipOption);
        assertSame(resolved, catalog.get(1277));
        assertEquals(1, calls[0]);
    }

    @Test public void currentLabelsCannotGrantUnverifiedEquipmentOrReviveRemovedActions() {
        Native950ItemCatalog.Entry current = entry("Current weapon", new String[]{"Read","Wield",null,null,"Destroy"}, -1, 0);
        Native950ItemCatalog.Entry resolved = Native950ItemCatalog.withCacheOptions(null, current);
        assertEquals("Wield", resolved.option(2));
        assertEquals("Destroy", resolved.option(5));
        assertEquals(-1, resolved.equipSlot);
        Native950ItemCatalog.Entry obsolete = entry("Current weapon", new String[]{"Read","Wield",null,null,null}, 3, 2);
        Native950ItemCatalog.Entry changed = entry("Current weapon", new String[]{null,null,null,null,"Destroy"}, -1, 0);
        resolved = Native950ItemCatalog.withCacheOptions(obsolete, changed);
        assertNull(resolved.option(1));
        assertNull(resolved.option(2));
        assertEquals(-1, resolved.equipSlot);
        assertEquals(0, resolved.equipOption);
    }

    @Test public void changedIdentityAndUnavailableCacheCannotReusePinnedEquipment() {
        Native950ItemCatalog.Entry pinned = entry("Old weapon", new String[]{null,"Wield",null,null,null}, 3, 2);
        Native950ItemCatalog.Entry changed = entry("Different weapon", new String[]{null,"Wield",null,null,"drop"}, -1, 0);
        Native950ItemCatalog.Entry resolved = Native950ItemCatalog.withCacheOptions(pinned, changed);
        assertEquals("Different weapon", resolved.name);
        assertEquals(-1, resolved.equipSlot);
        assertNull(new Native950ItemCatalog(Collections.singleton(pinned)).withCurrentCache(id -> null).get(1277));
    }

    private static Native950ItemCatalog.Entry entry(String name, String[] options, int equipSlot, int equipOption) {
        return new Native950ItemCatalog.Entry(1277, name, false, options, equipSlot, equipOption);
    }
}