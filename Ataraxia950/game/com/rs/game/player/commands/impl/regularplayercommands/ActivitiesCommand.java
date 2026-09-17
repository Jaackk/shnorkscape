package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.activities.ActivitiesScheduler;
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
        possibleCommands = {"activities", "events","schedule"},
        description = "displays the event schedule"
        )
public class ActivitiesCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        ActivitiesScheduler.getInstance().sendScheduleInterface(player);
    }
}
