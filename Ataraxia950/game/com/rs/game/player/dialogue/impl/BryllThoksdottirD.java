package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

public class BryllThoksdottirD extends Dialogue {

    private int npcId;

    @Override
    public void start() {
        npcId = (int) this.parameters[0];
        sendNPCDialogue(npcId, NORMAL, "Hello there, did you come to help me with the temple?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            stage = 3;
            sendPlayerDialogue(ANGRY, "Noo, I'm just here to buy stuff.");
            break;
        case 0:
            stage = 1;
            sendNPCDialogue(npcId, CALM_TALKING, "Alright, I sell some lucky charms for 10k dungeoneering tokens ea.");
            break;
        case 1:
            stage = 2;
            sendOptionsDialogue("HOW MANY LUCKY CHARMS WOULD YOU LIKE TO BUY?", "Buy 1 lucky charm.", "Buy 5 lucky charms.", "Buy X lucky charms.", "Nevermind.");
            break;
        case 2:
            end();
            if (componentId == OPTION_4)
                return;
            if (componentId == OPTION_3) {
                player.sendInputInteger("Enter amount: ", new InputIntegerEvent() {

                    @Override
                    public void run(Player player) {
                        int amount = getInteger();
                        if (amount <= 0)
                            return;
                        buyLuckyCharm(amount);
                    }
                });
                return;
            }
            buyLuckyCharm(componentId == OPTION_1 ? 1 : 5);
            break;
        case 3:
            stage = 4;
            sendOptionsDialogue("WHAT WOULD YOU LIKE TO BUY?", "Lucky charms (10k dung tokens)", "Seiryu's fang (100k dung tokens)", "Nevermind.");
            break;
        case 4:
            if (componentId == OPTION_3) {
                end();
                return;
            }
            switch (componentId) {
            case OPTION_1:
                stage = 2;
                sendOptionsDialogue("HOW MANY LUCKY CHARMS WOULD YOU LIKE TO BUY?", "Buy 1 lucky charm.", "Buy 5 lucky charms.", "Buy X lucky charms.", "Nevermind.");
                break;
            case OPTION_2:
                end();
                if (player.getInventory().getFreeSlots() == 0) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have enough inventory space to buy that.");
                    return;
                }
                if (player.getDungeoneeringManager().getTokens() < 100000) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have enough dungeoneering tokens to buy that.");
                    return;
                }
                player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() - 100000);
                player.getInventory().addItem(37823, 1);
                player.getPackets().sendGameMessage("You bought Seiryu's fang for 100k dungeoneering tokens.");
                break;
            }
            break;
        }
    }

    public void buyLuckyCharm(final int amount) {
        if (player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(43066, 1)) {
            player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have enough inventory space to buy that.");
            return;
        }
        if (((long) amount * 10000L) > Integer.MAX_VALUE)
            return;
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("That will be " + Utils.formatNumber((amount * 10000)) + ", Do you want to continue?", "Yes.", "No.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (componentId == OPTION_2)
                    return;
                int amountToBuy = amount;
                if (player.getDungeoneeringManager().getTokens() < (amountToBuy * 10000)) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "You don't have enough dungeoneering tokens to buy that.");
                    return;
                }
                int amountInInv = player.getInventory().getAmountOf(43066);
                if (((long) amountInInv + (long) amountToBuy) > Integer.MAX_VALUE)
                    amountToBuy = Integer.MAX_VALUE - amountInInv;
                player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() - (amountToBuy * 10000));
                player.getInventory().addItem(43066, amountToBuy);
                player.getPackets().sendGameMessage("You bought " + amountToBuy + " lucky charms for " + Utils.formatNumber((amountToBuy * 10000)) + " dungeoneering tokens.");
            }

            @Override
            public void finish() {

            }

        });
    }

    @Override
    public void finish() {

    }

}
