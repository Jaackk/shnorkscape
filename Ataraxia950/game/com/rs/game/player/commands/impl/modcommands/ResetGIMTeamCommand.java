package com.rs.game.player.commands.impl.modcommands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Future;

import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import lombok.AllArgsConstructor;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.MODERATOR,
        possibleCommands = {"resetgimteam"},
        description = "resets an entire team's GIM score to 0"
)
public final class ResetGIMTeamCommand extends Command {

    @AllArgsConstructor
    private static final class ResetGIMTeamSQL extends SQLRunnable {
        private final int groupId;

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "ataraxia");
                 PreparedStatement updateMembers = c.prepareStatement("UPDATE gim_group_score SET total_xp = 0, total_deaths = 0, total_boss_points = 0, total_levels = 0 WHERE group_id = ?;")) {
                updateMembers.setInt(1, groupId);
                if (updateMembers.executeUpdate() < 1) {
                    throw new IllegalStateException("Team was not reset because it does not exist.");
                }
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendDialogue("Please enter the name of the group you would like to reset. " + Colors.DARK_RED + "This cannot be undone!</col>");
                stage = 0;
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case 0:
                        player.sendInputString("Enter the group name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String groupKey = getString().toLowerCase().trim();
                                GIMGroup group = GIM.getGroupData().get(groupKey);
                                if (group == null) {
                                    sendDialogue("Group '" + groupKey + "' not found!");
                                    stage = 1;
                                    return;
                                }
                                Dialogue.sendSingleDialogue(player,"A request has been sent to the database. You will be notified in the chat box when it completes.");
                                sendRequest(player, group.getGroupId());
                            }
                        });
                        break;
                    case 1:
                        end();
                        break;
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    private void sendRequest(Player player, int groupId) {
        Future<?> resetTask = CoresManager.getServiceProvider().executeNow(new ResetGIMTeamSQL(groupId));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (resetTask.isDone()) {
                    stop();
                    GIM.getHighscores().requestRefresh();
                    player.sendMessage(Colors.PINK + "Developer: The database request completed successfully.");
                }
            }
        }, 2, 1);
    }
}
