package com.rs.game.player.content.interfaces.potiontimers;

import com.rs.game.player.Player;
import com.rs.game.player.content.jujupotions.JujuPotionManager;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 28/07/2019
 * #Shnek6969
 */
public class PotionTimerInterface {


    public static final int INTERFACE_ID_FIXED = 1926;
    public static final int INTERFACE_ID_RESIZABLE = 1927;
    public static final int RESIZABLE_TAB = 239;
    public static final int FIXED_TAB = 203;
    public static final int[] CONTAINERS_FIXED = {14, 11, 8, 5, 2};
    public static final int[] CONTAINERS_RESIZABLE = {14, 11, 8, 5, 1};

    public static void sendInterface(Player player) {
        boolean resizable = player.getInterfaceManager().isResizableScreen();
//        player.getInterfaceManager().sendTab(resizable ? RESIZABLE_TAB : FIXED_TAB, resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED); TODO RS3
        hideContainers(player);
    }

    private static void hideContainers(Player player) {
        boolean resizable = player.getInterfaceManager().isResizableScreen();
//        player.getPackets().sendHideComponents(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, true, resizable ? CONTAINERS_RESIZABLE : CONTAINERS_FIXED);
    }

    public static void updateInterface(Player player) {
//        boolean resizable = player.getInterfaceManager().isResizableScreen();
//        if(!(player.getInterfaceManager().containsInterface(INTERFACE_ID_FIXED) || player.getInterfaceManager().containsInterface(INTERFACE_ID_RESIZABLE)) && player.isShowPotionTimers())//sending the interface if not already there
//            sendInterface(player);
//        for (int index = 0; index < player.getActivePotions().size(); index++) {
//            if (index >= CONTAINERS_FIXED.length) { // in case someone adds all timers
//                return;
//            }
//            PotionTimer timer = PotionTimer.getTimerByItemId(player.getActivePotions().get(index), player);
//            if (timer != null) {
//                String timeformat = getTimeFormatString(player, timer);
//                player.getPackets().sendHideComponents(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, false, resizable ? CONTAINERS_RESIZABLE[index] : CONTAINERS_FIXED[index]);
//                player.getPackets().sendIComponentSprite(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, resizable ? CONTAINERS_RESIZABLE[index] + 1 : CONTAINERS_FIXED[index] + 1, -1);
//                player.getPackets().sendItemOnIComponent(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, resizable ? CONTAINERS_RESIZABLE[index] + 1 : CONTAINERS_FIXED[index] + 1, -1);
//                if (timer == PotionTimer.BONFIRE)
//                    player.getPackets().sendIComponentSprite(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, resizable ? CONTAINERS_RESIZABLE[index] + 1 : CONTAINERS_FIXED[index] + 1, timer.getDisplayItem(player));
//                else
//                    player.getPackets().sendItemOnIComponent(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, resizable ? CONTAINERS_RESIZABLE[index] + 1 : CONTAINERS_FIXED[index] + 1, timer.getDisplayItem(player));
//                player.getPackets().sendIComponentText(resizable ? INTERFACE_ID_RESIZABLE : INTERFACE_ID_FIXED, resizable ? CONTAINERS_RESIZABLE[index] + 2 : CONTAINERS_FIXED[index] + 2, timeformat);
//                if (timeformat.equalsIgnoreCase("") || timeformat.contains("-") || timeformat.equals("0m")) {
//                    removeTimer(player, index);
//                }
//            } else
//                removeTimer(player, index);
//        }
    }

    public static String getTimeFormatString(Player player, PotionTimer timer) {
        long time = timer.getTime(player);
        String name = timer.name();
        if (timer.equals(PotionTimer.AURA))
            return getFormatedTime(timer.getTime(player));
        else if (JujuPotionManager.JUJU_EFFECTS.containsKey(name))
            return player.jujuPotions.getRemainingTime(name);
        else if (timer.equals(PotionTimer.BONFIRE))
            return time / 100 + "m";
        else
            return Utils.formatActiveTimerShort(timer.getTime(player));
    }

    public static String getFormatedTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes -= hours * 60;
        seconds -= (hours * 60 * 60) + (minutes * 60);
        String minutesString = (minutes < 10 ? "0" : "") + minutes;
        String secondsString = (seconds < 10 ? "0" : "") + seconds;
        return minutesString + ":" + secondsString;
    }

    private static void removeTimer(Player player, int index) {
        player.getActivePotions().remove(index);
        hideContainers(player);
    }

    public static void addTimer(Player player, PotionTimer timer, int awdwad) {
        if (player.getActivePotions().contains(timer.getDisplayItem(player))) {//don't need dupes {
            updateInterface(player);
            return;
        }
        player.getActivePotions().add(timer.getDisplayItem(player));
        updateInterface(player);
    }

}
