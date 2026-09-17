package com.rs.game.player.security.pin;

import com.google.common.collect.Iterables;
import com.rs.game.player.dialogue.Dialogue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DAccountPinSettings extends Dialogue {

    private final AccountPinModification[] modOptions = new AccountPinModification[4];
    private AccountPinModification chosen;

    @Override
    public void start() {
        player.getAccountPin().activateMod();
        if (player.unlockedSettings) {
            sendSettings();
        } else {
            sendDialogue("Please verify your PIN to access the settings.");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMainMenu();
                break;
            case 0:
                AccountPin.openEnterPin(player, d -> {
                    player.sendMessage(AccountPin.COLOR + "You have unlocked your PIN settings for this session.");
                    player.unlockedSettings = true;
                    resend();
                }, d -> resend());
                break;
            case 1:
                Collection<AccountPinModification> pinMod = player.getAccountPin().mods().values();
                if (componentId == OPTION_1) {
                    Long hours = player.getAccountPin().getModTime("Change PIN");
                    if (hours == null) {
                        AccountPin.openChangePin(player, false);
                    } else {
                        String remainingStr = hours > 0 ? hours + "h" : "under an hour";
                        sendDialogue("You already have a pending PIN change with " + remainingStr + " left until it takes effect.",
                                "Please cancel that before applying a new one.");
                        stage = -1;
                    }
                } else if (componentId == OPTION_2) {
                    Long hours = player.getAccountPin().getModTime("Change recovery delay");
                    if (hours == null) {
                        AccountPin.openChangeRecoveryDelay(player);
                    } else {
                        String remainingStr = hours > 0 ? hours + "h" : "under an hour";
                        sendDialogue("You already have a pending recovery delay change with " + remainingStr + " left until it takes effect.",
                                "Please cancel that before applying a new one.");
                        stage = -1;
                    }
                } else if (componentId == OPTION_3) {
                    if (pinMod.size() > 0) {
                        sendPendingChanges(pinMod);
                    } else {
                        end();
                    }
                }
                break;
            case 2:
                if (componentId == OPTION_1) {
                    chosen = modOptions[0];
                } else if (componentId == OPTION_2) {
                    chosen = modOptions[1];
                } else if (componentId == OPTION_3) {
                    chosen = modOptions[2];
                } else if (componentId == OPTION_4) {
                    chosen = modOptions[3];
                }
                if (chosen == null) {
                    sendMainMenu();
                    break;
                }
                sendDialogue("Are you sure you would like to cancel '" + chosen.getDescription() + "'?");
                stage = 3;
                break;
            case 3:
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Yes",
                        "No");
                stage = 4;
                break;
            case 4:
                if (componentId == OPTION_1) {
                    player.sendMessage(AccountPin.COLOR + "The pending change '" + chosen.getDescription() + "' has been cancelled.");
                    player.getAccountPin().mods().remove(chosen.getDescription());
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {
        chosen = null;
    }

    private void resend() {
        player.getDialogueManager().startDialogue(new DAccountPinSettings());
    }

    private void sendMainMenu() {
        Collection<AccountPinModification> pinMod = player.getAccountPin().mods().values();
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                "Change your PIN.",
                "Change your recovery delay.",
                pinMod.size() > 0 ? "View pending changes." : "Nevermind.");
        stage = 1;
    }

    private void sendSettings() {
        Collection<AccountPinModification> pinMod = player.getAccountPin().mods().values();
        if (pinMod.size() > 0) {
            sendPendingChanges(pinMod);
        } else {
            sendMainMenu();
        }
    }

    private void sendPendingChanges(Collection<AccountPinModification> pinMod) {
        List<String> options = new ArrayList<>();
        int index = 0;
        for (AccountPinModification it : pinMod) {
            long hours = LocalDateTime.now().until(it.getActivationDate(), ChronoUnit.HOURS);
            String remainingStr = hours > 0 ? "(" + hours + "h left)." : "(under an hour left).";
            modOptions[index++] = it;
            options.add("Cancel '" + it.getDescription() + "' " + remainingStr);
        }
        options.add("View other settings.");
        if (options.size() > 5) {
            throw new IllegalStateException("Too many modifications.");
        }
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, Iterables.toArray(options, String.class));
        stage = 2;
    }
}
