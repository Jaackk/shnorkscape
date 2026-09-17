package com.rs.game.player.dialogue.impl;

import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class TogglePerksD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Which perk to toggle?",
                "Drop Catcher (" + dropCatcher() + ")",
                "Arcane Alchemist (" + arcaneAlchemist() + ")");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if (componentId == OPTION_1 && player.getPerkManager().hasPerk(DonationPerk.DROP_CATCHER)) {
                    player.getPerkManager().togglePerkActivation(DonationPerk.DROP_CATCHER);
                    player.sendMessage("You have toggled the " + Colors.GREEN + "Drop Catcher</col> perk [" + dropCatcher() + "].");
                    end();
                } else if (componentId == OPTION_2 && player.getPerkManager().hasPerk(DonationPerk.ARCANE_ALCHEMIST)) {
                    player.getPerkManager().togglePerkActivation(DonationPerk.ARCANE_ALCHEMIST);
                    player.sendMessage("You have toggled the " + Colors.GREEN + "Arcane Alchemist</col> perk [" + arcaneAlchemist() + "].");
                    end();
                } else {
                    sendDialogue("You have not purchased this perk.");
                    stage = 1;
                }
                break;
            case 1:
                player.getDialogueManager().startDialogue("TogglePerksD");
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    private String dropCatcher() {
        return player.getPerkManager().hasPerkActive(DonationPerk.DROP_CATCHER) ? Colors.GREEN + "ON</col>" : Colors.RED + "OFF</col>";
    }

    private String arcaneAlchemist() {
        return player.getPerkManager().hasPerkActive(DonationPerk.ARCANE_ALCHEMIST) ? Colors.GREEN + "ON</col>" : Colors.RED + "OFF</col>";
    }
}