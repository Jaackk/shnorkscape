package modern947;

import com.rs.game.player.client.Native950IdValidity;
import com.rs.game.player.client.Native950IdValidity.Kind;
import com.rs.game.player.client.Native950IdValidity.Verdict;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The 910 -&gt; 950 id validity table, from the loader's side. Cache free: every case runs on
 * a small hand-written fixture whose rows are the four situations the milestone cares about
 * (an id that survived, one that now names something else, one with no 950 definition, and
 * one that kept its name but lost the equipment slot that made it usable), plus the shapes
 * that must reject the whole table rather than produce a wrong "safe".
 *
 * <p>One test does read the checked-in table, but only to prove it parses, that the two ids
 * the milestone brief names by hand - NPC 42 "Sheep" -&gt; "Bill" and item 1079 "Rune
 * platelegs" with its slot removed - really are refused by the shipped file, and that the
 * case-only renames the owner admitted on 2026-09-07 really are accepted by it.
 */
public class Native950IdValidityTest {

    /**
     * The fixture. Deliberately tiny and deliberately shaped like the real table: id sets
     * as inclusive ranges, and one reason string per non-same id.
     */
    private static final String FIXTURE =
            "{\n"
            + "  \"format\": 2,\n"
            + "  \"revision\": 950,\n"
            + "  \"legacyCache\": \"fixture-910\",\n"
            + "  \"modernCache\": \"fixture-950\",\n"
            + "  \"safeVerdicts\": [\"same\",\"restyled\"],\n"
            + "  \"kinds\": {\n"
            + "    \"npc\": {\n"
            + "      \"scanned\": {\"total\": 8, \"same\": 3, \"restyled\": 1, \"renamed\": 1, \"repurposed\": 2, \"missing\": 1, \"unverifiable\": 0},\n"
            + "      \"referenced\": {\"total\": 8, \"same\": 3, \"restyled\": 1, \"renamed\": 1, \"repurposed\": 2, \"missing\": 1, \"unverifiable\": 0},\n"
            + "      \"coverage\": \"whole-index\",\n"
            + "      \"same\": \"0-1,494\",\n"
            + "      \"missing\": \"7000\",\n"
            + "      \"unverifiable\": \"\",\n"
            + "      \"restyled\": {\"117\": \"\\\"Hill Giant\\\" -> \\\"Hill giant\\\" (case only)\"},\n"
            + "      \"renamed\": {\"1631\": \"\\\"Rockslug\\\" -> \\\"Rock slug\\\"\"},\n"
            + "      \"repurposed\": {\"42\": \"\\\"Sheep\\\" -> \\\"Bill\\\"\",\n"
            + "        \"1327\": \"\\\"Bear Cub\\\" -> \\\"Bear cub\\\" differs only in case but size changed 1 -> 2\"}\n"
            + "    },\n"
            + "    \"item\": {\n"
            + "      \"scanned\": {\"total\": 4, \"same\": 1, \"restyled\": 0, \"renamed\": 0, \"repurposed\": 2, \"missing\": 1, \"unverifiable\": 0},\n"
            + "      \"referenced\": {\"total\": 4, \"same\": 1, \"restyled\": 0, \"renamed\": 0, \"repurposed\": 2, \"missing\": 1, \"unverifiable\": 0},\n"
            + "      \"coverage\": \"referenced-ids-only\",\n"
            + "      \"same\": \"995\",\n"
            + "      \"missing\": \"29492\",\n"
            + "      \"unverifiable\": \"1\",\n"
            + "      \"restyled\": {},\n"
            + "      \"renamed\": {},\n"
            + "      \"repurposed\": {\"1079\": \"\\\"Rune platelegs\\\" kept its name but equipSlot changed 7 -> -1\",\n"
            + "        \"6\": \"\\\"Cannon base\\\" kept its name but inventory option 1 changed \\\"Set-up\\\" -> \\\"\\\"\"}\n"
            + "    },\n"
            + "    \"object\": {\n"
            + "      \"scanned\": {\"total\": 1, \"same\": 1, \"restyled\": 0, \"renamed\": 0, \"repurposed\": 0, \"missing\": 0, \"unverifiable\": 0},\n"
            + "      \"referenced\": {\"total\": 1, \"same\": 1, \"restyled\": 0, \"renamed\": 0, \"repurposed\": 0, \"missing\": 0, \"unverifiable\": 0},\n"
            + "      \"coverage\": \"referenced-ids-only\",\n"
            + "      \"same\": \"2213\",\n"
            + "      \"missing\": \"\",\n"
            + "      \"unverifiable\": \"\",\n"
            + "      \"restyled\": {},\n"
            + "      \"renamed\": {},\n"
            + "      \"repurposed\": {}\n"
            + "    }\n"
            + "  }\n"
            + "}\n";

