package com.rs.game.npc.others;

public class SecondaryBar {

	private final int beginningOffset;
    private final int totalUnits;
    private final int incrementalUnits;
	private final boolean permanent;

	public SecondaryBar(int beginningOffset, int totalUnits,
			int incrementalUnits, boolean permanent) {
		this.beginningOffset = beginningOffset;
		this.totalUnits = totalUnits;
		this.incrementalUnits = incrementalUnits;
		this.permanent = permanent;
	}

	public int getBeginningOffset() {
		return beginningOffset;
	}

	public int getTotalUnits() {
		return totalUnits;
	}

	public int getIncrementalUnits() {
		return incrementalUnits;
	}

	public boolean isPermenant() {
		return permanent;
	}
}