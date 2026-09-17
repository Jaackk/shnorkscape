package com.rs.game.player.commands.impl.supportcommands;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.security.pin.AccountPin;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.SUPPORT,
        possibleCommands = {"unlockacc"},
        description = "unlocks a players account"
)
public class UnlockAccountCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        String unlockName = getRestOfInput(1, args);
        Player unlock = World.getPlayerByDisplayName(unlockName);
        if (unlock != null && !unlock.getAccountPin().hasEnteredPin()) {
            unlock.unlock();
            unlock.getAccountPin().setPinEntered();
            unlock.getAccountPin().resetPinDelay();
            unlock.getInterfaceManager().closeChatBoxInterface();
            unlock.sendMessage(AccountPin.COLOR + "Your account has been forcibly unlocked by " + player.getDisplayName() + ".");
            player.sendMessage("You have unlocked " + unlockName + "'s account.");
        }
    }

}
