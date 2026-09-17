package com.rs.game.player.commands.impl.regularplayercommands;

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
        rank = CommandRights.NORMAL,
        possibleCommands = {"cosmetics"},
        description = "opens up the cosmetic interface"
)
public class CosmeticsCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getInterfaceManager().openMenu(1, 2);
    }
}
