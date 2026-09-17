package com.rs.game.npc.glacor;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Glacyte extends NPC {

	private Glacor glacor;
	private byte effect, explosionTicks;
	private final boolean isGlacior;
	private int targetIndex;

	public Glacyte(final Glacor glacor, final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
		setCapDamage(9000);
		setForceMultiArea(true);
		setForceMultiAttacked(true);
		setNoDistanceCheck(true);
		this.glacor = glacor;
		isGlacior = id == 14301;
		if (!isGlacior) {
			effect = (byte) (id - 14302);
			getCombat().setTarget(glacor.getCombat().getTarget());
		}
		explosionTicks = 25;
		setTargetIndex(-1);
	}

	@Override
	public void sendDeath(final Entity killer) {
		super.sendDeath(killer);
		setTargetIndex(-1);
		if (!isGlacior) {
			glacor.verifyGlaciteEffect(this);
		}
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getSource() != null) {
			if (getTargetIndex() == -1) {
				setTargetIndex(hit.getSource().getIndex());
			}
		}
		int damage = hit.getDamage();
		if (effect == 2) {
			final Entity target = glacor != null ? glacor.getCombat().getTarget() : getCombat().getTarget();
			if (damage > 0) {
				damage = (int) ((((6 - Utils.getDistance(target, glacor)) / 10) + .4D) * damage);
			}
			hit.setDamage(damage);
		}
		super.handleIngoingHit(hit);
	}

	/**
	 * Effects go as this, 0 - explosion, 1 - sap prayer , 2 - endurance
	 */
	@Override
	public void processNPC() {
		super.processNPC();
		final Entity target = glacor != null ? glacor.getCombat().getTarget() : getCombat().getTarget();
		if (glacor == null || target == null || target.isDead() || glacor.isDead() || isDead()) {
			explosionTicks = 0;
			setTargetIndex(-1);
			if (!isGlacior) {
				finish();
			} else {
				if (!glacor.isDead() || !isDead()) {
					reset();
				}
				glacor.resetMinions();
			}
			return;
		}
		if (effect == 0) {
			explosionTicks--;
			if (explosionTicks <= 0) {
				explosionTicks = 25;
				final WorldTile tile = new WorldTile(this);
				for (final Entity e : getPossibleTargets()) {
					if (e == null || e.isDead() || !e.withinDistance(tile, isGlacior ? 3 : 1)) {
						continue;
					}
					e.applyHit(new Hit(target, e.getHitpoints() / 3, HitLook.REGULAR_DAMAGE));
				}
				applyHit(new Hit(target, (int) (getHitpoints() * .9), HitLook.REFLECTED_DAMAGE));
				setNextGraphics(new Graphics(956));
			} else {
				if (explosionTicks >= 20) {
					// just so it can delay healing a little
				} else if (explosionTicks >= 13) {
					heal((int) (getMaxHitpoints() * .05));
				}
			}
		}
	}

	public byte getEffect() {
		return effect;
	}

	public void setEffect(final byte effect) {
		this.effect = effect;
	}

	public Glacyte getGlacor() {
		return glacor;
	}

	public void setGlacor(final Glacor glacor) {
		this.glacor = glacor;
	}

	public int getTargetIndex() {
		return targetIndex;
	}

	public void setTargetIndex(final int targetIndex) {
		this.targetIndex = targetIndex;
	}
}
