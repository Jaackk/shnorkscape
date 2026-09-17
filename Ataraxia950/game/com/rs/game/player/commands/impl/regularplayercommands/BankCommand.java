package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activities.aod.AoDController;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.controllers.bossInstance.SpiderBossInstanceController;
import com.rs.game.player.controllers.bossInstance.TelosInstanceController;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;


/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"bank","b"},
        description = "opens the bank"
        )
public class BankCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        if (player.getPerkManager().hasPerkActive(DonationPerk.BANK_COMMAND)) {
            if (!player.canSpawn()) {
                player.sendMessage("You cannot open your bank account at the moment.");
                return;
            }
            if (player.isLocked() || player.getTemporaryAttributtes().get(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY) == Boolean.TRUE) {
                player.sendMessage("You can't bank at the moment, please wait.");
                return;
            }
            if (!player.canSpawn() || (player.getControlerManager().getControler() != null && (((player.getControlerManager().getControler() instanceof AoDController)) || player.getCurrentInstance() != null || player.getControlerManager().getControler() instanceof TelosInstanceController || player.getControlerManager().getControler() instanceof VoragoInstanceController || player.getControlerManager().getControler() instanceof SpiderBossInstanceController))) {
                player.sendMessage("You can't bank while you're in this area.");
                return;
            }
            if (player.isUnderCombat()) {
                player.sendMessage("You can't bank while in combat, please wait.");
                return;
            }
            if (!player.promptList()) {
                player.getBank().openBank();
            } else {
                player.getDialogueManager().startDialogue("BankList", false);
            }
            return;
        } else {
            player.sendMessage("You have to purchase the Bank Command perk in order to do this.");
        }
    }
}
