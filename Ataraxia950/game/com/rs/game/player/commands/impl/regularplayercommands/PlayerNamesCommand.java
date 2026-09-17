package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.interfaces.clientsettings.PlayerGameSettings;
import com.rs.game.player.content.interfaces.clientsettings.PlayerGameSettingsInterface;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"playernames"},
        description = "toggles playernames names above player heads"
        )
public class PlayerNamesCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        boolean value = false;
        if(player.getGameSettings().containsKey(PlayerGameSettings.PLAYER_NAMES))
             value = player.getGameSettings().get(PlayerGameSettings.PLAYER_NAMES);
        PlayerGameSettingsInterface.update(player, PlayerGameSettings.PLAYER_NAMES, !value);
    }
}
