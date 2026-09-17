package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.CheckVote;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"voted"},
        description = "claims your vote reward"
)
public class VotedCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.isKingOfTheSkillGameMode()) {
            player.getKingOfTheSkillGameModeHandler().sendNoVotesMessageToPlayer();
        }
        QueryExecutor.submit(new CheckVote(player));
    }
}
