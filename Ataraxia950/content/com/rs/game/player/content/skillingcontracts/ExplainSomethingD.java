package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ExplainSomethingD extends Dialogue {
    int npc;
    boolean highLevel;

    @Override
    public void start() {
        highLevel = (boolean) parameters[0];
        npc = highLevel ? 219 : 943;
        loadMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    sendNPCDialogue(npc, ANGRY, "I've stood by far too long watching you lot kill the same beasts over and over.",
                            "To circumvent this, I will be assigning skilling contracts to anyone who wants them.", "Do them for various rewards and XP.");
                    stage = 1;
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(npc, ANGRY, "You can spend your Skilling tickets on various effects to make contracts easier and/or more rewarding.",
                            "Temporary ones will last a total of " + TempContractEffect.DURATION + " contracts (or " + TempContractEffect.DURATION * 2 + " with perk) before wearing off.");
                    stage = 2;
                } else if (componentId == OPTION_3) {
                    sendNPCDialogue(npc, GOOFY_LAUGH, "Daily tasks are no more. I'm your daddy now.");
                    stage = 1;
                } else if (componentId == OPTION_4) {
                    player.getDialogueManager().startDialogue("SkillingMasterD", highLevel);
                }
                break;
            case 1:
                loadMainMenu();
                break;
            case 2:
                sendNPCDialogue(npc, ANGRY, "Others will allow for skipping and blocking certain types of contracts.");
                stage = 1;
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    public void loadMainMenu() {
        sendOptionsDialogue("Select an option.",
                "What are skilling contracts?",
                "What can I spend my Skilling tickets on?",
                "What happened to daily tasks?",
                "Nevermind.");
        stage = 0;
    }
}
