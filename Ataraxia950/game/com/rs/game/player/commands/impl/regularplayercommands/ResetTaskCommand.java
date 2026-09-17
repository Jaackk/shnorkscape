package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.PerkManager.DonationPerk;
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
        possibleCommands = {"ressettask", "removetask","rt"},
        description = "Resets your slayertask if you have the perslaysion perk"
        )
public class ResetTaskCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (!player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
            player.sendMessage("Purchase the Perslaysion perk from the ::store to use this command.", false);
            return;
        }

        if (player.getTask() == null) {
            player.sendMessage("You don't have a task to reset.");
            return ;
        }
        player.setTask(null);
        player.sendMessage("Your slayer task has been reset, speak to a slayer master to get a new one.", true);
        return ;

    }
}
