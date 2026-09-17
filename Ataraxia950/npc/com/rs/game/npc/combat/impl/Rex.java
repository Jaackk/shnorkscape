package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class Rex extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		npc.setNextAnimation(new Animation(npc.getCombatDefinitions().getAttackEmote()));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 400, NPCCombatDefinitionConstants.MELEE, target)));
		return 4;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 2883 };
	}
}