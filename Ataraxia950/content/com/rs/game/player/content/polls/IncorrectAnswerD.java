package com.rs.game.player.content.polls;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class IncorrectAnswerD extends Dialogue {

    private final Runnable action;

    public IncorrectAnswerD(Runnable action) {
        this.action = action;
    }

    @Override
    public void start() {
        sendDialogue("You've entered an incorrect letter.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                action.run();
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}