package com.rs.game.npc.combat.impl.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.gwd2.gregorovic.Shadow;
import com.rs.utils.Utils;

public class ShadowCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 22444 };
	}

	public void shurikenAttack(NPC npc, Entity target) {
		Shadow shadow = (Shadow) npc;
		shadow.setNextAnimation(new Animation(28228));
		World.sendProjectile(npc, npc, target, 6133, 38, 30, 35, 25, 16, 0);
		delayHit(npc, 2, target, getRangeHit(npc, Utils.random(50, 82)));
	}

	@Override
	public int attack(NPC npc, Entity target) {
		shurikenAttack(npc, target);
		return npc.getAttackSpeed();
	}

}