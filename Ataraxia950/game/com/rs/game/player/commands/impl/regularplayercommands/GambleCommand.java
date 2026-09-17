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
        possibleCommands = {"gamble","dice","fp","flowerpoker","poker"},
        description = "Takes you to the gambling area"
        )
public class GambleCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getControlerManager().getControler() == null) {
            player.sendMessage(Colors.RED + "Join the Gambling Friends chat or risk being kicked from Ataraxia help chat");
            if (player.isATypeOfIronman()) {
                player.sendMessage("You cannot do this as an ironman!");
                return;
            }
            player.getControlerManager().startControler("GamblingAreaController");
        }
        return;

    }
}
