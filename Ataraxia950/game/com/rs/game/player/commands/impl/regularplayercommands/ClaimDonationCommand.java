package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.Donations;
import com.rs.utils.Logger;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"redeem", "donated", "receive","claimdonation","reward","claim"},
        description = "Claims your donation"
        )
public class ClaimDonationCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.isKingOfTheSkillGameMode()) {
            player.getKingOfTheSkillGameModeHandler().sendNoDonationsMessageToPlayer();
            return;
        }
        if (player.getControlerManager().getControler() != null) {
            player.sendMessage("You can't claim your donation here.");
            return;
        }
        try {
            Donations.rspsdata(player, player.getUsername());
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
