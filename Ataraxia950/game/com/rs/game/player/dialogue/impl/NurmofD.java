package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class NurmofD extends Dialogue {

    private int npcId;
    private int chance;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        chance = Utils.random(15);

            sendNPCDialogue(npcId, NORMAL, "'Ello, what are you after then?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (chance != 0) {
                    sendPlayerDialogue(NORMAL, "Can I view your shop please?");
                    stage = 0;
                } else {
                    sendPlayerDialogue(NORMAL,"What's it like just standing around here all day?");
                    stage = 10;
                }
                break;

            case 0:
                sendNPCDialogue(npcId, NORMAL, "Sure thing matey.");
                stage = 1;
                break;

            case 1:
                ShopsDataParser.openShop(player, 159);
                end();
                break;

            case 10:
                sendNPCDialogue(npcId, SAD, "Quite lonely... I've been standing here for " + Utils.random(10000)
                        + " days now, I wish the developers had coded me to be something more...");
                stage = 11;
                break;

            case 11:
                sendPlayerDialogue(SAD, "Oh my, that is quite unfortunate.");
                stage = 12;
                break;

            case 12:
                sendNPCDialogue(npcId, SAD, "'Aye...");
                stage = 13;
                break;

            case 13:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }

}
