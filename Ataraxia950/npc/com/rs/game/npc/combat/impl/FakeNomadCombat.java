package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class FakeNomadCombat extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(12697));
		boolean hit = getRandomMaxHit(npc, 50, NPCCombatDefinitionConstants.MAGE, target) != 0;
		delayHit(npc, 2, target, getRegularHit(npc, hit ? 50 : 0));
		World.sendProjectile(npc, target, 1657, 30, 30, 75, 25, 0, 0);
		if (hit) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(2278, 0, 100));
					this.stop();
				}
			}, 1);
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 8529 };
	}
}