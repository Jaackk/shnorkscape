package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class AutomatonRangerCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(18237));
		npc.setNextGraphics(new Graphics(4958, 0, 145));
		World.sendProjectile(new NewProjectile(npc, target, 249, 50, 30, 40, 5, 100, 0));
		delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 98, NPCCombatDefinitionConstants.RANGE, target)));
		return 5;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22475 };
	}

}
