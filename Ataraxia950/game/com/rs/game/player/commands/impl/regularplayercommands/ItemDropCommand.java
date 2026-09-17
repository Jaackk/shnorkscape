package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.dropprediction.DropUtils;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"itemdrop"},
        description = "shows you which npc drops a certain item"
        )
public class ItemDropCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (args.length <= 1) {
            player.sendMessage("Usage is ;;itemdrop (item_name1) (item_name2) (item_name3) ..., example ;;itemdrop raw shark");
            return;
        }
        final StringBuilder itemName = new StringBuilder(args[1]);
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                itemName.append(" ").append(args[i]);
            }
        }
        DropUtils.sendItemDrops(player, itemName.toString());
    }
}
