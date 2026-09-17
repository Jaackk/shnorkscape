package com.rs.game.player.content.dungeoneering.skills.farming;

import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.utils.Utils;

public class DungeoneeringPatch {

	private final WorldObject patch;
	private final Player owner;
	private final DungeoneeringFarmingData data;
	private int cropsAmount;
	
	public DungeoneeringPatch(WorldObject patch, Player owner, DungeoneeringFarmingData data) {
		this.patch = patch;
		this.owner = owner;
		this.data = data;
		cropsAmount = Utils.random(owner.getRingOfKinship().getBoost(RingOfKinship.GATHERER) > 0 ? Utils.random(7, 10) : Utils.random(5, 8));
	}
	
	public final WorldObject getPatch() {
		return patch;
	}
	
	public final Player getOwner() {
		return owner;
	}
	
	public final DungeoneeringFarmingData getData() {
		return data;
	}
	
	public int getCropsAmount() {
		return cropsAmount;
	}
	
	public void decrementCrops() {
		cropsAmount--;
	}
}
