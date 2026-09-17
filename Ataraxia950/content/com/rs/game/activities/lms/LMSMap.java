package com.rs.game.activities.lms;

import java.util.HashMap;
import java.util.Map;

public class LMSMap {
	
	public enum MapBlocks {
		ENTRY1("name2"),
		ENTRY2("name");
		
		private static final Map<String, MapBlocks> blocks = new HashMap<String, MapBlocks>();

		static {
			for (MapBlocks block : MapBlocks.values())
				blocks.put(block.getName(), block);
		}

		private final String name;

		MapBlocks(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
	}
}
