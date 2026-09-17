package com.rs.game.player.content.interfaces;

import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 28/09/2019
 * #Shnek6969
 */
public class ReaperBenefitsInterface {

    public static final int INTERFACE_ID = 132;
    public static int[] ITEM_LIST_COMPONENTS = {18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,
            35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,71,72,73,74
    };


    public static void sendInterface(Player player){
        sendPerkList(player);
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        player.getPackets().sendText(INTERFACE_ID,16, "Reaper Perks");
        player.getPackets().sendText(INTERFACE_ID,86, "Select an perk for more info");
        player.getPackets().sendText(INTERFACE_ID, 94, player.getReaperPoints()+"");

    }

    private static String getFormatedPerkDisplay(Player player,ReaperPerks perk) {
        String perkName = Utils.formatPlayerNameForDisplay(perk.name()).replace("Reapers", "Reaper's");
        return player.reaperPerkUnlocked(perk) ? ("<col=" + (player.reaperPerkActivated(perk) ? "00ff00" : "FFFF00") + ">" + perkName + " " + (player.reaperPerkActivated(perk) ? "(Active)" : "(Inactive)")) : ("<col=ff0000>" + perkName + " (" + perk.getPrice() + ")");
    }

    private static void sendEmptyString(Player player){
        player.getPackets().sendHideComponents(INTERFACE_ID,true,ITEM_LIST_COMPONENTS);
    }

    private static void sendPerkList(Player player){
        sendEmptyString(player);
        for(int i = 0; i < ReaperPerks.values().length; i++){
            player.getPackets().sendHideIComponent(INTERFACE_ID,ITEM_LIST_COMPONENTS[i], false);
            player.getPackets().sendText(INTERFACE_ID, ITEM_LIST_COMPONENTS[i], getFormatedPerkDisplay(player,ReaperPerks.values()[i]));
        }
    }

    private static void sendPerkInfo(Player player, ReaperPerks perk){
        String perkName = Utils.formatPlayerNameForDisplay(perk.name()).replace("Reapers", "Reaper's");
        player.getTemporaryAttributtes().put("selected_reaper_perk", perk);
        player.getPackets().sendText(INTERFACE_ID,86,perkName);
        player.getPackets().sendText(INTERFACE_ID,80,perk.getDesc());
        if (player.reaperPerkUnlocked(perk)) {
            player.getPackets().sendText(INTERFACE_ID,82, (player.reaperPerkActivated(perk) ? "Activated" : "Deactivated"));
        } else
            player.getPackets().sendText(INTERFACE_ID,82, "Buy");
        player.getPackets().sendHideComponents(INTERFACE_ID,false, 81);
    }
    public static void handelButtonOptions(Player player, int componentId){
        if(componentId >= 18 && componentId <= 74){
            sendPerkInfo(player, ReaperPerks.values()[componentId - 18]);
        }else if(componentId == 82){
            if(player.getTemporaryAttributtes().get("selected_reaper_perk") == null) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need to select a perk before you can buy something.");
                return;
            } else {
                ReaperPerks selectedPerk = (ReaperPerks) player.getTemporaryAttributtes().get("selected_reaper_perk");
                String perkName = Utils.formatPlayerNameForDisplay(selectedPerk.name()).replace("Reapers", "Reaper's");
                if (player.reaperPerkUnlocked(selectedPerk)) {
                    player.togglePerkActivation(selectedPerk.ordinal());
                    player.getPackets().sendText(INTERFACE_ID,82, (player.reaperPerkActivated(selectedPerk) ? "Activated" : "Deactivated"));
                    sendPerkList(player);
                    player.getDialogueManager().startDialogue("SimpleMessage",perkName + " has been " + (player.reaperPerkActivated(selectedPerk) ? "Activated" : "Deactivated") + ".");
                    return;
                }

                player.getDialogueManager().startDialogue(new Dialogue() {
                    @Override
                    public void start() {
                        sendOptionsDialogue("ARE YOU SURE YOU WANT TO BUY " + perkName.toUpperCase() + "? Current Reaper Points: " + player.getReaperPoints(), "Yes, i am sure.", "Nevermind.");
                        stage = 1;
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        if(stage == 1) {
                            if (componentId == OPTION_1) {
                                if (player.getReaperPoints() >= selectedPerk.getPrice()) {
                                    player.setReaperPoints(player.getReaperPoints() - selectedPerk.getPrice());
                                    player.getReaperPerks().add(selectedPerk);
                                    sendDialogue("Perk Unlocked: " + perkName + "!");
                                    if (player.reaperPerkUnlocked(selectedPerk)) {
                                        player.getPackets().sendText(INTERFACE_ID, 82, (player.reaperPerkActivated(selectedPerk) ? "Activated" : "Deactivated"));
                                    } else
                                        player.getPackets().sendText(INTERFACE_ID, 82, "Buy");
                                    player.getPackets().sendText(INTERFACE_ID, 94, player.getReaperPoints() + "");
                                    sendPerkList(player);
                                    end();
                                }
                                sendDialogue("You don't have enough reaper points to purchase " + perkName + ".");
                                stage = 2;
                            }
                        } else {
                            end();
                        }
                    }

                    @Override
                    public void finish() {

                    }
                });
                }

        }
    }
}
