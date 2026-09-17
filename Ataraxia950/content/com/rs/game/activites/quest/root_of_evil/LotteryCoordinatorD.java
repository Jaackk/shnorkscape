package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96
 */
public final class LotteryCoordinatorD extends Dialogue {
    private int currentStage;

    @Override
    public void start() {
        currentStage = player.quests.getCurrentStage(RootOfEvil.class);
        if (currentStage == 3) {
            sendNPCDialogue(2998, ANGRY, "Why're you lookin' at me like that?");
            stage = 0;
        } else if (currentStage == 5) {
            sendNPCDialogue(2998, ANGRY, "Do you have my beer?");
            stage = 0;
        } else if (currentStage == 6) {
            sendNPCDialogue(2998, CONFUSED, "This beer smells kind of... herby. What brew is this?");
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (currentStage == 3) {
            switch (stage) {
                case 0:
                    sendPlayerDialogue(CONFUSED, "What can you tell me about the recent Evil Tree invasions?");
                    stage = 1;
                    break;
                case 1:
                    sendNPCDialogue(2998, ANGRY, "Nothin'! And there definitely isn't anything suspicious going on with them. You should give up your investigation.");
                    stage = 2;
                    break;
                case 2:
                    sendPlayerDialogue(CONFUSED, "If you know they aren't suspicious... you must have some info on them, right?");
                    stage = 3;
                    break;
                case 3:
                    sendNPCDialogue(2998, ANGRY, "I ain't tellin' you nothin'. Why don't you do something useful instead and go fetch me a beer? I'm thirsty.");
                    stage = 4;
                    break;
                case 4:
                    sendPlayerDialogue(UNSURE, "(The lottery coordinator clearly knows something... I wonder if Mister Wiggles can help me make him talk.)");
                    stage = 5;
                    player.questionedNpcs.clear();
                    player.quests.advanceStage(RootOfEvil.class);
                    break;
                case 5:
                    end();
                    break;
            }
        } else if (currentStage == 5) {
            switch (stage) {
                case 0:
                    if (player.getInventory().containsItem(5747, 1)) {
                        sendPlayerDialogue(CONFUSED, "Yes, right here.");
                        player.getInventory().deleteItem(5747, 1);
                        player.quests.advanceStage(RootOfEvil.class);
                        currentStage++;
                        stage = 0;
                    } else {
                        sendPlayerDialogue(CONFUSED, "Err... no.");
                        stage = 1;
                    }
                    break;
                case 1:
                    sendNPCDialogue(2998, ANGRY, "Get out of my face then!");
                    stage = 2;
                    break;
                case 2:
                    end();
                    break;
            }
        } else if (currentStage == 6) {
            switch (stage) {
                case 0:
                    sendNPCDialogue(2998, CONFUSED, "This beer smells kind of... herby. What brew is this?");
                    stage = 1;
                    break;
                case 1:
                    sendPlayerDialogue(HAPPY, "It's nothing but the finest for you sir. Royal Ataraxian stout!");
                    stage = 2;
                    break;
                case 2:
                    sendNPCDialogue(2998, HAPPY, "I feel different.... much happier... I love this stout!");
                    stage = 3;
                    break;
                case 3:
                    sendPlayerDialogue(CALM, "Yes, yes... So do you know anything about the recent Evil Tree attacks?");
                    stage = 4;
                    break;
                case 4:
                    sendNPCDialogue(2998, NORMAL, "Yes. I know lots... first of all, the attacks are fabricated. The Evil Trees are",
                            "created through magic machinery here in Ataraxia. They do not come from ScapeRune.");
                    stage = 5;
                    break;
                case 5:
                    sendNPCDialogue(2998, NORMAL, "A wizard is using the life essence in evil magical roots to accomplish this.",
                            "As a matter of fact, we're working together. I am the mastermind and I'm providing all the funds for the operation.");
                    stage = 6;
                    break;
                case 6:
                    sendPlayerDialogue(CONFUSED, "But why?!? Why are you doing this?");
                    stage = 7;
                    break;
                case 7:
                    sendNPCDialogue(2998, ANGRY, "Hah! King Jaedmo IV thinks he can just reduce lottery taxes without consulting me?",
                            "My funds have gone down significantly! I went from making millions a week to millions a month!",
                            "You will all pay with your lives.");
                    stage = 8;
                    break;
                case 8:
                    sendPlayerDialogue(CONFUSED, "Where is the magical machinery creating Evil Trees located?");
                    stage = 9;
                    break;
                case 9:
                    sendNPCDialogue(2998, NORMAL,  "The entrance to the underground vault containing the Evil Tree machinery is located",
                            "in some bushes behind Wydin's Food Store in Port Sari-- Wait, why am I telling you all this?!");
                    player.quests.advanceStage(RootOfEvil.class);
                    stage = 10;
                    break;
                case 10:
                    sendPlayerDialogue(HAPPY, "You've told me all I need to know. Thank you!");
                    stage = 11;
                    break;
                case 11:
                    sendNPCDialogue(2998, ANGRY, "Piss off.");
                    stage = 12;
                    break;
                case 12:
                    end();
                    break;
            }
        }
    }

    @Override
    public void finish() {

    }
}
