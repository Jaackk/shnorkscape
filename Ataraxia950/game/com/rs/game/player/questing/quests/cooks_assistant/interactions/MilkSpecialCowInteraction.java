package com.rs.game.player.questing.quests.cooks_assistant.interactions;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 8/4/2017.
 */
public class MilkSpecialCowInteraction extends QuestInteraction {

    public MilkSpecialCowInteraction(Quest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        player.getInventory().addItem(1, 1);
        player.sendMessage("You milk the cow for top-quality milk.");
    }

    @Override
    public void fail(Player player) {
        player.sendMessage("You'll need an empty bucket to collect the milk.");
    }
}
