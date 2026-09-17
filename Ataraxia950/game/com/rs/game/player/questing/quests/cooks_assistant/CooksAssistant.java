package com.rs.game.player.questing.quests.cooks_assistant;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;
import com.rs.game.player.questing.framework.quest.QuestStage;
import com.rs.game.player.questing.quests.cooks_assistant.dialogues.CookFinishDialogue;
import com.rs.game.player.questing.quests.cooks_assistant.dialogues.CookPostQuestDialogue;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.CookFinishInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.CookStartInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.GillieGroatsInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.MilkNormalCowInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.MilkSpecialCowInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.MillieMillerInteraction;
import com.rs.game.player.questing.quests.cooks_assistant.interactions.TakeFlourInteraction;

/**
 * Created by David on 8/4/2017.
 */
public class CooksAssistant extends Quest {

    protected CooksAssistant(Player player) {
        super(player);
    }

    @Override
    protected void createFlow() {
        addToFlow(QuestStage.builder().append(p -> true, new CookStartInteraction(this,
                QuestInteraction.InteractionType.NPC_1_INTERACTION, 1)));
        addToFlow(QuestStage.builder()
                .append(p -> true, new MilkNormalCowInteraction(this,
                    QuestInteraction.InteractionType.NPC_2_INTERACTION, 1))
                .append(p -> true, new GillieGroatsInteraction(this,
                    QuestInteraction.InteractionType.NPC_1_INTERACTION, 1))
                .append(p -> p.getInventory().containsItem(1, 1), new MilkSpecialCowInteraction(this,
                    QuestInteraction.InteractionType.OBJECT_1_INTERACTION, 1))
                .append(p -> true, new MillieMillerInteraction(this,
                        QuestInteraction.InteractionType.NPC_1_INTERACTION, 1))
                .append(p -> p.getInventory().containsItem(1, 1), new TakeFlourInteraction(this,
                        QuestInteraction.InteractionType.OBJECT_1_INTERACTION, 1))
                .append(this::checkHasIngredients, new CookFinishInteraction(this,
                        QuestInteraction.InteractionType.NPC_1_INTERACTION, 1)));
    }

    @Override
    protected QuestStage finishQuest() {
        // Get rewards, give them to player or deposit to bank...

        return QuestStage.builder().append(p -> true, new QuestInteraction(this,
                QuestInteraction.InteractionType.NPC_1_INTERACTION, 1) {

            @Override
            public void succeed(Player player) {
                player.getDialogueManager().startDialogue(new CookPostQuestDialogue(), 1);
            }

            @Override
            public void fail(Player player) {

            }
        });
    }

    private boolean checkHasIngredients(Player player) {
        return player.getInventory().containsItem(CookFinishDialogue.FLOUR_ID, 1) &&
                player.getInventory().containsItem(CookFinishDialogue.MILK_ID, 1) &&
                player.getInventory().containsItem(CookFinishDialogue.EGG_ID, 1);
    }
}
