package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

public class SerenWarriorCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(Utils.random(2) == 1 ? 18235 : 18226));
		delayHit(npc, 2, target, getMeleeHit(npc, getRandomMaxHit(npc, 98, NPCCombatDefinitionConstants.MELEE, target)));
		return 5;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22471 };
	}

}
