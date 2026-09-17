package com.rs.game.player.questing.quests.cooks_assistant.interactions;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 8/4/2017.
 */
public class TakeFlourInteraction extends QuestInteraction {

    public TakeFlourInteraction(Quest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        // give flour
        player.getInventory().addItem(1, 1);
        player.sendMessage("You fill a pot with extra-fine flour from the bin.");
    }

    @Override
    public void fail(Player player) {
        player.sendMessage("You need an empty pot to hold the flour.");
    }
}
