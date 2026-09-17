package com.rs.game.player.commands.impl.regularplayercommands;

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
        possibleCommands = {"reset"},
        description = "Resets your duo slayer partner"
        )
public class ResetDuoslayerCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getSlayerPartner() == null) {
            player.sendMessage(Colors.RED + Colors.SHAD + "You currently don't have a co-op slayer partner!", false);
        } else {
            player.coOpSlayer.cleanConfig(player, Colors.SALMON + "You have reset your duo-slayer configuration!</col>");
        }
    }
}
