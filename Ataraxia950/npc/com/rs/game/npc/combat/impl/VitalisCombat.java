package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.vorago.Vitalis;
import com.rs.utils.Utils;

public class VitalisCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 17157 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		Vitalis vita = (Vitalis) npc;
		if (!Utils.isOnRange(vita, target, 1))
			return 0;
		NPCCombatDefinition def = vita.getCombatDefinitions();
		vita.setNextAnimation(new Animation(def.getAttackEmote()));
		int damage = getMaxHit(vita, 125, NPCCombatDefinitionConstants.MELEE, target);
		delayHit(vita, 0, target, getMeleeHit(npc, damage));
		return 8;
	}

}
