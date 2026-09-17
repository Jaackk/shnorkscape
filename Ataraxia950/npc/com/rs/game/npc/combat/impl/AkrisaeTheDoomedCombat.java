package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class AkrisaeTheDoomedCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		if (Utils.random(4) == 0) {
			if (target instanceof Player) {
				Player player = ((Player) target);
				player.getPrayer().drainPrayer(Utils.random(200));
				if (!player.getPrayer().isUsingProtectionPrayer())
					npc.setNextForceTalk(new ForceTalk("Ahh, more prayers."));
			}
		}
		npc.setNextAnimation(new Animation(15071));
		delayHit(npc, 0, target, getMagicHit(npc, getRandomMaxHit(npc, 340, NPCCombatDefinitionConstants.MAGE, target)));
		return 6;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 14297, 14298, 14299, 14300 };
	}

}
