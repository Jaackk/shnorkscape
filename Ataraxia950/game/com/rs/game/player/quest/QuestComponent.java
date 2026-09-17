package com.rs.game.player.quest;

import com.rs.game.player.Player;
import com.rs.game.player.quest.impl.DwarfCannon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A component that is used for storing, and handling quests.
 *
 * @author Thomas Le Godais <thomaslegodais@live.com>
 */
public class QuestComponent implements Serializable {

	private static final long serialVersionUID = 6032476188090490691L;

	/**
	 * A map containing current quests.
	 **/
	private final Map<Integer, AbstractQuest<?, ?>> questMap = new HashMap<Integer, AbstractQuest<?, ?>>();

	/**
	 * Constructs a new QuestComponent instance.
	 */
	public QuestComponent() {
		for (Quests quests : Quests.values()) {
			questMap.put(quests.ordinal(), quests.getQuest());
		}
	}

	public Map<Integer, AbstractQuest<?, ?>> getQuestMap() {
		return questMap;
	}

	/**
	 * Starts a quest based on the id.
	 *
	 * @param player
	 *            The player.
	 * @param questId
	 *            The quest id.
	 */
	public void startQuest(Player player, int questId) {
		questMap.get(questId).handleQuest(player);
	}

	/**
	 * Gets a certain quest from the map.
	 *
	 * @param questIndex
	 *            the index.
	 * @return the quest.
	 */
	public AbstractQuest<?, ?> getQuest(int questIndex) {
		return questMap.get(questIndex);
	}

	/**
	 * Gets the quests.
	 *
	 * @return the quests.
	 */
	public AbstractQuest<?, ?> getQuests() {
		for (int index = 0; index < questMap.size();)
			return questMap.get(index);
		return null;
	}

	public enum Quests {

		DO_NOT_REMOVE(null),

		DWARF_CANNON(new DwarfCannon());

		private final AbstractQuest<?, ?> quest;

		Quests(AbstractQuest<?, ?> quest) {
			this.quest = quest;
		}

		public AbstractQuest<?, ?> getQuest() {
			return quest;
		}
	}
}