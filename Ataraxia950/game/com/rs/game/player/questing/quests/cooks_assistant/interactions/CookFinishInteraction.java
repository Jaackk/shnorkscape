package com.rs.game.player.questing.quests.cooks_assistant.interactions;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.dialogues.CookFinishDialogue;

/**
 * Created by David on 8/4/2017.
 */
public class CookFinishInteraction extends QuestInteraction {

    public CookFinishInteraction(Quest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        player.getDialogueManager().startDialogue(new CookFinishDialogue(), this, 1, true);
    }

    @Override
    public void fail(Player player) {
        player.getDialogueManager().startDialogue(new CookFinishDialogue(), this, 1, false);
    }
}
