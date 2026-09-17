package com.rs.game.activities.rots;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.06
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class ShadowPit extends WorldObject {

	private static final long serialVersionUID = -5769088600898909652L;

	public ShadowPit(int id, int type, int rotation, WorldTile tile) {
		super(id, type, rotation, tile);
		World.spawnObject(this);
	}
	
	/**
	 * Spawns a pit for a random amount of ticks between 80 & 150, after which it returns to normal.
	 */
	private int ticks = Utils.random(80, 150);
	
	public boolean process() {
		if (ticks-- <= 0) {
			World.spawnObject(new WorldObject(88092, getType(), getRotation(), getX(), getY(), getPlane()));
			return false;
		}
		return true;
	}
	
}
