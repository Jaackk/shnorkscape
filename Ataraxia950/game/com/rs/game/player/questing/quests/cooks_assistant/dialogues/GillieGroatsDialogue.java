package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Created by David on 8/4/2017.
 */
public class GillieGroatsDialogue extends Dialogue {

    private int npcId;

    @Override
    public void start() {
        npcId = (int) parameters[0];
        stage = 0;
        sendNPCDialogue(npcId, Dialogue.CALM, "Hello, I'm Gillie. " +
                "What can I do for you?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 0) {
            stage = 1;
            sendPlayerDialogue(Dialogue.CALM, "I'm after some top-quality milk.");
        } else if(stage == 1) {
            stage = 2;
            sendNPCDialogue(npcId, Dialogue.CALM, "Really? Is it for something special?");
        } else if(stage == 2) {
            stage = 3;
            sendPlayerDialogue(Dialogue.CALM, "Most certainly! It's for the " +
                    "cook to make a cake for Duke Horacio!");
        } else if(stage == 3) {
            stage = 4;
            sendNPCDialogue(npcId, Dialogue.CALM, "Wow, it's quite an honour " +
                    "that you'd pick my cows. I'd suggest " +
                    "you get some milk from my prized cow.");
        } else if(stage == 4) {
            stage = 5;
            sendPlayerDialogue(Dialogue.CALM, "Which one's that?");
        } else if(stage == 5) {
            sendNPCDialogue(npcId, Dialogue.CALM, "She's on the east side " +
                    "of the field, over by the cliff. Be gentle!");
            end();
        }
    }

    @Override
    public void finish() {

    }
}
