package com.rs.game.activities.seasonalevents.christmas;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96
 */
public final class SantaExtraRewardDialogue extends Dialogue {

    private final int rewardAmount;

    public SantaExtraRewardDialogue(int rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    @Override
    public void start() {
        if (rewardAmount > 1) {
            for (int loops = 0; loops < rewardAmount; loops++) {
                PresentHandler.givePresentReward(player, false);
            }
            sendItemDialogue(ChristmasSeasonalEvent.LARGE_PRESENT_ITEM, rewardAmount, "Santa smiles and hands you " + rewardAmount + " gifts.");
            stage = 0;
        } else {
            Item item = PresentHandler.givePresentReward(player, false);
            if (item == null)
                return;
            sendItemDialogue(item.getId(), item.getAmount(), "Santa smiles and hands you a gift.");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            sendNPCDialogue(ChristmasSeasonalEvent.SANTA_NPC, NORMAL, "That's for all your hard work so far. Happy holidays!");
            stage = -1;
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }
}
