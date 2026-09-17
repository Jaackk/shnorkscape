package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;

import java.util.function.Predicate;

/**
 * A wrapper data structure containing a
 * {@link Predicate} and a {@link QuestInteraction}.
 * @author David O'Neill
 */
final class QuestCondition {

    private final Predicate<Player> condition;
    private final QuestInteraction interaction;

    QuestCondition(Predicate<Player> condition, QuestInteraction interaction) {
        this.condition = condition;
        this.interaction = interaction;
    }

    Predicate<Player> getConditionCheck() {
        return condition;
    }

    QuestInteraction getInteraction() {
        return interaction;
    }

}

