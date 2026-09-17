package com.rs.game.player.quest;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;

import java.io.Serializable;

/**
 * Represents a single quest of the Rune-Force server.
 *
 * @param T
 *            An enumeration that is used to handle quest states.
 * @param E
 *            An enumeration that is used to handle interaction.
 * @author Thomas Le Godais <thomaslegodais@live.com>
 */
public abstract class AbstractQuest<T extends Enum<T>, E extends Enum<E>> implements Serializable {

	private static final long serialVersionUID = 2606069341811307619L;

	/**
	 * The name of the quest.
	 **/
	private final String name;

	/**
	 * The id of the quest
	 **/
	private final int questId;

	/**
	 * The state of the quest to handle.
	 **/
	private T questStage;

	/**
	 * The node interaction.
	 **/
	private E nodeInteraction;

	/**
	 * Constructs a new AbstractQuest instance.
	 *
	 * @param name
	 *            The name of the quest.
	 * @param questId
	 *            The id of the quest.
	 * @param questState
	 *            The state of the quest.
	 */
	public AbstractQuest(String name, int questId, T questState, E nodeInteraction) {
		this.name = name;
		this.questId = questId;
		this.questStage = questState;
		this.nodeInteraction = nodeInteraction;
	}

	/**
	 * Handles an quest.
	 *
	 * @param player
	 *            The player to handle the quest.
	 */
	public abstract void handleQuest(Player player);

	/**
	 * Handles the dialogue of the quest.
	 *
	 * @param player
	 *            the player interacting with the npc.
	 * @param npcId
	 *            the npc interacting with the player.
	 */
	public abstract void handleDialogue(Player player, int npcId);

	/**
	 * Handles object clicks.
	 *
	 * @param player
	 *            the player clicking objects.
	 * @param objectId
	 *            the object id.
	 * @param firstClick
	 *            the first click.
	 * @param secondClick
	 *            the second click.
	 * @param thirdClick
	 *            the third click.
	 */
	public abstract void handleObjectClick(Player player, WorldObject object, boolean firstClick, boolean secondClick,
			boolean thirdClick);

	/**
	 * Does the quest contain dialogue?
	 *
	 * @return True, if it does.
	 */
	public abstract boolean hasDialogue();

	/**
	 * Does the quest contain object click?
	 *
	 * @return True, if it does.
	 */
	public abstract boolean hasObjectClick();

	/**
	 * Checks if a player has interacted with a node.
	 *
	 * @param node
	 *            the node to interact.
	 * @return has interacted or not.
	 */
	public abstract boolean hasInteracted(E node);

	/**
	 * Gets the current state of the quest.
	 *
	 * @return The current state.
	 */
	public T getQuestStage() {
		return questStage;
	}

	/**
	 * Sets the current state of the quest.
	 *
	 * @param questStage
	 *            The state of the quest.
	 */
	public void setQuestStage(T questStage) {
		this.questStage = questStage;
	}

	/**
	 * Gets the id of the quest.
	 *
	 * @return The quest id.
	 */
	public int getQuestId() {
		return questId;
	}

	/**
	 * Gets the name of the quest.
	 *
	 * @return The name of the quest.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the node interaction.
	 *
	 * @return the interaction.
	 */
	public E getNodeInteraction() {
		return nodeInteraction;
	}

	/**
	 * Sets the node interaction.
	 *
	 * @param nodeInteraction
	 *            the interaction to set.
	 */
	public void setNodeInteraction(E nodeInteraction) {
		this.nodeInteraction = nodeInteraction;
	}
}