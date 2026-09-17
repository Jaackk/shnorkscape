package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class CulinaromancerCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		switch (1) {
		case 1: // Magic
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			npc.setNextGraphics(new Graphics(2728));
			World.sendProjectile(npc, target, 2735, 18, 18, 50, 50, 3, 0);
			World.sendProjectile(npc, target, 2736, 18, 18, 50, 50, 20, 0);
			World.sendProjectile(npc, target, 2736, 18, 18, 50, 50, 110, 0);
			delayHit(npc, 2, target, getMagicHit(npc, damage));
			break;
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Culinaromancer" };
	}
}