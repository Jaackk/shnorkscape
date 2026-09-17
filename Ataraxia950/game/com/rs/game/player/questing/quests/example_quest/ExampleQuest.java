package com.rs.game.player.questing.quests.example_quest;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.questing.framework.quest.Quest;
import com.rs.game.player.questing.framework.quest.QuestInteraction;
import com.rs.game.player.questing.framework.quest.QuestStage;

/**
 * Created by David on 7/30/2017.
 */
public class ExampleQuest extends Quest {

    public ExampleQuest(Player player) {
        super(player);
    }

    @Override
    protected void createFlow() {
        addToFlow(QuestStage.builder().append(player -> player.getSkills().getLevel(Skills.ATTACK) > 98,
                new StartInteraction(this, QuestInteraction.InteractionType.NPC_1_INTERACTION, 28)));
        addToFlow(QuestStage.builder().append(player -> player.getInventory().containsCoins(1000),
                new ZookeeperInteraction(this, QuestInteraction.InteractionType.NPC_1_INTERACTION, 28)));
    }

    @Override
    protected QuestStage finishQuest() {
        player.sendMessage("Quest complete! Fuck yeah, bitches");

        return QuestStage.builder().append(__ -> true, new
                QuestInteraction(this, QuestInteraction.InteractionType.NPC_1_INTERACTION, 28) {
            @Override
            public void succeed(Player player) {
                player.getDialogueManager().startDialogue("SimpleMessage", "Thanks for the help!");
            }

            @Override
            public void fail(Player player) {

            }
        });

    }
}
