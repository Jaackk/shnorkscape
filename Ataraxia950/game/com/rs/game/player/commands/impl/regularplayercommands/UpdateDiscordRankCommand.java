package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.Settings;
import com.rs.game.World;
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
        possibleCommands = {"updaterank"},
        description = "Lets you update your discord rank"
        )
public class UpdateDiscordRankCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (Settings.TEST_SERVER_MODE) {
            player.sendMessage("Nice try lol.");
            return;
        }
        }
    }

