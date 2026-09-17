package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimerInterface;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"pots", "potions"},
        description = "Hides/Displays the potion timers"
)
public class PotionTimersCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.setShowPotionTimers(!player.isShowPotionTimers());
//        if (!player.isShowPotionTimers()) TODO RS3
//            player.getPackets().closeInterface(player.getInterfaceManager().isResizableScreen() ? PotionTimerInterface.RESIZABLE_TAB : PotionTimerInterface.FIXED_TAB);
        player.sm("Potion timers are now " + (player.isShowPotionTimers() ? "visible" : "hidden") + ".");
    }
}
