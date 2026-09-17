package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class FallenChampionMageCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(20743));
		World.sendProjectile(new NewProjectile(npc, target, 2730, 55, 30, 35, 5, 100, 0));
		delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 98, NPCCombatDefinitionConstants.MAGE, target)));
		return 5;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22484 };
	}

}
