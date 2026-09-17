package com.rs.game.player.commands.impl.modcommands;

import clojure.main;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMGroupKey;
import com.rs.game.activites.gim.GIMKickPlayerSql;
import com.rs.game.activites.gim.GIMTransferLeadershipSql;
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.MODERATOR,
        possibleCommands = {"changegimleader"},
        description = "transfers the GIM team leadership to another player"
)
public final class ChangeGIMLeaderCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            GIMGroup group;

            @Override
            public void start() {
                sendDialogue("Please enter the current leader's group name.");
                stage = 0;
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case -1:
                        end();
                        break;
                    case 0:
                        player.sendInputString("Enter the group name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String groupKey = getString().toLowerCase().trim();
                                group = GIM.getGroupData().get(groupKey);
                                if (group == null) {
                                    sendDialogue("No record of a group with the name '" + groupKey + "' was found.");
                                    stage = -1;
                                    return;
                                }
                                sendDialogue("Now please enter the name of the new leader for that group.");
                                stage = 1;
                            }
                        });
                        break;
                    case 1:
                        player.sendInputString("Enter the new leader's name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String newLeader = DisplayNames.getUsername(getString().toLowerCase().trim());
                                String response = GIM.canTransferLeadership(group, newLeader, true);
                                if (response != null) {
                                    sendDialogue(response);
                                    stage = -1;
                                    return;
                                }
                                Dialogue.sendSingleDialogue(player, "A request has been sent to the database. You will be notified in the chat box when it completes.");
                                sendRequest(player, group.getGroupId(), newLeader);
                            }
                        });
                        break;
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    private void sendRequest(Player player, int groupId, String newLeader) {
        Future<?> transferTask = CoresManager.getServiceProvider().runNow(new GIMTransferLeadershipSql(groupId, newLeader));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (transferTask.isDone()) {
                    stop();
                    boolean failed;
                    try {
                        transferTask.get();
                        failed = false;
                    } catch (InterruptedException | ExecutionException e) {
                        failed = true;
                        Logger.getGlobal().error("Error while kicking player from GIM group!", e);
                    }
                    if (failed) {
                        player.sendMessage(Colors.PINK + "Developer: The database request failed. Do not try again and please report this to lare96.");
                    } else {
                        player.sendMessage(Colors.PINK + "Developer: The database request completed successfully.");
                    }
                }
            }
        }, 2, 1);
    }
}
