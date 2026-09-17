package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public final class AuraConfirmationD extends Dialogue {

    private Runnable yesAction;

    @Override
    public void start() {
        yesAction = (Runnable) parameters[0];
        sendDialogue("Doing this will deactivate your current aura. Are you sure you wish to proceed?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    yesAction.run();
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}
