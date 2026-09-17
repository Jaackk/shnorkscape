package com.rs.game.player.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Region;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/** Actual 950 catalogue bytes and legacy regional choices, independent of gameplay packets.
 * The 950 enums append six songs; all 1616 prior names and 1614 prior resource mappings
 * are unchanged, independently compared against the 947 logical cache files.
 */
public class Native950RegionMusicCatalogTest {
    private JsonObject files() throws Exception {
        return new Gson().fromJson(new String(Files.readAllBytes(Paths.get(
                "tests/fixtures/native950-region-music.json")), StandardCharsets.UTF_8), JsonObject.class)
                .getAsJsonObject("files");
    }

    private byte[] file(JsonObject files, int index, int group, int file) {
        return Base64.getDecoder().decode(files.getAsJsonObject(index + "/" + group + "/" + file)
                .get("base64").getAsString());
    }

    private Native950RegionMusicCatalog catalog() throws Exception {
        JsonObject files = files();
        return new Native950RegionMusicCatalog((i, g, f) -> file(files, i, g, f), archive -> true);
    }

    private void track(Native950RegionMusicCatalog catalog, int region, String name, int id, int archive) {
        Native950RegionMusicCatalog.Track track = catalog.lookup(region);
        assertNotNull("region " + region, track);
        assertEquals(name, track.name); assertEquals(id, track.trackId); assertEquals(archive, track.archiveId);
    }

    @Test public void lumbridgeAndItsNeighboursUseTheirActual950Resources() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        track(catalog, 12850, "Harmony", 58, 36067);
        track(catalog, 12851, "Autumn Voyage", 17, 37990);
        track(catalog, 12849, "Yesteryear", 161, 37355);
        track(catalog, 12593, "Book of Spells", 23, 38764);
    }

    @Test public void eachCatalogueFileIsRequiredAndRejectsOneByteDrift() throws Exception {
        JsonObject files = files();
        assertEquals(2, files.size());
        for (String changed : files.keySet()) {
            try {
                new Native950RegionMusicCatalog((i, g, f) -> {
                    byte[] data = file(files, i, g, f);
                    if (changed.equals(i + "/" + g + "/" + f)) data[data.length - 1] ^= 1;
                    return data;
                }, archive -> true);
                fail("Accepted changed " + changed);
            } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains(changed)); }
            try {
                new Native950RegionMusicCatalog((i, g, f) -> changed.equals(i + "/" + g + "/" + f)
                        ? null : file(files, i, g, f), archive -> true);
                fail("Accepted missing " + changed);
            } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains(changed)); }
        }
    }

    @Test public void onlyExplicitSpellingCorrectionsAndWhitespaceAreApplied() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        track(catalog, 12341, "Barbarianism", 257, 23353);
        track(catalog, 12082, "Sea Shanty II", 107, 34639);
        track(catalog, 11570, "Wander", 140, 39191);
        track(catalog, 12446, "Wilderness I", 142, 32428);
    }

    @Test public void ambiguousLegacyNamesAreNotGuessed() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        assertNull(catalog.lookup(9781)); // "Gnome Village" cannot distinguish I from II.
        assertNull(catalog.lookup(11575)); // "Spiritual" exists in neither the 910 nor 950 catalogue.
    }

    @Test public void duplicateNamesPreserveTheOldChoiceAndSameResourceIdentity() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        track(catalog, 9265, "Far Away", 292, 5305); // ID582 has the same title but resource30432.
        track(catalog, 13874, "Distant Land", 353, 15482); // ID577 shares resource15482.
    }

    @Test public void unknownOrInvalidRegionsHaveNoRandomFallback() throws Exception {
        JsonObject files = files();
        byte[] names = file(files, 17, 5, 65), resources = file(files, 17, 5, 71);
        // Raw enum default metadata must be consumed, but cannot become a fallback track.
        assertArrayEquals(new byte[] {3, 32, 0, 0},
                java.util.Arrays.copyOfRange(names, names.length - 4, names.length));
        assertArrayEquals(new byte[] {4, 0, 0, 8, 0x77, 0},
                java.util.Arrays.copyOfRange(resources, resources.length - 6, resources.length));
        Native950RegionMusicCatalog catalog = catalog();
        for (int region : new int[] {-1, 0, 13106, 13360, 13407, 18516, 18517, 18773, 18775,
                65536, Integer.MAX_VALUE}) assertNull(catalog.lookup(region));
    }

    @Test public void primaryRegionalCoverageMatchesTheIndependentSourceAndCacheCensus() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        int declared = 0, resolved = 0;
        java.util.Set<Integer> archives = new java.util.HashSet<Integer>();
        for (int region = 0; region <= 65535; region++) {
            if (Region.getMusicNames(region).length == 0) continue;
            declared++;
            Native950RegionMusicCatalog.Track selected = catalog.lookup(region);
            if (selected != null) { resolved++; archives.add(selected.archiveId); }
        }
        assertEquals(315, declared);
        assertEquals(307, resolved);
        assertEquals(241, archives.size());
    }

    @Test public void firstRegionalNameWinsAndLookupIsMemoized() throws Exception {
        Native950RegionMusicCatalog catalog = catalog();
        Native950RegionMusicCatalog.Track selected = catalog.lookup(9012);
        assertNotNull(selected);
        assertEquals("Elven Daffodil", selected.name); // First of the existing seventeen region choices.
        for (int i = 0; i < 20; i++) assertSame(selected, catalog.lookup(9012));
    }

    @Test public void unavailableMusicDoesNotBecomeAnIndex14SoundOrAnotherTrack() throws Exception {
        JsonObject files = files(); AtomicInteger probes = new AtomicInteger();
        Native950RegionMusicCatalog catalog = new Native950RegionMusicCatalog(
                (i, g, f) -> file(files, i, g, f), archive -> {
                    probes.incrementAndGet(); assertEquals(7012, archive); return false;
                });
        // This region has seventeen alternatives. An unavailable primary still means no selection.
        assertNull(catalog.lookup(9012)); assertNull(catalog.lookup(9012));
        assertEquals(1, probes.get());
    }

    @Test public void archiveAvailabilityIsSharedAcrossRegionsWithTheSameMusic() throws Exception {
        JsonObject files = files(); AtomicInteger probes = new AtomicInteger();
        Native950RegionMusicCatalog catalog = new Native950RegionMusicCatalog(
                (i, g, f) -> file(files, i, g, f), archive -> { probes.incrementAndGet(); return true; });
        Native950RegionMusicCatalog.Track first = catalog.lookup(9008), second = catalog.lookup(9007);
        assertNotNull(first); assertNotNull(second);
        assertEquals("Lost Soul", first.name); assertEquals(first.archiveId, second.archiveId);
        assertEquals(1, probes.get());
    }

    @Test public void constructionAndLookupWithoutACacheRemainSafe() {
        Store previous = Cache.STORE;
        try {
            Cache.STORE = null;
            Native950RegionMusicCatalog catalog = new Native950RegionMusicCatalog();
            assertNull(catalog.lookup(12850));
        } finally { Cache.STORE = previous; }
    }
}
