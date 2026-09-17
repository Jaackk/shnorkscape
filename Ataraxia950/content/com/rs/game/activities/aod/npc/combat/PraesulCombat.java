package com.rs.game.activities.aod.npc.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.activities.aod.ability.praesul.PraesulAbility;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * A class handling the Praesul combat. Praesul will constantly attack with different abilities.
 * @author Kris | 30. sept 2017 : 17:24.44
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class PraesulCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final PraesulMinion minion = (PraesulMinion) npc;
		if (target instanceof Player) {
			try {
				final PraesulAbility ability = minion.generateNextAbility((Player) target);
				if (minion.canUseAbility(ability.getClass(), ability.getCooldown())) {
					minion.setAdrenaline(minion.getAdrenaline() + ability.getAdrenaline());
					minion.useAbility(ability);
					return ability.getDuration();
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				Logger.getGlobal().catching(e);
			}
		}
		minion.setAdrenaline(minion.getAdrenaline() + 3);
		npc.setNextAnimation(new Animation(18355));
		World.sendProjectile(new NewProjectile(npc, target, 2730, 35, 30, 35, 5, 100, 0));
		delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 111, NPCCombatDefinitionConstants.MAGE, target)));
		return 4;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 24010, 24011, 24012, 24013 };
	}

}
