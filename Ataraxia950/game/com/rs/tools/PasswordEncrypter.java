package com.rs.tools;

import com.rs.game.player.Player;
import com.rs.utils.Encrypt;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.io.File;

public class PasswordEncrypter {

	public static void encrypt() {
		File[] chars = new File("checkacc/").listFiles();
		for (File acc : chars) {
			try {
				Player player = (Player) SerializableFilesManager.loadSerializedFile(acc);
				if (player == null || player.getPassword() == null)
					continue;
				Logger.getGlobal().info(player.getPassword());
				player.setPassword(Encrypt.encryptSHA1(player.getPassword()));
				Logger.getGlobal().info(player.getPassword());
				SerializableFilesManager.storeSerializableClass(player, acc);
			} catch (Throwable e) {
				Logger.getGlobal().info("failed: " + acc.getName() + ", " + e);
			}
		}
	}

	public static void main(String[] args) {
		encrypt();
	}
}