package com.rs.game.player.content.crystaltriskellion;

public enum TriskelionNPCs {

	COMMON_NPCS(CrystalTriskelion.COMMON, 17182),
	UNCOMMON_NPCS(CrystalTriskelion.UNCOMMON, 19463, 9176, 9177, 9178, 9179, 9180),
//	RARE_NPCS(CrystalTriskelion.RARE),
	VERY_RARE_NPCS(CrystalTriskelion.VERY_RARE, 20290, 19109, 14696, 9463, 18621, 18622, 17149, 17150, 17151, 17152, 17153, 17154, 2027, 2026, 2025, 2029, 2028, 2783, 13820, 13821, 6221, 1615, 3334);
	
	private final int rarity;
	private final int[] npcs;

	TriskelionNPCs(int rarity, int... npcs) {
		this.rarity = rarity;
		this.npcs = npcs;
	}
	
	public int getRarity() {
		return rarity;
	}
	
	public int[] getNPCs() {
		return npcs;
	}
}
