package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A base class for quests. A quest is characterized
 * by a {@code flow}, represented by a FIFO Queue of {@link QuestStage}
 * objects. The flow is initialized via the {@link Quest#createFlow()} method
 * which is called from the constructor body. The {@link Quest#addToFlow(QuestStage)}
 * method is provided in order to expand the quest flow. The {@link Quest#finishQuest()}
 * method will be called when the flow queue is empty.
 * @author David O'Neill
 */
public abstract class Quest {

    private Queue<QuestStage> flow;
    protected transient Player player;
    private QuestStage currentStage;
    private boolean initialPollFlag;

    /**
     * Called from {@code Quest} constructor, responsible
     * for adding {@link QuestStage} objects to the flow queue.
     */
    protected abstract void createFlow();

    /**
     * Called automatically when the flow queue is empty.
     * Rewards and/or experience gains should be explicitly
     * handled from this implementation. Furthermore, this
     * method must return a {@link QuestStage} object
     * containing the finished quest state interactions that
     * the player should experience after finishing the quest.
     * @return a {@link QuestStage} containing finished-state
     *         interactions
     */
    protected abstract QuestStage finishQuest();

    /**
     * Creates a new {@link Quest} object. Upon server startup,
     * new quests which were added to the {@link QuestHandler} will be
     * imported into the player's {@link QuestManager}. Likewise,
     * unused quests which have been removed from the handler will be removed
     * from the {@link QuestManager}.
     * @param player the player this quest object belongs to
     */
    protected Quest(Player player) {
        this.player = player;
        createFlow();
    }

    /**
     * Adds a new quest stage to the flow. {@code QuestStage} objects
     * are not instantiated using the {@code new} keyword, but should intead
     * be created using the static {@link QuestStage#builder()} function.<br/><br/>
     *
     * See {@link QuestStage}.
     * @param questStage the stage to add to the flow queue
     */
    protected final void addToFlow(QuestStage questStage) {
        if(flow == null)
            flow = new LinkedBlockingQueue<>();
        flow.add(questStage);
        if(flow.size() == 1 && !initialPollFlag) {
            currentStage = flow.poll();
            initialPollFlag = true;
        }
    }

    /**
     * Polls the flow queue, moving the quest to the next stage. Also
     * performs a check to make sure the quest hasn't
     * already been started. This method should only be called
     * <b>once</b> from one of the {@link QuestInteraction} subclasses.
     * Subsequent advances of the quest flow should be made using
     * {@link Quest#advance()}.
     */
    public final void start() {
        if(player.getQM().startQuest(this))
            currentStage = flow.poll();
    }

    /**
     * Polls the flow queue, moving the quest to the next stage.
     * If the result of the {@link Queue#poll()} call returns null,
     * {@link Quest#finishQuest()} will automatically be called,
     * and the quest manager will add this quest to the list of finished quests.
     */
    public final void advance() {
        if(!player.getQM().getQuestsInProgress().contains(this))
            return;
        if((currentStage = flow.poll()) == null) {
            player.getQM().finishQuest(this);
        }
    }

    public final void addQuestPoints(int qp) {
        player.getQM().addQuestPoints(qp);
    }

    /**
     * Accessor to obtain the current quest stage. This function
     * is used internally by the framework.
     * @return the current stage of the quest
     */
    QuestStage getCurrentStage() {
        return currentStage;
    }

}
