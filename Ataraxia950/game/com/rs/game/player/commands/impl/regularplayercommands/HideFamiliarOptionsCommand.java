package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

@CommandInfo(rank = CommandRights.NORMAL, possibleCommands = { "hidefamiliaroptions", "hideoptions" }, description = "toggles the hiding of pets and familiars options")
public class HideFamiliarOptionsCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.switchHideFamiliarOptions();
        player.sm("Familiar/Pets options are now hidden: " + player.isHideFamiliarOptions());
    }

}
