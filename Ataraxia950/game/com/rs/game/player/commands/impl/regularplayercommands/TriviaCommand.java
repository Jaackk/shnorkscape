package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.content.TriviaBot;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"trivia","ans","answer"},
        description = "to answer trivia questions"
)
public class TriviaCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (args.length >= 2) {
            String answer = args[1];
            if (args.length == 3) {
                answer = args[1] + " " + args[2];
            }
            if (args.length == 4) {
                answer = args[1] + " " + args[2] + " " + args[3];
            }
            if (args.length == 5) {
                answer = args[1] + " " + args[2] + " " + args[3] + " " + args[4];
            }
            if (args.length == 6) {
                answer = args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " " + args[5];
            }
            TriviaBot.verifyAnswer(player, answer);
        } else {
            player.sendMessage("Syntax is ::" + args[0] + " <your answer here without the brackets>.");
        }
    }
}
