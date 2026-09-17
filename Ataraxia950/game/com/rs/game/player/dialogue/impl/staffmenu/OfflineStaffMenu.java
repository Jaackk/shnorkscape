package com.rs.game.player.dialogue.impl.staffmenu;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.*;

import java.io.File;
import java.io.IOException;

import static com.rs.utils.Colors.RED;

/**
 * @author Xenthium
 */

public class OfflineStaffMenu extends Dialogue {

    private Player target;
    private File save;
    private String targetName;

    @Override
    public void start() {
        target = (Player) parameters[0];
        save = (File) parameters[1];
        targetName = target.getUsername();
        if (player.isStaff()) {
            mainMenu();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 10:
                switch (componentId) {
                    case OPTION_1:
                        sendOptionsDialogue("Are you sure you want to mute " + targetName + "?", "Yes", "Back...");
                        stage = 11;
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Are you sure you want to ban " + targetName + "?", "Yes", "Back...");
                        stage = 12;
                        break;
                    case OPTION_3:
                        end();
                        break;
                }
                break;

            case 11:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                handlePunishment(PunishmentType.MUTE, getInteger());
                            }
                        });
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 12:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                handlePunishment(PunishmentType.BAN, getInteger());
                            }
                        });
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;
        }

    }

    @Override
    public void finish() {

    }

    private void mainMenu() {
        sendOptionsDialogue("Offline Staff menu for " + targetName, "Mute (opens confirmation)", "Ban (opens confirmation)", "Close menu");
        stage = 10;
    }

    private boolean canPerformRequest() {
        return !World.containsPlayer(targetName);
    }

    private void sendTargetError() {
        sendErrorDialogue("Something went wrong when attempting to do that, perhaps the player logged in?");
    }

    private void handlePunishment(final PunishmentType punishmentType, final int durationHours) {
        final long duration = Utils.currentTimeMillis() + (durationHours * 60 * 60 * 1000);
        final boolean permanent = durationHours > 48;
        switch (punishmentType) {
            case MUTE:
                if (canPerformRequest()) {
                    target.setMuted(permanent ? Long.MAX_VALUE : duration);
                    player.sendMessage(permanent ? "You have permanently muted " + targetName + "." : "You have muted " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                    try {
                        SerializableFilesManager.savePlayer(target);
                        SerializableFilesManager.storeSerializableClass(target, save);
                    } catch (IOException e) {
                        sendErrorDialogue("Something might've gone wrong when attempting to store the character file,<br> please report it to a developer in staff chat.");
                        return;
                    }
                } else {
                    sendTargetError();
                }
                break;
            case BAN:
                if (canPerformRequest()) {
                    if (permanent) {
                        target.setPermBanned(true);
                    } else {
                        target.setBanned(duration);
                    }
                    try {
                        SerializableFilesManager.savePlayer(target);
                        SerializableFilesManager.storeSerializableClass(target, save);
                    } catch (IOException e) {
                        sendErrorDialogue("Something might've gone wrong when attempting to store the character file,<br> please report it to a developer in staff chat.");
                        return;
                    }
                    player.sendMessage(permanent ? RED + "You have permanently banned " + targetName + "." : "You have banned " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                } else {
                    sendTargetError();
                }
                break;
        }
    }

    private void sendErrorDialogue(final String errorMessage) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendDialogue(errorMessage);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
            }

            @Override
            public void finish() {
            }
        });
    }




    private enum PunishmentType {
        MUTE, BAN
    }

}
