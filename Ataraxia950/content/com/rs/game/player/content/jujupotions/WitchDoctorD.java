package com.rs.game.player.content.jujupotions;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WitchDoctorD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.",
                "How can I make juju potions?",
                "Can I see your shop?",
                "Can you take me somewhere?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            if (componentId == OPTION_1) {
                player.getDialogueManager().startDialogue(new WitchDoctorTutorialD());
            } else if (componentId == OPTION_2) {
                ShopsDataParser.openShop(player, 183);
            } else if (componentId == OPTION_3) {
                player.getDialogueManager().startDialogue(new WitchDoctorTeleportD());
            }
        }
    }

    @Override
    public void finish() {

    }
}
