package com.rs.game.player.commands.impl.regularplayercommands;



import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
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
        possibleCommands = {"b1","b2","b3","b4","b5","b6","b7","b8","b9","b10"},
        description = "Opens more banks when you have the bankcommand perk."
        )
public class MoreBanksCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getPerkManager().hasPerkActive(DonationPerk.BANK_COMMAND)) {
            if (!player.canSpawn()) {
                player.sendMessage("You cannot open your bank account at the moment.");
                return;
            }
            if (player.isLocked()) {
                player.sendMessage("You can't bank at the moment, please wait.");
                return ;
            }
            if (player.isUnderCombat()) {
                player.sendMessage("You can't bank while in combat, please wait.");
                return;
            }
            int bankId = Integer.parseInt(command.replace("b", ""));
            if (player.getBanks().size() >= bankId) {
                player.getBanks().get(bankId - 1).setPlayer(player);
                player.getBanks().get(bankId - 1).openBank();
                player.setBank(player.getBanks().get(bankId - 1));
                player.getBank().openBank();
            }
            return;
        } else {
            player.sendMessage("You have to purchase the Bank Command perk in order to do this.");
        }
    }
}
