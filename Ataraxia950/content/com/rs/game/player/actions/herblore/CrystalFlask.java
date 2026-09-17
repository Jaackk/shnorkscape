package com.rs.game.player.actions.herblore;

import com.rs.game.item.Item;

public class CrystalFlask {

	public enum CrystalPot {

		GRAND_STRENGTH(new Item[] { new Item(113, 1), new Item(2440, 1) }, 32958, 75, 123.8, 0),

		GRAND_RANGING(new Item[] { new Item(2444, 1), new Item(27504, 1) }, 32970, 76, 144.4, 1),

		GRAND_MAGE(new Item[] { new Item(3040, 1), new Item(27512, 1) }, 32982, 77, 155.7, 2),

		GRAND_ATTACK(new Item[] { new Item(2428, 1), new Item(2436, 1) }, 32994, 78, 93.8, 3),

		GRAND_DEFENCE(new Item[] { new Item(2432, 1), new Item(2442, 1) }, 33006, 79, 146.3, 4),

		SUPER_MELEE(new Item[] { new Item(2436, 1), new Item(2440, 1), new Item(2442, 1) }, 33018, 81, 200, 5),

		SUPER_WARMASTER(new Item[] { new Item(2436, 1), new Item(2440, 1), new Item(2442, 1), new Item(3040, 1), new Item(2444, 1) }, 33030, 85, 250, 6),

		SUPER_PRAYER_RENEWAL(new Item[] { new Item(2434, 1), new Item(21630, 1) }, 33186, 96, 208.2, 7),

		WYRMFIRE(new Item[] { new Item(2452, 1), new Item(15304, 1) }, 33054, 89, 260, 16),

		REPLENISHMENT(new Item[] { new Item(15300, 1), new Item(3024, 1) }, 33042, 87, 250, 18),

		ENHANCED_REPLENISHMENT(new Item[] { new Item(33042, 1), new Item(39067, 1) }, 39230, 90, 280, 25),

		EXTREME_BRAWLER(new Item[] { new Item(15308, 1), new Item(15312, 1), new Item(15316, 1) }, 33066, 91, 290, 8),

		EXTREME_WARMASTER(new Item[] { new Item(15308, 1), new Item(15312, 1), new Item(15316, 1), new Item(15324, 1), new Item(15320, 1) }, 33102, 93, 300, 100),

		EXTREME_SHARPSHOOTER(new Item[] { new Item(15324, 1), new Item(15316, 1) }, 33090, 93, 300, 9),

		EXTREME_BATTLEMAGE(new Item[] { new Item(15320, 1), new Item(15316, 1) }, 33078, 93, 300, 10),

		SUPREME_STRENGTH(new Item[] { new Item(2440, 1), new Item(15312, 1) }, 33114, 93, 266.3, 11),

		SUPREME_ATTACK(new Item[] { new Item(15308, 1), new Item(2436, 1) }, 33126, 93, 267, 12),

		SUPREME_DEFENCE(new Item[] { new Item(15316, 1), new Item(2442, 1) }, 33138, 93, 267, 13),

		SUPREME_MAGIC(new Item[] { new Item(15320, 1), new Item(3040, 1) }, 33150, 93, 267, 14),

		SUPREME_RANGING(new Item[] { new Item(15324, 1), new Item(2444, 1) }, 33162, 93, 267, 15),

		BRIGHTFIRE(new Item[] { new Item(15304, 1), new Item(21630, 1) }, 33174, 94, 300, 17),

		PERFECT_PLUS(new Item[] { new Item(15332, 1), new Item(32270, 1), new Item(32947, 1) }, 33234, 99, 600, 19),

		HOLY_OVERLOAD(new Item[] { new Item(15332, 1), new Item(21630, 1) }, 33246, 97, 350, 20),

		SEARING_OVERLOAD(new Item[] { new Item(15332, 1), new Item(15304, 1) }, 33258, 97, 350, 21),

		OVERLOAD_SALVE(new Item[] { new Item(15332, 1), new Item(21630, 1), new Item(15304, 1), new Item(2452, 1), new Item(2448, 1), new Item(2434, 1) }, 33198, 97, 400, 22),

		SUPREME_OVERLOAD(new Item[] { new Item(15332, 1), new Item(2436, 1), new Item(2442, 1), new Item(2440, 1), new Item(2444, 1), new Item(3040, 1) }, 33210, 98, 450, 23),

		SUPREME_OVERLOAD_SALVE(new Item[] { new Item(33210, 1), new Item(2434, 1), new Item(15304, 1), new Item(21630, 1), new Item(2448, 1) }, 33222, 99, 600, 25);

		private final Item[] requiredPotions;
		private final int producedPotion;
		private final int levelRequired;
		private final int order;
		private final double baseXP;

		CrystalPot(Item[] requiredPotions, int producedPotion, int levelRequired, double baseXP, int order) {
			this.requiredPotions = requiredPotions;
			this.producedPotion = producedPotion;
			this.levelRequired = levelRequired;
			this.baseXP = baseXP;
			this.order = order;
		}

		public static CrystalPot getCrystalPotion(int id) {
			for (CrystalPot cpotion : CrystalPot.values()) {
				if (cpotion.getProducedPotion() == id)
					return cpotion;
			}
			return null;
		}

		public Item[] getRequiredPotion() {
			return requiredPotions;
		}

		public int getProducedPotion() {
			return producedPotion;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public double getBaseXP() {
			return baseXP;
		}

		public int getOrder() {
			return order;
		}
	}
}
