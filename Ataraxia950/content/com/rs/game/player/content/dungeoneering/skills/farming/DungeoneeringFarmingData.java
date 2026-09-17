package com.rs.game.player.content.dungeoneering.skills.farming;

public enum DungeoneeringFarmingData {
	
	POTATO(17823, 1, 1.3, 17817, 2),
	GISSEL(17824, 34, 3.5, 17819, 1),
	EDICAP(17825, 68, 5.7, 17821, 1),
	SAGEWORT(17826, 7, 15, 17494, 3),
	VALERIAN(17827, 18, 18.1, 17496, 3),
	ALOE(17828, 29, 21.9, 17498, 3),
	WORMWOOD(17829, 40, 27.1, 17500, 3),
	MAGEBANE(17830, 51, 34.4, 17502, 3),
	FEATHERFOIL(17831, 62, 44.5, 17504, 3),
	WINTERS_GRIP(17832, 73, 58.1, 17506, 3),
	LYCOPUS(17833, 84, 75.9, 17508, 3),
	BUCKTHORN(17834, 95, 98.6, 17510, 3);

	private final int seed, lvl, product, time;
	private final double exp;

	DungeoneeringFarmingData(int seed, int lvl, double exp, int product, int time) {
		this.seed = seed;
		this.lvl = lvl;
		this.exp = exp;
		this.product = product;
		this.time = time;
	}

	public int getSeed() {
		return seed;
	}

	public int getLevel() {
		return lvl;
	}

	public double getExperience() {
		return exp;
	}

	public int getProduct() {
		return product;
	}
	
	public int getTime() {
		return time;
	}
}
