package com.rs.game.player.security.pin;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;

import java.util.function.Consumer;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DIncorrectAccountPin extends Dialogue {

    private final Consumer<Dialogue> action;

    public DIncorrectAccountPin(Consumer<Dialogue> action) {
        this.action = action;
    }

    @Override
    public void start() {
        player.getAccountPin().setLocked();
        player.lock();
        sendDialogue(Colors.DARK_RED + "Invalid PIN entered. You may retry in " + player.getAccountPin().getPinDelay() + "...");
        WorldTasksManager.schedule(new WorldTask() {
            private int pinDelay = player.getAccountPin().getPinDelay();
            @Override
            public void run() {
                if (--pinDelay <= 0) {
                    stop();
                    player.unlock();
                    action.accept(DIncorrectAccountPin.this);
                } else {
                    sendDialogue(Colors.DARK_RED + "Invalid PIN entered. You may retry in " + pinDelay + "...");
                }
            }
        }, 2, 2);
        player.getAccountPin().increasePinDelay();
    }

    @Override
    public void run(int interfaceId, int componentId) {

    }

    @Override
    public void finish() {

    }
}
