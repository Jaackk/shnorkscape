package com.rs.game.player.security.pin;

import com.rs.game.player.dialogue.Dialogue;

import java.util.function.Consumer;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DEnterAccountPin extends Dialogue {

    private final Consumer<Dialogue> success;
    private final Consumer<Dialogue> fail;

    public DEnterAccountPin(Consumer<Dialogue> success, Consumer<Dialogue> fail) {
        this.success = success;
        this.fail = fail;
    }

    @Override
    public void start() {
        AccountPin.openPinInput(player, input -> {
            String realPin = player.getAccountPin().getPin();
            if (input.equals(realPin)) {
                success.accept(this);
            } else {
                player.getDialogueManager().startDialogue(new DIncorrectAccountPin(fail));
            }
        });
    }

    @Override
    public void run(int interfaceId, int componentId) {

    }

    @Override
    public void finish() {

    }
}