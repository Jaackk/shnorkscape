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
 * Wrack RS3 ability, cast by the Praesul. Sends an attack of 18.8% - 94% damage to the target. 
 * The damage is doubled if the target is frozen at the time of the attack.
 * Lasts 4 ticks, adds 8% adrenaline to the Praesul and has a cooldown of only 3 seconds.
 * @author Kris | 30. sept 2017 : 17:07.31
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Wrack extends PraesulAbility {

	public Wrack(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}
	
	private static final Animation ANIMATION = new Animation(18379);
	private static final Graphics HIT_GFX = new Graphics(3536);
	private static final Graphics SPELL_GFX = new Graphics(3531);
	
	private int ticks; 
	@Override
	public void run() {
		if (ticks == 0) {
		praesul.setNextAnimation(ANIMATION);
		praesul.setNextGraphics(SPELL_GFX);
		player.setNextGraphics(HIT_GFX);
		} else if (ticks == 1) {
			player.applyHit(new Hit(praesul, (int) (MAX * ((player.isFrozen() ? Utils.random(376, 1880) : Utils.random(188, 940)) / 1000d)), HitLook.MAGIC_DAMAGE));
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
		return 3;
	}

}
