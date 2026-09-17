package modern947;

import com.google.gson.JsonObject;
import com.rs.tools.modern.IdValidityProbe;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The classification rule itself, driven directly with two hand-written definitions.
 *
 * <p>{@code Native950IdValidityTest} covers the loader - what a table already written down
 * means. This covers the decision that produces it, which is where the owner's 2026-09-07
 * case-folding ruling actually lives: 947 moved definition names to sentence case, so "Hill
 * Giant" became "Hill giant" and 89 spawn rows were refused as {@code renamed} for a
 * difference in letter case alone.
 *
 * <p>The rule admits that difference and nothing else. Every field check that an
 * identically-named id must pass is run for a case-only rename too, so the tests here come
 * in pairs: the case change alone is {@code restyled}, and the same case change with any
 * compared field moved is still {@code repurposed}. Cache free - both sides of every
 * comparison are built here.
 */
public class Native950IdValidityRuleTest {

    /** One NPC as the probe's dump records it: name, size, and five menu option slots. */
    private static JsonObject npc(String name, int size, String options) {
        JsonObject o = new JsonObject();
        o.addProperty("n", name);
        o.addProperty("sz", size);
        o.addProperty("mv", 0);
        o.addProperty("lv", 1);
        o.addProperty("op", options);
        return o;
    }

    /** One item as the probe's dump records it: name, equipment slot and type, two option arrays. */
    private static JsonObject item(String name, int equipSlot, int equipType, String inventoryOptions) {
        JsonObject o = new JsonObject();
        o.addProperty("n", name);
        o.addProperty("es", equipSlot);
        o.addProperty("et", equipType);
        o.addProperty("io", inventoryOptions);
        o.addProperty("go", "|||Take|");
        return o;
    }

    private static String verdictOf(String kind, JsonObject before, JsonObject after) {
        return IdValidityProbe.verdict(kind, before, after)[0];
    }

    private static String reasonOf(String kind, JsonObject before, JsonObject after) {
        return IdValidityProbe.verdict(kind, before, after)[1];
    }

    @Test
    public void anIdenticalDefinitionIsStillSame() {
        JsonObject before = npc("Hill giant", 2, "Attack||Examine||");
        assertEquals(IdValidityProbe.SAME, verdictOf(IdValidityProbe.NPC, before, npc("Hill giant", 2, "Attack||Examine||")));
    }

    @Test
    public void aNameThatOnlyChangedCaseIsRestyled() {
        // NPC 117, and 35 spawn rows.
        JsonObject before = npc("Hill Giant", 2, "Attack||Examine||");
        JsonObject after = npc("Hill giant", 2, "Attack||Examine||");
        assertEquals(IdValidityProbe.RESTYLED, verdictOf(IdValidityProbe.NPC, before, after));
        String reason = reasonOf(IdValidityProbe.NPC, before, after);
        assertTrue(reason, reason.contains("Hill Giant") && reason.contains("Hill giant"));
        assertTrue("the reason must say why this one was admitted", reason.contains("case only"));
    }

    @Test
    public void aCaseOnlyRenameWhoseSizeMovedIsStillRepurposed() {
        // NPC 1327, and the reason the fold is not applied to the name by itself: this is a
        // real case-only rename in the shipped table whose footprint also doubled, which moves
        // the entity's collision and its wire size. Folding on names alone would spawn it.
        JsonObject before = npc("Bear Cub", 1, "Attack||Examine||");
        JsonObject after = npc("Bear cub", 2, "Attack||Examine||");
        assertEquals(IdValidityProbe.REPURPOSED, verdictOf(IdValidityProbe.NPC, before, after));
        assertTrue(reasonOf(IdValidityProbe.NPC, before, after).contains("size changed 1 -> 2"));
    }

    @Test
    public void aCaseOnlyRenameWhoseDispatchedOptionMovedIsStillRepurposed() {
        // An option index is what a click is dispatched by. Slots 1-4 are forwarded to the 910
        // handler, so a moved slot there means the menu and the branch disagree.
        JsonObject before = npc("Sand Snake", 1, "Attack||Talk-to||");
        JsonObject after = npc("Sand snake", 1, "Attack||Pay||");
        assertEquals(IdValidityProbe.REPURPOSED, verdictOf(IdValidityProbe.NPC, before, after));
        assertTrue(reasonOf(IdValidityProbe.NPC, before, after).contains("menu option 3"));
    }

    @Test
    public void aCaseOnlyRenameWhoseFifthOptionMovedIsStillRestyled() {
        // Slot 5 has no 910 branch at all - Native950ActionRouter.legacyNpcOption refuses it -
        // so it is compared by nobody, for a restyled id exactly as for a same one.
        JsonObject before = npc("Hill Giant", 2, "Attack||Examine||");
        JsonObject after = npc("Hill giant", 2, "Attack||Examine||Load Last Preset from");
        assertEquals(IdValidityProbe.RESTYLED, verdictOf(IdValidityProbe.NPC, before, after));
    }

