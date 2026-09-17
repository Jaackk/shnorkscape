package com.rs.game.activities.aod.npc.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.NewProjectile;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.ability.AoDAbility;
import com.rs.game.activities.aod.ability.Freeze;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * A class that handles AoD's combat. By default, AoD will attack with either AoE melee attack
 * or an instance-covering magic attack. However, it can also use abilities available to it.
 * @author Kris | 30. sept 2017 : 17:23.05
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDNexCombat extends CombatScript {

	private static final Animation MELEE_ATTACK = new Animation(17453);
	private static final Animation MAGIC_ATTACK = new Animation(17413);
	
	@Override
	public int attack(NPC npc, Entity target) {
		final AoDNex nex = (AoDNex) npc;
		if (nex.getSpecialDelay() < Utils.currentTimeMillis()) {
			final AoDAbility ability = nex.generateNextAbility();
			if (ability != null) {
				nex.useAbility(ability);
				if (ability instanceof Freeze) {
					nex.setNextAnimation(MAGIC_ATTACK);
					nex.getInstance().getPlayers().forEach(p -> {
						final NewProjectile projectile = new NewProjectile(nex.getMiddleWorldTile(), p, 3371, 40, 15, 15);
						nex.getInstance().sendProjectile(projectile);
						final int hit = getRandomMaxHit(nex, (int) (171 * nex.getEnrageMultiplier(p)), NPCCombatDefinitionConstants.MAGE, p);
						if (nex.heal())
							nex.heal(hit);
						delayHit(nex, projectile.getTime() / 191, p, getMagicHit(nex, hit));
					});
				}
				return 5;
			}
		}
		final boolean withinMeleeDistance = isWithinMeleeDistance(nex, target);
		final int attack = nex.getInstance().getStage() == 4 ? 2 : withinMeleeDistance ? Utils.random(3) : 2;
		if (nex.getInstance().getStage() == 4) {
			if (nex.isForceFollowClose())
				nex.setForceFollowClose(false);
		} else
			nex.setForceFollowClose(Utils.random(3) == 0);
		final int damage = (int) (181 * (target instanceof Player ? nex.getEnrageMultiplier((Player) target) : 1));
		switch(attack) {
		case 0:
		case 1:
			nex.setNextAnimation(MELEE_ATTACK);
			delayHit(nex, 0, target, getMeleeHit(nex, getRandomMaxHit(nex, damage, NPCCombatDefinitionConstants.MELEE, target)));
			if (target instanceof Player)
				nex.addEnragePlayer((Player) target);
			nex.getInstance().getPlayers().forEach(p -> {
				if (p == target) {
					return;
				}

				//wrong abs
				if (p.withinDistance(new WorldTile(Math.abs((nex.getX() + target.getX()) / 2), Math.abs((nex.getY() + target.getX()) / 2), nex.getPlane()), 2)) {
					final int pDamage = (int) (161 * nex.getEnrageMultiplier(p));
					delayHit(nex, 0, p, getMeleeHit(nex, getRandomMaxHit(nex, pDamage, NPCCombatDefinitionConstants.MELEE, p)));
				}
			});
			break;
			default:
				nex.setNextAnimation(MAGIC_ATTACK);
				nex.getInstance().getPlayers().forEach(p -> {
					final NewProjectile projectile = new NewProjectile(nex.getMiddleWorldTile(), p, 3371, 40, 15, 15);
					nex.getInstance().sendProjectile(projectile);
					final int hit = getRandomMaxHit(nex, (int) (161 * nex.getEnrageMultiplier(p)), NPCCombatDefinitionConstants.MAGE, p);
					if (nex.heal())
						nex.heal(hit);
					delayHit(nex, projectile.getTime() / 161, p, getMagicHit(nex, hit));
				});
				break;
		}
		return 4;
	}
	
	/**
	 * A boolean determining whether the target is within melee distance of AoD.
	 * @param npc AoD.
	 * @param target targeted entity.
	 * @return whether within melee distance.
	 */
	public boolean isWithinMeleeDistance(final AoDNex npc, final Entity target) {
		final int distanceX = target.getX() - npc.getX();
		final int distanceY = target.getY() - npc.getY();
		final int size = npc.getDefinitions().size;
		return !(distanceX > size + 1 || distanceX < -1 || distanceY > size + 1 || distanceY < -1);
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 24004 };
	}

}
