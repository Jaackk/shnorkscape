package com.rs.game.player.commands.impl.modcommands;

import com.google.common.primitives.Ints;
import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIMGroupKey;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.Commands;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.MODERATOR,
        possibleCommands = {"setgimdata"},
        description = "Sets the GIM data to match SQL data"
)
public final class SetGIMDataCommand extends Command {

    @Data
    private static final class GIMData {
        private final String groupName;
        private final String groupKey;
        private final Set<String> members;
    }

    @RequiredArgsConstructor
    private static final class RetrieveGIMDataSql extends SQLRunnable implements Callable<GIMData> {
        private final int groupId;
        private GIMData data;

        @Override
        public void execute(DatabaseCredential auth) {
            try (Connection c = Pool.getConnection(auth, "ataraxia");
                 PreparedStatement selectGroup = c.prepareStatement("SELECT * FROM gim_group_data WHERE group_id = ?;");
                 PreparedStatement selectMembers = c.prepareStatement("SELECT * FROM gim_group_score WHERE group_id = ?;")) {
                selectGroup.setInt(1, groupId);
                String groupName = null;
                String groupKey = null;
                try (ResultSet results = selectGroup.executeQuery()) {
                    if (results.next()) {
                        groupName = results.getString("group_name");
                        groupKey = results.getString("group_key");
                    }
                }

                selectMembers.setInt(1, groupId);
                Set<String> members = new HashSet<>(6);
                try (ResultSet results = selectGroup.executeQuery()) {
                    while (results.next()) {
                        members.add(results.getString("member_name"));
                    }
                }
                data = new GIMData(groupName, groupKey, members);
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }

        @Override
        public GIMData call() throws Exception {
            prepare();
            if (data == null) {
                throw new IllegalStateException("Data not computed!");
            }
            return data;
        }
    }

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        Integer groupId = Ints.tryParse(args[1]);
        if (groupId == null) {
            player.sendMessage("Argument must be a valid integer (usage: ;;setgimdata <group_id>).");
            return;
        }
        sendRequest(player, groupId);
    }

    private void sendRequest(Player player, int groupId) {
        Future<GIMData> setDataResult = CoresManager.getServiceProvider().submitNow(new RetrieveGIMDataSql(groupId));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (setDataResult.isDone()) {
                    stop();
                    GIMData loadedData = null;
                    try {
                        loadedData = setDataResult.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.getGlobal().catching(e);
                    }
                    if (loadedData == null) {
                        player.sendMessage(Colors.PINK + "Developer: The database request errored!");
                        return;
                    }
                    final GIMData newData = loadedData;
                    for (String username : newData.members) {
                        Commands.doCommandOnPlayerUsername(player, username, (plr, fileLoaded) -> {
                            plr.gimKey = new GIMGroupKey(groupId, newData.groupKey);
                            plr.gimName = newData.groupName;
                        });
                    }
                    player.sendMessage(Colors.PINK + "Developer: The database request completed successfully.");
                }
            }
        }, 2, 1);
    }
}
