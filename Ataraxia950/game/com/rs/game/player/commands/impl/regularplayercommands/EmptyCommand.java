package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.World;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Lend;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"empty"},
        description = "empties your inventory"
        )
public class EmptyCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getWarnEmpty()) {
            final Lend lend = LendingManager.getLend(player);
            if (lend != null) {
                final Player lender = World.getPlayer(lend.getLendee());
                if (lender != null && lender.getInventory().containsOneItem(lend.getItem().getDefinitions().getLendId())) {
                    LendingManager.unLend(lend);
                }
            }
            player.getActionManager().forceStop();
            player.getInventory().reset();
            player.sendMessage("Successfully deleted inventory.");
        } else {
            player.getDialogueManager().startDialogue("EmptyD");
        }
    }
}
