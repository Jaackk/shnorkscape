package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class CollapseTabDialogue extends Dialogue {
    private int tabId;

    @Override
    public void start() {
        tabId = (int) parameters[0];
        sendDialogue("Are you sure you wish to collapse this bank tab? This is irreversible! Items will be transferred to the master bank tab.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, I am sure.", "No, I don't wish to.");
                stage = 0;
                break;
            case 0:
                if (componentId == OPTION_1) {
                    if(player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().collapse(tabId);
                    } else {
                        player.getBank().collapse(tabId);
                    }
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}
