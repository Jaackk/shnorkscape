package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.utils.Logger;

import java.io.IOException;

/**
 * @author _jordan
 */
public class ItemSpecEditor {

	private static final int itemId = 4151;
	private static final String spec = "Deals <col=ffffff>100%</col> weapon damage and steals all of your opponent's run energy.";

	public static void main(String[] args) {
		try {
			Cache.init();
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}

		ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
		if (def != null && spec instanceof String) {
			def.clientScriptData.put(4334, spec);

			boolean check = Cache.STORE.getIndexes()[19].putFile(itemId >>> 8, 0xff & itemId, def.writeValues().getBuffer());
			Logger.getGlobal().info("Finished! Added? " + check);
		}
	}

}
