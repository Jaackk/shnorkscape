package com.rs.game.player.questing.quests.example_quest;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;

/**
 * Created by David on 7/30/2017.
 */
public class StartInteraction extends QuestInteraction {

    protected StartInteraction(Quest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        player.getDialogueManager().startDialogue("SimpleMessage", "I guess you can help me... Go get me 5000 coins.");
        quest.start();
    }

    @Override
    public void fail(Player player) {
        player.getDialogueManager().startDialogue("SimpleMessage", "You should train your attack a bit more...");
    }
}
