package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"materials", "bag"},
        description = "displays invention bag of materials"
        )
public class OpenBagOfMaterialsCommand extends Command{

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getInventionManager().openBagOfMaterialsInterface();
    }

}
