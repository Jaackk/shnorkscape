package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 19, 2017
 */

@SuppressWarnings("serial")
public class RipperDemon extends NPC {

	private int phase;
	private WorldTile hitTile;

	public RipperDemon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setPhase(0);
		setIntelligentRouteFinder(true);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		for (Entity targets : getPossibleTargets()) {
            setForceAgressive(targets.getHitpoints() < (targets.getMaxHitpoints() / 2));
		}
	}

	@Override
	public void sendDeath(final Entity source) {
		super.sendDeath(source);
		Player killer = getMostDamageReceivedSourcePlayer();
		hitTile = new WorldTile(killer.getX(), killer.getY(), killer.getPlane());
		setNextGraphics(new Graphics(5915));
		setNextAnimation(new Animation(27767));
		for (Player players : World.getPlayers()) {
			WorldTasksManager.schedule(new WorldTask() {
				int ticks = 0;

				@Override
				public void run() {
					if (ticks >= 4)
						stop();
					if (players.withinDistance(hitTile, 1)) {
						if (getNextAnimation() != new Animation(27767) && isDead()) {
							if (ticks >= 1 && ticks <= 4)
								players.applyHit(new Hit(RipperDemon.this, Utils.random(75, 125), HitLook.REGULAR_DAMAGE));
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

	public void setPhase(int phase) {
		this.phase = phase;
	}

}
