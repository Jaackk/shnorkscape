package com.rs.game.player;

import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class QuestManager implements Serializable {

	private static final long serialVersionUID = -8085932531253271252L;
	private transient Player player;
	private final List<Quests> completedQuests;
	private HashMap<Quests, Integer> questStages;

	public QuestManager() {
		completedQuests = new ArrayList<Quests>();
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (questStages == null)
			questStages = new HashMap<Quests, Integer>();
	}

	public int getQuestStage(Quests quest) {
		if (completedQuests.contains(quest))
			return -1;
		Integer stage = questStages.get(quest);
		return stage == null ? -2 : stage;
	}

	public void setQuestStageAndRefresh(Quests quest, int stage) {
		setQuestStage(quest, stage);
		sendStageData(quest);
	}

	public void sendQuestInterface(String... data) {
		player.getPackets().sendIComponentText(1245, 330, data[0]);
		player.getInterfaceManager().closeScreenInterface();
		for (int i = 0; i < 30; i++)
			player.getPackets().sendIComponentText(1245, i, "");
		int start = 13;
		for (int i = 0; i < data.length; i++) {
			if (i == 0)
				continue;
			player.getPackets().sendIComponentText(1245, start, data[i]);
			start++;
		}
	}

	public void refreshQuests() {
		player.getVarBitManager().sendVarBit(4291, 1);
		player.getPackets().sendConfigByFile(4536, 1); // sort books
		player.getPackets().sendConfigByFile(358, 0); // desert treasure
		player.getPackets().sendConfigByFile(2448, 0); // lunar diplomacy
		player.getPackets().sendConfigByFile(4684, 0); // perils of ice mountain
		player.getPackets().sendConfig(0, 4);
		if (player.isKilledCulinaromancer())
			player.getPackets().sendConfigByFile(1850, 5);
		else
			player.getPackets().sendConfigByFile(1850, 4);
	}

	public void setQuestStage(Quests quest, int stage) {
		if (completedQuests.contains(quest))
			return;
		questStages.put(quest, stage);
	}

	public void init() {
		checkCompleted(); // temporary
		for (Quests quest : completedQuests)
			sendCompletedQuestsData(quest);
		for (Quests quest : questStages.keySet())
			sendStageData(quest);
	}

	public void checkCompleted() {
		if (!completedQuests.contains(Quests.PERIL_OF_ICE_MONTAINS) && player.getSkills()
				.hasRequiriments(Skills.CONSTRUCTION, 10, Skills.FARMING, 10, Skills.HUNTER, 10, Skills.THIEVING, 11))
			completeQuest(Quests.PERIL_OF_ICE_MONTAINS);
		if (!completedQuests.contains(Quests.KINGS_RANSOM) && player.getSkills().hasRequiriments(Skills.MAGIC, 45, Skills.DEFENCE, 65))
			completeQuest(Quests.KINGS_RANSOM);
	}

	public void completeQuest(Quests quest) {
		completedQuests.add(quest);
		questStages.remove(quest);
		sendCompletedQuestsData(quest);
		player.getPackets().sendGameMessage(
				"<col=ff0000>You have completed quest: " + Utils.formatPlayerNameForDisplay(quest.toString()) + ".");
		// message completed quest
	}

	public void completeQuest2(Quests quest) {
		sendCompletedQuestsData(quest);
		completedQuests.add(quest);
	}

	public void sendCompletedQuestsData(Quests quest) {
		switch (quest) {
		case PERIL_OF_ICE_MONTAINS:
			player.getPackets().sendConfigByFile(4684, 150); // air machine
			// instead of
			// dragons +
			// trapdoor
			// opened
			break;
		case KINGS_RANSOM:
			player.getVarsManager().sendVarBit(3910, 1);
			break;
		case NOMADS_REQUIEM:
			break;
		default:
			break;
		}
	}

	private void sendStageData(Quests quest) {
		switch (quest) {
		case NOMADS_REQUIEM:
			player.getPackets().sendConfigByFile(30519, 3, true);
			break;
		case PERIL_OF_ICE_MONTAINS:
			break;
		default:
			break;
		}
	}

	public boolean completedQuest(Quests quest) {
		return completedQuests.contains(quest);
	}

	public enum Quests {
		PERIL_OF_ICE_MONTAINS,
		NOMADS_REQUIEM,
		KINGS_RANSOM

	}
}
