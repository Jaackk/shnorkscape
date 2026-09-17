package com.rs.game.player.content.death;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class DeathPurchaseD extends Dialogue {

    private int cost;
    private int ringId = -1;

    @Override
    public void start() {
        int itemCount = player.deathItemsManager.getItemCount();
        if(itemCount == 0) {
            sendNPCDialogue(14386, CROOKED_HEAD, "I don't have any items to sell you.");
            stage = -1;
            return;
        }
        boolean hasCharges = hasCharges();
        cost = player.deathItemsManager.getTotalCost();
        int taxAmt = player.deathItemsManager.getFormattedTax();
        int degradeAmt = player.deathItemsManager.getDegradePercentageInt();
        String formattedCost = Utils.formatNumber(hasCharges ? cost / 2 : cost);
        if (hasCharges) {
            sendNPCDialogue(14386, CROOKED_HEAD, "I'm holding " + itemCount + " of your items.",
                    "Your tax is  " + taxAmt + "% and your degrade percentage is " + degradeAmt + "%.",
                    "I see you're wearing my ring... In that case, it will cost " + formattedCost + "gp to get your items back.");
        } else {
            sendNPCDialogue(14386, CROOKED_HEAD, "I'm holding " + itemCount + " of your items.",
                    "Your tax is " + taxAmt + "% and your degrade percentage is " + degradeAmt + "%.",
                    "It will cost " + formattedCost + "gp to get your items back.");
        }
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                sendOptionsDialogue("Select an option.", "Yeah, I'll pay.", "No way!");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (hasCharges())
                        cost /= 2;
                    if (player.removeMoney(cost)) {
                        player.deathItemsManager.resetTax();
                        DeathStatistics.getInstance().totalGpTaken.addAndGet(cost);
                        removeCharges();
                        player.deathItemsManager.addClaimedItems();
                        sendNPCDialogue(14386, CROOKED_HEAD, "You can claim your items from me by right clicking me and selecting the 'Claim' option.");
                        stage = -1;
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD, "Don't waste my time mortal. Either pay in full or do without.");
                        stage = -1;
                    }
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void removeCharges() {
        if (ringId != -1) {
//            String itemName = ItemDefinitions.getItemDefinitions(ringId).name;
//            int maxCharges = ItemConstants.getItemDefaultCharges(ringId);
//            int requiredCharges = (int) (maxCharges * 0.25);
//            int charges = player.getCharges().getCharges(ringId);
//            if (charges >= requiredCharges) {
//                int newCharges = charges - requiredCharges;
//                if (newCharges < 1)
//                    newCharges = 1;
//                player.rodSaved = true;
//                player.sendMessage(Colors.RED + "Your " + itemName + " has degraded a little.");
//                player.getCharges().setCharges(ringId, newCharges);
//                return;
//            }
//            player.sendMessage(Colors.RED + "Your " + itemName + " did not have enough charge for death to recognize it.");
        }
    }

    private boolean hasCharges() {
        ringId = -1;
//        if (player.getEquipment().containsOneItem(31871)) {
//            ringId = 31871;
//        } else if (player.getEquipment().containsOneItem(41069)) {
//            ringId = 41069;
//        }
//        if (ringId != -1) {
//            int maxCharges = ItemConstants.getItemDefaultCharges(ringId);
//            int requiredCharges = (int) (maxCharges * 0.25);
//            int charges = player.getCharges().getCharges(ringId);
//            return charges >= requiredCharges;
//        }
        return false;
    }
}
