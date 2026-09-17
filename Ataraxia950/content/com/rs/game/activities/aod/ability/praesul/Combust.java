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
 * Combust RS3 ability, cast by the Praesul. Sends 5 stacks of 100-188% damage to the target. Damage is
 * doubled if the target has moved its location from the original location.
 * Lasts five ticks, adds +8% adrenaline to the Praesul & has a 15 second cooldown.
 * @author Kris | 30. sept 2017 : 17:00.39
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Combust extends PraesulAbility {

	public Combust(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}
	
	private static final Animation ANIMATION = new Animation(18452);
	private static final Graphics SPELL_GFX = new Graphics(3569);
	private static final Graphics HIT_GFX = new Graphics(3574);
	
	private int ticks, hash;

	@Override
	public void run() {
		if (ticks == 0) {
			hash = player.getTileHash();
			praesul.setNextAnimation(ANIMATION);
			praesul.setNextGraphics(SPELL_GFX);
		} else if (ticks % 2 == 0 && ticks < 11) {
			player.setNextGraphics(HIT_GFX);
			player.applyHit(new Hit(praesul, (int) (MAX * (Utils.random(100, 188) / (player.getTileHash() != hash ? 50d : 100d))), HitLook.MAGIC_DAMAGE));
		} else if (ticks == 11) {
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
		return 8;
	}

	@Override
	public int getCooldown() {
		return 15;
	}

}
