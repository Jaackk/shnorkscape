package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public final class QuestioningD extends Dialogue {

    private final NPC npc;

    public QuestioningD(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void start() {
        sendPlayerDialogue(CONFUSED, Utils.randomFrom(RootOfEvil.PLAYER_QUESTIONS));
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                if(npc.getId() == 13790) {
                    sendNPCDialogue(13790, ANGRY, "I'm obviously not the perpetrator, I hunt trees for a living you imbecile!");
                } else {
                    sendNPCDialogue(npc.getId(), CONFUSED, Utils.randomFrom(RootOfEvil.NPC_RESPONSES));
                }
                stage = 1;
                player.sendMessage("I don't think they know anything of value. I should keep questioning people.");
                player.questionedNpcs.add(npc.getId());
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
