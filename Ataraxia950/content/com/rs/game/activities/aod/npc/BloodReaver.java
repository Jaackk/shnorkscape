package com.rs.game.activities.aod.npc;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.npc.NPC;
import com.rs.utils.Utils;

/**
 * Handles the blood reaver spawned by Nex during a specific ability. Reavers will not attack players
 * but instead, they will however walk towards nex after 3 ticks has passed.
 * If they reach nex, nex will heal, they will die and the instance will receive a punishing attack.
 * @author Kris | 30. sept 2017 : 17:17.16
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class BloodReaver extends NPC {

	private static final long serialVersionUID = -6727652551320803745L;
	private final AngelOfDeath aod;
	private int delay = 6;
	
	public BloodReaver(final WorldTile tile, final AngelOfDeath aod) {
		super(24009, tile, -1, true, true);
		this.aod = aod;
		setCannotMove(true);
		setForceMultiArea(true);
	}

	@Override
	public boolean isIntelligentRouteFinder() {
		return true;
	}

	@Override
	public void processNPC() {
		if (delay-- > 0 || isDead() || hasFinished())
			return;
		if (this.getFreezeDelay() > Utils.currentTimeMillis()) {
			if (this.hasWalkSteps())
				this.resetWalkSteps();
			return;
		}
		if (withinDistance(aod.getNex().getMiddleWorldTile(), aod.getNex().getSize() - 1)) {
			final int hitpoints = getHitpoints();
			applyHit(new Hit(null, hitpoints, HitLook.REGULAR_DAMAGE));
			aod.getNex().applyHit(new Hit(null, hitpoints / 2, HitLook.HEALED_DAMAGE));
			aod.getNex().sendMessage("Taste the true power of a blood sacrifice.");
			aod.getPlayers().forEach(p -> p.applyHit(new Hit(null, (int) (hitpoints * 0.6), HitLook.REGULAR_DAMAGE)));
			aod.getNex().addEnrage(aod.getPlayers().size() * 2);
			return;
		}
		if (!hasWalkSteps()) {
			final WorldTile tile = new WorldTile(aod.getNex());
			addWalkSteps(tile.getX(), tile.getY(), -1, false);
		}
	}
	
	@Override
	public int getSize() {
		return 1;
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
	
}
