package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.MetalBank;

@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = { "metalbank", "depositmetal", "withdrawmetal" },
        description = "manage stored smithing ores and bars"
        )
public class MetalBankCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if ("depositmetal".equals(command)) {
            MetalBank.depositInventory(player);
            return;
        }
        if ("withdrawmetal".equals(command)) {
            MetalBank.withdraw(player, args);
            return;
        }
        MetalBank.show(player);
    }
}
