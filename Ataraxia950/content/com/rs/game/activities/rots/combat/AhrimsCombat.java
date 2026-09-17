package com.rs.game.activities.rots.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.npcs.AhrimNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:39.22
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class AhrimsCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 18538, 18539 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final RiseOfTheSix instance = ((RiseOfTheSixNPC) npc).getInstance();
		int damage = getRandomMaxHit(npc, instance.withinShadowRealm() ? 172 : 86, NPCCombatDefinitionConstants.MAGE, target);
		if (damage != 0 && target instanceof Player && Utils.random(3) == 0) {
			target.setNextGraphics(new Graphics(400, 0, 100));
			Player targetPlayer = (Player) target;
			int currentLevel = targetPlayer.getSkills().getLevel(Skills.STRENGTH);
			targetPlayer.getSkills().set(Skills.STRENGTH, currentLevel < 5 ? 0 : currentLevel - 5);
		}
		if (npc.getId() == 18539) {
			npc.setNextAnimation(new Animation(21925));
			delayHit(npc, 2, target, getMagicHit(npc, damage)); 
			World.sendProjectile(npc, target, 374, 50, 18, 50, 35, 0, 0);
			return 3;
		}
		final AhrimNPC ahrims = (AhrimNPC) npc;
		if (Utils.random(7) == 1 && ahrims.canPerformEffect() && ahrims.getEffect() == null) {
			final RoTSEffect effect = ahrims.generateEffect((Player) target);
			ahrims.setEffect(effect);
			ahrims.setLastEffect(effect);
			return 1;
		}
		npc.setNextAnimation(new Animation(18288));
		delayHit(npc, 2, target, getMagicHit(npc, damage)); 
		return 3;
	}

}
