package com.rs.game.player.content.dropcollection;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ataraxia-server
 * paolo 07/06/2019
 * #Shnek6969
 */
public class DropCollectionInterface {

    public enum pages {BOSSES,CLUES,MINIGAMES,OTHERS}

    public static final int INTERFACE_ID = 129;
    private static final int[] BASE_COMPONENT_IDS = {51,54,57,60,63,66,69,72,75,78, 81,84,87,90,93,96,99,102,105,108,   111,114,117,120,123,126,129,132,135,138 ,141,144,151,163,166};
    private static  final  int UNDERLAYER_CONTAINER = 45;
    private static  final  int OVERLAY_CONTAINER = 149;
    private static  final  int UNDERLAYER_KEY = 90;
    private static  final  int OVERLAYER_KEY = 91;

    /**
     * sends the interface
     * @param player
     */
    public static void sendInterface(Player player){
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        sendBossNames(player);
        player.getPackets().sendIComponentText(INTERFACE_ID, 40, "");
        player.getPackets().sendIComponentText(INTERFACE_ID, 41, "");
        player.getPackets().sendIComponentText(INTERFACE_ID, 42, "");
        player.getTemporaryAttributtes().put("PageType",pages.BOSSES);
    }

    /**
     * sends all the npc names to the interface
     * @param player
     */
    private static void sendBossNames(Player player){
        for(int i = 0; i < DropCollectionConstants.BOSS_DATA.values().length; i ++){
            player.getPackets().sendHideIComponent(INTERFACE_ID, BASE_COMPONENT_IDS[i], false);

            player.getPackets().sendIComponentText(INTERFACE_ID, BASE_COMPONENT_IDS[i] + 2,
                    (player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.values()[i]) ? Colors.GREEN : Colors.RED) +
                     DropCollectionConstants.BOSS_DATA.values()[i].getName());
        }
    }

    private static void sendMiniGameNames(Player player){
        for(int i = 0; i < DropCollectionConstants.MINIGAME_DATA.values().length; i ++){
            player.getPackets().sendHideIComponent(INTERFACE_ID, BASE_COMPONENT_IDS[i], false);
            player.getPackets().sendIComponentText(INTERFACE_ID, BASE_COMPONENT_IDS[i] + 2, DropCollectionConstants.MINIGAME_DATA.values()[i].getName());
        }
    }

    /**
     *
     * @param player
     */
    private static void sendClueNames(Player player){
        for(int i = 0; i < DropCollectionConstants.CLUE_TYPE.values().length; i ++){
            player.getPackets().sendHideIComponent(INTERFACE_ID, BASE_COMPONENT_IDS[i], false);
            player.getPackets().sendIComponentText(INTERFACE_ID, BASE_COMPONENT_IDS[i] + 2, StringUtils.capitalize(DropCollectionConstants.CLUE_TYPE.values()[i].name().toLowerCase()));
        }
    }
    /**
     * when clicked on a boss,it sends the correct info of that boss
     * @param player
     * @param data
     */
    private static void sendBossInformation(Player player, DropCollectionConstants.BOSS_DATA data){
        sendAllItem(data.getDrops(), player);
        sendReveivedItems(player, player.getDropCollectionHandler().getReceivedMonsterDrops(data));
        player.getPackets().sendIComponentText(INTERFACE_ID, 40, "<col=ff9c24>"+data.getName()+"</col>");
        player.getPackets().sendIComponentText(INTERFACE_ID, 42, "Kills: "+player.getDropCollectionHandler().getCorrectBossCount(data)+"</col>");
        player.getPackets().sendIComponentText(INTERFACE_ID, 41, player.getDropCollectionHandler().getUniqueCount(player.getDropCollectionHandler().getBossCollection().get(data.getNpcId()), data.getDrops())+"/"+data.getDrops().length);
    }

    /**
     * sends the clue information when pressing
     * @param player
     * @param type
     */
    private static void sendClueInformation(Player player, DropCollectionConstants.CLUE_TYPE type){
        player.getPackets().sendIComponentText(INTERFACE_ID, 40, "<col=ff9c24>"+type.name().toLowerCase()+" clue</col>");
        sendAllItem(player.getDropCollectionHandler().getClueLootByType(type), player);
        sendReveivedItems(player, player.getDropCollectionHandler().getReceivedClueDrops(type));
        player.getPackets().sendIComponentText(INTERFACE_ID, 41, player.getDropCollectionHandler().getUniqueCount(player.getDropCollectionHandler().getClueCollection().get(type), player.getDropCollectionHandler().getClueLootByType(type))+"/"+player.getDropCollectionHandler().getClueLootByType(type).length);

    }

    private static void sendMinigameInformation(Player player, DropCollectionConstants.MINIGAME_DATA data){
        player.getPackets().sendIComponentText(INTERFACE_ID, 40, "<col=ff9c24>"+data.getName()+"</col>");
        sendAllItem(data.getDrops(), player);
        sendReveivedItems(player, player.getDropCollectionHandler().getReceivedMiniGame(data));
    }

    /**
     * sends the items to the upper container
     * @param player
     * @param items
     */
    private static void sendReveivedItems(Player player, ItemsContainer<Item> items){
        for(int i=0;i<items.getItems().length;i++)
            if(items.getItems()[i] != null && items.getItems()[i].getId() >= 252958)
            items.set(i, null);
        player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, OVERLAY_CONTAINER, OVERLAYER_KEY, 7, 18, "");
        player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, OVERLAY_CONTAINER, 0, 160, 0);
        player.getPackets().sendItems(OVERLAYER_KEY, false, items);
    }
    /**
     * sends all the items to an underlayer container
     * @param player
     */
    private static void sendAllItem(Item[] items, Player player){
        for(int i=0;i<items.length;i++)
            if(items[i] != null && items[i].getId() >= 252958)
            items[i] = null;
        player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, UNDERLAYER_CONTAINER, UNDERLAYER_KEY, 7, 18, "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, UNDERLAYER_CONTAINER, 0, 160, 0);
        player.getPackets().sendItems(UNDERLAYER_KEY, false, items);
    }

    private static void resetInterface(Player player){
        player.getPackets().sendIComponentText(INTERFACE_ID, 40, "");
        player.getPackets().sendIComponentText(INTERFACE_ID, 41, "");
        player.getPackets().sendIComponentText(INTERFACE_ID, 42, "");
        for(int comp : BASE_COMPONENT_IDS)
            player.getPackets().sendHideIComponent(INTERFACE_ID, comp, true);
    }

    /**
     * handles the button clicks
     * @param player
     * @param componentId
     */
    public static void handleButtons(Player player, int componentId){
        if(componentId >= 53 && componentId <=168){
            int index = Arrays.stream(BASE_COMPONENT_IDS).boxed().collect(Collectors.toList()).indexOf(componentId -2);
            if(player.getTemporaryAttributtes().get("PageType") == pages.BOSSES) {
                sendBossInformation(player, DropCollectionConstants.BOSS_DATA.values()[index]);
                return;
            }
            if(player.getTemporaryAttributtes().get("PageType") == pages.CLUES) {
                sendClueInformation(player, DropCollectionConstants.CLUE_TYPE.values()[index]);
                return;
            }
            if(player.getTemporaryAttributtes().get("PageType") == pages.MINIGAMES) {
                sendMinigameInformation(player, DropCollectionConstants.MINIGAME_DATA.values()[index]);
                return;
            }
        }
        switch(componentId){
            case 18:
                player.getTemporaryAttributtes().put("PageType",pages.BOSSES);
                resetInterface(player);
                sendBossNames(player);
                break;
            case 21:
                player.getTemporaryAttributtes().put("PageType",pages.CLUES);
                resetInterface(player);
                sendClueNames(player);
                break;
            case 24:
                player.getTemporaryAttributtes().put("PageType",pages.MINIGAMES);
                resetInterface(player);
                sendMiniGameNames(player);
                break;
            case 30:
            case 27:
                player.sm("Coming Soon.");
                break;
        }
    }
}
