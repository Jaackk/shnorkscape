package com.rs.game.worldlist;

public abstract class WorldEntry {

	public static final int MAX_PLAYER_CAP = 2048;

	public abstract int getWorldId();

	public abstract String getWorldAddress();

	public abstract String getWorldActivity();

	public abstract int getCountryId();

	public abstract boolean isMembersOnly();

	public abstract boolean isLootshareEnabled();


}
