package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class EnergyTransfer implements PlayerSpell {

	@Override
	public int getId() {
		return 27;
	}

	@Override
	public int getLevel() {
		return 91;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 3), new Item(LAW_RUNE, 2), new Item(NATURE_RUNE, 1) };
	}

	@Override
	public int getDelay() {
		return 6000;
	}

	@Override
	public boolean spellEffect(Player player, Player target) {
		if (target == null || !target.isActive() || target.hasFinished() || !target.isAcceptingAid()) {
			player.sendMessage("The targeted player is not accepting aid.");
			return false;
		} else if (!player.isAtMultiArea()) {
			player.sendMessage("You need to be in a multi area to cast this spell.");
			return true;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (player == null || !player.isActive() || player.hasFinished() || target == null
						|| target.hasFinished() || !target.isActive()) {
					this.stop();
					return;
				}
				if (loop == 0) {
					player.faceEntity(target);
					player.getSkills().addXp(Skills.MAGIC, 100);
					player.setNextAnimation(new Animation(4411));
					final int toHit = player.getHitpoints() > 10 ? 10 : player.getHitpoints() - 1;
					player.sendMessage("You transfer your energy to " + target.getDisplayName() + ".");
					player.applyHit(new Hit(player, toHit > 1 ? Utils.random(toHit) : 0, HitLook.REGULAR_DAMAGE));
				} else if (loop == 1) {
					target.setNextGraphics(new Graphics(734, 0, 100));
					final int p1 = player.getCombatDefinitions().getSpecialAttackPercentage();
					final int p2 = target.getCombatDefinitions().getSpecialAttackPercentage();
					if (p2 != 100) {
						player.getCombatDefinitions().setSpecialAttack(0);
						target.getCombatDefinitions().setSpecialAttack(p1 + p2 > 100 ? 100 : p1 + p2 <= 0 ? 0 : p1 + p2);
					}
					int r1 = player.getRunEnergy();
					int r2 = target.getRunEnergy();
					if (r2 != 100) {
						target.setRunEnergy(r1 + r2 <= 0 ? 0 : r1 + r2 > 100 ? 100 : r1 + r2);
						player.setRunEnergy(player.getRunEnergy() - r1 <= 0 ? 0 : player.getRunEnergy() > 100 ? 100 : player.getRunEnergy() - r1);
					}
					target.sendMessage(player.getDisplayName() + " has transferred their energy to you.");
					this.stop();
				}
				loop++;
			}
		}, 0, 3);
		return true;
	}
}
