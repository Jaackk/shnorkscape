package com.rs.game.player.commands.impl.modcommands;

import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMGroupKey;
import com.rs.game.activites.gim.GIMKickPlayerSql;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.SUPPORT,
        possibleCommands = {"kickgimplayer"},
        description = "force kicks a GIM player from their team"
)
public final class KickGIMPlayerCommand extends Command {

    private enum RequestType {
        KICK,
        LEAVE
    }

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            private RequestType type;

            @Override
            public void start() {
                int kickReqs = GIM.getKickRequests().size();
                int leaveReqs = GIM.getLeaveRequests().size();
                sendOptionsDialogue("Select an option.",
                        "Force kick player",
                        "Accept kick request(" + kickReqs + ")",
                        "Accept leave request(" + leaveReqs + ")");
                stage = 0;
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case -1:
                        player.sendInputString("Enter the group name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String groupKey = getString().toLowerCase().trim();
                                GIMGroup group = GIM.getGroupData().get(groupKey);
                                if (group == null) {
                                    sendDialogue("Unable to fetch group data for '" + groupKey + "'.");
                                    stage = 2;
                                    return;
                                }
                                if (group.getMembers().size() <= 2) {
                                    sendDialogue("That group only has 2 members.");
                                    stage = 2;
                                    return;
                                }

                                String requestMember = null;
                                switch (type) {
                                    case KICK:
                                        requestMember = GIM.getKickRequests().get(groupKey);
                                        break;
                                    case LEAVE:
                                        Iterator<String> iter = GIM.getLeaveRequests().get(groupKey).iterator();
                                        while(iter.hasNext()) {
                                            requestMember = iter.next();
                                            iter.remove();
                                            break;
                                        }
                                        break;
                                }
                                if (requestMember == null) {
                                    sendDialogue("No request for group '" + groupKey + "' was found!");
                                    stage = 2;
                                    return;
                                }

                                Dialogue.sendSingleDialogue(player, "A request has been sent to the database. You will be notified in the chat box when it completes.");
                                sendRequest(player, groupKey, requestMember);
                            }
                        });
                        break;
                    case 0:
                        if (componentId == OPTION_1) {
                            sendDialogue("Please enter the name of the player you would like to kick.");
                            stage = 1;
                        } else if (componentId == OPTION_2) {
                            sendDialogue("Please enter the name of the group whose kick request you wish to accept.");
                            stage = -1;
                            type = RequestType.KICK;
                        } else if (componentId == OPTION_3) {
                            sendDialogue("Please enter the name of the group whose leave request you wish to accept.");
                            stage = -1;
                            type = RequestType.LEAVE;
                        }
                        break;
                    case 1:
                        player.sendInputString("Enter the player name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String memberDisplayName = getString().toLowerCase().trim();
                                String memberUsername = DisplayNames.getUsername(memberDisplayName);
                                GIMGroup group = GIM.getGroupMemberData().get(memberUsername);
                                if (group == null) {
                                    sendDialogue("GIM player '" + memberDisplayName + "' was not found!");
                                    stage = 2;
                                    return;
                                } else if (memberUsername.equals(group.getLeaderName())) {
                                    sendDialogue("The group leader cannot be kicked. Transfer leadership to someone else in the group using ;;changegimleader first.");
                                    stage = 2;
                                    return;
                                } else if (group.getMembers().size() <= 2) {
                                    sendDialogue("That group only has 2 members.");
                                    stage = 2;
                                    return;
                                }
                                Dialogue.sendSingleDialogue(player, "A request has been sent to the database. You will be notified in the chat box when it completes.");
                                sendRequest(player, group.getGroupKey(), memberUsername);
                            }
                        });
                        break;
                    case 2:
                        end();
                        break;
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    private void sendRequest(Player player, String groupKey, String memberUsername) {
        Future<?> kickTask = CoresManager.getServiceProvider().runNow(() -> {
            GIMKickPlayerSql kickPlayerSql = new GIMKickPlayerSql(memberUsername);
            kickPlayerSql.prepare();

            final Player kickedPlayer = World.getPlayer(memberUsername);
            if (kickedPlayer != null) {
                CoresManager.getServiceProvider().addGameTask(() -> {
                    GIM.sendPlayerMsg(kickedPlayer, "You have been kicked from your group.");
                    kickedPlayer.gimName = "";
                    kickedPlayer.gimKey = new GIMGroupKey(-1, "");
                    SerializableFilesManager.savePlayer(memberUsername, kickedPlayer);
                });
            } else {
                Player loadedPlayer = SerializableFilesManager.loadPlayer(memberUsername);
                if (loadedPlayer == null) {
                    throw new IllegalStateException("Could not find character file for '" + memberUsername + "'");
                }
                loadedPlayer.gimName = "";
                loadedPlayer.gimKey = new GIMGroupKey(-1, "");
                SerializableFilesManager.savePlayer(memberUsername, loadedPlayer);
            }
        });

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (kickTask.isDone()) {
                    stop();
                    boolean failed;
                    try {
                        kickTask.get();
                        failed = false;
                    } catch (InterruptedException | ExecutionException e) {
                        failed = true;
                        Logger.getGlobal().error("Error while kicking player from GIM group!", e);
                    }
                    if (failed) {
                        player.sendMessage(Colors.PINK + "Developer: The database request failed. Do not try again and please report this to lare96.");
                    } else {
                        GIM.getKickRequests().remove(groupKey, memberUsername);
                        GIM.getLeaveRequests().remove(groupKey, memberUsername);
                        GIM.getHighscores().requestRefresh();
                        player.sendMessage(Colors.PINK + "Developer: The database request completed successfully.");
                    }
                }
            }
        }, 2, 1);
    }
}
