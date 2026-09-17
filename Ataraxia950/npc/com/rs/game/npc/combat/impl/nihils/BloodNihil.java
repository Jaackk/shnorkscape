package com.rs.game.npc.combat.impl.nihils;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.Nihils;
import com.rs.utils.Utils;

public class BloodNihil extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 19146 };
	}

	private boolean start;
	
	@Override
	public int attack(NPC npc, Entity target) {
		if (!(npc instanceof Nihils)) {
			return 0;
		}
		if (Utils.random(10) == 0) {
			npc.setNextForceTalk(new ForceTalk("Hiss."));
			start = true;
			return 2;
		}
		if (start) {
			npc.setNextAnimation(new Animation(17409));
			npc.setNextGraphics(new Graphics(3370));
			((Nihils) npc).startHealing();
			start = false;
			return 10;
		}
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		npc.setNextGraphics(new Graphics(4690));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitionConstants.MELEE, target)));
		return 6;
	}

}
