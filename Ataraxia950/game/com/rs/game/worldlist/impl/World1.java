package com.rs.game.worldlist.impl;

import com.rs.game.worldlist.WorldEntry;

public class World1 extends WorldEntry {

	@Override
	public int getWorldId() {
		return 1;
	}

	@Override
	public String getWorldAddress() {
		return "127.0.0.1";
	}

	@Override
	public String getWorldActivity() {
		return "Ataraxia Main";
	}

	@Override
	public int getCountryId() {
		return 1;
	}

	@Override
	public boolean isMembersOnly() {
		return false;
	}

	@Override
	public boolean isLootshareEnabled() {
		return true;
	}

}
