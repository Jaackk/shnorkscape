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
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class MossgolemCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		if (npc.withinDistance(target, npc.getSize())) {
			switch (Utils.random(5)) {
				case 1:
					rangeAttack(npc, target);
					break;
				case 2:
					rangeAttack(npc, target);
					break;
				case 3:
					rangeAttack(npc, target);
					break;
				case 4:
					mageAttack(npc, target);
					break;
				case 5:
					rangeAttack(npc, target);
					break;
			}

		}
		return defs.getAttackDelay();
	}
	public void mageAttack(NPC npc, Entity target) {
		for (Entity t : npc.getPossibleTargets()) {
			npc.setNextAnimation(new Animation(31086));
			final WorldTile center = new WorldTile(t);
			World.sendGraphics(npc, new Graphics(6888), center);   //5649
			npc.setNextForceTalk(new ForceTalk("MOSS Power Incoming"));
			WorldTasksManager.schedule(new WorldTask() {
				int count = 0;

				@Override
				public void run() {
					for (Player player : World.getPlayers()) { // lets just loop
						// all players
						// for massive
						// moves

						if (player == null || player.isDead()
								|| player.hasFinished())
							continue;
						if (player.withinDistance(center, 1)) {
							delayHit(npc, 2, player,
									new Hit(npc, Utils.random(150),
											HitLook.REGULAR_DAMAGE));
						}
					}
					if (count++ == 8) {
						stop();
						return;
					}

				}
			}, 0, 0);
		}
	}

//
	public void rangeAttack(NPC npc, Entity target) {

		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(31085));
			World.sendGraphics(npc, new Graphics(6864), t);
		int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, t);
		damage += Utils.random(150, 350);
		delayHit(npc, 1, t, getMeleeHit(npc, damage));
	}
	}


	

	
	@Override
	public Object[] getKeys() {
		return new Object[] { 24832 };
	}

}