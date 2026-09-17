package com.rs.game.npc.combat.impl.dung;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.familiar.Familiar;

public class BloodragerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[]
				{ 11106, 11108, 11110, 11112, 11114, 11116, 11118, 11120, 11122, 11124, 11126 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		@SuppressWarnings("unused")
        NPCCombatDefinition def = npc.getCombatDefinitions();
		Familiar familiar = (Familiar) npc;
		boolean usingSpecial = familiar.hasSpecialOn();
		int tier = (npc.getId() - 11106) / 2;

		int damage = 0;
		if (usingSpecial) {
			npc.setNextGraphics(new Graphics(2444));
			damage = getRandomMaxHit(npc, (int) (npc.getMaxHit(NPCCombatDefinitionConstants.MELEE) * (1.05 * tier)), NPCCombatDefinitionConstants.MELEE, target);
		} else
			damage = getMaxHit(npc, NPCCombatDefinitionConstants.MELEE, target);
		delayHit(npc, usingSpecial ? 1 : 0, target, getMeleeHit(npc, damage));
		npc.setNextAnimation(new Animation(13617));
		return npc.getAttackSpeed();
	}
}
