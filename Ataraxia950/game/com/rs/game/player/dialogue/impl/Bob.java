package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class Bob extends Dialogue {

    private boolean repairAll;
    private int npcId;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        repairAll = (Boolean) parameters[1];
        if (repairAll) {
            end();
            player.getChargesManagerNew().sendRechargeItemDialogue(null, npcId);
        } else {
            sendOptionsDialogue("Select an Option", "Repair all damaged/broken items", "Open skilling supplies shop");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                end();
                player.getChargesManagerNew().sendRechargeItemDialogue(null, npcId);
                break;
            case OPTION_2:
                ShopsDataParser.openShop(player, 16);
                player.getInterfaceManager().closeChatBoxInterface();
                break;
            }
            break;
        }
    }

    @Override
    public void finish() {

    }

}