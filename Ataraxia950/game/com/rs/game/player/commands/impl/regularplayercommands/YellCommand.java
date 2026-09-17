package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.YellManager;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"yell", "y"},
        description = "global yell"
        )
public class YellCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        String inputLine1 = "";
        for (int i = 1; i < args.length; i++) {
            inputLine1 += args[i] + ((i == args.length - 1) ? "" : " ");
        }
        YellManager.sendYell(player, Utils.fixChatMessage(inputLine1));
    }
}
