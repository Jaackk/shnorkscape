package com.rs.game.player.security.pin;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DChangeRecoveryDelay extends Dialogue {

    protected static final int MIN_RECOVERY_DELAY = 3;
    protected static final int MAX_RECOVERY_DELAY = 30;

    @Override
    public void start() {
        sendMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                player.sendInputInteger("Enter your new recovery delay days.", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        int input = getInteger();
                        if (input < MIN_RECOVERY_DELAY) {
                            sendInvalid("shorter than " + MIN_RECOVERY_DELAY + " days");
                        } else if (input > MAX_RECOVERY_DELAY) {
                            sendInvalid("longer than " + MAX_RECOVERY_DELAY + " days");
                        } else {
                            player.getAccountPin().changeRecoveryDays(input);
                            end();
                        }
                    }
                });
                break;
            case 1:
                sendMainMenu();
                break;
        }
    }

    @Override
    public void finish() {

    }


    private void sendInvalid(String type) {
        sendDialogue("The recovery delay cannot be " + type + ".");
        stage = 1;
    }

    private void sendMainMenu() {
        sendDialogue("How long would you like the recovery delay to be?");
        stage = 0;
    }
}
