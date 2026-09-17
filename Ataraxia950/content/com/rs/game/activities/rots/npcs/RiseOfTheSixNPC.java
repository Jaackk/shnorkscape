package com.rs.game.activities.rots.npcs;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Healing;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.29
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public abstract class RiseOfTheSixNPC extends NPC {

	private static final long serialVersionUID = 1734800058592701808L;

	public RiseOfTheSixNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.instance = instance;
		setRun(true);
		setForceMultiArea(true);
		setForceTargetDistance(30);
		setCantDoDefenceEmote(true);
		setFreezeImmune(true);
	}
	
	protected RoTSEffect effect;
	protected RiseOfTheSix instance;
	private Healing healingTask;
	public abstract RoTSEffect generateEffect(final Player target);
	private long specialDelay;
	
	public void refreshSpecialDelay() {
		this.specialDelay = Utils.currentTimeMillis() + 12000;
	}
	
	public boolean canPerformEffect() {
		return specialDelay < Utils.currentTimeMillis();
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public int getCapDamage() {
		return 1000;
	}
	
	public RiseOfTheSix getInstance() {
		return instance;
	}

	public void setEffect(RoTSEffect effect) {
		this.effect = effect;
		effect.start();
	}
	
	public void finishEffect() {
		this.effect = null;
	}
	
	public RoTSEffect getEffect() {
		return effect;
	}
	
	public Healing getHealing() {
		return healingTask;
	}
	
	@Override
	public void sendDeath(Entity source) {
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		boolean complete = true;
		for (NPC brothers : instance.getWights()) {
			if (!brothers.isDead()) 
				complete = false;
		}
		if (!complete)
			WorldTasksManager.schedule(healingTask = new Healing(this, true), 0, 0);
		else {
			if (instance.withinShadowRealm()) {
				WorldTasksManager.schedule(new WorldTask() {
					private int ticks;

					@Override
					public void run() {
						if (ticks == 0) {
							instance.getPlayers().forEach(p -> {
								p.lock(2);
								p.setNextAnimation(new Animation(21917));
								p.setNextGraphics(new Graphics(4413));
							});
						} else if (ticks == 1) {
							instance.getPlayers().forEach(p -> {
								p.setNextAnimation(new Animation(21915));
								p.setNextGraphics(new Graphics(4413));
								p.setNextWorldTile(instance.convertToRegularRealm(p));
							});
						} else if (ticks == 3) {
							instance.engulfWights(getName());
							stop();
							return;
						}
						ticks++;
					}
				}, 0, 0);
			} else
				instance.engulfWights(getName());
			for (NPC brothers : instance.getWights()) 
				brothers.finish();
			if (instance.withinShadowRealm())
				instance.setShadowRealm(false);
		}
	}
	
}
