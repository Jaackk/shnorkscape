package com.rs.game.activities.rots;

import com.rs.game.WorldTile;

/**
 * @author Kris | 3. sept 2017 : 23:33.30
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public enum RiseOfTheSixEscapeObject {

	/**
	 * TODO: Patch WB 1, 2 & 3. Check all stones and vines to make sure they're all in the middle of the bridge, not anywhere else
	 * due to the movement glitch.
	 */
	WB0("waterbridge", 0, new WorldTile(5, 10, 0), new WorldTile(5, 13, 0), new WorldTile(6, 10, 0), new WorldTile(6, 11, 0), new WorldTile(4, 11, 0), new WorldTile(8, 11, 0)),
	WB1("waterbridge", 1, new WorldTile(10, 8, 1), new WorldTile(13, 8, 1), new WorldTile(10, 9, 1), new WorldTile(11, 9, 1), new WorldTile(11, 7, 1), new WorldTile(11, 11, 1)),
	WB2("waterbridge", 2, new WorldTile(8, 5, 2), new WorldTile(8, 7, 2), new WorldTile(9, 1, 2), new WorldTile(9, 2, 2), new WorldTile(7, 4, 2), new WorldTile(11, 4, 2)),
	WB3("waterbridge", 3, new WorldTile(5, 5, 3), new WorldTile(2, 5, 3), new WorldTile(1, 6, 3), new WorldTile(2, 6, 3), new WorldTile(4, 4, 3), new WorldTile(4, 8, 3)),
	EB0("emptybridge", 0, new WorldTile(7, 9, 0), new WorldTile(7, 13, 0), new WorldTile(8, 9, 0), new WorldTile(8, 11, 0)),
	EB1("emptybridge", 1, new WorldTile(9, 6, 1), new WorldTile(13, 6, 1), new WorldTile(9, 7, 1), new WorldTile(11, 7, 1)),
	EB2("emptybridge", 2, new WorldTile(6, 6, 2), new WorldTile(6, 3, 2), new WorldTile(7, 2, 2), new WorldTile(7, 3, 2)),
	EB3("emptybridge", 3, new WorldTile(6, 7, 3), new WorldTile(2, 7, 3), new WorldTile(1, 8, 3), new WorldTile(2, 8, 3)),
	HALL20("hall2", 0, new WorldTile(9, 6, 1), new WorldTile(13, 6, 1), new WorldTile(9, 7, 1), new WorldTile(10, 7, 1), new WorldTile(11, 5, 0), new WorldTile(11, 9, 0)),
	HALL21("hall2", 1, new WorldTile(6, 6, 2), new WorldTile(6, 2, 2), new WorldTile(7, 11, 0), new WorldTile(7, 12, 0), new WorldTile(5, 4, 0), new WorldTile(9, 4, 0)),
	HALL22("hall2", 2, new WorldTile(6, 7, 3), new WorldTile(3, 7, 3), new WorldTile(2, 8, 1), new WorldTile(3, 8, 1), new WorldTile(4, 6, 0), new WorldTile(4, 10, 0)),
	HALL23("hall2", 3, new WorldTile(7, 9, 0), new WorldTile(7, 13, 0), new WorldTile(8, 9, 0), new WorldTile(8, 10, 0), new WorldTile(6, 11, 0), new WorldTile(10, 11, 0)),
	HALL30("hall3", 0, new WorldTile(10, 10, 1), new WorldTile(6, 10, 1), new WorldTile(6, 11, 1), new WorldTile(7, 11, 1)),
	HALL31("hall3", 1, new WorldTile(10, 5, 2), new WorldTile(10, 9, 2), new WorldTile(11, 5, 0), new WorldTile(11, 6, 0)),
	HALL32("hall3", 2, new WorldTile(5, 3, 3), new WorldTile(9, 3, 3), new WorldTile(5, 4, 1), new WorldTile(6, 4, 1)),
	HALL33("hall3", 3, new WorldTile(3, 10, 0), new WorldTile(3, 7, 0), new WorldTile(4, 6, 0), new WorldTile(4, 7, 0));

	private final String roomName;
	private final int roomRotation;
	private final WorldTile bridge, ledge, vine, pillar;
	private final WorldTile[] unclipTiles;
	
	RiseOfTheSixEscapeObject(String roomName, int roomRotation, WorldTile bridge, WorldTile ledge, WorldTile vine, WorldTile pillar, WorldTile... unclipTiles) {
		this.roomName = roomName;
		this.roomRotation = roomRotation;
		this.bridge = bridge;
		this.ledge = ledge;
		this.vine = vine;
		this.pillar = pillar;
		this.unclipTiles = unclipTiles;
	}
	
	public final String getRoomName() {
		return roomName;
	}
	
	public final int getRoomRotation() {
		return roomRotation;
	}
	
	public final WorldTile getBridgeCoordinates() {
		return bridge;
	}
	
	public final WorldTile getLedgeCoordinates() {
		return ledge;
	}
	
	public final WorldTile getVineCoordinates() {
		return vine;
	}
	
	public final WorldTile getPillarCoordinates() {
		return pillar;
	}
	
	/**
	 * List of tiles that will manually need to be unclipped.
	 * @return
	 */
	public final WorldTile[] getUnclipTiles() {
		return unclipTiles;
	}
}
