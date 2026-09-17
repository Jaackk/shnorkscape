package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Colors;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"players"},
        description = "Displays the player amount"
)
public class PlayerCountCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.sendMessage("There are currently [" + Colors.RED + World.getPlayersOnline() + "</col>] players online.");
        if (player.isDonator() || Settings.DEBUG) {
            World.playersList(player, false);
        }
    }
}
