package com.rs.game.player.client;
import com.rs.game.player.actions.Fishing.Fish;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.Cooking.Cookables;
import org.junit.Test;
import static org.junit.Assert.*;
public final class Native950FishingCookingTest {
    @Test public void ordinaryCatchCurveKeepsOriginalLevelEndpointsAndCaps(){
        assertEquals(48,Native950Fishing.catchRoll(1,Fish.SHRIMP));
        assertEquals(255,Native950Fishing.catchRoll(99,Fish.SHRIMP));
        assertEquals(255,Native950Fishing.catchRoll(120,Fish.SHRIMP));
        assertEquals(32,Native950Fishing.catchRoll(1,Fish.TROUT));
        assertEquals(192,Native950Fishing.catchRoll(99,Fish.TROUT));
        assertTrue(Native950Fishing.catchRoll(50,Fish.SHARK)>Native950Fishing.catchRoll(1,Fish.SHARK));
    }
    @Test public void originalFishingCadenceRemainsFourOrFiveTicks(){
        assertEquals(4,Native950Fishing.delay(FishingSpots.NET));
        assertEquals(4,Native950Fishing.delay(FishingSpots.LURE));
        assertEquals(5,Native950Fishing.delay(FishingSpots.HARPOON));
        assertEquals(5,Native950Fishing.delay(FishingSpots.ROCKTAIL_SHOAL));
    }
    @Test public void burnCurveDistinguishesFireAndRangeAndStopsAtOriginalThreshold(){
        assertEquals(55,Native950Cooking.burnChance(Cookables.RAW_SHRIMP,1,true),0.001);
        assertEquals(50,Native950Cooking.burnChance(Cookables.RAW_SHRIMP,1,false),0.001);
        assertEquals(0,Native950Cooking.burnChance(Cookables.RAW_SHRIMP,34,true),0.001);
        assertEquals(0,Native950Cooking.burnChance(Cookables.RAW_LAVA_EEL,53,true),0.001);
        assertTrue(Native950Cooking.burnChance(Cookables.RAW_SHARK,87,true)<Native950Cooking.burnChance(Cookables.RAW_SHARK,80,true));
    }
    @Test public void cookingCadenceUsesOriginalAboveRequirementRule(){
        assertEquals(3,Native950Cooking.delay(Cookables.RAW_SHRIMP,1));
        assertEquals(2,Native950Cooking.delay(Cookables.RAW_SHRIMP,5));
        assertEquals(1,Native950Cooking.delay(Cookables.RAW_SHRIMP,9));
        assertEquals(1,Native950Cooking.delay(Cookables.RAW_SHRIMP,120));
    }
    @Test public void exactCookingMenuNamesCannotTreatLightingOrBonfireAsCooking(){
        assertTrue(Native950Cooking.isCookOption("Cook"));assertTrue(Native950Cooking.isCookOption("Cook-at"));
        assertFalse(Native950Cooking.isCookOption("Light"));assertFalse(Native950Cooking.isCookOption("Use"));assertFalse(Native950Cooking.isCookOption(null));
    }
}
