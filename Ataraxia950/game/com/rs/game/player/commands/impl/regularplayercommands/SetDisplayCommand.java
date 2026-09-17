package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"setdisplay", "changename"},
        description = "lets your change your display name"
)
public class SetDisplayCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (!player.isLegendaryDonator() && !player.isOwner()) {
            player.sendMessage("You must be a Gold Member in order to use this command.");
            return;
        }
        if (player.isDisplayBanned()) {
            player.sendMessage(Colors.RED + "You are currently banned from using custom display names.");
            return;
        }
        if (player.getMoneySpent() < 500 && (Utils.currentTimeMillis() - player.displayNameChange) < (24 * 60 * 60 * 1000) && !player.isOwner()) { // 24
            final long toWait = (24 * 60 * 60 * 1000) - (Utils.currentTimeMillis() - player.displayNameChange);
            player.sendMessage("You must wait another " + Utils.millisecsToMinutes(toWait) + " " + "minutes to change your display name.");
            return;
        }
        player.sendInputName("Enter the display name you wish:", new InputNameEvent() {
            @Override
            public void run(final Player player) {
                final String value = getString();
                if (Utils.invalidAccountName(Utils.formatPlayerNameForProtocol(value))) {
                    player.getPackets().sendGameMessage("Invalid name.");
                    return;
                }
                if (!DisplayNames.setDisplayName(player, value)) {
                    player.getPackets().sendGameMessage("Name already in use!");
                    return;
                }
                DisplayNames.setDisplayName(player, value);
                player.displayNameChange = Utils.currentTimeMillis();
                player.getPackets().sendGameMessage("Display name successfully set to: " + player.getDisplayName() + ".");
            }
        });
    }
}
