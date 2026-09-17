package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.Settings;
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
        possibleCommands = {"spin"},
        description = "quick spinning from the squeel of fortune"
        )
public class SpinCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.sendMessage("This has been disabled");
//        if (player.isKingOfTheSkillGameMode()) {
//            player.getKingOfTheSkillGameModeHandler().sendNoSquealMessageToPlayer();
//            return;
//        }

//        Integer toSpin;
//        player.sendMessage("Your rewards have been sent to your bank.");
//        if (args.length >= 2) {
//            try {
//                toSpin = Integer.valueOf(args[1]);
//            } catch (final NumberFormatException e) {
//                player.sendMessage("Use as ::spin amount");
//                return;
//            }
//            if (toSpin != null) {
//                if (player.getSquealOfFortune() != null) {
//                    player.getSquealOfFortune().useCommand(toSpin > 15 && !Settings.DEBUG ? 15 : toSpin);
//                }
//            }
//        }
    }
}
