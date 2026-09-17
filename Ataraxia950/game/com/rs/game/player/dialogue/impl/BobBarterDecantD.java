package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.decantation.Decanting;
import com.rs.game.player.content.decantation.VialToFlask;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class BobBarterDecantD extends Dialogue {

    int npcId;

    @Override
    public void start() {
        npcId = (int) parameters[0];
        sendOptionsDialogue("Select an option.", "Can you decant my potions?", "Can you replace all my vials with flasks?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case 0:
            switch (componentId) {
            case OPTION_1:
                Decanting decanter = new Decanting(player);
                decanter.decantAllPotions();
                break;
            case OPTION_2:
                VialToFlask.vialsToFlasks(player);
                break;
            }
            npc(npcId, CALM, "There ya go chum..");
            stage = 1;
            break;
        case 1:
            end();
            break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

}