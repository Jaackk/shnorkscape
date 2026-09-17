package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Native settings capture/restore over isolated players and temporary profile fixtures. */
public final class Native950BankPreferencesTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();
    private final List<EmbeddedChannel> channels = new ArrayList<>();

    @After public void cleanup() {
        for (EmbeddedChannel channel : channels) channel.finishAndReleaseAll();
    }

    @Test public void rawXSelectionSurvivesBinderAndSaveReloadWithoutDirtyingItems() throws Exception {
        Player original = player();
        Native950Save base = emptySave();
        Native950Save before = Native950PlayerBinder.capture(original, base, 1234L);
        Map<String, Integer> settings = oldSettings();
        settings.put(Player.SETTING_BANK_WITHDRAW_NOTES, 1);
        settings.put(Player.SETTING_BANK_LAST_X, 37);
        settings.put(Player.SETTING_BANK_QUANTITY_MODE, 11);
        // Keep the unrelated values unchanged, so only bank preferences cause the diff.
        for (String key : oldSettings().keySet()) settings.put(key, before.settings().get(key));
        long packetCalls = packetCalls(original);
        original.applyNativeSettings(settings);
        Native950Save after = Native950PlayerBinder.capture(original, base, 1234L);
        assertEquals(EnumSet.of(Native950Save.Section.SETTINGS), after.changedSections(before));
        assertEquals(packetCalls, packetCalls(original));
        assertArrayEquals(before.bankIds(), after.bankIds());
        assertArrayEquals(before.bankAmounts(), after.bankAmounts());
        assertArrayEquals(before.inventoryIds(), after.inventoryIds());
        assertArrayEquals(before.inventoryAmounts(), after.inventoryAmounts());
        assertEquals(Integer.valueOf(11), after.settings().get(Player.SETTING_BANK_QUANTITY_MODE));
        Native950SaveStore store = new Native950SaveStore(temporary.newFolder("profiles").toPath());
        store.save(after);
        Native950Save loaded = store.load("bankprefs");
        Player restored = player();
        packetCalls = packetCalls(restored);
        Native950PlayerBinder.restore(restored, loaded);
        assertPreferences(restored, true, 37, 11);
        assertEquals(37, restored.getBank().getDefaultInteractionAmount());
        assertEquals(after.settings(), restored.nativeSettingsSnapshot());
        assertEquals(packetCalls, packetCalls(restored));
    }

    @Test public void olderProfilesWithoutBankKeysUseDefaultsAndRetainExistingSettings() {
        Player restored = player();
        restored.getBank().restoreNativePreferences(true, 91, 11);
        Map<String, Integer> old = oldSettings();
        old.put("unknownFutureKey", 77);
        long packetCalls = packetCalls(restored);
        restored.applyNativeSettings(old);
        assertPreferences(restored, false, 1, 1);
        for (Map.Entry<String, Integer> entry : oldSettings().entrySet())
            assertEquals(entry.getValue(), restored.nativeSettingsSnapshot().get(entry.getKey()));
        assertFalse(restored.nativeSettingsSnapshot().containsKey("unknownFutureKey"));
        assertEquals(packetCalls, packetCalls(restored));
        // Schema 1/2 use an empty settings map rather than a null map.
        restored.getBank().restoreNativePreferences(true, 77, 5);
        restored.applyNativeSettings(Collections.<String, Integer>emptyMap());
        assertPreferences(restored, false, 1, 1);
        assertEquals(packetCalls, packetCalls(restored));
    }

    @Test public void eachVerifiedQuantityModeAndMaximumXKeepTheirDistinctMeaning() {
        Player original = player(), restored = player();
        long originalCalls = packetCalls(original), restoredCalls = packetCalls(restored);
        for (int mode : new int[] {1, 5, 10, 11, Integer.MAX_VALUE}) {
            original.getBank().restoreNativePreferences(true, Integer.MAX_VALUE, mode);
            restored.applyNativeSettings(original.nativeSettingsSnapshot());
            assertPreferences(restored, true, Integer.MAX_VALUE, mode);
            assertEquals(mode == 11 ? Integer.MAX_VALUE : mode, restored.getBank().getDefaultInteractionAmount());
        }
        assertEquals(originalCalls, packetCalls(original));
        assertEquals(restoredCalls, packetCalls(restored));
    }

    @Test public void malformedBankValuesFallBackIndependentlyWithoutTouchingItems() {
        Player restored = player();
        Item banked = new Item(995, 123), carried = new Item(995, 456);
        restored.getBank().bankTabs = new Item[][] {{banked}};
        restored.getInventory().items.set(0, carried);
        long packetCalls = packetCalls(restored);
        Map<String, Integer> settings = new LinkedHashMap<>();
        settings.put(Player.SETTING_BANK_WITHDRAW_NOTES, 1);
        settings.put(Player.SETTING_BANK_LAST_X, 25);
        for (int mode : new int[] {Integer.MIN_VALUE, -1, 0, 2, 6, 37}) {
            settings.put(Player.SETTING_BANK_QUANTITY_MODE, mode);
            restored.applyNativeSettings(settings);
            assertPreferences(restored, true, 25, 1);
        }
        settings.put(Player.SETTING_BANK_QUANTITY_MODE, 11);
        for (int count : new int[] {Integer.MIN_VALUE, -1, 0}) {
            settings.put(Player.SETTING_BANK_LAST_X, count);
            restored.applyNativeSettings(settings);
            assertPreferences(restored, true, 1, 11);
        }
        settings.put(Player.SETTING_BANK_LAST_X, 25);
        for (int flag : new int[] {Integer.MIN_VALUE, -1, 0, 2, Integer.MAX_VALUE}) {
            settings.put(Player.SETTING_BANK_WITHDRAW_NOTES, flag);
            restored.applyNativeSettings(settings);
            assertPreferences(restored, false, 25, 11);
        }
        assertSame(banked, restored.getBank().bankTabs[0][0]);
        assertEquals(123, banked.getAmount());
        assertSame(carried, restored.getInventory().items.get(0));
        assertEquals(456, carried.getAmount());
        assertEquals(packetCalls, packetCalls(restored));
    }

    private Player player() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channels.add(channel);
        return Player.createNative950("bankprefs", new WorldTile(3217, 3258, 0), channel);
    }

    private static Native950Save emptySave() {
        int[] ids = new int[28]; Arrays.fill(ids, -1);
        return new Native950Save("bankprefs", 3217, 3258, 0, ids, new int[28],
                new int[] {995, 1511}, new int[] {123, 8});
    }

    private static Map<String, Integer> oldSettings() {
        Map<String, Integer> settings = new LinkedHashMap<>();
        settings.put(Player.SETTING_CHAT_EFFECTS, 1);
        settings.put(Player.SETTING_PROFANITY_FILTER, 0);
        settings.put(Player.SETTING_MOUSE_BUTTONS, 1);
        settings.put(Player.SETTING_ACCEPT_AID, 0);
        return settings;
    }

    private static void assertPreferences(Player player, boolean notes, int lastX, int rawMode) {
        assertEquals(notes, player.getBank().getWithdrawNotes());
        assertEquals(lastX, player.getBank().getLastX());
        assertEquals(rawMode, player.getBank().getNativeDefaultInteractionAmount());
    }

    private static long packetCalls(Player player) {
        Native950PacketDispatcher.Counters counters = ((Native950PacketDispatcher) player.getPackets()).counters();
        return counters.totalSent() + counters.totalDropped() + counters.totalNoops() + counters.totalStrictHits();
    }
}