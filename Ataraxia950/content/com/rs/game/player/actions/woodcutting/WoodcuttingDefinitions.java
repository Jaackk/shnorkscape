package com.rs.game.player.actions.woodcutting;

import com.google.common.collect.ImmutableList;

public class WoodcuttingDefinitions {

	public enum TreeDefinitions {
		NORMAL(1, 25, 1511, 20, 4, 1341, 8, 10),
		EVERGREEN(1, 25, 1511, 20, 4, 57931, 8, 0),
		DEAD(1, 25, 1511, 20, 4, 12733, 8, 0),
		FRUIT_TREES(1, 25, -1, 20, 4, 1341, 8, 0),
		OAK(15, 37.5, 1521, 30, 4, 1341, 15, 15),
		WILLOW(30, 67.5, 1519, 50, 4, 1341, 51, 15),
		MAPLE(45, 100, 1517, 55, 16, 31057, 72, 10),
		MAHOGANY(50, 125, 6332, 55, 16, 31057, 72, 10),
		YEW(60, 175, 1515, 60, 13, 1341, 94, 10),
		IVY(68, 332.5, -1, 70, 14, 46319, 58, 10),
		MAGIC(75, 250, 1513, 70, 15, 37824, 121, 10),
		TEAK(35, 85, 6333, 60, 4, -1, 51, 15),
		CURSED_MAGIC(82, 250, 1513, 80, 21, 37822, 121, 10),
		BLOOD(85, 320, 24121, 80, 21, -1, 500, 6),
		JADE_ROOT_HEALTHY(83, 100, 21349, 30, 10, -1, 180, 5),
		JADE_ROOT_MUTATED(83, 100, 21358, 30, 10, -1, 180, 5),
		DONOR_TREE(1, 25, -1, 80, 10, -1, -1, -1),
		TANGLE_GUM_VINE(1, 35, 17682, 20, 4, 49706, 8, 5),
		SEEPING_ELM_TREE(10, 60, 17684, 25, 4, 49708, 12, 5),
		BLOOD_SPINDLE_TREE(20, 85, 17686, 35, 4, 49710, 16, 5),
		UTUKU_TREE(30, 115, 17688, 60, 4, 49712, 51, 5),
		SPINEBEAM_TREE(40, 145, 17690, 60, 16, 49714, 68, 5),
		BOVISTRANGLER_TREE(50, 175, 17692, 60, 16, 49716, 75, 5),
		THIGAT_TREE(60, 210, 17694, 70, 16, 49718, 83, 10),
		CORPESTHORN_TREE(70, 245, 17696, 70, 16, 49720, 90, 10),
		ENTGALLOW_TREE(80, 285, 17698, 70, 17, 49722, 94, 10),
		GRAVE_CREEPER_TREE(90, 330, 17700, 80, 21, 49724, 121, 10),
		CRYSTAL_TREE_SHARD(94, 434.5, -1, 80, 30, -1, 10, -1),
		BAMBOO(90, 202.5, 37770, 20, 15, 104008, 30, 15),
		DREAM_TREE(85, 387.1, 34528, 80, 25, -1, -1, -1),
		ELDER(90, 325, 29556, 80, 25, 37824, 150, 10);

		private final int level;
		private final double xp;
		private final int logsId;
		private final int logBaseTime;
		private final int logRandomTime;
		private final int stumpId;
		private final int respawnDelay;
		private final int randomLifeProbability;

		TreeDefinitions(int level, double xp, int logsId, int logBaseTime, int logRandomTime, int stumpId, int respawnDelay, int randomLifeProbability) {
			this.level = level;
			this.xp = xp;
			this.logsId = logsId;
			this.logBaseTime = logBaseTime;
			this.logRandomTime = logRandomTime;
			this.stumpId = stumpId;
			this.respawnDelay = respawnDelay;
			this.randomLifeProbability = randomLifeProbability;
		}

		public int getLevel() {
			return level;
		}

		public int getLogBaseTime() {
			return logBaseTime;
		}

		public int getLogRandomTime() {
			return logRandomTime;
		}

		public int getLogsId() {
			return logsId;
		}

		public int getRandomLifeProbability() {
			return randomLifeProbability;
		}

		public int getRespawnDelay() {
			return respawnDelay;
		}

		public int getStumpId() {
			return stumpId;
		}

		public double getXp() {
			return xp;
		}
	}

	public enum HatchetDefinitions {

		BRONZE(1351, 1, 1, 879, 480),
		IRON(1349, 1, 2, 877, 482),
		STEEL(1353, 6, 3, 875, 484),
		BLACK(1361, 11, 4, 873, 0),
		MITHRIL(1355, 21, 5, 871, 486),
		ADAMANT(1357, 31, 7, 869, 488),
		RUNE(1359, 41, 10, 867, 490),
		DRAGON(6739, 61, 13, 2846, 0),
		INFERNO(13661, 61, 13, 10251, 0),
		CRYSTAL(32645, 71, 14, 25165, 0),
		NOVITE_HATCHET(16361, 1, 1, 13118, 0),
		BATHUS_HATCHET(16363, 10, 2, 13119, 0),
		MARMAROS_HATCHET(16365, 20, 3, 13120, 0),
		KRATONITE_HATCHET(16367, 30, 4, 13121, 0),
		FRACTITE_HATCHET(16369, 40, 5, 13122, 0),
		ZEPHYRIUM_HATCHET(16371, 50, 6, 13123, 0),
		ARGONITE_HATCHET(16373, 60, 7, 13124, 0),
		KATAGON_HATCHET(16375, 70, 8, 13125, 0),
		GORGONITE_HATCHET(16377, 80, 9, 13126, 0),
		PROMETHIUM_HATCHET(16379, 90, 10, 13127, 0),
		PRIMAL_HATCHET(16381, 99, 11, 13128, 0);

		private final int itemId;
		private final int levelRequired;
		private final int axeTime;
		private final int emoteId;
		private final int axeHead;
		public static final ImmutableList<HatchetDefinitions> ALL = ImmutableList.copyOf(values());

		HatchetDefinitions(int itemId, int levelRequried, int axeTime, int emoteId, int axeHead) {
			this.itemId = itemId;
			this.levelRequired = levelRequried;
			this.axeTime = axeTime;
			this.emoteId = emoteId;
			this.axeHead = axeHead;
		}

		public int getItemId() {
			return itemId;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public int getAxeTime() {
			return axeTime;
		}

		public int getEmoteId() {
			return emoteId;
		}

		public int getAxeHeadForItem() {
			return axeHead;
		}
	}
}
