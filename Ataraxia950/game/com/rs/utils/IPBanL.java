package com.rs.utils;

import com.rs.game.World;
import com.rs.game.player.Player;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class IPBanL {

    private static final String PATH = "data/punishments/IPBan.noel";
    public static CopyOnWriteArrayList<String> ipList;
    private static boolean edited;

    public static void ban(Player player, boolean loggedIn) {
        if (!player.isOwner()) {
            player.setPermBanned(true);
            if (loggedIn) {
                World.getPlayers().stream().
                        filter(it -> Objects.equals(player.getIP(), it.getIP())).
                        forEach(it -> {
                            it.getRealChannel().disconnect();
                            it.setPermBanned(true);
                        });
                ipList.add(player.getIP());
                player.getRealChannel().disconnect();
            } else {
                World.getPlayers().stream().
                        filter(it -> Objects.equals(player.getLastIP(), it.getIP())).
                        forEach(it -> {
                            it.getRealChannel().disconnect();
                            it.setPermBanned(true);
                        });
                ipList.add(player.getLastIP());
                SerializableFilesManager.savePlayer(player);
            }
            edited = true;
        }
    }

    public static void checkCurrent() {
        for (String list : ipList) {
            Logger.getGlobal().info(list);
        }
    }

    public static CopyOnWriteArrayList<String> getList() {
        return ipList;
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        File file = new File(PATH);
        if (file.exists())
            try {
                ipList = (CopyOnWriteArrayList<String>) SerializableFilesManager.loadSerializedFile(file);
                return;
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        ipList = new CopyOnWriteArrayList<String>();
    }

    public static boolean isBanned(String ip) {
        return ipList.contains(ip);
    }

    public static final void save() {
        if (!edited)
            return;
        try {
            SerializableFilesManager.storeSerializableClass(ipList, new File(PATH));
            edited = false;
            Logger.getGlobal().info("Saved " + ipList.size() + " banned IP's!");
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void unban(Player player) {
        player.setPermBanned(false);
        player.setBanned(0);
        ipList.remove(player.getLastIP());
        edited = true;
        save();
    }
}