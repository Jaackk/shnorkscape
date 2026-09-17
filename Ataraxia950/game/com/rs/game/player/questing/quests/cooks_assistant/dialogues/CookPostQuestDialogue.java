package com.rs.game.player.questing.quests.cooks_assistant.dialogues;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Created by David on 8/4/2017.
 */
public class CookPostQuestDialogue extends Dialogue {

    private int npcId;

    @Override
    public void start() {
        npcId = (int) parameters[0];
        sendNPCDialogue(npcId, Dialogue.CALM, "Thanks for your help!");
        end();
    }

    @Override
    public void run(int interfaceId, int componentId) {

    }

    @Override
    public void finish() {

    }
}
