package com.rs.game.player.commands.impl.regularplayercommands;


import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Colors;

import java.time.LocalDateTime;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"suggest","feature","idea","request"},
        description = "used for suggesting new feature"
        )
public class SuggestCommand extends Command {
    private static final int MINUTES_DELAY = 3;

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        String input = getRestOfInput(1, args);
        if (input.isEmpty()) {
            player.sendMessage("Your suggestion cannot be empty!");
            return;
        }
        if (args.length < 4) {
            player.sendMessage("Your suggestion must contain at least 3 words.");
            return;
        }
        LocalDateTime checkFor = LocalDateTime.now().minusMinutes(MINUTES_DELAY);
        boolean isBefore = player.lastSuggest2 == null;
        if (isBefore || (isBefore = player.lastSuggest2.isBefore(checkFor)) || player.isStaff() || player.isDonator()) {
            if (player.isStaff() && !isBefore) {
                player.sendMessage(Colors.PINK + "Staff: You have bypassed the throttle timer.");
            }
            StringBuilder sb = new StringBuilder(input);
            sb.append("\n\n");
            String discordUser = player.getDiscordId();
            if (discordUser != null) {
                sb.append("Suggestion sent by <@").append(discordUser).append("> | ").append(player.getDisplayName()).append(" (").append(player.getUsername()).append(").");
            } else {
                sb.append("Suggestion sent by ").append(player.getDisplayName()).append(" (").append(player.getUsername()).append(").");
            }
            player.lastSuggest2 = LocalDateTime.now();
            player.sendMessage("Your suggestion has been sent to the Discord channel.");
        } else {
            player.sendMessage("You need to wait a few minutes before submitting another suggestion.");
        }
    }
}
