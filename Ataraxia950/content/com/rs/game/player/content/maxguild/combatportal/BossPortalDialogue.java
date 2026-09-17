package com.rs.game.player.content.maxguild.combatportal;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author Xenthium.
 */

public class BossPortalDialogue extends Dialogue {

    private BossPortalType portal;

    @Override
    public void start() {
        portal = (BossPortalType) parameters[0];
        if (player.getBossPortal() != portal) {
            sendOptionsDialogue("Redirect the portal to " + Utils.formatPlayerNameForDisplay(portal.name()) + "?" + (player.getMoneySpent() < 500 ? " This will incur a fee of " + Colors.wrap(Colors.RED, Utils.formatNumber(BossPortal.getRetuneCost(player))) + " coins." : ""), "Yes.", "No.");
        } else {
            player.getDialogueManager().startDialogue("SimpleMessage", "The portal is already focused towards that destination!");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            player.getTemporaryAttributtes().remove(BossPortal.RETUNING_COMBAT_PORTAL_KEY);
            player.getInterfaceManager().closeScreenInterface();
            if (player.getMoneySpent() < 500) {
                if (player.getMoneyPouch().removeAmount(BossPortal.getRetuneCost(player))) {
                    player.setCombatPortalRetuneDelay(BossPortal.getRetuneDelay(player));
                } else {
                    player.sendMessage("You need " + Colors.wrap(Colors.RED, Utils.formatNumber(BossPortal.getRetuneCost(player))) + " coins to retune the portal.");
                    end();
                    return;
                }
            }
            player.setBossPortal(portal);
            BossPortal.spawn(player);
            player.sendMessage("You've focused the portal towards " + Colors.wrap(Colors.RED, Utils.formatPlayerNameForDisplay(player.getBossPortal().name())) + ".");
        }
        end();
    }

    @Override
    public void finish() {
    }

}
