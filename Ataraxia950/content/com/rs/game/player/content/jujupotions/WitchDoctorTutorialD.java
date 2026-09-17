package com.rs.game.player.content.jujupotions;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WitchDoctorTutorialD extends Dialogue {

    private static final String URL = "https://ataraxia-ps.com/forums/topic/791-juju-potions/";

    @Override
    public void start() {
        sendNPCDialogue(17508, NORMAL,
                "Everything you need to know about juju potions is in the guide on our forums.",
                "Would you like me to take you there?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Yes please",
                        "No thanks");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    end();
                    player.getPackets().sendOpenURL(URL);
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue(new WitchDoctorD());
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
