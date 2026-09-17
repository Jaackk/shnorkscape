package com.rs.utils;

import com.rs.game.player.Player;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public final class IPMute {

    private static final String PATH = "data/punishments/IPMute.noel";
    public static CopyOnWriteArrayList<String> muteipList;
    private static boolean edited;

    public static void checkCurrent() {
        for (String list : muteipList) {
            Logger.getGlobal().info(list);
        }
    }

    public static CopyOnWriteArrayList<String> getList() {
        return muteipList;
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        File file = new File(PATH);
        if (file.exists())
            try {
                muteipList = (CopyOnWriteArrayList<String>) SerializableFilesManager.loadSerializedFile(file);
                return;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        muteipList = new CopyOnWriteArrayList<String>();
    }

    public static void ipMute(Player player) {
        player.setMuted(Utils.currentTimeMillis() + (48 * 60 * 60 * 1000));
        muteipList.add(player.getIP());
        edited = true;
        save();
    }

    public static boolean ipMute(final Player player, final boolean isOnline) {
        if (player == null) {
            return false;
        }
        final String ip = isOnline ? player.getIP() : player.getLastIP();
        if (ip != null) {
            muteipList.add(ip);
            edited = true;
            save();
            return true;
        } else {
            Logger.getGlobal().info("[IPMute] IP was null when attempting to IP mute " + player.getUsername());
            return false;
        }
    }


    public static boolean isMuted(String ip) {
        return muteipList.contains(ip);
    }

    public static final void save() {
        if (!edited)
            return;
        try {
            SerializableFilesManager.storeSerializableClass(muteipList, new File(PATH));
            edited = false;
            Logger.getGlobal().info("Saved " + muteipList.size() + " muted IP's!");
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void unmute(Player player) {
        player.setMuted(0);
        muteipList.remove(player.getLastIP());
        edited = true;
        save();
    }
}