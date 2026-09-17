package com.rs.game.activites.gim.bank;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonParser;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages the shared GIM bank for a group member.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMBankManager {

    /**
     * The maximum amount of history entries.
     */
    private static final int MAX_HISTORY_ENTRIES = 150;

    /**
     * The cache of banks.
     */
    private static final LoadingCache<String, GIMBank> banks = CacheBuilder.newBuilder().expireAfterAccess(12, TimeUnit.HOURS).
            maximumSize(50).build(new GIMBankLoader());

    /**
     * Request a cached bank. This should only be called from the game thread.
     */
    public static GIMBank requestBank(String groupName) {
        Stopwatch bankRequest = Stopwatch.createStarted();
        try {
            return banks.get(groupName);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        } finally {
            long millis = bankRequest.elapsed().toMillis();
            if (millis >= 250) {
                Logger.getGlobal().warn("Synchronous bank request took {}ms!", millis);
            }
        }
    }

    /**
     * Converts all JSON banks to serializable.
     */
    @Deprecated
    public static void convertBanks() {
        Path mainDir = Paths.get("data", "gim", "banks");
        File[] banks = mainDir.toFile().listFiles();

        for (File bankFile : banks) {
            if (bankFile.getName().endsWith(".json")) {
                try {
                } catch (Exception e) {
                    throw new Error("Could not load bank " + bankFile.getName(), e);
                }
            }
        }
        for (File bankFile : banks) {
            if (bankFile.getName().endsWith(".json")) {
                try {
                    if (bankFile.delete()) {
                        System.out.println("Bank " + bankFile.getName() + " converted to serializable.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the path to the bank.
     */
    public static Path getBankPath(String groupName) {
        return Paths.get("data", "gim", "banks", groupName + ".p");
    }

    /**
     * The player instance.
     */
    private final Player player;

    /**
     * The current open bank.
     */
    private GIMBank currentBank;

    /**
     * Creates a new {@link GIMBankManager}.
     */
    public GIMBankManager(Player player) {
        this.player = player;
    }

    /**
     * Opens the GIM bank.
     */
    public void open() {
        if (player.isUnregisteredGIM()) {
            player.sendMessage("You are not apart of a group. Speak to the GIM guide to join one.");
            return;
        } else if (!player.isGroupIronman()) {
            player.sendMessage("Only group ironmen may use this bank.");
            return;
        }
        requestBank(player.gimName).open(player);
    }

    /**
     * Called when the GIM bank closes.
     */
    public void close() {
        if (currentBank != null) {
            currentBank.save();
            if (currentBank.getBank().getWithdrawNotes())
                currentBank.getBank().switchWithdrawNotes();
            currentBank.getBank().refreshLeavePlaceHolders();
            currentBank.getBank().shiftItems();
            currentBank.getBank().setPlayer(null);
            currentBank.setUsingBank(null);
            currentBank = null;
        }
    }

    /**
     * Displays the GIM bank history.
     */
    public void displayHistory() {
        if (player.isUnregisteredGIM()) {
            player.sendMessage("You are not apart of a group. Speak to the GIM guide to join one.");
            return;
        } else if (!player.isGroupIronman()) {
            player.sendMessage("Only group ironmen may check their group's bank history.");
            return;
        }
        requestBank(player.gimName).displayHistory(player);
    }

    /**
     * Determines if the group member has the GIM bank open.
     */
    public boolean isOpen() {
        return currentBank != null;
    }

    /**
     * Adds a history entry.
     */
    public void addHistory(String action, Item item) {
        if (isOpen()) {
            List<GIMBankHistory> history = currentBank.getHistory();
            if (history.size() >= MAX_HISTORY_ENTRIES) {
                history.clear();
            }
            history.add(new GIMBankHistory(player.getDisplayName(), action + " " + Colors.DARK_RED + item.getName() + "(x" + Utils.formatNumber(item.getAmount()) + ")</col>"));
        }
    }

    public static LoadingCache<String, GIMBank> getBanks() {
        return banks;
    }

    public GIMBank getCurrentBank() {
        return currentBank;
    }

    void setCurrentBank(GIMBank currentBank) {
        this.currentBank = currentBank;
    }
}
