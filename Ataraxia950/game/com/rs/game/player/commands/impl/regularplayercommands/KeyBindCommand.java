package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.activities.ActivitiesScheduler;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.interfaces.keybinds.KeyBindInterface;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"keybind","keybinds"},
        description = "Opens the keybind interface"
        )
public class KeyBindCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        KeyBindInterface.sendInterface(player);
    }
}
