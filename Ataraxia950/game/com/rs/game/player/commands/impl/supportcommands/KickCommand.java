package com.rs.game.player.commands.impl.supportcommands;

import com.rs.game.World;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.SUPPORT,
        possibleCommands = {"kick"},
        description = "Kicks a given player"
)
public class KickCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            name.append(args[i]).append((i == args.length - 1) ? "" : " ");
        }
        World.removePlayerLobby(name.toString());
        Player target = World.getPlayerByDisplayName(name.toString());
        if (target == null) {
            player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
            return;
        }
        if (target.getControlerManager().getControler() instanceof DuelArena) {
            player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a duel!");
            return;
        }
        if (target.getFlowerPokerSession() != null) {
            player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a flower poker session!");
            return;
        }
        SerializableFilesManager.savePlayer(target);
        target.forceLogout();
        player.sendMessage("You have kicked: " + target.getDisplayName() + ".");
        Logger.getGlobal().info("Player " + player.getDisplayName() + " has kicked " + target.getDisplayName() + "!");
    }
}
