package com.rs.game.npc.gwd2.twinfuries;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.TwinFuriesInstance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;

/**
 * @author Tom
 * @date April 14, 2017
 */

public class Nymora extends NPC {

	private static final long serialVersionUID = 1739739909683329194L;
	private final TwinFuriesInstance instance;
	private boolean finished;

	public Nymora(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, TwinFuriesInstance instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceTargetDistance(50);
		setRun(true);
		setIntelligentRouteFinder(true);
		setForceAgressive(true);
		setForceMultiArea(true);
		this.instance = instance;
		instance.setPhase(0);
		setFreezeDelay(1);
	}
	
	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}
	
	@Override
	public void spawn() {
		super.spawn();
		setNextAnimation(new Animation(28242));
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public int getCapDamage() {
		return 1250;
	}
	
	@Override
	public void handleIngoingHit(Hit hit) {
		if (Settings.USE_DAMAGE_CAP) {
			if (getCapDamage() != -1 && hit.getDamage() > getCapDamage())
				hit.setDamage(getCapDamage());
		}
		if (instance.isChannelling())
			hit.setDamage(hit.getDamage() * 2);
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
			HeartOfGielinor.refreshHealth(instance, getHitpoints() - hit.getDamage(), getMaxHitpoints());
			return;
		}
		handlePrayers(hit);
		final int health = getHitpoints() - hit.getDamage();
		getInstance().getAvaryss().setHitpoints(health);
		HeartOfGielinor.refreshHealth(instance, health, getMaxHitpoints());
	}
	
	@Override
	public void sendDeath(final Entity source) {
		if (finished)
			return;
		finished = true;
		setHitpoints(0);
		super.sendDeath(source);
		getInstance().getAvaryss().sendDeath(source);
	}
	
	public TwinFuriesInstance getInstance(){
		return instance;
	}

}
