package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.RingTransformation;
import com.rs.game.player.actions.crafting.JewellerySmithing;
import com.rs.game.player.dialogue.Dialogue;

public class RingOfRaresD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("WHAT WOULD YOU LIKE TO TRANSFORM INTO?", "PartyHat", "Hallow'en Mask", "Santa Hat", "Easter Egg", "More options");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                stage = 0;
                sendOptionsDialogue("WHICH COLOUR PARTYHAT WOULD YOU LIKE TO TRANSFORM INTO?", "Blue", "White", "Red", "Green", "More options");
                break;
            case OPTION_2:
                stage = 2;
                sendOptionsDialogue("WHICH COLOUR HALLOW'EN MASK WOULD YOU LIKE TO TRANSFORM INTO?", "Green", "Blue", "Red");
                break;
            case OPTION_3:
                stage = 3;
                sendOptionsDialogue("WHICH COLOUR SANTA HAT WOULD YOU LIKE TO TRANSFORM INTO?", "Regular", "Black");
                break;
            case OPTION_4:
                transformInto(25427);
                break;
            case OPTION_5:
                stage = 4;
                sendOptionsDialogue("WHAT WOULD YOU LIKE TO TRANSFORM INTO?", "Pumpkin", "Christmas cracker", "Back");
                break;
            }
            break;
        case 0:
            switch (componentId) {
            case OPTION_1:
                transformInto(25415);
                break;
            case OPTION_2:
                transformInto(25416);
                break;
            case OPTION_3:
                transformInto(25417);
                break;
            case OPTION_4:
                transformInto(25418);
                break;
            case OPTION_5:
                stage = 1;
                sendOptionsDialogue("WHICH COLOUR PARTYHAT WOULD YOU LIKE TO TRANSFORM INTO?", "Yellow", "Purple", "Back");
                break;
            }
            break;
        case 1:
            switch (componentId) {
            case OPTION_1:
                transformInto(25419);
                break;
            case OPTION_2:
                transformInto(25420);
                break;
            case OPTION_3:
                stage = 0;
                sendOptionsDialogue("WHICH COLOUR PARTYHAT WOULD YOU LIKE TO TRANSFORM INTO?", "Blue", "White", "Red", "Green", "More options");
                break;
            }
            break;
        case 2:
            switch (componentId) {
            case OPTION_1:
                transformInto(25421);
                break;
            case OPTION_2:
                transformInto(25422);
                break;
            case OPTION_3:
                transformInto(25423);
                break;
            }
            break;
        case 3:
            switch (componentId) {
            case OPTION_1:
                transformInto(25424);
                break;
            case OPTION_2:
                transformInto(25425);
                break;
            }
            break;
        case 4:
            switch (componentId) {
            case OPTION_1:
                transformInto(25426);
                break;
            case OPTION_2:
                transformInto(17205);
                break;
            case OPTION_3:
                stage = -1;
                sendOptionsDialogue("WHAT WOULD YOU LIKE TO TRANSFORM INTO?", "PartyHat", "Hallow'en Mask", "Santa Hat", "Easter Egg", "More options");
                break;
            }
            break;
        }
    }

    public void transformInto(int npcId) {
        end();
        player.getActionManager().setAction(new Action() {

            @Override
            public boolean start(Player player) {
                player.stopAll(true);
                player.getActionManager().forceStop();
                player.getAppearence().transformIntoNPC(npcId);
                player.getInterfaceManager().sendInventoryInterface(375);
                return true;
            }

            @Override
            public boolean process(Player player) {
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                return 0;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 3);
                RingTransformation.resetTransformation(player);
            }
        });
    }

    @Override
    public void finish() {

    }

}
