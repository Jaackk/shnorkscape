package com.rs.game.npc.glacor;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.utils.Utils;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Glacor extends Glacyte {

	private List<Glacyte> glacites;
	private boolean rangeAttack;

	public Glacor(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea) {
		super(null, id, tile, tile.getRegionHash(), canBeAttackFromOutOfArea);
		setCapDamage(2500);
		setEffect((byte) -1);
		setGlacor(this);
		setSpawned(false);
		setCanBeAttackFromOutOfArea(true);
		setNoDistanceCheck(true);
		setRandomWalk(2);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (glacites == null) {
			if ((getHitpoints() - hit.getDamage()) <= 2500) {
				glacites = new ArrayList<Glacyte>(2);
				createGlacites();
			}
		} else if (glacites.size() != 0) {
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}

	private void createGlacites() {
		for (int index = 0; index < 3; index++) {
			tileLoop: for (int tileAttempt = 0; tileAttempt < 10; tileAttempt++) {
				final WorldTile tile = new WorldTile(this, 2);
				if (World.isTileFree(0, tile.getX(), tile.getY(), 1)) {
					glacites.add(new Glacyte(this, 14302 + index, tile, -1, true));
					break tileLoop;
				}
			}
		}
	}
	
	@Override
	public void processNPC() {
		if (isDead() || isLocked()) {
			return;
		}
		loadNPCSettings();
		if (!getCombat().process()) {
			if (getTargetIndex() != -1) {
				setTargetIndex(-1);
			}
			if (glacites != null && !glacites.isEmpty()) {
				for (val glacite : glacites) {
					glacite.finish();
				}
				resetMinions();
				this.reset();
			}
			if (getHitpoints() > getMaxHitpoints()) {
				setHitpoints(getMaxHitpoints());
			}
			if (!isForceWalking()) {
				if (!isCantInteract()) {
					if (!checkAgressivity()) {
						if (getFreezeDelay() < Utils.currentTimeMillis()) {
								boolean can = false;
								for (int i = 0; i < 2; i++) {
									if (Math.random() * 1000.0 < 100.0) {
										can = true;
										break;
									}
								}
								if (can) {
									final int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
									final int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
									resetWalkSteps();
									if (this.getRegionId() == getRespawnTile().getRegionId()) {
										addWalkSteps(getX() + moveX, getY() + moveY, 5, true);
									} else {
										addWalkSteps(getRespawnTile().getX() + moveX, getRespawnTile().getY() + moveY, 5, true);
									}
								}
						}
					}
				}
			}
		}
		if (isForceWalking()) {
			if (getFreezeDelay() < Utils.currentTimeMillis()) {
				if (getX() != getForceWalk().getX() || getY() != getForceWalk().getY()) {
					if (!hasWalkSteps()) {
						final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(getForceWalk().getX(), getForceWalk().getY()), true);
						final int[] bufferX = RouteFinder.getLastPathBufferX();
						final int[] bufferY = RouteFinder.getLastPathBufferY();
						for (int i = steps - 1; i >= 0; i--) {
							if (!addWalkSteps(bufferX[i], bufferY[i], 25, true)) {
								break;
							}
						}
					}
					if (!hasWalkSteps()) {
						setNextWorldTile(new WorldTile(getForceWalk()));
						setForceWalk(null);
					}
				} else {
					setForceWalk(null);
				}
			}
		}
	}

	public void verifyGlaciteEffect(final Glacyte glacite) {
		if (glacites.size() == 1) {
			setEffect(glacites.get(0).getEffect());
		}
		glacites.remove(glacite);
	}

	@Override
	public void sendDeath(final Entity killer) {
		super.sendDeath(killer);
		glacites = null;
	}

	public boolean isRangeAttack() {
		return rangeAttack;
	}

	public void setRangeAttack(final boolean rangeAttack) {
		this.rangeAttack = rangeAttack;
	}

	public void resetMinions() {
		glacites = null;
		setEffect((byte) -1);
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
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }
    
    
}
