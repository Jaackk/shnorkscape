package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * Handles the Asphyxiate RS3 ability for the Praesul. Deals 4 stacks of 37.6% - 188% damage with two 
 * tick intervals. Freezes the target for 3.6 seconds. 
 * Lasts 12 ticks, costs 15% adrenaline and has a cooldown of 20 seconds.
 * @author Kris | 30. sept 2017 : 16:57.54
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Asphyxiate extends PraesulAbility {

	public Asphyxiate(final Player player, final PraesulMinion praesul, final AngelOfDeath instance) {
		super(player, praesul, instance);
	}
	
	private static final Animation ATTACK = new Animation(18394);
	private static final Graphics GFX = new Graphics(3542, 0, 45);

	private int ticks;
	@Override
	public void run() {
		if (ticks == 0) {
			praesul.setNextAnimation(ATTACK);
			praesul.setNextGraphics(GFX);
			//player.addFreezeDelay(3600);
		} else if (ticks < 9 && ticks % 2 != 0) {
			player.applyHit(new Hit(praesul, (int) (MAX * (Utils.random(376, 1880) / 1000d)), HitLook.MAGIC_DAMAGE));
		} else if (ticks == 9) {
			stop();
		}
		ticks++;
	}

	@Override
	public int getDuration() {
		return 12;
	}

	@Override
	public int getAdrenaline() {
		return -15;
	}

	@Override
	public int getCooldown() {
		return 20;
	}

}
