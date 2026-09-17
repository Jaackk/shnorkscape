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

public class CloackedzealCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		if (npc.withinDistance(target, npc.getSize())) {
			switch (Utils.random(10)) {
			case 1:
				meleeAttack2(npc, target);
				break;
			case 2:
				rangeAttack(npc, target);
				break;
			case 3:
				poisonAttack(npc, target);
				break; 
			case 4:
				mageAttack(npc, target);
				break;
			case 5:
				aoeAttack(npc, target);
				break;	
			default:
				meleeAttack(npc, target);
				break;
			}
		} else {
			switch (Utils.random(5)) {
			case 0:
			case 1:
				rangeAttack(npc, target);
				break;
			case 2:
				aoeAttack(npc, target);
				break;
			case 3:
				rangeAttack(npc, target);
				break;
			case 4:
				poisonAttack(npc, target);
				break;
			default:
				mageAttack(npc, target);
				break;
			}
		}
		return defs.getAttackDelay();
	}

	public void mageAttack(NPC npc, Entity target) {
		npc.setNextForceTalk(new ForceTalk(""));
		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(14339));
		World.sendGraphics(npc, new Graphics(6865), t);
			int damage = 0;
			damage = getRandomMaxHit(npc, 122, NPCCombatDefinitionConstants.MAGE, target);
		delayHit(npc, 0, t, getMagicHit(npc, damage));
	}
	}
//test
	public void aoeAttack(NPC npc, Entity target) {
    	for (Entity t : npc.getPossibleTargets()) {
    	npc.setNextAnimation(new Animation(18221));
	    final WorldTile center = new WorldTile(t);
	    World.sendGraphics(npc, new Graphics(6991), center);   //5649
	    npc.setNextForceTalk(new ForceTalk("Reef Power Imcoming"));
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
			    delayHit(npc, 0, player,
				    new Hit(npc, Utils.random(25),
					    HitLook.REGULAR_DAMAGE));
			}
		    }
		    if (count++ == 10) {
			stop();
			return;
		      }
		   
		}
	    }, 0, 0);
    	}	
    }	
//	
	public void rangeAttack(NPC npc, Entity target) {
		npc.setNextForceTalk(new ForceTalk(""));
		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(18100));
		World.sendProjectile(npc, t, 6896, 41, 16, 41, 35, 16, 0);//39, 36, 41, 50, 0, 100); 
			int damage = 0;
			damage = getRandomMaxHit(npc, 140, NPCCombatDefinitionConstants.MAGE, target);
		delayHit(npc, 0, t, getMagicHit(npc, damage));
	}
	}
	public void poisonAttack(NPC npc, Entity target) {
		for (Entity t : npc.getPossibleTargets()) {
		final Player player = t instanceof Player ? (Player) t : null;	
		if (player != null) {
			npc.setNextAnimation(new Animation(18221));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.getPackets().sendGameMessage("", true);
					int damage = 0;
					damage = getRandomMaxHit(npc, 140, NPCCombatDefinitionConstants.MAGE, target);
					player.setNextGraphics(new Graphics(6898, 50, 0));
					player.getPoison().makePoisoned(100);
					stop();
				}
			}, 0);
		}
	}
	}

	public void meleeAttack(NPC npc, Entity target) {
		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(18221));
			int damage = 0;
			damage = getRandomMaxHit(npc, 140, NPCCombatDefinitionConstants.MAGE, target);
		World.sendGraphics(npc, new Graphics(5516), t);
		damage += Utils.random(150, 200);
		delayHit(npc, 0, t, getMagicHit(npc, damage));
	}
	}
	public void meleeAttack2(NPC npc, Entity target) {
		for (Entity t : npc.getPossibleTargets()) {
			npc.setNextAnimation(new Animation(21141));
			int damage = 0;
			damage = getRandomMaxHit(npc, 140, NPCCombatDefinitionConstants.MAGE, target);
			World.sendGraphics(npc, new Graphics(5516), t);
			damage += Utils.random(300, 350);
			delayHit(npc, 0, t, getMagicHit(npc, damage));

		}
	}


	
	@Override
	public Object[] getKeys() {
		return new Object[] {  26175};
	}

}