    @Test
    public void aCaseOnlyRenamedItemThatLostItsEquipmentSlotIsStillRepurposed() {
        JsonObject before = item("Rune Platelegs", 7, 3, "|Wear|||Drop");
        JsonObject after = item("Rune platelegs", -1, 3, "|Wear|||Drop");
        assertEquals(IdValidityProbe.REPURPOSED, verdictOf(IdValidityProbe.ITEM, before, after));
        assertTrue(reasonOf(IdValidityProbe.ITEM, before, after).contains("equipSlot changed 7 -> -1"));
    }

    @Test
    public void aCaseOnlyRenamedItemThatKeptEveryFieldIsRestyled() {
        JsonObject before = item("Rune Platelegs", 7, 3, "|Wear|||Drop");
        JsonObject after = item("Rune platelegs", 7, 3, "|Wear|||Drop");
        assertEquals(IdValidityProbe.RESTYLED, verdictOf(IdValidityProbe.ITEM, before, after));
    }

    @Test
    public void aGenuineRenameIsNotAdmittedByTheCaseRule() {
        // "Rockslug" -> "Rock slug" and "Cow calf" -> "Calf" read as the same creature to a
        // human and are still refused: the rule is case, not similarity. Only the advisory
        // renamed/repurposed split uses the similarity heuristic, and both are refused.
        assertEquals(IdValidityProbe.RENAMED, verdictOf(IdValidityProbe.NPC,
                npc("Rockslug", 1, "Attack||||"), npc("Rock slug", 1, "Attack||||")));
        assertEquals(IdValidityProbe.RENAMED, verdictOf(IdValidityProbe.NPC,
                npc("Cow calf", 1, "Attack||||"), npc("Calf", 1, "Attack||||")));
        assertEquals(IdValidityProbe.REPURPOSED, verdictOf(IdValidityProbe.NPC,
                npc("Sheep", 1, "Attack||||"), npc("Bill", 1, "Talk-to||||")));
    }

    @Test
    public void caseFoldingDoesNotReachPunctuationOrWhitespace() {
        // A dropped hyphen or a moved word break is a different string, not a different case,
        // and stays refused. This is the boundary the owner's decision was scoped to.
        assertEquals(IdValidityProbe.RENAMED, verdictOf(IdValidityProbe.NPC,
                npc("Al-Kharid warrior", 1, "Attack||||"), npc("Al Kharid warrior", 1, "Attack||||")));
        assertEquals(IdValidityProbe.RENAMED, verdictOf(IdValidityProbe.NPC,
                npc("Hill giant ", 1, "Attack||||"), npc("Hill giant", 1, "Attack||||")));
    }

    @Test
    public void anEmptyNameIsNeverRestyled() {
        // Two empty names are equal, so they never reach the case branch; the dump rejects
        // placeholder and nameless definitions before this point, and this pins that a blank
        // pair cannot become a safe verdict by way of equalsIgnoreCase.
        assertEquals(IdValidityProbe.SAME, verdictOf(IdValidityProbe.NPC,
                npc("", 1, "||||"), npc("", 1, "||||")));
    }

    @Test
    public void aMissingOrUnverifiableSideIsUnchangedByTheCaseRule() {
        assertEquals(IdValidityProbe.MISSING, verdictOf(IdValidityProbe.NPC, npc("Hill Giant", 2, "||||"), null));
        assertEquals(IdValidityProbe.UNVERIFIABLE, verdictOf(IdValidityProbe.NPC, null, npc("Hill giant", 2, "||||")));
    }

    @Test
    public void theTrustedVerdictsAreExactlyTwoAndInOrder() {
        // The one place the policy is written down as data. IdValidityProbe stamps it into
        // every table it writes and Native950IdValidity refuses any table that declares
        // something else, so these two lists must agree or nothing loads.
        assertEquals("same", IdValidityProbe.SAME);
        assertEquals("restyled", IdValidityProbe.RESTYLED);
        String[] trusted = com.rs.game.player.client.Native950IdValidity.safeVerdicts();
        assertEquals(2, trusted.length);
        assertEquals(IdValidityProbe.SAME, trusted[0]);
        assertEquals(IdValidityProbe.RESTYLED, trusted[1]);
        assertEquals("the trusted list must equal IdValidityProbe.verdicts()'s safe prefix",
                IdValidityProbe.verdicts().subList(0, 2), java.util.Arrays.asList(trusted));
    }
}
