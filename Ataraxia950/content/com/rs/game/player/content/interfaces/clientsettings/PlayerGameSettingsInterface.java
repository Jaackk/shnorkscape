package com.rs.game.player.content.interfaces.clientsettings;

import com.rs.game.player.Player;

/**
 * ataraxia-server
 * paolo 05/09/2019
 * #Shnek6969
 */
public class PlayerGameSettingsInterface {

    public static int INTERFACE_ID = -1;

    public static void sendInterface(Player player){
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
    }

    /**
     * checks if the setting is a client gameSettings and sends clientpacket if it is
     * @param setting
     */
    public static void handleClientSetting(Player player,PlayerGameSettings setting){
        boolean value = player.getGameSettings().get(setting);
        if(setting.isRequiresPacket())
            player.getPackets().sendSettingPacket(setting.ordinal(),value);
        if(setting == PlayerGameSettings.POTION_OVERLAY){
            //TODO do something
        }
    }

    public static void update(Player player,PlayerGameSettings setting, boolean value) {
        player.getGameSettings().put(setting, value);
        handleClientSetting(player,setting);
    }
}
