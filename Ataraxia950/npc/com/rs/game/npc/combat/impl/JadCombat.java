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
import com.rs.utils.Utils;

public class JadCombat extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(3);
		if (attackStyle == 2) { // melee
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
				attackStyle = Utils.random(2); // set mage
			else {
				npc.setNextAnimation(new Animation(defs.getAttackEmote(), -1, -1, -1, -1, 0));
				delayHit(npc, 1, target,
						getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle == 1) { // range
			npc.setNextAnimation(new Animation(16202, -1, -1, -1, -1, 0));
			npc.setNextGraphics(new Graphics(2994));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					target.setNextGraphics(new Graphics(3000));
					delayHit(npc, 1, target, getRangeHit(npc,
							getRandomMaxHit(npc, defs.getMaxHit() - 2, NPCCombatDefinitionConstants.RANGE, target)));
				}
			}, 3);
		} else {
			npc.setNextAnimation(new Animation(16195, -1, -1, -1, -1, 0));
			npc.setNextGraphics(new Graphics(2995));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					World.sendProjectile(npc, target, 2996, 80, 30, 40, 20, 5, 0);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(2741, 0, 100));
							delayHit(npc, 0, target, getMagicHit(npc,
									getRandomMaxHit(npc, defs.getMaxHit() - 2, NPCCombatDefinitionConstants.MAGE, target)));
						}

					}, 1);
				}
			}, 2);
		}

		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {

		return new Object[] { 2745, 15208 };
	}

}
