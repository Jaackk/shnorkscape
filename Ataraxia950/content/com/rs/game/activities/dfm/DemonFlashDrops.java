package com.rs.game.activities.dfm;

public enum DemonFlashDrops {

	INFERNAL_ASHES(20268, 5, 100, 1),
	COINS(995, 45000, 40, 20000),
	BLACK_DRAGONHIDE(1748, 10, 40),
	GREEN_DRAGONHIDE(1754, 30, 40),
	INFERNAL_ASHES_NOTED(20269, 0, 40, 30, 40, 60),
	RED_DRAGONHIDE(1750, 15, 40),
	RUNE_PLATEBODY(1127, 0, 40, 1),
	RUNE_PLATELEGS(1079, 0, 40, 1),
	DRAGON_HELM(1149, 1, 10),
	ADAMANT_BAR(2362, 25, 40),
	MITHRIL_BAR(2360, 0, 40, 20),
	RUNE_BAR(2364, 4, 40),
	UNCUT_RUBY(1620, 0, 40, 10, 20),
	GRIMY_AVANTOE(212, 20, 40, 8),
	GRIMY_IRIT(210, 30, 40, 14),
	GRIMY_LANTADYME(2486, 10, 40, 5),
	GRIMY_TORSTOL(220, 0, 40, 3),
	MAGIC_LOGS(1514, 40, 40),
	MAPLE_LOGS(1518, 0, 40, 200),
	YEW_LOGS(1516, 0, 40, 60),
	COAL(454, 0, 70, 80),
	MITHRIL_ORE(448, 0, 40, 40),
	RUNITE_ORE(452, 5, 40),
	LANTADYME_SEED(5302, 3, 40, 2),
	TORSTOL_SEED(5304, 0, 10, 1),
	DEMON_CLAW(33935, 0, 40, 6, 7, 8, 9),
	DRAGON_LONGSWORD(1305, 0, 40, 1),
	RUNE_2H_SWORD(1319, 1, 40);
	
	private final int itemId;
    private final int wildernessAmount;
    private final int rarity;
	private final int[] amounts;
	
	DemonFlashDrops(int itemId, int wildernessAmount, int rarity, int... amounts) {
		this.itemId = itemId;
		this.wildernessAmount = wildernessAmount;
		this.rarity = rarity;
		this.amounts = amounts;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getWildernessAmount() {
		return wildernessAmount;
	}
	
	public int getRarity() {
		return rarity;
	}
	
	public boolean isDroppedInWildernessOnly() {
		return amounts.length == 0;
	}
	
	public int[] getAmount() {
		return amounts;
	}
}
