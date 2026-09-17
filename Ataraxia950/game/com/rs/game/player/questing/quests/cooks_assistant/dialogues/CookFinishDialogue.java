package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 8/4/2017.
 */
public class CookFinishDialogue extends Dialogue {

    private int npcId;
    private boolean hasIngredients;
    private QuestInteraction interaction;

    public static final int EGG_ID = 1;
    public static final int FLOUR_ID = 1;
    public static final int MILK_ID = 1;

    @Override
    public void start() {
        npcId = (int) parameters[1];
        hasIngredients = (boolean) parameters[2];
        interaction = (QuestInteraction) parameters[0];
        stage = 0;
        sendNPCDialogue(npcId, Dialogue.CALM, "How are you " +
                "getting on with finding the ingredients?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 0) {
            stage = 1;
            if(hasIngredients) {
                sendPlayerDialogue(Dialogue.CALM, "Yes, I have them all right here.");
            } else {
                sendPlayerDialogue(Dialogue.CALM, "I haven't got all of them yet, I'm still looking.");
            }
        } else if(stage == 1) {
            if(hasIngredients) {
                stage = 2;
                sendItemDialogue(EGG_ID, 1, "You hand the egg over to the chef.");
            } else {
                sendNPCDialogue(npcId, Dialogue.SAD, "Please get the ingredients quickly. " +
                        "I'm running out of time! The Duke will throw me out onto the street!");
                end();
            }
        } else if(stage == 2) {
            stage = 3;
            sendItemDialogue(MILK_ID, 1, "You hand the bucket of milk over to the chef.");
        } else if(stage == 3) {
            stage = 4;
            sendItemDialogue(FLOUR_ID, 1, "You hand the pot of flour over to the chef.");
        } else if(stage == 4) {
            stage = 5;
            sendNPCDialogue(npcId, Dialogue.CALM, "You've brought me everything I need! I am saved! Thank you!");
        } else if(stage == 5) {
            stage = 6;
            sendPlayerDialogue(Dialogue.CALM, "So, do I get to go to the Duke's party?");
        } else if(stage == 6) {
            stage = 7;
            sendNPCDialogue(npcId, Dialogue.CALM, "I'm afraid not. Only the big " +
                    "cheeses get to dine with the Duke.");
        } else if(stage == 7) {
            stage = 8;
            sendPlayerDialogue(Dialogue.CALM, "Well, maybe one day, " +
                    "I'll be important enough to sit at the Duke's table.");
        } else if(stage == 8) {
            sendNPCDialogue(npcId, Dialogue.CALM, "Maybe, but I won't be holding my breath.");
            end();
        }
    }

    @Override
    public void finish() {
        if(hasIngredients) {
            interaction.getQuest().advance();
        }
    }
}
