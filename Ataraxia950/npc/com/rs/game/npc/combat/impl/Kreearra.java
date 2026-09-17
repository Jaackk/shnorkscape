package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Kreearra extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		boolean hardMode = ((KreeArra) npc).isHardMode();
		int possibleTargets = npc.getPossibleTargets().size();
		if (hardMode) {
			if (Utils.random(2) == 1)
				((KreeArra) npc).spawnWhirlwind();

			((KreeArra) npc).setAttacks(((KreeArra) npc).getAttacks() + 1);
			if (((KreeArra) npc).getAttacks() == 8) {
				((KreeArra) npc).setAttacks(0);
				npc.setTarget(null);
				npc.setCantInteract(true);
				npc.setNextForceTalk(new ForceTalk("Storms, align to me!"));
				npc.setForceWalk(npc.getRespawnTile());
				for (int i = 0; i < 10; i++) {
					if (((KreeArra) npc).getWhirlwinds()[i] != null) {
						((KreeArra) npc).getWhirlwinds()[i].setForceWalk(npc.getRespawnTile());
						((KreeArra) npc).getWhirlwinds()[i].setCantInteract(true);
					}
				}
				WorldTasksManager.schedule(new WorldTask() {
					boolean returnToCombat;
					int ticks;

					@Override
					public void run() {
						if (returnToCombat) {
							for (int i = 0; i < 10; i++) {
								if (((KreeArra) npc).getWhirlwinds()[i] != null) {
									WorldTile toTile = null;
									for (int x = 0; x < 10; x++) {
										toTile = new WorldTile(npc.getRespawnTile(), 15);
										if (World.isTileFree(toTile.getPlane(), toTile.getX(), toTile.getY(), 1))
											break;
									}
									((KreeArra) npc).getWhirlwinds()[i].setForceWalk(toTile);
									((KreeArra) npc).getWhirlwinds()[i].setCantInteract(false);
								}
							}
							npc.setTarget(target);
							npc.setCantInteract(false);
							stop();
							return;
						}
						if (!npc.hasWalkSteps() && ticks >= 4)
							returnToCombat = true;
						ticks++;
					}
				}, 0, 1);
				return 10;
			}
		}
		if (!npc.isUnderCombat()) {
			npc.setNextAnimation(new Animation(17396));
			if (hardMode) {
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 150, NPCCombatDefinitionConstants.MELEE, target)));
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 150, NPCCombatDefinitionConstants.MELEE, target)));
			} else
				delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 260, NPCCombatDefinitionConstants.MELEE, target)));
			return defs.getAttackDelay();
		}
		npc.setNextAnimation(new Animation(17397));
		for (Entity t : npc.getPossibleTargets()) {
			if (Utils.getRandom(2) == 0) {
				if (hardMode && possibleTargets > 1) {
					sendMagicAttack(npc, npc.getPossibleTargets().get(Utils.random(possibleTargets)));
					sendMagicAttack(npc, t);
				} else
					sendMagicAttack(npc, t);
			} else {
				delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 720, NPCCombatDefinitionConstants.RANGE, t)));
				World.sendProjectile(npc, t, 1197, 41, 16, 41, 35, 16, 0);
			}
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 6222 };
	}

	private void sendMagicAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(17397));
		for (Entity t : npc.getPossibleTargets()) {
			delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 210, NPCCombatDefinitionConstants.MAGE, t)));
			World.sendProjectile(npc, t, 1198, 41, 16, 41, 35, 16, 0);
			target.setNextGraphics(new Graphics(1196));
		}
	}
}
