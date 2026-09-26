package com.rs.game.player.client;

import com.rs.cache.loaders.AnimationDefinitions;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Stage B Phase 4: cacheless test of {@link Native950NpcCombatAnimations#compatibleFamilies}, the
 * legacy-frame-on-animaya render gate adopted from Artaven. Runs without the local cache, unlike
 * {@link Native950NpcAnimationGapCoverageTest}.
 */
public final class Native950NpcCombatAnimationsRenderGateTest {

    @Test public void animayaStandRefusesALegacyFrameCombatSequence() {
        AnimationDefinitions animayaStand = new AnimationDefinitions();
        animayaStand.modernInt26b = 30; // has a modern duration...
        animayaStand.anIntArray2153 = null; // ...and no legacy per-frame array.
        AnimationDefinitions legacyCombat = new AnimationDefinitions();
        legacyCombat.anIntArray2153 = new int[]{5, 5, 5};
        assertFalse(Native950NpcCombatAnimations.compatibleFamilies(animayaStand, legacyCombat));
    }

    @Test public void legacyStandAcceptsALegacyFrameCombatSequence() {
        AnimationDefinitions legacyStand = new AnimationDefinitions();
        legacyStand.anIntArray2153 = new int[]{10, 10};
        AnimationDefinitions legacyCombat = new AnimationDefinitions();
        legacyCombat.anIntArray2153 = new int[]{5, 5, 5};
        assertTrue(Native950NpcCombatAnimations.compatibleFamilies(legacyStand, legacyCombat));
    }

    @Test public void animayaStandAcceptsAnAnimayaCombatSequence() {
        AnimationDefinitions animayaStand = new AnimationDefinitions();
        animayaStand.modernInt26b = 30;
        AnimationDefinitions animayaCombat = new AnimationDefinitions();
        animayaCombat.modernInt26b = 20;
        assertTrue(Native950NpcCombatAnimations.compatibleFamilies(animayaStand, animayaCombat));
    }

    @Test public void absentSequenceOrRenderIdIsTrivialAcceptance() {
        assertTrue(Native950NpcCombatAnimations.compatibleWithRender(-1, 500));
        assertTrue(Native950NpcCombatAnimations.compatibleWithRender(500, -1));
    }
}
