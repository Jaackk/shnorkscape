package com.rs.game.player.questing.quests.example_quest;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 7/30/2017.
 */
public class ZookeeperInteraction extends QuestInteraction {

    protected ZookeeperInteraction(ExampleQuest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        player.getDialogueManager().startDialogue("SimpleMessage", "Good, you brought the NPC the coins. Congratulations!");
        quest.advance();
    }

    @Override
    public void fail(Player player) {
        player.getDialogueManager().startDialogue("SimpleMessage", "Still no money yet, huh?");
    }
}
