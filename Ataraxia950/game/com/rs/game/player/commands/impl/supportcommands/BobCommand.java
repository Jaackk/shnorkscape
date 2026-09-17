package com.rs.game.player.commands.impl.supportcommands;

import com.rs.game.World;
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
        rank = CommandRights.SUPPORT,
        possibleCommands = {"bob"},
        description = "players afk display"
)
public class BobCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        int afkCount = 0;
        int playerCount = World.getPlayersOnline();
        for (Player p : World.getPlayers()) {
            if (p == null)
                continue;
            if (p.isAFK())
                afkCount++;
        }
        if (afkCount == 0) {
            player.sendMessage(Colors.RED + "No one is AFK!");
        } else {
            int percentage = (int) (((double) afkCount / playerCount) * 100);
            player.sendMessage(Colors.RED + "Percentage of players AFK: " + percentage + "% (" + afkCount + "/" + playerCount + " players).");
        }
    }

}
