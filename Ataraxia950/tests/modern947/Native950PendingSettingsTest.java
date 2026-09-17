package com.rs.game.player.client;

import org.junit.Test;
import static org.junit.Assert.*;

/** Positive cache fixtures; accepting a local preference would produce a false rejection notice. */
public class Native950PendingSettingsTest {
    @Test public void onlyTheFourPairedCombatModeCheckboxesRequireServerRecovery() {
        // enum14569 menu25 -> DBrow1306: Full Manual, Revolution, Classic.
        for (int slot : new int[] {10240, 10241, 10242})
            assertTrue(Native950PendingSettings.isPendingCheckbox(1, slot));
        // menu47 -> DBrow1320 repeats the Classic Combat Mode setting.
        assertTrue(Native950PendingSettings.isPendingCheckbox(1, 15872));
    }

    @Test public void accessibilityAndOtherPagesCannotReuseGameplayNotificationSlots() {
        for (int page : new int[] {-1, 0, 2, 3, 4, 5, 6, 7, Integer.MAX_VALUE})
            for (int slot : new int[] {10240, 10241, 10242, 15872})
                assertFalse(Native950PendingSettings.isPendingCheckbox(page, slot));
    }

    @Test public void localPreferencesMalformedSlotsAndAdjacentControlsAreNotRejected() {
        // Category13 System / row3522 index2 is the local Lock zoom checkbox.
        assertFalse(Native950PendingSettings.isPendingCheckbox(1, 0x1c02));
        for (int slot : new int[] {-1, 0, 4095, 4096, 25, 47, 10239, 10243,
                15871, 15873, 0xfaff, 65535, Integer.MIN_VALUE, Integer.MAX_VALUE})
            assertFalse(Native950PendingSettings.isPendingCheckbox(1, slot));
    }
}
