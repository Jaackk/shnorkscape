package com.rs.game.player.content.dungeoneering.skills.firemaking;

public enum DungeoneeringFiremakingData {

	TANGLE_GUM(17682, 1, 300, 49940, 25),
	SEEPING_ELM(17684, 10, 400, 49941, 44.5),
	BLOOD_SPINDLE(17686, 20, 500, 49942, 65.6),
	UTUKU(17688, 30, 600, 49943, 88.3),
	SPINEBEAM(17690, 40, 700, 49944, 112.6),
	BOVISTRANGLER(17692, 50, 800, 49945, 138.5),
	THIGAT(17694, 60, 900, 49946, 166),
	CORPSETHORN(17696, 70, 1000, 49947, 195.1),
	ENTGALLOW(17698, 80, 1100, 49948, 225.8),
	GRAVE_CREEPER(17700, 90, 1200, 49949, 258.1);
	
	private final int logId;
    private final int level;
    private final int time;
    private final int fireId;
	private final double xp;
	
	DungeoneeringFiremakingData(int logId, int level, int time, int fireId, double xp) {
		this.logId = logId;
		this.level = level;
		this.time = time;
		this.fireId = fireId;
		this.xp = xp;
	}
	
	public int getLogsId() {
		return logId;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getFireId() {
		return fireId;
	}
	
	public double getExperience() {
		return xp;
	}
}
