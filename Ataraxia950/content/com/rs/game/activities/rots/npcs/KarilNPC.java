package com.rs.game.activities.rots.npcs;

import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Bombard;
import com.rs.game.activities.rots.effects.LightningConductor;
import com.rs.game.activities.rots.effects.PortalDash;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.Throw;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.25
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class KarilNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;

	public KarilNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	private RoTSEffect lastEffect;
	private boolean cantSetGraphics;
	
	public void setCantSetGraphics(boolean val) {
		this.cantSetGraphics = val;
	}
	
	public void setLastEffect(RoTSEffect lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		if (getEffect() != null && getEffect() instanceof LightningConductor)
			hit.setDamage(0);
		super.handleIngoingHit(hit);
	}
	
	@Override
	public double getRangePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public void setNextGraphics(Graphics nextGraphics) {
		if (cantSetGraphics)
			return;
		super.setNextGraphics(nextGraphics);
	}

	public RoTSEffect generateNextEffect(Player target) {
		if (lastEffect == null) {
			int random = Utils.random(3);
			return random == 0 ?  new Bombard(1, this, null) : random == 1 ? new PortalDash(1, this, target) : new LightningConductor(1, this, null);
		} else return (lastEffect instanceof LightningConductor ? new Bombard(1, this, null) 
				: lastEffect instanceof Bombard ? new PortalDash(1, this, target) 
						: new LightningConductor(1, this, null));
	}

	@Override
	public RoTSEffect generateEffect(Player target) {
		if (Utils.random(5) == 0)
			return new Throw(5, this, null);
		if (lastEffect == null) {
			final int randomEffect = Utils.random(3);
			switch(randomEffect) {
			case 0:
				return new LightningConductor(15, this, null);
			case 1:
				return new Bombard(20, this, null);
			default:
				return new PortalDash(30, this, null);
			}
		}
		if (lastEffect instanceof PortalDash)
			return new LightningConductor(15, this, null);
		else if (lastEffect instanceof LightningConductor)
			return new Bombard(20, this, null);
		return new PortalDash(30, this, null);
	}
	
}
