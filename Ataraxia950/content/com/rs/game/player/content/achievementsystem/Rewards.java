package com.rs.game.player.content.achievementsystem;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Oct 25, 2018.
 */
public enum Rewards {

	COINS_1M(995, 10000),
	//
	EASY_CLUE_SCROLL(2678, 1),
	//
	BLOOD_RUNES(565, 1000),
	//
	ABYSSAL_WHIP(4151, 1),
	//
	VOID_GLOVES(8842, 1),
	//
	MYSTERY_BOX(6199, 1),
	//
	SILVERHAWK_FEATHERS(30915, 50),
	//
	COINS_5M(995, 50000),
	//
	MYSTERY_BOX_2(6199, 2),
	//
	MEDIUM_CLUE_SCROLL(2803, 1),
	//
	HUGE_EXP_LAMP(23716, 1),
	//
	LARGE_FARMING_LAMP(23812, 1),
	//
	SILVERHAWK_FEATHERS_100(30915, 100),
	//
	VETERAN_CAPE_5(20763, 1),
	//
	DHAROK_SET(11848, 1),
	//
	KARIL_SET(11852, 1),
	//
	AHRIM_SET(11846, 1),
	//
	HARD_CLUE_SCROLL(2723, 1),
	//
	COINS_10M(995, 100000),
	//
	SILVERHAWK_FEATHERS_150(30915, 150),
	//
	VETERAN_CAPE_10(24709, 1),
	//
	ASCENDRI_BOLTS_500(31868, 500),
	//
	ROCKTAIL_SOUP_200(26314, 200),
	//
	COINS_20M(995, 200000),
	//
	MYSTERY_BOX_3(6199, 3),
	//
	ELITE_CLUE_SCROLL(19044, 1),
	//
	HUGE_FARMING_LAMP(23764, 1),
	//
	CORRUPTION_SIGIL(36156, 1),
	//
	COINS_15M(995, 150000),
	//
	MIMIC_PLUSH(38993, 1),
	//
	HYDRIX(31855, 1),
	//
	BABY_TROLL(23030, 1),
	//
	ROWENA(32512, 1),
	//
	SILVERHAWK_BOOTS(30920, 1),
	//
	VETERAN_CAPE_15(39644, 1),
	//
	COINS_30M(995, 300000),
	//
	MYSTERY_BOX_5(6199, 5),
	//
	RARE_ITEM_TOKENS_2(34027, 2),
	//
	RARE_ITEM_TOKENS_3(34027, 3),
	//gwd specialist
	GWD_SPECIALIST(41343, 1),
	//
	ANCIENT_EMBLEM(36159, 1),
	//
	PERFECT_CHITIN(36163, 1);

	private final int itemId;
	private final int amount;

	Rewards(int itemId, int amount) {
		this.itemId = itemId;
		this.amount = amount;
	}

	public int getItemId() {
		return itemId;
	}

	public int getAmount() {
		return amount;
	}

}
