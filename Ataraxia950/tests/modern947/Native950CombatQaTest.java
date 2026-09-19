package com.rs.game.player.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class Native950CombatQaTest {
    @Test public void cleanupRequiresReviewAndAnExplicitDisposableLifecycle() {
        for(String state:new String[]{"ACTIVE","UNREVIEWED","UNRESOLVED_EVIDENCE","BASELINE"})
            assertFalse(Native950CombatQa.mayDelete("UNREVIEWED",true,state));
        assertFalse(Native950CombatQa.mayDelete("ACTIVE",true,"REDUNDANT"));
        assertFalse(Native950CombatQa.mayDelete("UNREVIEWED",false,"REDUNDANT"));
        assertTrue(Native950CombatQa.mayDelete("UNREVIEWED",true,"REDUNDANT"));
        assertTrue(Native950CombatQa.mayDelete("REVIEWED",true,"STALE"));
        assertTrue(Native950CombatQa.mayDelete("REVIEWED",true,"SUPERSEDED"));
    }

    @Test public void collapsedChannelTicksAreDetectedWithoutFlaggingNormalCadence() {
        assertFalse(Native950CombatQa.collapsedTicks("[]"));
        assertFalse(Native950CombatQa.collapsedTicks("[101, 102, 103]"));
        assertTrue(Native950CombatQa.collapsedTicks("[101, 101, 103]"));
    }

    @Test public void staleVisualEvidenceIsDroppedBeforeCapture() {
        assertFalse(Native950CombatQa.captureIsStale(false,1000L,3500L));
        assertTrue(Native950CombatQa.captureIsStale(false,1000L,3501L));
        assertFalse(Native950CombatQa.captureIsStale(true,1000L,8000L));
        assertTrue(Native950CombatQa.captureIsStale(true,1000L,8001L));
    }

    @Test public void combatQaCommandsArePassiveAndRegistered() {
        for(String command:new String[]{";;combatqa",";;combatqa stop",";;combatqa status",";;combatqa reset",";;combatqa cleanup"}) {
            assertTrue(Native950DevelopmentCommands.isCommand(command));
            assertTrue(Native950DevelopmentCommands.preservesGameplay(command));
        }
    }
}
