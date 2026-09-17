package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.Magic;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"home","h","respawn"},
        description = "Teleports you back to the home area"
)
public class HomeCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
      Magic.vineTeleport(player, new WorldTile(5414, 2339, 0));
    }
}
