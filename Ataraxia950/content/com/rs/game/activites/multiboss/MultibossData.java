package com.rs.game.activites.multiboss;

import com.rs.game.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Noel Usage: Superclass for creating multi-boss events For Ataraxian
 *         use only! <3
 */

public class MultibossData {

	/*
	 * Section of useful information that can be applied to this minigame!
	 * 
	 * Coords: 1902 6046 - elevator 2037 6048 - guthixian place with many spawn
	 * rooms 1632 6176 - spellcasting chamber ariane 1928 5987 - lower level,
	 * end, next to guthix, altar 2195 4257 1 - weird island area
	 * 
	 * Items:
	 * 
	 * NPCs:
	 * 
	 */

	public Events event;

	/**
	 * The events enum, containing all multi-boss events
	 */
	public enum Events {
		FIVE_KINGS("The Five Kings", new int[] { 16698, 2882, 2883, 50, 6247 },
				new Item[] { new Item(26587), new Item(25037), new Item(6740, 5) });

		/**
		 * This is a Map object, allowing us to store the data as (String,
		 * Events) Events is any one item in the Events enum, now we can search
		 * the enum by String
		 */
		private static final Map<String, Events> events = new HashMap<String, Events>();

		// The interface to place Events items by String, event
		static {
			for (Events event : Events.values())
				events.put(event.getName(), event);
		}

		private final String name;
		private final int[] bosses;
		private final Item[] rares;

		/**
		 * Contructor for Enum, which allows it us to perform various functions
		 * on each Events object by declaring (this) variables here dynamically
		 * for each entry
		 */
        Events(String name, int[] bosses, Item[] rares) {
			this.name = name;
			this.bosses = bosses;
			this.rares = rares;
		}

		/**
		 * When calling an instance of this, you can use getEvent(String) to
		 * call the name and fetch that item in the enum. It will return an
		 * Events object you can now call the designated functions upon.
		 */

		public static Events getEvent(String name) {
			return events.get(name);
		}

		public int[] getBosses() {
			return this.bosses;
		}

		public Item[] getRares() {
			return this.rares;
		}

		public String getName() {
			return this.name;
		}
	}
}