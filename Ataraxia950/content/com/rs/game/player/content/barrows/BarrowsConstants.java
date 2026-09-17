package com.rs.game.player.content.barrows;

import com.rs.game.WorldTile;

public enum BarrowsConstants {
	
	AHRIM(new WorldTile(3564, 3287, 0), new WorldTile(3546, 9590, 0), new WorldTile(3546, 9586, 0), 66017, 2025),
	DHAROK(new WorldTile(3573, 3296, 0), new WorldTile(3556, 9718, 3), new WorldTile(3555, 9716, 3), 63177, 2026),
	GUTHAN(new WorldTile(3574, 3279, 0), new WorldTile(3534, 9704, 3), new WorldTile(3539, 9702, 3), 66020, 2027), 
	KARIL(new WorldTile(3563, 3276, 0), new WorldTile(3546, 9684, 3), new WorldTile(3549, 9683, 3), 66018, 2028), 
	TORAG(new WorldTile(3553, 3281, 0), new WorldTile(3568, 9683, 3), new WorldTile(3571, 9686, 3), 66019, 2029), 
	VERAC(new WorldTile(3556, 3296, 0), new WorldTile(4077, 5710, 0), new WorldTile(4074, 5710, 0), 66016, 2030),
	AKRISAE(null, null, new WorldTile(4073, 5723, 0), 61189, 14297),
	LINZA(null, null, new WorldTile(3532, 9588, 0), 103163, 22721);

	
	private final WorldTile out, in, bySarcophagus;
	private final int sarcophagusId, npcId;
	BarrowsConstants(final WorldTile out, final WorldTile in, final WorldTile bySarcophagus, final int sarcophagusId, final int npcId) {
		this.out = out;
		this.in = in;
		this.bySarcophagus = bySarcophagus;
		this.sarcophagusId = sarcophagusId;
		this.npcId = npcId;
	}
	
	public final WorldTile getOutsideCoordinates() {
		return out;
	}
	
	public final WorldTile getInsideCoordinates() {
		return in;
	}
	
	public final WorldTile getBySarcophagusCoordinates() {
		return bySarcophagus;
	}
	
	public final int getSaprcophagusId() {
		return sarcophagusId;
	}
	
	public final int getNpcId() {
		return npcId;
	}
	
	public final int getStaircaseId() {
		return 6702 + ordinal();
	}
}
