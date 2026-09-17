package com.rs.game.player.actions.slayer;

public enum SlayerMasterData {

	TURAEL(8461, 1, 1, 0),
	MAZCHNA(8464, 1, 20, 2),
	VANNAKA(1597, 1, 40, 4),
	CHAELDAR(1598, 1, 75, 10),
	SUMONA(7780, 35, 90, 12),
	DURADEL(8466, 50, 100, 15),
	KURADAL(9085, 75, 110, 18),
	MORVRAN(20112, 85, 120, 20);
	
	private final int npcId, slayerReq, combatReq, pointsPerTask;
	
	SlayerMasterData(int npcId, int slayerReq, int combatReq, int pointsPerTask) {
		this.npcId = npcId;
		this.slayerReq = slayerReq;
		this.combatReq = combatReq;
		this.pointsPerTask = pointsPerTask;
	}
	
	public final int getNpcId() {
		return npcId;
	}
	
	public final int getSlayerRequirement() {
		return slayerReq;
	}
	
	public final int getCombatRequirement() {
		return combatReq;
	}
	
	public final int getPointsPerTask() {
		return pointsPerTask;
	}
	
	public final int get10thTaskPoints() {
		return pointsPerTask * 5;
	}
	
	public final int get50thTaskPoints() {
		return get10thTaskPoints() * 5;
	}
}
