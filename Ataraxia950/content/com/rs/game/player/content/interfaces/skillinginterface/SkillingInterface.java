package com.rs.game.player.content.interfaces.skillinginterface;

import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.interfaces.teleport.TeleportLocation;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * ataraxia-server
 * paolo 23/11/2019
 * #Shnek6969
 */
public class SkillingInterface {

    public static final int[] TELEPORT_CONTAINERS = {80,84,87,90,93,96,99,102,105,108,111,114,117,120,123,126,129,132};
    public static final int[] FAVORITE_CONTAINERS = {138,142,145,148,151,154,157,160,163,166};
    private static final String SELECTED_TELEPORT_KEY = "selectedTeleportKey";
    private static final int FAVORITE_SPRITE = 23796;
    private static final int NOT_FAVORITE_SPRITE = 23798;

    public static final int INTERFACE_ID = 1937;

    public static void sendInterface(Player player){
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        hideTeleportContainers(player);
        hideFavoriteContainers(player);
        sendFavorites(player);
    }

    private static void hideTeleportContainers(Player player){
            player.getPackets().sendHideComponents(INTERFACE_ID, true , TELEPORT_CONTAINERS);
    }

    private static void hideFavoriteContainers(Player player){
            player.getPackets().sendHideComponents(INTERFACE_ID, true, FAVORITE_CONTAINERS);
    }

    private static void sendPossibleTeleports(Player player, SkillingTeleportData skillingTeleportData){
        hideTeleportContainers(player);
        player.getTemporaryAttributtes().put(SELECTED_TELEPORT_KEY, skillingTeleportData);
        for(int i = 0; i <skillingTeleportData.locations.length; i++){
            if(i >= TELEPORT_CONTAINERS.length)
                return;
            player.getPackets().sendHideIComponent(INTERFACE_ID, TELEPORT_CONTAINERS[i], false);
            player.getPackets().sendIComponentSprite(INTERFACE_ID,TELEPORT_CONTAINERS[i] + 1,
                    (player.getFavoriteSkillingTeleports().contains(skillingTeleportData.locations[i]) ? FAVORITE_SPRITE : NOT_FAVORITE_SPRITE ));
            player.getPackets().sendText(INTERFACE_ID, TELEPORT_CONTAINERS[i] + 2, skillingTeleportData.locations[i].getName());
        }
    }


    public static void handelButtonClicks(Player player, int componentId){
        switch (componentId){
            case 11:
                sendPossibleTeleports(player, SkillingTeleportData.AGILITY);
                return;
            case 13:
                sendPossibleTeleports(player, SkillingTeleportData.CONSTRUCTION);
                return;
                case 16:
                sendPossibleTeleports(player, SkillingTeleportData.COOKING);
                    return;
                case 23:
                sendPossibleTeleports(player, SkillingTeleportData.CRAFTING);
                    return;
                case 26:
                sendPossibleTeleports(player, SkillingTeleportData.DUNGEONEERING);
                    return;
                case 29:
                sendPossibleTeleports(player, SkillingTeleportData.FARMING);
                    return;
                case 32:
                sendPossibleTeleports(player, SkillingTeleportData.FIREMAKING);
                    return;
                case 35:
                sendPossibleTeleports(player, SkillingTeleportData.FISHING);
                    return;
                case 38:
                sendPossibleTeleports(player, SkillingTeleportData.FLETCHING);
                    return;
                case 41:
                sendPossibleTeleports(player, SkillingTeleportData.HERBLORE);
                    return;
                case 44:
                sendPossibleTeleports(player, SkillingTeleportData.HUNTER);
                    return;
                case 47:
                sendPossibleTeleports(player, SkillingTeleportData.MINING);
                    return;
                case 50:
                sendPossibleTeleports(player, SkillingTeleportData.PRAYER);
                    return;
                case 53:
                sendPossibleTeleports(player, SkillingTeleportData.RUNECRAFTING);
                    return;
                case 56:
                sendPossibleTeleports(player, SkillingTeleportData.SLAYER);
                    return;
                case 59:
                sendPossibleTeleports(player, SkillingTeleportData.SMITHING);
                    return;
                case 62:
                sendPossibleTeleports(player, SkillingTeleportData.SUMMONING);
                    return;
                case 65:
                sendPossibleTeleports(player, SkillingTeleportData.THIEVING);
                    return;
                case 68:
                sendPossibleTeleports(player, SkillingTeleportData.WOODCUTTING);
                    return;
                case 71:
                sendPossibleTeleports(player, SkillingTeleportData.DIVINATION);
                    return;
            case 74:
                sendPossibleTeleports(player, SkillingTeleportData.INVENTION);
                return;
        }
        for(int i : TELEPORT_CONTAINERS) {
            if (componentId == i + 2) {
                handelTeleport(player, i);
                return;
            }
            if (componentId == i + 1)   {
                handelFavorite(player, i);
                return;
            }
        }

        for(int i : FAVORITE_CONTAINERS) {
            if (componentId == i + 2) {
                handelFavoriteTeleport(player, i);
                return;
            }
            if (componentId == i + 1)   {
                removeFromFavorite(player, i);
                return;
            }
        }
    }

