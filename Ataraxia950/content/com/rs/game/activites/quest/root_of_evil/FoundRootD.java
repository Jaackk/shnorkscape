package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.player.dialogue.Dialogue;

public final class FoundRootD extends Dialogue {

    private int currentStage;
    @Override
    public void start() {
        currentStage = player.quests.getCurrentStage(RootOfEvil.class);
        if(currentStage == 0) {
            sendPlayerDialogue(CONFUSED, "I wonder what this Evil root is? I should take it to the Evil Tree Hunter...");
        } else if(currentStage == 1) {
            sendPlayerDialogue(CONFUSED, "I should take this root to Mister Wiggles and have him check it out.");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        end();
    }

    @Override
    public void finish() {

    }
}
