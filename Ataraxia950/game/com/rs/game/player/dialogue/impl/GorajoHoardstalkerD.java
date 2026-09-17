package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class GorajoHoardstalkerD extends Dialogue {

    @Override
    public void start() {
        sendNPCDialogue(16825, Dialogue.NORMAL, "What's up " + player.getDisplayName() + ", would you like to view my Dungeoneering shop?");
    }

    @Override
    public void run(int interfaceId, int componentId) {

        switch (stage) {
            case -1:
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes", "I'll pass");
                stage = 0;
                break;

            case 0:
                if (componentId == OPTION_1) {
                    if (player.getSkills().getLevel(Skills.DUNGEONEERING) >= 120) {
                        ShopsDataParser.openShop(player, 1210);
                    } else {
                        player.sendMessage(Colors.PINK + "You must reach 120 Dungeoneering to view the Keepsake shop");
                        end();
                    }
                } else {
                    sendNPCDialogue(16825, Dialogue.ANGRY, "I don't want casual Dungeoneers viewing my shop");
                    stage = 1;
                }
                break;

            case 1:
                end();
                break;
        }

    }

    @Override
    public void finish() {
    }
}
