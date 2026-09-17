package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.World;
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

public class AirutRangedCombat extends CombatScript {

	boolean berserk;
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		Airut airut = (Airut) npc;
		
		if(Utils.random(14) == 5 && !airut.BerserkMode) 
			Berserk(npc, target);

		
		if(berserk) {
			npc.setNextGraphics(new Graphics(4798));
			npc.setNextAnimation(new Animation(22155));
			int damage = getRandomMaxHit(npc, 120, NPCCombatDefinitionConstants.RANGE, target);
			damage *= 2;
			World.sendProjectile(new NewProjectile(npc, target, defs.getAttackProjectile(), 41, 30, 25, 16, 35, 0));
			delayHit(npc, 2, target, getRangeHit(npc, damage));
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
			World.sendProjectile(new NewProjectile(npc, target, defs.getAttackProjectile(), 41, 30, 25, 16, 30, 0));
			delayHit(npc, 2, target, getRangeHit(npc, damage));
		}
		
		
		

		return berserk ? 2 : defs.getAttackDelay();
	}
	
	public void Berserk(NPC npc, Entity target) {
		Airut airut = (Airut) npc;
		
		WorldTasksManager.schedule(new WorldTask() {
			int stage = 0;
			
			@Override
			public void run() {
				switch(stage) {
					case 0:
						npc.setNextAnimation(new Animation(22154));
					break;
					
					case 1:
						airut.BerserkMode = true;
						berserk = true;
						break;
						
					case 8: 
						airut.BerserkMode = false;
						berserk = false;
						stop();
						return;
				}

				stage++;
			}
			
		}, 1, 1);
	}

	@Override
	public Object[] getKeys() {
		return new Object[] {18622};
	}

}
