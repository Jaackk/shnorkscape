package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class PumpkinPete extends NPC {

	private final Player target;

	public PumpkinPete(Player target, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, -1, false, true);
		this.addFreezeDelay(1000);
		this.target = target;
	}

	@Override
	public void processNPC() {
		faceEntity(target);
		if (this.getFreezeDelay() < 100)
			this.addFreezeDelay(900);
	}

	public Player getTarget() {
		return target;
	}
}
