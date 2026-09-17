package com.rs.game.player.commands.impl.regularplayercommands;

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
        possibleCommands = {"title"},
        description = "Lets you set a title if you are an extreme donator"
)
public class TitleCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (!player.isExtremeDonator()) {
            player.sendMessage("You need to be an Extreme Donator to use this command! Type ::donate.");
            return;
        }
        if (args.length != 2) {
            player.sendMessage("Usage ;;title <new title>.");
            return;
        }
        try {
            final int id = Integer.parseInt(args[1]);
            if (id != 2000) {
                if (id < 1 || id > 88) {
                    player.sendMessage("Title ID can only be 1-88; your title can be cleared at 'Xuan'.");
                    return;
                }
            } else if (!player.getUsername().equalsIgnoreCase("hohenheim")) {
                return;
            }
            player.getAppearence().setTitle(id);
            player.getAppearence().generateAppearenceData();
        } catch (NumberFormatException e) {
            player.sendMessage("There was a problem when setting your title.");
        }
    }
}
