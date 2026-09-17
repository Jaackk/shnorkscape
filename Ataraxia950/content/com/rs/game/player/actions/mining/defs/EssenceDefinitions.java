package com.rs.game.player.actions.mining.defs;

/**
 * EssenceDefinitions.java | 11:48:02 AM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum EssenceDefinitions {

	Rune_Essence(1, 6, 1437, 1, 1), Pure_Essence(30, 12, 7937, 1, 1);

	private final int level;
	private final double xp;
	private final int oreId;
	private final int oreBaseTime;
	private final int oreRandomTime;

	EssenceDefinitions(int level, double xp, int oreId, int oreBaseTime, int oreRandomTime) {
		this.level = level;
		this.xp = xp;
		this.oreId = oreId;
		this.oreBaseTime = oreBaseTime;
		this.oreRandomTime = oreRandomTime;
	}

	public int getLevel() {
		return level;
	}

	public int getOreBaseTime() {
		return oreBaseTime;
	}

	public int getOreId() {
		return oreId;
	}

	public int getOreRandomTime() {
		return oreRandomTime;
	}

	public double getXp() {
		return xp;
	}
}
