package com.rs.game.player.client;

import com.rs.game.player.actions.mining.defs.RockDefinitions;
import org.junit.Test;
import static org.junit.Assert.*;

/** Shared original skill policy and cache-name admission for Native 950 Mining. */
public class Native950MiningTest {

    @Test
    public void originalOrdinaryRockNamesSelectTheirExistingDefinitions() {
        String[] names = {
            "Clay rock", "Soft clay rock", "Copper rock", "Tin rock", "Iron rock", "Silver rock",
            "Coal rock", "Sandstone rock", "Gold rock", "Mithril rock", "Adamantite rock",
            "Luminite rock", "Granite rock", "Runite rock", "Orichalcite rock", "Drakolith rock",
            "Phasmatite rock", "Necrite rock", "Banite rock", "Light animica rock", "Dark animica rock",
            "Gem rock", "Common gem rock", "Uncommon gem rock", "Precious gem rock", "Rare gem rock"
        };
        RockDefinitions[] types = {
            RockDefinitions.CLAY, RockDefinitions.SOFT_CLAY, RockDefinitions.Copper_Ore, RockDefinitions.Tin_Ore,
            RockDefinitions.Iron_Ore, RockDefinitions.Silver_Ore, RockDefinitions.Coal_Ore, RockDefinitions.Sandstone_Ore,
            RockDefinitions.Gold_Ore, RockDefinitions.Mithril_Ore, RockDefinitions.Adamant_Ore,
            RockDefinitions.LUMINITE_ORE, RockDefinitions.Granite_Ore, RockDefinitions.Runite_Ore,
            RockDefinitions.ORICHALCITE_ORE, RockDefinitions.DRAKOLITH_ORE, RockDefinitions.PHASMATITE_ORE,
            RockDefinitions.NECRITE_ORE, RockDefinitions.BANE_ORE, RockDefinitions.LIGHT_ORE, RockDefinitions.DARK_ORE,
            RockDefinitions.GEM_ROCK, RockDefinitions.GEM_ROCK, RockDefinitions.GEM_ROCK, RockDefinitions.GEM_ROCK, RockDefinitions.GEM_ROCK
        };
        for (int i = 0; i < names.length; i++) {
            assertSame(names[i], types[i],
                    Native950Mining.definition(names[i], new String[]{"Mine", null, null, "Prospect", null}));
        }
    }

    @Test
    public void aNameWithoutACacheMineOperationCannotRunTheAction() {
        assertNull(Native950Mining.definition("Copper rock", new String[]{"Prospect"}));
        assertNull(Native950Mining.definition("Iron rock", null));
        assertNull(Native950Mining.definition(null, new String[]{"Mine"}));
    }

    @Test
    public void repurposedObjectsAndQuestResourcesDoNotFallThroughToOrdinaryOres() {
        for (String name : new String[]{
            "Punishment rock", "Feldip swamp rock", "Rockslide", "Convenient rock", "Mossy rock", "Rock", "Tree"
        }) {
            assertNull(name, Native950Mining.definition(name, new String[]{"Mine"}));
        }
    }

    @Test
    public void rockMenuMayMoveSlotsWithoutInventingAnOperation() {
        assertSame(RockDefinitions.Copper_Ore,
                Native950Mining.definition("COPPER ROCK", new String[]{null, null, "Mine"}));
        assertNull(Native950Mining.definition("Copper rock", new String[]{"Chop down"}));
    }

    @Test
    public void pickaxeProgressionIsOrderedAndCoversTiers() {
        Native950Mining.PickaxeDef[] pickaxes = Native950Mining.PICKAXES;
        assertEquals(13, pickaxes.length);
        for (int i = 0; i < pickaxes.length - 1; i++) {
            assertTrue("Pickaxes must be sorted descending by level/tier: " + pickaxes[i].itemId,
                    pickaxes[i].level >= pickaxes[i + 1].level);
            assertTrue(pickaxes[i].tier >= pickaxes[i + 1].tier);
        }
        assertEquals(44834, pickaxes[0].itemId); // Earth & Song
        assertEquals(90, pickaxes[0].level);
        assertEquals(90, pickaxes[0].tier);
        assertEquals(32618, pickaxes[0].animationId);

        assertEquals(1265, pickaxes[pickaxes.length - 1].itemId); // Bronze
        assertEquals(1, pickaxes[pickaxes.length - 1].level);
        assertEquals(1, pickaxes[pickaxes.length - 1].tier);
        assertEquals(32540, pickaxes[pickaxes.length - 1].animationId);
    }

    @Test
    public void ordinaryRockDefinitionsRetainOriginalRequirements() {
        assertEquals(1, RockDefinitions.Copper_Ore.getLevel());
        assertEquals(436, RockDefinitions.Copper_Ore.getOreId());
        assertEquals(10, RockDefinitions.Iron_Ore.getLevel());
        assertEquals(440, RockDefinitions.Iron_Ore.getOreId());
        assertEquals(30, RockDefinitions.Mithril_Ore.getLevel());
        assertEquals(447, RockDefinitions.Mithril_Ore.getOreId());
        assertEquals(40, RockDefinitions.Adamant_Ore.getLevel());
        assertEquals(449, RockDefinitions.Adamant_Ore.getOreId());
        assertEquals(50, RockDefinitions.Runite_Ore.getLevel());
        assertEquals(451, RockDefinitions.Runite_Ore.getOreId());
        assertEquals(80, RockDefinitions.BANE_ORE.getLevel());
        assertEquals(21778, RockDefinitions.BANE_ORE.getOreId());
        assertEquals(90, RockDefinitions.LIGHT_ORE.getLevel());
        assertEquals(44830, RockDefinitions.LIGHT_ORE.getOreId());
        assertEquals(90, RockDefinitions.DARK_ORE.getLevel());
        assertEquals(44832, RockDefinitions.DARK_ORE.getOreId());
    }
    @Test
    public void staminaUnlocksAt15AndFollowsMiningAndVisibleAgilityMilestones() {
        assertEquals(0, Native950Mining.staminaCapacity(14,99));
        int[] levels={15,18,19,25,26,32,33,45,46,56,57,66,67,70,71,87,88,99};
        int[] capacity={31,31,41,41,51,51,61,61,71,71,81,81,91,91,101,101,111,111};
        for(int i=0;i<levels.length;i++)assertEquals("Mining level "+levels[i],capacity[i],Native950Mining.staminaCapacity(levels[i],1));
        assertEquals(209,Native950Mining.staminaCapacity(99,99));
        assertEquals(220,Native950Mining.staminaCapacity(99,110));
    }
    @Test
    public void depletedStaminaHasRealPenaltyOnlyAfterUnlockAndUiUsesPercentage() {
        assertEquals(1.0,Native950Mining.staminaMultiplier(14,0,0),0);
        assertEquals(1.0,Native950Mining.staminaMultiplier(15,31,31),0);
        assertEquals(0.9,Native950Mining.staminaMultiplier(15,21,31),0);
        assertEquals(0.2,Native950Mining.staminaMultiplier(15,0,31),0);
        assertEquals(100,Native950Mining.staminaPercent(209,209));
        assertEquals(52,Native950Mining.staminaPercent(109,209));
        assertEquals(0,Native950Mining.staminaPercent(-1,209));
    }
}
