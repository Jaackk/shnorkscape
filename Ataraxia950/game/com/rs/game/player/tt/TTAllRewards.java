package com.rs.game.player.tt;

import com.rs.game.item.Item;

/**
 * @author Kris
 * Using 'amount' field as minimum amount and 'charges' field as maximum amount.
 */
public enum TTAllRewards {

	COINS(new Item(995, 2500, 50000), 50),
	PURPLE_SWEETS(new Item(10476, 2, 27), 50),
	MISCELLANIA_TELEPORT(new Item(19477, 3, 10), 50),
	LUMBER_YARD_TELEPORT(new Item(19480, 3, 10), 50),
	BANDIT_CAMP_TELEPORT(new Item(19476, 3, 10), 50),
	PHOENIX_LAIR_TELEPORT(new Item(19478, 3, 10), 50),
	TAI_BWO_WANNAI_TELEPORT(new Item(19479, 3, 10), 50),
	SARADOMIN_PAGE_1(new Item(3827, 1), 5),
	SARADOMIN_PAGE_2(new Item(3828, 1), 5),
	SARADOMIN_PAGE_3(new Item(3829, 1), 5),
	SARADOMIN_PAGE_4(new Item(3830, 1), 5),
	ZAMORAK_PAGE_1(new Item(3831, 1), 5),
	ZAMORAK_PAGE_2(new Item(3832, 1), 5),
	ZAMORAK_PAGE_3(new Item(3833, 1), 5),
	ZAMORAK_PAGE_4(new Item(3834, 1), 5),
	GUTHIX_PAGE_1(new Item(3835, 1), 5),
	GUTHIX_PAGE_2(new Item(3836, 1), 5),
	GUTHIX_PAGE_3(new Item(3837, 1), 5),
	GUTHIX_PAGE_4(new Item(3838, 1), 5),
	BANDOS_PAGE_1(new Item(19600, 1), 5),
	BANDOS_PAGE_2(new Item(19601, 1), 5),
	BANDOS_PAGE_3(new Item(19602, 1), 5),
	BANDOS_PAGE_4(new Item(19603, 1), 5),
	ARMADYL_PAGE_1(new Item(19604, 1), 5),
	ARMADYL_PAGE_2(new Item(19605, 1), 5),
	ARMADYL_PAGE_3(new Item(19606, 1), 5),
	ARMADYL_PAGE_4(new Item(19607, 1), 5),
	ANCIENT_PAGE_1(new Item(19608, 1), 5),
	ANCIENT_PAGE_2(new Item(19609, 1), 5),
	ANCIENT_PAGE_3(new Item(19610, 1), 5),
	ANCIENT_PAGE_4(new Item(19611, 1), 5),
	RED_FIRELIGHTER(new Item(7329, 20, 60), 25),
	GREEN_FIRELIGHTER(new Item(7330, 20, 60), 25),
	BLUE_FIRELIGHTER(new Item(7331, 20, 60), 25),
	PURPLE_FIRELIGHTER(new Item(10326, 20, 60), 25),
	WHITE_FIRELIGHTER(new Item(10327, 20, 60), 25),
	SARADOMIN_ARROWS(new Item(19152, 20, 100), 40),
	GUTHIX_ARROWS(new Item(19157, 20, 100), 40),
	ZAMORAK_ARROWS(new Item(19162, 20, 100), 40),
	MEERKAT_POUCH(new Item(19623, 3, 15), 70),
	FETCH_CASKET_SCROLL(new Item(19621, 50, 80), 70);
	
	private final Item item;
	private final double rarity;
	
	public static final TTAllRewards[] VALUES = values();
	
	public static double ALL_TOTAL, EASY_TOTAL, MEDIUM_TOTAL, HARD_TOTAL, ELITE_TOTAL;
	
	public static void init() {
		for (TTAllRewards rewards : TTAllRewards.VALUES) 
			ALL_TOTAL += rewards.getRarity();
		for (TTEasyRewards rewards : TTEasyRewards.VALUES)
			EASY_TOTAL += rewards.getRarity();
		for (TTMediumRewards rewards : TTMediumRewards.VALUES)
			MEDIUM_TOTAL += rewards.getRarity();
		for (TTHardRewards rewards : TTHardRewards.VALUES)
			HARD_TOTAL += rewards.getRarity();
		for (TTEliteRewards rewards : TTEliteRewards.VALUES)
			ELITE_TOTAL += rewards.getRarity();
	}
	
	TTAllRewards(Item item, double rarity) {
		this.item = item;
		this.rarity = rarity;
	}
	
	public Item getItem() {
		return item;
	}
	
	public double getRarity() {
		return rarity;
	}
	
}
