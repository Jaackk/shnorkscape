package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.io.File;
import java.io.IOException;

public class DeserializationTool {

	public static void main(String[] args) throws IOException {
		Cache.init();
		File[] chars = new File("data/playersaves/characters").listFiles();
		for (File acc : chars) {
			try {
				Player target = (Player) SerializableFilesManager.loadSerializedFile(acc);
				target.setFamiliar(null);
				SerializableFilesManager.storeSerializableClass(target, acc);
			} catch (Exception e) {
				Logger.getGlobal().info("ERROR: "+e);
				Logger.getGlobal().info("Not a Player file: "+acc.getName());
				continue;
			}

		}
	}
}
