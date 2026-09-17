package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * Omnipower RS3 ability, cast by the Praesul. Sends an elemental attack against the target
 * resulting in 2 to 5 times the damage of the original attack. 
 * Attack lasts 6 ticks, costs 100% adrenaline and has a cooldown of 20 seconds.
 * @author Kris | 30. sept 2017 : 17:05.02
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Omnipower extends PraesulAbility {

	private static final Animation ATTACK = new Animation(18370);
	private static final Graphics SPELL_GFX = new Graphics(3564, 0, 60);
	private static final Graphics HIT_GFX = new Graphics(3566, 0, 50);
	
	public Omnipower(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}
	
	private int ticks, nextStep;

	@Override
	public void run() {
		if (ticks == 0) {
			praesul.setNextAnimation(ATTACK);
			praesul.setNextGraphics(SPELL_GFX);
			final NewProjectile projectile = new NewProjectile(praesul, player, 3565, 25, 15, 50, 75);
			instance.sendProjectile(projectile);
			nextStep = projectile.getTime() / 335;
		}
		if (ticks == nextStep) {
			player.setNextGraphics(HIT_GFX);
			player.applyHit(new Hit(praesul, Utils.random(MAX * Utils.random(2, 5)), HitLook.MAGIC_DAMAGE));
			stop();
			return;
		}
		ticks++;
	}

	@Override
	public int getDuration() {
		return 6;
	}

	@Override
	public int getAdrenaline() {
		return -100;
	}

	@Override
	public int getCooldown() {
		return 20;
	}

}
