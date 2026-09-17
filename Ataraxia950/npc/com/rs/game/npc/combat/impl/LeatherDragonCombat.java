package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class LeatherDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Green dragon", "Blue dragon", "Red dragon", "Black dragon", 742, 14548, 5362 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
			return 0;
		int damage = Utils.random(0, 100);
		if (Utils.getRandom(4) != 0) {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target,
					getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
			ArrayList<Entity> possibleTargets = npc.getPossibleTargets();
			for (Entity t : possibleTargets) {
				if (target.withinDistance(t, 2)) {
					if (t instanceof Player) {
						Player p = (Player) t;
						playSound(408, p, target);
					}
				}
			}
		} else {
			npc.setNextAnimation(new Animation(12259));
			npc.setNextGraphics(new Graphics(1, 0, 100));
			final Player player = target instanceof Player ? (Player) target : null;
			damage = Utils.random(80, 650);
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
			delayHit(npc, 1, target, getRegularHit(npc, damage));
		}
		return defs.getAttackDelay();
	}
}