package com.rs.game.player.actions.fletching.defs;

/**
 * BoltTips.java | 11:45:11 AM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum BoltTips {

	OPAL(1609, 45, 15.0, 11, 12, 886),

	JADE(1611, 9187, 20, 26, 12, 886),

	PEARL(411, 46, 20, 41, 6, 886),

	PEARLS(413, 46, 20, 41, 24, 886),

	RED_TOPAZ(1613, 9188, 25, 48, 12, 887),

	SAPPHIRE(1607, 9189, 50, 56, 12, 888),

	EMERALD(1605, 9190, 67, 58, 12, 889),

	RUBY(1603, 9191, 85, 63, 12, 887),

	DIAMOND(1601, 9192, 107.5, 65, 12, 890),

	DRAGONSTONE(1615, 9193, 137.5, 71, 12, 885),

	ONYX(6573, 9194, 167.5, 73, 24, 2717),

	HYDRIX(31855, 31867, 10.6, 80, 36, 2717);

	private final double experience;
	private final int levelRequired;
    private final int gemId;
    private final int tipId;
    private final int amount;
    private final int emote;

	BoltTips(int gemId, int tipId, double experience, int levelRequired, int amount, int emote) {
		this.gemId = gemId;
		this.tipId = tipId;
		this.experience = experience;
		this.levelRequired = levelRequired;
		this.amount = amount;
		this.emote = emote;
	}

	public int getLevelRequired() {
		return levelRequired;
	}

	public double getExperience() {
		return experience;
	}

	public int getGemId() {
		return gemId;
	}

	public int gettipId() {
		return tipId;
	}

	public int getAmount() {
		return amount;
	}

	public int getEmote() {
		return emote;
	}

}
