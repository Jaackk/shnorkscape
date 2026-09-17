package com.rs.game.worldlist;

import com.rs.game.worldlist.impl.World1;
import com.rs.game.worldlist.impl.World2;

import java.util.ArrayList;
import java.util.List;

public class WorldRepository {

	private static final List<WorldEntry> worlds = new ArrayList<WorldEntry>();

	public static void startWorlds() {
		worlds.add(new World1());
		worlds.add(new World2());
	}

	public static void add(WorldEntry e) {
		worlds.add(e);
	}

	public static List<WorldEntry> getWorlds() {
		return worlds;
	}

	public static WorldEntry getWorld(int worldId) {
		return worlds.get(worldId - 1);
	}

}
