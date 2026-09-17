package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.AccountInterfaceManager;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"settings"},
        description = "open the settings interface"
        )
public class SettingsCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getInterfaceManager().containsChatBoxInter()) {
            player.getInterfaceManager().closeChatBoxInterface();
        }

        if (player.getInterfaceManager().containsInventoryInter()) {
            player.getInterfaceManager().closeInventoryInterface();
        }

        AccountInterfaceManager.sendInterface(player);
    }
}
