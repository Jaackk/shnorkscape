package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.telos.Telos;
import com.rs.game.npc.telos.Telos.TelosAttacks;
import com.rs.game.player.Player;

public class TelosCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 22891, 22908 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		Telos telos = (Telos) npc;
		if (!(target instanceof Player)) {// Player uses Familiar, problem
											// happens here but this should be
											// fixed in Telos
			telos.switchTarget(true);
			return 1;
		}
		Player player = (Player) target;
		if (telos.startInstantBombAttack()) {
			int delay = TelosAttacks.FONT_SIPHON.sendAttack(telos, player);
			telos.setAutoAttacks(2);
			telos.addResetAttackDelayCycle();
			return delay;
		}
		if (telos.getAutoAttacks() >= 1) {
			int delay = telos.getPhase() == 4 ? TelosAttacks.MAGICAL_DISCHARGE.sendAttack(telos, player)
					: TelosAttacks.SWIPE_MAGICAL_DISCHARGE.sendAttack(telos, player);
			telos.decreaseAutoAttacks();
			telos.addResetAttackDelayCycle();
			return delay;
		}
		if ((telos.getPhase() == 0 || telos.getPhase() == 3) && telos.getAnima() == 100) {
			int delay = TelosAttacks.ANIMA_ULTIMATE.sendAttack(telos, player);
			telos.setAutoAttacks(2);
			telos.addResetAttackDelayCycle();
			return delay;
		}
		TelosAttacks attack = Telos.SpecialAttacks[telos.getPhase()][telos.getAttackRotation()];
		if (attack == null) {
			telos.increaseAttackRotation();
			return 0;
		}
		int delay = attack.sendAttack(telos, player);
		telos.processRedBarSpecialAttack(attack == TelosAttacks.VIRUS);
		telos.setAutoAttacks(attack == TelosAttacks.MINIONS ? 10
				: (telos.getPhase() == 4 && attack == TelosAttacks.VIRUS) ? 15 : attack == TelosAttacks.STUN ? 2 : 3);
		telos.increaseAttackRotation();
		telos.addResetAttackDelayCycle();
		return delay;
	}

}
