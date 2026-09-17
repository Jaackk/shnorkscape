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
        possibleCommands = {"hidepets"},
        description = "toggles the hiding of pets and familiars"
        )
public class HidePetsCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.setHidePets(!player.isHidePets());
        player.sm("Pets are now hidden: " + player.isHidePets());
        player.getLocalNPCUpdate().reset();
    }
}
