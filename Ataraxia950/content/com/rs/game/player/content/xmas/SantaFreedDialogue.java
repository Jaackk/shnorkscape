package com.rs.game.player.content.xmas;

import com.rs.game.player.dialogue.Dialogue;

public class SantaFreedDialogue extends Dialogue {
    private final int npcId = 17539; // if this is weird, change this to 8540;

    @Override
    public void start() {
        sendNPCDialogue(npcId, NORMAL, "*Talking in sleep* Rud..olph.. avo..id the meatloaf");
        player.getXmas().freedSanta = true;
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendPlayerDialogue(NORMAL, "I found you! Christmas is saved!");
                stage = 1;
                break;
            case 1:
                sendNPCDialogue(npcId, NORMAL, "Quick get me out of this cage! Winter is coming. Christmas is coming I mean!");
                stage = 2;
                break;
            case 2:
                sendNPCDialogue(npcId, NORMAL, "Thank you for freeing me, I shouldn't go around eating random " +
                        "things I guess... Ooh cookie..!");
                stage = -1;
                break;
            case -1:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}
