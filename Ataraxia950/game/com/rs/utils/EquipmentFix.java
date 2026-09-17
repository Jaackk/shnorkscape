package com.rs.utils;

import com.rs.cache.Cache;
import com.rs.game.player.Player;

import java.io.File;
import java.io.IOException;

public class EquipmentFix {

	public static void main(String... args) throws IOException {
		Cache.init();
		File[] chars = new File("data/playersaves/characters").listFiles();
		for (File acc : chars) {
			try {
				Player target = (Player) SerializableFilesManager.loadSerializedFile(acc);
				target.getEquipment().resetEquipmentSlots();
				SerializableFilesManager.storeSerializableClass(target, acc);
			} catch (Exception e) {
				Logger.getGlobal().info("Failed to fix character: "+acc.getName());
				continue;
			}
		}
	}
}