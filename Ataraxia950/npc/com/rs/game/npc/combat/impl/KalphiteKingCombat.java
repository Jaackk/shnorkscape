package com.rs.game.npc.combat.impl;

import com.rs.game.Entity;
import com.rs.game.map.bossInstance.impl.KalphiteKingInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.game.npc.kalphiteking.KalphiteKing.KalphiteKingAttacks;
import com.rs.utils.Utils;

public class KalphiteKingCombat extends CombatScript {

	private KalphiteKing kalphiteKing;
	private KalphiteKingInstance instance;

	@Override
	public Object[] getKeys() {
		return new Object[] { "Kalphite King" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		kalphiteKing = (KalphiteKing) npc;
		instance = kalphiteKing.getKKInstance();
		int sequentialRotation = kalphiteKing.getSequentialRotation();
		int attackRotation = kalphiteKing.getAttackRotation();
		int attackStyle = npc.getId() - 16697;
		if (Utils.random(5) == 0 && sequentialRotation >= 5) {
			kalphiteKing.switchAttackStyle();
			return 1;
		}
		if (Utils.random(5) == 0 && kalphiteKing.switchTarget())
			return 0;
		if (Utils.random(7) == 0)
			kalphiteKing.activateShield();
		KalphiteKingAttacks attack = getKKAttack(sequentialRotation, attackRotation, attackStyle);
		int specialAttack = getSpecialAttack(sequentialRotation);
		if (specialAttack == -1)
			kalphiteKing.setAttackRotation(
					(attackRotation + 1) > (KalphiteKing.ATTACKS[attackStyle].length - 1) ? 0 : (attackRotation + 1));
		kalphiteKing.setSequentialRotation((sequentialRotation + 1) >= 9 ? 0 : (sequentialRotation + 1));
		if (kalphiteKing.spawnMinions()) {
			return 1;
		}
		sequentialRotation = kalphiteKing.getSequentialRotation();
		attackRotation = kalphiteKing.getAttackRotation();
		KalphiteKingAttacks nextAttack = getKKAttack(sequentialRotation, attackRotation, attackStyle);
		if (nextAttack == KalphiteKingAttacks.BARGE_ATTACK)
			kalphiteKing.setCantFollowUnderCombat(true);
		return attack.sendAttack(kalphiteKing, target);
	}

	public KalphiteKingAttacks getKKAttack(int sequentialRotation, int attackRotation, int attackStyle) {
		int specialAttack = getSpecialAttack(sequentialRotation);
		return specialAttack != -1 ? (KalphiteKing.SPECIAL_ATTACKS[attackStyle][specialAttack])
				: (KalphiteKing.ATTACKS[attackStyle][attackRotation]);
	}

	public int getSpecialAttack(int sequentialRotation) {
		return sequentialRotation == 2 ? 0 : sequentialRotation == 5 ? 1 : sequentialRotation == 8 ? 2 : -1;
	}

	public KalphiteKing getKalphiteKing() {
		return kalphiteKing;
	}

	public KalphiteKingInstance getKKInstance() {
		return instance;
	}

}
