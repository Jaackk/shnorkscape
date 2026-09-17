package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.Magic;
import com.rs.utils.Colors;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"dz","diamondzone"},
        description = "Teleports you to the diamondzone"
        )
public class DiamondzoneCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if(player.isGroupIronman()) {
            player.sendMessage("Group Ironmen cannot go here.");
            return;
        }
        if (player.isDiamondDonor() || player.isPlayerOfTheMonth()) {
            Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(4128, 5848, 0));
        } else {
            player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a Diamond donator to use this command!");
        }
    }
}
