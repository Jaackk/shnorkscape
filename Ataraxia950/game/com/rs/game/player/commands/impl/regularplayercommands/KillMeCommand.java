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
        possibleCommands = {"killme"},
        description = "kills you"
)
public class KillMeCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.isCanPvp()) {
            player.sendMessage("You can not do this in player-versus-player areas.");
            return;
        }
        if (player.isUnderCombat()) {
            player.sendMessage("You cannot do this while being under combat.");
            return;
        }
        if (!player.killme) {
            player.getDialogueManager().startDialogue("KillmeWarning");
        } else {
            player.killme();
        }
    }
}
