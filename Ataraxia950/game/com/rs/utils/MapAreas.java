package com.rs.utils;

import com.rs.game.WorldTile;

import java.util.HashMap;

public final class MapAreas {

	private final static HashMap<Integer, int[]> mapAreas = new HashMap<Integer, int[]>();
	private final static Object lock = new Object();

	public static final boolean isAtArea(String areaName, WorldTile tile) {
		return isAtArea(Utils.getNameHash(areaName), tile);
	}

	public static final boolean isAtArea(int areaNameHash, WorldTile tile) {
		int[] coordsList = mapAreas.get(areaNameHash);
		if (coordsList == null)
			return false;
		int index = 0;
		while (index < coordsList.length) {
			if (tile.getPlane() == coordsList[index] && tile.getX() >= coordsList[index + 1]
					&& tile.getX() <= coordsList[index + 2] && tile.getY() >= coordsList[index + 3]
					&& tile.getY() <= coordsList[index + 4])
				return true;
			index += 5;
		}
		return false;
	}

	public static final void removeArea(int areaNameHash) {
		mapAreas.remove(areaNameHash);
	}

	public static final void addArea(int areaNameHash, int[] coordsList) {
		mapAreas.put(areaNameHash, coordsList);
	}

	public static final int getRandomAreaHash() {
		synchronized (lock) {
			while (true) {
				long id = Utils.getRandom(Integer.MAX_VALUE) + Utils.getRandom(Integer.MAX_VALUE);
				id -= Integer.MIN_VALUE;
				if (id != -1 && !mapAreas.containsKey((int) id))
					return (int) id;
			}
		}
	}
}