    private static Native950IdValidity fixture() {
        return Native950IdValidity.load(new StringReader(FIXTURE));
    }

    @Test
    public void anIdThatMeansTheSameThingInBothCachesIsSafe() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.SAME, table.verdict(Kind.NPC, 0));
        assertEquals(Verdict.SAME, table.verdict(Kind.NPC, 1));
        assertEquals(Verdict.SAME, table.verdict(Kind.NPC, 494));
        assertTrue(table.isSafe(Kind.NPC, 494));
        assertTrue(table.isSafe(Kind.ITEM, 995));
        assertTrue(table.isSafe(Kind.OBJECT, 2213));
    }

    @Test
    public void aRepurposedIdIsRefusedAndSaysWhatItBecame() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.REPURPOSED, table.verdict(Kind.NPC, 42));
        assertFalse("NPC 42 loads cleanly in 950 and is the wrong creature", table.isSafe(Kind.NPC, 42));
        String reason = table.reason(Kind.NPC, 42);
        assertTrue(reason, reason.contains("Sheep"));
        assertTrue(reason, reason.contains("Bill"));
    }

    @Test
    public void anIdWithNo950DefinitionIsRefusedAndSaysSo() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.MISSING, table.verdict(Kind.NPC, 7000));
        assertFalse(table.isSafe(Kind.NPC, 7000));
        assertEquals("no usable 950 definition", table.reason(Kind.NPC, 7000));
    }

    @Test
    public void anItemThatKeptItsNameButLostItsEquipmentSlotIsRefused() {
        Native950IdValidity table = fixture();
        assertEquals("name equality alone must not make an id safe",
                Verdict.REPURPOSED, table.verdict(Kind.ITEM, 1079));
        assertFalse(table.isSafe(Kind.ITEM, 1079));
        String reason = table.reason(Kind.ITEM, 1079);
        assertTrue(reason, reason.contains("equipSlot"));
        assertTrue(reason, reason.contains("7 -> -1"));
    }

    @Test
    public void anItemThatKeptItsNameAndMovedAnOptionIsRefused() {
        // An option index is what a click is dispatched by, so a name-equal id whose option
        // array moved is as dangerous as one that lost its equipment slot: the menu the client
        // renders and the branch the 910 handler runs would disagree.
        Native950IdValidity table = fixture();
        assertEquals("option parity is part of the item rule, not just the name",
                Verdict.REPURPOSED, table.verdict(Kind.ITEM, 6));
        assertFalse(table.isSafe(Kind.ITEM, 6));
        String reason = table.reason(Kind.ITEM, 6);
        assertTrue(reason, reason.contains("inventory option 1"));
        assertTrue(reason, reason.contains("Set-up"));
    }

    @Test
    public void aRenamedIdIsRefusedToo() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.RENAMED, table.verdict(Kind.NPC, 1631));
        assertFalse("only 'same' is trusted; a rename still loses every 910 field assumption",
                table.isSafe(Kind.NPC, 1631));
        assertTrue(table.reason(Kind.NPC, 1631).contains("Rock slug"));
    }

    @Test
    public void aCaseOnlyRenameIsSafeAndStillSaysWhatItWasCalled() {
        // The owner's 2026-09-07 decision. 950 moved NPC names to sentence case; a letter's
        // case cannot change what a definition denotes, and the probe only reaches this
        // verdict after every size and option check that 'same' runs has already passed.
        Native950IdValidity table = fixture();
        assertEquals(Verdict.RESTYLED, table.verdict(Kind.NPC, 117));
        assertTrue("a case-only rename is safe to spawn", table.isSafe(Kind.NPC, 117));
        String reason = table.reason(Kind.NPC, 117);
        assertTrue(reason, reason.contains("Hill Giant"));
        assertTrue(reason, reason.contains("Hill giant"));
        assertTrue("a safe id must still be able to say why", reason.contains("case-only"));
    }

    @Test
    public void aCaseOnlyRenameThatAlsoMovedAFieldStaysRefused() {
        // The loosening is one character class wide and nothing more. 1327 is the real case in
        // the shipped table: "Bear Cub" -> "Bear cub" AND size 1 -> 2, so the probe files it
        // under repurposed and the loader refuses it like any other repurposed id.
        Native950IdValidity table = fixture();
        assertEquals(Verdict.REPURPOSED, table.verdict(Kind.NPC, 1327));
        assertFalse(table.isSafe(Kind.NPC, 1327));
        assertTrue(table.reason(Kind.NPC, 1327), table.reason(Kind.NPC, 1327).contains("size changed"));
    }

    @Test
    public void anIdWithNo910DefinitionCannotBeJudgedAndIsRefused() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.UNVERIFIABLE, table.verdict(Kind.ITEM, 1));
        assertFalse(table.isSafe(Kind.ITEM, 1));
        assertEquals("no 910 definition to compare against", table.reason(Kind.ITEM, 1));
    }

    @Test
    public void anIdTheTableNeverSawIsUnclassifiedAndRefused() {
        Native950IdValidity table = fixture();
        assertEquals(Verdict.UNCLASSIFIED, table.verdict(Kind.NPC, 999999));
        assertFalse(table.isSafe(Kind.NPC, 999999));
        assertTrue(table.reason(Kind.NPC, 999999).contains("not classified"));
        assertEquals("a negative id is never safe", Verdict.UNCLASSIFIED, table.verdict(Kind.NPC, -1));
        assertFalse(table.isSafe(Kind.ITEM, -1));
    }

    @Test
    public void rangeEndpointsAreInclusiveAndTheGapsAreNotSafe() {
        Native950IdValidity table = fixture();
        assertTrue(table.isSafe(Kind.NPC, 0));
        assertTrue(table.isSafe(Kind.NPC, 1));
        assertFalse("2 sits in the gap between the two npc ranges", table.isSafe(Kind.NPC, 2));
        assertFalse(table.isSafe(Kind.NPC, 493));
        assertTrue(table.isSafe(Kind.NPC, 494));
        assertFalse(table.isSafe(Kind.NPC, 495));
    }

    @Test
    public void countsAndCoverageComeFromTheTable() {
        Native950IdValidity table = fixture();
        assertEquals(8, table.scanned(Kind.NPC).total);
        assertEquals(3, table.scanned(Kind.NPC).same);
        assertEquals(1, table.scanned(Kind.NPC).restyled);
        assertEquals("safe() is same plus restyled, and nothing else", 4, table.scanned(Kind.NPC).safe());
        assertEquals(2, table.referenced(Kind.NPC).repurposed);
        assertEquals("whole-index", table.coverage(Kind.NPC));
        assertEquals("referenced-ids-only", table.coverage(Kind.ITEM));
        assertEquals("fixture-910", table.legacyCache());
        assertEquals("fixture-950", table.modernCache());
    }

    @Test
    public void aTableForAnotherRevisionOrFormatIsRejectedWhole() {
        rejects(FIXTURE.replace("\"revision\": 950", "\"revision\": 910"), "revision");
        rejects(FIXTURE.replace("\"revision\": 950", "\"revision\": 947"), "revision");
        rejects(FIXTURE.replace("\"format\": 2", "\"format\": 3"), "format");
        // Format 1 is the pre-restyled shape. A server that trusts 'restyled' must not load a
        // table generated before the bucket existed: every case-only rename in such a table is
        // filed under 'renamed', so it would silently keep refusing what the owner admitted.
        rejects(FIXTURE.replace("\"format\": 2", "\"format\": 1"), "format");
    }

    @Test
    public void aTableThatChangedTheSafePolicyIsRejectedWhole() {
        // The trusted set is pinned exactly, in order. Widening it, narrowing it, or merely
        // reordering it all reject the table: adding a verdict must be a deliberate edit to
        // this loader, never something a regenerated resource can do on its own.
        String declared = "[\"same\",\"restyled\"]";
        rejects(FIXTURE.replace(declared, "[\"same\",\"restyled\",\"renamed\"]"), "verdicts");
        rejects(FIXTURE.replace(declared, "[\"same\"]"), "verdicts");
        rejects(FIXTURE.replace(declared, "[\"restyled\",\"same\"]"), "order");
        rejects(FIXTURE.replace(declared, "[\"same\",\"renamed\"]"), "order");
    }

    @Test
    public void aMissingKindRejectsTheWholeTable() {
        rejects(FIXTURE.replace("\"object\"", "\"objects\""), "object");
    }

    @Test
    public void anIdClassifiedTwiceRejectsTheWholeTable() {
        // 42 is repurposed; also claiming it as "same" would make the answer depend on
        // lookup order, so the table is refused rather than resolved.
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"0-1,42,494\""), "twice");
        // With 'restyled' safe, an id in both it and a refused bucket would make it depend on
        // lookup order whether that id may be spawned at all. Refuse the table instead.
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"0-1,117,494\""), "twice");
        rejects(FIXTURE.replace("\"restyled\": {\"117\"", "\"restyled\": {\"42\""), "twice");
    }

    @Test
    public void malformedOverlappingOrBackwardsRangesRejectTheWholeTable() {
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"0-1,1-494\""), "overlapping");
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"494,0-1\""), "order");
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"10-1\""), "Backwards");
        rejects(FIXTURE.replace("\"same\": \"0-1,494\"", "\"same\": \"0-1,banana\""), "Malformed");
    }

    private static void rejects(String json, String expectedInMessage) {
        try {
            Native950IdValidity.load(new StringReader(json));
            fail("Expected the table to be rejected for: " + expectedInMessage);
        } catch (RuntimeException expected) {
            String message = expected.getMessage() == null ? expected.toString() : expected.getMessage();
            assertTrue(message, message.contains(expectedInMessage));
        }
    }

    /**
     * The shipped table, parsed. Everything else here runs on the fixture; this one test
     * exists so a regenerated or truncated resource cannot slip through, and so the two ids
     * the milestone names by hand are known to be refused by the file we actually ship.
     */
    @Test
    public void theCheckedInTableParsesAndRefusesTheKnownDriftedIds() throws Exception {
        InputStream in = Native950IdValidity.class.getResourceAsStream(Native950IdValidity.RESOURCE);
        assertNotNull("resources/native950/id-validity-950.json must be on the classpath", in);
        Native950IdValidity table;
        try {
            table = Native950IdValidity.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } finally {
            in.close();
        }
        assertEquals(950, Native950IdValidity.REVISION);
        assertEquals("/native950/id-validity-950.json", Native950IdValidity.RESOURCE);
        assertEquals("Customs officer 380 changed option 1 after 947; the old table admitted it",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 380));
        assertTrue(table.reason(Kind.NPC, 380), table.reason(Kind.NPC, 380).contains("Travel (Port Sarim)"));
        assertEquals("whole-index", table.coverage(Kind.NPC));
        assertTrue("the NPC scan must cover both whole indexes", table.scanned(Kind.NPC).total > 20000);
        // 494's fifth option moved too (950 gained "Load Last Preset from" where 910 is empty),
        // but native option 5 has no 910 branch at all - Native950ActionRouter.legacyNpcOption
        // refuses it - so slot 5 cannot dispatch anything and is not compared.
        assertTrue("Banker 494 is the anchor NPC every smoke spawns", table.isSafe(Kind.NPC, 494));
        assertFalse("NPC 42 is a Sheep in 910 and Bill in 950", table.isSafe(Kind.NPC, 42));
        assertEquals(Verdict.REPURPOSED, table.verdict(Kind.NPC, 42));
        // Name-equal, size-equal ids whose FORWARDED option slots moved. Each of these would
        // otherwise spawn and hand a click to a 910 branch that means something else.
        assertEquals("3021 Tool leprechaun: 910 slot 4 Teleport, 950 slot 4 Note produce",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 3021));
        assertTrue(table.reason(Kind.NPC, 3021), table.reason(Kind.NPC, 3021).contains("menu option 4"));
        assertEquals("5917 Stray dog: 950 fills slot 1 and 3, 910 fills only slot 5",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 5917));
        assertEquals("456 Father Aereck: 950 slot 3 View Gravestones, 910 slot 3 empty",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 456));
        assertEquals("2333 Vasquen: Talk-to and Pay swapped slots",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 2333));
        // The owner's case-folding decision, in the file we actually ship. These two ids carry
        // 49 spawn rows between them and were refused as 'renamed' before 2026-09-07.
        assertEquals("117 Hill Giant -> Hill giant", Verdict.RESTYLED, table.verdict(Kind.NPC, 117));
        assertTrue(table.isSafe(Kind.NPC, 117));
        assertEquals("1265 Rock Crab -> Rock crab", Verdict.RESTYLED, table.verdict(Kind.NPC, 1265));
        assertTrue(table.isSafe(Kind.NPC, 1265));
        // And the one a fold on names alone would have got wrong: 1327's name differs only in
        // case and its footprint doubled, so the size check that runs for every restyled id
        // sends it to repurposed. This is why the fold was not applied to the name by itself.
        assertEquals("1327 Bear Cub -> Bear cub, size 1 -> 2",
                Verdict.REPURPOSED, table.verdict(Kind.NPC, 1327));
        assertFalse(table.isSafe(Kind.NPC, 1327));
        assertTrue(table.reason(Kind.NPC, 1327), table.reason(Kind.NPC, 1327).contains("size changed 1 -> 2"));
        assertTrue("a restyled id must still name both spellings",
                table.reason(Kind.NPC, 117).contains("Hill Giant"));
        assertFalse("item 6 Cannon base kept its name and lost Set-up", table.isSafe(Kind.ITEM, 6));
        assertFalse("item 1079 keeps its name and loses its equipment slot", table.isSafe(Kind.ITEM, 1079));
        assertEquals(Verdict.REPURPOSED, table.verdict(Kind.ITEM, 1079));
        assertFalse(table.isSafe(Kind.ITEM, 1333));
    }
}
