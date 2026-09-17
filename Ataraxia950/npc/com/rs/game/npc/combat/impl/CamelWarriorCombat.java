package com.rs.game.npc.combat.impl;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/*
 * @author Movee
 */

public class CamelWarriorCombat extends CombatScript {
	
	private boolean activeAttack;
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		
		int attackStyle = Utils.random(2);
		
		if(!activeAttack) {
			switch(attackStyle) {
				case 0:
					mageAttack(npc, target);
					return 6;
				case 1:
					rangedAttack(npc, target);
					return 4;
			}
		}
		
		return defs.getAttackDelay();
	}
	
	
	private void mageAttack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		
		activeAttack = true;
		npc.setNextAnimation(new Animation(27792));
		
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

			@Override
			public void run() {
				int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
				delayHit(npc, 1, target, getMagicHit(npc, damage));	
				target.setNextGraphics(new Graphics(5930));
				activeAttack = false;
			}
			
		}, 1, TimeUnit.SECONDS);
	}
	
	private void rangedAttack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		
		activeAttack = true;
		npc.setNextGraphics(new Graphics(5926));
		npc.setNextAnimation(new Animation(27793));
		
		
		CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

			@Override
			public void run() {
				int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
				delayHit(npc, 2, target, getRangeHit(npc, damage));	
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

					@Override
					public void run() {
						target.setNextGraphics(new Graphics(5928));
						activeAttack = false;
					}
					
				}, 1300, TimeUnit.MILLISECONDS);
				
			}
			
		}, 700, TimeUnit.MILLISECONDS);
		
	}

	@Override
	public Object[] getKeys() {
		return new Object[]  {22001};
	}

}
