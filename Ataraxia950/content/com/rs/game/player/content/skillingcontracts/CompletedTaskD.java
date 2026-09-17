package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CompletedTaskD extends Dialogue {

    @Override
    public void start() {
        sendPlayerDialogue(CALM, "I have completed my assigned contract.",
                "I should return to a skilling master for another one.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeScreenInterface();
    }
}