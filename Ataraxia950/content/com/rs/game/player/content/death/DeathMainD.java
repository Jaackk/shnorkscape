package com.rs.game.player.content.death;

import com.google.common.collect.ImmutableList;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * @author lare96
 */
public final class DeathMainD extends Dialogue {

    private int questStage;
    private static final ImmutableList<String> DEATH = ImmutableList.of(
            "Bwahahaha, what an entertaining death!",
            "I see you're back, as expected.",
            "Welcome back.",
            "Well, that death of yours wasn't pretty. There's still quite a few body parts to clean up...",
            "Well, well, well... look who it is.",
            "You've died again?",
            "I have a feeling I'm going to be seeing you here a lot..."
    );
    private int degradePercentage;

    @Override
    public void start() {
        if (player.getControlerManager().getControler() instanceof DeathController) {
            sendNPCDialogue(14386, NORMAL, Utils.randomFrom(DEATH));
            stage = 0;
        } else {
            questStage = player.quests.getCurrentStage(DeathsBounty.class);
            if (DeathsBounty.activateDeathDialogue(player, questStage)) {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Talk about 'Death's Bounty'.",
                        "Talk about death related things.");
                stage = -1;
            } else {
                sendNPCDialogue(14386, NORMAL, "What do you want with me, mortal?");
                stage = 0;
            }
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue("DeathD", questStage);
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(14386, NORMAL, "What do you want with me, mortal?");
                    stage = 0;
                }
                break;
            case 0:
                sendMainMenu();
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue(new DeathPurchaseD());
                } else if (componentId == OPTION_2) {
                    DeathStatistics.getInstance().openClaim(player);
                } else if (componentId == OPTION_3) {
                    DeathStatistics.getInstance().openStats(player);
                } else if (componentId == OPTION_4) {
                    DeathResetDegradeD.start(player, degradePercentage);
                }
                break;
        }
    }

    @Override
    public void finish() {
    }

    private void sendMainMenu() {
        degradePercentage = player.deathItemsManager.getDegradePercentageInt();
        sendOptionsDialogue("Select an option",
                "Can I buy my items back? (" + player.deathItemsManager.getItemCount() + ")",
                "Can I claim my bought items? (" + player.deathItemsManager.getClaimableItemCount() + ")",
                "Can I see the server death statistics?",
                "Can I reset my item degrade percentage? (" + degradePercentage + "%)");
        stage = 0;
    }
}
