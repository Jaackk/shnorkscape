package com.rs.game.player.content;

import com.rs.game.WorldTile;

import java.util.LinkedHashMap;

public class WorldLocation {
	
	public enum Locations {
		/** Cities **/
		EDGEVILLE("Edgeville", new WorldTile(3087, 3496, 0)),
		KARAMJA("Karamja", new WorldTile(2908, 3152, 0)),
		DRAYNOR("Draynor", new WorldTile(3105, 3251, 0)),
		ALKHARID("Al Kharid", new WorldTile(3274, 3165,0)),
		LUMBY("Lumbridge", new WorldTile(3222, 3218, 0)),
		VARROCK("Varrock", new WorldTile(3217, 3426, 0)),
		FALADOR("Falador", new WorldTile(2965, 3379, 0)),
		CAMELOT("Camelot", new WorldTile(2758, 3478, 0)),
		ARDOUGNE("Ardougne", new WorldTile(2660, 3306, 0)),
		
		/** Sentinel woodcutting teleports */
		WEST_VARROCK_WC("Varrock west wc", new WorldTile(3137, 3402, 0)),
		EAST_VARROCK_WC("Varrock east wc", new WorldTile(3285, 3445, 0)),
		CATHERBY("Catherby", new WorldTile(2792, 3431, 0)),
		
		DRAYNOR_WILLOWS("Draynor willows", new WorldTile(3090, 3232, 0)),
		BARBARIAN_WILLOWS("Barbarian willows", new WorldTile(2519, 3571, 0)),
		
		SEERS_YEWS("Seers yews", new WorldTile(2711, 3462, 0)),
		EDGEVILLE_YEWS("Edgeville yews", new WorldTile(3087, 3475, 0)),
		VARROCK_YEWS("Varrock yews", new WorldTile(3213, 3502, 0)),
		
		VARROCK_SOUTH_IVY("Varrock south ivy", new WorldTile(3229, 3456, 0)),
		VARROCK_NORTH_IVY("Varrock north ivy", new WorldTile(3216, 3501, 0)),
		TAVERLY_IVY("Taverly ivy", new WorldTile(2937, 3429, 0)),
		YANILLE_IVY("Yanille ivy", new WorldTile(2592, 3114, 0)),
		CASTLE_WARS_IVY("Castle wars ivy", new WorldTile(2431, 3060, 0)),
		
		
		RANGING_GUILD("Ranging guild", new WorldTile(2694, 3425, 0)),
		MAGE_TRAINING_ARENA("Mage training arena", new WorldTile(3363, 3290, 0)),
		SORCERERS_TOWER("Sorcerers tower", new WorldTile(2702, 3398, 0)),
		TAI_BWO_WANNAI("Tai bwo wannai", new WorldTile(2819, 3084, 0)),
		CASTLE_WARS_TEAK("Castle wars teak", new WorldTile(2333, 3047, 0)),
		APE_ATOLL_TEAK("Ape atoll teak", new WorldTile(2773, 2696, 0)),
		APE_ATOLL_MAHOGANY("Ape atoll mahogany", new WorldTile(2718, 2722, 0));
		
		private static final LinkedHashMap<String, Locations> locations = new LinkedHashMap<String, Locations>();
	
		static {
			for (Locations location : Locations.values())
				locations.put(location.getName(), location);
		}

		private final String name;
		private final WorldTile tile;

		Locations(String name, WorldTile tile) {
			this.name = name;
			this.tile = tile;
		}
		
		public static Locations getLocation(String name) {
			return locations.get(name);
		}
		
		public String getName() {
			return this.name;
		}
		
		public WorldTile getTile() {
			return this.tile;
		}
	}
}
