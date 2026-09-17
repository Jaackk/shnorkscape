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
        possibleCommands = {"skipdtcutscene"},
        description = "Lets you skip the dt cutscenes"
        )
public class SkipdtCutsceneCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (!player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION)) {
            player.sendMessage("You must have the \"Dominion Domination\" perk in order to use this command.");
            return;
        }
        player.getDominionTower().setSkipCutscenes(!player.getDominionTower().isSkipCutscenes());
        String skippingText = player.getDominionTower().isSkipCutscenes() ? "skipping" : "not skipping";
        player.sendMessage("You are now " + skippingText + " dominion tower cutscenes.");
    }
}
