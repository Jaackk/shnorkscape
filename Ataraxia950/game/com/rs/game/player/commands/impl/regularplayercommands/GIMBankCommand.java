package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;


import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;


/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"gimbank"},
        description = "opens the GIM bank"
)
public final class GIMBankCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getPerkManager().hasPerkActive(DonationPerk.BANK_COMMAND) || player.isOwner()) {
            if (!player.canSpawn()) {
                player.sendMessage("You cannot open your bank account at the moment.");
                return;
            }
            if (player.isLocked() || player.getTemporaryAttributtes().get(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY) == Boolean.TRUE) {
                player.sendMessage("You can't bank at the moment, please wait.");
                return;
            }
            if (player.isUnderCombat()) {
                player.sendMessage("You can't bank while in combat, please wait.");
                return;
            }
            player.gimBank.open();
        } else {
            player.sendMessage("You have to purchase the Bank Command perk in order to do this.");
        }
    }
}
