package com.rs.game.npc.combat.impl.dung;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.dungeonnering.AsteaFrostweb;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class AsteaFrostwebCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Astea Frostweb" };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		if (Utils.getRandom(10) == 0) {
			AsteaFrostweb boss = (AsteaFrostweb) npc;
			boss.spawnSpider();
		}
		if (Utils.getRandom(10) == 0) { // spikes
			ArrayList<Entity> possibleTargets = npc.getPossibleTargets();
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			for (Entity t : possibleTargets)
				delayHit(npc, 1, t, new Hit(npc, Utils.random((int) (npc.getMaxHit(NPCCombatDefinitionConstants.MAGE) * 0.5) + 1), HitLook.REGULAR_DAMAGE));
			return defs.getAttackDelay();
		} else {
			int attackStyle = Utils.random(2);
			if (attackStyle == 1) { // check melee
				if (Utils.getDistance(npc.getX(), npc.getY(), target.getX(), target.getY()) > 1)
					attackStyle = 0; // set mage
				else { // melee
					npc.setNextAnimation(new Animation(defs.getAttackEmote()));
					delayHit(npc, 0, target, getMeleeHit(npc, getMaxHit(npc, NPCCombatDefinitionConstants.MELEE, target)));
					return defs.getAttackDelay();
				}
			}
			if (attackStyle == 0) { // mage
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				ArrayList<Entity> possibleTargets = npc.getPossibleTargets();

				int d = getMaxHit(npc, NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 1, target, getMagicHit(npc, d));
				if (d != 0) {
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							if (target.isFrozen())
								target.setNextGraphics(new Graphics(1677, 0, 100));
							else {
								target.setNextGraphics(new Graphics(369));
								target.setFreezeDelay(8);
							}
						}
					}, 1);
					for (final Entity t : possibleTargets) {
						if (t != target && t.withinDistance(target, 2)) {
							int damage = getMaxHit(npc, NPCCombatDefinitionConstants.MAGE, t);
							delayHit(npc, 1, t, getMagicHit(npc, damage));
							if (damage != 0) {
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										if (t.isFrozen())
											t.setNextGraphics(new Graphics(1677, 0, 100));
										else {
											t.setNextGraphics(new Graphics(369));
											t.setFreezeDelay(8);
										}
									}
								}, 1);
							}

						}
					}
				}
				if (Utils.getDistance(npc.getX(), npc.getY(), target.getX(), target.getY()) <= 1) { // lure
					// after
					// freeze
					npc.resetWalkSteps();
					npc.addWalkSteps(target.getX() + Utils.random(3), target.getY() + Utils.random(3));
				}
			}
		}
		return defs.getAttackDelay();
	}
}
