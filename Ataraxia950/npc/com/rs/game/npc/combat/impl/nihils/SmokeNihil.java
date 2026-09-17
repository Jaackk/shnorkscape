package com.rs.game.npc.combat.impl.nihils;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.Nihils;
import com.rs.utils.Utils;

public class SmokeNihil extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 19149 };
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
			((Nihils) npc).setSmokeTile(new WorldTile(target.getX(), target.getY(), target.getPlane()));
			start = false;
		}
		npc.setNextAnimation(new Animation(23037));
		World.sendProjectile(npc, npc, target, 4692, 0, 10, 3, 15, 0, 0);
		delayHit(npc, 0, target, getMagicHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitionConstants.MAGE, target)));
		return 6;
	}

}
