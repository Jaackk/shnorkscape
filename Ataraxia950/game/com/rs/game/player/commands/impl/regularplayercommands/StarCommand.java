package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.World;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
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
        possibleCommands = {"star"},
        description = "Teleports you to the active crashed star"
        )
public class StarCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (!player.getPerkManager().hasPerkActive(DonationPerk.THE_STARGAZER)) {
            player.sm("Purchase the Stargazer perk to use this command.");
            return;
        }
        if (World.activeStar && ShootingStar.star != null) {
                Magic.sendNormalTeleportSpell(player, 0, 0, ShootingStar.star.getLastWorldTile(), true);
        } else {
            player.sendMessage("There isn't any active star at the moment.");
        }
    }
}
