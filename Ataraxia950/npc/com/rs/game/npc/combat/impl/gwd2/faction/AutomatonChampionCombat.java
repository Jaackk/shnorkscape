package com.rs.game.npc.combat.impl.gwd2.faction;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

public class AutomatonChampionCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		npc.setNextGraphics(new Graphics(6152));
		npc.setNextAnimation(new Animation(18364));
		World.sendProjectile(new NewProjectile(npc, target, 3565, 60, 25, 70, 5, 75, 0));
		delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, 104, NPCCombatDefinitionConstants.MAGE, target)));
		return 5;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 22476, 22477 };
	}

}
