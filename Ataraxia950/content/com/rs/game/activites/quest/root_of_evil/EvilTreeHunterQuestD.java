package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.player.dialogue.Dialogue;

public class EvilTreeHunterQuestD extends Dialogue {

    private int currentStage;

    @Override
    public void start() {
        currentStage = player.quests.getCurrentStage(RootOfEvil.class);
        if (currentStage == 0) {
            sendPlayerDialogue(CONFUSED, "I found this root after slaying an Evil Tree, could you tell me what it is?");
            stage = -1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (currentStage == 0) {
            switch (stage) {
                case -1:
                    if(player.getInventory().containsItem(9919, 1)) {
                        sendNPCDialogue(13790, NORMAL, "Let me see...");
                        stage++;
                    } else {
                        sendNPCDialogue(13790, SCARED, "I don't see anything! Stop wasting my time.");
                        stage = 5;
                    }
                    break;
                case 0:
                    sendNPCDialogue(13790, SCARED, "Incredible! It seems to be teeming with evil power.",
                            "I... I'm sorry but I don't what this is.");
                    stage++;
                    break;
                case 1:
                    sendNPCDialogue(13790, NORMAL, "I know who might know though...",
                            "Go see someone named Mister Wiggles, an old gnome mage in hiding.",
                            "You can find him on the second floor of Zaff's staff shop, in Varrock.");
                    stage++;
                    break;
                case 2:
                    sendPlayerDialogue(CONFUSED, "Err... why is he in hiding?");
                    stage++;
                    break;
                case 3:
                    sendNPCDialogue(13790, NORMAL,
                            "He senses dangerous events before they happen.",
                            "His predictions can cause people to become... very angry when they come true.");
                    stage++;
                    break;
                case 4:
                    sendPlayerDialogue(NORMAL, "Alright. I'll go see him.");
                    player.quests.advanceStage(RootOfEvil.class);
                    stage++;
                    break;
                case 5:
                    end();
                    break;
            }
        }
    }

    @Override
    public void finish() {

    }
}
