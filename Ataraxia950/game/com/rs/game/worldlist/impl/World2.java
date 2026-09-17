package com.rs.game.worldlist.impl;

import com.rs.game.worldlist.WorldEntry;

public class World2 extends WorldEntry {

	@Override
	public int getWorldId() {
		return 2;
	}

	@Override
	public String getWorldAddress() {
		return "127.0.0.1";
	}

	@Override
	public String getWorldActivity() {
		return "Ataraxia Developer World";
	}

	@Override
	public int getCountryId() {
		return 144;
	}

	@Override
	public boolean isMembersOnly() {
		return true;
	}

	@Override
	public boolean isLootshareEnabled() {
		return true;
	}

}
