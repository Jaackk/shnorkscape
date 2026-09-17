package com.rs.game.npc.combat.impl.nihils;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MuspahCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		if (npc.getId() == 19152)
			delayHit(npc, 0, target, getMeleeHit(npc, Utils.random(50)));
		else if (npc.getId() == 19151) {
			delayHit(npc, 1, target, getMagicHit(npc, Utils.random(50)));
			World.sendProjectile(npc, npc, target, 4682, 40, 40, 30, 30, 0, 0);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(4683));
				}
			}, 1);
		} else {
			delayHit(npc, 1, target, getRangeHit(npc, Utils.random(50)));
			World.sendProjectile(npc, npc, target, 4679, 5, 5, 30, 30, 0, 0);
		}
		return npc.getAttackSpeed();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 19150, 19151, 19152 };
	}

}
