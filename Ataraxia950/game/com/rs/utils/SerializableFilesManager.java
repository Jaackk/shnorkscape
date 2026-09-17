package com.rs.utils;

import com.rs.game.player.Player;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.content.clans.Clan;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.game.player.content.grandExchange.OfferHistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class SerializableFilesManager {

    private static final String PATH = "data/playersaves/characters/";
    private static final String CLAN_PATH = "data/clans/";
    private static final String BACKUP_PATH = "data/playersaves/charactersBackup/";
    /**
     * Bot saves go in their own folder so AutoBackup, mainsave zips, and any
     * "real accounts" tooling can ignore them. If bot persistence is ever
     * re-enabled (see commented-out LumbridgeIronmanPersistence), files land
     * here instead of polluting characters/.
     */
    private static final String BOT_PATH = "data/playersaves/bots/";

    private static String pathFor(Player player) {
        return (player instanceof BotPlayer) ? BOT_PATH : PATH;
    }

    private static void ensureDir(String dir) {
        File d = new File(dir);
        if (!d.exists()) d.mkdirs();
    }

    private static final String GE_OFFERS = "data/grandExchange/geOffers.data";
    private static final String GE_OFFERS_HISTORY = "data/grandExchange/geOffersTrack.data";
    private static final String GE_PRICES = "data/grandExchange/gePrices.ataraxia";

    public synchronized static final boolean containsPlayer(String username) {
        return new File(PATH + username + ".p").exists()
                || new File(BOT_PATH + username + ".p").exists();
    }

    public synchronized static final boolean containsPlayerBackup(String username) {
        return new File(BACKUP_PATH + username + ".p").exists();
    }

    public static boolean createBackup(String username) {
        try {
            Utils.copyFile(new File(PATH + username + ".p"), new File(BACKUP_PATH + username + ".p"));
            return true;
        } catch (Throwable e) {
        	if (e instanceof IOException)
        		System.out.println("Invalid path to backup directory.. maybe missing?");
            Logger.getGlobal().catching(e);
            return false;
        }
    }

    public synchronized static Player loadPlayer(String username) {
        File real = new File(PATH + username + ".p");
        File bot  = new File(BOT_PATH + username + ".p");
        File target = real.exists() ? real : (bot.exists() ? bot : null);
        if (target == null) return null;
        try {
            return (Player) loadSerializedFile(target);
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    public static final Object loadSerializedFile(File f) throws IOException, ClassNotFoundException {
        if (!f.exists())
            return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return in.readObject();
        }
	}

    public synchronized static void savePlayer(Player player) {
        String dir = pathFor(player);
        ensureDir(dir);
        try {
            storeSerializableClass(player, new File(dir + player.getUsername() + ".p"));
        } catch (ConcurrentModificationException e) {
            Logger.getGlobal().catching(e);
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }
    public synchronized static void savePlayer(String username, Player player) {
        player.setUsername(username);
        savePlayer(player);
    }
    public synchronized static final void storeSerializableClass(Serializable o, File f) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
            out.writeObject(o);
        } catch (ConcurrentModificationException e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static boolean containsClan(String clanName) {
        File file = new File(CLAN_PATH + clanName + ".c");
        return file.exists();
    }

    public synchronized static void saveClan(Clan clan) {
        try {
            storeSerializableClass(clan, new File(CLAN_PATH + clan.getClanName() + ".c"));
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static Clan loadClan(String clanName) {
        try {
            return (Clan) loadSerializedFile(new File(CLAN_PATH + clanName + ".c"));
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    public static void deleteClan(Clan clan) {
        File file = new File(CLAN_PATH + clan.getClanName() + ".c");
        if (!file.exists()) {
            return;
        }
        file.delete();
    }

    @SuppressWarnings("unchecked")
    public static synchronized HashMap<Long, Offer> loadGEOffers() {
        if (new File(GE_OFFERS).exists()) {
            try {
                return (HashMap<Long, Offer>) loadSerializedFile(new File(GE_OFFERS));
            } catch (Throwable t) {
                Logger.getGlobal().catching(t);
                return null;
            }
        } else {
            return new HashMap<Long, Offer>();
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized ArrayList<OfferHistory> loadGEHistory() {
        if (new File(GE_OFFERS_HISTORY).exists()) {
            try {
                return (ArrayList<OfferHistory>) loadSerializedFile(new File(GE_OFFERS_HISTORY));
            } catch (Throwable t) {
                Logger.getGlobal().catching(t);
                return null;
            }
        } else {
            return new ArrayList<OfferHistory>();
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized HashMap<Integer, Integer> loadGEPrices() {
        if (new File(GE_PRICES).exists()) {
            try {
                return (HashMap<Integer, Integer>) loadSerializedFile(new File(GE_PRICES));
            } catch (Throwable t) {
                Logger.getGlobal().catching(t);
                return null;
            }
        } else {
            return new HashMap<Integer, Integer>();
        }
    }


    public static synchronized void saveGEOffers(HashMap<Long, Offer> offers) {
        try {
            SerializableFilesManager.storeSerializableClass(offers, new File(GE_OFFERS));
        } catch (Throwable t) {
            Logger.getGlobal().catching(t);
        }
    }

    public static synchronized void saveGEHistory(ArrayList<OfferHistory> history) {
        try {
            SerializableFilesManager.storeSerializableClass(history, new File(GE_OFFERS_HISTORY));
        } catch (Throwable t) {
            Logger.getGlobal().catching(t);
        }
    }

    public static synchronized void saveGEPrices(HashMap<Integer, Integer> prices) {
        try {
            SerializableFilesManager.storeSerializableClass(prices, new File(GE_PRICES));
            Logger.getGlobal().info("Re-calculated Grand Exchange item prices..");
        } catch (Throwable t) {
            Logger.getGlobal().catching(t);
        }
    }

}