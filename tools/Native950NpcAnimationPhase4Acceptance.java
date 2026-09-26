package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

/** Fresh-JVM, real-cache coverage check. Does not spawn actors or write player state. */
public final class Native950NpcAnimationPhase4Acceptance {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected flat cache path");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser.init();
        com.rs.utils.data.parsers.npcs.NPCStatsDataParser.init();
        Native950NpcAttackAnimations.verifyCacheBindings();
        Native950NpcDrawnWeapons.verifyCacheBindings();
        java.lang.reflect.Method verify = Native950DeveloperConsole.class.getDeclaredMethod("verify");
        verify.setAccessible(true);
        verify.invoke(null);
        Set<Integer> ids = new LinkedHashSet<Integer>(Native950NpcAttackAnimations.rows().keySet());
        ids.addAll(Native950NpcDrawnWeapons.rows().keySet());
        int gained = 0, retained = 0, refused = 0;
        System.out.println("npc\tname\tbefore\tafter\tresult\tevidence");
        for (int id : ids) {
            Native950NpcCombatProfile before = Native950NpcCombatCatalog.inspectRunningCache(id).profile;
            int oldAttack = before == null ? -1 : before.attackAnim;
            Native950DeveloperPreview after = Native950DeveloperPreview.resolve(id);
            String result;
            if (oldAttack >= 0) {
                require(after.attack == oldAttack, "Authored attack overridden: " + id);
                retained++; result = "RETAINED";
            } else if (after.attack >= 0) {
                require(after.attack == Native950NpcAttackAnimations.attackFor(id), "Wrong fallback: " + id);
                gained++; result = "GAINED";
            } else { refused++; result = "REFUSED"; }
            Native950NpcCombatProfile still = Native950NpcCombatCatalog.inspectRunningCache(id).profile;
            require((still == null ? -1 : still.attackAnim) == oldAttack, "Live combat changed: " + id);
            Native950NpcDrawnWeapons.Row row = Native950NpcDrawnWeapons.row(id);
            System.out.println(id + "\t" + NPCDefinitions.getNPCDefinitions(id).name + "\t" + oldAttack
                    + "\t" + after.attack + "\t" + result + "\t" + (row == null ? "declared-family" : row.evidence));
        }
        for (int id : new int[]{1, 172, 6260, 21502, 22472, 22478}) {
            require(!ids.contains(id), "Control must not be in imported tables: " + id);
            Native950NpcCombatProfile profile = Native950NpcCombatCatalog.inspectRunningCache(id).profile;
            int before = profile == null ? -1 : profile.attackAnim;
            Native950DeveloperPreview preview = Native950DeveloperPreview.resolve(id);
            require(preview.attack == before, "Uncovered control changed: " + id);
            if (id == 6260) require(before >= 0, "Graardor baseline missing: load production NPC data");
            System.out.println("CONTROL " + id + " " + NPCDefinitions.getNPCDefinitions(id).name
                    + " idle=" + preview.idle + " attack=" + preview.attack + " zoom=" + preview.zoom);
        }
        System.out.println("PASS rows=" + ids.size() + " gained=" + gained + " retained=" + retained
                + " refused=" + refused + " drawnBlocksAccepted=" + Native950NpcDrawnWeapons.acceptedBlockCount()
                + "; cache pins and Developer Console verification passed; no visual acceptance claimed");
    }
    private static void require(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
    }
}
