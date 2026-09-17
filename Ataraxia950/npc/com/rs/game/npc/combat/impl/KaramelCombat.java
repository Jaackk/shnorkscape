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
import com.rs.utils.Utils;

public class KaramelCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		switch (1) {
		case 1: // Magic
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			World.sendProjectile(npc, target, 368, 60, 32, 50, 50, 0, 0);
			target.setNextGraphics(new Graphics(369));
			delayHit(npc, 1, target, getMagicHit(npc, damage));
			if (Utils.random(10) == 0) {
				if (target instanceof Player) {
					player.getSkills().drainLevel(0, player.getSkills().getLevel(0) / 15);
					player.getSkills().drainLevel(1, player.getSkills().getLevel(1) / 15);
					player.getSkills().drainLevel(2, player.getSkills().getLevel(2) / 15);
					player.getSkills().drainLevel(4, player.getSkills().getLevel(2) / 15);
					player.getSkills().drainLevel(6, player.getSkills().getLevel(2) / 15);
					player.sendMessage("<col=CC0033>Karamel drained your combat skills!");
				}
			}
			break;
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Karamel" };
	}
}