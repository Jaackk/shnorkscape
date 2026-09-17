package com.rs.game.player.actions.slayer.elite;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. okt 2018 : 15:04:41
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class SlasherDemon extends EliteNPC {

	private static final long serialVersionUID = -6797613623096302277L;

	protected SlasherDemon(final Player owner, final int id, final WorldTile tile) {
		super(owner, id, tile);
		setPhase(0);
		setIntelligentRouteFinder(true);
	}

	private int phase;
	private WorldTile hitTile;

	@Override
	protected String getVaryingName() {
		return "Slasher demon";
	}
	
	@Override
	public void processNPC() {
		super.processNPC();
		for (final Entity targets : getPossibleTargets()) {
            setForceAgressive(targets.getHitpoints() < (targets.getMaxHitpoints() / 2));
		}
	}

	@Override
	public void sendDeath(final Entity source) {
		super.sendDeath(source);
		final Player killer = getMostDamageReceivedSourcePlayer();
		hitTile = new WorldTile(killer.getX(), killer.getY(), killer.getPlane());
		setNextGraphics(new Graphics(5915));
		setNextAnimation(new Animation(27767));
		for (final Player players : World.getPlayers()) {
			WorldTasksManager.schedule(new WorldTask() {
				int ticks = 0;

				@Override
				public void run() {
					if (ticks >= 4) {
						stop();
					}
					if (players.withinDistance(hitTile, 1)) {
						if (getNextAnimation() != new Animation(27767) && isDead()) {
							if (ticks >= 1 && ticks <= 4) {
								players.applyHit(new Hit(SlasherDemon.this, Utils.random(75, 125), HitLook.REGULAR_DAMAGE));
							}
						}
					}
					ticks++;
				}
			}, 0, 1);
		}
	}

	@Override
	public void spawn() {
		super.spawn();
		setNextAnimation(new Animation(27772));
		setRun(false);
		setForceAgressive(false);
	}

	public int getPhase() {
		return phase;
	}

	public void nextPhase() {
		phase++;
	}

	public void setPhase(final int phase) {
		this.phase = phase;
	}
	
}
