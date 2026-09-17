package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.conditions.InteractionKey;
import com.rs.game.player.questing.quests.example_quest.ExampleQuest;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Responsible for managing the currently handled, in-game
 * quests, as well as performing {@link QuestStage} node
 * processing.
 * @author David O'Neill
 */
public final class QuestHandler {

    static final List<Class<Quest>> handledQuests = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            handledQuests.add((Class<Quest>) Class.forName(ExampleQuest.class.getCanonicalName()));
            Logger.getGlobal().info("Init Quest handler with " + handledQuests.size() + " quest(s).");
        } catch(ClassNotFoundException e) {
            Logger.getGlobal().fatal("Error initializing Quest Handler.", e);
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Performs {@link QuestHandler#processNodesBulk(List,
     *      QuestInteraction.InteractionType, Player, InteractionKey)}
     * on all the quests (not started, in progress, and finished) in
     * a players {@link QuestManager}. The nodes are processed
     * in priority-order: Not started quests are considered first,
     * followed by in progress, ending with finished.
     * @param qm the {@link QuestManager} object containing the quests to process
     * @param t the target interaction type
     * @param p the player
     * @param k the {@link InteractionKey}
     * @return a boolean representing if any of the nodes triggered a
     *         {@link QuestInteraction} for the given target interaction type
     */
    public static boolean processAllNodes(QuestManager qm, QuestInteraction.InteractionType t, Player p, InteractionKey<?> k) {
        return processNodesBulk(qm.getNotStartedQuests(), t, p, k) ||
               processNodesBulk(qm.getQuestsInProgress(), t, p, k) ||
               processNodesBulk(qm.getFinishedQuests(), t, p, k);
    }

    /**
     * Performs {@link QuestHandler#processNode(QuestStage,
     *      QuestInteraction.InteractionType, Player, InteractionKey)}
     * on all the nodes from a list of quests.
     * @param quests a list of {@link Quest} objects
     * @param t the target interaction type to process
     * @param p the player
     * @param k the {@link InteractionKey}
     * @return a boolean representing if any of the nodes triggered a {@link QuestInteraction}
     *         for the given target interaction type
     */
    private static boolean processNodesBulk(List<Quest> quests, QuestInteraction.InteractionType t, Player p, InteractionKey<?> k) {
        for(Quest q : quests) {
            if(q.getCurrentStage() == null)
                continue;
            if(processNode(q.getCurrentStage(), t, p, k))
                return true;
        }
        return false;
    }

    /**
     * Gets the handle of a {@code QuestStage} node, and processes
     * the node in the packet handlers.
     *
     * @param stage the current quest stage
     * @param t the target interaction type
     * @param p the player
     * @param k the assertion key
     * @return a boolean representing whether or not the
     *         interaction key corresponded with the assertion key
     */
    private static boolean processNode(QuestStage stage, QuestInteraction.InteractionType t, Player p, InteractionKey<?> k) {
        QuestCondition con =
                stage.getNode().stream().filter(condition -> condition.getInteraction().getType() == t)
                        .findFirst().orElse(null);
        if(con != null) {
            Predicate<Player> c = con.getConditionCheck();
            QuestInteraction i = con.getInteraction();
            if(c.test(p) && k.equals(i.getInteractionKey())) {
                i.succeed(p);
                return true;
            }
            else if(!c.test(p) && k.equals(i.getInteractionKey())) {
                i.fail(p);
                return true;
            }
        }
        return false;
    }


    private QuestHandler() {

    }
}
