package com.rs.game.player.actions.smithing.defs;

import com.rs.game.item.Item;
import com.rs.game.player.Skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated SmeltingBar.java for 910 build
 * Includes Orikalkum, Necronium, Bane, Elder Rune, and Dungeoneering bars
 */
public enum SmeltingBar {

	BRONZE(1, 6.2, new Item[] { new Item(436), new Item(438) }, new Item(2349), 0),
	IRON(10, 12.5, new Item[] { new Item(440) }, new Item(2351), 1),
	SILVER(20, 13.7, new Item[] { new Item(442) }, new Item(2355), 2),
	STEEL(20, 17.5, new Item[] { new Item(440), new Item(453, 1) }, new Item(2353), 3),
	GOLD(40, 22.5, new Item[] { new Item(444) }, new Item(2357), 4),
	MITHRIL(30, 30, new Item[] { new Item(447), new Item(453, 1) }, new Item(2359), 5),
	ADAMANT(40, 37.5, new Item[] { new Item(449), new Item(453, 1) }, new Item(2361), 6),
	RUNE(50, 50, new Item[] { new Item(451), new Item(453, 1) }, new Item(2363), 7),

	// Post-rework bars
	ORIKALKUM(60, 40, new Item[] { new Item(44822), new Item(44820) }, new Item(44838), 8),
	NECRONIUM(70, 50, new Item[] { new Item(44826), new Item(44824) }, new Item(44840), 9),
	BANE(80, 60, new Item[] { new Item(44828), new Item(21779) }, new Item(44842), 10),
	ELDER_RUNE(90, 70, new Item[] { new Item(44830), new Item(44832) }, new Item(44844), 11),

	// Special Bars
	DRAGONBANE(80, 50, new Item[] { new Item(21779) }, new Item(21783), 12),
	CORRUPTED_ORE(89, 150, new Item[] { new Item(32262) }, new Item(32262), 13),
	CANNON_BALLS(35, 25.6, new Item[] { new Item(2353), new Item(4) }, new Item(2, 4), 14),

	// Dungeoneering Bars
	NOVITE(1, 7, new Item[] { new Item(17630) }, new Item(17650), Skills.SMITHING),
	BATHUS(10, 13.3, new Item[] { new Item(17632) }, new Item(17652), Skills.SMITHING),
	MARMAROS(20, 19.6, new Item[] { new Item(17634) }, new Item(17654), Skills.SMITHING),
	KRATONITE(30, 25.9, new Item[] { new Item(17636) }, new Item(17656), Skills.SMITHING),
	FRACTITE(40, 32.2, new Item[] { new Item(17638) }, new Item(17658), Skills.SMITHING),
	ZEPHYRIUM(50, 38.5, new Item[] { new Item(17640) }, new Item(17660), Skills.SMITHING),
	ARGONITE(60, 44.8, new Item[] { new Item(17642) }, new Item(17662), Skills.SMITHING),
	KATAGON(70, 51.1, new Item[] { new Item(17644) }, new Item(17664), Skills.SMITHING),
	GORGONITE(80, 57.4, new Item[] { new Item(17646) }, new Item(17666), Skills.SMITHING),
	PROMETHIUM(90, 63.7, new Item[] { new Item(17648) }, new Item(17668), Skills.SMITHING);

	private static final Map<Integer, SmeltingBar> bars = new HashMap<>();

	static {
		for (SmeltingBar bar : SmeltingBar.values()) {
			if (!bars.containsKey(bar.getButtonId()))
				bars.put(bar.getButtonId(), bar);
		}
	}

	private static final SmeltingBar[] FURNACE_BARS = {
			BRONZE, IRON, SILVER, STEEL, GOLD, MITHRIL, ADAMANT, RUNE,
			ORIKALKUM, NECRONIUM, BANE, ELDER_RUNE, DRAGONBANE, CORRUPTED_ORE, CANNON_BALLS
	};

	private final int levelRequired;
	private final double experience;
	private final Item[] itemsRequired;
	private final int buttonId;
	private final Item producedBar;

	SmeltingBar(int levelRequired, double experience, Item[] itemsRequired, Item producedBar, int buttonId) {
		this.levelRequired = levelRequired;
		this.experience = experience;
		this.itemsRequired = itemsRequired;
		this.producedBar = producedBar;
		this.buttonId = buttonId;
	}

	public static SmeltingBar forId(int buttonId) {
		return bars.get(buttonId);
	}

	public static SmeltingBar forProductId(int productId) {
		for (SmeltingBar bar : FURNACE_BARS) {
			if (bar.getProducedBar().getId() == productId)
				return bar;
		}
		return null;
	}

	public static SmeltingBar[] getFurnaceBars() {
		return FURNACE_BARS;
	}

	public int getButtonId() {
		return buttonId;
	}

	public double getExperience() {
		return experience;
	}

	public Item[] getItemsRequired() {
		return itemsRequired;
	}

	public int getLevelRequired() {
		return levelRequired;
	}

	public Item getProducedBar() {
		return producedBar;
	}
}
