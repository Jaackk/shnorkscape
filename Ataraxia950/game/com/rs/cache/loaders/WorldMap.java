package com.rs.cache.loaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.ArchiveReference;
import com.rs.cache.filestore.store.FileReference;
import com.rs.cache.filestore.store.Index;
import com.rs.game.WorldTile;
import com.rs.network.codec.ProtocolSet;
import com.rs.network.io.InputStream;

public class WorldMap {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		Cache.init();
		File file = new File("coordsList.txt"); // = new
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.append("//Version = "+ ProtocolSet.REVISION);
		writer.newLine();
		writer.flush();

		for (int i : Cache.STORE.getIndexes()[23].getTable().getArchives()[0].getValidFileIds()) {
			// byte[] data = Cache.STORE.getIndexes()[42].getFile(0, i);
			byte[] data = Cache.STORE.getIndexes()[23].getFile(0, i);

			InputStream stream = new InputStream(data);

			String mapName = stream.readString();
			String areaName = stream.readString();

			WorldTile tile = new WorldTile(stream.readInt());

			int unknown1 = stream.readInt();
			boolean useMap = stream.readUnsignedByte() == 1;
			int unknown2 = stream.readUnsignedByte();
			int unknown3 = stream.readUnsignedByte();
			// File("information/itemlist.txt");

			writer.append("Map: " + mapName + " | Area: " + areaName + " -  Coords: " + tile);
			writer.newLine();
			writer.flush();

			System.out.println(i + ", map: " + mapName + ", area: " + areaName + ", coords: " + tile);
		}
		writer.close();
		// System.out.println(Cache.STORE.getIndexes()[23].getLastArchiveId());
	}

	public static void mainx3(String[] args) throws IOException {
		Cache.init();
		byte[] dataS1 = { 39, 15, -128, -110, -128, -106, -128, -92, 90, 13, 96, 103, -128, -107, 124, -128, -93, 101,
				64, 53, -128, -111, -128, -94, 94, 7, 10, 125, -128, -108, 67, 37, -128, -109, 79, 91, 63, 98, 81, 65,
				56, 49, 80, 23, 97, 123, 114, 39, 36, 17, -128, -61, 42, 114, -128, -106, 85, 120, 75, 123, -128, -104,
				59, 112, 64, -128, -95, 92, 56, 15, 61, 0, 48, -104, -1, -1, -1, -1, -1, -1, -1, -1, -8, -8, -8, -8, -8,
				-8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
				-8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
				-8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
				-8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
				-8, -8 };
		for (int index = 41; index < 52; index++) {
			if (Cache.STORE.getIndexes()[index] == null)
				continue;
			System.out.println("checking index " + index);
			for (int arcId : Cache.STORE.getIndexes()[index].getTable().getValidArchiveIds()) {
				if (Cache.STORE.getIndexes()[index].getTable().getArchives()[arcId] == null)
					continue;
				for (int fileId : Cache.STORE.getIndexes()[index].getTable().getArchives()[arcId].getValidFileIds()) {
					byte[] file = Cache.STORE.getIndexes()[index].getFile(arcId, fileId);
					if (file == null || file.length < dataS1.length)
						continue;

					byte[] data = new byte[dataS1.length];
					for (int i = 0; i < data.length; i++)
						data[i] = file.length >= i ? 0 : file[i];
					if (Arrays.equals(dataS1, data)) {
						System.out.println("index=" + index + ",arcId=" + arcId + ",fileId=" + fileId);
					}
				}
			}
		}
	}

	public static void main5(String[] args) throws IOException {
		Cache.init();
		Index index = Cache.STORE.getIndexes()[23];
		int archiveId = 3;
		int[] validFileIds = index.getTable().getArchives()[archiveId].getValidFileIds();

		if (validFileIds != null) {
			for (int i = 0; i < validFileIds.length; i++) {
				InputStream stream = new InputStream(index.getFile(archiveId, validFileIds[i]));
				ArchiveReference archiveRefrences = index.getTable().getArchives()[archiveId];
				FileReference fileReference = index.getTable().getArchives()[archiveId].getFiles()[validFileIds[i]];
				System.out.println("Start:- ArchiveId = " + archiveId + ", FileId=" + validFileIds[i]
						+ ", archiveNameHash=" + archiveRefrences.getNameHash() + " fileNameHash="
						+ fileReference.getNameHash() + ", Data=");
				System.out.println("Map Name=" + stream.readString());// MapName
				System.out.println("Area Name=" + stream.readString());// AreaName
				WorldTile tile = new WorldTile(stream.readInt());
				System.out.println("Tile=" + tile);
				System.out.println(stream.readInt());
				boolean useMap = stream.readUnsignedByte() == 1;
				System.out.println("useMap=" + useMap);
				System.out.println(stream.readUnsignedByte());
				System.out.println(stream.readUnsignedByte());
				int length = stream.readUnsignedByte();
				System.out.println("Length=" + length);
				for (int j = 0; j < length; j++) {
					System.out.println(stream.readUnsignedByte());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
					System.out.println(stream.readUnsignedShort());
				}
				System.out.println("Finish!");
				System.out.println();
			}
		}
	}

	public static int getDetailsArchiveId() {
		return Cache.STORE.getIndexes()[23].getArchiveId("details");
	}

}
