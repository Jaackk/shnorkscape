package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.client.ClientProfile;

public class Cook extends Dialogue {

    private int npcId;

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                stage = 0;
                sendOptionsDialogue("Would you like to do a favour for the Cook?", "Yes", "No");
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        stage = 1;
                        sendPlayerDialogue(NORMAL, "Sure, What do you need help with?");
                        break;
                    case OPTION_2:
                        stage = 1;
                        end();
                        break;
                }
                break;
            case 1:
                sendNPCDialogue(npcId, CROOKED_HEAD,
                        "An Evil Old Chef from Lumbridge has gone crazy! He's hiding in that portal over there.");
                stage = 2;
                break;
            case 2:
                sendPlayerDialogue(SCARED, "Woah, that sounds crazy! Do you need help?");
                stage = 3;
                break;
            case 3:
                sendNPCDialogue(npcId, NORMAL, "Yes, please. If you help me get rid of him, I will reward you.");
                stage = 4;
                break;
            case 4:
                sendPlayerDialogue(CROOKED_HEAD, "Okay, how prepared should I be?");
                stage = 5;
                break;
            case 5:
                sendNPCDialogue(npcId, UNSURE, "I'm not sure, but I think he got his friends with him. So be prepared to fight for your life!");
                stage = 6;
                break;
            case 6:
                if (finishUnavailableNativeAdventure()) break;
                if (player.isHCIronMan()) {
                    sendNPCDialogue(npcId, NORMAL, "Before I tell you any more I must warn you that this miniquest is <u>NOT</u> safe for hardcore accounts. Should you die during the fight, your hardcore status will be revoked.");
                    stage = 10;
                } else {
                    player.setTalkedToCook();
                    sendPlayerDialogue(NORMAL, "Okay, I'll do it, wish me luck!");
                    stage = 7;
                }
                break;
            case 7:
                sendNPCDialogue(npcId, NORMAL, "Goodluck! Please kill them all, I'm counting on you!");
                stage = 8;
                break;
            case 8:
                if (finishUnavailableNativeAdventure()) break;
                player.getControlerManager().startControler("ImpossibleJadControler");
                end();
                break;
            case 9:
                end();
                break;
            case 10:
                sendOptionsDialogue("Are you sure you wish to proceed? This miniquest is NOT safe for hardcore accounts!", "Yes", "No");
                stage = 11;
                break;
            case 11:
                if (componentId == OPTION_1) {
                    player.setTalkedToCook();
                    sendPlayerDialogue(NORMAL, "Okay, I'll do it, wish me luck!");
                    stage = 7;
                } else {
                    end();
                }
                break;
            default:
                end();
                break;
        }

    }

    /** Dialogue presentation is ported; the legacy dynamic combat encounter is not. */
    private boolean finishUnavailableNativeAdventure() {
        if (player.getClientProfile() != ClientProfile.NATIVE_950) return false;
        sendNPCDialogue(npcId, NORMAL, "That adventure is not available yet. Please come back later.");
        stage = 9;
        return true;
    }

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        if (player.isKilledCulinaromancer()) {
            stage = 8;
            sendNPCDialogue(npcId, 9827, "May Saradomin bless you " + player.getDisplayName() + "! You are welcome to use my chest as reward!");
        } else if (!player.hasTalkedtoCook()) {
            stage = -1;
            sendNPCDialogue(npcId, 9827, "Hello " + player.getDisplayName() + ". Before we talk, can you do me a favour?");
        } else if (player.hasTalkedtoCook()) {
            stage = 8;
            sendNPCDialogue(npcId, ANGRY, "Keep going " + player.getDisplayName() + ", kill them all!");
        }
    }

    @Override
    public void finish() {

    }

}