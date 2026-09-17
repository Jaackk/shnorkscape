package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementDifficulty;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.achievementsystem.Achievements;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 25, 2018.
 */
public class AchievementInterface {

	public static final int INTERFACE_ID = 126;

	public static void open(Player player) {
		player.setSelectedAchivementsTab(null);
		player.setSelectedAchivement(null);
		sendListOfAchievements(player, true);
		sendProgressInformation(player, true);
		sendRewards(player, true);
		sendTabs(player);
		player.getPackets().sendIComponentText(INTERFACE_ID, 17, "Achievements");
		player.getPackets().sendIComponentText(INTERFACE_ID, 92, "");
		player.getInterfaceManager().sendInterface(INTERFACE_ID);
	}

	public static void handleButtons(Player player, int buttonId, int slotId) {
		if (buttonId >= 77 && buttonId <= 80) {// tabs
			AchievementDifficulty tab = getSelectedTab(buttonId);
			if (tab == null)
				return;

			player.setSelectedAchivementsTab(tab);
			sendTabs(player);
			selectTab(player, buttonId, tab.name(), tab.getDifficultyColor());
			sendListOfAchievements(player, false);
		} else if (buttonId >= 49 && buttonId <= 76) {// tasks
			selectAchievement(player, buttonId);
			sendProgressInformation(player, false);
			populateProgressInformation(player, buttonId);
			sendRewards(player, false);
			populateRewards(player, buttonId);
		} else if (buttonId == 87) {// examine rewards
			AchievementList list = player.getSelectedAchivement();
			if (list == null)
				return;

			ItemsContainer<Item> rewards = getRewards(list);
			if (rewards == null)
				return;

			Item item = rewards.get(slotId);
			if (item == null)
				return;

			player.sendMessage(ItemExaminesDataParser.getExamine(item));
		}
	}

	private static void sendTabs(Player player) {
		for (AchievementDifficulty difficulty : AchievementDifficulty.values()) {
			if (difficulty == null)
				continue;

			player.getPackets().sendIComponentText(INTERFACE_ID, (difficulty.ordinal() + 77), indent(difficulty.name(), (10 - difficulty.name().length()), true));
		}
	}

	private static void selectTab(Player player, int componentId, String string, String color) {
		player.getPackets().sendIComponentText(INTERFACE_ID, componentId, (color + indent(string, (10 - string.length()), true)));
	}

	private static void selectAchievement(Player player, int buttonId) {
		AchievementList list = getSelectedAchievement(player, buttonId);
		if (list == null)
			return;

		player.setSelectedAchivement(list);
	}

	private static String indent(String string, int spaces, boolean format) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i <= spaces; i++)
			builder.append(" ");
		return (format ? Utils.formatString(builder + string) : (builder + string));
	}

	private static void sendListOfAchievements(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(INTERFACE_ID, 23, hide);
		player.getPackets().sendHideIComponent(INTERFACE_ID, 36, hide);
		for (int component = 47; component <= 76; component++)
			player.getPackets().sendHideIComponent(INTERFACE_ID, component, hide);
		if (!hide)
			populateAchievementList(player);
		// 48 = scroll
		// 49 = 76 = text
		// 47 = text container
		// 36 = container background
		// 23 = container
	}

	private static void populateAchievementList(Player player) {
		for (int component = 49; component <= 76; component++)
			player.getPackets().sendIComponentText(INTERFACE_ID, component, "");

		List<String> toList = new ArrayList<String>();
		for (AchievementList list : AchievementList.values()) {
			if (list == null)
				continue;

			if (list.getDifficulty() == player.getSelectedAchivementsTab())
				toList.add(player.getAchievements().getCurrentState(list).getStateColor() + list.getMiniName());
		}

		for (String name : toList) {
			if (name == null)
				continue;

			player.getPackets().sendIComponentText(INTERFACE_ID, toList.indexOf(name) + 49, name);
		}
	}

	private static void sendProgressInformation(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(INTERFACE_ID, 28, hide);
		// 28 = container
	}

	private static void populateProgressInformation(Player player, int buttonId) {
		AchievementList list = getSelectedAchievement(player, buttonId);
		if (list == null)
			return;

		player.getPackets().sendIComponentText(INTERFACE_ID, 83, indent(list.getMiniName(), (17 - list.getMiniName().length()), false));
		player.getPackets().sendIComponentText(INTERFACE_ID, 85, list.getName());

		Achievements achievements = player.getAchievements();
		if (achievements == null)
			return;

		int percent = (int) Math.floor((achievements.getCurrentProgressAmount(list) * 100.0f) / list.getAmountToComplete());
		player.getPackets().sendIComponentText(INTERFACE_ID, 84, "Progress: " + Math.min(percent, 100) + "% (" + achievements.getCurrentProgressAmount(list) + "/" + list.getAmountToComplete() + ")");
	}

	private static void sendRewards(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(INTERFACE_ID, 30, hide);
		for (int component = 89; component <= 95; component++) {
			if (component == 94)
				continue;
			player.getPackets().sendHideIComponent(INTERFACE_ID, component, hide);
		}
		if (!hide) {
			player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, 87, 90, 5, 8, "Examine");
			player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, 87, 0, 160, 0);
		}
		// 30 = rewards container
		// 89 = scrollbar
		// 91 = possible rewards text
		// 95 = possible rewards outline
	}

	private static void populateRewards(Player player, int buttonId) {
		AchievementList list = getSelectedAchievement(player, buttonId);
		if (list == null)
			return;

		ItemsContainer<Item> rewards = getRewards(list);
		if (rewards == null)
			return;

		player.getPackets().sendItems(90, false, rewards);
	}

	private static ItemsContainer<Item> getRewards(AchievementList list) {
		ItemsContainer<Item> rewards = new ItemsContainer<Item>(28, false);
		rewards.add(new Item(list.getReward().getItemId(), list.getReward().getAmount()));
		return rewards;
	}

	private static AchievementDifficulty getSelectedTab(int buttonId) {
		for (AchievementDifficulty difficulty : AchievementDifficulty.values()) {
			if (difficulty == null)
				continue;

			if ((difficulty.ordinal() + 77) == buttonId)
				return difficulty;
		}
		return null;
	}

	private static AchievementList getSelectedAchievement(Player player, int buttonId) {
		List<AchievementList> toList = new ArrayList<AchievementList>();
		for (AchievementList list : AchievementList.values()) {
			if (list == null)
				continue;

			if (list.getDifficulty() == player.getSelectedAchivementsTab())
				toList.add(list);
		}

		for (AchievementList list : toList) {
			if (list == null)
				continue;

			if ((toList.indexOf(list) + 49) == buttonId)
				return list;
		}
		return null;
	}

}
