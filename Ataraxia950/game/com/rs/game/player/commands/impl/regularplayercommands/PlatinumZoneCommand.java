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
        possibleCommands = {"pz","platinumzone","pmz","platzone"},
        description = "Teleports you to the platinum zone"
        )
public class PlatinumZoneCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if(player.isGroupIronman()) {
            player.sendMessage("Group Ironmen cannot go here.");
            return;
        }
        if (player.getMoneySpent() < 250 && !player.isOwner() && !player.isDeveloper() && !player.isFakeDev()) {
            player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a platinum donator to use this command!");
            return;
        }
        Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(4128, 5848, 0));
    }
}
