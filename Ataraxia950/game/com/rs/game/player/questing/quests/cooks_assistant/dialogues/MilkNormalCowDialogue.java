package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Created by David on 8/4/2017.
 */
public class MilkNormalCowDialogue extends Dialogue {

    @Override
    public void start() {
        stage = 0;
        sendPlayerDialogue(Dialogue.CALM, "Hmm, I'm not sure if this " +
                "is top-quality milk. Maybe that lady over there can help me.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 0) {
            stage = 1;
            sendOptionsDialogue("Milk it anyway for ordinary milk?");
        } else if(stage == 1) {
            switch(componentId) {
                case OPTION_1:
                    // Give player dat milk
                    end();
                    break;
                case OPTION_2:
                    end();
                    break;
            }
        }
    }

    @Override
    public void finish() {

    }
}
