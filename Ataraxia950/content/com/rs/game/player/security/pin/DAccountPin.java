package com.rs.game.player.security.pin;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DAccountPin extends Dialogue {

    @Override
    public void start() {
        if (!player.getAccountPin().hasPin()) {
            sendDialogue("Runescape values the security of our players. For this reason, you'll need to create an account PIN.",
                    "It will only have to be entered when you login from a different IP than your last login.");
            stage = 0;
        } else {
            sendMainOptions();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMainOptions();
                break;
            case 0:
                AccountPin.openCreatePin(player);
                break;
            case 1:
                if (componentId == OPTION_1) {
                    AccountPin.openEnterPin(player);
                } else if (componentId == OPTION_2) {
                    Long hours = player.getAccountPin().getModTime("Change PIN");
                    if (hours == null) {
                        sendDialogue("If you've forgotten your PIN, it will have to be changed. Would you like to do that?");
                        stage = 2;
                    } else {
                        String remainingStr = hours > 0 ? hours + "h" : "under an hour";
                        sendDialogue("You already have a pending PIN change with " + remainingStr + " left until it takes effect.",
                                "Please cancel that before applying a new one.");
                        stage = -1;
                    }
                }
                break;
            case 2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1) {
                    AccountPin.openChangePin(player, true);
                } else if (componentId == OPTION_2) {
                    sendMainOptions();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendMainOptions() {
        sendOptionsDialogue("Select an option.", "Enter account PIN.", "I forgot my PIN.");
        stage = 1;
    }
}