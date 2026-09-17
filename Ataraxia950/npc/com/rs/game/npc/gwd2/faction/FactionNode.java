package com.rs.game.npc.gwd2.faction;

import com.rs.game.WorldTile;

public final class FactionNode {

	private int type;
	private final WorldTile tile;
	private final String name;
	
	public FactionNode(final WorldTile tile, final int defaultType, final String name) {
		this.tile = tile;
		this.type = defaultType;
		this.name = name;
	}
	
	/**
	 * The center of the node.
	 * @return center WorldTile.
	 */
	public WorldTile getTile() {
		return tile;
	}
	
	/**
	 * The type of the faction currently owning the node.
	 * @return type.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Sets the owner of the node to a new faction.
	 * @param type faction type.
	 */
	public void setType(final int type) {
		this.type = type;
	}
	

	/**
	 * Gets the name of the Node.
	 * @return String name
	 */
	public String getName() {
		return name;
	}
	
}
