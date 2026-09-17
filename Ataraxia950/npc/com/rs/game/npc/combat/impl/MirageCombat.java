package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.camelwarrior.Mirage;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

public class MirageCombat extends CombatScript {

	private int damage;
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(4);
		Mirage mirage = (Mirage) npc;
		
		for(Entity t : npc.getPossibleTargets(false, true)) {
			if(mirage.getCamel().getTargetIndex() != -1 && t.getIndex() == mirage.getCamel().getTargetIndex()) {
				target = t;
				break;
			}
		}
		
		switch(attackStyle) {
			case 0: // Blood
				npc.setNextAnimation(new Animation(27811));
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 2, target, getMagicHit(npc, damage));
				World.sendProjectile(npc, target, 374, 18, 18, 50, 50, 0, 0);
				npc.applyHit(new Hit(npc, damage / 5, HitLook.HEALED_DAMAGE));
				break;
				
			case 1: // Shadow
				npc.setNextAnimation(new Animation(27811));
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 2, target, getMagicHit(npc, damage));
				World.sendProjectile(npc, target, 378, 18, 18, 50, 50, 0, 0);
				
				break;
				
			case 2: // Ice
				npc.setNextAnimation(new Animation(27811));
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 2, target, getMagicHit(npc, damage));
				World.sendProjectile(npc, target, 362, 18, 18, 50, 50, 0, 0);

				break;
				
			case 3: //Smoke
				npc.setNextAnimation(new Animation(27811));
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 2, target, getMagicHit(npc, damage));
				World.sendProjectile(npc, target, 384, 18, 18, 50, 50, 0, 0);
				break;
		}
		
		return 6;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] {22002, 22003, 22004};
	}

}
