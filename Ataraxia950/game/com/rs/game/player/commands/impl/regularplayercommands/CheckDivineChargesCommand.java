package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Utils;

@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"divinecharges", "charges"},
        description = "displays invention bag of materials"
        )
public class CheckDivineChargesCommand extends Command{

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getPackets().sendGameMessage("You currently have "+Utils.getFormattedNumber(player.getInventionManager().getDivineCharges() / 3000)+" divine charges stored in your charge pack.");
    }

}
