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
import com.rs.game.player.controllers.Wilderness;

public class FlashDemonFamiliarCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 20611, 20612, 20613, 20614, 20615, 20616 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		int damage = 0;
		if (usingSpecial) {// priority over regular attack
			npc.setNextAnimation(new Animation(25720));
			npc.setNextGraphics(new Graphics(3802));
			for (Entity entities : npc.getPossibleTargets()) {
				if (entities.withinDistance(npc, 7)) {
					if (Wilderness.isAtWild(npc))
						delayHit(npc, 1, entities, getMagicHit(npc, 105));
					else {
						if (entities instanceof NPC)
							delayHit(npc, 1, entities, getMagicHit(npc, 105));
					}
				}
			}
		} else {
			if (npc.getId() != 20611 && npc.getId() != 20612) {
				switch (npc.getId()) {
				case 20615:// magic
				case 20616:
					damage = getRandomMaxHit(npc, 200, NPCCombatDefinitionConstants.MAGE, target);
					npc.setNextAnimation(new Animation(defs.getAttackEmote()));
					World.sendProjectile(npc, target, 1451, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getMagicHit(npc, damage));
					break;
				case 20613:// range
				case 20614:
					damage = getRandomMaxHit(npc, 200, NPCCombatDefinitionConstants.RANGE, target);
					npc.setNextAnimation(new Animation(defs.getAttackEmote()));
					World.sendProjectile(npc, target, 1445, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getRangeHit(npc, damage));
					break;
				}
			} else {// melee
				damage = getRandomMaxHit(npc, 200, NPCCombatDefinitionConstants.MELEE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 1, target, getMeleeHit(npc, damage));
			}
		}
		return defs.getAttackDelay();
	}
}
