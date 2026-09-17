package com.rs.game.player.content.dungeoneering.skills.herblore;

public enum DungeoneeringHerbs {

	SAGEWORT(17494, 2.1, 3, 17512),
	VALERIAN(17496, 3.2, 4, 17514),
	ALOE(17498, 4, 8, 17516),
	WORMWOOD_LEAF(17500, 7.2, 34, 17518),
	MAGEBANE(17502, 7.7, 37, 17520),
	FEATHERFOIL(17504, 8.6, 41, 17522),
	WINTERS_GRIP(17506, 12.7, 67, 17524),
	LYCOPUS(17508, 13.1, 70, 17526),
	BUCKTHORN(17510, 13.8, 74, 17528);

	private final int herbId;
	private final int level;
	private final int cleanId;
	private final double xp;

	DungeoneeringHerbs(int herbId, double xp, int level, int cleanId) {
		this.herbId = herbId;
		this.xp = xp;
		this.level = level;
		this.cleanId = cleanId;
	}

	public int getCleanId() {
		return cleanId;
	}

	public double getExperience() {
		return xp;
	}

	public int getHerbId() {
		return herbId;
	}

	public int getLevel() {
		return level;
	}
}
