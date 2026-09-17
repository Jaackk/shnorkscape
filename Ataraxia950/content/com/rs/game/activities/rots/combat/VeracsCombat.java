package com.rs.game.activities.rots.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.activities.rots.npcs.VeracNPC;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:39.48
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class VeracsCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 18545 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final RiseOfTheSix instance = ((RiseOfTheSixNPC) npc).getInstance();
		final VeracNPC n = (VeracNPC) npc;
		int damage = getRandomMaxHit(npc, instance.withinShadowRealm() ? 260 : 130, NPCCombatDefinitionConstants.MELEE, target);
		if (Utils.random(3) == 0)
			damage = instance.withinShadowRealm() ? Utils.random(200, 260) : Utils.random(100, 130);
		if (Utils.random(7) == 1 && n.canPerformEffect() && n.getEffect() == null) {
			final RoTSEffect effect = n.generateEffect((Player) target);
			n.setEffect(effect);
			return 1;
		}
		npc.setNextAnimation(new Animation(18222));
		delayHit(npc, 0, target, getMeleeHit(npc, damage)); 
		return 5;
	}

}
