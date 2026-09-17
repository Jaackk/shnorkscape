package com.rs.game.player.content.death;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

public final class DeathResetDegradeD extends Dialogue {

    public static void start(Player player, int degradePercentage) {
        if (degradePercentage > 1) {
            player.getDialogueManager().startDialogue(new DeathResetDegradeD());
        } else {
            Dialogue.sendSingleNPCDialogue(player, 14386, NORMAL, "I can't set your item degrade percentage any lower than 1%.");
        }
    }

    private DeathResetDegradeD() {}

    @Override
    public void start() {
        sendNPCDialogue(14386, NORMAL, "How would you like to pay for your item degrade percentage reset?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                player.getDialogueManager().startDialogue(new DeathMainD());
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        "20M Coins",
                        "100 Reaper points");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (player.removeMoney(20_000_000)) {
                        sendAccepted();
                    } else {
                        sendCantAfford();
                    }
                } else if (componentId == OPTION_2) {
                    if (player.getReaperPoints() >= 100) {
                        player.addReaperPoints(-100);
                        sendAccepted();
                    } else {
                        sendCantAfford();
                    }
                }
                break;
            case 2:
                end();
                break;
        }
    }

    @Override
    public void finish() {
    }

    private void sendAccepted() {
        sendNPCDialogue(14386, NORMAL, "I have reset your item degrade percentage to 1%.");
        stage = 2;
        player.deathItemsManager.resetDeaths();
    }

    private void sendCantAfford() {
        sendNPCDialogue(14386, NORMAL, "You cannot afford that!");
        stage = -1;
    }
}
