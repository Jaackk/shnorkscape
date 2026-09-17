package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.dragons.CelestialDragonB;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CelestialDragon extends CombatScript {

	int tick = 0;
	boolean timeTrap;
	
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		
		if(tick < 5) {
			if(npc.withinDistance(target, 2)) {
				meleeAttack(npc, target);
			} else {
				switch(Utils.random(4)) {
					case 0:
					case 1:
						dragonFireAttack(npc, target);
						break;
					case 2:
					case 3:
						rangeAttack(npc, target);
						break;
				}
			}
		} else {
			if(!timeTrap) 
				timeTrap(npc, target);
			
			tick = 0;
		}
		
		tick++;
		
		return defs.getAttackDelay();
	}
	
	public void rangeAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(26524));
		World.sendProjectile(npc, target, 16, 28, 16, 35, 20, 16, 0);
		int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
		damage += Utils.random(150, 200);
		
		delayHit(npc, 1, target, getRangeHit(npc, damage));
	}

	public void timeTrap(NPC npc, Entity target) {
		final Player player = target instanceof Player ? (Player) target : null;
		CelestialDragonB celdragon = (CelestialDragonB) npc;
		
		if (target instanceof Player) {
			Familiar familiar = player.getFamiliar();
			timeTrap = true;
			
			WorldTasksManager.schedule(new WorldTask() {
				int stage;
				
				@Override
				public void run() {
					switch(stage) {
						case 0:
							npc.setNextGraphics(new Graphics(4614));
							break;
						case 1:
							player.setNextGraphics(new Graphics(4613));
							tick = 0;
							
							if(familiar != null) {
								celdragon.familiarTeleported = true;
								celdragon.familiarTile = new WorldTile(familiar.getX(), familiar.getY(), familiar.getPlane());
								familiar.setNextWorldTile(new WorldTile(familiar.getX(), familiar.getY(), familiar.getPlane()+1));
								familiar.callBlocked = true;
								familiar.setCantInteract(true);	
							}
							
							player.addFreezeDelay(10000);
							break;
						case 7:
							player.setFreezeDelay(0);
							if(familiar != null) {
								familiar.setCantInteract(false);
								familiar.callBlocked = false;
							}
							timeTrap = false;
							tick = 0;
							stop();
							return;
					}
					
					stage++;
				}
				
				
			}, 1, 1);
					
		}
	}
	
	
	
	public void meleeAttack(NPC npc, Entity target) {
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 0, target,
				getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
	}
	
	public void dragonFireAttack(NPC npc, Entity target) {
		final Player player = target instanceof Player ? (Player) target : null;
		int damage = Utils.random(80, 550 + npc.getCombatLevel());
		if (target instanceof Player) {
			String message = Combat.getProtectMessage(player);
			if (message != null) {
				player.sendMessage(message, true);
				if (message.contains("fully"))
					damage *= 0;
				else if (message.contains("most"))
					damage *= 0.05;
				else if (message.contains("some"))
					damage *= 0.1;
			}
			if (damage > 0)
				player.sendMessage(
						"You are hit by the dragon's fiery breath!",
						true);
		}
		npc.setNextAnimation(new Animation(14245));
		World.sendProjectile(npc, target, 439, 28, 16, 35, 20, 16, 0);
		delayHit(npc, 1, target, getRegularHit(npc, damage));
	}
	
	@Override
	public Object[] getKeys() {
		return new Object[] {19109};
	}

}
