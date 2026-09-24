package com.rs.game.player.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Paired950 raw SeqTypes, independent of a live cache and player equipment policy. */
public final class Native950NpcCombatAnimationsTest {
    private static final Map<Integer, byte[]> RAW = fixtures();
    private static final Native950NpcCombatAnimations.DefinitionSource SOURCE = id -> RAW.get(id);

    @Test public void admittedNpcSequencesKeepTheirVerifiedDurations() {
        int[][] samples = {{422,39},{5387,35},{5388,56},{5389,153},{6182,90},{6183,30},{6184,55},
                {23567,60},{23566,161},{14859,63},{14860,165}};
        for (int[] sample : samples) {
            Native950NpcCombatAnimations.Sequence sequence = Native950NpcCombatAnimations.resolve(sample[0], SOURCE);
            assertNotNull("Sequence " + sample[0], sequence);
            assertEquals(sample[0], sequence.id());
            assertEquals(sample[1], sequence.durationCycles());
        }
    }

    @Test public void generatedCatalogCoversAllCompatibleAuthoredBindingsAndOmitsChangedFrames() {
        assertEquals(880, Native950NpcCombatAnimations.verifiedDefinitionCount());
        for (int id : new int[] {3310,3311,3312,17347,32041,32050,32053,32205,32206,32208,32695,32697})
            assertFalse("Changed sequence " + id, Native950NpcCombatAnimations.hasCatalogBinding(id));
        assertFalse(Native950NpcCombatAnimations.hasCatalogBinding(Integer.MAX_VALUE));
    }

    @Test public void absenceAndUnknownIdsNeverReadTheCacheOrSubstituteAnAnimation() {
        Native950NpcCombatAnimations.DefinitionSource forbidden = id -> { throw new AssertionError("Unexpected read " + id); };
        assertNull(Native950NpcCombatAnimations.resolve(-1, forbidden));
        assertNull(Native950NpcCombatAnimations.resolve(-2, forbidden));
        assertNull(Native950NpcCombatAnimations.resolve(3310, forbidden));
        assertTrue(Native950NpcCombatAnimations.acceptedFromRunningCache(-1));
        assertFalse(Native950NpcCombatAnimations.acceptedFromRunningCache(-2));
        assertEquals(-1, Native950NpcCombatAnimations.durationCycles(-1));
    }

    @Test public void changedTruncatedMissingOrUnavailableDefinitionsFailClosed() {
        byte[] changed = RAW.get(5387).clone(); changed[1] ^= 1;
        assertNull(Native950NpcCombatAnimations.resolve(5387, id -> changed));
        assertNull(Native950NpcCombatAnimations.resolve(5387, id -> new byte[] {1}));
        assertNull(Native950NpcCombatAnimations.resolve(5387, id -> null));
        assertNull(Native950NpcCombatAnimations.resolve(5387, id -> {throw new IllegalStateException("unavailable");}));
        assertNull(Native950NpcCombatAnimations.resolve(5387, id -> RAW.get(5388)));
    }

    @Test public void cacheRechecksBindingsWhenTheStoreIdentityChanges() {
        Native950NpcCombatAnimations.VerificationCache cache = new Native950NpcCombatAnimations.VerificationCache();
        Object firstStore = new Object(); Object replacementStore = new Object();
        int[] calls = {0};
        Native950NpcCombatAnimations.DefinitionSource good = id -> {calls[0]++; return RAW.get(id);};
        Native950NpcCombatAnimations.Sequence first = cache.resolve(firstStore, 5387, good);
        assertNotNull(first);
        assertSame(first, cache.resolve(firstStore, 5387, good)); assertEquals(1, calls[0]);
        assertNull(cache.resolve(replacementStore, 5387, id -> {calls[0]++; return new byte[] {0};}));
        assertEquals(2, calls[0]);
        // The fixed flat store is immutable; a refusal is retained until its identity changes.
        assertNull(cache.resolve(replacementStore, 5387, good)); assertEquals(2, calls[0]);
        assertNotNull(cache.resolve(firstStore, 5387, good)); assertEquals(3, calls[0]);
    }

    @Test public void oneRefusedSequenceDoesNotPoisonOtherCachedSequences() {
        Native950NpcCombatAnimations.VerificationCache cache = new Native950NpcCombatAnimations.VerificationCache();
        Object store = new Object();
        assertNull(cache.resolve(store, 5387, id -> null));
        assertNotNull(cache.resolve(store, 5388, SOURCE));
        assertNull(cache.resolve(store, 5387, SOURCE));
    }

    private static Map<Integer, byte[]> fixtures() {
        try {
            String json = new String(Files.readAllBytes(Paths.get("tests/fixtures/native950-npc-combat-animations.json")), StandardCharsets.US_ASCII);
            Map<String, String> encoded = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
            Map<Integer, byte[]> raw = new HashMap<Integer, byte[]>();
            for (Map.Entry<String, String> entry : encoded.entrySet())
                raw.put(Integer.parseInt(entry.getKey()), Base64.getDecoder().decode(entry.getValue()));
            return raw;
        } catch (Exception error) { throw new ExceptionInInitializerError(error); }
    }
}
