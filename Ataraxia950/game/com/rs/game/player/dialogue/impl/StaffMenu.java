package com.rs.game.player.dialogue.impl;


import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.rs.utils.Colors.RED;
import static com.rs.utils.Colors.SHAD;

/**
 * @author Toby/Xenthium
 * 23/02/19
 */

public class StaffMenu extends Dialogue {

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
                        sendOptionsDialogue("Assist menu for " + targetName, "Kick", "Teleport to", "Teleport to me", "Send home / unnull", "Back to main menu...");
                        goStage(20);
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Punishment menu for " + targetName, "Kick", "Jail", "Mute", "Ban", "Back to main menu...");
                        goStage(30);
                        break;
                    case OPTION_3:
                        sendOptionsDialogue("Other options menu for " + targetName, "Kick from server FC", "Remove controller", "Send home / unnull", "Session info", "Back to main menu...");
                        goStage(40);
                        break;
                    case OPTION_4:
                        end();
                        break;
                }
                break;

            case 20:
                switch (componentId) {
                    case OPTION_1:
                        if (targetExists()) {
                            sendOptionsDialogue("Are you sure you want to kick " + targetName + "?", "Yes", "No");
                            goStage(100);
                        } else {
                            sendPlayerNotOnlineError();
                            end();
                        }
                        break;
                    case OPTION_2:
                        if (targetExists()) {
                            player.getControlerManager().removeControlerWithoutCheck();
                            player.resetCombat();
                            player.resetWalkSteps();
                            player.setNextWorldTile(new WorldTile(target));
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_3:
                        if (targetExists()) {
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.resetCombat();
                            target.resetWalkSteps();
                            target.setNextWorldTile(new WorldTile(player));
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_4:
                        if (targetExists()) {
                            target.setNextWorldTile(target.getHome());
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.unlock();
                            target.resetWalkSteps();
                            target.resetCombat();
                            player.sendMessage("You have sent " + targetName + " home.");
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 30:
                switch (componentId) {
                    case OPTION_1:
                        if (targetExists()) {
                            sendOptionsDialogue("Are you sure you want to kick " + targetName + "?", "Yes", "No");
                            goStage(100);
                        } else {
                            sendPlayerNotOnlineError();
                            end();
                        }
                        break;
                    case OPTION_2:
                    case OPTION_3:
                    case OPTION_4:
                        if (targetExists()) {
                            player.sendInputInteger("Enter the duration in hours (1-48).<br>Input a value above 48 to apply it permanently.", new InputIntegerEvent() {
                                @Override
                                public void run(Player player) {
                                    handlePunishment((componentId == OPTION_2 ? PunishmentType.JAIL : componentId == OPTION_3 ? PunishmentType.MUTE : PunishmentType.BAN), getInteger());
                                }
                            });
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 40:
                switch (componentId) {
                    case OPTION_1:
                        if (targetExists()) {
                            if (target.getCurrentFriendChat() != null && target.getCurrentFriendChat().getOwnerName().equals("xhybrid")) {
                                target.getCurrentFriendChat().silentKickPlayerFromChat(target);
                                player.sendMessage("You have kicked " + targetName + " from the default server friends chat.");
                            } else {
                                player.sendMessage("This player is not currently in the default server friends chat.");
                            }
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_2:
                        if (targetExists()) {
                            if (target.getControlerManager().getControler() != null) {
                                target.getControlerManager().removeControlerWithoutCheck();
                                player.sendMessage("You removed their current controller.");
                            } else {
                                player.sendMessage(targetName + " is not within any active controller.");
                            }
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_3:
                        if (targetExists()) {
                            target.setNextWorldTile(target.getHome());
                            target.getControlerManager().removeControlerWithoutCheck();
                            target.unlock();
                            target.resetWalkSteps();
                            target.resetCombat();
                            player.sendMessage("You have sent " + targetName + " home.");
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_4:
                        if (targetExists()) {
                            getSessionInfo();
                        } else {
                            sendPlayerNotOnlineError();
                            end();
                        }
                        break;
                    case OPTION_5:
                        mainMenu();
                        break;
                }
                break;

            case 100:
                switch (componentId) {
                    case OPTION_1:
                        if (targetExists()) {
                            if (target.getFlowerPokerSession() != null) {
                                end();
                                player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a flower poker session!");
                                return;
                            }
                            target.forceLogout();
                            player.sendMessage("You have kicked " + targetName + ".");
                        } else {
                            sendPlayerNotOnlineError();
                        }
                        end();
                        break;
                    case OPTION_2:
                        mainMenu();
                        break;
                }
                break;

            case 127:
                sendOptionsDialogue("Other options menu for " + targetName, "Kick from server FC", "Remove controller", "Send home / unnull", "Session info", "Back to main menu...");
                goStage(40);
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

    /**
     * Used to prevent errors if the target logs out while the player is navigating the menu.
     */
    private boolean targetExists() {
        return target != null && target.isRunning() && World.containsPlayer(target.getUsername());
    }

    private void sendPlayerNotOnlineError() {
        player.sendMessage(RED + "Uh oh, it looks like something went wrong when attempting to do that, perhaps the player logged out?");
    }

    private void getSessionInfo() {
        sendDialogue(
                Colors.wrap(RED + SHAD, "Session info for " + targetName)
                + "<br><br>Logged in for: " + Utils.getTimePlayed(target.getRecordedPlayTime())
                + (target.hasDisplayName() ? "<br>" + targetName + "'s username is: " + target.getUsername() : "")
                + "<br>" + getOtherOnlineAccounts()
        );
        goStage(127);
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

    private void handlePunishment(final PunishmentType punishmentType, final int durationHours) {
        final long duration = Utils.currentTimeMillis() + (durationHours * 60 * 60 * 1000);
        switch (punishmentType) {
            case JAIL:
                if (targetExists()) {
                    if (durationHours > 0 && durationHours < 49) {
                        target.getControlerManager().removeControlerWithoutCheck();
                        target.setJailed(duration);
                        target.getControlerManager().startControler("JailController");
                        SerializableFilesManager.savePlayer(target);
                        player.sendMessage("You have jailed " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                        target.sendMessage(Colors.RED + "You have been jailed for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                    } else if (durationHours > 48) {
                        target.getControlerManager().removeControlerWithoutCheck();
                        target.setJailed(Long.MAX_VALUE);
                        target.getControlerManager().startControler("JailController");
                        SerializableFilesManager.savePlayer(target);
                        player.sendMessage("You have permanently jailed " + targetName + ".");
                        target.sendMessage(Colors.RED + "You have been permanently jailed.");
                    }
                } else {
                    sendPlayerNotOnlineError();
                }
                break;
            case MUTE:
                if (targetExists()) {
                    if (durationHours > 0 && durationHours < 49) {
                        target.setMuted(duration);
                        player.sendMessage("You have muted " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                        target.sendMessage(Colors.RED + "You have been muted for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                    } else if (durationHours > 48) {
                        target.setMuted(Long.MAX_VALUE);
                        player.sendMessage("You have permanently muted " + targetName + ".");
                        target.sendMessage(Colors.RED + "You have been permanently muted.");
                    }
                } else {
                    sendPlayerNotOnlineError();
                }
                break;
            case BAN:
                if (targetExists()) {
                    if (durationHours > 0 && durationHours < 49) {
                        target.setBanned(duration);
                        SerializableFilesManager.savePlayer(target);
                        target.forceLogout();
                        player.sendMessage("You have banned " + targetName + " for " + durationHours + (durationHours == 1 ? " hour." : " hours."));
                    } else if (durationHours > 48) {
                        target.setPermBanned(true);
                        SerializableFilesManager.savePlayer(target);
                        target.forceLogout();
                        player.sendMessage(Colors.RED + "You have permanently banned " + targetName + ".");
                    }
                } else {
                    sendPlayerNotOnlineError();
                }
                break;
        }
    }

    private enum PunishmentType {
        JAIL, MUTE, BAN
    }

}
