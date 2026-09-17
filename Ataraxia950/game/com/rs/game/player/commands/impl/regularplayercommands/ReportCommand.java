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
        possibleCommands = {"report", "bug", "bugreport"},
        description = "used to report a bug"
)
public class ReportCommand extends Command {
    private static final int MINUTES_DELAY = 2;

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        String input = getRestOfInput(1, args);
        if (input.isEmpty()) {
            player.sendMessage("Your bug report cannot be empty!");
            return;
        }
        if (args.length < 4) {
            player.sendMessage("Your bug report must contain at least 3 words.");
            return;
        }
        LocalDateTime checkFor = LocalDateTime.now().minusMinutes(MINUTES_DELAY);
        boolean isBefore = player.lastBug2 == null;
        if (isBefore || (isBefore = player.lastBug2.isBefore(checkFor)) || player.isStaff()) {
            if (player.isStaff() && !isBefore) {
                player.sendMessage(Colors.PINK + "Staff: You have bypassed the throttle timer.");
            }
            StringBuilder sb = new StringBuilder(input);
            sb.append("\n\n");
            String discordUser = player.getDiscordId();
            if (discordUser != null) {
                sb.append("Bug report sent by <@").append(discordUser).append("> | ").append(player.getDisplayName()).append(" (").append(player.getUsername()).append(").");
            } else {
                sb.append("Bug report sent by ").append(player.getDisplayName()).append(" (").append(player.getUsername()).append(").");
            }
            player.lastBug2 = LocalDateTime.now();
            player.sendMessage("Your bug report has been sent to the Discord channel.");
        } else {
            player.sendMessage("You need to wait a few minutes before submitting another bug report.");
        }
    }
}
