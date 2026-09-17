package com.rs.game.player.commands;

import com.rs.game.player.Player;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 * <p>
 * NOTE : make sure to annote every command with the @CommandInfo
 */
public abstract class Command {


    public boolean canExecute(Player player, String command) {
        CommandInfo metaData = this.getClass().getAnnotation(CommandInfo.class);
        if (!player.getControlerManager().processCommand(command, false, false))
            return false;
        if (metaData == null)
            return false;
        if (player.isOwner()) // can use every command
            return true;
        CommandRights rights = metaData.rank();
        if (rights == CommandRights.NORMAL)
            return true;
        if (rights == CommandRights.SUPPORT && (player.isSupport1() || player.isSupport() || player.getRights() > 0))
            return true;
        if (rights == CommandRights.MODERATOR && player.hasModRights())
            return true;
        return rights == CommandRights.ADMIN && player.hasAdminRights();
    }

    public abstract void executeCommand(Player player, boolean isClientCommand, String command, String... args);

    public String getRestOfInput(int from, String[] cmd) {
        StringBuilder name = new StringBuilder();
        for (int i = from; i < cmd.length; i++) {
            name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
        }
        return name.toString();
    }

}
