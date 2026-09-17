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
import com.rs.utils.Utils;

public class SteelTitanCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 7344, 7343 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		boolean distant = false;
		int size = npc.getSize();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		int damage = 0;
		if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
			distant = true;
		if (usingSpecial) {// priority over regular attack
			npc.setNextAnimation(new Animation(8190));
			target.setNextGraphics(new Graphics(1449));
			if (distant) {// range hit
				delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.RANGE, target)), getRangeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.RANGE, target)));
			} else {// melee hit
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.MELEE, target)), getMeleeHit(npc, getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.MELEE, target)));
			}
		} else {
			if (distant) {
				int attackStage = Utils.getRandom(1);// 2
				switch (attackStage) {
				case 0:// magic
					damage = getRandomMaxHit(npc, 255, NPCCombatDefinitionConstants.MAGE, target);
					npc.setNextAnimation(new Animation(7694));
					World.sendProjectile(npc, target, 1451, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getMagicHit(npc, damage));
					break;
				case 1:// range
					damage = getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.RANGE, target);
					npc.setNextAnimation(new Animation(8190));
					World.sendProjectile(npc, target, 1445, 34, 16, 30, 35, 16, 0);
					delayHit(npc, 2, target, getRangeHit(npc, damage));
					break;
				}
			} else {// melee
				damage = getRandomMaxHit(npc, 244, NPCCombatDefinitionConstants.MELEE, target);
				npc.setNextAnimation(new Animation(8183));
				delayHit(npc, 1, target, getMeleeHit(npc, damage));
			}
		}
		return defs.getAttackDelay();
	}
}
