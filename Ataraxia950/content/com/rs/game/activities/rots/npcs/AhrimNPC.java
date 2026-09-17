package com.rs.game.activities.rots.npcs;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Flight;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.ShadowPits;
import com.rs.game.activities.rots.effects.Throw;
import com.rs.game.activities.rots.effects.TurretOfFire;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.10
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class AhrimNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;

	public AhrimNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	private RoTSEffect lastEffect;
	private boolean initialFlight;
	private long lastPit;
	private boolean cantSetGraphics;
	
	public void setCantSetGraphics(boolean val) {
		this.cantSetGraphics = val;
	}
	
	public long getLastPit() {
		return lastPit;
	}
	
	public void setLastPit(long pit) {
		this.lastPit = pit;
	}
	
	public void setLastEffect(RoTSEffect lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	@Override
	public void setNextGraphics(Graphics nextGraphics) {
		if (cantSetGraphics)
			return;
		super.setNextGraphics(nextGraphics);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (getEffect() != null && getEffect() instanceof TurretOfFire)
			hit.setDamage(0);
		else if (getEffect() != null && getEffect() instanceof Flight) {
			if (hit.getLook() == HitLook.MAGIC_DAMAGE)
				hit.setDamage(hit.getDamage() / 2);
			else if (hit.getLook() == HitLook.MELEE_DAMAGE)
				hit.setDamage(0);
		} else if (getEffect() == null && !initialFlight && getHitpoints() <= (getMaxHitpoints() - 500)) {
			getCombat().addCombatDelay(5);
			setEffect(new Flight(200, this, null));
			initialFlight = true;
		}
		super.handleIngoingHit(hit);
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.5;
	}

	@Override
	public RoTSEffect generateEffect(Player target) {
		if (lastEffect == null) {
			final int randomEffect = Utils.random(3);
			switch(randomEffect) {
			case 0:
				return new TurretOfFire(15, this, null);
			case 1:
				return new Throw(5, this, null);
			default:
				return new ShadowPits(10, this, null);
			}
		}
		if (getHitpoints() < 4500 && Utils.random(5) == 0)
			return new Flight(Utils.random(100, 200), this, null);
		if (lastEffect instanceof ShadowPits)
			return new TurretOfFire(15, this, null);
		else if (lastEffect instanceof TurretOfFire)
			return new Throw(5, this, null);
		return new ShadowPits(10, this, null);
	}

}
