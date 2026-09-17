package com.rs.game.npc.combat.impl.gwd2.twins;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.gwd2.twinfuries.Avaryss;
import com.rs.game.npc.gwd2.twinfuries.ChannelledBomb;
import com.rs.game.npc.gwd2.twinfuries.WallCharge;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 14, 2017
 */
public class AvaryssCombat extends CombatScript {


	@Override
	public Object[] getKeys() {
		return new Object[] { 22453 };
	}
	@Override
	public int attack(NPC npc, Entity target) {
		final Avaryss avaryss = (Avaryss) npc;
		if (avaryss.getInstance().getPhase() == 0 && avaryss.getInstance().getSpecialDelay() < Utils.currentTimeMillis()) {
			avaryss.getInstance().setSpecialDelay(Utils.currentTimeMillis() + Utils.random(30000, 35000));
			new WallCharge(npc, target).effect();
			avaryss.getInstance().nextPhase();
			return 25;
		} else if (avaryss.getInstance().getPhase() == 2 && avaryss.getInstance().getSpecialDelay() < Utils.currentTimeMillis()) {
			avaryss.getInstance().setSpecialDelay(Utils.currentTimeMillis() + Utils.random(20000, 25000));
			new ChannelledBomb(npc, target).effect();
			avaryss.getInstance().setPhase(0);
			return 15;
		}
		npc.setNextAnimation(new Animation(28239));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, avaryss.getInstance().isHardMode() ? Utils.random(100, 200) : Utils.random(70, 150), NPCCombatDefinitionConstants.MELEE, target)));
		return 4;
	}

}