package com.rs.game.player.commands.impl.supportcommands;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.SUPPORT,
        possibleCommands = {"getlogin"},
        description = "gets the login of a player"
)
public class GetLoginCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        StringBuilder name = new StringBuilder(getRestOfInput(1, args));
        Player other = World.getPlayerByDisplayName(name.toString());
        if (other == null) {
            player.sendMessage("The player must be logged on to check their actual login name.");
        } else {
            player.sendMessage("The player's actual login name is: " + other.getUsername());
        }
    }

}
