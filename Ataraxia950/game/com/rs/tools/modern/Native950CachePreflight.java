package com.rs.tools.modern;

import com.rs.cache.Cache;
import com.rs.game.player.client.Native950Settings;
import com.rs.game.player.client.Native950Dialogues;
import com.rs.game.player.client.Native950QuantityInput;
import com.rs.game.player.client.Native950WorldMap;
import com.rs.game.player.client.ui.Native950Bindings;
import java.nio.file.Paths;

/** Checks lazy UI and combat cache dependencies before a player can encounter them in a live session. */
public final class Native950CachePreflight {
    private Native950CachePreflight() { }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950CachePreflight <950-flat-cache>");
        if ("false".equalsIgnoreCase(System.getProperty("ataraxia.native.verifyCache")))
            throw new IllegalStateException("Preflight requires cache verification enabled");
        Cache.initFlatReadOnly(Paths.get(args[0]).toAbsolutePath().normalize());
        if (Native950Bindings.tryLoad() == null) throw new IllegalStateException("950 UI bindings unavailable");
        com.rs.game.player.client.Native950IdValidity.get();
        Native950Settings.verify();
        com.rs.game.player.client.Native950ExitUi.verify();
        com.rs.game.player.client.Native950SkillGuide.verify();
        com.rs.game.player.client.Native950Navigation.verify();
        com.rs.game.player.client.Native950Quests.verifyCacheBindings();
        com.rs.game.player.client.Native950Beasts.verifyCacheBindings();
        com.rs.game.player.client.Native950Minigames.verifyCacheBindings();
        Native950Dialogues.verify();
        Native950QuantityInput.verify();
        Native950WorldMap.verify();
        com.rs.game.player.client.Native950Toolbelt.verifyCacheBindings();
        com.rs.game.player.client.Native950ProductionMenu.verifyCacheBindings();
        com.rs.game.player.client.Native950ForgeUi.verifyCacheBindings();
        com.rs.game.player.client.Native950Lodestones.verify();
        if (!com.rs.game.player.client.Native950LodestoneTeleport.verifyAssets())
            throw new IllegalStateException("950 lodestone teleport effects unavailable");
        com.rs.game.player.client.Native950Hitbars.verifyCache();
        if (!com.rs.game.player.client.Native950Hits.verifyCache()
                || !com.rs.game.player.client.Native950CombatAnimations.verifyCache()
                || !com.rs.game.player.client.Native950MeleeEquipment.verifyCache())
            throw new IllegalStateException("950 ordinary melee display bindings unavailable");
        com.rs.game.player.client.Native950Runecrafting.verifyCacheBindings();
        com.rs.game.player.client.Native950Divination.verifyCacheBindings();
        if (!com.rs.game.player.client.Native950Thieving.verifyCacheBindings()
                || !com.rs.game.player.client.Native950Hunter.verifyCacheBindings())
            throw new IllegalStateException("950 Thieving/Hunter assets unavailable");
        com.rs.game.player.client.Native950Crafting.verifyCacheBindings();
        com.rs.game.player.client.Native950Summoning.verifyCacheBindings();
        com.rs.game.player.client.Native950Agility.verifyCacheBindings();
        com.rs.game.player.client.Native950Smithing.verifyCacheBindings();
        com.rs.game.player.client.Native950Slayer.verifyCacheBindings();
        com.rs.game.player.client.Native950Farming.verifyCacheBindings();
        com.rs.game.player.client.Native950Construction.verifyCacheBindings();
        com.rs.game.player.client.Native950Invention.verifyCacheBindings();
        com.rs.game.player.client.Native950Archaeology.verifyCacheBindings();
        com.rs.game.player.client.Native950Dungeoneering.verifyCacheBindings();
        com.rs.game.player.client.Native950NpcAttackAnimations.verifyCacheBindings();
        com.rs.game.player.client.Native950NpcDrawnWeapons.verifyCacheBindings();
        System.out.println("PASS 950 cache preflight: 950 identity table, UI table, toolbelt, native Make-X and Smithing/Smelting, settings (including pending controls), dialogue, quantity input, world map, lodestone network and teleport effects, ordinary HP bars, melee hitmarks, animations, starter equipment, Runecrafting, Divination, Thieving, Hunter, Crafting, Summoning Smithing, Slayer masters, three Agility courses and the exact-950 NPC weapon-swing preview tables");
    }
}
