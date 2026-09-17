package com.rs.game.player.content.dungeoneering.skills.runecrafting;

public enum DungeoneeringRunecraftingData {

	AIR_RUNE(17780, 1, 0.1, 11),
	WATER_RUNE(17781, 5, 0.12, 19),
	EARTH_RUNE(17782, 9, 0.13, 26),
	FIRE_RUNE(17783, 14, 0.14, 35),
	MIND_RUNE(17784, 2, 0.11, 14),
	CHAOS_RUNE(17785, 35, 0.17, 74),
	DEATH_RUNE(17786, 65, 0.2, -1),
	BLOOD_RUNE(17787, 77, 0.21, -1),
	BODY_RUNE(17788, 20, 0.15, 46),
	COSMIC_RUNE(17789, 27, 0.16, 59),
	LAW_RUNE(17792, 54, 0.19, -1),
	NATURE_RUNE(17791, 44, 0.18, 91),
	ASTRAL_RUNE(17790, 40, 0.174, 82),
	CATALYTIC_WAND(27707, 50, 39.5, 27695),
	CATALYTIC_ORB(27957, 50, 39.5, 27945),
	CATALYTIC_STAFF(16967, 50, 39.5, 16985),
	EMPOWERED_CATALYTIC_WAND(27709, 99, 106, 27705),
	EMPOWERED_CATALYTIC_ORB(27959, 99, 106, 27955),
	EMPOWERED_CATALYTIC_STAFF(17015, 99, 106, 16995);
	
	private final int runeId;
    private final int requiredLevel;
    private final int doubleRunes;
	private final double experience;
	
	DungeoneeringRunecraftingData(int runeId, int requiredLevel, double experience, int doubleRunes) {
		this.runeId = runeId;
		this.requiredLevel = requiredLevel;
		this.experience = experience;
		this.doubleRunes = doubleRunes;
	}
	
	public int getRuneId() {
		return runeId;
	}
	
	public int getRequiredLevel() {
		return requiredLevel;
	}
	
	public double getExperience() {
		return experience;
	}
	
	/**
	 * In runes, it's used to get the level at which double runes start, in staves, it's used to determine the id of the item necessary.
	 * @return
	 */
	public int getDoubleRunesLevel() {
		return doubleRunes;
	}
	
	public static final int[] getSpecificRunes(int type) {
		int[] runes = new int[type == 2 ? 5 : type == 3 ? 6 : 4];
		int i = 0;
		for (DungeoneeringRunecraftingData data : DungeoneeringRunecraftingData.values()) {
			if (type == 0 && data.ordinal() < 4) {
				runes[i] = data.getRuneId();
				i++;
			} else if (type == 1 && data.ordinal() > 3 && data.ordinal() < 8) {
				runes[i] = data.getRuneId();
				i++;
			} else if (type == 2 && data.ordinal() > 7 && data.ordinal() < 13) {
				runes[i] = data.getRuneId();
				i++;
			} else if (type == 3 && data.ordinal() > 12) {
				runes[i] = data.getRuneId();
				i++;
			}
		}
		return runes;
	}
}
