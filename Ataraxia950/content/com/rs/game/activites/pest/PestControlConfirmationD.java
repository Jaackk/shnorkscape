package com.rs.game.activites.pest;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class PestControlConfirmationD extends Dialogue {

    Integer landerId;

    @Override
    public void start() {
        landerId = (Integer) parameters[0];
        sendOptionsDialogue("Would you like to start a game?",
                "Yes, I would like to start.",
                "I'd rather continue waiting for more players.");
        stage = 0;
    }


    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                finish();
                switch (componentId) {
                    case OPTION_1:
                        Lander.getLanders()[landerId].confirm(player);
                        break;
                    case OPTION_2:
                        Lander.getLanders()[landerId].reject(player);
                        break;
                }
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}