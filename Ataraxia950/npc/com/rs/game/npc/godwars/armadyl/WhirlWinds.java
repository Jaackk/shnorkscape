package com.rs.game.npc.godwars.armadyl;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

public class WhirlWinds extends NPC {

	private static final long serialVersionUID = 4586158467400385922L;

	public WhirlWinds(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, NPC boss) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.boss = boss;
		this.setForceTargetDistance(30);
	}
	
	private final NPC boss;
	
	@Override
	public void processEntity() {
		super.processEntity();
		boss.getPossibleTargets().forEach(t -> {
			if (t.getHash() == this.getHash())
				t.applyHit(new Hit(boss, Utils.random(200), HitLook.REGULAR_DAMAGE));
		});
	}

}
