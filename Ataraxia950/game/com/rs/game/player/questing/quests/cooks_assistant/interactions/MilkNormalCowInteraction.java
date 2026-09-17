package com.rs.game.player.questing.quests.cooks_assistant.interactions;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.dialogues.MilkNormalCowDialogue;

/**
 * Created by David on 8/4/2017.
 */
public class MilkNormalCowInteraction extends QuestInteraction {

    public MilkNormalCowInteraction(Quest quest, InteractionType type, Object key) {
        super(quest, type, key);
    }

    @Override
    public void succeed(Player player) {
        player.getDialogueManager().startDialogue(new MilkNormalCowDialogue());
    }

    @Override
    public void fail(Player player) {

    }
}
