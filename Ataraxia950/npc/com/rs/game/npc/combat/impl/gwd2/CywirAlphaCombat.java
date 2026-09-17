package com.rs.game.npc.combat.impl.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

/**
 * CywirAlphaCombat.java | 10:22:54 PM
 * @author Chryonic
 * @date Mar 18, 2017
 */
public class CywirAlphaCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 22439, 22441 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(23578));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
		return npc.getAttackSpeed();
	}
	
}