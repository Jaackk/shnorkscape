package com.rs.game.player.content.interfaces;

import com.rs.game.ForceTalk;
import com.rs.game.player.ActivityTimersManager;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ataraxia-server
 * paolo 01/09/2019
 * #Shnek6969
 */
public class BossTimerInterface {

    public static final int INTERFACE_ID = 141;

    public static final int[] COMPONENT_IDS = {96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196};

    public static void sendInterface(Player player){
        sendInfo(player);
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
    }


    private static void sendInfo(Player player){
        int killCountIndex = 52;
        int timeIndex = 96;
        int activityIndex = 0;
        for(int i = 75; i < 173; i++){
            if(activityIndex  >= ActivityTimersManager.ActivityTimers.values().length) {
                return;
            }
            String bossName = Utils.formatString(ActivityTimersManager.ActivityTimers.values()[activityIndex].name().replaceAll("_", " "));
            player.getPackets().sendText(INTERFACE_ID, i , bossName);
            player.getPackets().sendText(INTERFACE_ID, killCountIndex, getCorrectCount(player,bossName));
            player.getPackets().sendText(INTERFACE_ID, timeIndex, Utils.formatTime(player.getActivityTimersManager().getTimerByName(bossName)));
            killCountIndex++;
            timeIndex++;
            activityIndex++;
            if(i == 92){ //new list of component ids
                i = 157;
                killCountIndex = 137;
                timeIndex = 179;
            }

        }
    }

   private static int getCorrectCount(Player player, String bossName){
        int amount = 0;
        switch (bossName.toLowerCase()){
            case "kreearra":
                bossName = "kree'arra";
                break;
        }
        amount = player.increaseKillStatistics(bossName, false);
        return amount > 0 ? amount : 0;
   }

   public static void handleButtons(Player player, int componentId){
       int index = Arrays.stream(COMPONENT_IDS).boxed().collect(Collectors.toList()).indexOf(componentId);
       String bossName = Utils.formatString( ActivityTimersManager.ActivityTimers.values()[index].name().replaceAll("_", " "));
       String time = Utils.formatTime(player.getActivityTimersManager().getTimerByName(bossName));

       player.setNextForceTalk(new ForceTalk("I've killed "+bossName+" ["+getCorrectCount(player,bossName)+"] times. My fastest time was: ["+time+"]."));

   }
}
