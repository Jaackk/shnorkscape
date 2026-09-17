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
import com.rs.utils.Utils;

public class MossTitanCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		int damage = 0;
		if (usingSpecial) {// priority over regular attack
			npc.setNextAnimation(new Animation(8223));
			npc.setNextGraphics(new Graphics(1460));
			for (Entity targets : npc.getPossibleTargets()) {
				if (targets.equals(target) && !targets.isAtMultiArea())
					continue;
				sendSpecialAttack(targets, npc);
			}
			sendSpecialAttack(target, npc);
		} else {
			damage = getRandomMaxHit(npc, 160, NPCCombatDefinitionConstants.MELEE, target);
			npc.setNextAnimation(new Animation(8222));
			delayHit(npc, 1, target, getMeleeHit(npc, damage));
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 7330, 7329 };
	}

	public void sendSpecialAttack(Entity target, NPC npc) {
		if (target.isAtMultiArea() && Wilderness.isAtWild(target)) {
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 160, NPCCombatDefinitionConstants.MAGE, target)));
			World.sendProjectile(npc, target, 1462, 34, 16, 30, 35, 16, 0);
			if (Utils.getRandom(3) == 0)// 1/3 chance of being poisioned
				target.getPoison().makePoisoned(58);
		}
	}
}
