package com.rs.game.player.questing.framework.quest;

import com.rs.game.player.Player;
import com.rs.game.player.questing.framework.conditions.InteractionKey;

/**
 * Represents an interaction the player can have during
 * the quest. Each {@link QuestStage} can have multiple
 * types of interactions. The quest interaction is
 * characterized by an {@link InteractionType}, and mapped
 * to an {@link InteractionKey}. When the node of the
 * {@link QuestStage} is processed in the packet handlers,
 * the interaction will be triggered by matching keys.<br/><br/>
 *
 * Each quest interaction must implement success-mode behavior and
 * fail-mode behavior. Which behavior is transmitted to the player
 * is determined by a {@link java.util.function.Predicate}.
 *
 * @author David O'Neill
 */
public abstract class QuestInteraction {

    /**
     * Small enum containing the different interaction types.
     */
    public enum InteractionType {
        OBJECT_1_INTERACTION,
        OBJECT_2_INTERACTION,
        NPC_1_INTERACTION,
        NPC_2_INTERACTION,
        USE_ITEM_INTERACTION,
        WALK_INTERACTION,
        PROCESS_PLAYER
    }

    private final InteractionType type;

    private final InteractionKey<?> key;

    protected Quest quest;

    /**
     * Constructs a new {@code QuestInteraction}. The interaction
     * belongs to a quest, has an {@link InteractionType}, and an
     * interaction key which is passed as a generic {@link Object},
     * and then converted to an {@link InteractionKey}.
     *
     * @param quest the quest this interaction belongs to
     * @param type the type of interaction this represents
     * @param key a generic object key corresponding to the interaction type.
     *            for example, NPC and Object interactions should be passed an
     *            {@code int} corresponding to the NPC/Object ID that should trigger
     *            this interaction
     */
    protected QuestInteraction(Quest quest, InteractionType type, Object key) {
        this.quest = quest;
        this.type = type;
        this.key = initKey(key);
    }

    /**
     * Transforms the generic object key into an {@link InteractionKey}
     * @param __Key the object key
     * @return an {@link InteractionKey} wrapping around the object key.
     */
    private InteractionKey<?> initKey(Object __Key) {
        InteractionKey<?> _Key = null;

        if(     type == InteractionType.OBJECT_1_INTERACTION ||
                type == InteractionType.NPC_1_INTERACTION ||
                type == InteractionType.WALK_INTERACTION ||
                type == InteractionType.OBJECT_2_INTERACTION ||
                type == InteractionType.NPC_2_INTERACTION )
        {
            _Key = InteractionKey.intInteractionKey((Integer) __Key);
        } else if(type == InteractionType.USE_ITEM_INTERACTION || type == InteractionType.PROCESS_PLAYER) {
            _Key = InteractionKey.intArrayInteractionKey((int[]) __Key);
        }

        return _Key;
    }

    public Quest getQuest() {
        return quest;
    }

    /**
     * Accessor to this interaction's {@link InteractionKey}.
     * @return
     */
    InteractionKey<?> getInteractionKey() {
        return key;
    }

    /**
     * Accessor to this interaction's {@link InteractionType}
     * @return
     */
    InteractionType getType() {
        return type;
    }

    /**
     * Describes the sucess-mode behavior of this interaction.
     * This method should implement what the player should experience
     * when they have successfully acquired/completed some condition
     * to advance to the next stage.<br/><br/>
     *
     * <b>NOTE:</b> This is where {@link Quest#start()} call or {@link Quest#advance()}
     * should be called if this interaction is the trigger interaction for officially
     * starting the quest, or advancing the quest to the next stage.
     * @param player the player
     */
    public abstract void succeed(Player player);

    /**
     * Describes the fail-mode behavior of this interaction.
     * This method should implement what the player should experience
     * when they have not yet acquired/completed some condition
     * to advance to the next stage.
     * @param player the player
     */
    public abstract void fail(Player player);

}
