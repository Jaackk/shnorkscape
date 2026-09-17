package com.rs.game.npc.gwd2.twinfuries;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;

public abstract class TwinSpecialAttack {

	public TwinSpecialAttack(NPC npc, Entity entity) {
		this.npc = npc;
		this.entity = entity;
	}
	
	protected NPC npc;
	protected Entity entity;
	
	public abstract void effect();
	
	public NPC getNPC() {
		return npc;
	}
	
	public Entity getentity() {
		return entity;
	}
	
}
