package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

public class ShadowDemonCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		if (npc.withinDistance(target, npc.getSize()) && Utils.random(3) == 0) {
			npc.setNextAnimation(new Animation(19589));
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 130, NPCCombatDefinitionConstants.MELEE, target)));
		} else {
			npc.setNextAnimation(new Animation(16990));
			if (Utils.random(2) == 0) 
				delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 130, NPCCombatDefinitionConstants.MAGE, target)));
			else
				delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 130, NPCCombatDefinitionConstants.RANGE, target)));
		}
		return 3;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22485 };
	}

}
