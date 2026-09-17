package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 8/4/2017.
 */
public class CookIntroDialogue extends Dialogue {

    private QuestInteraction interaction;
    private int npcId;

    @Override
    public void start() {
        interaction = (QuestInteraction) parameters[0];
        npcId = (Integer) parameters[1];
        stage = 0;
        sendNPCDialogue(npcId, Dialogue.SAD, "What am I to do?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 0) {
            stage = 1;
            sendPlayerDialogue(Dialogue.CALM, "What's wrong?");
        } else if(stage == 1) {
            stage = 2;
            sendNPCDialogue(npcId, Dialogue.SAD, "Oh dear, oh dear, " +
                    "oh dear, I'm in a terrible, terrible mess!");
        } else if(stage == 2) {
            stage = 3;
            sendNPCDialogue(npcId, Dialogue.SAD, "It's the Duke's birthday " +
                    "today and I should be making him a lovely, " +
                    "big birthday cake using special ingredients...");
        } else if(stage == 3) {
            stage = 4;
            sendNPCDialogue(npcId, Dialogue.SAD, "...but I've forgotten " +
                    "the ingredients. I'll never get them in time now.");
        } else if(stage == 4) {
            stage = 5;
            sendNPCDialogue(npcId, Dialogue.SAD, "He'll sack me! Whatever will I do? " +
                    "I have four children and a goat " +
                    "to look after. Would you help me? Please?");
        } else if(stage == 5) {
            stage = 6;
            sendOptionsDialogue("Begin Cook's Assistant?", "Yes", "No");
        } else if(stage == 6) {
            switch(componentId) {
                case OPTION_1:
                    stage = 7;
                    interaction.getQuest().start();
                    sendNPCDialogue(npcId, Dialogue.CALM, "Oh, thank you, thank you. " +
                            "I must tell you that this is no ordinary cake, " +
                            "though - only the best ingredients will do!");
                    break;
                case OPTION_2:
                    sendPlayerDialogue(Dialogue.CALM, "No, sorry, I'm busy right now.");
                    end();
                    break;
            }
        } else if(stage == 7) {
            stage = 8;
            sendNPCDialogue(npcId, Dialogue.CALM, "I need a super large egg, " +
                    "top-quality milk and some extra fine flour.");
        } else if(stage == 8) {
            stage = 9;
            sendPlayerDialogue(Dialogue.CALM, "Where can I find those then?");
        } else if(stage == 9) {
            stage = 10;
            sendNPCDialogue(npcId, Dialogue.SAD, "That's the problem: I don't exactly know. " +
                    "I usually send my assistant to get them for me...");
        } else if(stage == 10) {
            stage = 11;
            sendNPCDialogue(npcId, Dialogue.SAD, "...but he quit. I trust you know where " +
                    "to find such ingredients?...");
        } else if(stage == 11) {
            sendPlayerDialogue(Dialogue.CALM, "I'll do my best...");
            end();
        }
    }

    @Override
    public void finish() {

    }
}
