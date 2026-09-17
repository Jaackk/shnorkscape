package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class AllTeleportsD extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.", "Training teleports", "Boss teleports", "Minigame teleports", "PKing teleports", "Skilling teleports");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == OPTION_1) {
            player.getDialogueManager().startDialogue("TrainingTeleports");
        } else if (componentId == OPTION_2) {
            player.getDialogueManager().startDialogue("BossTeleports");
        } else if (componentId == OPTION_3) {
            player.getDialogueManager().startDialogue("MinigameTeleports");
        } else if (componentId == OPTION_4) {
            player.getDialogueManager().startDialogue("PkingTeleports");
        } else if (componentId == OPTION_5) {
            player.getDialogueManager().startDialogue("SkillingTeleportsD");
        }
    }

    @Override
    public void finish() {

    }
}
