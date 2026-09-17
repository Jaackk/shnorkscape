package com.rs.game.npc.combat.impl.nihils;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.Nihils;
import com.rs.utils.Utils;

public class ShadowNihil extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 19148 };
	}
	
	/**
	 * TODO: Defence mechanism.
	 */

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
			World.spawnObjectTemporary(new WorldObject(57261, 10, 0, target.getX(), target.getY(), 0), 1800);
			start = false;
		}
		npc.setNextAnimation(new Animation(23037));
		World.sendProjectile(npc, npc, target, 4692, 0, 10, 3, 15, 0, 0);
		delayHit(npc, 0, target, getRangeHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitionConstants.RANGE, target)));
		return 6;
	}

}
