package com.rs.game.player.commands.impl.modcommands;

import com.rs.game.activites.gim.GIM;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.MODERATOR,
        possibleCommands = {"lookupgimteam"},
        description = "search a group"
)
public final class LookupGIMPlayerCommand extends Command {
    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        GIM.getHighscores().searchPlayer(player);
    }
}
