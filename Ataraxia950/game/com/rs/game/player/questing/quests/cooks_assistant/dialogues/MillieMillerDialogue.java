package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Created by David on 8/4/2017.
 */
public class MillieMillerDialogue extends Dialogue {

    private int npcId;

    @Override
    public void start() {
        npcId = (int) parameters[0];
        stage = 0;
        sendNPCDialogue(npcId, Dialogue.CALM, "Hello adventurer. " +
                "Welcome to Mill Lane Mill. Can I help you?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 0) {
            stage = 1;
            sendPlayerDialogue(Dialogue.CALM, "I'm looking for extra fine flour.");
        } else if(stage == 1) {
            stage = 2;
            sendNPCDialogue(npcId, Dialogue.CALM, "What's wrong with ordinary flour?");
        } else if(stage == 2) {
            stage = 3;
            sendPlayerDialogue(Dialogue.CALM, "Well, I'm no expert chef, " +
                    "but apparently it makes better cakes. This cake, " +
                    "you see, is for Duke Horacio.");
        } else if(stage == 3) {
            stage = 4;
            sendNPCDialogue(npcId, Dialogue.CALM, "Really? How marvellous! Well, " +
                    "I can sure help you out there. " +
                    "Go ahead and use the mill and " +
                    "I'll realign the millstones to produce " +
                    "extra fine flour. Anything else?");
        } else if(stage == 4) {
            sendPlayerDialogue(Dialogue.CALM, "No, that'll be all, thanks.");
            end();
        }
    }

    @Override
    public void finish() {

    }
}
