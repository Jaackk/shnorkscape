package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class WhatAreTreesD extends Dialogue {

    @Override
    public void start() {
        sendNPCDialogue(13790, NORMAL, "Evil Trees are demonic monsters from ScapeRune.",
                "They are powerful, powerful mages that take on the form of trees when in our dimension.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendNPCDialogue(13790, NORMAL, "The vile beasts are known to kill wandering adventurers who",
                        "somehow mistake them for regular trees.", "I am here to slay every last one of them.");
                stage = 1;
                break;
            case 1:
                sendNPCDialogue(13790, NORMAL, "I can take you directly to an Evil Tree when I sense one.",
                        "You can also see the Evil Tree's status at the bottom of your task tab.");
                stage = 2;
                break;
            case 2:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}