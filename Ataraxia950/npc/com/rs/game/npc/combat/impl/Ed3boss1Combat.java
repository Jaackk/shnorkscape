package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.npc.Ed3boss1.Ed3boss1;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.player.Player;

public class Ed3boss1Combat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		Ed3boss1 ed3boss1 = (Ed3boss1) npc;
		if (!(target instanceof Player)) {// Player uses Familiar, problem
			// happens here but this should be
			// fixed in Telos
			ed3boss1.switchTarget(true);
			return 1;
		}
		Player player = (Player) target;



		Ed3boss1.Ed3boss1Attacks attack = Ed3boss1.SpecialAttacks[ed3boss1.getPhase()][ed3boss1.getAttackRotation()];
		if (attack == null) {
			ed3boss1.increaseAttackRotation();
			return 0;
		}
		int delay = attack.sendAttack(ed3boss1, player);
		ed3boss1.increaseAttackRotation();
		ed3boss1.addResetAttackDelayCycle();
		return delay;
	}
	

	
	@Override
	public Object[] getKeys() {
		return new Object[] { 26050 };
	}

}