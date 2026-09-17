package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class TheInadequacyCombat extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(6325));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				// World.sendProjectile(npc, target, 1067, 18, 18, 50, 30, 0,
				// 0);
				World.sendProjectile(target, npc, 1067, 34, 16, 30, 35, 16, 0);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						// target.setNextGraphics(new Graphics(1068, 0, 100));
						delayHit(npc, 0, target, getMagicHit(npc, Utils.random(350)));
					}
				}, 1);
			}
		}, 1);

		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {

		return new Object[] { 5902 };
	}

}
