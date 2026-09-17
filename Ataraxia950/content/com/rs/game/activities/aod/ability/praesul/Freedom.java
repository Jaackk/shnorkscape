package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.Animation;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;

/**
 * Freedom RS3 Ability. Frees the Praesul from all kinds of freezes as well as sets it immune to freeze
 * for the next 6 seconds.
 * Lasts 4 ticks, adds +8% adrenaline to the Praesul and has a cooldown of 30 seconds.
 * @author Kris | 30. sept 2017 : 17:02.19
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Freedom extends PraesulAbility {

	public Freedom(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}

	private static final Animation ANIMATION = new Animation(18070);
	
	private int ticks;
	
	@Override
	public void run() {
		if (ticks == 0) {
		praesul.setNextAnimation(ANIMATION);
		praesul.setFreezeDelay(0);
		praesul.setFreezeImmune(true);
		} else if (ticks == 10) {
			praesul.setFreezeImmune(false);
			stop();
			return;
		}
		ticks++;
	}

	@Override
	public int getDuration() {
		return 4;
	}

	@Override
	public int getAdrenaline() {
		return 8;
	}

	@Override
	public int getCooldown() {
		return 30;
	}

}
