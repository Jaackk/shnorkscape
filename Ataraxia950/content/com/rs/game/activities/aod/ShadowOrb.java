package com.rs.game.activities.aod;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

public class ShadowOrb extends WorldObject {

	private static final long serialVersionUID = 5709890164651375163L;

	public ShadowOrb(WorldTile tile, final AngelOfDeath aod) {
		super(100809, 10, 0, tile);
		World.spawnObject(this);
		this.aod = aod;
	}
	
	private final AngelOfDeath aod;
	private int ticks = 500;
	private int amount = 1;
	
	/**
	 * Processes the orb object. Process method is being called every tick from the instance class.
	 * Any players standing on an orb will be hit for between 300 and 500 unblockable damage.
	 * By default, the duration of the orb is 3 minutes. The amount lowers if there are
	 * any other orbs existing on the x or y axis of the orb itself, lowering
	 * the duration of the orb by the amount of other orbs.
	 * @return whether process was successful or orb needs removing from the list.
	 */
	public boolean process() {
		ticks -= amount;
		aod.getPlayers().forEach(p -> {
			if (p.getTileHash() == getTileHash())
				p.applyHit(new Hit(null, Utils.random(88, 144), HitLook.REGULAR_DAMAGE));
		});
		if (ticks <= 0) {
			World.removeObject(this);
			return false;
		}
		return true;
	}
	
	/**
	 * Adds an additional orb if one was spawned on the x or y axis of this orb. 
	 * Determined when spawning new orbs.
	 */
	public void incrementAmount() {
		amount++;
	}

}
