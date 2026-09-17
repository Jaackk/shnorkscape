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

public class MatureGrotworm extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		int hit = 0;
		if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
			mageAttack(npc, target, hit);
			return defs.getAttackDelay();
		}
		int attackStyle = Utils.getRandom(1);
		switch (attackStyle) {
		case 0:
			hit = getRandomMaxHit(npc, Utils.random(300), NPCCombatDefinitionConstants.MELEE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMagicHit(npc, Utils.random(200)));
			break;
		case 1:
			mageAttack(npc, target, hit);
			break;
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Mature grotworm", 15463 };
	}

	private void mageAttack(final NPC npc, final Entity target, int hit) {
		hit = getRandomMaxHit(npc, Utils.random(300), NPCCombatDefinitionConstants.MAGE, target);
		npc.setNextAnimation(new Animation(16789));
		npc.setNextGraphics(new Graphics(3163));
		World.sendProjectile(npc, target, 3164, 34, 16, 35, 35, 16, 0);
		delayHit(npc, 2, target, getMagicHit(npc, Utils.random(200)));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.setNextGraphics(new Graphics(3165));
				stop();
			}
		}, 2);
	}
}