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

public class HealOther implements PlayerSpell {

	@Override
	public int getId() {
		return 14866;
	}

	@Override
	public int getLevel() {
		return 92;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 3), new Item(LAW_RUNE, 3), new Item(BLOOD_RUNE, 1) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player, Player target) {
		if (target == null || !target.isActive() || target.hasFinished() || !target.isAcceptingAid()) {
			player.sendMessage("The targeted player is not accepting aid.");
			return false;
		}
		if (player.getHitpoints() <= player.getMaxHitpoints() * 0.11) {
			player.getPackets().sendGameMessage("You need atleast 11% of your hitpoints to use this spell.");
			return false;
		}
		int heal = target.getMaxHitpoints() - target.getHitpoints();
		if (heal <= 0) {
		    player.getPackets().sendGameMessage("Your target already has full hp.");
		    return false;
		}
		if (heal > ((double)player.getHitpoints() * 0.75))
		    heal = (int)((double)player.getHitpoints() * 0.75);
		final int h = heal;
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
					player.faceEntity((target));
					player.getSkills().addXp(Skills.MAGIC, 101);
					player.setNextAnimation(new Animation(4411));
					player.applyHit(new Hit(player, h, HitLook.REGULAR_DAMAGE));
				} else if (loop == 1) {
					target.setNextGraphics(new Graphics(736, 0, 100));
					target.heal(h);
					this.stop();
				}
				loop++;
			}
		}, 0, 3);
		return true;
	}

}
