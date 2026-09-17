package com.rs.game.activities.aod.ability.praesul;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * Sends 2 quick attacks of 50-215% damage against the player.
 * Lasts 5 ticks, costs 15% adrenaline and has a cooldown of 20 seconds.
 * @author Kris | 30. sept 2017 : 17:06.26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class WildMagic extends PraesulAbility {

	public WildMagic(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}

	private static final Animation ATTACK = new Animation(18410);
	private int ticks;
	
	@Override
	public void run() {
		if (ticks == 0) {
			praesul.setNextAnimation(ATTACK);
			instance.sendProjectile(new NewProjectile(praesul, player, 2729, 45, 30, 75, 30));
			instance.sendProjectile(new NewProjectile(praesul, player, 2729, 45, 30, 50, 50));
		} else if (ticks < 3) {
			player.applyHit(new Hit(praesul, (int) (MAX * ((Utils.random(50, 215) / 100d))), HitLook.MAGIC_DAMAGE));
		} else if (ticks == 3)
			stop();
		ticks++;
	}

	@Override
	public int getDuration() {
		return 5;
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
