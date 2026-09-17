package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.List;


public class PraesulCodex extends Dialogue {

    private static final String[] CODEX_PRAYERS_NAMES = { "Malevolence", "Desolation", "Affliction" };
    private List<Integer> availablePrayers;
    private int choosenPrayer;

    @Override
    public void start() {
        availablePrayers = new ArrayList<Integer>();
        choosenPrayer = -1;
        boolean hasAll = true;
        for (int i = 0; i < player.getPrayer().getCodexPrayers().length; i++) {
            boolean codex = player.getPrayer().getCodexPrayers()[i];
            if (!codex) {
                hasAll = false;
                availablePrayers.add(i);
            }
        }
        if (hasAll) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "You already have unlocked all of the prayers available.");
            return;
        }
        stage = -1;
        sendDialogue("By reading this codex you can unlock one of the three new prayers.");
    }

    public String[] getDialogueOptions() {
        String[] options = new String[availablePrayers.size() + 1];
        for (int i = 0; i < availablePrayers.size(); i++) {
            options[i] = "Unlock " + CODEX_PRAYERS_NAMES[availablePrayers.get(i)] + " prayer";
        }
        options[options.length - 1] = "Cancel";
        return options;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                stage = 0;
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, getDialogueOptions());
                break;
            case 0:
                int length = availablePrayers.size() + 1;
                if (componentId == OPTION_1) {
                    sendConfirm(availablePrayers.get(0));
                } else if (componentId == OPTION_2) {
                    if (length == 2) {
                        end();
                        return;
                    }
                    sendConfirm(availablePrayers.get(1));
                } else if (componentId == OPTION_3) {
                    if (length == 3) {
                        end();
                        return;
                    }
                    sendConfirm(availablePrayers.get(2));
                } else if (componentId == OPTION_4) {
                    end();
                }
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.getInventory().containsItem(39584, 1)) {
                        player.getInventory().deleteItem(new Item(39584, 1));
                        player.getPrayer().unlockCodexPrayer(choosenPrayer);
                        player.getPackets().sendGameMessage("You have unlocked a new prayer!");
                    }
                    end();
                } else if (componentId == OPTION_2) {
                    end();
                } else if (componentId == OPTION_3) {
                    stage = 0;
                    choosenPrayer = -1;
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, getDialogueOptions());
                }
                break;
        }
    }

    private void sendConfirm(int choosenPrayer) {
        this.choosenPrayer = choosenPrayer;
        stage = 2;
        sendOptionsDialogue("ARE YOU SURE YOU WANT TO UNLOCK THIS PRAYER?", "Yes, unlock it.", "No, don't unlock it.",
                "Back to previous options");
    }

    @Override
    public void finish() {

    }
}
