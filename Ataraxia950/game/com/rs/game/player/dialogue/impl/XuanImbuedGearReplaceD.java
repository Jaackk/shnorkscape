package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

public class XuanImbuedGearReplaceD extends Dialogue {
    private int selectedAmount;

    @Override
    public void start() {
        int amountGears = player.getInventory().getNumberOf(41407);
        selectedAmount = parameters.length > 0 ? (int) parameters[0] : -1;
        if (selectedAmount == -1 && amountGears > 1) {
            end();
            player.sendInputInteger("How many would you like to replace?", new InputIntegerEvent() {
                @Override
                public void run(Player player) {
                    int value = getInteger();
                    if (value <= 0)
                        return;
                    if (value > amountGears)
                        value = amountGears;
                    player.getDialogueManager().startDialogue("XuanImbuedGearReplaceD", value);
                }
            });
            return;
        }
        if (selectedAmount == -1)
            selectedAmount = 1;
        sendOptionsDialogue("REPLACE " + selectedAmount + " X IMBUED GEAR FOR:", (50 * selectedAmount) + " vote points.", Utils.formatNumber(20000 * selectedAmount) + " chimes.", "Nevermind.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
        if (componentId == OPTION_1) {
            player.setVotePoints(player.getVotePoints() + (50 * selectedAmount));
            player.getInventory().deleteItem(new Item(41407, selectedAmount));
            player.getPackets().sendGameMessage("You have recieved " + (50 * selectedAmount) + " vote points. You currently have " + player.getVotePoints() + " vote points.");
        } else if (componentId == OPTION_2) {
            if ((((long) 20000 * (long) selectedAmount) + (long) player.getInventory().getAmountOf(37753)) > Integer.MAX_VALUE) {
                player.getPackets().sendGameMessage("You can't have more than 2147m of an item.");
                return;
            }
            player.getInventory().deleteItem(new Item(41407, selectedAmount));
            player.getInventory().addItem(37753, 20000 * selectedAmount);
        }
    }

    @Override
    public void finish() {

    }

}
