package com.rs.game.activities.rots.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.ShadowDrag;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:39.38
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class KarilsCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 18543 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final KarilNPC n = (KarilNPC) npc;
		if (n.getEffect() != null && !(n.getEffect() instanceof ShadowDrag))
			return 1;
		final RiseOfTheSix instance = ((RiseOfTheSixNPC) npc).getInstance();
		int damage = getRandomMaxHit(npc, instance.withinShadowRealm() ? 172 : 86, NPCCombatDefinitionConstants.RANGE, target);
		if (damage != 0 && target instanceof Player && Utils.random(3) == 0) {
			target.setNextGraphics(new Graphics(401, 0, 100));
			final Player targetPlayer = (Player) target;
			int drain = (int) (targetPlayer.getSkills().getLevelForXp(Skills.AGILITY) * 0.2);
			int currentLevel = targetPlayer.getSkills().getLevel(Skills.AGILITY);
			targetPlayer.getSkills().set(Skills.AGILITY, currentLevel < drain ? 0 : currentLevel - drain);
		}
		if (Utils.random(7) == 1 && n.canPerformEffect() && n.getEffect() == null) {
			final RoTSEffect effect = n.generateEffect((Player) target);
			n.setEffect(effect);
			n.setLastEffect(effect);
			return 1;
		}
		npc.setNextAnimation(new Animation(18232));
		delayHit(npc, 2, target, getMeleeHit(npc, damage)); 
		World.sendProjectile(npc, target, 27, 41, 16, 41, 35, 16, 0);
		return 3;
	}

}
