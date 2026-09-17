package com.rs.game.player.content.grandExchange;

import com.rs.utils.FileUtilities;
import com.rs.utils.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Arno
 */
public class UnlimitedGEReader {

	private final static String TXT_PATH = "data/grandExchange/unlimited named Items.txt";
	private static final Set<Integer> items = new HashSet<>();

	public static void init() {
		try {
			readToStoreCollection();
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
	}

	private static void readToStoreCollection() throws Exception {
		for (String lines : FileUtilities.readFile(TXT_PATH)) {
			String[] split = lines.split(" - ");
			items.add(Integer.parseInt(split[0]));
			// Logger.getGlobal().info("[GEReader] : Added item
			// #"+Integer.parseInt(lines)+ " to items<int>");
		}
		Logger.getGlobal().info("Initiated " + items.size() + " To Buy Make Sure To Pay %5 Over...");
	}

	public static Set<Integer> getLimitedItems() {
		return items;
	}

	public static void reloadLimiteditems() {
		try {
			items.clear();
			readToStoreCollection();
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static boolean itemIsUnlimited(int itemId) {
		return items.contains(itemId);
	}
}