package com.rs.game.player.security.pin;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DCreateAccountPin extends Dialogue {
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 8;

    private final boolean changePin;
    private final boolean forgot;
    private String enteredPin;

    public DCreateAccountPin(boolean changePin, boolean forgot) {
        this.changePin = changePin;
        this.forgot = forgot;
    }


    @Override
    public void start() {
        sendMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                AccountPin.openPinInput(player, input -> {
                    if (input.length() < MIN_LENGTH || input.length() > MAX_LENGTH) {
                        sendDialogue("Your PIN must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " digits.");
                        stage = 2;
                        return;
                    }
                    enteredPin = input;
                    sendDialogue("Now please confirm the PIN you just entered.");
                    stage = 1;
                }, input -> {
                    sendDialogue("Your PIN must only contain digits.");
                    stage = 2;
                });
                break;
            case 1:
                AccountPin.openPinInput(player, input -> {
                    if (!input.equals(enteredPin)) {
                        sendDialogue("Your PINs do not match. Please re-enter them.");
                        stage = 2;
                        enteredPin = null;
                        return;
                    }
                    if (changePin) {
                        player.getAccountPin().changePin(input);
                        if (forgot) {
                            sendDialogue("You will be able to access your account with your new PIN in " + player.getAccountPin().getRecoveryDays() + " days.");
                            stage = 3;
                        } else {
                            end();
                        }
                    } else {
                        end();
                        player.getAccountPin().setPinEntered();
                        player.getAccountPin().setPin(input);
                        player.sendMessage(AccountPin.COLOR + "Your PIN has been created. You can access your PIN settings through the banks at ;;home.");
                    }
                });
                break;
            case 2:
                sendMainMenu();
                break;
            case 3:
                player.getDialogueManager().startDialogue(new DAccountPin());
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendMainMenu() {
        sendDialogue("Please enter your desired PIN. It must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " digits.");
        stage = 0;
    }
}