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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain RS3 ability, cast by the Praesul. Sends an attack dealing 20-100% of the regular damage to the
 * main target, after which up to two more attacks will be sent from the target to nearby targets,
 * if there are any targets available within 5 tiles.
 * Lasts 5 ticks, adds +8% adrenaline to the Praesul & has a cooldown of 10 seconds.
 * @author Kris | 30. sept 2017 : 16:59.28
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class Chain extends PraesulAbility {

	public Chain(Player player, PraesulMinion praesul, AngelOfDeath instance) {
		super(player, praesul, instance);
	}
	
	private static final Animation ANIMATION = new Animation(18426);
	private static final Graphics HIT_GFX = new Graphics(3559, 0, 100);
	private static final Graphics SPELL_GFX = new Graphics(3554);

	private int ticks, nextStage;
	private final Map<Player, Integer> targets = new ConcurrentHashMap<Player, Integer>();
	
	@Override
	public void run() {
		if (ticks == 0) {
			praesul.setNextAnimation(ANIMATION);
			praesul.setNextGraphics(SPELL_GFX);
			final NewProjectile projectile = new NewProjectile(praesul, player, 3551, 45, 40, 75, 20);
			instance.sendProjectile(projectile);
			nextStage = projectile.getTime() / 335;
		}
		if (ticks == nextStage) {
			player.applyHit(new Hit(praesul, (int) (MAX * (Utils.random(20, 100) / 100d)), HitLook.MAGIC_DAMAGE));
			for (int i = 0; i < 2; i++) {
				loop : for (Player p : instance.getPlayers()) {
					if (p.withinDistance(player, 6) && !p.isDead() && p != player && !targets.containsKey(player)) {
						final NewProjectile projectile = new NewProjectile(player, p, 3551, 45, 25, 75, 0);
						targets.put(p, projectile.getTime() / 335);
						break loop;
					}
				}
			}
		} 
		if (ticks >= nextStage) {
			targets.forEach((k, v) -> {
				if (v == ticks + nextStage) {
					player.setNextGraphics(HIT_GFX);
					player.applyHit(new Hit(praesul, (int) (MAX * (Utils.random(20, 100) / 100d)), HitLook.MAGIC_DAMAGE));
					targets.remove(k);
				}
			});
			if (targets.isEmpty())
				stop();
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
		return 10;
	}

}
