package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.airut.Airut;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/*
 * author Movee
 */

public class AirutMeleeCombat extends CombatScript {

	public boolean berserk;
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Airut airut = (Airut) npc;

		if(Utils.random(14) == 5 && !airut.BerserkMode) 
			Berserk(npc, target);
		
		if(!berserk) { 
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
			delayHit(npc, 0, target, getMeleeHit(npc, damage));
		} else {
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
			damage *= 2;
			if(npc.withinDistance(target, 1)) {
				//delayHit(npc, 0, target, getMeleeHit(npc, damage));
				target.applyHit(new Hit(target, damage, HitLook.MELEE_DAMAGE));
			}
		}
		
		
		return berserk ? 1 : defs.getAttackDelay();
	}
	
	public void Berserk(NPC npc, Entity target) {
		Airut airut = (Airut) npc;

		airut.BerserkMode = true;
		
		WorldTasksManager.schedule(new WorldTask() {
			int tick;
			
			@Override
			public void run() {
				switch(tick) {
					case 0:
						npc.setNextAnimation(new Animation(22170));
						break;
					case 1:
						npc.setNextAnimation(new Animation(22175));
						berserk = true;
						break;
					case 4:
						airut.BerserkMode = false;
						berserk = false;
						stop();
						return;
				}
				
				tick++;
			}
			
		}, 1, 1);
	}

	@Override
	public Object[] getKeys() {
		return new Object[] {18621};
	}

}
