package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.dragons.RuneDragon;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

public class RuneDragonCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Rune dragon" };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		final RuneDragon dragon = (RuneDragon) npc;
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		switch (Utils.random(Utils.isOnRange(npc.getX(), npc.getY(), npc.getSize(), target.getX(), target.getY(),
				target.getSize(), 0) ? 4 : 3)) {
		case 3: // melee
			if (dragon.getPhase() != 2)
				meleeAttack(dragon, target);
			else
				dragonFireAttack(dragon, target);
			break;
		case 2: // magic
			mageAttack(dragon, target);
			break;
		case 1: // range
			rangeAttack(dragon, target);
			break;
		case 0: // dragonfire
			dragonFireAttack(dragon, target);
			break;
		}
		return defs.getAttackDelay();
	}

	/**
	 * Executes the Melee attack.
	 *
	 * @param dragon
	 *            The Dragon.
	 * @param player
	 *            The target.
	 */
	private void meleeAttack(RuneDragon dragon, Entity target) {
		NPCCombatDefinition defs = dragon.getCombatDefinitions();
		dragon.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(dragon, 0, target,
				getMeleeHit(dragon, getRandomMaxHit(dragon, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
	}

	/**
	 * Executes the Range attack.
	 *
	 * @param dragon
	 *            The Dragon.
	 * @param player
	 *            The target.
	 */
	private void rangeAttack(RuneDragon dragon, Entity target) {
		NPCCombatDefinition defs = dragon.getCombatDefinitions();
		dragon.setNextAnimation(new Animation(dragon.getPhase() == 2 ? 26530 : 26524));
		World.sendProjectile(dragon, target, 16, (dragon.getPhase() == 2 ? 56 : 28), 16, 35, 20, 16, 0);
		int damage = getRandomMaxHit(dragon, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
		// small random chance of disabling movement + extra damage
		if (Utils.random(3) == 0) {
			damage += Utils.random(1, 100);
			if (target instanceof Player) {
				((Player) target).sendMessage("You've been prevented from moving.", true);
				target.setFreezeDelay(10);
			}
		}
		delayHit(dragon, 1, target, getRangeHit(dragon, damage));
	}

	/**
	 * Executes the Mage attack.
	 *
	 * @param dragon
	 *            The Dragon.
	 * @param player
	 *            The target.
	 */
	private void mageAttack(RuneDragon dragon, Entity target) {
		NPCCombatDefinition defs = dragon.getCombatDefinitions();
		dragon.setNextAnimation(new Animation(dragon.getPhase() == 2 ? 26530 : 26525));
		World.sendProjectile(dragon, target, 2735, (dragon.getPhase() == 2 ? 56 : 28), 16, 35, 20, 16, 0);
		delayHit(dragon, 1, target,
				getMagicHit(dragon, getRandomMaxHit(dragon, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target)));
	}

	/**
	 * Executes the Dragonfire attack.
	 *
	 * @param dragon
	 *            The Dragon.
	 * @param player
	 *            The target.
	 */
	private void dragonFireAttack(RuneDragon dragon, Entity target) {
		final Player player = target instanceof Player ? (Player) target : null;
		int damage = Utils.random(80, 550 + dragon.getCombatLevel());
		if (target instanceof Player) {
			String message = Combat.getProtectMessage(player);
			if (message != null) {
				player.sendMessage(message, true);
				if (message.contains("fully"))
					damage *= dragon.getPhase() == 2 ? 0.05 : 0;
				else if (message.contains("most"))
					damage *= dragon.getPhase() == 2 ? 0.1 : 0.05;
				else if (message.contains("some"))
					damage *= dragon.getPhase() == 2 ? 0.2 : 0.1;
			}
			if (damage > 0)
				player.sendMessage(
						"You are hit by the dragon's " + (dragon.getPhase() == 2 ? "enhanced " : "") + "fiery breath!",
						true);
		}
		dragon.setNextAnimation(new Animation(dragon.getPhase() == 2 ? 26530 : 14245));
		World.sendProjectile(dragon, target, 393, (dragon.getPhase() == 2 ? 56 : 28), 16, 35, 20, 16, 0);
		delayHit(dragon, 1, target, getRegularHit(dragon, damage));
	}
}