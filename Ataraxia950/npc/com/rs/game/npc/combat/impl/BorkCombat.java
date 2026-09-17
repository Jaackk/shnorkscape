package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.Bork;

/**
 * Handles Bork's combat script.
 *
 * @author Noel
 */
public class BorkCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition cdef = npc.getCombatDefinitions();
		Bork bork = (Bork) npc;
		if (bork.getHitpoints() <= (cdef.getHitpoints() * 0.6) && !bork.isSpawnedMinions()) {
			bork.spawnMinions();
			return 0;
		}
		bork.setNextAnimation(new Animation(cdef.getAttackEmote()));
		delayHit(bork, 0, target,
				getMeleeHit(bork, getRandomMaxHit(bork, bork.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
		return cdef.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Bork" };
	}
}