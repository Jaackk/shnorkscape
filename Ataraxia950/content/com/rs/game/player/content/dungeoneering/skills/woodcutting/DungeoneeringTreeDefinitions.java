package com.rs.game.player.content.dungeoneering.skills.woodcutting;

public enum DungeoneeringTreeDefinitions {

	TANGLE_GUM_VINE(1, 35, 17682, 20, 4, 49706, 8, 5),
	SEEPING_ELM_TREE(10, 60, 17684, 25, 4, 49708, 12, 5),
	BLOOD_SPINDLE_TREE(20, 85, 17686, 35, 4, 49710, 16, 5),
	UTUKU_TREE(30, 115, 17688, 60, 4, 49712, 51, 5),
	SPINEBEAM_TREE(40, 145, 17690, 76, 16, 49714, 68, 5),
	BOVISTRANGLER_TREE(50, 175, 17692, 85, 16, 49716, 75, 5),
	THIGAT_TREE(60, 210, 17694, 95, 16, 49718, 83, 10),
	CORPESTHORN_TREE(70, 245, 17696, 111, 16, 49720, 90, 10),
	ENTGALLOW_TREE(80, 285, 17698, 120, 17, 49722, 94, 10),
	GRAVE_CREEPER_TREE(90, 330, 17700, 150, 21, 49724, 121, 10);

	private final int level;
	private final double xp;
	private final int logsId;
	private final int logBaseTime;
	private final int logRandomTime;
	private final int stumpId;
	private final int respawnDelay;
	private final int randomLifeProbability;

	DungeoneeringTreeDefinitions(int level, double xp, int logsId, int logBaseTime, int logRandomTime, int stumpId, int respawnDelay, int randomLifeProbability) {
		this.level = level;
		this.xp = xp;
		this.logsId = logsId;
		this.logBaseTime = logBaseTime;
		this.logRandomTime = logRandomTime;
		this.stumpId = stumpId;
		this.respawnDelay = respawnDelay;
		this.randomLifeProbability = randomLifeProbability;
	}

	public int getLevel() {
		return level;
	}

	public int getLogBaseTime() {
		return logBaseTime;
	}

	public int getLogRandomTime() {
		return logRandomTime;
	}

	public int getLogsId() {
		return logsId;
	}

	public int getRandomLifeProbability() {
		return randomLifeProbability;
	}

	public int getRespawnDelay() {
		return respawnDelay;
	}

	public int getStumpId() {
		return stumpId;
	}

	public double getXp() {
		return xp;
	}
}
