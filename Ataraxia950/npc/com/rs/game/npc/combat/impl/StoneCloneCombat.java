package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.vorago.StoneClone;

public class StoneCloneCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 17158, 17159, 17160 };// melee,range,mage
	}

	private StoneClone clone;

	@Override
	public int attack(NPC npc, Entity target) {
		clone = (StoneClone) npc;
		int combatType = clone.getId() == 17160 ? NPCCombatDefinitionConstants.MAGE
				: clone.getId() == 17159 ? NPCCombatDefinitionConstants.RANGE : NPCCombatDefinitionConstants.MELEE;
		int damage = getRandomMaxHit(npc, 350, NPCCombatDefinitionConstants.MELEE, target);
		delayHit(npc, 1, target, combatType == NPCCombatDefinitionConstants.MELEE ? getMeleeHit(npc, damage)
				: combatType == NPCCombatDefinitionConstants.RANGE ? getRangeHit(npc, damage) : getMagicHit(npc, damage));
		return 3;
	}

}
