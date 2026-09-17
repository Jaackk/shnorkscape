package com.rs.game.player.actions.herblore;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class AutoUnfPotionD extends Dialogue {

    @Override
    public void start() {
        sendNPCDialogue(17170, CALM, "Hello, I can transmute noted clean herbs into noted unfinished potions.");
        stage = 0;
    }

    @Override
    public void run(final int interfaceId, final int componentId) {
        switch (stage) {
            case 0:
                if (player.getInventory().getCoinsAmount() < 10_000) {
                    sendNPCDialogue(17170, CALM, "It'll cost 10K per herb.", "You don't seem to have enough money on you.");
                    stage = 2;
                } else {
                    sendNPCDialogue(17170, CALM, "It'll cost 10K per herb.", "Would you like me to do that for you?");
                    stage = 1;
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    AutoUnfPotion.toUnf(player);
                }
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}
