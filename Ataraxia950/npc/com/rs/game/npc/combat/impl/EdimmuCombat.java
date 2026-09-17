package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

public class EdimmuCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Edimmu" };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		// To avoid making a new NPC class
		npc.setForceFollowClose(true);
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		final int random = Utils.random(2);
		switch (random) {
		case 0:
			sendMageAttack(npc, target);
			break;
		default:
			sendMeleeAttack(npc, target);
			break;
		}
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		return defs.getAttackDelay();
	}

	/**
	 * Executes Edimmu's magic attack.
	 *
	 * @param npc
	 *            The Edimmu NPC.
	 * @param target
	 *            The target.
	 */
	private void sendMageAttack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Hit hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target));
		if (Utils.random(5) == 0)
			npc.heal(hit.getDamage() / 4);
		delayHit(npc, 2, target, hit);
	}

	/**
	 * Executes Edimmu's melee attack.
	 *
	 * @param npc
	 *            The Edimmu NPC.
	 * @param target
	 *            The target.
	 */
	private void sendMeleeAttack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Hit hit = getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target));
		if (!target.withinDistance(npc, 1))
			sendMageAttack(npc, target);
		else {
			if (Utils.random(5) == 0)
				npc.heal(hit.getDamage() / 4);
			delayHit(npc, 0, target, hit);
		}
	}
}