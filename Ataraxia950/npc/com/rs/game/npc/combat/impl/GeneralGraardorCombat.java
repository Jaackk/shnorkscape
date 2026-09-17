package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class GeneralGraardorCombat extends CombatScript {
	
	@Override
	public int attack(final NPC npc, final Entity target) {
		boolean hardMode = ((GeneralGraardor) npc).isHardMode();
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		if (Utils.getRandom(4) == 0) {
			switch (Utils.getRandom(10)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("Death to our enemies!"));
				npc.playSound(3219, 2);
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("Brargh!"));
				npc.playSound(3209, 2);
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("Break their bones!"));
				break;
			case 3:
				npc.setNextForceTalk(new ForceTalk("For the glory of Bandos!"));
				break;
			case 4:
				npc.setNextForceTalk(new ForceTalk("Split their skulls!"));
				npc.playSound(3229, 2);
				break;
			case 5:
				npc.setNextForceTalk(new ForceTalk("We feast on the bones of our enemies tonight!"));
				npc.playSound(3206, 2);
				break;
			case 6:
				npc.setNextForceTalk(new ForceTalk("CHAAARGE!"));
				npc.playSound(3220, 2);
				break;
			case 7:
				npc.setNextForceTalk(new ForceTalk("Crush them underfoot!"));
				npc.playSound(3224, 2);
				break;
			case 8:
				npc.setNextForceTalk(new ForceTalk("All glory to Bandos!"));
				npc.playSound(3205, 2);
				break;
			case 9:
				npc.setNextForceTalk(new ForceTalk("GRAAAAAAAAAR!"));
				npc.playSound(3207, 2);
				break;
			case 10:
				npc.setNextForceTalk(new ForceTalk("FOR THE GLORY OF THE BIG HIGH WAR GOD!"));
				break;
			}
		}
		if (Utils.getRandom(2) == 0) { // range attack
			npc.setNextAnimationForce(new Animation(17391));
			for (Entity t : npc.getPossibleTargets()) {
				delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 355, NPCCombatDefinitionConstants.RANGE, t)));
				World.sendProjectile(npc, t, 1200, 41, 16, 41, 35, 16, 0);
			}
		} else {
			if (npc.withinDistance(target, 3)) {
				if (Utils.getRandom(2) == 0) {
					if (hardMode) {
						int random = Utils.getRandom(3);
						if (random == 0) {
							npc.setNextForceTalk(new ForceTalk("Graardor mad!"));
							((GeneralGraardor) npc).setTargetTile(new WorldTile(target));
							npc.resetWalkSteps();
							npc.setCannotMove(true);
							WorldTasksManager.schedule(new WorldTask() {
								int ticks;

								@Override
								public void run() {
									if (npc.isDead() || npc.hasFinished()) {
										stop();
										return;
									}
									for (Entity entities : npc.getPossibleTargets()) {
										if (entities.withinDistance(((GeneralGraardor) npc).getTargetTile(), 2))
											entities.applyHit(new Hit(npc, Utils.random(200, 400), HitLook.REGULAR_DAMAGE));
									}
									if (ticks == 5) {
										npc.setCannotMove(false);
										npc.setTarget(target);
										stop();
										return;
									}
									npc.setNextAnimationForce(new Animation(17389));
									ticks++;
								}
							}, 0, 2);
							return 10;
						} else if (random == 1) {
							npc.setNextForceTalk(new ForceTalk("Graardor protect!"));
							npc.setNextAnimation(new Animation(19844));
							npc.resetWalkSteps();
							npc.setCannotMove(true);
							((GeneralGraardor) npc).switchProtectMode();
							WorldTasksManager.schedule(new WorldTask() {
								int ticks;

								@Override
								public void run() {
									if (npc.isDead() || npc.hasFinished()) {
										stop();
										return;
									}
									if (ticks == 2) {
										((GeneralGraardor) npc).getProtectDamageEntities().forEach((k, v) -> {
											if (k != null && !k.hasFinished() && !k.isDead())
												k.applyHit(new Hit(npc, v / 2, HitLook.REFLECTED_DAMAGE));
										});
										((GeneralGraardor) npc).switchProtectMode();
										((GeneralGraardor) npc).clearDamage();
										npc.setNextAnimationForce(new Animation(-1));
									} else if (ticks == 3) {
										npc.setCannotMove(false);
										npc.setTarget(target);
										stop();
										return;
									}
									ticks++;
								}
							}, 0, 1);
							return 4;
						} else if (random == 2) {
							npc.setNextForceTalk(new ForceTalk("Graardor smash!"));
							npc.setNextAnimationForce(new Animation(19843));
							npc.resetWalkSteps();
							npc.setCannotMove(true);
							WorldTasksManager.schedule(new WorldTask() {
								int ticks;

								@Override
								public void run() {
									if (npc.isDead() || npc.hasFinished()) {
										stop();
										return;
									}
									if (ticks == 7) {
										npc.setCannotMove(false);
										npc.setTarget(target);
									} else if (ticks > 4 && ticks < 16) {
										if (Utils.random(2) == 1) {
											WorldTile targetTile = new WorldTile(npc.getRespawnTile(), 6);
											World.sendGraphics(null, new Graphics(3862), targetTile);
											npc.getPossibleTargets().forEach(target -> {
												if (target.withinDistance(targetTile, 3))
													target.applyHit(new Hit(npc, Utils.random(100, 500), HitLook.REGULAR_DAMAGE));
											});
										}
									} else if (ticks == 20) {
										stop();
										return;
									}
									ticks++;
								}
							}, 0, 1);
							return 8;
						}
					}
				}
				npc.setNextAnimationForce(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
				return defs.getAttackDelay();
			} else {
				npc.setNextAnimationForce(new Animation(17391));
				for (Entity t : npc.getPossibleTargets()) {
					delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 355, NPCCombatDefinitionConstants.RANGE, t)));
					World.sendProjectile(npc, t, 1200, 41, 16, 41, 35, 16, 0);
				}
			}
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 6260, 16989 };
	}
}