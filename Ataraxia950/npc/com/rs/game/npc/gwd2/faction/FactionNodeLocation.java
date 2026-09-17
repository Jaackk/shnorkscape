package com.rs.game.npc.gwd2.faction;

import com.rs.game.WorldTile;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.utils.Utils;

public enum FactionNodeLocation {

	NORTH_WESTERN_NODE(new WorldTile(3140, 6995, 1), HeartOfGielinor.ZAMORAK),
	NORTHERN_NODE(new WorldTile(3203, 7035, 1), HeartOfGielinor.ZAMORAK),
	NORTH_EASTERN_NODE(new WorldTile(3260, 7003, 1), HeartOfGielinor.SLISKE),
	EASTERN_NODE(new WorldTile(3220, 6987, 1), HeartOfGielinor.SLISKE),
	SOUTH_EASTER_NODE(new WorldTile(3259, 6955, 1), HeartOfGielinor.SEREN),
	SOUTHERN_NODE(new WorldTile(3196, 6899, 1), HeartOfGielinor.SEREN),
	SOUTH_WESTERN_NODE(new WorldTile(3140, 6948, 1), HeartOfGielinor.ZAROS),
	WESTERN_NODE(new WorldTile(3172, 6979, 1), HeartOfGielinor.ZAROS);
	
	private final WorldTile tile;
	private final int defaultType;
	
	FactionNodeLocation(final WorldTile tile, final int defaultType) {
		this.tile = tile;
		this.defaultType = defaultType;
	}
	
	public final int getDefaultType() {
		return defaultType;
	}
	
	public final WorldTile getTile() {
		return tile;
	}
	
	@Override
	public final String toString() {
		return Utils.formatString(name().replaceAll("_", " "));
	}
}
