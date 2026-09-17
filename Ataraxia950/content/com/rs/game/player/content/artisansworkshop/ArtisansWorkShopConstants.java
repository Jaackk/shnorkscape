package com.rs.game.player.content.artisansworkshop;

import com.rs.game.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ArtisansWorkShopConstants {
	public enum Ingot {
		// IRON INGOTS
		IRON_I(20632, 30, 101, new int[] { 20572, 20577, 20582, 20587 }, new Item(440, 1)),

		IRON_II(20637, 30, 202, new int[] { 20592, 20597, 20602, 20607 }, new Item(440, 9)),

		IRON_III(20642, 30, 240, new int[] { 20612, 20617, 20622, 20627 }, new Item(440, 12)),

		IRON_IV(20648, 70, 2100, new int[] { 20567 }, new Item(440, 75)),

		// STEEL INGOTS
		STEEL_I(20633, 45, 131, new int[] { 20573, 20578, 20583, 20588 }, new Item(440, 1), new Item(453, 2)),

		STEEL_II(20638, 45, 253, new int[] { 20593, 20598, 20603, 20608 }, new Item(440, 4), new Item(453, 7)),

		STEEL_III(20643, 45, 354, new int[] { 20613, 20618, 20623, 20628 }, new Item(440, 9), new Item(453, 17)),

		STEEL_IV(20649, 75, 2300, new int[] { 20568 }, new Item(440, 40), new Item(453, 80)),

		// MITHRIL INGOTS
		MITHRIL_I(20634, 60, 164, new int[] { 20574, 20579, 20584, 20589 }, new Item(447, 1), new Item(453, 4)),

		MITHRIL_II(20639, 60, 316, new int[] { 20594, 20599, 20604, 20609 }, new Item(447, 3), new Item(453, 12)),

		MITHRIL_III(20644, 60, 404, new int[] { 20614, 20619, 20624, 20629 }, new Item(447, 6), new Item(453, 24)),

		MITHRIL_IV(20650, 80, 2723, new int[] { 20569 }, new Item(447, 30), new Item(453, 120)),

		// ADAMANT INGOTS
		ADAMANT_I(20635, 70, 278, new int[] { 20575, 20580, 20585, 20590 }, new Item(449, 1), new Item(453, 6)),

		ADAMANT_II(20640, 70, 455, new int[] { 20595, 20600, 20605, 20610 }, new Item(449, 3), new Item(453, 14)),

		ADAMANT_III(20645, 70, 568, new int[] { 20615, 20620, 20625, 20630 }, new Item(449, 4), new Item(453, 22)),

		ADAMANT_IV(20651, 85, 3436, new int[] { 20570 }, new Item(449, 25), new Item(453, 150)),

		// RUNE INGOTS
		RUNE_I(20636, 90, 505, new int[] { 20576, 20581, 20586, 20591 }, new Item(451, 1), new Item(453, 8)),

		RUNE_II(20641, 90, 631, new int[] { 20596, 20601, 20606, 20611 }, new Item(451, 2), new Item(453, 16)),

		RUNE_III(20646, 90, 758, new int[] { 20616, 20621, 20626, 20631 }, new Item(451, 4), new Item(453, 30)),

		RUNE_IV(20652, 90, 4279, new int[] { 20571 }, new Item(451, 18), new Item(453, 144)),;
		private static final Map<Integer, Ingot> ingots = new HashMap<Integer, Ingot>();

		static {
			for (Ingot ingot : Ingot.values()) {
				ingots.put(ingot.getItemId(), ingot);
			}
		}

		public static Ingot forId(int itemId) {
			return ingots.get(itemId);
		}

		private final int itemId;
		private final int requiredLevel;
		private final int[] products;
		private final Item[] requiredItems;
		private final double xp;

		Ingot(int itemId, int requiredLevel, double xp, int[] products, Item... requiredItems) {
			this.itemId = itemId;
			this.xp = xp * 1.5;
			this.products = products;
			this.requiredLevel = requiredLevel;
			this.requiredItems = requiredItems;
		}

		public int getItemId() {
			return itemId;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}

		public int[] getProducts() {
			return products;
		}

		public Item[] getRequiredItems() {
			return requiredItems;
		}

		public double getXp() {
			return xp;
		}

	}

	public enum Track {
		BRONZE_RAILS(20506, 1, 1.4, 20502),

		BRONZE_BASE_PLATE(20507, 2, 1.4, 20502),

		BRONZE_SPIKES(20508, 5, 1.4, 20502),

		BRONZE_JOINT(20509, 8, 1.4, 20502),

		BRONZE_TIE(20510, 11, 1.4, 20502),

		BRONZE_TRACK_40(20511, 3, 6, 20506, 20507),

		BRONZE_TRACK_60(20512, 6, 7, 20511, 20508),

		BRONZE_TRACK_80(20513, 9, 9, 20512, 20509),

		BRONZE_TRACK_100(20514, 12, 10, 20513, 20510),

		IRON_RAILS(20515, 15, 5.1, 20503),

		IRON_BASE_PLATE(20516, 19, 5.1, 20503),

		IRON_SPIKES(20517, 24, 5.1, 20503),

		IRON_JOINT(20518, 29, 5.1, 20503),

		IRON_TIE(20519, 34, 5.1, 20503),

		IRON_TRACK_40(20525, 20, 10, 20515, 20516),

		IRON_TRACK_60(20526, 25, 11, 20525, 20517),

		IRON_TRACK_80(20527, 30, 12, 20526, 20518),

		IRON_TRACK_100(20528, 35, 13, 20527, 20519),

		STEEL_RAILS(20520, 39, 8.8, 20504),

		STEEL_BASE_PLATE(20521, 44, 8.8, 20504),

		STEEL_SPIKES(20522, 49, 8.8, 20504),

		STEEL_JOINT(20523, 54, 8.8, 20504),

		STEEL_TIE(20524, 59, 8.8, 20504),

		STEEL_TRACK_40(20529, 45, 13, 20520, 20521),

		STEEL_TRACK_60(20530, 50, 16, 20529, 20522),

		STEEL_TRACK_80(20531, 55, 22, 20530, 20523),

		STEEL_TRACK_100(20532, 60, 25, 20531, 20524);

		private final int itemId;
		private final int requiredLevel;
		private final double xp;
		private final int[] requiredItems;

		Track(int itemId, int requiredLevel, double xp, int... requiredItems) {
			this.itemId = itemId;
			this.requiredLevel = requiredLevel;
			this.xp = xp * 2.5;
			this.requiredItems = requiredItems;
		}

		public int getItemId() {
			return itemId;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}

		public double getXp() {
			return xp;
		}

		public int[] getRequiredItems() {
			return requiredItems;
		}

	}

	public static int START_SPRITE = 4710;

	public enum CeremonialSword {
		SWORD_1(new int[][] { { 3, 4, 4, 2, 2, 3, 3, 8 } }),

		SWORD_2(new int[][] { { 5, 5, 3, 5, 5, 5, 3, 8 } }),

		SWORD_3(new int[][] { { 4, 4, 3, 4, 4, 4, 3, 8 } }),

		SWORD_4(new int[][] { { 5, 4, 4, 5, 5, 5, 4, 8 }, { 4, 3, 3, 4, 4, 4, 1, 8 } }),

		SWORD_5(new int[][] { { 4, 5, 2, 4, 4, 3, 0, 8 } }),

		SWORD_6(new int[][] { { 0, 2, 2, 1, 1, 1, 1, 8 }, { 4, 3, 1, 4, 1, 1, 1, 1 } }),

		SWORD_7(new int[][] { { 5, 5, 4, 4, 5, 2, 1, 8 } }),

		SWORD_8(new int[][] { { 5, 5, 4, 4, 5, 2, 4, 8 } }),

		SWORD_9(new int[][] { { 4, 2, 3, 3, 2, 2, 2, 8 } }),

		SWORD_10(new int[][] { { 4, 3, 1, 3, 1, 3, 1, 8 } }),

		SWORD_11(new int[][] { { 4, 3, 3, 3, 3, 3, 3, 8 }, { 4, 4, 5, 5, 2, 5, 2, 8 } }),

		SWORD_12(new int[][] { { 4, 4, 4, 3, 4, 4, 2, 8 } }),

		SWORD_13(new int[][] { { 3, 3, 2, 2, 2, 1, 1, 8 } }),

		SWORD_14(new int[][] { { 4, 3, 3, 3, 3, 3, 3, 8 }, { 3, 2, 4, 2, 2, 2, 2, 8 } }),

		SWORD_15(new int[][] { { 4, 3, 3, 4, 4, 4, 1, 8 }, { 4, 3, 3, 4, 3, 1, 1, 8 } }),

		SWORD_16(new int[][] { { 5, 5, 5, 5, 5, 5, 5, 8 }, { 3, 3, 3, 1, 3, 1, 1, 8 } }),

		SWORD_17(new int[][] { { 6, 6, 5, 5, 5, 4, 3, 8 } }),

		SWORD_18(new int[][] { { 5, 4, 3, 5, 5, 5, 2, 8 } }),

		SWORD_19(new int[][] { { 5, 5, 4, 4, 5, 2, 1, 8 } });

		private final int[][] design;

		CeremonialSword(int[][] design) {
			this.design = design;
		}

		public int[][] getDesign() {
			return design;
		}

		public int getCoolDown() {
			int totalHits = 0;
			for (int i = 0; i < design.length; i++)
				for (int j = 0; j < design[i].length; j++)
					totalHits += design[i][j] / 1.7;
			if (design.length == 1)
				totalHits *= 2;
			return totalHits;
		}

	}
}
