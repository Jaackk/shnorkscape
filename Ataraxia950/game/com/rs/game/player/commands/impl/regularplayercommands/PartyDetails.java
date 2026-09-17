package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"pd", "partydetails"},
        description = "opens party details game tab."
        )
public class PartyDetails extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.openPartyInterface();
    }


}
