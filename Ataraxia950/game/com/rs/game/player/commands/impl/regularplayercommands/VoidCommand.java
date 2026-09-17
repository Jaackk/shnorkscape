package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.combat.CombatUtils;
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
        possibleCommands = {"void"},
        description = "Displays your current bonus with your equiped void armor"
        )
public class VoidCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        CombatUtils.VoidType type;
        if ((type = CombatUtils.getWornVoidType(player, CombatUtils.VoidCombatType.MELEE)) != null) {
            player.sendMessage("You are wearing " + type + ". " + type.getMeleeBoost());
        } else if ((type = CombatUtils.getWornVoidType(player, CombatUtils.VoidCombatType.MAGE)) != null) {
            player.sendMessage("You are wearing " + type + ". " + type.getMagicBoost());
        } else if ((type = CombatUtils.getWornVoidType(player, CombatUtils.VoidCombatType.RANGER)) != null) {
            player.sendMessage("You are wearing " + type + ". " + type.getRangedBoost());
        } else {
            player.sendMessage("You aren't currently wearing any void set.");
        }
    }
}
