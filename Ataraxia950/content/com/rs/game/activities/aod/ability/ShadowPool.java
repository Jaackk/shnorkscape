package com.rs.game.activities.aod.ability;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.UnstableSmoke;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * AoD sends a black hole-like graphic to a certain point around the center of
 * the instance. Once a player enters the graphic, an unstable smoke object will
 * appear. If no one consumes the smoke within 6 ticks of its appearance, the
 * whole team will receive a punishing unblockable attack of 3750 damage total,
 * spread between all members of the team. If however a player does infact
 * consume the smoke, all players within the smoke graphic will receive damage
 * for 1400 damage spreaed between themselves.
 * 
 * @author Kris | 30. sept 2017 : 16:54.00
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 *      profile</a>}
 */
public final class ShadowPool extends AoDAbility {

	public ShadowPool(final AoDNex nex, final AngelOfDeath instance) {
		super(nex, instance);
	}

	private static final Graphics CONSUMPTION = new Graphics(6531);
	private static final Animation ATTACK = new Animation(17413);
	private int ticks;
	private final NewProjectile projectile = new NewProjectile(nex, instance.getWorldTile(Utils.random(2840, 2857), Utils.random(1816, 1833)), 6523, 30, 5, 10, 0);
	private final int nextStep = (projectile.getTime() / 335) + 3;
	private UnstableSmoke smoke;

	@Override
	public void run() {
		if (ticks == 0) {
			nex.sendMessage("Even I can not control the element of smoke... Let us see if you can.");
			nex.resetCombat();
			nex.setCannotMove(true);
		} else if (ticks == 2) {
			nex.setNextFaceWorldTile(projectile.getTo());
			nex.setNextAnimation(ATTACK);
		} else if (ticks == 3) {
			instance.getPlayers().forEach(p -> p.sendMessage("<col=ff0000>Nex casts a thick black smoke towards the centre of the arena."));
			instance.sendProjectile(projectile);
			nex.setCannotMove(false);
			nex.setTarget(nex.getTargetedPlayer());
		}
		if (ticks == nextStep) {
			instance.sendGraphics(CONSUMPTION, projectile.getTo());
		}
		if (smoke == null && ticks < nextStep + 10 && ticks >= nextStep) {
			for (final Player p : instance.getPlayers()) {
				if (p.withinDistance(projectile.getTo(), 2)) {
					smoke = new UnstableSmoke(projectile.getTo(), instance);
					break;
				}
			}
		} else if (ticks == nextStep + 10) {
			if (smoke == null || !smoke.isCantInteract()) {
				instance.sendGraphics(AngelOfDeath.RESET_GFX, projectile.getTo());
				if (smoke != null) {
					smoke.finish();
				}
				if (instance.getPlayers().size() > 0) {
					final int damage = 750 / instance.getPlayers().size();
					instance.getPlayers().forEach(p -> p.applyHit(new Hit(null, damage, HitLook.REGULAR_DAMAGE)));
				}
			}
			stop();
			return;
		}
		ticks++;
	}

}
