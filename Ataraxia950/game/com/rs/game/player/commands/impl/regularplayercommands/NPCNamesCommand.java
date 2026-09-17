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
        possibleCommands = {"npcnames"},
        description = "toggles npc names above their head"
        )
public class NPCNamesCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        boolean value = false;
        if(player.getGameSettings().containsKey(PlayerGameSettings.NPC_NAMES))
             value = player.getGameSettings().get(PlayerGameSettings.NPC_NAMES);
        PlayerGameSettingsInterface.update(player, PlayerGameSettings.NPC_NAMES, !value);
    }
}
