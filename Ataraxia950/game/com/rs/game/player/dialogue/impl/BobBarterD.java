package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.Pots;
import com.rs.game.player.dialogue.Dialogue;

public class BobBarterD extends Dialogue {

    private int npcId;
    private int option;
    private int decantToDoses = 4;

    @Override
    public void start() {
        npcId = (int) this.parameters[0];
        option = (int) this.parameters[1];
        switch (option) {
        case 1:// Talk
            stage = -1;
            sendPlayerDialogue(NORMAL, "Hi.");
            break;
        case 2:// Info
            stage = 16;
            sendPlayerDialogue(NORMAL, "I would like to view herbs info.");
            break;
        case 3:// Decant
            sendPlayerDialogue(NORMAL, "Can you decant things for me?");
            stage = 11;
            break;
        case 4:// Decant X
            sendPlayerDialogue(NORMAL, "Can you decant things for me but not to 4 doses?");
            stage = 13;
            break;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            sendNPCDialogue(npcId, HAPPY, "Hello, chum, fancy buyin' some designer jewllery? They've come all the way from Ardougne! Most pukka!");
            stage = 0;
            break;
        case 0:
            sendPlayerDialogue(CONFUSED, "Erm, no. I'm all set, thanks.");
            stage = 1;
            break;
        case 1:
            sendNPCDialogue(npcId, HAPPY, "Okay, chum, so what can I do for you? I can tell you the very latest herb prices, or perhaps I could help you decant your potions.");
            stage = 2;
            break;
        case 2:
            sendOptionsDialogue("Select an Option", "Who are you?", "Can you decant things for me?", "Sorry I've got to split.");
            stage = 3;
            break;
        case 3:
            if (componentId == OPTION_1) {
                sendPlayerDialogue(NORMAL, "Who are you?");
                stage = 4;
            } else if (componentId == OPTION_2) {
                sendPlayerDialogue(NORMAL, "Can you decant things for me?");
                stage = 11;
            } else if (componentId == OPTION_3) {
                sendPlayerDialogue(NORMAL, "Sorry I've got to split.");
                stage = 15;
            }
            break;
        case 4:
            sendNPCDialogue(npcId, NORMAL, "Why, I'm Bob! Your friendly seller of smashin goods!");
            stage = 5;
            break;
        case 5:
            sendPlayerDialogue(NORMAL, "So what do you have to sell?");
            stage = 6;
            break;
        case 6:
            sendNPCDialogue(npcId, NORMAL, "Oh, not much at the moment. Cuz, ya know. Business being so well and cushie.");
            stage = 7;
            break;
        case 7:
            sendPlayerDialogue(NORMAL, "You don't really look like you're being so successful.");
            stage = 8;
            break;
        case 8:
            sendNPCDialogue(npcId, MAD, "You plonka! It's all a show, innit! If I let people knows I'm in good business they'll want a share of the moolah!");
            stage = 9;
            break;
        case 9:
            sendPlayerDialogue(MOCK, "You conviently have a responce for everything.");
            stage = 10;
            break;
        case 10:
            sendNPCDialogue(npcId, HAPPY, "That's the Ardougne way, my son.");
            stage = 15;
            break;
        case 11:
            sendNPCDialogue(npcId, HAPPY, "Why of course my son.");
            stage = 12;
            break;
        case 12:
            end();
            sendNPCDialogueNoContinue(player, npcId, LAUGHING, "Chum chum chum...");
            Pots.decantPotsInInv(player, npcId, decantToDoses);
            stage = 15;
            break;
        case 13:
            stage = 14;
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "I want to decant my potions to 3 doses.", "I want to decant my potions to 2 doses.", "I want to decant my potions to 1 doses.", "Nevermind.");
            break;
        case 14:
            if (componentId == OPTION_4) {
                end();
                break;
            }
            stage = 12;
            decantToDoses = componentId == OPTION_1 ? 3 : componentId == OPTION_2 ? 2 : 1;
            sendNPCDialogue(npcId, HAPPY, "Why of course my son.");
            break;
        case 15:
            end();
            break;
        case 16:// TODO
            stage = 15;
            sendNPCDialogue(npcId, SAD, "Sorry i can't do that right now.");
            break;
        }
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub

    }

}
