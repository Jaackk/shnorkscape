package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.AchievementInterface;
import com.rs.game.player.content.interfaces.skillinginterface.SkillingInterface;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"skillingteleport", "sp"},
        description = "open the skillingteleport interface"
        )
public class SkillingTeleportCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        SkillingInterface.sendInterface(player);
    }
}
