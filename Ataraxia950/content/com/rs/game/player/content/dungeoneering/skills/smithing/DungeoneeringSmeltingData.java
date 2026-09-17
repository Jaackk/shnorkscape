package com.rs.game.player.content.dungeoneering.skills.smithing;

public enum DungeoneeringSmeltingData {

	NOVITE_BAR(17650, 17630, 1, 7),
	BATHUS_BAR(17652, 17632, 10, 13.3),
	MARMAROS_BAR(17654, 17634, 20, 19.6),
	KRATONITE_BAR(17656, 17636, 30, 25.9),
	FRACTITE_BAR(17658, 17638, 40, 32.2),
	ZEPHYRIUM_BAR(17660, 17640, 50, 38.5),
	ARGONITE_BAR(17662, 17642, 60, 44.8),
	KATAGON_BAR(17664, 17644, 70, 51.1),
	GORGONITE_BAR(17666, 17646, 80, 57.4),
	PROMETHIUM_BAR(17668, 17648, 90, 63.7);
	
	private final int barId;
    private final int oreId;
    private final int level;
	private final double xp;
	
	DungeoneeringSmeltingData(int barId, int oreId, int level, double xp) {
		this.barId = barId;
		this.oreId = oreId;
		this.level = level;
		this.xp = xp;
	}
	
	public int getBarId() {
		return barId;
	}
	
	public int getOreId() {
		return oreId;
	}
	
	public int getLevel() {
		return level;
	}
	
	public double getExperience() {
		return xp;
	}
}
