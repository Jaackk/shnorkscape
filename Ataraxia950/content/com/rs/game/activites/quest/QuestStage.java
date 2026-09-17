package com.rs.game.activites.quest;

import com.rs.game.player.Player;

import java.io.Serializable;

/**
 * An interface representing a stage in a quest.
 */
public interface QuestStage<T extends AbstractQuest> extends Serializable {

    /**
     * Called when this quest stage is entered.
     */
    default void enter(Player player, T quest) {

    }

    /**
     * Called when this quest stage is exited.
     */
    default void exit(Player player, T quest) {

    }

    /**
     * Displays the description for this quest stage.
     */
    String[] description(Player player, T quest);
}
