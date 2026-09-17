package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.vorago.Vorago;
import com.rs.game.npc.vorago.Vorago.VoragoAttacks;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class VoragoCombat extends CombatScript {

	private Vorago vorago;

	public static int WHITE_BORDER = 0, RED_BORDER = 1;

	@Override
	public Object[] getKeys() {
		return new Object[] { "Vorago" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		if (!(npc instanceof Vorago))
			return 0;
		if (!(target instanceof Player)) {
			npc.switchTarget(true);
			return 0;
		}
		vorago = (Vorago) npc;
		if (Utils.colides(vorago, target)) {
			if (!vorago.isAtLastPhase()) {
				vorago.calcFollow(target, true);
				if (Utils.random(10) == 8 && vorago.getTemporaryAttributtes().get("CantBeAttacked") == null)
					sendBindAttack();
				return 0;
			}
		}
		if (vorago.getTemporaryAttributtes().get("BringHimDownClick") != null)
			return 0;
		if (vorago.getTemporaryAttributtes().get("VoragoType") != null
				&& ((int) vorago.getTemporaryAttributtes().get("VoragoType") == 1) && !vorago.isAtLastPhase()) {
			vorago.setCantFollowUnderCombat(Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(), target.getSize(),
					9));
		}
		VoragoAttacks attack = VoragoAttacks.ATTACK;
		return attack.sendAttack(vorago, (Player) target);
	}

	public void sendBindAttack() {
		Player player = (Player) vorago.getCombat().getTarget();
		if (player == null)
			return;
		delayHit(vorago, 0, player, new Hit(vorago, 600, HitLook.REGULAR_DAMAGE));
	}

}
