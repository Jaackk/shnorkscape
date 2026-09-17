package com.rs.game.player.dialogue.impl.staffmenu;


import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.IPBanL;
import com.rs.utils.IPMute;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.MACBan;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rs.utils.Colors.RED;

/**
 * @author Xenthium
 */

public class OnlineStaffMenu extends Dialogue {

    private Player target;
    private String targetName;

    @Override
    public void start() {
        target = (Player) parameters[0];
        targetName = target.getDisplayName();
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
                        sendOptionsDialogue("Assist menu for " + targetName, "Kick (opens confirmation)", "Teleport to", "Teleport to me", "Send home / unnull", "Back to main menu...");
                        goStage(20);
                        break;
                    case OPTION_2:
                        punishmentMenu();
                        break;
                    case OPTION_3:
                        otherOptionsMenu();
                        break;
                    case OPTION_4:
                        end();
                        break;
                }
                break;

            case 20:
                switch (componentId) {
                    case OPTION_1:
                        sendOptionsDialogue("Are you sure you want to kick " + targetName + "?", "Yes", "No");
                        goStage(100);
                        break;
                    case OPTION_2:
                        if (canPerformAction()) {
                            player.getControlerManager().removeControlerWithoutCheck();
                            player.resetCombat();
                            player.resetWalkSteps();
                            player.setNextWorldTile(new WorldTile(target));
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_3:
                        if (canPerformAction()) {
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.resetCombat();
                            target.resetWalkSteps();
                            target.setNextWorldTile(new WorldTile(player));
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_4:
                        if (canPerformAction()) {
                            target.setNextWorldTile(target.getHome());
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.unlock();
                            target.resetWalkSteps();
                            target.resetCombat();
                            player.sendMessage("You have sent " + targetName + " home.");
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 30:
                switch (componentId) {
                    case OPTION_1:
                        sendOptionsDialogue("Are you sure you want to kick " + targetName + "?", "Yes", "No");
                        goStage(100);
                        break;
                    case OPTION_2:
                        end();
                        player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                handlePunishment(PunishmentType.JAIL, PunishmentMethod.STANDARD, getInteger());
                            }
                        });
                        break;
                    case OPTION_3:
                        sendOptionsDialogue("Mute menu for " + targetName, "Mute", "IP mute (opens confirmation)", "Back...");
                        goStage(50);
                        break;
                    case OPTION_4:
                        sendOptionsDialogue("Ban menu for " + targetName, "Ban", "IP ban (opens confirmation)", "MAC ban (opens confirmation)", "Back...");
                        goStage(60);
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 40:
                switch (componentId) {
                    case OPTION_1:
                        if (canPerformAction()) {
                            if (target.getCurrentFriendChat() != null && target.getCurrentFriendChat().getOwnerName().equals("xhybrid")) {
                                target.getCurrentFriendChat().silentKickPlayerFromChat(target);
                                player.sendMessage("You have kicked " + targetName + " from the default server friends chat.");
                            } else {
                                player.sendMessage("This player is not currently in the default server friends chat.");
                            }
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_2:
                        if (canPerformAction()) {
                            if (target.getControlerManager().getControler() != null) {
                                target.getControlerManager().removeControlerWithoutCheck();
                                player.sendMessage("You removed their current controller.");
                            } else {
                                player.sendMessage(targetName + " is not within any active controller.");
                            }
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_3:
                        if (canPerformAction()) {
                            target.setNextWorldTile(target.getHome());
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.unlock();
                            target.resetWalkSteps();
                            target.resetCombat();
                            player.sendMessage("You have sent " + targetName + " home.");
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_4:
                        getSessionInfo();
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 50:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                handlePunishment(PunishmentType.MUTE, PunishmentMethod.STANDARD, getInteger());
                            }
                        });
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Are you sure you want to permanently IP mute " + targetName + "?", "Yes", "Back...");
                        goStage(101);
                        break;
                    case OPTION_3:
                        punishmentMenu();
                        break;
                }
                break;

            case 60:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                handlePunishment(PunishmentType.BAN, PunishmentMethod.STANDARD, getInteger());
                            }
                        });
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Are you sure you want to permanently IP ban " + targetName + "?", "Yes", "Back...");
                        goStage(102);
                        break;
                    case OPTION_3:
                        sendOptionsDialogue("Are you sure you want to permanently MAC ban " + targetName + "?", "Yes", "Back...");
                        goStage(103);
                        break;
                    case OPTION_4:
                        punishmentMenu();
                        break;
                }
                break;

            case 100: // Confirm kick
                switch (componentId) {
                    case OPTION_1:
                        if (canPerformAction()) {
                            target.forceLogout();
                            player.sendMessage("You have kicked " + targetName + ".");
                            end();
                        } else {
                            sendTargetError();
                        }
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 101: // Confirm IP mute
                switch (componentId) {
                    case OPTION_1:
                        mutePlayer(PunishmentMethod.IP, -1);
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 102: // Confirm IP ban
                switch (componentId) {
                    case OPTION_1:
                        banPlayer(PunishmentMethod.IP, -1);
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 103: // Confirm MAC ban
                switch (componentId) {
                    case OPTION_1:
                        banPlayer(PunishmentMethod.MAC, -1);
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 127:
                otherOptionsMenu();
                break;
        }
    }

    @Override
    public void finish() {
    }

    private void goStage(final int stage) {
        this.stage = (byte) stage;
    }

    private void mainMenu() {
        sendOptionsDialogue("Staff menu for " + targetName, "Assist", "Punish", "Other options", "Close menu");
        stage = 10;
    }

    private void punishmentMenu() {
        sendOptionsDialogue("Punishment menu for " + targetName, "Kick ", "Jail", "Mute (opens options)", "Ban (opens options)", "Back to main menu...");
        stage = 30;
    }

    private void otherOptionsMenu() {
        sendOptionsDialogue("Other options menu for " + targetName, "Kick from server FC", "Remove controller", "Send home / unnull", "Session info", "Back to main menu...");
        stage = 40;
    }

    private boolean canPerformAction() {
        return target != null && target.isRunning() && World.getPlayers().contains(target);
    }

    private void sendTargetError() {
        end();
        sendErrorDialogue();
    }

    private void getSessionInfo() {
        if (canPerformAction()) {
            sendDialogue(
                    "<u>Session info for " + targetName
                            + "<br><br>Logged in for: " + Utils.getTimePlayed(target.getRecordedPlayTime())
                            + (target.hasDisplayName() ? "<br>" + targetName + "'s username is: " + target.getUsername() : "")
                            + "<br>" + getOtherOnlineAccounts()
            );
            goStage(127);
        } else {
            sendTargetError();
        }
    }

    private String getOtherOnlineAccounts() {
        List<String> names = new ArrayList<>();
        for (Player p : World.getPlayers()) {
            if (p.getIP().equals(target.getIP()) && p != target) {
                names.add(p.getDisplayName());
            }
        }
        return names.isEmpty() ? targetName + " has no other accounts logged in." : "Online " + (names.size() == 1 ? "account" : "accounts") + " with matching IP: " + names;
    }

    private void handlePunishment(final PunishmentType punishmentType, final PunishmentMethod punishmentMethod, final int durationHours) {
        switch (punishmentType) {
            case JAIL:
                jailPlayer(durationHours);
                break;
            case MUTE:
                mutePlayer(punishmentMethod, durationHours);
                break;
            case BAN:
                banPlayer(punishmentMethod, durationHours);
                break;
        }
    }

    private void jailPlayer(final int durationHours) {
        final long duration = Utils.currentTimeMillis() + (durationHours * 60 * 60 * 1000);
        final boolean permanent = durationHours > 48;
        if (canPerformAction()) {
            if (player.getCurrentInstance() != null) {
                player.getCurrentInstance().destroyInstance();
            }
            target.setJailed(permanent ? Long.MAX_VALUE : duration);
            target.getControlerManager().removeControlerWithoutCheck();
            target.getControlerManager().startControler("JailController");
            SerializableFilesManager.savePlayer(target);
            player.sendMessage(permanent ? RED + "You have permanently jailed " + targetName + "." : "You have jailed " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
            target.sendMessage(RED + (permanent ? "You have been permanently jailed." : "You have been jailed for " + durationHours + (durationHours == 1 ? " hour." : " hours.")));
        } else {
            sendTargetError();
        }
    }

    private void mutePlayer(final PunishmentMethod punishmentMethod, final int durationHours) {
        final long duration = Utils.currentTimeMillis() + (durationHours * 60 * 60 * 1000);
        final boolean permanent = durationHours > 48;
        if (canPerformAction()) {
            if (punishmentMethod.equals(PunishmentMethod.STANDARD)) {
                target.setMuted(permanent ? Long.MAX_VALUE : duration);
                player.sendMessage(permanent ? RED + "You have permanently muted " + targetName + "." : "You have muted " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                target.sendMessage(RED + (permanent ? "You have been permanently muted." : "You have been muted for " + durationHours + (durationHours == 1 ? " hour." : " hours.")));
            } else {
                if (IPMute.ipMute(target, true)) {
                    player.sendMessage(RED + "You have IP muted " + targetName + ".");
                    target.sendMessage(RED + "You have been IP muted.");
                }
            }
            SerializableFilesManager.savePlayer(target);
        } else {
            sendTargetError();
        }
    }

    private void banPlayer(final PunishmentMethod punishmentMethod, final int durationHours) {
        final long duration = Utils.currentTimeMillis() + (durationHours * 60 * 60 * 1000);
        final boolean permanent = durationHours > 48;
        if (canPerformAction()) {
            if (punishmentMethod.equals(PunishmentMethod.STANDARD)) {
                if (permanent) {
                    target.setPermBanned(true);
                } else {
                    target.setBanned(duration);
                }
                SerializableFilesManager.savePlayer(target);
                target.forceLogout();
                player.sendMessage(permanent ? RED + "You have permanently banned " + targetName + "." : "You have banned " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
            } else if (punishmentMethod.equals(PunishmentMethod.IP)) {
                IPBanL.ban(target, true);
                player.sendMessage(RED + "You have IP banned " + targetName + ".");
            } else {
                MACBan.macban(target, true);
                player.sendMessage(RED + "You have MAC banned " + targetName + ".");
            }
        } else {
            sendTargetError();
        }
    }

    private void sendErrorDialogue() {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendDialogue("Something went wrong when attempting to do that,<br> perhaps the player logged out?");
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
        JAIL, MUTE, BAN
    }

    private enum PunishmentMethod {
        STANDARD, IP, MAC
    }

}
