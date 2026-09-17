package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

public class MightyBloodReaverCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(7004));
		if (npc.withinDistance(target, npc.getSize()) && Utils.random(3) == 0) {
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 165, NPCCombatDefinitionConstants.MELEE, target)));
			return 4;
		}
		World.sendProjectile(new NewProjectile(new WorldTile(npc, npc.getPlane()), target, 2231, 15, 30, 45, 5, 100, 0));
		final int style = Utils.random(2);
		if (style == 0)
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 165, NPCCombatDefinitionConstants.MAGE, target)));
		else if (style == 1)
			delayHit(npc, 1, target, getRangeHit(npc, getRandomMaxHit(npc, 165, NPCCombatDefinitionConstants.RANGE, target)));
		return 4;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22502 };
	}

}
