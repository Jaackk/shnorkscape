package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

public class AdamantDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Adamant dragon" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		NPCCombatDefinition defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		switch (Utils.random(Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(),
				target.getSize(), 0) ? 4 : 3)) {
		case 3: // melee
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target,
					getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
			break;
		case 2: // magic
			npc.setNextAnimation(new Animation(14244));
			World.sendProjectile(npc, target, 2721, 28, 16, 35, 20, 16, 0);
			delayHit(npc, 1, target,
					getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target)));
			break;
		case 1: // range
			npc.setNextAnimation(new Animation(14244));
			World.sendProjectile(npc, target, 16, 28, 16, 35, 20, 16, 0);
			delayHit(npc, 1, target,
					getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target)));
			break;
		case 0: // dragonfire
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
				} else
					player.sendMessage("You are hit by the dragon's fiery breath!", true);
			}
			npc.setNextAnimation(new Animation(14245));
			World.sendProjectile(npc, target, 393, 28, 16, 35, 20, 16, 0);
			delayHit(npc, 1, target, getRegularHit(npc, damage));
			break;
		}
		return defs.getAttackDelay();
	}
}