package com.rs.game.player.actions.slayer;

public final class TaskSet {

	private final SlayerMasterData master;
	private final int weight, minAmount, maxAmount;
	
	public TaskSet(SlayerMasterData master, int weight, int minAmount, int maxAmount) {
		this.master = master;
		this.weight = weight;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
	
	public final SlayerMasterData getSlayerMaster() {
		return master;
	}
	
	public final int getWeight() {
		return weight;
	}
	
	public final int getMinimumAmount() {
		return minAmount;
	}
	
	public final int getMaximumAmount() {
		return maxAmount;
	}
}
