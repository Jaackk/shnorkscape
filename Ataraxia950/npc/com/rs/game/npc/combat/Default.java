package com.rs.game.npc.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.npc.kalphiteking.ExiledKalphiteMarauder;
import com.rs.game.npc.telos.ColoredAnimaGolem;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Default extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		if (npc instanceof ColoredAnimaGolem && !Utils.isOnRange(npc, target, 0)) {
			return 0;
		}
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		int attackStyle = defs.getAttackStyle();
		if (attackStyle == NPCCombatDefinitionConstants.MELEE) {
			if (npc instanceof DungeonNPC)
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), attackStyle, target)));
			else
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target)));
		} else {
			int damage = 0;
			if (npc instanceof DungeonNPC)
				damage = getRandomMaxHit(npc, npc.getMaxHit(), attackStyle, target);
			else
				damage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, target);
			delayHit(npc, 2, target, attackStyle == NPCCombatDefinitionConstants.RANGE ? getRangeHit(npc, damage)
					: getMagicHit(npc, damage));
			if (defs.getAttackProjectile() != -1)
				World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 16, 41, 35, 16, 0);
			if (npc instanceof ExiledKalphiteMarauder) {
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						target.getPoison().makePoisoned(Utils.random(40, 70), 5000);
					}
				}, 2);
			}
		}
		if (defs.getAttackGfx() != -1)
			npc.setNextGraphics(new Graphics(defs.getAttackGfx()));
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		int delay = defs.getAttackDelay();
		if (npc instanceof DungeonNPC) {
			if (npc.getTemporaryAttributtes().get("sloweddown") != null)
				delay++;
		}
		return delay;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Default" };
	}
}
