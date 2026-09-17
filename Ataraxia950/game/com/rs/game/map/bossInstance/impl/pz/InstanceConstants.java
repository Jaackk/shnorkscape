package com.rs.game.map.bossInstance.impl.pz;

import com.rs.game.WorldTile;

/**
 * @author Kris | 9. sept 2018 : 21:54:26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class InstanceConstants {

	/**
	 * Respawn delay in ticks per instanced NPC.
	 */
	public static final int RESPAWN_DELAY = 15;

	public static final WorldTile[] MONO_SPAWN = new WorldTile[] { new WorldTile(3039, 6050, 0) };
	
	public static final WorldTile[] DI_SPAWN = new WorldTile[] { new WorldTile(3035, 6050, 0), new WorldTile(3040, 6050, 0) };

	public static final WorldTile[] TRI_SPAWN = new WorldTile[] { new WorldTile(3034, 6050, 0), new WorldTile(3038, 6050, 0), new WorldTile(3042, 6050, 0)};

	public static final WorldTile[] TETRA_SPAWN = new WorldTile[] {
			new WorldTile(3035, 6050, 0), new WorldTile(3041, 6050, 0),
			new WorldTile(3035, 6042, 0), new WorldTile(3041, 6042, 0)
	};
	
	public static final WorldTile[] HEXA_SPAWN = new WorldTile[] {
			new WorldTile(3035, 6050, 0), new WorldTile(3041, 6050, 0), new WorldTile(3038, 6050, 0),
			new WorldTile(3035, 6042, 0), new WorldTile(3041, 6042, 0), new WorldTile(3038, 6042, 0)
	};
	public static final WorldTile[] DECCA_SPAWN = new WorldTile[] {
			new WorldTile(3035, 6050, 0), new WorldTile(3041, 6050, 0), new WorldTile(3038, 6050, 0),
			new WorldTile(3035, 6046, 0), new WorldTile(3041, 6046, 0), new WorldTile(3038, 6046, 0),
			new WorldTile(3037, 6050, 0), new WorldTile(3039, 6050, 0), new WorldTile(3040, 6050, 0)
	};
	
	

}
