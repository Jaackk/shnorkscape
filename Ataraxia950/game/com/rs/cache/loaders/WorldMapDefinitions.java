package com.rs.cache.loaders;

import java.io.IOException;

import com.rs.cache.Cache;
import com.rs.cache.filestore.io.InputStream;
import com.rs.game.WorldTile;
import com.rs.utils.Logger;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 28, 2018.
 */
public class WorldMapDefinitions {

	public static void main(String[] args) throws IOException {
		Cache.init();

		for (int x = 0; x <= 60; x++) {
			for (int i : Cache.STORE.getIndexes()[23].getTable().getArchives()[x].getValidFileIds()) {
				byte[] data = Cache.STORE.getIndexes()[23].getFile(Cache.STORE.getIndexes()[23].getArchiveId("details"), i);
				// byte[] data2 = Cache.STORE.getIndexes()[23].getFile(1, i);

				InputStream stream = new InputStream(data);

				String mapName = stream.readString();
				String areaName = stream.readString();

				WorldTile tile = new WorldTile(stream.readInt());

				stream.readInt();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();

				// File("information/itemlist.txt");

//			writer.append("Map: " + mapName + " | Area: " + areaName
//					+ " -  Coords: " + tile.toString());
//			writer.newLine();
//			writer.flush();

				Logger.getGlobal().info(i + ", map: " + mapName + ", area: " + areaName + ", coords: " + tile);
			}
			Logger.getGlobal().info(Cache.STORE.getIndexes()[23].getLastArchiveId());
		}
	}

}
