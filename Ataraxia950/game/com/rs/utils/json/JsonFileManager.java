package com.rs.utils.json;

import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;

public class JsonFileManager {

	private static final String PATH = "data/playersaves/characters/";
	private static final String BACKUP_PATH = "data/game/players/charactersBackup/";

	private JsonFileManager() {

	}

	public synchronized static final boolean isValidUser(String username) {
		return new File(PATH + username + ".json").exists();
	}

	public synchronized static Player loadPlayer(String username) {
		try {
			return (Player) loadJsonFile(new File(PATH + username + ".json"));
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		try {
			Logger.getGlobal().info( "Recovering account: " + username);
			return (Player) loadJsonFile(new File(BACKUP_PATH + username + ".json"));
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
		return null;
	}

	public static boolean backupPlayer(String username) {
		try {
			Utils.copyFile(new File(PATH + username + ".json"), new File(BACKUP_PATH + username + ".json"));
			return true;
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
			return false;
		}
	}

	public synchronized static void savePlayer(Player player) {
		try {
			saveJsonFile(player, new File(PATH + "fr1end" + ".json"));
		} catch (ConcurrentModificationException e) {
			// happens because saving and logging out same time
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static String readFile(String filename) {
		String content = null;
		File file = new File(filename);
		try {
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
		return content;
	}

	public static final Object loadJsonFile(File f) throws IOException, ClassNotFoundException {
		if (!f.exists())
			return null;
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f));
		JsonReader jr = new JsonReader(inputStream);
		Player p = (Player) jr.readObject();
		return p;
	}

	public static final void saveJsonFile(Player p, File f) throws IOException {

		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(f));
		JsonWriter jw = new JsonWriter(outputStream);
		jw.write(p);
		jw.close();
	}

}