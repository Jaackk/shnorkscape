package com.rs.game.activities.rots.npcs;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.GreatestAxe;
import com.rs.game.activities.rots.effects.Hurricane;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.WallSlam;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.15
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class DharokNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;

	public DharokNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	public void setLastEffect(RoTSEffect lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	private int greatestAxeDamage;
	private RoTSEffect lastEffect;
	
	
	public int getGreatestAxeDamage() {
		return greatestAxeDamage;
	}
	
	public void resetDamage() {
		this.greatestAxeDamage = 0;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		if (effect != null && effect instanceof GreatestAxe) {
			this.greatestAxeDamage += hit.getDamage();
			return;
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public RoTSEffect generateEffect(Player target) {
		if (lastEffect == null) {
			final int randomEffect = Utils.random(3);
			switch(randomEffect) {
			case 0:
				return new Hurricane(10, this, null);
			case 1:
				return new GreatestAxe(14, this, target);
			default:
				return new WallSlam(10, this, null);
			}
		}
		if (lastEffect instanceof WallSlam)
			return new Hurricane(10, this, null);
		else if (lastEffect instanceof Hurricane) {
			return new GreatestAxe(14, this, null);
		}
		return new WallSlam(10, this, null);
	}

}
