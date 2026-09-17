package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public final class RunespanData {

	public static final int FIRE_RUNE = 24213;
	public static final int WATER_RUNE = 24214;
	public static final int AIR_RUNE = 24215;
	public static final int EARTH_RUNE = 24216;
	public static final int MIND_RUNE = 24217;
	public static final int BODY_RUNE = 24218;
	public static final int DEATH_RUNE = 24219;
	public static final int NATURE_RUNE = 24220;
	public static final int CHAOS_RUNE = 24221;
	public static final int LAW_RUNE = 24222;
	public static final int COSMIC_RUNE = 24223;
	public static final int ASTRAL_RUNE = 24224;
	public static final int BLOOD_RUNE = 24225;
	public static final int SOUL_RUNE = 24226;
	public static final int RUNE_ESSENCE = 24227;
	public static final int RUNE_DUST = 24228;

	private static final int[] RUNE_IDS = {
			AIR_RUNE, MIND_RUNE, WATER_RUNE, EARTH_RUNE, FIRE_RUNE, BODY_RUNE, COSMIC_RUNE,
			CHAOS_RUNE, ASTRAL_RUNE, NATURE_RUNE, LAW_RUNE, DEATH_RUNE, BLOOD_RUNE, SOUL_RUNE
	};

	private static final int[] RUNE_LEVELS = {
			1, 1, 5, 9, 14, 20, 27, 35, 40, 44, 54, 65, 77, 90
	};

	private static final double[] POINTS_BY_ITEM_OFFSET = {
			0.5, 0.3, 0.1, 0.4, 0.2, 0.7, 2.5, 1.5, 1.1, 1.7, 0.9, 1.3, 3.0, 3.5, 0.0
	};

	private RunespanData() {
	}

	public static boolean isRunespanRuneOrEssence(int itemId) {
		return itemId >= FIRE_RUNE && itemId <= RUNE_ESSENCE;
	}

	public static boolean isRunespanRune(int itemId) {
		return itemId >= FIRE_RUNE && itemId <= SOUL_RUNE;
	}

	public static double getRunePointValue(int itemId) {
		int index = itemId - FIRE_RUNE;
		if (index < 0 || index >= POINTS_BY_ITEM_OFFSET.length)
			return 0;
		return POINTS_BY_ITEM_OFFSET[index];
	}

	public static int getInventoryPointValue(Player player) {
		double value = 0;
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item == null || !isRunespanRune(item.getId()))
				continue;
			value += getRunePointValue(item.getId()) * item.getAmount();
		}
		return (int) Math.floor(value + 1.0E-9);
	}

	public static int[] getAvailableRuneIds(int runecraftingLevel) {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < RUNE_IDS.length; i++) {
			if (runecraftingLevel >= RUNE_LEVELS[i])
				ids.add(RUNE_IDS[i]);
		}
		int[] available = new int[ids.size()];
		for (int i = 0; i < ids.size(); i++)
			available[i] = ids.get(i);
		return available;
	}

	public static int getStarterRuneCost(int runecraftingLevel) {
		double cost = 0;
		for (int runeId : getAvailableRuneIds(runecraftingLevel))
			cost += getRunePointValue(runeId) * 10;
		return (int) Math.floor(cost + 1.0E-9);
	}

	public static int getRandomRuneId() {
		return RUNE_IDS[Utils.random(RUNE_IDS.length)];
	}

	public static int getRandomAvailableRuneId(int runecraftingLevel) {
		int[] ids = getAvailableRuneIds(runecraftingLevel);
		if (ids.length == 0)
			return AIR_RUNE;
		return ids[Utils.random(ids.length)];
	}
}
