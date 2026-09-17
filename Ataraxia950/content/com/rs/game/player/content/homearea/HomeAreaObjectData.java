package com.rs.game.player.content.homearea;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Xenthium.
 */
// TODO: Change name of precious/ore containers to the proper name, Edit Fountain options, Edit Altar options
@AllArgsConstructor
public enum HomeAreaObjectData {
    // Diamond+ objects
    BANITE_ROCK(500, "Collect protean bars from these rocks."),
    DREAM_TREE(500, "Cut Protean logs from this magical tree."),
    PORTAL(0, "Anyone home?"),
    PORTABLE_FORGE(500, ""),
    PORTABLE_WELL(500, ""),
    PORTABLE_RANGE(500, ""),
    PORTABLE_BRAZIER(500, ""),
    PORTABLE_FLETCHER(500, ""),
    PORTABLE_CRAFTER(500, ""),
    SCRIMSHAW_CRAFTER(500, ""),
    DIVINE_SIMULACRUM_II(500, ""),
    DIVINE_SIMULACRUM_I(500, ""),
    RUNECRAFTING_ALTAR(500, "Runecraft soul runes here."),
    GEM_DISPLAY_CASE(500, "A gem stall, but better."),
    FISHING_SPOT1(500, ""), // TODO: Great white fishing node @ diamond zone has same object name as mz fishing node
    RESOURCE_CHEST(500, "An upgraded version of the Supply table."),
    CRYSTAL_GLASS_CONTAINER(500, "Collect crystal glass from this."),
    ROBUST_GLASS_CONTAINER(250, "Collect robust glass from this."),
    DELICATE_GEM_STALL(500, ""),
    // Platinum+ objects
    HARP(250, ""),
    CORRUPTED_SEREN_STONE(250, ""),
    CRYSTAL_TREE_SHARD(250, "Gives xp."),
    ROBUST_GLASS_MACHINE(250, "Used for creating potion flasks."),
    SEREN_STONE(250, "Mine corrupted ore from these."),
    DIVINE_BOX_TRAP(250, ""),
    DIVINE_DEADFALL_TRAP(250, ""),
    DIVINE_HERB_PATCH_I(250, ""),
    DIVINE_HERB_PATCH_II(250, ""),
    DIVINE_HERB_PATCH_III(250, ""),
    DIVINE_COAL_ROCK(250, ""),
    DIVINE_ADAMANTITE_ROCK(250, ""),
    DIVINE_MITHRIL_ROCK(250, ""),
    DIVINE_RUNITE_ROCK(250, ""),
    DIVINE_ROCKTAIL_BUBBLE(250, ""),
    DIVINE_SHARK_BUBBLE(250, ""),
    DIVINE_CAVEFISH_BUBBLE(250, ""),
    DIVINE_YEW_TREE(250, ""),
    DIVINE_MAGIC_TREE(250, ""),
    YUBIUSK_PORTAL(250, "Contains a wide range of instanced Bosses & Slayer monsters.") {
        @Override
        public String toString() {
            return "Yu'biusk portal";
        }
    },

    // Bronze+ objects
    DRAMEN_TREE(20, "Cut almost every log from just one tree!"),
    FURNACE(0, "Smelt ore here; also hot."),
    GEM_STALL(20, ""),
    ROCKTAIL_SHOAL(20, "Catch Rocktail from here."),
    CAVEFISH_SHOAL(20, "Catch Cavefish from here."),
    FISHING_SPOT(20, ""),
    COOKING_RANGE(0, "Cook stuff here. Likely hot too."),
    SUPPLY_TABLE(20, "Provides overload effect & restores special attack."),
    BLURITE_ROCK(20, ""),
    PRIFDDINAS_GEM_ROCK(20, ""),
    OBELISK_OF_AIR(20, ""),
    OBELISK_OF_FIRE(20, ""),
    OBELISK_OF_WATER(20, ""),
    OBELISK_OF_EARTH(20, ""),
    SPINNING_WHEEL(0, ""),
    DEATHS_PORTAL(0, ""),
    DUNGEONEERING_PORTAL(0, "Quick access to Dungeoneering."),
    PORT_PORTAL(0, "Quick access to ports."),
    ALTAR(0, "Use bones on me or just recharge your Prayer points."),
    FIRE(0, "Hot."),
    CHEST(0, "Maybe I'll get lucky? Opens with crystal keys."),
    BANK_CHEST(0, "I'll store your items for you senpai. OwO"),
    ANVIL(0, "Used to smash metal with metal on metal."),
    LAVA_CRATER(0, "I should try chucking some logs in this."),
    NULL(0, null) {
        @Override
        public String toString() {
            return "null"; // Hides the examine from map objects with null object names.
        }
    },
    ;

    @Getter
    public int amountRequiredToInteract; // The amount you need to have donated to interact with the object.
    @Getter
    public String description;

    public String getObjectName() {
        return StringUtils.capitalize(this.toString().toLowerCase().replaceAll("_", " "));
    }
}
