package com.rs.game.player.commands.impl.regularplayercommands;


import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.titles.PlayerTitleHandler;


/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"testxd"},
        description = "Opens the titles interface"
        )
public class TitlesInterfaceCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        PlayerTitleHandler.sendInterface(player);
    }
}
