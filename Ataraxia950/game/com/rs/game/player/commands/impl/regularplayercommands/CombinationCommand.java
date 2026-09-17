package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.interfaces.combinations.CombinationsInterface;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"combinations", "comb"},
        description = "Opens the combinations interface"
        )
public class CombinationCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getPackets().sendGameMessage("Good luck!");
        CombinationsInterface.sendInterface(player);
    }
}
