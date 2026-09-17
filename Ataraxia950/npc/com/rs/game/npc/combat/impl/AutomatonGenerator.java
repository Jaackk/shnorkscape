package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class AutomatonGenerator extends CombatScript {

	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		
		npc.setNextAnimation(new Animation(19819));
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
		World.sendProjectile(npc, target, 5031, 41, 16, 41, 35, 16, 0);
		delayHit(npc, 2, target, getMagicHit(npc, damage));
	
		if(Utils.random(15) == 10) {
			pulseAttack(npc, target);
			return 8;
		}
		
		return defs.getAttackDelay();
	}
	
	public void pulseAttack(NPC npc, Entity target) {
		
		npc.setCannotMove(true);
		
		WorldTasksManager.schedule(new WorldTask() {
			int loop;
			final WorldTile npcTile = new WorldTile(npc);
			
			@Override
			public void run() {
				switch(loop) {
					case 0:
						npc.setNextAnimation(new Animation(19818));
						break;
					case 1:
						if(npc.withinDistance(target, 5)) {
							target.applyHit(new Hit(target, 150, HitLook.REGULAR_DAMAGE));
						}
						World.sendGraphics(npc, new Graphics(5069), npcTile);
						break;
					case 2: 
						if(npc.withinDistance(target, 5)) {
							target.applyHit(new Hit(target, 150, HitLook.REGULAR_DAMAGE));
						}
						World.sendGraphics(npc, new Graphics(5069), npcTile);
						break;
						
					case 3:
						if(npc.withinDistance(target, 5)) {
							target.applyHit(new Hit(target, 150, HitLook.REGULAR_DAMAGE));
						}
						World.sendGraphics(npc, new Graphics(5069), npcTile);
						break;
					case 4:
						if(npc.withinDistance(target, 5)) {
								target.applyHit(new Hit(target, 150, HitLook.REGULAR_DAMAGE));
						}
						World.sendGraphics(npc, new Graphics(5069), npcTile);
						break;
					case 5:
						npc.setCannotMove(false);
						stop();
						npc.setTarget(target);
						return;
						
				}
				
				loop++;
			}
			
		}, 1, 1);
	}
		

	@Override
	public Object[] getKeys() {
		return new Object[] {16905};
	}

}
