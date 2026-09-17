package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.player.content.items.AshScattering;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilDustScatterConfirmD extends Dialogue {
// NOTE: Not being used ATM.
    private final int slot;

    public EvilDustScatterConfirmD(int slot) {
        this.slot = slot;
    }

    @Override
    public void start() {
        if (!player.stopEvilDustConfirmation) {
            sendDialogue("Are you sure you'd like to scatter these ashes?");
            stage = 0;
        } else {
            AshScattering.scatter(player, slot);
            end();
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.", "Yes.", "No.", "Don't ask again (Yes).");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    AshScattering.scatter(player, slot);
                } else if (componentId == OPTION_3) {
                    player.stopEvilDustConfirmation = true;
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}