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
        possibleCommands = {"dropcatcher"},
        description = "configure the dropcatcher"
        )
public class DropCatcherCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.isOwner())
            player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
        if (player.getPerkManager().hasPerkActive(DonationPerk.DROP_CATCHER)) {
            player.getDialogueManager().startDialogue("DropCatcherManagerD");
        } else {
            player.sendMessage("You must have purchased the drop catcher perk to use this command.");
        }
    }
}
