package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;

/**
 * An ultimate RS3 ability, cast by the Praesul. Temporarily boosts the Praesul damage by 62.5%. Lasts 15 seconds.
 * Attack lasts 4 ticks, costs 100% arenaline and has a cooldown of one minute.
 * @author Kris | 30. sept 2017 : 17:03.29
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Metamorphosis extends PraesulAbility {

	public Metamorphosis(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}

	private static final Animation ANIMATION = new Animation(18418);
	private static final Graphics GFX = new Graphics(3550);
	
	private int ticks;
	
	@Override
	public void run() {
		if (ticks == 0) {
		praesul.setNextAnimation(ANIMATION);
		praesul.setNextGraphics(GFX);
		praesul.setHitBoost(1.625);
		} else if (ticks == 26) {
			praesul.setHitBoost(1);
			stop();
			return;
		}
		ticks++;
	}

	@Override
	public int getDuration() {
		return 5;
	}

	@Override
	public int getAdrenaline() {
		return -100;
	}

	@Override
	public int getCooldown() {
		return 60;
	}

}
