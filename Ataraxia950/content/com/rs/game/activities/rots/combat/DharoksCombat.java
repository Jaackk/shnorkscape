package com.rs.game.activities.rots.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.ShadowDrag;
import com.rs.game.activities.rots.npcs.DharokNPC;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:39.26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class DharoksCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 18540 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final DharokNPC n = (DharokNPC) npc;
		if (n.getEffect() != null && !(n.getEffect() instanceof ShadowDrag))
			return 1;
		if (Utils.random(7) == 1 && n.canPerformEffect() && n.getEffect() == null) {
			final RoTSEffect effect = n.generateEffect((Player) target);
			n.setEffect(effect);
			n.setLastEffect(effect);
			return 1;
		}
		final RiseOfTheSix instance = n.getInstance();
		npc.setNextAnimation(new Animation(18236));
		int damage = 130;
		if (npc.getHitpoints() < 50000)
			damage *= (npc.getMaxHitpoints() - npc.getHitpoints()) / 1600;
		if (n.getGreatestAxeDamage() == 0)
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, instance.withinShadowRealm() ? damage * 2 : damage, NPCCombatDefinitionConstants.MELEE, target)));
		else {
			delayHit(npc, 0, target, getMeleeHit(npc, instance.withinShadowRealm() ? n.getGreatestAxeDamage() * 2 : n.getGreatestAxeDamage()));
			n.resetDamage();
		}
		return 5;
	}

}
