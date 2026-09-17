package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class TellMeMoreD extends Dialogue {

    @Override
    public void start() {
        sendNPCDialogue(13790, NORMAL, "Aha, I'd be glad to.",
                "My answer will surely help you on your Evil Tree killing adventures.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendNPCDialogue(13790, NORMAL, "Evil Trees first appear as saplings.",
                        "Nurturing them will give you Farming XP and a chance at an Evil herbs and seeds.",
                        "Herbs can be used to create unfinished potions.",
                        "Seeds are used to travel to ScapeRune to slay an Evil Tree by yourself or with friends!");
                stage = 1;
                break;
            case 1:
                sendNPCDialogue(13790, NORMAL, "Every tree has a patch that you can rake while its growing.",
                        "They give you small amounts of Farming XP and a chance at an Evil seeds.");
                stage = 2;
                break;
            case 2:
                sendNPCDialogue(13790, NORMAL, "Once a tree is fully grown, you can slay it!",
                        "You can chop it for Woodcutting XP and a chance at Evil bark.",
                        "Evil bark is used to create unfinished potions. It can also be lit/bonfired for Firemaking XP,",
                        "or used with a Quickshafter for arrow shafts and Fletching XP.");
                stage = 3;
                break;
            case 3:
                sendNPCDialogue(13790, NORMAL, "The patch will be burnt to a crisp by the tree once it grows,",
                        "so stop raking when it appears. The leftover ash from the burned patch is known as Evil Dust.",
                        "It's used as a secondary to complete Evil herb and bark unfinished potions.");
                stage = 4;
                break;
            case 4:
                sendNPCDialogue(13790, NORMAL, "When its dead, you can loot its insides.",
                        "In order to keep things fair, the loot you'll get depends on how much you helped.",
                        "This is based on a couple of factors like damage, tree type, and the amount of players involved in the killing.",
                        "If you die while fighting the tree, you won't get anything!");
                stage = 5;
                break;
            case 5:
                sendPlayerDialogue(NORMAL, "Wow! That's a mouthful, thanks for the advice!");
                stage = 6;
                break;
            case 6:
                sendNPCDialogue(13790, NORMAL, "Aye.");
                stage = 7;
                break;
            case 7:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}