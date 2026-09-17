package com.rs.game.activities.rots.npcs;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Hurricane;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.WallSlam;
import com.rs.game.activities.rots.effects.Whack;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:36.35
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class ToragNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;

	public ToragNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	private int damageTaken;
	private RoTSEffect lastEffect;
	
	public void setLastEffect(RoTSEffect lastEffect) {
		this.lastEffect = lastEffect;
	}
	
	public int getDamageTaken() {
		return damageTaken;
	}
	
	public void resetDamageTaken() {
		this.damageTaken = 0;
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		if (effect != null && effect instanceof Whack) {
			this.damageTaken += hit.getDamage();
			return;
		}
		super.handleIngoingHit(hit);
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public void setTarget(Entity entity) {
		if (isForceWalking() || getEffect() instanceof Whack)
			return;
		if (entity instanceof Familiar) {
			if (((Familiar) entity).getOwner() != null) {
				getCombat().setTarget(((Familiar) entity).getOwner());
				setLastAttackedByTarget(Utils.currentTimeMillis());
				return;
			}
		}
		getCombat().setTarget(entity);
		setLastAttackedByTarget(Utils.currentTimeMillis());
	}

	@Override
	public RoTSEffect generateEffect(Player target) {
		if (lastEffect == null) {
			final int randomEffect = Utils.random(3);
			switch(randomEffect) {
			case 0:
				return new Hurricane(10, this, null);
			case 1:
				if (getInstance().getPlayers().size() > 1) {
					int players = 0;
					final WorldTile center = instance.getWorldTile(34, 20);
					for (Player p : instance.getPlayers()) {
						if (p.getX() > center.getX() && getX() > center.getX() || p.getX() < center.getX() && getX() < center.getX())
							players++;
					}
					if (players > 1)
						return new Whack(1, this, target);
				}
				return Utils.randomBool() ? new Hurricane(10, this, null) : new WallSlam(10, this, null);
			default:
				return new WallSlam(10, this, null);
			}
		}
		if (lastEffect instanceof WallSlam)
			return new Hurricane(10, this, null);
		else if (lastEffect instanceof Hurricane) {
			if (getInstance().getPlayers().size() > 1) {
				int players = 0;
				final WorldTile center = instance.getWorldTile(34, 20);
				for (Player p : instance.getPlayers()) {
					if (p.getX() > center.getX() && getX() > center.getX() || p.getX() < center.getX() && getX() < center.getX())
						players++;
				}
				if (players > 1)
					return new Whack(1, this, target);
			}
			return new WallSlam(10, this, null);
		}
		return new WallSlam(10, this, null);
	}

}
