package com.rs.game.activities.aod.reward;

import static com.rs.game.activities.aod.AngelOfDeath.MAINDROP;
import static com.rs.game.activities.aod.AngelOfDeath.SIDEDROP;

/**
 * An enum containing all possible side and main drops for AoD: Nex.
 * @author Kris | 30. sept 2017 : 15:25.24
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public enum AoDRewards {

	CRYSTAL_KEY(MAINDROP, 990, 80, 14, 16),
	CRYSTAL_TRISKELION(MAINDROP, 28550, 80, 1, 1),
	INCANDESCENT_ENERGY(MAINDROP, 29324, 80, 2934, 3204),
	LUMINOUS_ENERGY(MAINDROP, 29323, 80, 3152, 3152),
	SIRENIC_SCALE(MAINDROP, 29863, 80, 1, 2),
	PRAESUL_CODEX(MAINDROP, 39584, 5, 1, 1),
	IMPERIUM_CORE(MAINDROP, 39579, 2, 1, 1),
	WAND_OF_THE_PRAESUL(MAINDROP, 39574, 2, 1, 1),
	INTRICATE_BLOOD_STAINED_CHEST(MAINDROP, 39586, 1, 1, 1),
	INTRICATE_ICE_CHEST(MAINDROP, 39588, 1, 1, 1),
	INTRICATE_SHADOW_CHEST(MAINDROP, 39590, 1, 1, 1),
	INTRICATE_SMOKE_SHROUDED_CHEST(MAINDROP, 39592, 1, 1, 1),
	ASCENDRI_BOLTS_E(MAINDROP, 31868, 50, 60, 77),
	ONYX_BOLT_TIPS(MAINDROP, 9194, 50, 73, 89),
	RUNE_PLATESKIRT(MAINDROP, 1094, 80, 13, 17),
	RUNE_BAR(MAINDROP, 2364, 80, 41, 50),
	UNCUT_DRAGONSTONE(MAINDROP, 1632, 50, 40, 50),
	UNCUT_ONYX(MAINDROP, 6571, 50, 1, 1),
	GRIMY_CADANTINE(MAINDROP, 216, 80, 83, 119),
	GRIMY_SNAPDRAGON(MAINDROP, 3052, 80, 105, 150),
	MAGIC_LOGS(MAINDROP, 1514, 80, 766, 1249),
	DRAGON_LONGSWORD(MAINDROP, 1306, 80, 10, 15),
	RUNE_2H_CROSSBOW(MAINDROP, 25930, 50, 20, 69),
	RUNE_WARHAMMER(MAINDROP, 1348, 50, 25, 34),
	CAVEFISH(SIDEDROP, 15267, 80, 35, 60),
	EARTH_ORB(SIDEDROP, 576, 80, 25, 40),
	LUMINOUS_ENERGY_SIDE(SIDEDROP, 29323, 80, 500, 800),
	ROCKTAIL(SIDEDROP, 15273, 80, 23, 55),
	BLACK_DRAGONHIDE(SIDEDROP, 1748, 50, 18, 37),
	MAHOGANY_PLANK(SIDEDROP, 8783, 50, 43, 43),
	SIRENIC_SCALE_SIDE(SIDEDROP, 29863, 50, 1, 1),
	WINE_OF_ZAMORAK(SIDEDROP, 246, 50, 3, 20),
	RUNE_BAR_SIDE(SIDEDROP, 2364, 50, 9, 16),
	SARADOMIN_BREW_3(SIDEDROP, 6688, 80, 6, 16),
	SUPER_RESTORE_3(SIDEDROP, 3027, 80, 14, 23),
	YEW_SEED(SIDEDROP, 5315, 50, 3, 5),
	EARTH_TALISMAN(SIDEDROP, 1441, 80, 15, 37),
	RUNE_CLAW(SIDEDROP, 3121, 50, 6, 11),
	PRAESUL_CODEX_SIDE(SIDEDROP, 39584, 5, 1, 1),
	INTRICATE_BLOOD_STAINED_CHEST_SIDE(SIDEDROP, 39586, 1, 1, 1),
	INTRICATE_ICE_CHEST_SIDE(SIDEDROP, 39588, 1, 1, 1),
	INTRICATE_SHADOW_CHEST_SIDE(SIDEDROP, 39590, 1, 1, 1),
	INTRICATE_SMOKE_SHROUDED_CHEST_SIDE(SIDEDROP, 39592, 1, 1, 1),
	IMPERIUM_CORE_SIDE(SIDEDROP, 39579, 1, 1, 1),
	WAND_OF_THE_PRAESUL_SIDE(SIDEDROP, 39574, 1, 1, 1),
	REEVES(MAINDROP, 39624, 1, 1, 1),
	;

	public static AoDRewards[] VALUES = values();
	
	private final int type, id, minAmount, maxAmount;
	private final double rate;
	
	AoDRewards(final int type, final int id, final double rate, final int minAmount, final int maxAmount) {
		this.type = type;
		this.id = id;
		this.rate = rate;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
	
	public final int getType() {
		return type;
	}
	
	public final int getId() {
		return id;
	}
	
	public final double getRate() {
		return rate;
	}
	
	public final int getMinimumAmount() {
		return minAmount;
	}
	
	public final int getMaximumAmount() {
		return maxAmount;
	}
}
