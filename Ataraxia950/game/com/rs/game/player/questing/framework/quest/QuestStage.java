package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A quest stage is characterized by a certain
 * set of conditions (each represented as a {@link QuestCondition}).
 * This set of conditions is called the "node", and it describes
 * the logical flow of interactions the player will have
 * in this particular stage of the quest.<br/><br/>
 *
 * The quest stages are built using the Builder pattern, and therefore
 * it is impossible to instantiate a {@code QuestStage} using the
 * {@code new} keyword. Instead, use {@link QuestStage#builder()}.
 *
 * @author David O'Neill
 * @since 7/29/2017
 */
public class QuestStage {

    private Set<QuestCondition> node;

    /**
     * Constructs and appends a new {@link QuestCondition} object
     * to the node. This {@code QuestStage} object is returned
     * for simple cascading calls.
     *
     * @param condition a {@link Predicate} which determines what type of
     *                  interaction functionality the player will experience.
     *                  {@link Predicate} is a functional interface,
     *                  so the condition can be supplied as a lambda expression
     *                  with a {@code Player} object as the parameter.
     * @param interaction a {@link QuestInteraction} the player will experience
     *                    in this stage.
     * @return
     */
    public QuestStage append(Predicate<Player> condition, QuestInteraction interaction) {
        if(node == null)
            node = new HashSet<>();
        QuestCondition c = new QuestCondition(condition, interaction);
        node.add(c);
        return this;
    }

    /**
     * Gives back a template {@link QuestStage} object onto which you can
     * call {@link QuestStage#append(Predicate, QuestInteraction)}.
     *
     * @return a blank {@link QuestStage}
     */
    public static QuestStage builder() {
        return new QuestStage();
    }

    /**
     * A handle to the node, wrapped in an immutable collection.
     * @return an unmodifiable version of the node.
     */
    Set<QuestCondition> getNode() {
        return Collections.unmodifiableSet(node);
    }


    private QuestStage() {

    }


}
