package com.rs.game.player.content.titles;

import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.util.List;

/**
 * ataraxia-server
 * paolo 02/11/2019
 * #Shnek6969
 */
public class PlayerTitleHandler {

    public static final int INTERFACE_ID = 142;
    public static final String ATTRIBUTE_KEY = "selectedTitle";
    public static final String ATTRIBUTE_KEY_LIST = "selectedTitleList";
    public static final int[] TITLE_COMPONENTS = { 42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,
    124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163};



    public static void sendInterface(Player player){
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        sendTitles(player,null);
    }

    private static void sendTitles(Player player,TitleType type){
        player.getPackets().sendHideComponents(INTERFACE_ID,true,TITLE_COMPONENTS);
        if(type == null){
            player.getTemporaryAttributtes().remove(ATTRIBUTE_KEY_LIST);
           for (int i = 0; i < PlayerTitle.values().length; i++) {
                player.getPackets().sendHideIComponent(INTERFACE_ID,TITLE_COMPONENTS[i], false);
                player.getPackets().sendText(INTERFACE_ID,TITLE_COMPONENTS[i], (PlayerTitle.values()[i].hasRequirements(player) ? Colors.GREEN : Colors.RED) +PlayerTitle.values()[i].getTitle());
            }
        } else {
            List<PlayerTitle> titles = PlayerTitle.getByType(type);
            player.getTemporaryAttributtes().put(ATTRIBUTE_KEY_LIST, titles);
            for (int i = 0; i <titles.size(); i++) {
                player.getPackets().sendHideIComponent(INTERFACE_ID,TITLE_COMPONENTS[i], false);
                player.getPackets().sendText(INTERFACE_ID,TITLE_COMPONENTS[i], (titles.get(i).hasRequirements(player) ? Colors.GREEN : Colors.RED) +titles.get(i).getTitle());
            }
        }
    }

    public static void sendTitleInfo(Player player, PlayerTitle title){
        player.getPackets().sendText(INTERFACE_ID,122, title.getRequirementsString());
        player.getPackets().sendText(INTERFACE_ID,123, title.isBeforeName() ?  title.getTitle() + player.getDisplayName():  player.getDisplayName()+title.getTitle());
        player.getTemporaryAttributtes().put(ATTRIBUTE_KEY, title);
    }

    public static void setTitle(Player player, PlayerTitle title){
        if(!title.hasRequirements(player)){
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have the requirements to set this title. Check the requirements info box on the interface.");
            return;
        }
        player.getAppearence().setTitle(title.getTitleId());
    }


    public static void handelComponents(Player player,int componentId){
        if(componentId == 111){
            PlayerTitle title = (PlayerTitle) player.getTemporaryAttributtes().get(ATTRIBUTE_KEY);
            if (title != null)
                setTitle(player, title);
            return;
        }
        if(componentId == 17){
            sendTitles(player, null);
            return;
        }if(componentId == 20){
            sendTitles(player, TitleType.PVM);
            return;
        }if(componentId == 23){
            sendTitles(player, TitleType.SKILLING);
            return;
        }if(componentId == 26){
            sendTitles(player, TitleType.MISC);
            return;
        }if(componentId == 29){
            sendTitles(player, TitleType.DONATOR);
            return;
        }
        List<PlayerTitle> titles = (List<PlayerTitle>) player.getTemporaryAttributtes().get(ATTRIBUTE_KEY_LIST);
        for(int i = 0; i < TITLE_COMPONENTS.length; i++){
            if(componentId == TITLE_COMPONENTS[i] ) {
                if(titles == null && i < PlayerTitle.values().length)
                    sendTitleInfo(player, PlayerTitle.values()[i]);
                else if (titles != null && i < titles.size())
                    sendTitleInfo(player, titles.get(i));
                return;
            }

        }
    }
}
