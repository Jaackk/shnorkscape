package com.rs.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.content.FriendChatsManager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public final class DisplayNames {

    private static final String PATH = "data/displayNames.ser";

    /**
     * Display name -> username map. You're welcome, rewriting this was a bitch.
     */
    private static final BiMap<String, String> cachedNames = Maps.synchronizedBiMap(HashBiMap.create());

    @SuppressWarnings("unchecked")
    public static void init() {
        File file = new File(PATH);
        if (file.exists()) {
            try {
                cachedNames.putAll((Map<? extends String, ? extends String>) SerializableFilesManager.loadSerializedFile(file));
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    public static boolean isDisplayName(String displayName) {
        return cachedNames.containsKey(displayName.toLowerCase());
    }

    public static boolean removeDisplayName(Player player, boolean manualBan) {
        if (!player.hasDisplayName()) {
            if (!manualBan) {
                player.sendMessage("You do not have a display name set.");
            }
            return false;
        }
        cachedNames.remove(player.getDisplayName().toLowerCase());
        player.setDisplayName(null);
        if (World.getPlayers().contains(player)) {
            player.getAppearence().generateAppearenceData();
        }
        if (!manualBan) {
            player.getPackets().sendGameMessage("Display name successfully removed!");
        }
        CoresManager.getServiceProvider().executeNow(DisplayNames::save);
        return true;
    }

    public static void save() {
        try {
            SerializableFilesManager.storeSerializableClass((Serializable) cachedNames, new File(PATH));
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Given display name must be lowercase.
     */
    public static String getUsername(String displayName) {
        String result = cachedNames.get(displayName);
        if (result == null)
            return Utils.formatPlayerNameForProtocol(displayName);
        return result;
    }

    public static String getDisplayName(String username) {
        String result = cachedNames.inverse().get(username);
        if (result == null)
            return Utils.formatPlayerNameForDisplay(username);
        return result;
    }

    public static boolean setDisplayName(Player player, String displayName) {
        String lcDisplayName = displayName.toLowerCase();
        if ((SerializableFilesManager.containsPlayer(Utils.formatPlayerNameForProtocol(displayName)) || cachedNames.containsKey(lcDisplayName)) || displayName.startsWith(" "))
            return false;
        if (player.hasDisplayName())
            cachedNames.remove(player.getDisplayName().toLowerCase());
        cachedNames.forcePut(lcDisplayName, player.getUsername());
        player.setDisplayName(displayName);
        FriendChatsManager.refreshChat(player);
        player.getAppearence().generateAppearenceData();
        CoresManager.getServiceProvider().executeNow(DisplayNames::save);
        return true;
    }
}