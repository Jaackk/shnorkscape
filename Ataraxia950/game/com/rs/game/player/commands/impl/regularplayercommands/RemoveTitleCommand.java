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
        possibleCommands = {"resettitle","removetitle"},
        description = "Removes your current title"
        )
public class RemoveTitleCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getAppearence().setTitle(0);
        player.getAppearence().generateAppearenceData();
        player.getDialogueManager().startDialogue("SimpleMessage", "Your Loyalty title has been cleared.");
    }
}
