package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

public class FrostDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Frost dragon" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		int damage;
		switch (Utils.getRandom(3)) {
		case 0: // Melee
			if (npc.withinDistance(target, 1)) {
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, damage));
			} else {
				damage = Utils.random(1, 650);
				if (target instanceof Player) {
					String message = Combat.getProtectMessage(player);
					if (message != null) {
						player.sendMessage(message, true);
						if (message.contains("fully")) {
							damage *= 0;
						}
						if (message.contains("most")) {
							damage *= 0.05;
						}
						if (message.contains("some")) {
							damage *= 0.1;
						}
					} else
						player.sendMessage("You are hit by the dragon's fiery breath!", true);
				}
				npc.setNextAnimation(new Animation(13155));
				npc.setNextGraphics(new Graphics(2465));
				World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			}
			break;
		case 1: // Range
			damage = Utils.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 2707, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 2: // Ice arrows range
			damage = Utils.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 369, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 3:
			damage = Utils.random(1, 650);
			if (target instanceof Player) {
				String message = Combat.getProtectMessage(player);
				if (message != null) {
					player.sendMessage(message, true);
					if (message.contains("fully")) {
						damage *= 0;
					}
					if (message.contains("most")) {
						damage *= 0.05;
					}
					if (message.contains("some")) {
						damage *= 0.1;
					}
				} else
					player.sendMessage("You are hit by the dragon's fiery breath!", true);
			}
			npc.setNextAnimation(new Animation(13155));
			npc.setNextGraphics(new Graphics(2465));
			World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRegularHit(npc, damage));
			break;
		case 4: // Orb crap
			break;
		}
		return defs.getAttackDelay();
	}
}