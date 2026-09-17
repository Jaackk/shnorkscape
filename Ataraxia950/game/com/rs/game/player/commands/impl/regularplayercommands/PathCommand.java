package com.rs.game.player.commands.impl.regularplayercommands;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;

@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"path"},
        description = "sends you current vorago/araxxor rotation and time till next rotation"
        )
public class PathCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(Settings.VORAGO_RELEASE_DATE, formatter);
        LocalDate currentDate = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(startDate, currentDate);
        int rotation = 0;
        for (int i = 0; i < daysPassed / Settings.DAYS_TO_CHANGE_ROTATION; i++) {
            rotation = rotation + 1 >= Settings.VORAGO_ROTATION_NAMES.length ? 0 : rotation + 1;
        }
        int daysLeft = (int) (Settings.DAYS_TO_CHANGE_ROTATION - (daysPassed % Settings.DAYS_TO_CHANGE_ROTATION));
        player.getPackets().sendGameMessage("<col=00ff00>Current Vorago rotation: "+Settings.VORAGO_ROTATION_NAMES[rotation] +", days till next rotation: less than "+daysLeft+" day"+(daysLeft == 1 ? "" : "s") + " left.");
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDate = LocalDate.parse(Settings.SPIDER_BOSS_RELEASE_DATE, formatter);
        currentDate = LocalDate.now();
        daysPassed = ChronoUnit.DAYS.between(startDate, currentDate);
        rotation = 0;
        for (int i = 0; i < daysPassed / Settings.SPIDER_BOSS_DAYS_TO_CHANGE_ROTATION; i++) {
            rotation = rotation + 1 >= Settings.SPIDER_BOSS_ROTATION_NAMES.length ? 0 : rotation + 1;
        }
        Settings.SPIDER_BOSS_ROTATION = rotation;
        daysLeft = (int) (Settings.SPIDER_BOSS_DAYS_TO_CHANGE_ROTATION - (daysPassed % Settings.SPIDER_BOSS_DAYS_TO_CHANGE_ROTATION));
        player.getPackets().sendGameMessage("<col=00ff00>Current Arraxor open paths: "+Settings.SPIDER_BOSS_ROTATION_NAMES[rotation] +", days till next rotation: less than "+daysLeft+" day"+(daysLeft == 1 ? "" : "s") + " left.");
    }

}
