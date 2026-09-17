package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Encrypt;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"changepass"},
        description = "lets you change your password"
        )
public class ChangePasswordCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        String inputLine = "";
        for (int i = 1; i < args.length; i++) {
            inputLine += args[i] + ((i == args.length - 1) ? "" : " ");
        }
        if (inputLine.length() > 15) {
            player.sendMessage("You cannot set your password with over 15 chars.");
            return ;
        }
        if (inputLine.length() < 5) {
            player.sendMessage("You cannot set your password with less than 5 chars.");
            return ;
        }
        player.setPassword(Encrypt.encryptSHA1(args[1]));
        player.sendMessage("You've successfully changed your password! Your new password is " + args[1] + ".");
        return;
    }
}
