package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class BloodReaverCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(7004));
		World.sendProjectile(new NewProjectile(new WorldTile(npc, npc.getPlane()), target, 2231, 15, 30, 45, 5, 100, 0));
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getId() == 22501 ? 104 : 165, NPCCombatDefinitionConstants.MAGE, target)));
		return 5;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22501 };
	}

}
