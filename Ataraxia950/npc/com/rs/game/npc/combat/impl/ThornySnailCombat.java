package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.familiar.Familiar;

public class ThornySnailCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		if (usingSpecial) {// priority over regular attack
			npc.setNextAnimation(new Animation(8148));
			npc.setNextGraphics(new Graphics(1385));
			World.sendProjectile(npc, target, 1386, 34, 16, 30, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 80, NPCCombatDefinitionConstants.RANGE, target)));
			npc.setNextGraphics(new Graphics(1387));
		} else {
			npc.setNextAnimation(new Animation(8143));
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 40, NPCCombatDefinitionConstants.RANGE, target)));
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 6807, 6806 };
	}

}