    private static void removeFromFavorite(Player player, int component){
        int index = ArrayUtils.indexOf(FAVORITE_CONTAINERS, component);
        SkillingTeleportData skillingData = (SkillingTeleportData) player.getTemporaryAttributtes().get(SELECTED_TELEPORT_KEY);
        player.getFavoriteSkillingTeleports().remove(index);
        if(skillingData != null)
            sendPossibleTeleports(player,skillingData);
        sendFavorites(player);
    }

    private static void teleportPlayer(Player player, TeleportLocation location){
        if (location.isWilderness()) {
            player.getDialogueManager().startDialogue("WildernessConfirmationD", location.getTeleTile());
            return;
        }
        if(!SkillingTeleportData.hasRequirement(player,location.getName()))
            return;
        Magic.vineTeleport(player,location.getTeleTile());
        if(location.getController() != null)
            player.getControlerManager().startControler(location.getController());
    }

    private static void handelFavoriteTeleport(Player player, int i) {
        int index = ArrayUtils.indexOf(FAVORITE_CONTAINERS, i);
        if(player.getFavoriteSkillingTeleports().get(index) != null) {
            teleportPlayer(player,player.getFavoriteSkillingTeleports().get(index));
        }
    }


    private static void sendFavorites(Player player){
        hideFavoriteContainers(player);
            for(int i = 0; i < player.getFavoriteSkillingTeleports().size(); i++){
                if(i >= FAVORITE_CONTAINERS.length)
                    return;
                player.getPackets().sendHideIComponent(INTERFACE_ID, FAVORITE_CONTAINERS[i], false);
                player.getPackets().sendIComponentSprite(INTERFACE_ID,FAVORITE_CONTAINERS[i] + 1, FAVORITE_SPRITE);
                player.getPackets().sendText(INTERFACE_ID, FAVORITE_CONTAINERS[i] + 2, player.getFavoriteSkillingTeleports().get(i).getName());
            }
    }

    private static void handelFavorite(Player player, int component) {
        int index = ArrayUtils.indexOf(TELEPORT_CONTAINERS, component);
        SkillingTeleportData skillingData = (SkillingTeleportData) player.getTemporaryAttributtes().get(SELECTED_TELEPORT_KEY);
        if(skillingData != null){

            if(player.getFavoriteSkillingTeleports().contains(skillingData.locations[index])){
                player.getFavoriteSkillingTeleports().remove(skillingData.locations[index]);
            } else {
                player.getFavoriteSkillingTeleports().add(skillingData.locations[index]);
            }
            if(skillingData != null)
                sendPossibleTeleports(player,skillingData);
            sendFavorites(player);

        }
    }

    private static void handelTeleport(Player player, int component) {
        int index = ArrayUtils.indexOf(TELEPORT_CONTAINERS, component);
        SkillingTeleportData locations = (SkillingTeleportData) player.getTemporaryAttributtes().get(SELECTED_TELEPORT_KEY);
        if(locations != null){
            teleportPlayer(player, locations.locations[index]);
            //Magic.vineTeleport(player,locations.locations[index].getTeleTile());
        }
    }
}
