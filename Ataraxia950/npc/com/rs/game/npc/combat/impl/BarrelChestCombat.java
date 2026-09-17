package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class BarrelChestCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		switch (1) {
		case 1: // Melee
			int damage = Utils.getRandom(450);
			npc.setNextAnimation(new Animation(Utils.random(2) == 1 ? 5894 : 5895));
			if (target instanceof Player && Utils.random(5) == 0) {
				player.getPrayer().drainPrayer(Utils.getRandom(65));
				player.sendMessage("Barrelchest drains some of your prayer points..", true);
			}
			delayHit(npc, 1, target, getMeleeHit(npc, damage));
			break;
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Barrelchest" };
	}
}